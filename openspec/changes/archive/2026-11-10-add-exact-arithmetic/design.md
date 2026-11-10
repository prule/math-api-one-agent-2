# Design

## Context

The walking skeleton exists: four empty layer packages, ArchUnit rules with an empty-layer `shortcut:`, a smoke test, Spring Boot 4.1.2 (Jackson 3, `tools.jackson`). PRD.md and DOMAIN-MODEL.md are the final v1 docs, so they already describe Arity, BR-1 and other Operations; this change builds only the slice the proposal names.

## Goals / Non-Goals

**Goals:**
- The `calculation` spec delta, end to end, with the domain shape DOMAIN-MODEL.md §2 describes, so DIVIDE (item 3) is one new constant plus an exception.

**Non-Goals:**
- `Operation.arity()` and the BR-9 check in `Calculation`: no unary Operation exists yet, so arity would be a field every constant sets to 2. Item 7 adds it.

## Decisions

### Domain
- `enum Operation { ADD, SUBTRACT, MULTIPLY }`, each constant overriding `abstract BigDecimal apply(BigDecimal a, BigDecimal b)` with `add`, `subtract`, `multiply` (exact: no `MathContext`).
- `static Operation named(String name)`: loop over `values()` comparing `name()` with `equals` (case-sensitive). `valueOf` is not used because it throws `IllegalArgumentException` for both unknown and `null`, which would leak a JDK message; `UnknownOperationException(name)` carries the name and the supported names in `values()` order.
- `record Calculation(Operation operation, BigDecimal a, BigDecimal b)`, compact constructor `Objects.requireNonNull` on each. `result()` = `operation.apply(a, b).stripTrailingZeros()` (BR-4). Plain notation is the adapter's job (`toPlainString`), per DOMAIN-MODEL.md §2.

### Application
`PerformCalculation.perform(String operation, BigDecimal a, BigDecimal b) → Calculation` does `new Calculation(Operation.named(operation), a, b)` and returns it, so the controller reads `operation()`, `a()`, `b()`, `result()` for the response. A plain class with no Spring import; `config.UseCaseConfig` declares it as a `@Bean`.

Alternative considered: the controller calling the domain directly. Rejected: STANDARDS.md §2 puts use cases in `application`, and item 10 (history) gives it work.

### Web
- `record CalculationRequest(@NotNull String operation, @NotNull BigDecimal a, @NotNull BigDecimal b)` bound with `@Valid @RequestBody`. `operation` is a `String`, not the enum, so an unknown name reaches `Operation.named` (unknown-operation) instead of failing JSON binding (invalid-request).
- Operands bind straight to `BigDecimal`: Jackson reads a JSON number into `BigDecimal` from its text (no `double` step), and coerces a JSON string to `BigDecimal` by default. `"abc"`, `true`, an object or an array fail binding → invalid-request.
- `record CalculationResponse(String operation, String a, String b, String result)`, built with `toPlainString()`; `a` and `b` keep their scale (`1e2` → `"100"`, `"1.50"` → `"1.50"`).
- `ProblemDetailsAdvice extends ResponseEntityExceptionHandler` (`@RestControllerAdvice`). The base class already turns framework exceptions (404 `NoResourceFoundException`, 405, 415, …) into `ProblemDetail`. Overrides:
  - `handleHttpMessageNotReadable` and `handleMethodArgumentNotValid` → 400 `/problems/invalid-request`, title "Invalid request", with a fixed `detail` (e.g. "The request body must be JSON with operation, a and b; a and b must be decimal numbers.") so no parser or class name leaks.
  - `@ExceptionHandler(UnknownOperationException)` → 400 `/problems/unknown-operation`, `detail` "Unknown operation 'x'. Supported operations: ADD, SUBTRACT, MULTIPLY."
  - `@ExceptionHandler(Exception)` → 500 Problem Details (`about:blank`, fixed `detail` "The calculation could not be completed."), logging the exception. Reachable today: `BigDecimal` throws `ArithmeticException` when a Result's scale leaves the `int` range (`1e-2000000000 × 1e-2000000000`).
- `type` URIs are relative (`URI.create("/problems/...")`), as listed in PRD.md §3.2. Spring 7 leaves `ProblemDetail.type` unset, which omits it from the JSON, so `createResponseEntity` (the base class's single exit) sets `about:blank` on every framework error, and the 500 handler sets it too (`@ExceptionHandler` return values bypass `createResponseEntity`). PRD.md §3.2 records `about:blank` for errors outside its table.
- A non-string `operation` (e.g. `123`) is coerced by Jackson to the name `"123"` and reported as unknown-operation, not invalid-request. Accepted: the name is still not an Operation, and the spec only requires invalid-request for non-numeric Operands.
- Validation order (invalid request before unknown Operation) follows from binding and `@Valid` running before the controller method.

### Tests
- Domain unit tests (`OperationTest`, `CalculationTest`): DOMAIN-MODEL.md §3 edge cases for the three Operations, BR-4, `named` exact and case-sensitive.
- Web slice: `@WebMvcTest(CalculationController.class)` with `@Import(UseCaseConfig.class)`, so the real use case and domain run (no mocks); `MockMvcTester`. Covers every spec scenario, including 405 and 415.
- Smoke: one happy-path POST added to `ServiceHealthSmokeTest` (STANDARDS.md §6: one `@SpringBootTest`).

### ArchUnit
Every layer now has a class, so drop `withOptionalLayers(true)`, both `allowEmptyShould(true)` and the `shortcut:` comment. `ArchitectureRulesAreLiveTest` stays: it still guards against a rule that silently matches nothing.

## Risks / Trade-offs

- [No operand limits until item 4: `ADD 1e999999999 + 1e-999999999` builds a ~2-billion-digit number, and even echoing one Operand like `1e999999999` builds a billion-character string in `toPlainString()`, exhausting CPU and memory; an `OutOfMemoryError` bypasses the 500 handler] → accepted for two items; the service MUST NOT be deployed before item 4 (BR-1) lands.

- [Jackson 3 coercion of strings or booleans to `BigDecimal` differs from the above] → the web-slice tests for `"abc"`, `true` and number/string Operands decide; adjust with a binding setting only if a test fails.
- [`detail` echoes the client's operation name] → it is returned as a JSON string value, so no injection; length is bounded by request limits in item 4.
- [DOMAIN-MODEL.md describes `arity()` that does not exist yet] → accepted; the docs are the v1 target, and item 7 adds it.
