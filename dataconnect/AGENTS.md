# dataconnect/ — Firebase Data Connect

Scope: `dataconnect/schema/`, `web/src/dataconnect/`

## Structure
- `dataconnect/schema/` holds the GQL schema, queries, and mutations — this is hand-edited and is the source of truth.
- `web/src/dataconnect/` is the **generated** TypeScript SDK. Never edit it directly; regenerate it from the schema instead (via the Firebase Data Connect CLI/emulator toolchain configured for this project).

## Conventions
- Every type/query/mutation touching a tenant table must scope by `storeId` — this is the multi-tenancy boundary between Android, web, and Postgres.
- Timeout defensive pattern applies to Data Connect queries — don't remove timeout config when editing queries.
- Circuit breaker pattern applies to Firebase auth calls specifically, not general Data Connect queries.

## Before finishing
After a schema edit, regenerate the SDK and confirm `web/` still type-checks (`cd web && npm run type-check`) and Android still builds if the shared models reference the same shapes.
