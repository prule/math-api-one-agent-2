## MODIFIED Requirements

### Requirement: Calculation request and response documented
The description SHALL document `POST /api/v1/calculations` with its request body (`operation` and `a` required, `b` optional (BR-9); `a` and `b` accept a JSON number or string; `operation` lists exactly the supported Operation names) and its `200` response as `application/json` with `operation`, `a`, `b` and `result` as strings.

#### Scenario: Allowed operations come from the domain
- **WHEN** the description is read
- **THEN** the request's `operation` enumerates exactly the supported Operations, in declaration order

#### Scenario: Operands accept number or string
- **WHEN** the description is read
- **THEN** the request's `a` and `b` each allow both `number` and `string`

#### Scenario: Success response is JSON strings
- **WHEN** the description is read
- **THEN** the `200` response has media type `application/json` (not `*/*`) and every property of its schema is a `string`

#### Scenario: Unary response documented without b
- **WHEN** the description is read
- **THEN** the `200` schema's `b` property says it is absent for a unary Operation

#### Scenario: b is optional
- **WHEN** the description is read
- **THEN** the request schema's `required` list is exactly `operation` and `a`

### Requirement: Every Problem Details response documented with examples
The description SHALL list, for `POST /api/v1/calculations`, the `400`, `415` and `422` responses as `application/problem+json`, with one example per Problem Details type the endpoint can return (listed in the first scenario). Each example SHALL be the whole body the API actually returns for that error, and the documented `ProblemDetail` schema SHALL list only the fields that appear on the wire.

#### Scenario: Error responses and examples
- **WHEN** the description is read
- **THEN** `400` has examples whose `type` is `/problems/invalid-request`, `/problems/unknown-operation` and `/problems/operand-out-of-range` (BR-1), and `422` has examples whose `type` is `/problems/division-by-zero` (BR-2), `/problems/invalid-exponent` (BR-5), `/problems/negative-square-root` (BR-8) and `/problems/result-out-of-range` (BR-6)

#### Scenario: Schema lists only wire fields
- **WHEN** the description is read
- **THEN** the `ProblemDetail` schema's properties are exactly `type`, `title`, `status`, `detail` and `instance`

#### Scenario: Unsupported media type documented
- **WHEN** the description is read
- **THEN** `POST /api/v1/calculations` also lists a `415` response as `application/problem+json`

#### Scenario: Examples match real responses
- **WHEN** the API is sent a request that fails with each documented type
- **THEN** the response body's `type`, `title`, `status`, `detail` and `instance` equal the documented example's

#### Scenario: Unknown-operation example lists exactly the supported Operations
- **WHEN** the description is read
- **THEN** the Operation names listed in the `unknown-operation` example's `detail` equal the supported Operations, in declaration order
