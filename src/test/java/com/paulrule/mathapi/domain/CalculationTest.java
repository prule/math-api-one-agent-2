package com.paulrule.mathapi.domain;

import static com.paulrule.mathapi.domain.Operation.ADD;
import static com.paulrule.mathapi.domain.Operation.SUBTRACT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CalculationTest {

    // Each row: operation, a, b, expected Result rendered as BR-4 plain text.
    @ParameterizedTest(name = "{0} {1} {2} = {3}")
    @CsvSource({
        "ADD, 0.1, 0.2, 0.3",
        "ADD, -1.5, -2.25, -3.75",
        "ADD, -5, 3, -2",
        "ADD, 12345678901234567890.12345678901234567890, 0.00000000000000000001, 12345678901234567890.12345678901234567891",
        "SUBTRACT, 1.5, 1.50, 0",
        "SUBTRACT, 2, 5.5, -3.5",
        "SUBTRACT, -1, -1, 0",
        "MULTIPLY, -7.25, 0, 0",
        "MULTIPLY, 0.00, 123.45, 0",
        "MULTIPLY, -2, -3, 6",
        "MULTIPLY, -2, 3, -6",
        "MULTIPLY, 1.25, -0.004, -0.005",
        "MULTIPLY, 0.001, 0.001, 0.000001",
        "DIVIDE, 2, 3, 0.6666666666666666666666666666666667",
        "DIVIDE, 10000000000000000000000000000000005, 10, 1000000000000000000000000000000000",
        "DIVIDE, 10000000000000000000000000000000015, 10, 1000000000000000000000000000000002",
        "DIVIDE, 1, 4, 0.25",
        "DIVIDE, 1, -8, -0.125",
        "DIVIDE, 100, 0.5, 200",
        "DIVIDE, 0.00, 7, 0",
    })
    void performsPerBR3AndNormalisesPerBR4(Operation operation, String a, String b, String expected) {
        BigDecimal result = new Calculation(operation, new BigDecimal(a), new BigDecimal(b)).result();

        assertThat(result.toPlainString()).isEqualTo(expected);
    }

    @Test
    void stripsTrailingZerosPerBR4() {
        BigDecimal result = new Calculation(ADD, new BigDecimal("1.50"), new BigDecimal("1.50")).result();

        assertThat(result.toPlainString()).isEqualTo("3");
        assertThat(result.scale()).isZero();
    }

    @Test
    void keepsItsOperandsAsGiven() {
        Calculation calculation = new Calculation(SUBTRACT, new BigDecimal("1.50"), new BigDecimal("1E+2"));

        assertThat(calculation.a().toPlainString()).isEqualTo("1.50");
        assertThat(calculation.b().toPlainString()).isEqualTo("100");
    }

    @Test
    void rejectsMissingComponents() {
        assertThatNullPointerException().isThrownBy(() -> new Calculation(null, BigDecimal.ONE, BigDecimal.ONE));
        assertThatNullPointerException().isThrownBy(() -> new Calculation(ADD, null, BigDecimal.ONE));
        assertThatNullPointerException().isThrownBy(() -> new Calculation(ADD, BigDecimal.ONE, null));
    }
}
