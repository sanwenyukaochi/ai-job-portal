---
name: flyway-migrations
description: >
  Create or review Flyway database migrations, schema changes, reference data,
  and migration configuration in this repository. Use only after the owning
  service and persistence boundary are confirmed; covers Spring Boot 4 setup,
  naming, immutable history, PostgreSQL-safe rollout, and migration tests.
---

# Flyway Migrations

Use Flyway as the single owner of relational schema evolution. Prefer the smallest forward-only
migration that keeps both the old and new application versions working during rollout.

## Project gates and ownership

- Follow the repository's six-stage spec workflow. Add production migrations only in Stage 6,
  after the domain, data ownership, boundary, and tests are confirmed.
- Each deployment unit owns its database and migration history. Never create a global migration
  module or a cross-service foreign key.
- Keep SQL migrations in the owning persistence adapter at
  `src/main/resources/db/migration`. The adapter resource is available when its container puts the
  adapter JAR on the runtime classpath.
- Keep Flyway dependencies, DataSource configuration, and startup wiring in the owning
  `container`.
- Do not expose Flyway, SQL, table names, or persistence records to Domain Core or application
  input ports.
- A migration may change only schemas and tables owned by its deployment unit.

## Spring Boot 4 dependencies

Spring Boot's JPA/JDBC starters do not replace the Flyway starter. PostgreSQL also requires its
Flyway database module. Add these only to a container that owns a PostgreSQL database; versions
come from the Spring Boot BOM.

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
}
```

The PostgreSQL JDBC driver remains a separate runtime dependency. Do not add the Flyway Gradle
plugin unless the build has a confirmed need to run Flyway independently of the application or
integration tests.

## File names and versions

Use the default location and naming conventions:

```text
src/main/resources/db/migration/
├── V1__create_initial_schema.sql
├── V2__add_application_status.sql
└── R__refresh_reporting_views.sql
```

- `V{version}__{description}.sql`: versioned migration, applied once.
- `R__{description}.sql`: repeatable migration, reapplied when its checksum changes.
- Use exactly two underscores between the version and description.
- Use lowercase `snake_case` descriptions.
- Start with monotonically increasing integers (`V1`, `V2`, `V3`) while they remain sufficient.
- Never reuse a version or insert a lower version after a higher version has been shared.
- If parallel work causes recurring collisions, adopt UTC timestamp versions for that service in
  a separate confirmed convention; do not mix strategies casually.
- Use repeatable migrations only for idempotent replaceable objects such as views or functions.
- Do not create undo migrations unless the project explicitly adopts the required Flyway edition
  and an operational rollback policy.

## Immutable migration history

Once a versioned migration is merged or applied to a shared or persistent environment, do not
edit, rename, reorder, or delete it. Roll forward with a new migration. Flyway stores checksums in
`flyway_schema_history`; changing applied files breaks validation and destroys reproducibility.

Before a migration has left a private disposable environment, it may be corrected only when every
affected database can be rebuilt. If there is any uncertainty, create a new migration.

Do not use `repair` to hide an accidental checksum mismatch. First determine whether the file or
the database history is wrong, preserve evidence, and obtain explicit operational approval before
changing shared history.

## Default configuration

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    validate-on-migrate: true
    validate-migration-naming: true
    out-of-order: false
    clean-disabled: true
  jpa:
    hibernate:
      ddl-auto: validate
  sql:
    init:
      mode: never
```

- Flyway is the schema owner. Do not combine it with Hibernate `update`/`create`, `schema.sql`, or
  production `data.sql` initialization.
- Keep `out-of-order` disabled. Resolve version conflicts instead of masking them.
- Keep `clean` disabled outside explicitly disposable test environments.
- Do not enable `baseline-on-migrate` by default. Greenfield databases do not need it.
- Introducing Flyway to a populated database requires a reviewed one-time baseline plan, an exact
  baseline version, a backup, and rehearsal against a production-like copy.

## Safe rollout patterns

Use expand/backfill/switch/contract for changes that affect running code:

1. Add a compatible nullable column, table, or index.
2. Deploy code that can work with both schemas and dual-write only when required.
3. Backfill existing rows with a bounded, observable operation.
4. Switch reads and verify consumers.
5. Add validated constraints when the data is ready.
6. Drop the old column or table only in a later release after all consumers have migrated.

Additional rules:

- Do not directly rename or drop a column used by a running or rollback-capable application.
- Do not assume that adding a `NOT NULL` column or a default is safe on a large table. Check the
  PostgreSQL version, table size, lock behavior, and rollout plan.
- Give constraints explicit stable names so later migrations can address them safely.
- Add indexes only for confirmed foreign-key, uniqueness, lookup, or ordering needs; every index
  increases write and maintenance cost.
- Separate large backfills from DDL when they cannot complete within the deployment window.
- Make data transformations deterministic and restart-safe. Do not depend on wall-clock time,
  external services, or application beans from SQL migrations.
- A destructive migration requires an explicit retention, backup, and restore decision in the
  relevant spec or ADR.

## PostgreSQL concurrent indexes

`CREATE INDEX CONCURRENTLY` cannot run in Flyway's normal transaction. Put it alone in its own
migration and add a matching sidecar configuration file:

```text
V4__add_status_index.sql
V4__add_status_index.sql.conf
```

```sql
CREATE INDEX CONCURRENTLY idx_application_status ON application (status);
```

```properties
executeInTransaction=false
```

Test the migration against PostgreSQL, not H2. A failed concurrent index can leave an invalid
index that must be inspected and handled deliberately before retrying.

## Reference and test data

- Stable reference data required for production correctness belongs in a versioned migration and
  must use deterministic identifiers and idempotent conflict handling where appropriate.
- Personal demo data, credentials, and realistic personal information never belong in production
  migrations.
- Test fixtures belong in test setup or `src/test/resources` migrations.
- Development-only data may use an explicitly profile-scoped Flyway location. It must never be on
  the production location list.
- Do not use an `ApplicationRunner` seeder as a second production schema/data migration system.

## Team workflow

- Pull or rebase before selecting the next version.
- Check all migration locations for duplicates before committing.
- Resolve duplicate versions by renumbering a migration that has not reached a shared environment;
  never renumber an already-applied migration.
- CI must start from an empty PostgreSQL database, apply every migration, and run Flyway validation.
- For risky changes, also test upgrading from the oldest schema version still supported in
  production.
- Keep deployment order compatible with rolling updates and rollback of application code.

## Tests and review checklist

Use the production database type and the production migration files in persistence/container
integration tests.

- A fresh PostgreSQL container migrates from empty to the latest schema.
- Hibernate validation succeeds against the migrated schema when JPA is used.
- Relevant previous schema states upgrade to the latest version.
- Constraints, defaults, indexes, and data transformations match the confirmed persistence model.
- Applied migration checksums remain unchanged.
- Repeatable migrations are idempotent.
- Non-transactional migrations are isolated and have recovery instructions.
- Migration failure stops startup or deployment; it is never silently ignored.
- No migration reaches another service's tables or schema.
- No secrets or environment-specific values are embedded in SQL.

## References

- [Spring Boot database initialization](https://docs.spring.io/spring-boot/how-to/data-initialization.html)
- [Flyway versioned migrations](https://documentation.red-gate.com/fd/versioned-migrations-273973333.html)
- [Flyway repeatable migrations](https://documentation.red-gate.com/fd/repeatable-migrations-273973335.html)
- [Flyway per-script configuration](https://documentation.red-gate.com/flyway/reference/script-configuration)
