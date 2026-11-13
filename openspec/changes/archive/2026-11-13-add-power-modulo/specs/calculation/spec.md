## ADDED Requirements

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

## MODIFIED Requirements

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
