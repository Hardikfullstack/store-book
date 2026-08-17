# StoreBook Monorepo Project

This is an offline-first Android POS + cloud-sync Next.js web dashboard for Indian small businesses. Android is Kotlin/Compose + SQLDelight; web is TypeScript + Next.js 16 + Redux Toolkit; sync goes through Firebase Data Connect → Cloud SQL PostgreSQL. Domain logic is shared via the KMP `:shared` module. The project is multi-tenant — every table carries `storeId`.

## Project Structure
- `app/` - Android app: Compose UI, ViewModels, SQLDelight-backed data layer, `SyncWorker`
- `shared/` - KMP module with domain models + SQLDelight schema (`.sq` files), targets android/jvm/ios
- `web/` - Next.js 16 App Router dashboard, Redux Toolkit + redux-persist
- `dataconnect/schema/` - Firebase Data Connect GQL schema/queries/mutations (source of truth); generated TS SDK lands in `web/src/dataconnect/`
- `specs/` - Planning docs, epics, bug registry, test plan (YAML is source of truth over Markdown)
- `backend/` - Legacy Express.js remnant, read-only unless explicitly asked

## Code Standards
- TypeScript strict mode across `web/`; no `any`, no raw SQL interpolation anywhere
- Kotlin package root: `com.storebook.inventoryapp.{data,ui,services,workers}`
- Every query/mutation touching a tenant-scoped table must carry `storeId` — no exceptions
- SQLDelight schema changes go through a `.sq` file edit + Gradle regeneration — there is no manual migration method
- Defensive patterns in scope: Retry (`SyncWorker`), Circuit breaker (Firebase auth), Timeout (Data Connect queries)

## Monorepo Conventions
- Web routes follow `web/src/app/{route}/page.tsx` + `{route}/{Route}Client.tsx`
- Roborazzi snapshots live in `app/src/test/snapshots/`, committed to repo, 0.5% pixel-diff threshold
- Write specs to `specs/` before generating code for any non-trivial feature or fix
- SQLDelight database class is `StoreBookDatabase` (`shared/build.gradle.kts`)

## Environment
- `web/.env.local` (Firebase config, Razorpay keys) is gitignored — ask the user if missing, never fabricate values
- Android Java home: `/opt/android-studio/jbr` (`.vscode/settings.json`)

## Commands
| Action            | Command                                                   |
|-------------------|-----------------------------------------------------------|
| Android Build     | `./gradlew :app:assembleDebug`                            |
| Android Install   | `./gradlew :app:installDebug`                             |
| Android Lint      | `./gradlew lintKtLintCheck` (ktlint 1.5.0 in `:app`)      |
| Android Unit Test | `./gradlew :shared:jvmTest`                                |
| Android Snapshot  | `./gradlew verifyRoborazziDebug`                            |
| Android Record    | `./gradlew recordRoborazziDebug` (updates golden images)  |
| Web Dev           | `cd web && npm run dev`                                    |
| Web Build         | `cd web && npm run build`                                  |
| Web Lint          | `cd web && npm run lint`                                    |
| Web Type Check    | `cd web && npm run type-check`                              |

No CI configured — all gates are local.

## Pre-Commit Checks
Run these after any code change, in order, fixing and re-running until all pass:
1. `./gradlew assembleDebug`
2. `./gradlew :app:ktlintCheck`
3. `./gradlew :shared:jvmTest`
4. `cd web && npm run lint`
5. `cd web && npm run type-check`

## Never
- Never commit Firebase credentials, `service-account.json`, or `local.properties`
- Never bypass `storeId` multi-tenancy guards in any query or mutation
- Never hard-code absolute paths outside `app/` or `web/`
- Never modify `backend/` unless explicitly asked
- Never modify `eslint.config.js`, `.prettierrc`, or add `eslint-disable` comments to bypass rules without explicit user approval — fix the underlying code, or ask if a rule seems wrong

## Directory-Specific Guidance

CRITICAL: When a task touches a path below, use your Read tool to load the matching file before editing in that area. Load only what's relevant to the current task — don't preemptively load all of them. Treat loaded content as mandatory, layered on top of the rules above.

| If working in...         | Read...                 | Covers |
|---------------------------|--------------------------|--------|
| `app/**`                  | @app/AGENTS.md           | Compose screens, ViewModels, tests, Roborazzi snapshots |
| `shared/`                  | @shared/AGENTS.md        | KMP domain models, SQLDelight schema (`.sq`), JVM unit tests |
| `web/src/app/**`           | @web/AGENTS.md           | Next.js App Router pages + Server Actions |
| `web/src/components/**`    | @web/AGENTS.md           | Shared React components |
| `web/src/lib/**`           | @web/AGENTS.md           | Firebase init, session, billing engine, utils |
| `web/src/store/**`         | @web/AGENTS.md           | Redux Toolkit slices (cart, inventory, udhaar) |
| `dataconnect/schema/`      | @dataconnect/AGENTS.md   | Firebase Data Connect schema + queries + mutations (GQL) |
| `web/src/dataconnect/`     | @dataconnect/AGENTS.md   | Auto-generated TS SDK — do not edit directly |
| `backend/`                  | @backend/AGENTS.md       | Legacy Express.js — read-only unless refactoring |
| `specs/`                    | @specs/AGENTS.md         | Planning docs, epics, bug registry, test plan (YAML > MD) |

Follow references recursively if a directory file itself points to something more specific.
