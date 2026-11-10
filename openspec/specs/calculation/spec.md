# calculation Specification

## Purpose
Lets a client service get the exact Result of applying one Operation to its Operands over HTTP, or a Problem Details response that says why the Calculation was rejected.

## Requirements

### Requirement: Exact binary Calculation
The service SHALL answer `POST /api/v1/calculations` with `{"operation", "a", "b"}`, where `operation` is `ADD`, `SUBTRACT` or `MULTIPLY`, with `200 OK` and the exact Result of `a + b`, `a − b` or `a × b` (BR-3: no rounding).

#### Scenario: Decimal addition is exact
- **WHEN** a client posts `{"operation": "ADD", "a": 0.1, "b": 0.2}`
- **THEN** the response is `200 OK` with `result` `"0.3"`

#### Scenario: Subtraction to a negative Result
- **WHEN** a client posts `{"operation": "SUBTRACT", "a": "2", "b": "5.5"}`
- **THEN** `result` is `"-3.5"`

#### Scenario: Multiplication keeps every digit
- **WHEN** a client posts `{"operation": "MULTIPLY", "a": "1.25", "b": "-0.004"}`
- **THEN** `result` is `"-0.005"`

#### Scenario: Large precision is exact
- **WHEN** a client posts ADD with `a` `"12345678901234567890.12345678901234567890"` and `b` `"0.00000000000000000001"`
- **THEN** `result` is `"12345678901234567890.12345678901234567891"`

### Requirement: Result normalisation
Every Result SHALL be normalised per BR-4: trailing zeros stripped, plain (non-scientific) notation, and zero as `"0"`.

#### Scenario: Trailing zeros are stripped
- **WHEN** a client posts `{"operation": "ADD", "a": "1.50", "b": "1.50"}`
- **THEN** `result` is `"3"`

#### Scenario: Zero Result is plain zero
- **WHEN** a client posts `{"operation": "SUBTRACT", "a": "1.5", "b": "1.50"}`
- **THEN** `result` is `"0"`

#### Scenario: Multiplying by zero is plain zero
- **WHEN** a client posts `{"operation": "MULTIPLY", "a": "-7.25", "b": "0"}`
- **THEN** `result` is `"0"`

#### Scenario: No scientific notation
- **WHEN** a client posts `{"operation": "MULTIPLY", "a": 1e3, "b": "1"}`
- **THEN** `result` is `"1000"`

### Requirement: Operands as numbers or strings, echoed as received
The request SHALL accept each Operand as a JSON number or a JSON string. The response SHALL echo `operation` and both Operands as JSON strings in plain notation with their given scale; only `result` is normalised.

#### Scenario: Number and string Operands
- **WHEN** a client posts `{"operation": "ADD", "a": 1e2, "b": "1.50"}`
- **THEN** the response is `{"operation": "ADD", "a": "100", "b": "1.50", "result": "101.5"}` with content type `application/json`

### Requirement: Unknown Operation rejected
An `operation` that is not exactly the name of a supported Operation (names are case-sensitive) SHALL be rejected with `400` Problem Details of type `/problems/unknown-operation`, whose `detail` lists the supported Operation names.

#### Scenario: Unsupported name
- **WHEN** a client posts `{"operation": "NOT_AN_OPERATION", "a": 1, "b": 2}`
- **THEN** the response is `400` with type `/problems/unknown-operation` and a `detail` naming every supported Operation

#### Scenario: Wrong case
- **WHEN** a client posts `{"operation": "add", "a": 1, "b": 2}`
- **THEN** the response is `400` with type `/problems/unknown-operation`

### Requirement: Invalid request rejected
A body that is not valid JSON, lacks `operation`, `a` or `b`, or has an Operand that is not a decimal number SHALL be rejected with `400` Problem Details of type `/problems/invalid-request`. This is checked before the Operation name.

#### Scenario: Malformed JSON
- **WHEN** a client posts `{"operation": "ADD", "a": 1,`
- **THEN** the response is `400` with type `/problems/invalid-request`

