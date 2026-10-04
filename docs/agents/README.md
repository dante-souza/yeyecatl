# Yeyecatl Agent System

Yeyecatl uses the same three-layer idea found in Cerberus:

1. **Repository policy** — `AGENTS.md`
2. **Role agents** — `.github/agents/*.agent.md`
3. **Reusable domain skills** — `.github/skills/*/SKILL.md`

The difference is that Yeyecatl is repository-native and intentionally lightweight: there is no mandatory Jira/Confluence dependency.

## Recommended flow

| Stage | Primary agent | Useful skills |
|---|---|---|
| Product shaping | `product-owner` | `android-platform`, `wifi-scanning` |
| Technical design | `architect` | `architecture`, `android-platform`, `security-privacy` |
| RF validation | `wifi-domain` | `wifi-scanning`, `rf-analysis` |
| Implementation | `developer` / `task-worker` | task-dependent skills |
| QA | `qa-engineer` | `test-strategy`, `quality-gates` |
| Review | `reviewer` + sub-reviewers | all relevant skills |

## Agent handoff contract

Every handoff should contain:

- goal
- acceptance criteria
- files/components in scope
- skills that apply
- decisions already made
- unresolved questions/blockers
- commands used to validate

Avoid handoffs that merely say “continue from here.”
