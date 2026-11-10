# Tasks

## 1. Domain (test-first)

- [x] 1.1 Change the name-list assertion in `OperationTest` to build the expected list from `Operation.values()` (plus one check that DIVIDE is listed), and add one exact `apply` case `dividesPerBR3` (`1 ÷ 3`) and `DivisionByZeroException` for `b` = `0`, `0.00`, `-0`, `0E+3` and for `0 ÷ 0`; verify it fails to compile
- [x] 1.2 Add the DIVIDE rows to `CalculationTest`'s table, the only home for the rounding and normalisation cases: `2 ÷ 3`, both HALF_EVEN ties, exact `1 ÷ 4`, negative divisor, `100 ÷ 0.5`, `0.00 ÷ 7`; verify they fail
- [x] 1.3 Implement `Operation.DIVIDE` and `DivisionByZeroException`; verify `OperationTest` and `CalculationTest` pass

## 2. Web (test-first)

- [x] 2.1 In `CalculationControllerTest`, build the unknown-operation detail from `Operation.values()`, add DIVIDE happy paths and a 422 test per zero representation (Problem Details shape via `assertProblem`); verify the 422 cases fail
- [x] 2.2 Add the `DivisionByZeroException` handler to `ProblemDetailsAdvice`; verify `CalculationControllerTest` passes

## 3. Integration check

- [x] 3.1 Run `./gradlew clean build`; verify it passes and that production changes are limited to `Operation`, `DivisionByZeroException` and `ProblemDetailsAdvice` (`git diff --stat main -- src/main`)
- [x] 3.2 Docs: confirm PRD.md, DOMAIN-MODEL.md and STANDARDS.md need no change; if implementation deviates, update the doc and its change log

## Workflow follow-up

- Code review (ponytail and `/code-review`), archive, retrospective, PR and merge on green, per `CLAUDE.md`.
