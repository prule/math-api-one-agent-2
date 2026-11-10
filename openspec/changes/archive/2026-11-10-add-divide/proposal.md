# Proposal

## Why

v1 promises four basic Operations, and three exist. DIVIDE is the first Operation whose Result can need rounding (BR-3) and the first that can fail on valid input (BR-2), so it also brings the first `422`.

## What Changes

- `Operation.DIVIDE`: `a ÷ b` under `MathContext.DECIMAL128` (34 significant digits, `HALF_EVEN`), Result normalised per BR-4 like every other Operation.
- Division by any zero representation (`0`, `0.00`, `-0`, `0E+3`) raises a domain `DivisionByZeroException`, mapped to `422` Problem Details of type `/problems/division-by-zero` (BR-2).
- Tests that pinned the exact list of supported names (`ADD, SUBTRACT, MULTIPLY`) now check the list against `Operation.values()`, so adding an Operation no longer edits them (STANDARDS.md §3: "Nothing else should need to change").

**Out of scope:** POWER, MODULO and every other Operation; operand limits (BR-1); client-chosen precision or rounding; OpenAPI docs.

## Capabilities

### New Capabilities

None.

### Modified Capabilities
- `calculation`: adds division with BR-3 rounding and the BR-2 division-by-zero rejection; the unknown-operation detail now names DIVIDE too.

## Impact

- `domain`: one new `Operation` constant and `DivisionByZeroException`. `adapter.in.web`: one `@ExceptionHandler`. Nothing else in production code.
- Tests: domain and web-slice cases for DIVIDE; the two name-list assertions in `OperationTest` and `CalculationControllerTest` stop hard-coding the names.
- Docs to update: none. PRD.md §3.1–§3.2 and DOMAIN-MODEL.md (BR-2, BR-3, §3 DIVIDE row, `DivisionByZeroException`) already describe this.
