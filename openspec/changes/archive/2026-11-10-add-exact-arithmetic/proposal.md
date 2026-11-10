# Proposal

## Why

The service starts but does nothing. This is the first vertical slice through every layer: a client can get the exact Result of ADD, SUBTRACT or MULTIPLY on two Operands, and every invalid request gets the PRD's Problem Details. It also fixes the request/response shape and error handling that every later Operation reuses.

## What Changes

- Domain: `Operation` enum with ADD, SUBTRACT, MULTIPLY (each constant implements `apply`, exact), `Operation.named` (exact, case-sensitive; otherwise `UnknownOperationException`), and an immutable `Calculation` whose `result()` is normalised per BR-4.
- Application: a `PerformCalculation` use case, wired as a bean in `config`.
- Web: `POST /api/v1/calculations` accepting `operation`, `a`, `b` (Operands as JSON numbers or strings) and returning `operation`, `a`, `b`, `result` as strings (PRD §3.2).
- One `@RestControllerAdvice` returning RFC 9457 Problem Details: `/problems/invalid-request` and `/problems/unknown-operation` (400), and Problem Details for framework errors (404, 405, 415, …).
- Removes the empty-layer shortcut (`withOptionalLayers`, `allowEmptyShould`) from `ArchitectureTest`, now that every layer has a class.

**Out of scope:** DIVIDE and every other Operation; operand limits (BR-1) and request-size limits; Arity and unary Operations (BR-9 beyond "binary needs `b`", which a missing `b` already covers as invalid request); OpenAPI docs.

## Capabilities

### New Capabilities
- `calculation`: performing a Calculation over HTTP — the request and response contract, exact Results, Result normalisation, and the Problem Details a client gets for a rejected request.

### Modified Capabilities

None.

## Impact

- New code in all four layers; new domain, web-slice tests; one happy-path request added to `ServiceHealthSmokeTest`.
- `ArchitectureTest` loses its `shortcut:`.
- Docs to update: PRD.md §3.2 gains one sentence (errors outside its table, including `500`, use `type` `about:blank`) and a change-log row. Otherwise PRD.md §3.2 and DOMAIN-MODEL.md §2–§4 already describe this behaviour (they are the final v1 docs, so they also describe Operations and rules that later items add). STANDARDS.md is unchanged.
