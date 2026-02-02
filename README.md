# MovieHub API

REST API для управления фильмами, реализованный с использованием TDD подхода.

## API Endpoints

### GET /movies
Получить список всех фильмов

**Ответ:** 200 OK
```json
[
  {
    "id": 1,
    "title": "Inception",
    "year": 2010
  }
]
```

### GET /movies?year=YYYY
Получить фильмы определенного года

**Параметры:**
- `year` - год выпуска фильма

**Ответ:** 200 OK

### GET /movies/{id}
Получить фильм по ID

**Ответ:** 200 OK / 404 Not Found

### POST /movies
Создать новый фильм

**Headers:**
- `Content-Type: application/json`

**Body:**
```json
{
  "title": "Inception",
  "year": 2010
}
```

**Ответ:** 201 Created
```json
{
  "id": 1,
  "title": "Inception",
  "year": 2010
}
```

**Валидация:**
- title: не пустой, максимум 100 символов
- year: от 1888 до (текущий год + 1)

### DELETE /movies/{id}
Удалить фильм

**Ответ:** 204 No Content / 404 Not Found
