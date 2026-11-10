package com.paulrule.mathapi.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record Calculation(Operation operation, BigDecimal a, BigDecimal b) {

    static final int MAX_PRECISION = 100;
    static final int MAX_SCALE = 1000;

    public Calculation {
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        requireInRange(a, "a");
        requireInRange(b, "b");
    }

    /** The Result, with trailing zeros stripped (BR-4); plain notation is rendered by the caller. */
    public BigDecimal result() {
        return operation.apply(a, b).stripTrailingZeros();
    }

    // BR-1 reads the Operand as written, so it bounds both the arithmetic and the echoed Operand.
    private static void requireInRange(BigDecimal operand, String name) {
        // Two comparisons, not Math.abs: abs(Integer.MIN_VALUE) is negative and would pass.
        if (operand.precision() > MAX_PRECISION || operand.scale() > MAX_SCALE || operand.scale() < -MAX_SCALE) {
            throw new OperandOutOfRangeException(name);
        }
    }
}
