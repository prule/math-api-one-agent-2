# Design

## Context

`Operation` is an enum whose constants implement `apply`; `Calculation.result()` strips trailing zeros (BR-4); `ProblemDetailsAdvice` maps domain exceptions. The backlog asks that DIVIDE need only a constant, an exception and its mapping.

## Goals / Non-Goals

**Goals:**
- DIVIDE with exactly the three production changes the backlog names, so the "adding an Operation" rule (STANDARDS.md §3) is proven.

**Non-Goals:**
- A shared `MathContext` constant or rounding helper: one Operation rounds today; POWER (item 6) can extract it when a second one needs it.

## Decisions

### `DIVIDE.apply`
```java
if (b.signum() == 0) throw new DivisionByZeroException();
return a.divide(b, MathContext.DECIMAL128);
```
`signum()` is zero for every zero representation (`0`, `0.00`, `-0` — `BigDecimal` has no negative zero — and `0E+3`), so one check covers BR-2. `divide(b, DECIMAL128)` is correctly rounded to 34 significant digits with `HALF_EVEN` (BR-3), and returns an exact quotient unrounded. The check sits in the Operation, not in `Calculation`, because BR-2 is a rule of the Operation (MODULO and POWER add their own cases later).

### `DivisionByZeroException`
A plain `RuntimeException` in `domain` with a fixed message ("Division by zero is undefined."), no fields.

### Web mapping
`@ExceptionHandler(DivisionByZeroException)` → `HttpStatus.UNPROCESSABLE_CONTENT` (422; Spring 7 deprecates `UNPROCESSABLE_ENTITY`), type `/problems/division-by-zero`, title "Division by zero", detail from the exception. Uses the existing `problem(...)` helper.

### Name-list tests
`OperationTest` and `CalculationControllerTest` asserted the literal "Supported operations: ADD, SUBTRACT, MULTIPLY." Adding DIVIDE would break both, which is the drift STANDARDS.md §3 forbids. `OperationTest` now parses the real message and checks its list equals `Operation.values()` in order (separator and order checked, names not pinned); the web test compares `detail` with the domain exception's own message, so neither copies the production join.

### Validation order
Unchanged: division by zero is found inside `apply`, after binding (invalid request) and the name lookup (unknown operation), matching PRD.md §3.2.

## Risks / Trade-offs

- [`a.divide(b, DECIMAL128)` with extreme scales throws `ArithmeticException`] → reaches the existing 500 handler; BR-1 (item 4) bounds scales.
- [No operand limits yet] → the existing `shortcut:` in `Calculation.result()` still applies; no new one.