#### Scenario: Missing field
- **WHEN** a client posts `{"operation": "ADD", "a": 1}`
- **THEN** the response is `400` with type `/problems/invalid-request`

#### Scenario: Non-numeric Operand
- **WHEN** a client posts `{"operation": "ADD", "a": "abc", "b": 1}`
- **THEN** the response is `400` with type `/problems/invalid-request`

#### Scenario: Invalid request reported before unknown Operation
- **WHEN** a client posts `{"operation": "NOPE", "a": 1}`
- **THEN** the response is `400` with type `/problems/invalid-request`

### Requirement: Every error is Problem Details
Every error response, including framework errors, SHALL be RFC 9457 Problem Details (`application/problem+json`) with `type`, `title`, `status` and a human-readable `detail` that exposes no internals (no class names, stack traces or parser messages).

#### Scenario: Problem Details shape
- **WHEN** a client posts `{"operation": "ADD", "a": "abc", "b": 1}`
- **THEN** the content type is `application/problem+json`, the body has `type`, `title`, `status` `400` and `detail`
- **AND** `detail` contains no Java class or package name

#### Scenario: Unexpected failure
- **WHEN** a client posts `{"operation": "MULTIPLY", "a": "1e-2000000000", "b": "1e-2000000000"}`, whose Result cannot be represented
- **THEN** the response is `500` Problem Details whose `detail` contains no Java class or package name

#### Scenario: Unsupported media type
- **WHEN** a client posts with content type `text/plain`
- **THEN** the response is `415` Problem Details

#### Scenario: Unsupported method
- **WHEN** a client sends `GET /api/v1/calculations`
- **THEN** the response is `405` Problem Details

### Requirement: Division rounded to 34 significant digits
The service SHALL answer a `DIVIDE` Calculation with `200 OK` and the Result of `a ÷ b`, rounded per BR-3 to 34 significant digits with `HALF_EVEN` (correctly rounded), then normalised per BR-4. An exact quotient that fits in 34 digits is returned unrounded.

#### Scenario: One third is rounded
- **WHEN** a client posts `{"operation": "DIVIDE", "a": 1, "b": "3"}`
- **THEN** `result` is `"0.3333333333333333333333333333333333"`

#### Scenario: Rounding up
- **WHEN** a client posts `{"operation": "DIVIDE", "a": 2, "b": 3}`
- **THEN** `result` is `"0.6666666666666666666666666666666667"`

#### Scenario: Ties round to even
- **WHEN** a client posts DIVIDE with `a` `"10000000000000000000000000000000005"` and `b` `"10"`
- **THEN** `result` is `"1000000000000000000000000000000000"`
- **AND** with `a` `"10000000000000000000000000000000015"` the `result` is `"1000000000000000000000000000000002"`

#### Scenario: Exact quotient is not rounded
- **WHEN** a client posts `{"operation": "DIVIDE", "a": 1, "b": 4}`
- **THEN** `result` is `"0.25"`

#### Scenario: Negative divisor
- **WHEN** a client posts `{"operation": "DIVIDE", "a": "1", "b": "-8"}`
- **THEN** `result` is `"-0.125"`

#### Scenario: Whole-number quotient in plain notation
- **WHEN** a client posts `{"operation": "DIVIDE", "a": "100", "b": "0.5"}`
- **THEN** `result` is `"200"`

#### Scenario: Zero divided
- **WHEN** a client posts `{"operation": "DIVIDE", "a": "0.00", "b": "7"}`
- **THEN** `result` is `"0"`

### Requirement: Division by zero rejected
A `DIVIDE` Calculation whose `b` is zero in any representation SHALL be rejected per BR-2 with `422` Problem Details of type `/problems/division-by-zero`.

#### Scenario: Each zero representation
- **WHEN** a client posts DIVIDE with `a` `1` and `b` each of `0`, `"0.00"`, `"-0"` and `"0E+3"`
- **THEN** each response is `422` with type `/problems/division-by-zero`

#### Scenario: Zero divided by zero
- **WHEN** a client posts `{"operation": "DIVIDE", "a": 0, "b": 0}`
- **THEN** the response is `422` with type `/problems/division-by-zero`
