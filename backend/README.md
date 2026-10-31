# BankEase backend

Spring Boot 3 / Java 17 REST MVP with session-based Spring Security. The default
`mock` profile seeds an in-memory service with demo users; the `mysql` profile
is ready for the included MySQL 8 Docker Compose file.

## Run

```bash
mvn spring-boot:run
# or: docker compose up -d mysql
SPRING_PROFILES_ACTIVE=mysql mvn spring-boot:run
```

Demo credentials: `customer/customer123`, `admin/admin123`, `support/support123`.
Login with `POST /api/auth/login` and retain the `JSESSIONID` cookie for subsequent
requests. Main endpoints include `/api/dashboard`, `/api/accounts`,
`/api/transactions`, `/api/transfers`, `/api/bills` and `/api/bills/pay`,
`/api/loans`, `/api/investments`, and support/admin workflows. The collection
endpoints for loans and investments return the authenticated user's records.

Validate with `mvn test` and `mvn package`.
