## ADDED Requirements

### Requirement: Operand count matches the Operation's Arity
A Calculation SHALL have exactly its Operation's Arity of Operands (BR-9): `b` absent (or `null`) for a unary Operation (`SQRT`, `NEGATE`), present for every binary one. Otherwise it SHALL be rejected with `400` Problem Details of type `/problems/invalid-request` whose `detail` names the Operation and the Operands it takes, never a value.

#### Scenario: Unary Operation given b
- **WHEN** a client posts `{"operation": "SQRT", "a": 4, "b": 0}`
- **THEN** the response is `400` with type `/problems/invalid-request` and a `detail` naming `SQRT`

#### Scenario: Binary Operation without b
- **WHEN** a client posts `{"operation": "ADD", "a": 1}`, or `{"operation": "ADD", "a": 1, "b": null}`
- **THEN** each response is `400` with type `/problems/invalid-request` and a `detail` naming `ADD`

#### Scenario: Null b counts as absent
- **WHEN** a client posts `{"operation": "NEGATE", "a": 5, "b": null}`
- **THEN** the response is `200 OK` with `result` `"-5"`

### Requirement: Square root rounded to 34 significant digits
The service SHALL answer a `SQRT` Calculation with `200 OK` and `√a`, rounded per BR-3 to 34 significant digits with `HALF_EVEN` (correctly rounded), then normalised per BR-4. Every zero representation gives `"0"` (BR-8).

#### Scenario: Exact roots
- **WHEN** a client posts SQRT with `a` `4`, then `"0.25"`, then `"1E+1000"`
- **THEN** the `result`s are `"2"`, `"0.5"` and `"1"` followed by 500 zeros

#### Scenario: Irrational roots are rounded
- **WHEN** a client posts SQRT with `a` `2`, then `10`
- **THEN** the `result`s are `"1.414213562373095048801688724209698"` and `"3.162277660168379331998893544432719"`

#### Scenario: Ties round to even
- **WHEN** a client posts SQRT with `a` `"1.00000000000000000000000000000000100000000000000000000000000000000025"`, then `"1.00000000000000000000000000000000300000000000000000000000000000000225"`
- **THEN** the `result`s are `"1"` and `"1.000000000000000000000000000000002"`

#### Scenario: Zero in every form
- **WHEN** a client posts SQRT with `a` `0`, `"-0"` and `"0.00"`
- **THEN** each `result` is `"0"`

### Requirement: Negative square root rejected
A `SQRT` Calculation whose `a` is below zero SHALL be rejected per BR-8 with `422` Problem Details of type `/problems/negative-square-root`.

#### Scenario: Negative operand
- **WHEN** a client posts SQRT with `a` `-1`, then `"-0.01"`
- **THEN** each response is `422` with type `/problems/negative-square-root`

### Requirement: Negation is exact
The service SHALL answer a `NEGATE` Calculation with `200 OK` and `−a`, exact (BR-3), normalised per BR-4.

#### Scenario: Sign flips
- **WHEN** a client posts NEGATE with `a` `5`, then `"-2.50"`
- **THEN** the `result`s are `"-5"` and `"2.5"`

#### Scenario: Zero in every form
- **WHEN** a client posts NEGATE with `a` `0`, `"-0"` and `"0.00"`
- **THEN** each `result` is `"0"`

## MODIFIED Requirements

### Requirement: Invalid request rejected
A body that is not valid JSON, lacks `operation` or `a`, or has an Operand that is not a decimal number SHALL be rejected with `400` Problem Details of type `/problems/invalid-request`. This is checked before the Operation name.

#### Scenario: Malformed JSON
- **WHEN** a client posts `{"operation": "ADD", "a": 1,`
- **THEN** the response is `400` with type `/problems/invalid-request`

#### Scenario: Missing field
- **WHEN** a client posts `{"operation": "ADD", "b": 1}`
- **THEN** the response is `400` with type `/problems/invalid-request`

#### Scenario: Non-numeric Operand
- **WHEN** a client posts `{"operation": "ADD", "a": "abc", "b": 1}`
- **THEN** the response is `400` with type `/problems/invalid-request`

#### Scenario: Invalid request reported before unknown Operation
- **WHEN** a client posts `{"operation": "NOPE", "b": 1}`
- **THEN** the response is `400` with type `/problems/invalid-request`

### Requirement: Operands as numbers or strings, echoed as received
The request SHALL accept each Operand as a JSON number or a JSON string. The response SHALL echo `operation` and each Operand given as JSON strings in plain notation with their given scale, with no `b` key for a unary Operation; only `result` is normalised.

#### Scenario: Number and string Operands
- **WHEN** a client posts `{"operation": "ADD", "a": 1e2, "b": "1.50"}`
- **THEN** the response is `{"operation": "ADD", "a": "100", "b": "1.50", "result": "101.5"}` with content type `application/json`

#### Scenario: Unary response has no b
- **WHEN** a client posts `{"operation": "NEGATE", "a": "-2.50"}`
- **THEN** the response is `{"operation": "NEGATE", "a": "-2.50", "result": "2.5"}` with no `b` key

### Requirement: Validation order
When a request fails more than one check, the service SHALL report only the first, in this order: invalid request → unknown Operation → Operand count (BR-9, reported as invalid request) → operand out of range (BR-1) → invalid exponent (BR-5) → division by zero (BR-2) → negative square root (BR-8) → result out of range (BR-6).

#### Scenario: Unknown Operation before operand limits
- **WHEN** a client posts `{"operation": "NOPE", "a": "1E+1001", "b": 1}`
- **THEN** the response is `400` with type `/problems/unknown-operation`

#### Scenario: Operand limits before division by zero
- **WHEN** a client posts `{"operation": "DIVIDE", "a": "1E+1001", "b": 0}`
- **THEN** the response is `400` with type `/problems/operand-out-of-range`

#### Scenario: Operand limits before invalid exponent
- **WHEN** a client posts `{"operation": "POWER", "a": "1E+1001", "b": 0.5}`
- **THEN** the response is `400` with type `/problems/operand-out-of-range`

#### Scenario: Invalid exponent before division by zero
- **WHEN** a client posts `{"operation": "POWER", "a": 0, "b": -1000}`
- **THEN** the response is `422` with type `/problems/invalid-exponent`

#### Scenario: Unknown Operation before Operand count
- **WHEN** a client posts `{"operation": "NOPE", "a": 1}`
- **THEN** the response is `400` with type `/problems/unknown-operation`

#### Scenario: Operand count before operand limits
- **WHEN** a client posts `{"operation": "SQRT", "a": "1E+1001", "b": 1}`
- **THEN** the response is `400` with type `/problems/invalid-request`

#### Scenario: Operand limits before negative square root
- **WHEN** a client posts `{"operation": "SQRT", "a": "-1E+1001"}`
- **THEN** the response is `400` with type `/problems/operand-out-of-range`
