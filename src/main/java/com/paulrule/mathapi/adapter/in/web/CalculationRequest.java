package com.paulrule.mathapi.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * {@code operation} is a String, not the enum, so an unknown name is reported as unknown-operation rather than
 * failing JSON binding. Operands bind from JSON numbers or strings. {@code b} may be absent or null: whether it
 * belongs is BR-9, checked in the domain.
 */
public record CalculationRequest(
        @NotNull String operation, @NotNull BigDecimal a, BigDecimal b) {}
