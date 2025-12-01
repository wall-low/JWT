# 🔐 Spring Boot JWT Authentication Demo

Демонстрационный проект на Spring Boot с полноценной JWT-аутентификацией, включая access/refresh токены и систему blacklist.

## 🚀 Технологии

- **Spring Boot 3.5.8** - основной фреймворк
- **Spring Security** - аутентификация и авторизация
- **JWT (jjwt 0.12.6)** - JSON Web Tokens
- **Spring Data JPA** - работа с базой данных
- **H2 Database** - встроенная база данных
- **Thymeleaf** - серверный рендеринг (опционально)
- **Lombok** - уменьшение boilerplate кода
- **Java 24** - последняя версия Java

## ✨ Возможности

- ✅ Регистрация и вход пользователей
- ✅ Генерация Access и Refresh токенов
- ✅ Автоматическое обновление токенов
- ✅ Blacklist для отозванных токенов (logout)
- ✅ Защищенные API эндпоинты
- ✅ REST API + MVC страницы (Thymeleaf)
- ✅ H2 консоль для отладки

## 📋 Требования

- Java 24 (или совместимая версия)
- Gradle (или используй встроенный `./gradlew`)

## 🛠️ Установка и запуск

### 1. Клонировать репозиторий

```bash
git clone <your-repo-url>
cd JWT
```

### 2. Запустить приложение

```bash
./gradlew bootRun
```

Приложение запустится на **http://localhost:8081**

### 3. Доступ к H2 консоли

- URL: **http://localhost:8081/h2-console**
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: _(оставить пустым)_

## 🌐 API Эндпоинты

### Публичные эндпоинты

#### Регистрация
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "user123",
  "password": "password123"
}
```

#### Вход
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "user123",
  "password": "password123"
}
```

**Ответ:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "user123"
}
```

#### Обновление токена
```bash
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "your-refresh-token"
}
```

### Защищенные эндпоинты

Требуют заголовок: `Authorization: Bearer <access-token>`

#### Информация о пользователе
```bash
GET /api/user
Authorization: Bearer <your-access-token>
```

#### Защищенные данные
```bash
GET /api/protected
Authorization: Bearer <your-access-token>
```

#### Выход (Logout)
```bash
POST /api/auth/logout
Authorization: Bearer <your-access-token>
```

## 🔑 Конфигурация JWT

В `src/main/resources/application.properties`:

```properties
# JWT настройки (для демо)
jwt.secret=my-super-secret-jwt-key-that-must-be-at-least-256-bits-long-to-work-with-hs256-algorithm-properly
jwt.access-token-expiration=5000        # 5 секунд (для демонстрации)
jwt.refresh-token-expiration=300000     # 5 минут
```

> ⚠️ **Важно:** Это демо-проект! В продакшене используйте переменные окружения для секретных ключей.

## 📁 Структура проекта

```
src/main/java/com/web_site/JWT/
├── config/
│   ├── SecurityConfig.java         # Spring Security конфигурация
│   └── DataInitializer.java        # Начальные данные
├── controller/
│   ├── AuthController.java         # REST API: регистрация/вход
│   ├── ApiController.java          # Защищенные API эндпоинты
│   └── PageController.java         # MVC страницы (Thymeleaf)
├── dto/
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   └── RefreshTokenRequest.java
├── model/
│   ├── User.java                   # JPA сущность пользователя
│   └── BlacklistedToken.java       # Токены в blacklist
├── repository/
│   ├── UserRepository.java
│   └── BlacklistedTokenRepository.java
├── security/
│   ├── JwtTokenProvider.java       # Генерация и валидация JWT
│   └── JwtAuthenticationFilter.java # Фильтр для проверки токенов
└── service/
    ├── UserService.java            # Бизнес-логика пользователей
    └── TokenBlacklistService.java  # Управление blacklist
```

## 🧪 Тестирование

### Запуск тестов
```bash
./gradlew test
```

### Пример использования с cURL

```bash
# 1. Регистрация
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'

# 2. Вход
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'

# Сохраните access_token из ответа

# 3. Доступ к защищенному эндпоинту
curl -X GET http://localhost:8081/api/user \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# 4. Выход
curl -X POST http://localhost:8081/api/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 🎨 Web Интерфейс

Доступны страницы с Thymeleaf:

- **http://localhost:8081/login** - Страница входа
- **http://localhost:8081/protected** - Защищенная страница
- **http://localhost:8081/profile** - Профиль пользователя

JavaScript автоматически управляет токенами через `token-manager.js`.

## 🔒 Как работает JWT аутентификация

1. **Регистрация/Вход** → Получение Access + Refresh токенов
2. **Запросы к API** → Отправка Access токена в заголовке
3. **Истечение Access токена** → Обновление через Refresh токен
4. **Logout** → Токен добавляется в blacklist (отзывается)

### Временные интервалы (демо):
- Access токен: **5 секунд** ⏱️
- Refresh токен: **5 минут** ⏱️

> В продакшене обычно: Access = 15 минут, Refresh = 7-30 дней

## 🗄️ База данных

Используется **H2** (in-memory):

**Таблицы:**
- `users` - Пользователи (id, username, password, role)
- `blacklisted_tokens` - Отозванные токены

При старте автоматически создается тестовый пользователь (см. `DataInitializer.java`)

## 🛡️ Безопасность

- ✅ Пароли хешируются через BCrypt
- ✅ STATELESS сессии (без cookies)
- ✅ CSRF отключен (для REST API)
- ✅ Blacklist для отозванных токенов
- ⚠️ JWT секрет в коде (только для демо!)

## 📚 Полезные команды

```bash
# Сборка проекта
./gradlew build

# Запуск приложения
./gradlew bootRun

# Запуск тестов
./gradlew test

# Очистка
./gradlew clean

# Очистка + сборка
./gradlew clean build
```
