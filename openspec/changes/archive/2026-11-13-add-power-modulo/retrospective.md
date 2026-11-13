# Retrospective: add-power-modulo

Milestone close: `/ponytail:ponytail-audit` ran on the whole repo before this first post-v1 item. It found the repo healthy, with one nice-to-have: an Operand whose exponent overflows an `int` (`"1e2147483648"`) is reported as `invalid-request`, not `operand-out-of-range`. Skipped, and recorded in the proposal: both are `400`, nothing leaks, the value is still rejected before any arithmetic, and the fix would add parsing logic to the adapter for input no client sends.

## Caught by review

Plan review (ponytail, on the planning artifacts):
1. **No test pinned BR-6 "after normalisation".** Checking the scale before `stripTrailingZeros` would wrongly reject `1.00^999` (raw scale 1,998), and no planned case would catch that. Added `1.00^999 = 1` and `0E-1000^2 = 0` to the domain tests, `1.00^999` through HTTP, and a spec scenario.
2. **The "worst operands" scenario didn't say what to expect.** `99…9 ^ 999` is out of range (422), so a test could time only one path. The scenario now names both: `1.0…01` gives 200 and `9…9` gives 422 result-out-of-range, each within 1 s.

Code review, ponytail: nothing found.

Code review, `/code-review` standards axis:
3. **Hand-written `422` description** ("Division by zero, invalid exponent or result out of range") would need an edit for every new 422 type. Now rule-based: "Valid input that breaks a business rule; one example per type".
4. **POWER duplicated BR-1's scale check.** Extracted `Calculation.scaleInRange(int)`, used by BR-1 and BR-6 (which DOMAIN-MODEL defines as "the BR-1 bounds"). It is a refactor, covered by the existing BR-1 and BR-6 tests.
5. **The double `stripTrailingZeros` looked redundant.** Added a comment: POWER strips because BR-6 judges the normalised Result.
6. **The new docs test's comment said where it came from, not why it exists.** It now says the example is built once at startup and the test proves it lists every Operation.
7. **The test helper sat between tests.** Moved it to the end of `OperationTest`.

Left as is, with reasons: MODULO repeats DIVIDE's two-line zero check (too small to share). `MAX_EXPONENT` lives in `Operation`, because BR-5 is POWER's own rule. The POWER/MODULO happy paths are repeated in the web slice, matching the existing DIVIDE rows and task 3.1. The message parsing in the docs test is the same deliberate approach as `OperationTest`, so the test doesn't pin the names.

Code review, `/code-review` spec axis:
8. **Negative-exponent BR-6 was untested** (`100^-999`, `0.01^-999`). Added to the domain and web tests, plus a spec scenario. Restricting BR-6 to positive exponents makes 6 tests fail (checked).
9. **The two worst-operand calls shared one 1 s budget**, which is stricter than "each response" and could flake on CI. Each call is now timed on its own.
10. **The PRD change-log called this the "first v2 item"**, but PRD §3.4 says new Operations stay in API v1. Reworded to "first post-v1 backlog item; still API v1".

## Surprises

- **The backlog prompt was out of date.** It said the calculation spec's "Unsupported name" scenario uses `POWER`, but item 2 had already written it with `NOT_AN_OPERATION`. Cause: the prompt was copied from the original project, where that scenario differed. Recorded as a prompt deviation in the proposal; no MODIFY was needed.
- **The new docs guard test passed immediately in the red step.** The unknown-operation example is generated from `Operation.values()`, so there was nothing to fix. It is a drift guard, not a test for new behaviour.
- **Strict validation of the synced `api-documentation` spec failed at archive.** The modified requirement was over 500 characters after the new types were added to its description. Cause: the propose step validated the delta, where MODIFIED length is not flagged; only the main spec is checked. Fixed by moving the type list into the scenario, in both the delta and the main spec.

## Shortcuts taken

None.

## Lessons

- **Propose step:** when a MODIFIED requirement's description grows, check its length (500 characters) before archive; `openspec validate <change> --strict` won't flag it, but the main-spec validation will.
- **Propose step:** check each backlog prompt's claims about existing specs against `openspec/specs/` before writing the plan; prompts copied from the original can be stale.
- **Plan review habit:** for each bounds rule, ask which test fails if the check moves (before or after normalisation, positive-only); both review rounds here found one.
