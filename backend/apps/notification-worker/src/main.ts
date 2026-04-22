import "dotenv/config";
import { Redis } from "ioredis";
import { logger } from "@cookie-shop/shared";

const redis = new Redis(process.env.REDIS_URL ?? "redis://localhost:6379");
const streamKey = process.env.EVENT_STREAM_KEY ?? "domain_events";
let lastId = "$";

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

const run = async () => {
  logger.info({ streamKey }, "notification worker started");

  while (true) {
    try {
      const events = await redis.xread("BLOCK", 5000, "STREAMS", streamKey, lastId);
      if (!events) {
        continue;
      }

      for (const [, streamEvents] of events) {
        for (const [id, fields] of streamEvents) {
          lastId = id;
          const eventField = fields.find((field: string, index: number) => index % 2 === 0 && field === "event");
          if (!eventField) {
            continue;
          }
          const eventIndex = fields.indexOf(eventField);
          const eventPayload = fields[eventIndex + 1];
          logger.info({ event: eventPayload }, "processed domain event");
        }
      }
    } catch (error) {
      logger.error({ error }, "worker read failed, retrying");
      await sleep(2000);
    }
  }
};

void run();
