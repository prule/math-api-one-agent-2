# Proposal

## Why

v1 offers four Operations. Clients also need integer powers and remainders, and doing them client-side brings back the decimal handling this API exists to remove. This is the first v2 item (backlog item 6). POWER needs bounds, because an unbounded exponent is a CPU-exhaustion hole.

## What Changes

- New Operations `POWER` and `MODULO`, as new `Operation` constants, test-first:
  - `POWER`: `a ^ b`, where `b` is an integer with |b| ≤ 999 (BR-5). The exact power is rounded once to 34 significant digits (BR-3). A negative `b` means `1 / a^|b|`, and `a^0 = 1`, including `0^0`. `0 ^ negative` is division by zero (BR-2). A Result whose scale after normalisation falls outside ±1000 is rejected (BR-6).
  - `MODULO`: `a mod b`, the exact remainder truncated toward zero, so it takes the sign of `a` (BR-7). `b = 0` is division by zero (BR-2).
- Two new `422` Problem Details types, each with a domain exception and a handler: `/problems/invalid-exponent` (BR-5) and `/problems/result-out-of-range` (BR-6). The validation order becomes: invalid request → unknown operation → BR-1 → BR-5 → BR-2 → BR-6.
- API docs: the new types are documented under `422` with examples produced by the advice (`ProblemExamples`), and the `422` description covers them. The `operation` enum and the unknown-operation example already come from `Operation.values()`. A new `ApiDocumentationTest` assertion pins that the example's listed names equal `Operation.values()` (v2 audit).

**Prompt deviation, recorded:** the backlog prompt says the calculation spec's "Unsupported name" scenario uses `POWER`. It does not: item 2 already wrote it with `NOT_AN_OPERATION`, a name that will never be an Operation. No MODIFY is needed. The api-documentation scenario that hard-codes "today `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`" is modified instead, so it stops drifting.

**Milestone-close audit (before v2):** `/ponytail:ponytail-audit` found the repo healthy, with one nice-to-have. An Operand whose exponent overflows an `int` (`"1e2147483648"`) is reported as `invalid-request` rather than `operand-out-of-range`. **Skipped:** both are `400`, nothing leaks, the value is still rejected before any arithmetic, and telling the two apart would add parsing logic to the adapter for input no client sends.

**Out of scope:** unary Operations and arity (item 7); the operations discovery endpoint (item 8); fractional exponents; client-chosen precision.

## Capabilities

### New Capabilities

None.

### Modified Capabilities
- `calculation`: adds POWER (BR-5, BR-6, BR-3) and MODULO (BR-7); widens division by zero (BR-2) to MODULO and `0 ^ negative`; extends the validation order.
- `api-documentation`: the allowed-operations scenario stops naming a fixed list; the two new `422` types are documented with real examples; the unknown-operation example must list exactly the supported Operations.

## Impact

- `domain`: `Operation.POWER`, `Operation.MODULO`, `InvalidExponentException`, `ResultOutOfRangeException`. `adapter.in.web`: two handlers in `ProblemDetailsAdvice`, two examples in `ProblemExamples`, and the `422` `@ApiResponse` description. No new dependency, no config change.
- Tests: `OperationTest` (every POWER/MODULO edge case in DOMAIN-MODEL §3), `CalculationControllerTest` (new error paths and the order), `ApiDocumentationTest`.
- Docs: PRD.md §3.1, §3.2 and DOMAIN-MODEL.md (BR-2, BR-3, BR-5–BR-7, §3) already describe POWER and MODULO, because they are the original's final docs; each gets a change-log row. PRD §6 roadmap has no POWER/MODULO row to remove. STANDARDS.md: none.
