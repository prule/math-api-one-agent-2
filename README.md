# math-api (one-agent rerun)

> Demonstration project: a rebuild of [`math-api`](https://github.com/prule/math-api) from the same PRD, domain model,
> standards and backlog, using the original's single-agent workflow with every review in place from the first change.
> The multi-agent rebuild is [`math-api-sdlc-pipeline`](https://github.com/prule/math-api-sdlc-pipeline).

A stateless REST service for exact decimal arithmetic. See [docs/PRD.md](docs/PRD.md).

## Comparing the builds

| | Original `math-api` | This rerun | `math-api-sdlc-pipeline` |
|---|---|---|---|
| Workflow | One agent; reviews added at PR #7, `/code-review` at PR #12 | One agent; propose → ponytail plan review → apply → ponytail + `/code-review` → archive → retro, from item 1 | Multi-agent pipeline with two human gates |
| Backlog | `docs/BACKLOG.md`, items 1–8 | [docs/BACKLOG.md](docs/BACKLOG.md), the same items and prompts (5b dropped, 5a given a prompt) | `BACKLOG.md`, B-1–B-9 as use cases |
| Docs at start | Grew from a PRD and domain model | The original's final PRD, DOMAIN-MODEL and STANDARDS, change logs reset | Same as this rerun |
| Records | `openspec/changes/archive/*/retrospective.md` (from item 6) | `openspec/changes/archive/*/retrospective.md` (every item), `reports/sessions/` | `retrospectives/`, `reports/sessions/` |

## Building it

See [CLAUDE.md](CLAUDE.md) for the change workflow.
