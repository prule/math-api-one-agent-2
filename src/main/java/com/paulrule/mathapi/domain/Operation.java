package com.paulrule.mathapi.domain;

import java.math.BigDecimal;
import java.math.MathContext;

/** The closed set of Operations. Each is exact unless its business rule says otherwise (BR-3). */
public enum Operation {
    ADD {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            return a.add(b);
        }
    },
    SUBTRACT {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            return a.subtract(b);
        }
    },
    MULTIPLY {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            return a.multiply(b);
        }
    },
    DIVIDE {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            if (b.signum() == 0) { // every zero representation (BR-2)
                throw new DivisionByZeroException();
            }
            return a.divide(b, MathContext.DECIMAL128); // BR-3
        }
    },
    POWER {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            // BR-5, compared as BigDecimal first so an exponent like 1E+1000 cannot overflow intValueExact.
            if (b.stripTrailingZeros().scale() > 0 || b.abs().compareTo(BigDecimal.valueOf(MAX_EXPONENT)) > 0) {
                throw new InvalidExponentException();
            }
            int n = b.intValueExact();
            if (a.signum() == 0 && n < 0) { // BR-2
                throw new DivisionByZeroException();
            }
            // BR-3: exact power, rounded once (pow(int, MathContext) is not correctly rounded).
            BigDecimal exact = a.pow(Math.abs(n));
            BigDecimal result = (n < 0
                            ? BigDecimal.ONE.divide(exact, MathContext.DECIMAL128)
                            : exact.round(MathContext.DECIMAL128))
                    .stripTrailingZeros(); // here, not only in Calculation.result(): BR-6 judges the normalised Result
            if (!Calculation.scaleInRange(result.scale())) { // BR-6
                throw new ResultOutOfRangeException();
            }
            return result;
        }
    },
    MODULO {
        @Override
        public BigDecimal apply(BigDecimal a, BigDecimal b) {
            if (b.signum() == 0) { // every zero representation (BR-2)
                throw new DivisionByZeroException();
            }
            return a.remainder(b); // BR-7: a - b * trunc(a / b), exact
        }
    };

    static final int MAX_EXPONENT = 999;

    public abstract BigDecimal apply(BigDecimal a, BigDecimal b);

    /** Exact, case-sensitive lookup; {@code valueOf} would leak a JDK message for unknown or null names. */
    public static Operation named(String name) {
        for (Operation operation : values()) {
            if (operation.name().equals(name)) {
                return operation;
            }
        }
        throw new UnknownOperationException(name);
    }
}
