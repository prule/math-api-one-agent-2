package com.paulrule.mathapi.domain;

/** BR-8: SQRT of an Operand below zero. */
public class NegativeSquareRootException extends RuntimeException {

    public NegativeSquareRootException() {
        super("The square root of a negative number is undefined.");
    }
}
