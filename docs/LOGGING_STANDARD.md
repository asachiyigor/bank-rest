# Стандарт логирования

## Формат логов

Все логи в проекте следуют единому формату:

```
[OPERATION_TAG] Status - параметр1=значение1, параметр2=значение2
```

## Теги операций

Определены в `LogConstants.java`:

### Transfer операции:
- `[TRANSFER_CREATE]` - Создание перевода
- `[TRANSFER_GET]` - Получение перевода по ID
- `[TRANSFER_HISTORY]` - Получение истории переводов карты
- `[TRANSFER_USER_HISTORY]` - Получение истории переводов пользователя

### Card операции:
- `[CARD_CREATE]` - Создание карты
- `[CARD_GET]` - Получение карты по ID
- `[CARD_GET_ALL]` - Получение списка карт пользователя
- `[CARD_BLOCK]` - Блокировка карты
- `[CARD_ACTIVATE]` - Активация карты
- `[CARD_DELETE]` - Удаление карты

### Auth операции:
- `[AUTH_LOGIN]` - Вход пользователя
- `[AUTH_REGISTER]` - Регистрация пользователя

### User операции:
- `[USER_GET_ALL]` - Получение списка всех пользователей
- `[USER_GET]` - Получение пользователя по ID
- `[USER_UPDATE]` - Обновление пользователя
- `[USER_DELETE]` - Удаление пользователя

## Статусы операций

- `Starting operation` - Начало выполнения операции
- `Operation completed successfully` - Успешное завершение
- `Operation failed` - Ошибка выполнения

## Примеры логов

### Успешный перевод:
```
INFO  [TRANSFER_CREATE] Starting operation - fromCardId=1, toCardId=2, amount=100.50
DEBUG [TRANSFER_CREATE] Updating balances - fromCardBalance=1000.00, toCardBalance=500.00
INFO  [TRANSFER_CREATE] Operation completed successfully - transferId=123, userId=5
```

### Получение карты:
```
INFO  [CARD_GET] Starting operation - cardId=1
INFO  [CARD_GET] Operation completed successfully - cardId=1, userId=5
```

### Вход пользователя:
```
INFO  [AUTH_LOGIN] Starting operation - username=john_doe
INFO  [AUTH_LOGIN] Operation completed successfully - username=john_doe, userId=5
```

### Получение перевода по ID:
```
INFO  [TRANSFER_GET] Starting operation - transferId=123
INFO  [TRANSFER_GET] Operation completed successfully - transferId=123, userId=5
```

### Получение истории переводов:
```
INFO  [TRANSFER_HISTORY] Starting operation - cardId=1, page=0, size=20
INFO  [TRANSFER_HISTORY] Operation completed successfully - cardId=1, transfersFound=15
```

### Получение списка пользователей:
```
INFO  [USER_GET_ALL] Getting all users - page=0, size=20
INFO  [USER_GET_ALL] Found users - count=150
```

### Получение пользователя:
```
INFO  [USER_GET] Getting user by id - userId=5
```

### Обновление пользователя:
```
INFO  [USER_UPDATE] Starting operation - userId=5
INFO  [USER_UPDATE] Operation completed successfully - userId=5
```

### Удаление пользователя:
```
INFO  [USER_DELETE] Starting operation - userId=5
INFO  [USER_DELETE] Operation completed successfully - userId=5
```

## Преимущества стандартизации

1. **Легкий поиск**: Можно искать все логи конкретной операции по тегу
   ```bash
   grep "\[TRANSFER_CREATE\]" application.log
   ```

2. **Мониторинг**: Легко настроить алерты на ошибки конкретных операций

3. **Анализ производительности**: Можно отследить полный путь операции от начала до конца

4. **Структурированные данные**: Все параметры указаны в формате key=value

## Уровни логирования

- `INFO` - Начало и успешное завершение операций, основные бизнес-события
- `DEBUG` - Детали выполнения операций (изменение балансов, количество записей и т.д.)
- `WARN` - Предупреждения (не критичные проблемы)
- `ERROR` - Ошибки выполнения операций (автоматически логируются через GlobalExceptionHandler)
