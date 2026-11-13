package com.paulrule.mathapi.domain;

import java.math.BigDecimal;
import java.util.Objects;

/** One Operation applied to its Operands; {@code b} is null for a unary Operation (BR-9). */
public record Calculation(Operation operation, BigDecimal a, BigDecimal b) {

    static final int MAX_PRECISION = 100;
    static final int MAX_SCALE = 1000;

    public Calculation {
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(a, "a");
        if (operation.arity() == 1 ? b != null : b == null) { // BR-9, before BR-1
            throw new ArityMismatchException(operation);
        }
        requireInRange(a, "a");
        if (b != null) {
            requireInRange(b, "b");
        }
    }

    /** The Result, with trailing zeros stripped (BR-4); plain notation is rendered by the caller. */
    public BigDecimal result() {
        return operation.apply(a, b).stripTrailingZeros();
    }

    // BR-1 reads the Operand as written, so it bounds both the arithmetic and the echoed Operand.
    private static void requireInRange(BigDecimal operand, String name) {
        if (operand.precision() > MAX_PRECISION || !scaleInRange(operand.scale())) {
            throw new OperandOutOfRangeException(name);
        }
    }

    /** The BR-1 scale bounds, also used for a POWER Result (BR-6). */
    static boolean scaleInRange(int scale) {
        // Two comparisons, not Math.abs: abs(Integer.MIN_VALUE) is negative and would pass.
        return scale <= MAX_SCALE && scale >= -MAX_SCALE;
    }
}
