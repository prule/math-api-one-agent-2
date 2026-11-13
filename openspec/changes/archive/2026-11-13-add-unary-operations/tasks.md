# Tasks

## 1. Domain: Arity (BR-9)

- [x] 1.1 In `CalculationTest`, write failing tests: every binary Operation without `b` throws `ArityMismatchException`, and so does every unary Operation with `b`; BR-9 is checked before BR-1 (`SQRT` with `a` `1E+1001` and a `b` → arity); a unary Calculation's `b()` is null; `operation`/`a` null still NPE. In `OperationTest`, test `arity()` is 2 for ADD…MODULO and 1 for SQRT and NEGATE. Verify they fail.
- [x] 1.2 Add `arity` to `Operation` (constructor arg), `ArityMismatchException` (message names the Operation and its Operands, never a value), and the BR-9 check in `Calculation` before BR-1, with BR-1 only on Operands present. Verify 1.1 passes.

## 2. Domain: SQRT and NEGATE

- [x] 2.1 In `OperationTest`, write failing tests for every DOMAIN-MODEL §3 edge case: `√4 = 2`, `√0.25 = 0.5`, `√2`, `√10` rounded to 34 digits, both ties to even, `√1E+1000 = 1E+500`, `√0`, `√-0`, `√0.00 = 0`; `√-1` and `√-0.01` throw `NegativeSquareRootException` (BR-8); `NEGATE`: `5 → -5`, `-2.50 → 2.5`, `0`, `-0`, `0.00 → 0`. Verify they fail.
- [x] 2.2 Add `NegativeSquareRootException` and `Operation.SQRT(1)` / `NEGATE(1)` per design.md. Verify 2.1 passes.

## 3. Web: request, response, errors

- [x] 3.1 In `CalculationControllerTest`, write failing tests for every calculation-delta scenario: SQRT and NEGATE happy paths with no `b` key in the response; `b` as `null` is accepted for a unary Operation; a unary with `b` and a binary without `b` → `400` `invalid-request` with the arity detail; `√-1` → `422` `negative-square-root`; the new order scenarios; `{"operation": "NOPE", "b": 1}` → invalid-request; `{"operation": "NOPE", "a": 1}` → unknown-operation. Keep a binary response's `b` key (regression). Verify they fail.
- [x] 3.2 Drop `@NotNull` from `CalculationRequest.b`, add `@JsonInclude(NON_NULL)` to `CalculationResponse.b` and map a null `b` to null in `CalculationResponse.of` (it would NPE), add the two handlers, and reword the generic invalid-request detail. Verify 3.1 passes and every existing test still passes.

## 4. API docs

- [x] 4.1 In `ApiDocumentationTest`, write failing assertions: request `required` is exactly `operation`, `a`; `422` examples include `negative-square-root`; `documentedExampleIsTheRealResponse` gets a `negative-square-root` row. Verify they fail.
- [x] 4.2 Add the `negative-square-root` example to `ProblemExamples`. Verify 4.1 passes.

## 5. Docs and build

- [x] 5.1 Check PRD.md §3.1–3.2 and DOMAIN-MODEL.md (glossary, §2, BR-3, BR-8, BR-9, §3) and STANDARDS.md §3 against what was built (fix any mismatch), and add a change-log row for `add-unary-operations` to each. Verify by reading those sections.
- [x] 5.2 Run `./gradlew clean build` and verify it passes (tests, ArchUnit, Spotless).

## 6. Code review fixes

- [x] 6.1 BR-9 condition reads as the rule; NPE test for a unary Calculation with null `a`; the `200` schema documents that `b` is absent for a unary Operation (`documentsThatAUnaryResponseHasNoB`, fails without the `@Schema` description); proposal states that `detail` wording is not contract. Verify `./gradlew clean build` passes.

## Workflow follow-up

- Both code reviews, archive, retrospective, PR, merge on green (CLAUDE.md).
