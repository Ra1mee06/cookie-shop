# Руководство по установке

## Пошаговая установка проекта Cookie Shop

### Шаг 1: Установка зависимостей

#### Node.js и npm
1. Скачайте и установите Node.js с [официального сайта](https://nodejs.org/)
2. Проверьте установку:
```bash
node --version
npm --version
```

#### Java и Maven
1. Установите JDK 11 или выше
2. Установите Maven с [официального сайта](https://maven.apache.org/)
3. Проверьте установку:
```bash
java -version
mvn --version
```

#### MySQL
1. Установите MySQL 8.0+ с [официального сайта](https://dev.mysql.com/downloads/mysql/)
   - Выберите "MySQL Installer for Windows"
   - При установке запишите пароль для пользователя `root` (он понадобится позже)

2. **Запуск MySQL на Windows:**

   **Способ 1: Через службы Windows (рекомендуется)**
   - Нажмите `Win + R`, введите `services.msc` и нажмите Enter
   - Найдите службу "MySQL80" (или "MySQL")
   - Правый клик → "Запустить" (Start)
   - Чтобы служба запускалась автоматически: правый клик → "Свойства" → "Тип запуска" → "Автоматически"

   **Способ 2: Через командную строку (PowerShell от имени администратора)**
   ```powershell
   # Запустить службу
   net start MySQL80
   
   # Или используя sc команду
   sc start MySQL80
   
   # Остановить службу (если нужно)
   net stop MySQL80
   ```

   **Способ 3: Через MySQL Command Line Client**
   - Найдите в меню Пуск "MySQL Command Line Client"
   - Запустите его, введите пароль root
   - Если клиент запустился, значит MySQL работает

3. Проверьте, что MySQL запущен:
```bash
mysql --version
```

4. **Проверка работы через командную строку:**
   ```bash
   # Откройте PowerShell или CMD
   mysql -u root -p
   # Введите пароль, который вы указали при установке
   # Если подключение успешно - MySQL работает!
   ```

### Шаг 2: Настройка базы данных

1. Войдите в MySQL:
```bash
mysql -u root -p
```

2. Выполните скрипт создания базы данных:
```bash
mysql -u root -p < database/schema.sql
```

Или вручную в MySQL консоли:
```sql
CREATE DATABASE IF NOT EXISTS cookie_shop;
USE cookie_shop;
SOURCE database/schema.sql;
```

3. Проверьте, что база создана:
```sql
SHOW DATABASES;
USE cookie_shop;
SHOW TABLES;
```

### Шаг 3: Настройка бэкенда

1. Перейдите в папку server:
```bash
cd server
```

2. Настройте подключение к БД в `src/main/resources/application.properties`:
```properties
spring.datasource.username=ваш_пользователь
spring.datasource.password=ваш_пароль
```

3. Установите зависимости Maven (автоматически при первом запуске):
```bash
mvn clean install
```

4. Запустите сервер:
```bash
mvn spring-boot:run
```

Сервер должен запуститься на `http://localhost:8080`

### Шаг 4: Настройка фронтенда

1. Откройте новое окно терминала
2. Перейдите в папку client-vue:
```bash
cd client-vue
```

3. Установите зависимости:
```bash
npm install
```

4. Настройте API endpoint (если нужно) в `src/composables/useApi.js`:
```javascript
const API_URL = 'http://localhost:8080/api';
```

5. Запустите dev сервер:
```bash
npm run dev
```

Фронтенд должен быть доступен на `http://localhost:5173`

### Шаг 5: Проверка работы

1. Откройте браузер и перейдите на `http://localhost:5173`
2. Проверьте, что товары загружаются
3. Попробуйте добавить товар в корзину
4. Проверьте консоль браузера на наличие ошибок

### Устранение проблем

#### Проблема: Бэкенд не подключается к БД
- Проверьте, что MySQL запущен
- Убедитесь, что credentials в `application.properties` правильные
- Проверьте, что база данных `cookie_shop` существует

#### Проблема: CORS ошибки
- Проверьте настройки CORS в `server/src/main/java/com/cookieshop/config/WebConfig.java`
- Убедитесь, что порт фронтенда разрешен

#### Проблема: Фронтенд не подключается к API
- Проверьте, что бэкенд запущен на порту 8080
- Проверьте URL в `useApi.js`
- Проверьте консоль браузера на ошибки

#### Проблема: npm install не работает
- Попробуйте очистить кеш: `npm cache clean --force`
- Удалите `node_modules` и `package-lock.json`, затем снова `npm install`
- Проверьте версию Node.js (нужна 16+)

## Запуск в production режиме

### Бэкенд
```bash
cd server
mvn clean package
java -jar target/cookie-shop-server-1.0.0.jar
```

### Фронтенд
```bash
cd client-vue
npm run build
npm run preview
```

Или разверните папку `dist` на любом веб-сервере (nginx, Apache и т.д.)

