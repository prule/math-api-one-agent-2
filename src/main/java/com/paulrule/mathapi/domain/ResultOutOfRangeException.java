package com.paulrule.mathapi.domain;

/** BR-6: a POWER Result, normalised, has a scale outside the BR-1 bounds. */
public class ResultOutOfRangeException extends RuntimeException {

    public ResultOutOfRangeException() {
        super("The result's scale must be between -" + Calculation.MAX_SCALE + " and " + Calculation.MAX_SCALE + ".");
    }
}
