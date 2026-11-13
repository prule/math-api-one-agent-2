# Design

## Context

`Operation` is an enum whose constants implement `apply(a, b)`. `Calculation`'s constructor checks BR-1 on both Operands, and `result()` applies the Operation and strips trailing zeros (BR-4). `DIVIDE` throws `DivisionByZeroException` for any zero `b`. `ProblemDetailsAdvice` maps each domain exception, and `ProblemExamples` documents each type by calling the advice's handlers. DOMAIN-MODEL §2 says a new kind of failure needs a domain exception and a handler, and nothing else.

## Goals / Non-Goals

**Goals:** POWER and MODULO as two constants with their BR checks inside `apply`; two exceptions and handlers; docs and examples follow automatically except the two new examples.

**Non-Goals:** `BigDecimal.pow(int, MathContext)` (X3.274 rounding, not correctly rounded); any exponent cap configuration.

## Decisions

- **POWER computes the exact power, then rounds once.** For `n = b.intValueExact()`, `a.pow(|n|)` is exact. A positive `n` rounds it with `round(DECIMAL128)`, and a negative `n` uses `ONE.divide(exact, DECIMAL128)`. Both are correctly rounded (BR-3). Measured: a 100-digit `a` to ±999 takes about 40 ms, inside the 1 s budget. Alternative: `pow(n, DECIMAL128)` is rejected because it can be off by an ulp, and BR-3 says correctly rounded.
- **Check order inside `POWER.apply`**: BR-5 first (`b.stripTrailingZeros().scale() > 0`, or `|b| > 999` compared as `BigDecimal` before `intValueExact`, so a huge `b` like `1E+1000` can't overflow), then BR-2 (`a.signum() == 0 && n < 0`), then the arithmetic, then BR-6 on `rounded.stripTrailingZeros().scale()` against `Calculation.MAX_SCALE`. BR-1 has already run in the `Calculation` constructor, which gives exactly the PRD order. `stripTrailingZeros()` on a zero returns scale 0 (JDK 8+), so `0^n` never trips BR-6.
- **BR-6 lives in `POWER.apply`, not `Calculation.result()`.** Only POWER can produce such a scale: the other Operations are bounded by BR-1 and DECIMAL128. `result()` strips trailing zeros again, which does nothing the second time.
- **MODULO uses `BigDecimal.remainder`**, which is defined as `a − b × trunc(a ÷ b)` (BR-7) and is exact. A zero `b` throws `DivisionByZeroException` first, the same check DIVIDE uses. Measured: the worst case (`99…9E+1000 mod 7E-1000`) takes under 1 ms.
- **New exceptions carry fixed messages that never echo the Operand**, like `OperandOutOfRangeException`. `InvalidExponentException`: "The exponent must be an integer between -999 and 999." `ResultOutOfRangeException`: "The result's scale must be between -1000 and 1000." Both build the bounds from constants.
- **`0^0 = 1`** comes from `BigDecimal.pow(0)` returning `ONE`; no special case.

## Risks / Trade-offs

- [A future Operation could produce an out-of-range scale too] → BR-6 is POWER-only by definition today; whoever adds such an Operation moves the check into `Calculation.result()`.
- [`Calculation.MAX_SCALE` is package-private and shared by BR-1 and BR-6] → both rules use "the BR-1 bounds" (DOMAIN-MODEL BR-6), so sharing the constant is the point.
