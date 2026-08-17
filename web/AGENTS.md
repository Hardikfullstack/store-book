# web/ — Next.js Dashboard

Scope: `web/src/app/**`, `web/src/components/**`, `web/src/lib/**`, `web/src/store/**`

## Structure
- Next.js 16 App Router with turbopack enabled. `next-pwa` is disabled in dev.
- Route pattern: `web/src/app/{route}/page.tsx` + `{route}/{Route}Client.tsx` — server component page delegates to a client component; don't collapse this pattern into a single file.
- Server Actions live under `web/src/app/api/`.
- `web/src/lib/` holds Firebase init, session handling, the billing engine, and general utils — treat billing engine changes as high-risk, small diffs.
- `web/src/store/` is Redux Toolkit: slices for cart, inventory, udhaar. Combined with redux-persist for client-side caching — be deliberate about what gets persisted (avoid persisting anything tenant-sensitive that should refresh from source).
- `web/src/dataconnect/` is auto-generated from `dataconnect/schema/` — never hand-edit it; regenerate instead (see `@dataconnect/AGENTS.md`).

## Environment
- Requires `web/.env.local` (Firebase config, Razorpay keys). Gitignored — ask the user if it's missing rather than fabricating values.

## Conventions
- No raw SQL interpolation anywhere in this tree — all data access goes through the generated Data Connect SDK.
- Every query/mutation touching a tenant-scoped table must carry `storeId`.
- Do not modify `eslint.config.js` or `.prettierrc`, and do not add `eslint-disable` comments to bypass a rule without explicit user approval — fix the underlying code, or ask if a rule seems wrong for the case.

## Commands
- Lint: `npm run lint` (or `npx eslint . --fix` / `npx eslint <file> --fix` for a single file)
- Type check: `npx tsc --noEmit` (or `npm run type-check`)
- Dev server: `npm run dev`
- Build: `npm run build`

## Before finishing
`npm run lint` and `npm run type-check` must both pass.
