# Security Plan — StoreBook Epic Rollout

security_plan_doc: SP1.0
last_updated: "2025-07-13"
epics_covered: [e01, e02, e03, e04, e05]
risk_model: OWASP Mobile + OWASP API Top 10

---

## A. Multi-Tenancy Enforcement (Critical)

### Threat Model

Each StoreBook store is a separate tenant. Data leakage between tenants is the highest-severity risk — one shop owner seeing another's inventory/sales/financials.

### Controls

| Control | Layer | Status | Epic Owner |
|---------|-------|--------|------------|
| `storeId` parameter on every SQL query | Android SQLite (StoreBookDbHelper) | Partially enforced | e02-s1 |
| `@auth(level: USER)` in Data Connect queries | Cloud GraphQL layer | Enforced | Existing |
| Session cookie validation on web routes | Next.js middleware | Implemented | — |
| Staff role-based access (owner/manager/cashier) | Web + Future Android | Missing | e04-s2 |

### Action Items

- **[SP-A1]** Audit all `StoreBookDbHelper` query methods for missing `WHERE store_id = ?` clauses. Any raw SQL without this filter is P0.
- **[SP-A2]** Verify Data Connect `schema.gql` mutations include `storeId: auth().uid` binding — no mutation should accept arbitrary storeId from client input without ownership verification.
- **[SP-A3]** Add middleware on Next.js API routes that checks `session.storeId === requestedStoreId` before returning data (e04-s2 story work).

---

## B. Data-at-Rest Protection

### Current State

Local SQLite on Android stores: inventory items, sales history, udhaar ledgers, expense records — all financial data.

| Control | Status | Gap |
|---------|--------|-----|
| Android Room/SQLite encryption | ❌ Not implemented | `androidx.security:security-crypto-ktx` exists in build.gradle but not wired up |
| SQLCipher fallback | N/A | Not needed if EncryptedSharedPreferences pattern is used for master key + default SQLite with access restrictions |

### Action Items

- **[SP-B1]** Migrate local DB to use `MasterKey` from `security-crypto-ktx`. StoreBookDbHelper should initialize with encrypted connection. Target: e02 implementation phase.
- **[SP-B2]** Ensure item photos (file paths in DB) are stored in app-specific external storage (`Context.getExternalFilesDir()`), not world-readable storage. Currently done — verify no path leaks via Logcat.

---

## C. Authentication & Session Security

### Firebase Auth Surface

| Concern | Current State | Risk Level | Action |
|---------|--------------|------------|--------|
| Phone OTP abuse rate limiting | Firebase default limits enabled | Low | No action needed |
| Session cookie expiry (web) | MaxAge set in `/api/session` endpoint | Medium | Verify expiry ≤ 7 days, refresh on rotation |
| Token revocation on sign-out | `auth().signOut()` calls present | Low | Add audit: verify no orphaned sessions after logout |
| Circuit Breaker for auth failures | ❌ Not implemented yet | High | e01-s2 add auth token expiry detection + backoff |

### Action Items

- **[SP-C1]** Implement circuit breaker in Android: when Firebase auth token refresh fails 3x consecutively, show logged-out state instead of infinite retry loop. Tie into e01 sync reliability work.
- **[SP-C2]** Add session rotation on web: if JWT age > half-maxAge, issue new cookie transparently (silent re-auth via Firebase SDK).

---

## D. Input Validation

### SQL Injection Prevention

| Layer | Current State | Status |
|-------|--------------|--------|
| Android SQLite | Parameterized queries via `?` placeholders | ✅ Enforced by convention (AGENTS.md) |
| Data Connect GraphQL | Generated SDK — no raw SQL from client | ✅ Safe by design |
| Web Server Actions | TypeScript types → parameterized DB calls | ✅ Safe if no template literals with user input |

### Validation Checklist for New Code

- [ ] All new Kotlin DbHelper methods use `?` parameters, never string concatenation
- [ ] Web server actions accept typed params (Zod schemas preferred for edge-case validation)
- [ ] File uploads (item photos) validated: size < 5MB, type whitelist (jpg/png), filename randomized
- [ ] Razorpay webhook signatures verified on every call (`e05-s2`)

---

## E. Dependency Security

### Critical Dependencies with Known Attack Surface

| Package | Purpose | Risk Area | Monitoring |
|---------|---------|-----------|------------|
| `com.google.firebase:firebase-auth-ktx` | Authentication | Token handling, credential storage | Dependabot auto-PRs |
| `androidx.work:work-runtime-ktx` | SyncWorker scheduler | No network risk, but DoS if worker misfires | Test: verify PeriodicWorkRequest minInterval honored |
| `@reduxjs/toolkit` + `persist` | Web state caching | Serialized session data in localStorage | Verify no sensitive secrets persisted (only UI state) |
| `razorpay` SDK | Payment processing | PCI DSS compliance via hosted forms | SDK version pinned, update quarterly |
| `com.google.android.gms:play-billing` | In-app purchases | License verification | Google Play handles entitlements, verify receipt validation on server |

### Action Items

- **[SP-E1]** Add `dependencyUpdates` Gradle plugin for automated semver bump detection
- **[SP-E2]** npm audit weekly (add to CI pipeline step)
- **[SP-E3]** Pin Firebase BOM version in root build.gradle — don't let transitive updates surprise us during sync work

---

## F. Privacy Compliance

### Data Collected

| Data Type | Purpose | Retention | User Control |
|-----------|---------|-----------|--------------|
| Phone number (Firebase Auth) | Identity verification | Until account delete | Settings → Delete Account |
| Inventory items + prices | Business operation | Until soft-deleted | Editable anytime |
| Sale transactions | Financial record-keeping | Indefinite (user chooses) | Export via CSV (future feature) |
| Udhaar ledger | Credit tracking | Until settlement | Viewable only by own store |

### Compliance Notes

- **India DPDP Act 2023**: Need privacy policy link in app settings and first-launch screen. Currently missing — add stub page pointing to `terms.storebook.in/privacy` (future infra).
- **Data Export Request**: Not implemented yet. Future addition: export all store data as JSON/CSV on demand.
- **Data Deletion**: Account deletion should cascade-delete all tenant-scoped data from Firebase + sync delete markers to SQLite next time device goes online.

---

## G. Incident Response Plan (Lightweight)

| Scenario | Severity | Response Action | Who Notified |
|----------|----------|----------------|--------------|
| Multi-tenant data leak detected | CRITICAL P0 | Freeze affected API routes, rotate auth keys, manual DB audit of leaked storeIds | All stakeholders within 1h |
| Sync corruption (5%+ divergence) | HIGH P1 | Disable auto-sync, publish emergency fix with sync hard-reset option within 24h | Engineering team |
| Firebase outage > 1h | MEDIUM P2 | Android works offline anyway. Show "Sync paused" banner on web. Wait for Firebase recovery. | Customer support via status page |
| Razorpay webhook processing failure | LOW P3 | Retry queue handles it. If persistent → manual reconciliation script. | Billing team within 24h |

---

## H. Security Review Gates (per release)

Before every release merge to `master`:

- [ ] Grep new code for string-concatenated SQL: `grep -r "\"SELECT.*\"" app/src/` — must return false positives only
- [ ] Verify no hardcoded credentials/secrets in source: search for `password`, `apiKey`, `token` literal assignments
- [ ] Run `./gradlew lint` — check for `SetJavaScriptEnabled`, `ExportedReceiver`, `HardcodedText` warnings
- [ ] Web: `npm audit --audit-level=high` — no high/critical vulnerabilities
