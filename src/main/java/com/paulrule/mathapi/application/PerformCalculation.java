package com.paulrule.mathapi.application;

import com.paulrule.mathapi.domain.Calculation;
import com.paulrule.mathapi.domain.Operation;
import java.math.BigDecimal;

public class PerformCalculation {

    /** @throws com.paulrule.mathapi.domain.UnknownOperationException if no Operation has exactly that name */
    public Calculation perform(String operation, BigDecimal a, BigDecimal b) {
        return new Calculation(Operation.named(operation), a, b);
    }
}
