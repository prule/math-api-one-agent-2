package com.paulrule.mathapi.domain;

/** BR-1: an Operand, as written, is too precise or its scale is out of bounds. Never carries the value. */
public class OperandOutOfRangeException extends RuntimeException {

    public OperandOutOfRangeException(String operandName) {
        super("Operand " + operandName + " must have at most " + Calculation.MAX_PRECISION
                + " significant digits and a scale between -" + Calculation.MAX_SCALE + " and "
                + Calculation.MAX_SCALE + ".");
    }
}
