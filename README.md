# Команды запуска

## База данных
```bash
mysql -u root -p > database/schema.sql
```

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

