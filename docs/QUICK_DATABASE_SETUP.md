# Быстрая настройка базы данных

## Способ 1: Через SOURCE команду (внутри MySQL)

1. Войдите в MySQL:
```bash
mysql -u root -p
```

2. Используйте SOURCE с полным путем:
```sql
SOURCE D:/cookie-shop/database/schema.sql;
```

**Важно:** Используйте прямые слеши `/` и полный абсолютный путь!

## Способ 2: Через командную строку (рекомендуется)

Выйдите из MySQL (команда `exit;`) и выполните:

```powershell
cd D:\cookie-shop
mysql -u root -p < database\schema.sql
```

MySQL запросит пароль, введите его. Скрипт выполнится автоматически.

## Способ 3: Выполнить команды вручную (внутри MySQL)

Если SOURCE не работает, выполните команды по очереди:

```sql
-- 1. Создать базу данных
CREATE DATABASE IF NOT EXISTS cookie_shop;

-- 2. Использовать базу данных
USE cookie_shop;

-- 3. Создать таблицу users
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'USER') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 4. Создать таблицу products
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    image_url VARCHAR(500),
    description TEXT,
    ingredients TEXT,
    calories VARCHAR(50),
    story TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 5. Добавить тестовые данные
INSERT INTO products (title, price, image_url, description, ingredients, calories, story) VALUES
('Бабушкины тайны', 150.00, '/cookie-1.jpg', 'Традиционная печенька с секретным рецептом', 'Мука, масло, мёд, специи', '350 ккал/100г', 'Рецепт создан в 1890 году'),
('Ореховый кранч', 180.00, '/cookie-2.jpg', 'Хрустящая печенька с орехами', 'Мука, масло, грецкие орехи, миндаль', '420 ккал/100г', 'Любимое лакомство альпинистов'),
('Шоколадный рай', 200.00, '/cookie-3.jpg', 'Для настоящих ценителей шоколада', 'Мука, масло, какао-бобы, шоколад', '380 ккал/100г', 'Вдохновлено бельгийскими рецептами');
```

## Проверка результата

После выполнения любого из способов проверьте:

```sql
-- Показать все базы данных
SHOW DATABASES;

-- Использовать базу данных
USE cookie_shop;

-- Показать таблицы
SHOW TABLES;

-- Проверить данные в таблице products
SELECT * FROM products;
```

Должны увидеть:
- База данных `cookie_shop` в списке
- Две таблицы: `users` и `products`
- Три записи в таблице `products`



