# shared/ — KMP Shared Module

Scope: `shared/`

## Structure
- Kotlin Multiplatform module targeting android, jvm, ios. Contains domain models and the SQLDelight schema (`.sq` files) — this is the single source of truth for the database shape used by `:app`.
- JVM target is used purely for fast unit tests against an in-memory SQLite instance; it is not a deployable target.

## Schema Changes
- Any schema change requires editing the relevant `.sq` file, then a Gradle regeneration (`./gradlew :shared:generateSqlDelightInterface` or a normal build). There is **no manual migration method** — don't hand-write migration SQL outside the `.sq` files.
- Every table must carry `storeId` for multi-tenancy. Never add a table or column that bypasses this.
- Database class is `StoreBookDatabase`, configured in `shared/build.gradle.kts`.

## Testing
- Run `./gradlew :shared:jvmTest` after any change in this module. This is also the command Android relies on for shared-logic coverage — don't assume `:app` tests cover it.

## Before finishing
`./gradlew :shared:jvmTest` must pass. If the `.sq` schema changed, also run `./gradlew assembleDebug` to confirm downstream generation succeeds.
