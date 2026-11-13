# Proposal

## Why

Every Operation today takes two Operands. Clients also need a square root and a sign flip, and forcing a dummy `b` on them would make the contract lie. This is backlog item 7. It must stay non-breaking for existing clients of `POST /api/v1/calculations` (PRD §3.4).

## What Changes

- **Arity in the domain.** Each `Operation` constant declares its Arity (1 or 2) via `arity()`. `Calculation` checks BR-9 first (exactly Arity Operands: `b` absent for a unary Operation, present for a binary one), then BR-1 on each Operand present. A mismatch raises `ArityMismatchException`, which is reported as `400` `/problems/invalid-request` with a detail naming the Operation and the Operands it takes.
- **New unary Operations**, test-first:
  - `SQRT`: `√a`, rounded per BR-3 (34 significant digits, `HALF_EVEN`, correctly rounded). An `a` below zero is rejected (BR-8) with new `422` `/problems/negative-square-root`. Every zero form gives `0`.
  - `NEGATE`: `−a`, exact. Every zero form gives `0`.
- **Request and response shape.** `b` becomes optional in the request body. The response leaves out the `b` key for a unary Operation. The validation order becomes: invalid request → unknown Operation → Operand count (BR-9) → BR-1 → BR-5 → BR-2 → BR-8 → BR-6.
- **API docs.** `b` is no longer required in the request schema. The new `negative-square-root` type is documented under `422` with an example produced by the advice. The `operation` enum and the unknown-operation example follow `Operation.values()` automatically.

**Compatibility:** every request that is valid today (a binary Operation with `a` and `b`) gets the same response, byte for byte. Two small changes affect only requests that are already rejected:
- A request with both an unknown Operation and no `b` (`{"operation": "NOPE", "a": 1}`) now gets `unknown-operation` instead of `invalid-request`. It is still `400`, and it follows PRD §3.2's order. The calculation spec scenario that used this body is modified to drop `a` instead.
- A binary Operation without `b` is still `400` `invalid-request`, but its `detail` now says which Operands the Operation takes.
- The generic `invalid-request` `detail` is reworded for an optional `b` ("…operation and a, plus b for a binary operation…").

`type` and `status` are the error contract; PRD §3.2 describes `detail` as human-readable, so its wording is not part of it.

**Out of scope:** the operations discovery endpoint (item 8, which will expose `arity`); n-ary Operations; client-chosen precision.

## Capabilities

### New Capabilities

None.

### Modified Capabilities
- `calculation`: Operand count (BR-9); SQRT (BR-3, BR-8) and NEGATE; `b` optional and omitted from unary responses; the request and validation-order requirements change.
- `api-documentation`: `b` is optional in the request schema; the `negative-square-root` example is added.

## Impact

- `domain`: `Operation` gains `arity()` and `SQRT`/`NEGATE`; `Calculation` allows a null `b` and checks BR-9; new `ArityMismatchException` and `NegativeSquareRootException`. `adapter.in.web`: `CalculationRequest.b` loses `@NotNull`; `CalculationResponse` omits a null `b`; two handlers; one new example; the generic invalid-request detail is reworded for an optional `b`. `application` and `config`: no change.
- Docs: PRD.md (§3.1, §3.2 already describe SQRT, NEGATE, BR-9 and the unary request/response) and DOMAIN-MODEL.md (glossary Arity, §2, BR-3, BR-8, BR-9, §3) already match this design, because they are the original's final docs. Each gets a change-log row. STANDARDS.md §3 already says "Add the `Operation` constant with its arity (1 or 2)", so the rule doesn't change; it gets a change-log row noting that the rule is now real.
