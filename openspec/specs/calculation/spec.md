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
- **WHEN** handling a request fails with an exception no other rule maps (BR-1 now rejects every input known to cause one, e.g. `1e-2000000000`)
- **THEN** the response is `500` Problem Details with type `about:blank` whose `detail` contains no Java class or package name and none of the exception's message

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
A `DIVIDE` or `MODULO` Calculation whose `b` is zero in any representation, and a `POWER` of zero to a negative exponent, SHALL be rejected per BR-2 with `422` Problem Details of type `/problems/division-by-zero`.

#### Scenario: Each zero representation
- **WHEN** a client posts DIVIDE with `a` `1` and `b` each of `0`, `"0.00"`, `"-0"` and `"0E+3"`
- **THEN** each response is `422` with type `/problems/division-by-zero`

#### Scenario: Zero divided by zero
- **WHEN** a client posts `{"operation": "DIVIDE", "a": 0, "b": 0}`
- **THEN** the response is `422` with type `/problems/division-by-zero`

#### Scenario: Modulo by zero
- **WHEN** a client posts MODULO with `a` `7` and `b` each of `0` and `"-0.0"`
- **THEN** each response is `422` with type `/problems/division-by-zero`

#### Scenario: Zero to a negative power
- **WHEN** a client posts `{"operation": "POWER", "a": "0.00", "b": -1}`
- **THEN** the response is `422` with type `/problems/division-by-zero`

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
When a request fails more than one check, the service SHALL report only the first, in this order: invalid request → unknown Operation → operand out of range (BR-1) → invalid exponent (BR-5) → division by zero (BR-2) → result out of range (BR-6).

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

### Requirement: Power of an integer exponent
The service SHALL answer a `POWER` Calculation with `200 OK` and `a ^ b` for an integer `b` (BR-5: `3.0` counts as 3). The exact power is rounded once per BR-3 (34 significant digits, `HALF_EVEN`), then normalised per BR-4. A negative `b` means `1 / a^|b|`, and `a^0` is `1` for every `a`, including zero.

#### Scenario: Positive exponent is exact when it fits
- **WHEN** a client posts `{"operation": "POWER", "a": 2, "b": 10}`
- **THEN** `result` is `"1024"`

#### Scenario: Negative exponent
- **WHEN** a client posts POWER with `a` `2` and `b` `-2`, then with `a` `3` and `b` `-1`
- **THEN** the `result`s are `"0.25"` and `"0.3333333333333333333333333333333333"`

#### Scenario: Negative base
- **WHEN** a client posts `{"operation": "POWER", "a": -2, "b": 3}`
- **THEN** `result` is `"-8"`

#### Scenario: Large power is rounded once
- **WHEN** a client posts `{"operation": "POWER", "a": 2, "b": 200}`
- **THEN** `result` is `"1606938044258990275541962092341163000000000000000000000000000"`

#### Scenario: Zero to the zero
- **WHEN** a client posts `{"operation": "POWER", "a": 0, "b": 0}`
- **THEN** `result` is `"1"`

#### Scenario: Integral exponent with a fraction part of zero
- **WHEN** a client posts `{"operation": "POWER", "a": 2, "b": "3.0"}`
- **THEN** `result` is `"8"`

#### Scenario: Worst operands stay fast
- **WHEN** a client posts POWER with `b` `999` or `-999` and a 100-digit `a`: `"1.` followed by 98 zeros and `1"`, or `"9"` repeated 100 times
- **THEN** each response arrives within one second: `200 OK` for the first `a`, `422` `/problems/result-out-of-range` for the second

### Requirement: Invalid exponent rejected
A `POWER` Calculation whose `b` is not an integer, or whose |b| exceeds 999, SHALL be rejected per BR-5 with `422` Problem Details of type `/problems/invalid-exponent`, before any arithmetic.

#### Scenario: Fractional exponent
- **WHEN** a client posts `{"operation": "POWER", "a": 2, "b": 0.5}`
- **THEN** the response is `422` with type `/problems/invalid-exponent`

#### Scenario: Exponent past the bound
- **WHEN** a client posts POWER with `a` `2` and `b` `1000`, or `b` `-1000`
- **THEN** each response is `422` with type `/problems/invalid-exponent`

#### Scenario: Exponent at the bound
- **WHEN** a client posts POWER with `a` `1.001` and `b` `999`, or `b` `-999`
- **THEN** each response is `200 OK`

### Requirement: Power result out of range rejected
A `POWER` Result whose scale, after rounding and normalisation, is outside ±1000 SHALL be rejected per BR-6 with `422` Problem Details of type `/problems/result-out-of-range`.

#### Scenario: Too large
- **WHEN** a client posts `{"operation": "POWER", "a": 100, "b": 999}`
- **THEN** the response is `422` with type `/problems/result-out-of-range`

#### Scenario: Too small
- **WHEN** a client posts `{"operation": "POWER", "a": "0.01", "b": 999}`
- **THEN** the response is `422` with type `/problems/result-out-of-range`

#### Scenario: Negative exponents are bounded too
- **WHEN** a client posts POWER with `a` `100` and `b` `-999`, or `a` `"0.01"` and `b` `-999`
- **THEN** each response is `422` with type `/problems/result-out-of-range`

#### Scenario: Scale is judged after normalisation
- **WHEN** a client posts `{"operation": "POWER", "a": "1.00", "b": 999}` (exact power written with 1,998 decimal places)
- **THEN** `result` is `"1"`

### Requirement: Modulo is the truncated remainder
The service SHALL answer a `MODULO` Calculation with `200 OK` and the exact remainder `a − b × trunc(a ÷ b)` (BR-7, BR-3: no rounding), which takes the sign of `a`, normalised per BR-4.

#### Scenario: Sign follows the dividend
- **WHEN** a client posts MODULO with `a` `-7` and `b` `3`, then with `a` `7` and `b` `-3`
- **THEN** the `result`s are `"-1"` and `"1"`

#### Scenario: Decimal operands
- **WHEN** a client posts MODULO with `a` `5.5` and `b` `2`, then with `a` `-7.5` and `b` `2`
- **THEN** the `result`s are `"1.5"` and `"-1.5"`

#### Scenario: Exact multiple
- **WHEN** a client posts `{"operation": "MODULO", "a": 6, "b": 3}`
- **THEN** `result` is `"0"`

#### Scenario: Huge dividend
- **WHEN** a client posts `{"operation": "MODULO", "a": "1E+1000", "b": 7}`
- **THEN** `result` is `"4"`
