import "dotenv/config";
import cors from "cors";
import express from "express";
import { Redis } from "ioredis";
import { z } from "zod";
import { pool, ORDER_CREATED_EVENT, ORDER_STATUS_CHANGED_EVENT } from "@cookie-shop/shared";

const app = express();
app.use(cors());
app.use(express.json());

const redis = new Redis(process.env.REDIS_URL ?? "redis://localhost:6379");
const port = Number(process.env.ORDER_SERVICE_PORT ?? 8083);
const streamKey = process.env.EVENT_STREAM_KEY ?? "domain_events";
const createOrderSchema = z.object({
  totalPrice: z.number().nonnegative(),
  finalTotal: z.number().nonnegative().optional(),
  discount: z.number().nonnegative().optional(),
  promoCode: z.string().trim().optional(),
  recipient: z.string().trim().min(2, "Укажите получателя (минимум 2 символа)"),
  address: z.string().trim().min(5, "Укажите корректный адрес доставки"),
  comment: z.string().optional(),
  paymentMethod: z.enum(["CASH", "CARD_ONLINE", "CARD_ON_DELIVERY"], {
    errorMap: () => ({ message: "Выберите способ оплаты" })
  }),
  tip: z.number().nonnegative().optional(),
  items: z.array(
    z.object({
      productId: z.number().int().positive(),
      quantity: z.number().int().positive(),
      price: z.number().nonnegative()
    })
  ).min(1, "Корзина пуста")
});
const suggestionSchema = z.object({
  author: z.string().trim().min(2, "Укажите имя (минимум 2 символа)"),
  productName: z.string().trim().min(2, "Укажите название товара (минимум 2 символа)"),
  description: z.string().trim().min(5, "Описание должно быть не короче 5 символов")
});
const statusSchema = z.object({
  status: z.enum(["PENDING", "CONFIRMED", "DELIVERED", "CANCELLED"])
});

const repairMojibakeSuggestions = async () => {
  await pool.query(
    `UPDATE suggestions
     SET author = CONVERT(BINARY CONVERT(author USING latin1) USING utf8mb4),
         product_name = CONVERT(BINARY CONVERT(product_name USING latin1) USING utf8mb4),
         description = CONVERT(BINARY CONVERT(description USING latin1) USING utf8mb4)
     WHERE author LIKE 'Ð%' OR product_name LIKE 'Ð%' OR description LIKE 'Ð%'`
  );
};

