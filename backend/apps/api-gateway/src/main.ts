import "dotenv/config";
import axios from "axios";
import cors from "cors";
import { randomUUID } from "crypto";
import express from "express";
import fs from "fs";
import jwt from "jsonwebtoken";
import { Redis } from "ioredis";
import multer from "multer";
import path from "path";
import { z } from "zod";
import { pool } from "@cookie-shop/shared";

const app = express();
app.use(cors());
app.use(express.json());

const redis = new Redis(process.env.REDIS_URL ?? "redis://localhost:6379");
const port = Number(process.env.API_GATEWAY_PORT ?? 8081);
const catalogServiceUrl = process.env.CATALOG_SERVICE_URL ?? "http://localhost:8082";
const orderServiceUrl = process.env.ORDER_SERVICE_URL ?? "http://localhost:8083";
const jwtSecret = process.env.JWT_SECRET ?? "dev-secret";
const uploadsRootDir = path.resolve(process.cwd(), "uploads");
const avatarsDir = path.resolve(uploadsRootDir, "avatars");
fs.mkdirSync(avatarsDir, { recursive: true });
app.use("/uploads", express.static(uploadsRootDir));

const avatarStorage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, avatarsDir),
  filename: (_req, file, cb) => {
    const extensionByMime: Record<string, string> = {
      "image/jpeg": ".jpg",
      "image/jpg": ".jpg",
      "image/png": ".png",
      "image/webp": ".webp",
      "image/gif": ".gif",
      "image/svg+xml": ".svg"
    };
    const originalExtension = path.extname(file.originalname || "").toLowerCase();
    const extension = extensionByMime[file.mimetype] ?? (originalExtension || ".bin");
    cb(null, `avatar-${Date.now()}-${Math.round(Math.random() * 1e9)}${extension}`);
  }
});

const avatarUpload = multer({
  storage: avatarStorage,
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter: (_req, file, cb) => {
    if (!file.mimetype.startsWith("image/")) {
      cb(new Error("Only image files are allowed"));
      return;
    }
    cb(null, true);
  }
});

const repairMojibakeUsers = async () => {
  await pool.query(
    `UPDATE users
     SET full_name = CONVERT(BINARY CONVERT(full_name USING latin1) USING utf8mb4)
     WHERE full_name LIKE 'Ð%'`
  );
};

const registerSchema = z.object({
  username: z.string().trim().min(2, "Логин должен содержать минимум 2 символа"),
  email: z.string().trim().email("Введите корректный email"),
  password: z.string().min(4, "Пароль должен содержать минимум 4 символа"),
  fullName: z.string().trim().min(2, "Имя должно содержать минимум 2 символа"),
  adminInviteCode: z.string().optional()
});

const loginSchema = z
  .object({
    loginOrEmail: z.string().trim().min(1, "Введите логин или email").optional(),
    email: z.string().trim().email("Введите корректный email").optional(),
    password: z.string().min(1, "Введите пароль")
  })
  .refine((data) => Boolean(data.loginOrEmail || data.email), {
    message: "Введите логин или email"
  });

const profileSchema = z.object({
  email: z.string().trim().email("Введите корректный email").optional(),
  fullName: z.string().trim().min(2, "Имя должно содержать минимум 2 символа").optional(),
  username: z.string().trim().min(2, "Логин должен содержать минимум 2 символа").optional()
});

const promoPayloadSchema = z.object({
  code: z.string().trim().min(2),
  type: z.enum(["ORDER_PERCENT", "PRODUCT_PERCENT", "BUY2GET1", "GIFT_CERTIFICATE"]),
  value: z.number().nonnegative().optional().nullable(),
  maxUses: z.number().int().positive().optional().nullable(),
  expiresAt: z.string().min(1).optional().nullable(),
  productId: z.number().int().positive().optional().nullable()
});

const toMySqlDateTime = (value: string) => {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return null;
  }
  return date.toISOString().slice(0, 19).replace("T", " ");
};

const firstValidationMessage = (flattened: { fieldErrors: Record<string, string[] | undefined> }) => {
  for (const errors of Object.values(flattened.fieldErrors)) {
    if (errors?.length) {
      return errors[0];
    }
  }
  return "Проверьте корректность заполнения формы";
};

