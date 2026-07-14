<!-- BEGIN bigpowers:project -->
# StoreBook — AI Agents

Read **CONVENTIONS.md** before any GitHub or git operation.

## Project
Multi-tenant retail inventory management platform for Indian small businesses. Offline-first Android POS with cloud sync to a Next.js web dashboard.
Stack: Kotlin + Jetpack Compose + SQLite (Android) / TypeScript + Next.js 14 + Redux Toolkit (Web) / Firebase Data Connect → Cloud SQL PostgreSQL / KMP shared module / Firebase Auth (Phone OTP + Email/Password)

## Commands
| Action       | Command                                      |
|---|---|
| Android Run  | `./gradlew :app:installDebug`                 |
| Android Build| `./gradlew :app:assembleDebug`                |
| Android Test | *(none yet)*                                 |
| Android Lint | *(none yet — no ktlint configured)*           |
| Web Run      | `cd web && npm run dev`                       |
| Web Build    | `cd web && npm run build`                     |
| Web Test     | *(none yet)*                                 |
| Web Lint     | `cd web && npm run lint`                      |
| Preflight    | `echo "no tests yet" && cd web && npm run lint && npm run build` |
| CI           | `gh pr checks` (when a PR is open)            |

## Architecture
Android app handles local CRUD on SQLite; `SyncWorker` pushes/pulls via Firebase Data Connect → Cloud SQL PostgreSQL. Web dashboard uses Next.js Server Actions + Data Connect SDK with Redux-persist caching. KMP shared module (`shared/`) provides domain models to both platforms. Multi-tenancy enforced via `storeId` on every table.

## Conventions
- Kotlin packages: `com.storebook.inventoryapp.{data,ui,services,workers}` — follow existing structure
- Web app: Next.js App Router colocation under `web/src/app/{route}/`
- All planning and specifications MUST be written to `specs/` before code is generated
- SQL queries use parameterized strings; no raw interpolation
- Defensive categories in scope: **Retry** (SyncWorker network calls), **Circuit breaker** (Firebase auth failures), **Timeout** (Data Connect queries)

## Never
- Never dismiss reproducible gate failures as pre-existing or out of scope
- Never proceed on red Preflight or red CI — invoke quick-fix or fix-bug first
- Never commit Firebase credentials, service-account.json, or local.properties to git
- Never directly modify SQLite schema tables without updating `StoreBookDbHelper.onUpgrade()` migration
- Never bypass `storeId` multi-tenancy guards in any query or mutation
- Never write code that hard-codes absolute paths outside the app/web modules

## Agent Rules
- **Workflow Mandate:** You MUST use the bigpowers skills (e.g. `plan-work`, `develop-tdd`, `orchestrate-project`) to perform tasks. DO NOT write code directly in response to a user prompt like "build this feature".
- **Always Green:** Preflight and CI must be green before forward work. Reproducible gate failures require **fix-or-log** (quick-fix → fix-bug) per CONVENTIONS § Discovered Defects.
- Read `specs/` before writing code.
- Write the minimum code that solves the stated problem. Nothing extra.
- Run tests after every change (when available). Show evidence before declaring done.
- One clarifying question beats a wrong assumption baked into 200 lines.

<!-- END bigpowers:project -->

<!-- BEGIN bigpowers:context-routing -->
## Context Routing

| Glob Pattern | Sub-doc |
|---|---|
| `app/**` | Android Kotlin source — Jetpack Compose screens, ViewModels, Repository |
| `web/src/app/**` | Next.js App Router pages + Server Actions |
| `web/src/components/**` | Shared React components |
| `web/src/lib/**` | Firebase init, session, billing engine |
| `web/src/store/**` | Redux Toolkit slices (cart, inventory, udhaar) |
| `dataconnect/**` | Firebase Data Connect schema + queries + mutations (GQL) |
| `shared/**` | KMP shared module — domain models |
| `backend/**` | Legacy Express.js (dev remnant — do not modify unless refactoring) |
| `docs/**` | Project specification documents (read-only reference) |

<!-- END bigpowers:context-routing -->

<!-- BEGIN bigpowers:learned-preferences -->
## Learned User Preferences

*(To be populated by session-state skill as patterns emerge.)*

## Workspace Facts

*(To be populated by session-state skill.)*

<!-- END bigpowers:learned-preferences -->
