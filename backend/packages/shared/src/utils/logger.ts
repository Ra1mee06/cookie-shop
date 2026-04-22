import pino from "pino";

export const logger = pino({
  name: "cookie-shop",
  level: process.env.LOG_LEVEL ?? "info"
});
