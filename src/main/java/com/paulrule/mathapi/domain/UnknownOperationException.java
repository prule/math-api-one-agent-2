package com.paulrule.mathapi.domain;

import java.util.Arrays;
import java.util.stream.Collectors;

/** The requested name is not exactly the name of an Operation. */
public class UnknownOperationException extends RuntimeException {

    public UnknownOperationException(String name) {
        super("Unknown operation '" + name + "'. Supported operations: "
                + Arrays.stream(Operation.values()).map(Operation::name).collect(Collectors.joining(", ")) + ".");
    }
}
