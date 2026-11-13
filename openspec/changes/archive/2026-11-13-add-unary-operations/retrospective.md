# Retrospective: add-unary-operations

Design decision (backlog prompt): Arity is an `int` field on the `Operation` enum, not separate unary and binary types, and `b` is optional in the request, with BR-9 enforced in the domain. This keeps one `Operation.values()` for the docs, the unknown-operation detail and item 8's listing. Every request that was valid before gets the same response.

## Caught by review

Plan review (ponytail, on the planning artifacts):
1. **Task 3.2 didn't mention `CalculationResponse.of`**, which called `b().toPlainString()` and would NPE (a 500) for a unary Calculation. Added to 3.2. Task 3.1's tests would also have caught it.

Code review, ponytail: nothing found.

Code review, `/code-review` standards axis:
2. **The BR-9 condition `(b == null) != (operation.arity() == 1)` had to be decoded.** Rewritten as `operation.arity() == 1 ? b != null : b == null`, which reads as BR-9 does. It is a refactor, covered by `requiresExactlyTheArityOfOperandsPerBR9`.

Left as is, with reasons: `Operation.isUnary()` (two uses; `int arity()` is the documented API and item 8 returns it as a JSON integer). A null `b` checked where it is read (DOMAIN-MODEL §2 defines a unary `b` as null). SQRT/NEGATE rows at both test levels (the same as MODULO/POWER, and STANDARDS §6).

Code review, `/code-review` spec axis:
3. **No test that a unary Calculation with a null `a` throws an NPE** (task 1.1 named it). Added to `rejectsMissingComponents`. It passes because `requireNonNull(a)` runs before BR-9; it guards that order.
4. **The OpenAPI `200` schema didn't say `b` can be missing.** Added `@Schema(description = "Absent for a unary Operation")` and `documentsThatAUnaryResponseHasNoB`, plus a spec scenario. The test fails without the description (checked).
5. **The compatibility note left out two changed details.** The proposal now lists the reworded generic `invalid-request` detail and says `type` and `status` are the error contract, while `detail` is human-readable (PRD §3.2).

## Surprises

- **A scripted edit stopped part-way through the review fixes.** The exact-match text for `CalculationResponse` no longer existed because `spotlessApply` had already split the record header across lines. Two edits had applied and the rest hadn't, while the build after it still passed. Cause: the same habit the `disable-api-docs-in-prod` retrospective warned about (exact-match edits after Spotless). Caught because the script asserts each match; the rest were applied against the file as it now reads.
- **Making `b` optional changed one existing scenario's outcome.** `{"operation": "NOPE", "a": 1}` used to be `invalid-request` (missing field), and is now `unknown-operation`, because the Operation name is checked before the Operand count. Expected from PRD §3.2's order; the scenario was modified to drop `a` instead, and the change is recorded in the proposal and the PRD change log.

## Shortcuts taken

None.

## Lessons

- **Apply and review habit:** after any `spotlessApply`, re-read the target lines before a scripted exact-match edit. Better, run the whole batch of edits before formatting. Second time this bit; the assert-per-edit pattern caught it both times, so keep that.
- **Propose step:** when a field goes from required to optional, list every documented artifact that describes it (request schema, response schema, examples) in the tasks. Only the request side was planned; review found the response side.
