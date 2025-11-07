-- Инициализация данных только если таблицы пусты
-- Проверяем и добавляем пользователей только если их нет
INSERT INTO users (username, email, password, full_name, role) 
SELECT 'admin', 'admin@cookieshop.com', 'admin123', 'Администратор', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- Главный администратор, указанный пользователем
INSERT INTO users (username, email, password, full_name, role)
SELECT 'Ra1mee', 'filipovichdaniil8@gmail.com', 'password1532', 'Ra1mee', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'Ra1mee');

-- Новый администратор по запросу: логин Ra1meeee, пароль Mama1532, почта ra1mee@gmail.com
INSERT INTO users (username, email, password, full_name, role)
SELECT 'Ra1meeee', 'ra1mee@gmail.com', 'Mama1532', 'Ra1meeee', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'Ra1meeee' OR email = 'ra1mee@gmail.com');

INSERT INTO users (username, email, password, full_name, role) 
SELECT 'user1', 'user1@mail.com', 'user123', 'Иван Иванов', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user1');

-- Добавляем товары только если их нет
-- Обновляем цены для существующих товаров (случайные цены от 5 до 15 BYN)
UPDATE products SET price = 8.50 WHERE title = 'Бабушкины тайны';

INSERT INTO products (title, price, image_url, description, ingredients, calories, story) 
SELECT 'Бабушкины тайны', 8.50, '/cookies/cookie1.png', 'Традиционная печенька с секретным рецептом, передающимся в семье из поколения в поколение. Содержит натуральные специи и мёд.', 'Мука, масло, мёд, специи', '350 ккал/100г', 'Рецепт создан в 1890 году в маленькой деревне на севере Беларуси'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Бабушкины тайны');

-- Обновляем цены для существующих товаров
UPDATE products SET price = 11.20 WHERE title = 'Ореховый кранч';

INSERT INTO products (title, price, image_url, description, ingredients, calories, story) 
SELECT 'Ореховый кранч', 11.20, '/cookies/cookie2.png', 'Хрустящая печенька с кусочками грецких орехов и миндаля. Идеальный баланс сладости и орехового послевкусия.', 'Мука, масло, сахар, грецкие орехи, миндаль', '420 ккал/100г', 'Любимое лакомство альпинистов и путешественников'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Ореховый кранч');

-- Обновляем цены для существующих товаров
UPDATE products SET price = 14.80 WHERE title = 'Шоколадный рай';

INSERT INTO products (title, price, image_url, description, ingredients, calories, story) 
SELECT 'Шоколадный рай', 14.80, '/cookies/cookie3.png', 'Для настоящих ценителей шоколада. Содержит какао-бобы высшего сорта и шоколадные капли.', 'Мука, масло, какао-бобы, шоколадные капли', '380 ккал/100г', 'Вдохновлено рецептами бельгийских шоколатье'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Шоколадный рай');

-- Обновляем цены для существующих товаров
UPDATE products SET price = 6.30 WHERE title = 'Ванильная мечта';

INSERT INTO products (title, price, image_url, description, ingredients, calories, story) 
SELECT 'Ванильная мечта', 6.30, '/cookies/cookie4.png', 'Нежная печенька с натуральной ванилью и легкой воздушной текстурой.', 'Мука, масло, сахар, натуральная ваниль', '320 ккал/100г', 'Создано по рецепту французских кондитеров'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Ванильная мечта');

-- Обновляем цены для существующих товаров
UPDATE products SET price = 9.70 WHERE title = 'Карамельное облако';

INSERT INTO products (title, price, image_url, description, ingredients, calories, story) 
SELECT 'Карамельное облако', 9.70, '/cookies/cookie5.png', 'Пышная печенька с нежной карамелью внутри и хрустящей корочкой снаружи. Тает во рту, оставляя сладкое послевкусие.', 'Мука, масло, сахар, карамель, сливки', '395 ккал/100г', 'Рецепт придуман кондитером из Праги во время зимних каникул 2015 года'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Карамельное облако');

-- Обновляем цены для существующих товаров
UPDATE products SET price = 7.90 WHERE title = 'Имбирное волшебство';

INSERT INTO products (title, price, image_url, description, ingredients, calories, story) 
SELECT 'Имбирное волшебство', 7.90, '/cookies/cookie6.png', 'Ароматное печенье с тёплыми специями: имбирь, корица и мускатный орех создают неповторимый праздничный вкус.', 'Мука, масло, сахар, имбирь, корица, мускатный орех', '365 ккал/100г', 'Традиционный рецепт рождественского печенья из Скандинавии, адаптированный нашими кондитерами'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Имбирное волшебство');

-- Обновляем цены для существующих товаров
UPDATE products SET price = 12.40 WHERE title = 'Клубничная феерия';

INSERT INTO products (title, price, image_url, description, ingredients, calories, story) 
SELECT 'Клубничная феерия', 12.40, '/cookies/cookie7.png', 'Яркая печенька с кусочками сушёной клубники и белым шоколадом. Освежающий вкус лета в каждом кусочке.', 'Мука, масло, сахар, сушёная клубника, белый шоколад, ваниль', '410 ккал/100г', 'Создано вдохновлённое полями клубники в Шотландии, где каждое лето собирают самые сладкие ягоды'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Клубничная феерия');

-- Обновляем цены для существующих товаров
UPDATE products SET price = 15.00 WHERE title = 'Фисташковый бриллиант';

INSERT INTO products (title, price, image_url, description, ingredients, calories, story) 
SELECT 'Фисташковый бриллиант', 15.00, '/cookies/cookie8.png', 'Премиальное печенье с крупными кусочками фисташек и тонкой прослойкой тёмного шоколада. Изысканный вкус для гурманов.', 'Мука, масло, сахар, фисташки, тёмный шоколад 70%, морская соль', '445 ккал/100г', 'Рецепт, привезённый из Ирана, где фисташки считаются золотыми орехами и используются в самых изысканных десертах'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE title = 'Фисташковый бриллиант');