# Design

## Context

`Operation` is an enum of constants that each implement `apply(a, b)`. `Calculation(operation, a, b)` requires all three to be non-null and checks BR-1 on both Operands. `CalculationRequest` marks `operation`, `a` and `b` `@NotNull`, so a missing `b` fails bean validation and becomes `invalid-request`. `CalculationResponse.of` renders all four fields. DOMAIN-MODEL §2 already describes `Operation.arity()`, a `b` that is null for a unary Operation, and `ArityMismatchException`.

## Goals / Non-Goals

**Goals:** a non-breaking way to express Arity; BR-9 enforced in the domain; SQRT and NEGATE as plain constants.

**Non-Goals:** a separate unary endpoint or request type; an `arity` field in responses (item 8).

## Decisions

- **Arity is an enum field, not separate types.** `ADD(2) … SQRT(1), NEGATE(1)`, with `public int arity()`, and the single `apply(BigDecimal a, BigDecimal b)` keeps its signature (a unary constant ignores `b`). Alternative: sealed `UnaryOperation` / `BinaryOperation` types. Rejected: `Operation.named`, `Operation.values()` (the docs enum, the unknown-operation detail, and item 8's listing) and the use case would all have to merge two sets, for no rule they can't express with an int. It also matches DOMAIN-MODEL §2 and STANDARDS §3 as written.
- **`b` is optional in the request; BR-9 decides, in the domain.** `CalculationRequest.b` drops `@NotNull`, and `Calculation`'s constructor checks `(b == null) != (operation.arity() == 1)` → `ArityMismatchException`, before BR-1. Alternative: validate arity in the adapter. Rejected: arity is a business rule (BR-9), and STANDARDS §2 keeps business rules out of `adapter.in.web`. Alternative: ignore a `b` sent with a unary Operation. Rejected: PRD §3.2 and BR-9 say reject, and silently ignoring input hides client bugs. JSON `null` and an absent `b` both bind to null, so both count as absent (PRD: "omitted (or `null`)").
- **The arity mismatch is `400` `invalid-request` with its own detail.** The handler uses the exception's message, e.g. "SQRT takes one operand, a; omit b." or "ADD takes two operands, a and b." It names no value. The generic parse-error detail becomes "The request body must be JSON with operation and a, plus b for a binary operation; a and b must be decimal numbers. …". The documented `invalid-request` example stays the parse error (one example per type).
- **Unary responses omit `b`.** The `b` component of `CalculationResponse` gets `@JsonInclude(NON_NULL)`, so binary responses are unchanged and unary ones have no `b` key (PRD §3.2). Alternative: a separate unary response record. Rejected: two schemas for one endpoint.
- **SQRT uses `BigDecimal.sqrt(DECIMAL128)`.** On JDK 25 it is correctly rounded with `HALF_EVEN`. Checked: `√2`, `√10`, and the tie `1.0000000000000000000000000000000005²` → `1.000000000000000000000000000000000` (even), `…15²` → `…002`. Zero in any form returns zero, and normalisation turns it into `0`. The BR-8 check is `a.signum() < 0`, so `-0` is not below zero. Cost: under 1 ms for any BR-1 Operand. Its Result's scale is about half the Operand's, so BR-6 can't trigger.
- **NEGATE is `a.negate()`**: exact, and `-0` / `0.00` normalise to `0`.

## Risks / Trade-offs

- [A client that relied on getting `invalid-request` for `{"operation": "NOPE", "a": 1}`] → both are `400`; PRD §3.2 already documents the new order; recorded in the proposal.
- [`apply(a, null)` called on a binary constant outside `Calculation` would NPE] → `Calculation` is the only caller, and it enforces BR-9 first. `Operation.apply` is a domain method, not a trust boundary.
