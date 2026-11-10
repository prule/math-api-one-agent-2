# Proposal

## Why

Operands are unbounded today: one short request such as `ADD 1e999999999 + 0` makes the service build a billion-character string, and `ADD 1e999999999 + 1e-999999999` a two-billion-digit number. Any caller can exhaust CPU and memory. BR-1 and the PRD's request limits close that before anyone else calls the API (backlog item 4), and remove the `shortcut:` carried since item 2.

## What Changes

- Domain: the `Calculation` constructor enforces BR-1 on each Operand — `precision() ≤ 100` and `-1000 ≤ scale() ≤ 1000`, on the Operand as written — checking `a` then `b`, before any arithmetic. A violation raises `OperandOutOfRangeException` naming the Operand (`a` or `b`).
- Web: `OperandOutOfRangeException` → `400` Problem Details, type `/problems/operand-out-of-range`; the `detail` names the Operand and the limits, never its value.
- Request limits (PRD §3.2): a body over 16,384 characters, or any JSON string or number over 1,000 characters, is rejected as `invalid-request` while parsing, before any number is built. Set with Spring Boot's Jackson read-constraint properties; no code.
- Removes the `shortcut:` comment in `Calculation.result()`.

**Out of scope:** making the limits configurable per environment; limits on the Result (BR-6 is POWER-only, item 6); Arity (BR-9, item 7).

## Capabilities

### New Capabilities

None.

### Modified Capabilities
- `calculation`: adds the BR-1 operand limits, the request-size limits, and their place in the validation order; the "Unexpected failure" scenario no longer names an input, because BR-1 now rejects it.

## Impact

- `domain`: `Calculation` constructor and a new `OperandOutOfRangeException`. `adapter.in.web`: one `@ExceptionHandler`. `application.yaml`: three Jackson read-constraint properties.
- Tests: domain boundary tests, web-slice tests for both kinds of limit and the validation order, timing assertions on the CPU-exhaustion inputs.
- Docs to update: none. PRD.md §3.2 (request limits, validation order, error table) and DOMAIN-MODEL.md (BR-1, `OperandOutOfRangeException`, constructor order) already describe this.
