# Руководство по разработке

## Рабочий процесс

### Подготовка к разработке

1. Создайте ветку для новой функции:
```bash
git checkout -b feature/название-функции
```

2. Убедитесь, что все зависимости установлены:
```bash
cd client-vue && npm install
cd ../server && mvn clean install
```

### Разработка фронтенда

#### Структура компонентов
- Используйте Composition API для новой логики
- Размещайте переиспользуемые компоненты в `src/components/`
- Страницы размещайте в `src/pages/`
- Бизнес-логику выносите в `src/composables/`

#### Стилизация
- Используйте TailwindCSS утилиты
- Для глобальных стилей используйте `src/assets/main.css`
- Следуйте существующим паттернам стилизации

#### Тестирование
```bash
cd client-vue
npm run dev  # Запуск dev сервера
npm run lint # Проверка кода
npm run format # Форматирование кода
```

### Разработка бэкенда

#### Структура пакетов
- **Controller** - только HTTP обработка, без бизнес-логики
- **Service** - вся бизнес-логика
- **Repository** - только доступ к данным
- **DTO** - для передачи данных между слоями

#### Создание нового эндпоинта

1. Создайте DTO (если нужен):
```java
// src/main/java/com/cookieshop/dto/YourDTO.java
```

2. Создайте/обновите Service:
```java
// src/main/java/com/cookieshop/service/YourService.java
```

3. Создайте Controller:
```java
// src/main/java/com/cookieshop/controller/YourController.java
```

4. Обновите SecurityConfig (если нужна авторизация)

#### Тестирование API
```bash
cd server
mvn spring-boot:run
```

Используйте Postman или curl для тестирования эндпоинтов.

### Работа с БД

#### Миграции
- Основные скрипты в `database/schema.sql`
- Данные для тестирования в `server/src/main/resources/data.sql`

#### Добавление новой таблицы
1. Обновите `database/schema.sql`
2. Обновите `server/src/main/resources/schema.sql`
3. Создайте Entity класс в `server/src/main/java/com/cookieshop/entity/`
4. Создайте Repository в `server/src/main/java/com/cookieshop/repository/`

### Коммиты

Используйте понятные сообщения коммитов:
```
feat: добавлена корзина покупок
fix: исправлена ошибка авторизации
docs: обновлена документация API
refactor: рефакторинг компонента Card
```

### Code Review

Перед созданием PR убедитесь:
- [ ] Код соответствует стилю проекта
- [ ] Нет ошибок линтера
- [ ] Все тесты проходят
- [ ] Документация обновлена (если нужно)
- [ ] Миграции БД обновлены (если нужно)

## Стандарты кода

### JavaScript/TypeScript
- Используйте ESLint правила проекта
- Форматируйте код через Prettier
- Используйте async/await вместо промисов где возможно
- Типизируйте через TypeScript где возможно

### Java
- Следуйте Java naming conventions
- Используйте аннотации Spring корректно
- Комментируйте сложную логику
- Обрабатывайте исключения

### SQL
- Используйте параметризованные запросы
- Индексируйте часто используемые поля
- Используйте транзакции где нужно

## Отладка

### Frontend
- Используйте Vue DevTools
- Проверяйте Network tab в браузере
- Смотрите консоль на ошибки

### Backend
- Проверяйте логи Spring Boot в консоли
- Используйте `@Slf4j` для логирования
- Проверяйте `application.properties` на правильность настроек

### Database
- Используйте MySQL Workbench для визуализации
- Проверяйте логи MySQL
- Используйте EXPLAIN для запросов

