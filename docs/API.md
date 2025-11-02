# API Документация

## Базовый URL
```
http://localhost:8080/api
```

## Эндпоинты

### Аутентификация

#### Регистрация
```
POST /auth/register
Content-Type: application/json

{
  "username": "string",
  "email": "string",
  "password": "string",
  "fullName": "string"
}
```

#### Вход
```
POST /auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}

Response:
{
  "token": "jwt_token",
  "user": { ... }
}
```

### Продукты

#### Получить все продукты
```
GET /products
```

#### Получить продукт по ID
```
GET /products/{id}
```

#### Поиск продуктов
```
GET /products/search?query={query}
```

### Корзина (требует аутентификации)
Все запросы требуют заголовок:
```
Authorization: Bearer {jwt_token}
```

#### Добавить в корзину
```
POST /cart/add
{
  "productId": 1,
  "quantity": 2
}
```

#### Получить корзину
```
GET /cart
```

### Избранное (требует аутентификации)

#### Добавить в избранное
```
POST /favorites/add
{
  "productId": 1
}
```

#### Получить избранное
```
GET /favorites
```

