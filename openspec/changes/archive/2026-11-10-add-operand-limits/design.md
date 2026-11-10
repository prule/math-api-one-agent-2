# Design

## Context

`Calculation` is a record built by `PerformCalculation` after `Operation.named`; `ProblemDetailsAdvice` maps domain exceptions; `HttpMessageNotReadableException` already maps to invalid-request. Spring Boot 4.1.2 uses Jackson 3.1.7, whose `StreamReadConstraints` default to a 1,000-character number limit and no document limit, and Boot exposes them as `spring.jackson.factory.constraints.read.*`.

## Goals / Non-Goals

**Goals:**
- No request can make the service do work or allocate memory beyond a small bound: the request limits bound parsing, BR-1 bounds the arithmetic and the echoed Operands.

**Non-Goals:**
- A request-size filter or Tomcat setting: Jackson's document limit already bounds what is read into memory for this JSON-only endpoint.
- Per-environment configuration of BR-1: business rules are code, not config.

## Decisions

### BR-1 in the `Calculation` constructor
After the null checks: `requireInRange(a, "a"); requireInRange(b, "b");` where the check is `x.precision() > 100 || x.scale() > 1000 || x.scale() < -1000` → `throw new OperandOutOfRangeException(name)`. `precision()` and `scale()` read the Operand as written (`1E+1000` has precision 1, scale −1000), which is what BR-1 says. Both are cheap for any `BigDecimal` Jackson can build, so `1e999999999` fails in microseconds. The limits are two package-private constants in `Calculation`, which `OperandOutOfRangeException` also uses to build its message, so the rule and its wording cannot drift. Two comparisons, never `Math.abs(scale)`: `Math.abs(Integer.MIN_VALUE)` is negative, so `new BigDecimal(BigInteger.ONE, Integer.MIN_VALUE)` would pass. Jackson 3.1.7 rejects such a value while parsing, so it is not reachable over HTTP today; the domain must still hold BR-1 for every caller.

Placing it in the constructor (not the use case or a validator) keeps BR-1 true for every Calculation and gives the PRD order for free: binding (invalid request) → `Operation.named` (unknown) → constructor (BR-1) → `result()` (BR-2).

### `OperandOutOfRangeException`
Plain domain `RuntimeException`. Message: "Operand b must have at most 100 significant digits and a scale between -1000 and 1000." — the name only, never the value (the value can be ~1,000 characters and is the client's own input).

### Web mapping
`@ExceptionHandler(OperandOutOfRangeException)` → `400`, type `/problems/operand-out-of-range`, title "Operand out of range", detail = message, via the existing `problem(...)` helper.

### Request limits: Boot properties, no code
```yaml
spring.jackson.factory.constraints.read:
  max-document-length: 16384
  max-number-length: 1000
  max-string-length: 1000
```
A violation throws Jackson's `StreamConstraintsException` while tokenising, which Spring wraps in `HttpMessageNotReadableException` → the existing invalid-request handler. The number limit is checked on the token's text, before a `BigDecimal` exists. `max-number-length` restates Jackson's default so the PRD limit is visible and does not move with a Jackson upgrade. The string limit also bounds `operation`, so the unknown-operation `detail`, which echoes the name, is at most ~1,000 characters.

Document length counts input units; Spring passes the body as bytes, so for non-ASCII bodies the limit is in UTF-8 bytes (stricter than characters). Accepted: every valid request is ASCII.

The invalid-request `detail` (one constant in `ProblemDetailsAdvice`) gains the limits, so a client rejected for size is told why: "The request body must be JSON with operation, a and b; a and b must be decimal numbers. The body may be at most 16,384 characters, and no value more than 1,000."

STANDARDS.md §8 says not to add custom properties that duplicate a library's switches; these are the library's own.

### Tests
- `CalculationTest`: each limit at and one past it, for `a` and `b`; `a` reported before `b`; `1e999999999` rejected (no timing needed in the domain: the check is two int comparisons).
- `CalculationControllerTest`: one out-of-range case per limit with `detail` naming the Operand and not containing the value; the validation-order scenarios; every request-limit scenario; the huge-exponent scenario wrapped in `assertTimeoutPreemptively(Duration.ofSeconds(1))`, and run once without the BR-1 check to prove it times out (then restored).
- `ServiceHealthSmokeTest` unchanged.

### Shortcut removed
The `shortcut:` comment on `Calculation.result()` goes: with BR-1, the largest Result is bounded (e.g. `1E+1000 + 1E-1000` has 2,001 digits; `1E-1000 ÷ 1E+1000` is `1E-2000`, ~2,000 characters plain).

## Risks / Trade-offs

- [Bodies padded with whitespace after the closing brace may stop being read before the document limit triggers] → the scenario pads *inside* the object, which must be read; trailing bytes after one complete JSON value are never parsed into anything.
- [The 500 handler is now unreachable from HTTP: BR-1 rejects `1e-2000000000`, the input item 2 used to reach it] → kept as the last line of "every error is Problem Details"; its test calls `ProblemDetailsAdvice.unexpected` directly, and the spec's "Unexpected failure" scenario is MODIFIED to describe any unmapped exception instead of that input. Found during apply (task 2.2), not in planning.
