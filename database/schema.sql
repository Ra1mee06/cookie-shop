CREATE DATABASE IF NOT EXISTS cookie_shop;
USE cookie_shop;

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

-- Добавьте тестовые данные
INSERT INTO products (title, price, image_url, description, ingredients, calories, story) VALUES
('Бабушкины тайны', 150.00, '/cookies/cookie1.png', 'Традиционная печенька с секретным рецептом', 'Мука, масло, мёд, специи', '350 ккал/100г', 'Рецепт создан в 1890 году'),
('Ореховый кранч', 180.00, '/cookies/cookie2.png', 'Хрустящая печенька с орехами', 'Мука, масло, грецкие орехи, миндаль', '420 ккал/100г', 'Любимое лакомство альпинистов'),
('Шоколадный рай', 200.00, '/cookies/cookie3.png', 'Для настоящих ценителей шоколада', 'Мука, масло, какао-бобы, шоколад', '380 ккал/100г', 'Вдохновлено бельгийскими рецептами');