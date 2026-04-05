# Ecommerce Platform

## Category Module (Phase 1)

This project currently implements a pure Category CRUD module using Spring Boot + JPA + PostgreSQL.

### Implemented API

- `POST /api/v1/categories`
- `GET /api/v1/categories`
- `GET /api/v1/categories/{id}`
- `PUT /api/v1/categories/{id}`
- `DELETE /api/v1/categories/{id}`

### Notes

- No Redis/RabbitMQ integration yet.
- Category name is unique (case-insensitive check in service + unique constraint at DB level).
- Validation is handled with Jakarta Validation annotations.