app.use((req, res, next) => {
  const correlationId = req.header("x-correlation-id") ?? randomUUID();
  req.headers["x-correlation-id"] = correlationId;
  res.setHeader("x-correlation-id", correlationId);
  const startedAt = Date.now();
  res.on("finish", () => {
    // eslint-disable-next-line no-console
    console.log(`[gateway] ${correlationId} ${req.method} ${req.originalUrl} -> ${res.statusCode} (${Date.now() - startedAt}ms)`);
  });
  next();
});

app.use(async (req, res, next) => {
  const ip = req.ip ?? "unknown";
  const key = `rl:${ip}`;
  const count = await redis.incr(key);
  if (count === 1) {
    await redis.expire(key, 60);
  }
  if (count > 240) {
    return res.status(429).json({ message: "Too many requests" });
  }
  return next();
});

const authMiddleware: express.RequestHandler = (req, res, next) => {
  const header = req.header("authorization");
  if (!header) {
    return res.status(401).json({ message: "Missing token" });
  }
  const token = header.replace("Bearer ", "");
  try {
    const payload = jwt.verify(token, jwtSecret) as { userId: number; role: string };
    req.headers["x-user-id"] = String(payload.userId);
    req.headers["x-user-role"] = payload.role;
    return next();
  } catch (_error) {
    return res.status(401).json({ message: "Invalid token" });
  }
};

const adminOnly: express.RequestHandler = (req, res, next) => {
  if (req.header("x-user-role") !== "ADMIN") {
    return res.status(403).json({ message: "Admin role required" });
  }
  return next();
};

const proxy = async (req: express.Request, res: express.Response, target: string) => {
  try {
    const downstreamPath = req.path.replace(/^\/api/, "");
    const response = await axios.request({
      url: `${target}${downstreamPath}`,
      method: req.method as "GET" | "POST" | "PUT" | "DELETE" | "PATCH",
      data: req.body,
      headers: {
        "x-user-id": req.header("x-user-id"),
        "x-correlation-id": req.header("x-correlation-id") ?? randomUUID()
      },
      params: req.query
    });
    return res.status(response.status).json(response.data);
  } catch (error) {
    const axiosError = error as { response?: { status: number; data: unknown } };
    return res.status(axiosError.response?.status ?? 500).json(axiosError.response?.data ?? { message: "Proxy error" });
  }
};

app.get("/api/health", (_req, res) => {
  res.json({ service: "api-gateway", status: "ok" });
});

app.get("/api/ready", async (_req, res) => {
  try {
    await pool.query("SELECT 1");
    await redis.ping();
    return res.json({ service: "api-gateway", ready: true });
  } catch (error) {
    return res.status(503).json({ service: "api-gateway", ready: false, error: String(error) });
  }
});

app.post("/api/auth/register", async (req, res) => {
  const parsed = registerSchema.safeParse(req.body);
  if (!parsed.success) {
    const flattened = parsed.error.flatten();
    return res.status(400).json({ message: firstValidationMessage(flattened), errors: flattened.fieldErrors });
  }
  const { username, email, password, fullName, adminInviteCode } = parsed.data;

  let role = "USER";
  if (adminInviteCode) {
    const [inviteRows] = await pool.query(
      "SELECT id FROM admin_invites WHERE code = ? AND used = 0 AND (expires_at IS NULL OR expires_at > NOW()) LIMIT 1",
      [adminInviteCode]
    );
    const invite = (inviteRows as Record<string, unknown>[])[0];
    if (invite) {
      role = "ADMIN";
      await pool.query("UPDATE admin_invites SET used = 1 WHERE id = ?", [invite.id]);
    }
  }

  const [insert] = await pool.query(
    "INSERT INTO users(username, email, password, full_name, role) VALUES (?, ?, ?, ?, ?)",
    [username, email, password, fullName, role]
  );
  const userId = (insert as { insertId: number }).insertId;
  const token = jwt.sign({ userId, role }, jwtSecret, { expiresIn: "7d" });
  return res.status(201).json({
    token,
    user: {
      id: userId,
      username,
      email,
      fullName,
      role,
      avatarUrl: null
    }
  });
});

