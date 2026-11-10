# Tasks

## 1. Domain: BR-1 (test-first)

- [x] 1.1 Add `CalculationTest` cases: `1E+1000`, `1E-1000`, `-1E+1000` and a 100-digit integer accepted as `a` and as `b`; `1E+1001`, `1E-1001` and a 101-digit integer rejected with `OperandOutOfRangeException` naming the Operand, for `a` and for `b`; both out of range names `a`; `1e999999999` rejected; the message never contains the value; verify it fails to compile
- [x] 1.2 Implement the BR-1 check in the `Calculation` constructor and `OperandOutOfRangeException`; remove the `shortcut:` comment on `result()`; verify `CalculationTest` passes

## 2. Web: operand-out-of-range and validation order (test-first)

- [x] 2.1 Add `CalculationControllerTest` cases: one per BR-1 limit → `400 /problems/operand-out-of-range`, `detail` naming the Operand and not containing its value; both out of range → names `a`; unknown Operation before BR-1; BR-1 before division by zero; the huge-exponent request within `assertTimeoutPreemptively(1 s)`; verify they fail
- [x] 2.2 Add the `OperandOutOfRangeException` handler to `ProblemDetailsAdvice`; verify the tests pass
- [x] 2.3 Prove the timing test can fail: temporarily disable the BR-1 check, verify the huge-exponent test fails (timeout or error), restore it

## 3. Request limits (test-first)

- [x] 3.1 Add `CalculationControllerTest` cases: body padded inside the object to 16,385 characters; a 1,001-digit JSON number Operand; a 1,001-character string Operand and `operation`; each → `400 /problems/invalid-request` with a `detail` stating the limits; a 1,000-digit string Operand → `operand-out-of-range`; verify the first four fail
- [x] 3.2 Add the three `spring.jackson.factory.constraints.read.*` properties to `application.yaml` and the limits to the invalid-request `detail`; verify the tests pass

## 4. Integration check

- [x] 4.1 Run `./gradlew clean build`; verify it passes and `git grep 'shortcut:'` finds nothing in `src/`
- [x] 4.2 Docs: confirm PRD.md, DOMAIN-MODEL.md and STANDARDS.md need no change; if implementation deviates, update the doc and its change log

## Workflow follow-up

- Code review (ponytail and `/code-review`), archive, retrospective, PR and merge on green, per `CLAUDE.md`.
