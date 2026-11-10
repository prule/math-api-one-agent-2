# Spec Delta

## ADDED Requirements

### Requirement: Operand limits
Each Operand, as written, SHALL have at most 100 significant digits and a scale within ±1000 (BR-1). Otherwise the Calculation SHALL be rejected, before any arithmetic, with `400` Problem Details of type `/problems/operand-out-of-range` whose `detail` names the Operand (`a` or `b`) and never contains its value. `a` is checked before `b`.

#### Scenario: Values at the limits are accepted
- **WHEN** a client posts ADD with `a` `"1E+1000"` and `b` `"1E-1000"`, or with a 100-digit integer Operand
- **THEN** the response is `200 OK`

#### Scenario: One past each limit is rejected
- **WHEN** a client posts ADD with `b` `"1E+1001"`, `"1E-1001"`, or `"1"` followed by 100 zeros (101 digits)
- **THEN** the response is `400` with type `/problems/operand-out-of-range` and a `detail` naming `b`
- **AND** the `detail` does not contain the Operand's value

#### Scenario: First out-of-range Operand is reported
- **WHEN** both `a` and `b` are out of range
- **THEN** the `detail` names `a`

#### Scenario: Huge exponents are rejected immediately
- **WHEN** a client posts `{"operation": "MULTIPLY", "a": "1e999999999", "b": "1e999999999"}`
- **THEN** the response is `400` with type `/problems/operand-out-of-range` within one second

### Requirement: Request size limits
A request body over 16,384 characters, or any JSON string or number in it over 1,000 characters, SHALL be rejected with `400` Problem Details of type `/problems/invalid-request` while the body is parsed, before any number is built.

#### Scenario: Oversized body
- **WHEN** a client posts a valid Calculation padded with whitespace to 16,385 characters
- **THEN** the response is `400` with type `/problems/invalid-request`
- **AND** the `detail` states the 16,384-character and 1,000-character limits

#### Scenario: Overlong number
- **WHEN** a client posts an Operand written as a JSON number of 1,001 digits
- **THEN** the response is `400` with type `/problems/invalid-request`

#### Scenario: Overlong string
- **WHEN** a client posts an Operand or an `operation` written as a JSON string of 1,001 characters
- **THEN** the response is `400` with type `/problems/invalid-request`

#### Scenario: String at the limit is parsed
- **WHEN** a client posts an Operand written as a JSON string of exactly 1,000 digits
- **THEN** the response is `400` with type `/problems/operand-out-of-range` (it was parsed, then failed BR-1)

### Requirement: Validation order
When a request fails more than one check, the service SHALL report only the first, in this order: invalid request → unknown Operation → operand out of range (BR-1) → division by zero (BR-2).

#### Scenario: Unknown Operation before operand limits
- **WHEN** a client posts `{"operation": "NOPE", "a": "1E+1001", "b": 1}`
- **THEN** the response is `400` with type `/problems/unknown-operation`

#### Scenario: Operand limits before division by zero
- **WHEN** a client posts `{"operation": "DIVIDE", "a": "1E+1001", "b": 0}`
- **THEN** the response is `400` with type `/problems/operand-out-of-range`

## MODIFIED Requirements

### Requirement: Every error is Problem Details
Every error response, including framework errors, SHALL be RFC 9457 Problem Details (`application/problem+json`) with `type`, `title`, `status` and a human-readable `detail` that exposes no internals (no class names, stack traces or parser messages).

#### Scenario: Problem Details shape
- **WHEN** a client posts `{"operation": "ADD", "a": "abc", "b": 1}`
- **THEN** the content type is `application/problem+json`, the body has `type`, `title`, `status` `400` and `detail`
- **AND** `detail` contains no Java class or package name

#### Scenario: Unexpected failure
- **WHEN** handling a request fails with an exception no other rule maps (BR-1 now rejects every input known to cause one, e.g. `1e-2000000000`)
- **THEN** the response is `500` Problem Details with type `about:blank` whose `detail` contains no Java class or package name and none of the exception's message

#### Scenario: Unsupported media type
- **WHEN** a client posts with content type `text/plain`
- **THEN** the response is `415` Problem Details

#### Scenario: Unsupported method
- **WHEN** a client sends `GET /api/v1/calculations`
- **THEN** the response is `405` Problem Details
