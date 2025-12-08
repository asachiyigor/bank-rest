# Bank Cards Management System

[![CI/CD Pipeline](https://github.com/asachiyigor/bank-rest/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/asachiyigor/bank-rest/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.12-green.svg)](https://spring.io/projects/spring-boot)

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

## Тестирование

Запуск тестов:

```bash
./mvnw.cmd test
```

Просмотр отчета о покрытии кода (JaCoCo):

```bash
./mvnw.cmd test jacoco:report
```

Отчет будет доступен в `target/site/jacoco/index.html`

## CI/CD

Проект настроен с GitHub Actions для автоматической сборки и тестирования:
- Автоматический запуск тестов при каждом push и pull request
- Генерация отчетов о покрытии кода (JaCoCo)
- Проверка качества кода
- Артефакты сборки доступны для скачивания

## API Документация

Swagger UI: http://localhost:8080/swagger-ui.html

## Учетные данные

**Администратор:**
- Username: `admin`
- Password: `admin`

**Пользователь:**
- Username: `user`
- Password: `user`
