package com.paulrule.mathapi.domain;

/** BR-2: the divisor is zero in some representation. */
public class DivisionByZeroException extends RuntimeException {

    public DivisionByZeroException() {
        super("Division by zero is undefined.");
    }
}
