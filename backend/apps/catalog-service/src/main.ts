import "dotenv/config";
import cors from "cors";
import express from "express";
import { Redis } from "ioredis";
import { pool } from "@cookie-shop/shared";

const app = express();
app.use(cors());
app.use(express.json());

const redis = new Redis(process.env.REDIS_URL ?? "redis://localhost:6379");
const port = Number(process.env.CATALOG_SERVICE_PORT ?? 8082);
const cacheTtlSeconds = 60;

const repairMojibakeProducts = async () => {
  await pool.query(
    `UPDATE products
     SET title = CONVERT(BINARY CONVERT(title USING latin1) USING utf8mb4),
         description = CONVERT(BINARY CONVERT(description USING latin1) USING utf8mb4),
         ingredients = CONVERT(BINARY CONVERT(ingredients USING latin1) USING utf8mb4),
         calories = CONVERT(BINARY CONVERT(calories USING latin1) USING utf8mb4),
         story = CONVERT(BINARY CONVERT(story USING latin1) USING utf8mb4)
     WHERE title LIKE 'Ð%' OR description LIKE 'Ð%' OR ingredients LIKE 'Ð%' OR calories LIKE 'Ð%' OR story LIKE 'Ð%'`
  );
};

const toProduct = (row: Record<string, unknown>) => ({
  id: row.id,
  title: row.title,
  price: Number(row.price),
  imageUrl: row.image_url,
  description: row.description,
  ingredients: row.ingredients,
  calories: row.calories,
  story: row.story
});

app.get("/health", (_req, res) => {
  res.json({ service: "catalog-service", status: "ok" });
});

app.get("/ready", async (_req, res) => {
  try {
    await pool.query("SELECT 1");
    await redis.ping();
    return res.json({ service: "catalog-service", ready: true });
  } catch (error) {
    return res.status(503).json({ service: "catalog-service", ready: false, error: String(error) });
  }
});

app.get("/products", async (req, res) => {
  const search = (req.query.search as string | undefined)?.trim();
  const cacheKey = search ? `products:search:${search}` : "products:all";
  const cached = await redis.get(cacheKey);
  if (cached) {
    res.setHeader("X-Cache", "HIT");
    return res.json(JSON.parse(cached));
  }

  const [rows] = search
    ? await pool.query("SELECT * FROM products WHERE title LIKE ? ORDER BY id DESC", [`%${search}%`])
    : await pool.query("SELECT * FROM products ORDER BY id DESC");

  const products = (rows as Record<string, unknown>[]).map(toProduct);
  await redis.set(cacheKey, JSON.stringify(products), "EX", cacheTtlSeconds);
  res.setHeader("X-Cache", "MISS");
  return res.json(products);
});

app.get("/products/:id", async (req, res) => {
  const id = Number(req.params.id);
  const cacheKey = `product:${id}`;
  const cached = await redis.get(cacheKey);
  if (cached) {
    res.setHeader("X-Cache", "HIT");
    return res.json(JSON.parse(cached));
  }

  const [rows] = await pool.query("SELECT * FROM products WHERE id = ? LIMIT 1", [id]);
  const product = (rows as Record<string, unknown>[])[0];
  if (!product) {
    return res.status(404).json({ message: "Product not found" });
  }

  const payload = toProduct(product);
  await redis.set(cacheKey, JSON.stringify(payload), "EX", cacheTtlSeconds);
  res.setHeader("X-Cache", "MISS");
  return res.json(payload);
});

app.get("/favorites", async (req, res) => {
  const userId = Number(req.header("x-user-id"));
  if (!userId) {
    return res.status(401).json({ message: "Missing user id" });
  }

  const [rows] = await pool.query(
    `SELECT f.id, p.id as productId, p.title, p.price, p.image_url, p.description
     FROM favorites f
     JOIN products p ON p.id = f.product_id
     WHERE f.user_id = ?
     ORDER BY f.id DESC`,
    [userId]
  );

  const favorites = (rows as Record<string, unknown>[]).map((row) => ({
    id: row.id,
    product: {
      id: row.productId,
      title: row.title,
      price: Number(row.price),
      imageUrl: row.image_url,
      description: row.description
    }
  }));

  return res.json(favorites);
});

app.post("/favorites", async (req, res) => {
  const userId = Number(req.header("x-user-id"));
  const productId = Number(req.body.productId);
  if (!userId) {
    return res.status(401).json({ message: "Missing user id" });
  }
  if (!productId) {
    return res.status(400).json({ message: "Invalid product id" });
  }

  await pool.query("INSERT IGNORE INTO favorites(user_id, product_id) VALUES(?, ?)", [userId, productId]);
  const [rows] = await pool.query(
    `SELECT f.id, p.id as productId, p.title, p.price, p.image_url, p.description
     FROM favorites f
     JOIN products p ON p.id = f.product_id
     WHERE f.user_id = ? AND f.product_id = ?
     LIMIT 1`,
    [userId, productId]
  );

  const row = (rows as Record<string, unknown>[])[0];
  return res.status(201).json({
    id: row.id,
    product: {
      id: row.productId,
      title: row.title,
      price: Number(row.price),
      imageUrl: row.image_url,
      description: row.description
    }
  });
});

app.delete("/favorites/:id", async (req, res) => {
  const userId = Number(req.header("x-user-id"));
  const favoriteId = Number(req.params.id);
  if (!userId) {
    return res.status(401).json({ message: "Missing user id" });
  }

  await pool.query("DELETE FROM favorites WHERE id = ? AND user_id = ?", [favoriteId, userId]);
  return res.status(204).send();
});

app.delete("/favorites", async (req, res) => {
  const userId = Number(req.header("x-user-id"));
  if (!userId) {
    return res.status(401).json({ message: "Missing user id" });
  }
  await pool.query("DELETE FROM favorites WHERE user_id = ?", [userId]);
  return res.status(204).send();
});

app.put("/admin/products/:id", async (req, res) => {
  const id = Number(req.params.id);
  const { title, price, imageUrl, description, ingredients, calories, story } = req.body as Record<string, unknown>;
  await pool.query(
    `UPDATE products
     SET title = COALESCE(?, title),
         price = COALESCE(?, price),
         image_url = COALESCE(?, image_url),
         description = COALESCE(?, description),
         ingredients = COALESCE(?, ingredients),
         calories = COALESCE(?, calories),
         story = COALESCE(?, story)
     WHERE id = ?`,
    [title, price, imageUrl, description, ingredients, calories, story, id]
  );
  await redis.del("products:all", `product:${id}`);
  return res.json({ message: "Product updated" });
});

repairMojibakeProducts()
  .catch((error) => {
    // eslint-disable-next-line no-console
    console.error("Failed to repair product encoding", error);
  })
  .finally(() => {
    app.listen(port, () => {
      // eslint-disable-next-line no-console
      console.log(`Catalog service running on ${port}`);
    });
  });
