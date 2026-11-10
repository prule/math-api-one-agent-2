# Tasks

## 1. Domain (test-first)

- [x] 1.1 Write `OperationTest`: `named` finds each of ADD, SUBTRACT, MULTIPLY; rejects `add`, `NOT_AN_OPERATION` and `null` with `UnknownOperationException` listing the supported names; `apply` exact for each Operation; verify it fails to compile
- [x] 1.2 Implement `Operation` and `UnknownOperationException`; verify `OperationTest` passes
- [x] 1.3 Write `CalculationTest` for the DOMAIN-MODEL.md §3 edge cases of ADD (`0.1 + 0.2 = 0.3`, negatives, large precision), SUBTRACT (zero, negative Result), MULTIPLY (× 0, sign rules, scale growth) and BR-4 (trailing zeros stripped, zero is `0`); null components rejected; verify it fails
- [x] 1.4 Implement `Calculation`; verify `CalculationTest` passes

## 2. Application and wiring

- [x] 2.1 Implement `PerformCalculation` and `config.UseCaseConfig`; verify `./gradlew compileJava` passes (behaviour is tested through the domain tests and `CalculationControllerTest`, which runs the real use case)

## 3. Web (test-first)

- [x] 3.1 Write `CalculationControllerTest` (`@WebMvcTest` + `@Import(UseCaseConfig.class)`, `MockMvcTester`) covering every `calculation` scenario: exact Results, BR-4, number/string Operands echoed, unknown and wrong-case names, malformed JSON, missing field, non-numeric and boolean Operands, invalid-before-unknown, Problem Details shape with no class names in `detail`, the 500 unexpected-failure case, 415, 405; verify it fails
- [x] 3.2 Implement `CalculationRequest`, `CalculationResponse`, `CalculationController` and `ProblemDetailsAdvice`; verify `CalculationControllerTest` passes

## 4. Smoke and architecture

- [x] 4.1 Add one happy-path `POST /api/v1/calculations` (`0.1 + 0.2` → `"0.3"`) to `ServiceHealthSmokeTest`; verify it passes
- [x] 4.2 Remove `withOptionalLayers(true)`, `allowEmptyShould(true)` and the `shortcut:` comment from `ArchitectureTest`; verify `ArchitectureTest` and `ArchitectureRulesAreLiveTest` pass

## 5. Integration check

- [x] 5.1 Run `./gradlew clean build`; verify it passes (tests, ArchUnit, Spotless)
- [x] 5.2 Docs: PRD.md §3.2 records `about:blank` for errors outside its table (framework errors, `500`), with a change-log row; DOMAIN-MODEL.md and STANDARDS.md need no change

## Workflow follow-up

- Code review (ponytail and `/code-review`), archive, retrospective, PR and merge on green, per `CLAUDE.md`.
