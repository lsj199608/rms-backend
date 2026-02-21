# AGENTS for rms-backend

## Scope
- Target scope: only this project directory (`rms-backend`) and files under it.
- Applies to Spring Boot API/backend work only.
- Coordinate with frontend through documented API responses, avoid touching frontend files from this scope.

## Stack
- Java 21
- Spring Boot 3.5.9
- MyBatis + JPA + Flyway + PostgreSQL
- Gradle

## Collaboration Rules
- Keep changes focused and incremental.
- Follow existing package structure under `src/main/java`.
- Prefer explicit, domain-driven naming for service/mapper/controller layers.
- Do not change unrelated SQL/migrations or configuration without reason.
- If behavior is uncertain, preserve compatibility and document assumptions.
- Do not edit outside `rms-backend` from this context.

## Code Style
- Use constructor or final-based DI in service classes.
- Return clear API DTOs from controllers.
- Keep transaction boundaries and exception mapping explicit.
- Add or update tests for changed behavior when possible.
- Avoid magic strings; use constants/enums for repeated business values.

## Commands
- `./gradlew bootRun` : run local server
- `./gradlew test` : run tests
- `./gradlew build` : compile + test + package
- `./gradlew clean` : cleanup build artifacts

## Common Task Focus
- Controller/service/mapper updates
- DB schema and migration handling with Flyway
- Domain and DTO contracts
- Error handling and response shape consistency

## Safety
- Never commit credentials (`application*.yml` values, DB passwords, API keys).
- Prefer environment variables/profiles over hardcoded config.

## Notes
- For DB changes, update migration files under `src/main/resources/db/migration` and keep them idempotent in intent.
