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
('Бабушкины тайны', 150.00, '/cookies/cookie1.png', 'Традиционная печенька с секретным рецептом, передающимся в семье из поколения в поколение. Содержит натуральные специи и мёд.', 'Мука, масло, мёд, специи', '350 ккал/100г', 'Рецепт создан в 1890 году в маленькой деревне на севере России'),
('Ореховый кранч', 180.00, '/cookies/cookie2.png', 'Хрустящая печенька с кусочками грецких орехов и миндаля. Идеальный баланс сладости и орехового послевкусия.', 'Мука, масло, сахар, грецкие орехи, миндаль', '420 ккал/100г', 'Любимое лакомство альпинистов и путешественников'),
('Шоколадный рай', 200.00, '/cookies/cookie3.png', 'Для настоящих ценителей шоколада. Содержит какао-бобы высшего сорта и шоколадные капли.', 'Мука, масло, какао-бобы, шоколадные капли', '380 ккал/100г', 'Вдохновлено рецептами бельгийских шоколатье'),
('Ванильная мечта', 170.00, '/cookies/cookie4.png', 'Нежная печенька с натуральной ванилью и легкой воздушной текстурой.', 'Мука, масло, сахар, натуральная ваниль', '320 ккал/100г', 'Создано по рецепту французских кондитеров'),
('Карамельное облако', 190.00, '/cookies/cookie5.png', 'Пышная печенька с нежной карамелью внутри и хрустящей корочкой снаружи. Тает во рту, оставляя сладкое послевкусие.', 'Мука, масло, сахар, карамель, сливки', '395 ккал/100г', 'Рецепт придуман кондитером из Праги во время зимних каникул 2015 года'),
('Имбирное волшебство', 175.00, '/cookies/cookie6.png', 'Ароматное печенье с тёплыми специями: имбирь, корица и мускатный орех создают неповторимый праздничный вкус.', 'Мука, масло, сахар, имбирь, корица, мускатный орех', '365 ккал/100г', 'Традиционный рецепт рождественского печенья из Скандинавии, адаптированный нашими кондитерами'),
('Клубничная феерия', 210.00, '/cookies/cookie7.png', 'Яркая печенька с кусочками сушёной клубники и белым шоколадом. Освежающий вкус лета в каждом кусочке.', 'Мука, масло, сахар, сушёная клубника, белый шоколад, ваниль', '410 ккал/100г', 'Создано вдохновлённое полями клубники в Шотландии, где каждое лето собирают самые сладкие ягоды'),
('Фисташковый бриллиант', 220.00, '/cookies/cookie8.png', 'Премиальное печенье с крупными кусочками фисташек и тонкой прослойкой тёмного шоколада. Изысканный вкус для гурманов.', 'Мука, масло, сахар, фисташки, тёмный шоколад 70%, морская соль', '445 ккал/100г', 'Рецепт, привезённый из Ирана, где фисташки считаются золотыми орехами и используются в самых изысканных десертах');

CREATE TABLE suggestions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    author VARCHAR(100) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);