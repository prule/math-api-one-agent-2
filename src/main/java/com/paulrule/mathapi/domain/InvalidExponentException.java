package com.paulrule.mathapi.domain;

/** BR-5: a POWER exponent is not an integer, or its magnitude is above the bound. Never carries the value. */
public class InvalidExponentException extends RuntimeException {

    public InvalidExponentException() {
        super("The exponent must be an integer between -" + Operation.MAX_EXPONENT + " and " + Operation.MAX_EXPONENT
                + ".");
    }
}
