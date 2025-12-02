# Команды запуска

## База данных (быстрая установка)

```bash
mysql -u root -p < database/init.sql
```
Эта команда создаст базу данных, все таблицы и заполнит их начальными данными.

## Бэкенд
```bash
cd server
mvn clean install
mvn spring-boot:run
```

## Фронтенд
```bash
cd client-vue
npm install
npm run dev
```