app.post("/api/auth/login", async (req, res) => {
  const parsed = loginSchema.safeParse(req.body);
  if (!parsed.success) {
    const flattened = parsed.error.flatten();
    return res.status(400).json({ message: firstValidationMessage(flattened), errors: flattened.fieldErrors });
  }
  const { loginOrEmail, email, password } = parsed.data;
  const login = loginOrEmail ?? email;
  const [rows] = await pool.query(
    "SELECT * FROM users WHERE (username = ? OR email = ?) AND password = ? LIMIT 1",
    [login, login, password]
  );
  const user = (rows as Record<string, unknown>[])[0];
  if (!user) {
    return res.status(401).json({ message: "Invalid credentials" });
  }
  const token = jwt.sign({ userId: user.id, role: user.role }, jwtSecret, { expiresIn: "7d" });
  return res.json({
    token,
    user: {
      id: user.id,
      username: user.username,
      email: user.email,
      fullName: user.full_name,
      role: user.role,
      avatarUrl: user.avatar_url
    }
  });
});

app.get("/api/auth/me", authMiddleware, async (req, res) => {
  const userId = Number(req.header("x-user-id"));
  const [rows] = await pool.query("SELECT * FROM users WHERE id = ? LIMIT 1", [userId]);
  const user = (rows as Record<string, unknown>[])[0];
  if (!user) {
    return res.status(404).json({ message: "User not found" });
  }
  return res.json({
    id: user.id,
    username: user.username,
    email: user.email,
    fullName: user.full_name,
    role: user.role,
    avatarUrl: user.avatar_url
  });
});

app.put("/api/auth/profile", authMiddleware, async (req, res) => {
  const userId = Number(req.header("x-user-id"));
  const parsed = profileSchema.safeParse(req.body);
  if (!parsed.success) {
    const flattened = parsed.error.flatten();
    return res.status(400).json({ message: firstValidationMessage(flattened), errors: flattened.fieldErrors });
  }
  const { email, fullName, username } = parsed.data;
  await pool.query(
    "UPDATE users SET email = COALESCE(?, email), full_name = COALESCE(?, full_name), username = COALESCE(?, username) WHERE id = ?",
    [email, fullName, username, userId]
  );
  return res.json({ message: "Profile updated" });
});

app.put("/api/auth/profile/password", authMiddleware, async (req, res) => {
  const userId = Number(req.header("x-user-id"));
  const { oldPassword, newPassword, confirmPassword } = req.body as Record<string, string>;
  if (newPassword !== confirmPassword) {
    return res.status(400).json({ message: "Passwords do not match" });
  }
  const [rows] = await pool.query("SELECT password FROM users WHERE id = ? LIMIT 1", [userId]);
  const user = (rows as Array<{ password: string }>)[0];
  if (!user || user.password !== oldPassword) {
    return res.status(400).json({ message: "Old password is invalid" });
  }
  await pool.query("UPDATE users SET password = ? WHERE id = ?", [newPassword, userId]);
  return res.json({ message: "Password updated" });
});

app.post("/api/auth/profile/avatar", authMiddleware, (req, res) => {
  avatarUpload.single("file")(req, res, async (uploadError) => {
    if (uploadError) {
      return res.status(400).json({ message: uploadError.message || "Avatar upload failed" });
    }
    try {
      const userId = Number(req.header("x-user-id"));
      const body = req.body as { avatarUrl?: string };
      const avatarUrl = req.file ? `/uploads/avatars/${req.file.filename}` : body.avatarUrl ?? null;
      await pool.query("UPDATE users SET avatar_url = ? WHERE id = ?", [avatarUrl, userId]);
      return res.json({ message: "Avatar updated", avatarUrl });
    } catch (error) {
      return res.status(500).json({ message: "Failed to update avatar", error: String(error) });
    }
  });
});

app.get("/api/products", (req, res) => proxy(req, res, catalogServiceUrl));
app.get("/api/products/:id", (req, res) => proxy(req, res, catalogServiceUrl));
app.get("/api/favorites", authMiddleware, (req, res) => proxy(req, res, catalogServiceUrl));
app.post("/api/favorites", authMiddleware, (req, res) => proxy(req, res, catalogServiceUrl));
app.delete("/api/favorites/:id", authMiddleware, (req, res) => proxy(req, res, catalogServiceUrl));
app.delete("/api/favorites", authMiddleware, (req, res) => proxy(req, res, catalogServiceUrl));

