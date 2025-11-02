# Архитектура проекта

## Общая структура

Проект использует классическую трехслойную архитектуру:

```
Frontend (Vue.js) ←→ Backend (Spring Boot) ←→ Database (MySQL)
```

## Frontend (client-vue)

### Структура папок
```
src/
├── components/     # Переиспользуемые Vue компоненты
├── pages/          # Страницы приложения
├── composables/    # Composition API хуки и логика
├── router/         # Конфигурация Vue Router
└── assets/         # Статические ресурсы (CSS, изображения)
```

### Компоненты
- **Card** - карточка товара
- **CardList** - список товаров
- **CartItem** - элемент корзины
- **Drawer** - боковая панель корзины
- **Header** - шапка приложения

### Composables
- **useCart** - управление корзиной
- **useFavorites** - управление избранным
- **useApi** - HTTP запросы к API
- **useSuggestions** - поисковые подсказки

## Backend (server)

### Структура пакетов
```
com.cookieshop/
├── config/         # Конфигурация Spring (Security, CORS)
├── controller/     # REST контроллеры
├── service/        # Бизнес-логика
├── repository/     # Доступ к данным (JPA)
├── entity/         # JPA сущности
└── dto/           # Data Transfer Objects
```

### Слои
1. **Controller** - обработка HTTP запросов
2. **Service** - бизнес-логика
3. **Repository** - доступ к БД через JPA

### Безопасность
- Spring Security для защиты эндпоинтов
- JWT токены для аутентификации
- CORS настройки для фронтенда

## Database

### Таблицы
- **users** - пользователи системы
- **products** - каталог товаров

### Миграции
SQL скрипты находятся в:
- `database/schema.sql` - основная схема
- `server/src/main/resources/schema.sql` - для Spring Boot
- `server/src/main/resources/data.sql` - начальные данные

## Поток данных

### Типичный запрос:
1. Пользователь взаимодействует с Vue компонентом
2. Компонент вызывает composable (useApi, useCart)
3. Composable отправляет HTTP запрос к Spring Boot
4. Controller получает запрос, валидирует его
5. Controller вызывает Service для бизнес-логики
6. Service использует Repository для работы с БД
7. Ответ возвращается обратно через все слои

