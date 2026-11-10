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
    };

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
