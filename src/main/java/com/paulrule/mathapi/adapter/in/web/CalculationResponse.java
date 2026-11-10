package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.domain.Calculation;

/** Every value is a string in plain notation, so clients never lose precision. */
public record CalculationResponse(String operation, String a, String b, String result) {

    static CalculationResponse of(Calculation calculation) {
        return new CalculationResponse(
                calculation.operation().name(),
                calculation.a().toPlainString(),
                calculation.b().toPlainString(),
                calculation.result().toPlainString());
    }
}
