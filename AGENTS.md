# AGENTS.md

## Project Overview

Web Manager API — REST API for managing employees and managers.
Built with **Java 21**, **Spring Boot 4.0.3** and **Maven**.

## Commands

```bash
./mvnw test             # run all tests (uses H2, profile "test")
./mvnw spring-boot:run  # run the app locally
./mvnw clean package    # build the jar
```

Running the app requires PostgreSQL (`jdbc:postgresql://localhost:5432/webmanager`) and the
env vars `DB_USER` and `DB_PASSWORD`. Schema is managed by Flyway migrations
(`src/main/resources/db/migration/`), which run automatically on startup.

## Project Structure

All code lives under `src/main/java/com/webmanager/`:

- `controller/` — REST endpoints (`@RestController`, `@RequestMapping`)
- `service/` — business logic (`@Service`), validation, exception throwing
- `repository/` — Spring Data JPA interfaces (`JpaRepository`)
- `entity/` — JPA entities; `enums/` for role types
- `dto/` — Java records for request/response payloads (`employee`, `manager`, `login`)
- `mapper/` — MapStruct interfaces converting Entity <-> DTO
- `exception/` — custom exceptions + `GlobalExceptionHandler`
- `security/` — JWT filter, `JwtService`, `SecurityConfig`
- `utils/` — `CpfConverter` (CPF column conversion), `ValidationUtils`
- `config/` — OpenAPI/Swagger configuration

## Conventions & Patterns

- **Controllers**: delegate to a service, return `ResponseEntity`. Class annotated with
  `@RequiredArgsConstructor` (constructor injection via `private final`).
- **Services**: inject repositories, mappers and `ValidationUtils`; use existing repository
  methods (`existsByEmail`, `existsByCpf`, `findByName`) for uniqueness/validation before
  saving. Throw custom exceptions (`UserNotFoundException`, `EmailAlreadyExistsExecption`,
  `CPFAlreadyExistsException`) instead of returning error responses.
- **Entities**: Lombok (`@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`),
  `@Entity(name = "table")`, `UUID` id with `GenerationType.UUID`, `@CreationTimestamp` /
  `@UpdateTimestamp` timestamps, `@Convert(converter = CpfConverter.class)` on `cpf`.
- **Repositories**: extend `JpaRepository<Entity, UUID>`.
- **Mappers**: MapStruct `@Mapper(componentModel = "spring")`, registered as Spring beans.
- **DTOs**: Java records with `jakarta.validation` constraints on request DTOs
  (`@NotBlank`, `@Email`, `@CPF`).
- **Controller path disambiguation**: `GET /employees/{id}` uses a UUID regex constraint,
  and `GET /employees/{email}` uses a negative-lookahead regex excluding UUID-shaped
  segments — keep these patterns mutually exclusive to avoid ambiguous mapping errors.

## Tests

- `src/test/java/com/webmanager/` — tests extend `BaseTest` (`@ActiveProfiles("test")`, H2).
- `service/EmployeeServiceTest.java` — Mockito unit tests (`@ExtendWith(MockitoExtension.class)`,
  `@Mock` + `@InjectMocks`), AssertJ assertions.
- `repository/EmployeeRepositoryTest.java` — `@DataJpaTest` integration tests.
- `WebManagerApplicationTests.java` — `@SpringBootTest` smoke test.
- Entities used in repository tests must set `cpf` and `phone` (NOT NULL columns).

## Security

- JWT-based auth. `POST /auth/login` (permitAll) returns a token.
- All other endpoints require `Authorization: Bearer <token>`.
- Roles: `MANAGER` (enum `com.webmanager.enums.Role`).