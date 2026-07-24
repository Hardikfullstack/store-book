<!-- BEGIN bigpowers:project -->
# StoreBook — AI Agents

## Project
Offline-first Android POS + cloud-sync Next.js web dashboard for Indian small businesses. Kotlin/Compose + SQLDelight (Android) / TypeScript + Next.js 16 + Redux Toolkit (Web) / Firebase Data Connect → Cloud SQL PostgreSQL / KMP shared module (`:shared`). Multi-tenant via `storeId` on every table.

## Commands
| Action            | Command                                                   |
|-------------------|-----------------------------------------------------------|
| Android Build     | `./gradlew :app:assembleDebug`                            |
| Android Install   | `./gradlew :app:installDebug`                             |
| Android Lint      | `./gradlew lintKtLintCheck` (ktlint 1.5.0 in `:app`)     |
| Android Unit Test | `./gradlew :shared:jvmTest`                               |
| Android Snapshot  | `./gradlew verifyRoborazziDebug`                          |
| Android Record    | `./gradlew recordRoborazziDebug` (updates golden images)  |
| Web Dev           | `cd web && npm run dev`                                   |
| Web Build          | `cd web && npm run build`                                 |
| Web Lint           | `cd web && npm run lint`                                  |
| Web Type Check     | `cd web && npm run type-check`                            |

No CI configured — all gates are local.

## Pre-Commit Checks
After completing any code changes, run the following checks before considering work done. If any check fails, fix the errors and re-run until all pass:
1. `./gradlew :app:ktlintCheck` — Android Kotlin linting
2. `./gradlew :shared:jvmTest` — KMP shared module unit tests
3. `cd web && npm run lint` — Web ESLint
4. `cd web && npm run type-check` — Web TypeScript type checking

## Architecture
- **Android** (`:app`) — Jetpack Compose UI, ViewModels in `ui/`, SQLite via SQLDelight (schema auto-generated from `:shared`). `SyncWorker` pushes/pulls through Firebase Data Connect.
- **Shared** (`:shared`) — KMP module. Domain models + SQLDelight schema. Targets android, jvm, ios. JVM target used for unit tests against in-memory SQLite.
- **Web** (`web/`) — Next.js 16 App Router, turbopack enabled, `next-pwa` (disabled in dev). Server Actions under `web/src/app/api/`. Redux + redux-persist client cache.
- **Data** — Firebase Data Connect schemas live in `dataconnect/schema/`, generated TS SDK at `web/src/dataconnect/`. No raw SQL interpolation anywhere.

## Environment
- Web requires `web/.env.local` (Firebase config, Razorpay keys). File is gitignored; ask the user if missing.
- Android Java home: `/opt/android-studio/jbr` (set in `.vscode/settings.json`).

## Conventions
- Kotlin package: `com.storebook.inventoryapp.{data,ui,services,workers}`
- Web routes: `web/src/app/{route}/page.tsx` + `{route}/{Route}Client.tsx` pattern
- Specs live in `specs/`; write to `specs/` before generating code. YAML is source of truth over markdown there.
- SQLDelight database class: `StoreBookDatabase` (configured in `shared/build.gradle.kts`). Any schema change requires a `.sq` file edit + Gradle regeneration — no manual migration method.
- Roborazzi snapshots go under `app/src/test/snapshots/`; golden images are committed to repo. 0.5% pixel-diff threshold.
- Defensive patterns in scope: **Retry** (SyncWorker), **Circuit breaker** (Firebase auth), **Timeout** (Data Connect queries).

## Never
- Never commit Firebase credentials, `service-account.json`, or `local.properties`.
- Never bypass `storeId` multi-tenancy guards in any query or mutation.
- Never hard-code absolute paths outside `app/` or `web/`.
- Never modify `backend/` (legacy Express.js dev remnant) unless explicitly asked.

<!-- END bigpowers:project -->

<!-- BEGIN bigpowers:context-routing -->
## Context Routing

| Glob Pattern | Sub-doc |
|---|---|
| `app/**` | Android Kotlin — Compose screens, ViewModels, tests, Roborazzi snapshots |
| `shared/` | KMP module — domain models, SQLDelight schema (.sq), JVM unit tests |
| `web/src/app/**` | Next.js App Router pages + Server Actions |
| `web/src/components/**` | Shared React components |
| `web/src/lib/**` | Firebase init, session, billing engine, utils |
| `web/src/store/**` | Redux Toolkit slices (cart, inventory, udhaar) |
| `dataconnect/schema/` | Firebase Data Connect schema + queries + mutations (GQL) |
| `web/src/dataconnect/` | Auto-generated TS SDK — do not edit directly |
| `backend/` | Legacy Express.js — read-only unless refactoring |
| `specs/` | Planning docs, epics, bug registry, test plan (YAML > MD) |

<!-- END bigpowers:context-routing -->