app.post("/api/orders", authMiddleware, (req, res) => proxy(req, res, orderServiceUrl));
app.get("/api/orders/user", authMiddleware, (req, res) => proxy(req, res, orderServiceUrl));
app.post("/api/suggestions", authMiddleware, (req, res) => proxy(req, res, orderServiceUrl));
app.get("/api/suggestions/user", authMiddleware, (req, res) => proxy(req, res, orderServiceUrl));

app.get("/api/admin/orders", authMiddleware, adminOnly, (req, res) => proxy(req, res, orderServiceUrl));
app.get("/api/admin/orders/:id", authMiddleware, adminOnly, (req, res) => proxy(req, res, orderServiceUrl));
app.put("/api/admin/orders/:id", authMiddleware, adminOnly, (req, res) => proxy(req, res, orderServiceUrl));
app.post("/api/admin/orders/:id/status", authMiddleware, adminOnly, (req, res) => proxy(req, res, orderServiceUrl));
app.get("/api/admin/suggestions", authMiddleware, adminOnly, (req, res) => proxy(req, res, orderServiceUrl));
app.delete("/api/admin/suggestions/:id", authMiddleware, adminOnly, (req, res) => proxy(req, res, orderServiceUrl));

app.get("/api/admin/users", authMiddleware, adminOnly, async (_req, res) => {
  const [rows] = await pool.query("SELECT id, username, email, full_name, role, avatar_url FROM users ORDER BY id DESC");
  res.json(rows);
});
app.get("/api/admin/users/:id", authMiddleware, adminOnly, async (req, res) => {
  const [rows] = await pool.query("SELECT id, username, email, full_name, role, avatar_url FROM users WHERE id = ? LIMIT 1", [
    Number(req.params.id)
  ]);
  const user = (rows as Record<string, unknown>[])[0];
  if (!user) {
    return res.status(404).json({ message: "User not found" });
  }
  return res.json(user);
});
app.put("/api/admin/users/:id", authMiddleware, adminOnly, async (req, res) => {
  const id = Number(req.params.id);
  const { email, fullName, username, avatarUrl, role } = req.body as Record<string, unknown>;
  await pool.query(
    `UPDATE users
     SET email = COALESCE(?, email),
         full_name = COALESCE(?, full_name),
         username = COALESCE(?, username),
         avatar_url = COALESCE(?, avatar_url),
         role = COALESCE(?, role)
     WHERE id = ?`,
    [email, fullName, username, avatarUrl, role, id]
  );
  return res.json({ message: "User updated" });
});
app.delete("/api/admin/users/:id", authMiddleware, adminOnly, async (req, res) => {
  await pool.query("DELETE FROM users WHERE id = ?", [Number(req.params.id)]);
  res.status(204).send();
});
app.get("/api/admin/users/:id/orders", authMiddleware, adminOnly, async (req, res) => {
  const [rows] = await pool.query("SELECT * FROM orders WHERE user_id = ? ORDER BY id DESC", [Number(req.params.id)]);
  res.json(rows);
});
app.get("/api/admin/users/:id/suggestions", authMiddleware, adminOnly, async (req, res) => {
  const [rows] = await pool.query("SELECT * FROM suggestions WHERE user_id = ? ORDER BY id DESC", [Number(req.params.id)]);
  res.json(rows);
});