const ensureOutbox = async () => {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS outbox_events (
      id BIGINT PRIMARY KEY AUTO_INCREMENT,
      event_type VARCHAR(100) NOT NULL,
      payload JSON NOT NULL,
      status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
      retry_count INT NOT NULL DEFAULT 0,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    )
  `);
};

const publishPendingOutboxEvents = async () => {
  const [rows] = await pool.query(
    "SELECT id, event_type, payload, retry_count FROM outbox_events WHERE status = 'PENDING' ORDER BY id ASC LIMIT 20"
  );
  for (const row of rows as Record<string, unknown>[]) {
    try {
      await redis.xadd(streamKey, "*", "event", JSON.stringify({ type: row.event_type, payload: row.payload }));
      await pool.query("UPDATE outbox_events SET status = 'PUBLISHED' WHERE id = ?", [row.id]);
    } catch (_error) {
      await pool.query(
        "UPDATE outbox_events SET retry_count = retry_count + 1 WHERE id = ? AND retry_count < 10",
        [row.id]
      );
    }
  }
};

setInterval(() => {
  void publishPendingOutboxEvents();
}, 5000);

app.get("/health", (_req, res) => {
  res.json({ service: "order-service", status: "ok" });
});

app.get("/ready", async (_req, res) => {
  try {
    await pool.query("SELECT 1");
    await redis.ping();
    return res.json({ service: "order-service", ready: true });
  } catch (error) {
    return res.status(503).json({ service: "order-service", ready: false, error: String(error) });
  }
});

app.post("/orders", async (req, res) => {
  const userId = Number(req.header("x-user-id")) || null;
  const parsed = createOrderSchema.safeParse(req.body);
  if (!parsed.success) {
    const fieldErrors = parsed.error.flatten().fieldErrors;
    const firstError =
      fieldErrors.recipient?.[0] ??
      fieldErrors.address?.[0] ??
      fieldErrors.paymentMethod?.[0] ??
      fieldErrors.items?.[0] ??
      "Проверьте корректность данных заказа";
    return res.status(400).json({ message: firstError, errors: fieldErrors });
  }
  const payload = parsed.data;

  const connection = await pool.getConnection();
  try {
    await connection.beginTransaction();
    const totalPrice = payload.finalTotal ?? payload.totalPrice;
    const [orderInsert] = await connection.query(
      `INSERT INTO orders(user_id, total_price, status, recipient, address, comment, promo_code, payment_method, tip, discount)
       VALUES (?, ?, 'PENDING', ?, ?, ?, ?, ?, ?, ?)`,
      [
        userId,
        totalPrice,
        payload.recipient ?? null,
        payload.address ?? null,
        payload.comment ?? null,
        payload.promoCode ?? null,
        payload.paymentMethod ?? "CASH",
        payload.tip ?? 0,
        payload.discount ?? 0
      ]
    );
    const orderId = (orderInsert as { insertId: number }).insertId;

    for (const item of payload.items ?? []) {
      await connection.query(
        "INSERT INTO order_items(order_id, product_id, quantity, price) VALUES(?, ?, ?, ?)",
        [orderId, item.productId, item.quantity, item.price]
      );
    }

    await connection.query("INSERT INTO outbox_events(event_type, payload) VALUES(?, ?)", [
      ORDER_CREATED_EVENT,
      JSON.stringify({
        orderId,
        userId,
        totalPrice,
        status: "PENDING"
      })
    ]);
    await connection.commit();
    return res.status(201).json({ id: orderId, status: "PENDING" });
  } catch (error) {
    await connection.rollback();
    return res.status(500).json({ message: "Failed to create order", error: String(error) });
  } finally {
    connection.release();
  }
});

app.get("/orders/user", async (req, res) => {
  const userId = Number(req.header("x-user-id"));
  if (!userId) {
    return res.status(401).json({ message: "Missing user id" });
  }
  const [rows] = await pool.query("SELECT * FROM orders WHERE user_id = ? ORDER BY id DESC", [userId]);
  return res.json(rows);
});

app.get("/admin/orders", async (_req, res) => {
  const [rows] = await pool.query("SELECT * FROM orders ORDER BY id DESC");
  return res.json(rows);
});

app.get("/admin/orders/:id", async (req, res) => {
  const [rows] = await pool.query("SELECT * FROM orders WHERE id = ? LIMIT 1", [Number(req.params.id)]);
  const order = (rows as Record<string, unknown>[])[0];
  if (!order) {
    return res.status(404).json({ message: "Order not found" });
  }
  return res.json(order);
});

app.put("/admin/orders/:id", async (req, res) => {
  const id = Number(req.params.id);
  const { recipient, address, comment, paymentMethod, status, totalPrice, discount } = req.body as Record<string, unknown>;
  await pool.query(
    `UPDATE orders
     SET recipient = COALESCE(?, recipient),
         address = COALESCE(?, address),
         comment = COALESCE(?, comment),
         payment_method = COALESCE(?, payment_method),
         status = COALESCE(?, status),
         total_price = COALESCE(?, total_price),
         discount = COALESCE(?, discount)
     WHERE id = ?`,
    [recipient, address, comment, paymentMethod, status, totalPrice, discount, id]
  );
  return res.json({ message: "Order updated" });
});

app.post("/admin/orders/:id/status", async (req, res) => {
  const id = Number(req.params.id);
  const parsed = statusSchema.safeParse(req.body);
  if (!parsed.success) {
    return res.status(400).json({ message: "Invalid payload", errors: parsed.error.flatten() });
  }
  const nextStatus = parsed.data.status;
  const [rows] = await pool.query("SELECT status FROM orders WHERE id = ? LIMIT 1", [id]);
  const previousStatus = (rows as Array<{ status: string }>)[0]?.status;
  await pool.query("UPDATE orders SET status = ? WHERE id = ?", [nextStatus, id]);
  await pool.query("INSERT INTO outbox_events(event_type, payload) VALUES(?, ?)", [
    ORDER_STATUS_CHANGED_EVENT,
    JSON.stringify({
      orderId: id,
      previousStatus,
      nextStatus
    })
  ]);
  return res.json({ message: "Status updated" });
});

app.post("/suggestions", async (req, res) => {
  const userId = Number(req.header("x-user-id")) || null;
  const parsed = suggestionSchema.safeParse(req.body);
  if (!parsed.success) {
    const fieldErrors = parsed.error.flatten().fieldErrors;
    const firstError =
      fieldErrors.author?.[0] ??
      fieldErrors.productName?.[0] ??
      fieldErrors.description?.[0] ??
      "Проверьте корректность заполнения формы";
    return res.status(400).json({ message: firstError, errors: fieldErrors });
  }
  const { author, productName, description } = parsed.data;
  const [insert] = await pool.query(
    "INSERT INTO suggestions(user_id, author, product_name, description) VALUES(?, ?, ?, ?)",
    [userId, author, productName, description]
  );
  return res.status(201).json({ id: (insert as { insertId: number }).insertId });
});

app.get("/suggestions/user", async (req, res) => {
  const userId = Number(req.header("x-user-id"));
  if (!userId) {
    return res.status(401).json({ message: "Missing user id" });
  }
  const [rows] = await pool.query("SELECT * FROM suggestions WHERE user_id = ? ORDER BY id DESC", [userId]);
  return res.json(rows);
});

app.get("/admin/suggestions", async (req, res) => {
  const userId = req.query.userId ? Number(req.query.userId) : null;
  const [rows] = userId
    ? await pool.query("SELECT * FROM suggestions WHERE user_id = ? ORDER BY id DESC", [userId])
    : await pool.query("SELECT * FROM suggestions ORDER BY id DESC");
  return res.json(rows);
});

app.delete("/admin/suggestions/:id", async (req, res) => {
  await pool.query("DELETE FROM suggestions WHERE id = ?", [Number(req.params.id)]);
  return res.status(204).send();
});

Promise.all([ensureOutbox(), repairMojibakeSuggestions()])
  .then(() => {
    app.listen(port, () => {
      // eslint-disable-next-line no-console
      console.log(`Order service running on ${port}`);
    });
  })
  .catch((error) => {
    // eslint-disable-next-line no-console
    console.error("Order service failed to start", error);
    process.exit(1);
  });
