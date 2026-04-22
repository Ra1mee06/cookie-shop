# Cookie Shop

Проект мигрирован на TypeScript backend с элементами распределенной информационной системы.

## Архитектура backend

- `api-gateway` (`:8081`) - единая точка входа для фронтенда, JWT auth, rate-limit.
- `catalog-service` (`:8082`) - продукты и избранное, Redis cache.
- `order-service` (`:8083`) - заказы и предложения, outbox + публикация доменных событий.
- `notification-worker` - асинхронный обработчик событий из Redis Stream.
- `MySQL` - основное хранилище.
- `Redis` - кэш, ограничение запросов и event bus.

## Основной запуск (Docker + TypeScript backend)

`docker-compose.yml` запускает только Node.js/TypeScript сервисы (`api-gateway`, `catalog-service`, `order-service`, `notification-worker`) и инфраструктуру (`mysql`, `redis`).

```bash
docker compose up -d --build
```

Контейнеры запускаются с политикой `restart: unless-stopped`, поэтому backend продолжает работать в фоне и после перезапуска Docker Desktop, пока вы сами не выполните `docker compose down`.

Порты инфраструктуры в Docker:
- MySQL: `localhost:3307` (внутри Docker-сети остается `3306`)
- Redis: `localhost:6380` (внутри Docker-сети остается `6379`)

После старта:
- API Gateway: `http://localhost:18081/api`
- Catalog service health: `http://localhost:18082/health`
- Order service health: `http://localhost:18083/health`

Остановка docker-стека:

```bash
docker compose down
```

## Запуск фронтенда

```bash
cd client-vue
npm install
npm run dev
```

Фронтенд уже настроен на `http://localhost:18081/api`.

## Что добавлено по теме "Распределенные информационные системы"

- Сервисное разделение (`gateway`, `catalog`, `orders`, `worker`).
- Асинхронный обмен событиями (`order.created`, `order.status.changed`) через Redis Stream.
- Паттерн надежности Outbox в `order-service` с ретраями публикации.
- Redis-кэш для горячих чтений каталога и инвалидация кэша при изменениях.
- Correlation ID и health endpoints для наблюдаемости и диагностики.

