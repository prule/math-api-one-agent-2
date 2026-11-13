package com.paulrule.mathapi.domain;

/** BR-9: a Calculation does not have exactly its Operation's Arity of Operands. */
public class ArityMismatchException extends RuntimeException {

    public ArityMismatchException(Operation operation) {
        super(operation.name()
                + (operation.arity() == 1 ? " takes one operand, a; omit b." : " takes two operands, a and b."));
    }
}
