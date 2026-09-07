# Local Development

## Requirements

- JDK 21
- Maven 3.9+
- Docker Desktop or Docker Engine

## Build All Modules

```bash
mvn clean verify
```

## Run Local Stack

```bash
docker compose up --build
```

## Default Ports

| Component | Port |
| --- | --- |
| API Gateway | 8080 |
| Fraud Detection Service | 8081 |
| Rule Management Service | 8082 |
| List Management Service | 8083 |
| Case Management Service | 8084 |
| Auth Service | 8085 |
| Administration Service | 8086 |
| Identity Service | 8087 |
| Configuration Management Service | 8088 |
| Ingestion Management Service | 8089 |
| Audit Logging Service | 8090 |
| Notification Management Service | 8091 |
| Report Management Service | 8092 |
| GDPR Service | 8093 |
| PostgreSQL | 5432 |
| RabbitMQ | 5672 |
| RabbitMQ Management UI | 15672 |
| Redis | 6379 |
