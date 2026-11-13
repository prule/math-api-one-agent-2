# Tasks

## 1. Domain: POWER

- [x] 1.1 In `OperationTest`, write failing tests for every POWER edge case in DOMAIN-MODEL §3: `2^10 = 1024`, `2^-2 = 0.25`, `3^-1` rounded, `(-2)^3 = -8`, `2^200` rounded once, `0^0 = 1`, `2^3.0 = 8`; `2^0.5`, `2^1000`, `2^-1000`, `2^1E+1000` throw `InvalidExponentException` (BR-5); `0^-1` and `0.00^-1` throw `DivisionByZeroException` (BR-2); `100^999` and `0.01^999` throw `ResultOutOfRangeException` (BR-6); `0^-1000` throws `InvalidExponentException` (BR-5 before BR-2); `1.00^999 = 1` and `0E-1000^2 = 0` (BR-6 judged after normalisation); within 1 s each, `1.0…01` (100 digits) to ±999 succeeds and `9…9` (100 nines) to ±999 throws `ResultOutOfRangeException`. Verify they fail to compile or fail.
- [x] 1.2 Add `InvalidExponentException` and `ResultOutOfRangeException` (fixed messages built from the bounds, never the Operand) and `Operation.POWER` per design.md. Verify the 1.1 tests pass.

## 2. Domain: MODULO

- [x] 2.1 In `OperationTest`, write failing tests: `-7 mod 3 = -1`, `7 mod -3 = 1`, `5.5 mod 2 = 1.5`, `-7.5 mod 2 = -1.5`, `6 mod 3 = 0`, `1E+1000 mod 7 = 4`; `b` of `0`, `0.00`, `-0` throws `DivisionByZeroException` (BR-2). Verify they fail.
- [x] 2.2 Add `Operation.MODULO` (`remainder`, zero check first). Verify 2.1 passes.

## 3. Web: errors and order

- [x] 3.1 In `CalculationControllerTest`, write failing tests for every scenario in the calculation delta: POWER and MODULO happy paths through HTTP (incl. `2^200`, `"3.0"` exponent), `422` `/problems/invalid-exponent` (fractional, ±1000), `200` at ±999, `422` `/problems/result-out-of-range` (`100^999`, `0.01^999`), `1.00^999 = 1`, the two worst-operand timings, `422` division-by-zero for MODULO zeros and `0.00^-1`, and the two new order scenarios. Verify they fail.
- [x] 3.2 Add the two handlers to `ProblemDetailsAdvice` (titles "Invalid exponent", "Result out of range"). Verify 3.1 passes.

## 4. API docs

- [x] 4.1 In `ApiDocumentationTest`, write failing assertions: `422` examples are exactly `division-by-zero`, `invalid-exponent`, `result-out-of-range`; `documentedExampleIsTheRealResponse` gains rows for the two new types; the unknown-operation example's listed names equal `Operation.values()` in order. Verify they fail.
- [x] 4.2 Add the two examples to `ProblemExamples` and widen the `422` `@ApiResponse` description in `CalculationController`. Verify 4.1 passes.

## 5. Docs and build

- [x] 5.1 Check PRD.md §3.1–3.2 and DOMAIN-MODEL.md BR-2, BR-3, BR-5–BR-7 and §3 against what was built (fix any mismatch), and add a change-log row for `add-power-modulo` to each. Verify by reading both sections.
- [x] 5.2 Run `./gradlew clean build` and verify it passes (tests, ArchUnit, Spotless).

## 6. Code review fixes

- [x] 6.1 Negative-exponent BR-6 cases (`100^-999`, `0.01^-999`) in domain and web tests, plus a spec scenario; verified they fail when BR-6 is limited to positive exponents.
- [x] 6.2 Shared `Calculation.scaleInRange` for BR-1 and BR-6; a rule-based `422` description; timing asserted per call; comments on why. Verify `./gradlew clean build` passes.

## Workflow follow-up

- Both code reviews, archive, retrospective, PR, merge on green (CLAUDE.md).
