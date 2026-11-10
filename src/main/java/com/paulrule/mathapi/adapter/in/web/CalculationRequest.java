package com.paulrule.mathapi.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * {@code operation} is a String, not the enum, so an unknown name is reported as unknown-operation rather than
 * failing JSON binding. Operands bind from JSON numbers or strings.
 */
public record CalculationRequest(
        @NotNull String operation,
        @NotNull BigDecimal a,
        @NotNull BigDecimal b) {}
