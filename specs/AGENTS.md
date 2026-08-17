# specs/ — Planning Docs

Scope: `specs/`

## Structure
- Contains epics, the bug registry, and the test plan.
- **YAML is the source of truth here, not Markdown.** If a `.yaml`/`.yml` file and a `.md` file describe the same thing and disagree, the YAML wins — flag the conflict to the user rather than silently picking one.

## Workflow
- Write to `specs/` **before** generating code for any non-trivial feature or fix — capture the plan/spec first, then implement against it.
- When implementing a spec, keep the spec file updated if the implementation diverges from what was planned (status fields, scope changes, etc.) rather than letting it go stale.