app.get("/api/admin/promos", authMiddleware, adminOnly, async (_req, res) => {
  const [rows] = await pool.query("SELECT * FROM promo_codes ORDER BY id DESC");
  res.json(rows);
});
app.post("/api/admin/promos", authMiddleware, adminOnly, async (req, res) => {
  const parsed = promoPayloadSchema.safeParse(req.body);
  if (!parsed.success) {
    const flattened = parsed.error.flatten();
    return res.status(400).json({ message: firstValidationMessage(flattened), errors: flattened.fieldErrors });
  }
  const { code, type, value, maxUses, expiresAt, productId } = parsed.data;
  const normalizedExpiresAt = expiresAt ? toMySqlDateTime(expiresAt) : null;
  if (expiresAt && !normalizedExpiresAt) {
    return res.status(400).json({ message: "Invalid expiresAt value" });
  }
  if (type === "PRODUCT_PERCENT" && !productId) {
    return res.status(400).json({ message: "PRODUCT_PERCENT requires productId" });
  }
  try {
    const [insert] = await pool.query(
      "INSERT INTO promo_codes(code, type, value, max_uses, expires_at, product_id, active) VALUES(?, ?, ?, ?, ?, ?, 1)",
      [code.toUpperCase(), type, value ?? null, maxUses ?? null, normalizedExpiresAt, productId ?? null]
    );
    return res.status(201).json({ id: (insert as { insertId: number }).insertId });
  } catch (error) {
    const dbError = error as { code?: string; message?: string };
    if (dbError.code === "ER_DUP_ENTRY") {
      return res.status(409).json({ message: "Промокод с таким кодом уже существует" });
    }
    // eslint-disable-next-line no-console
    console.error("Failed to create promo code", dbError);
    return res.status(500).json({ message: "Failed to create promo code" });
  }
});
app.put("/api/admin/promos/:id", authMiddleware, adminOnly, async (req, res) => {
  const id = Number(req.params.id);
  const parsed = promoPayloadSchema.partial().safeParse(req.body);
  if (!parsed.success) {
    const flattened = parsed.error.flatten();
    return res.status(400).json({ message: firstValidationMessage(flattened), errors: flattened.fieldErrors });
  }
  const { code, type, value, maxUses, expiresAt, productId } = parsed.data;
  const normalizedExpiresAt = expiresAt ? toMySqlDateTime(expiresAt) : undefined;
  if (expiresAt && !normalizedExpiresAt) {
    return res.status(400).json({ message: "Invalid expiresAt value" });
  }
  if (type === "PRODUCT_PERCENT" && productId === null) {
    return res.status(400).json({ message: "PRODUCT_PERCENT requires productId" });
  }
  try {
    await pool.query(
      `UPDATE promo_codes
       SET code = COALESCE(?, code),
           type = COALESCE(?, type),
           value = COALESCE(?, value),
           max_uses = COALESCE(?, max_uses),
           expires_at = COALESCE(?, expires_at),
           product_id = COALESCE(?, product_id)
       WHERE id = ?`,
      [code?.toUpperCase(), type, value, maxUses, normalizedExpiresAt, productId, id]
    );
    return res.json({ message: "Promo updated" });
  } catch (error) {
    const dbError = error as { code?: string; message?: string };
    if (dbError.code === "ER_DUP_ENTRY") {
      return res.status(409).json({ message: "Промокод с таким кодом уже существует" });
    }
    // eslint-disable-next-line no-console
    console.error("Failed to update promo code", dbError);
    return res.status(500).json({ message: "Failed to update promo code" });
  }
});
app.delete("/api/admin/promos/:id", authMiddleware, adminOnly, async (req, res) => {
  await pool.query("UPDATE promo_codes SET active = 0 WHERE id = ?", [Number(req.params.id)]);
  res.status(204).send();
});

app.post("/api/admin/invites", authMiddleware, adminOnly, async (req, res) => {
  const expiresInHours = Number(req.body.expiresInHours ?? 24);
  const userId = Number(req.header("x-user-id"));
  const code = `INV-${randomUUID()}`;
  await pool.query(
    "INSERT INTO admin_invites(code, created_by, expires_at) VALUES(?, ?, DATE_ADD(NOW(), INTERVAL ? HOUR))",
    [code, userId, expiresInHours]
  );
  res.status(201).json({ code, expiresInHours });
});
app.get("/api/admin/invites", authMiddleware, adminOnly, async (_req, res) => {
  const [rows] = await pool.query("SELECT * FROM admin_invites ORDER BY id DESC");
  res.json(rows);
});
app.delete("/api/admin/invites/:id", authMiddleware, adminOnly, async (req, res) => {
  await pool.query("DELETE FROM admin_invites WHERE id = ?", [Number(req.params.id)]);
  res.status(204).send();
});

repairMojibakeUsers()
  .catch((error) => {
    // eslint-disable-next-line no-console
    console.error("Failed to repair user encoding", error);
  })
  .finally(() => {
    app.listen(port, () => {
      // eslint-disable-next-line no-console
      console.log(`API gateway running on ${port}`);
    });
  });
