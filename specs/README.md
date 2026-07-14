# Specs

All planning documents for this project. Follows bigpowers spec convention: YAML is source of truth over markdown.

## Directory Layout

```
specs/
├── state.yaml                  - active flow, epic, git hash, handoff
├── release-plan.yaml           - target version, WSJF epic index
├── execution-status.yaml       - flat story/epic status
├── planning-status.yaml        - discover-phase checklist
├── product/                    - VISION, SCOPE, GLOSSARY (YAML)
│   └── snapshots/              - historical spec snapshots
├── tech-architecture/          - tech-stack, security, test plan, design plan
├── adr/                        - architecture decision records
├── verifications/              - verification artifacts
├── bugs/                       - BUG-*.md + registry.yaml
└── epics/archive/              - completed epic shards
```
