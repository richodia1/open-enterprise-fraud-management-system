# Database Providers

OEFMS should remain adaptable across database providers, but PostgreSQL is the default local test database for v0.1.

## Current v0.1 Position

- PostgreSQL 16 is the tested local development database.
- Each deployable service has its own database in local Docker Compose.
- Database-backed services use environment variables for JDBC URL, username, password, driver, Hibernate dialect, and Flyway locations.
- Ingestion Management Service now has the first service-owned persistence slice for received transaction receipts.
- Other services still have database configuration prepared before their first durable domain records are added.

## Local PostgreSQL Databases

| Service | Local Database |
| --- | --- |
| Auth Service | `openefms_auth` |
| Ingestion Management Service | `openefms_ingestion` |
| Fraud Detection Service | `openefms_fraud` |
| Explainable AI Service | `openefms_explainable_ai` |
| Rule Management Service | `openefms_rules` |
| List Management Service | `openefms_lists` |
| Case Management Service | `openefms_cases` |

The local PostgreSQL container creates these databases using:

```text
infrastructure/postgres/init/01-create-service-databases.sql
```

## Runtime Configuration

Each database-backed service supports the same configuration shape:

```yaml
spring:
  datasource:
    url: "${OEFMS_DATASOURCE_URL:jdbc:postgresql://localhost:5432/openefms_fraud}"
    username: "${OEFMS_DATASOURCE_USERNAME:openefms}"
    password: "${OEFMS_DATASOURCE_PASSWORD:openefms}"
    driver-class-name: "${OEFMS_DATASOURCE_DRIVER:org.postgresql.Driver}"
  jpa:
    hibernate:
      ddl-auto: "${OEFMS_JPA_DDL_AUTO:none}"
    properties:
      hibernate:
        dialect: "${OEFMS_HIBERNATE_DIALECT:org.hibernate.dialect.PostgreSQLDialect}"
  flyway:
    enabled: "${OEFMS_FLYWAY_ENABLED:true}"
    locations: "${OEFMS_FLYWAY_LOCATIONS:classpath:db/migration/common,classpath:db/migration/postgresql}"
```

## Provider Strategy

PostgreSQL is committed as the default driver for local development. Other providers should be added deliberately by service need, with their own JDBC driver, Hibernate dialect, and Flyway migration folder.

Examples:

| Provider | JDBC URL Pattern | Driver | Hibernate Dialect |
| --- | --- | --- | --- |
| PostgreSQL | `jdbc:postgresql://host:5432/database` | `org.postgresql.Driver` | `org.hibernate.dialect.PostgreSQLDialect` |
| MySQL | `jdbc:mysql://host:3306/database` | `com.mysql.cj.jdbc.Driver` | `org.hibernate.dialect.MySQLDialect` |
| SQL Server | `jdbc:sqlserver://host:1433;databaseName=database` | `com.microsoft.sqlserver.jdbc.SQLServerDriver` | `org.hibernate.dialect.SQLServerDialect` |
| Oracle | `jdbc:oracle:thin:@host:1521/service` | `oracle.jdbc.OracleDriver` | `org.hibernate.dialect.OracleDialect` |

Provider-specific migrations should be kept separate:

```text
src/main/resources/db/migration/common
src/main/resources/db/migration/postgresql
src/main/resources/db/migration/mysql
src/main/resources/db/migration/sqlserver
src/main/resources/db/migration/oracle
```

Only portable migrations should go into `common`.
