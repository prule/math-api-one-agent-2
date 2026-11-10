# Spec Delta

## ADDED Requirements

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

## MODIFIED Requirements

### Requirement: Unknown Operation rejected
An `operation` that is not exactly the name of a supported Operation (names are case-sensitive) SHALL be rejected with `400` Problem Details of type `/problems/unknown-operation`, whose `detail` lists the supported Operation names.

#### Scenario: Unsupported name
- **WHEN** a client posts `{"operation": "NOT_AN_OPERATION", "a": 1, "b": 2}`
- **THEN** the response is `400` with type `/problems/unknown-operation` and a `detail` naming every supported Operation

#### Scenario: Wrong case
- **WHEN** a client posts `{"operation": "add", "a": 1, "b": 2}`
- **THEN** the response is `400` with type `/problems/unknown-operation`
