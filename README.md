# Bank Cards Management System

Backend-приложение на Java (Spring Boot) для управления банковскими картами с поддержкой JWT аутентификации.

## Требования

- Java 17+
- Docker & Docker Compose

## Запуск приложения

### 1. Запустите PostgreSQL через Docker

```bash
  docker-compose up -d
```

### 2. Запустите приложение

```bash
  ./mvnw.cmd spring-boot:run
```

Приложение будет доступно на http://localhost:8080

## API Документация

Swagger UI: http://localhost:8080/swagger-ui.html

## Учетные данные

**Администратор:**
- Username: `admin`
- Password: `admin`

**Пользователь:**
- Username: `user`
- Password: `user`
