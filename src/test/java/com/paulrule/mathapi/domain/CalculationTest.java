package com.paulrule.mathapi.domain;

import static com.paulrule.mathapi.domain.Operation.ADD;
import static com.paulrule.mathapi.domain.Operation.SUBTRACT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

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
        assertThatNullPointerException().isThrownBy(() -> new Calculation(Operation.SQRT, null, null));
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    void requiresExactlyTheArityOfOperandsPerBR9(Operation operation) {
        BigDecimal missingOrExtraB = operation.arity() == 1 ? BigDecimal.ONE : null;

        assertThatExceptionOfType(ArityMismatchException.class)
                .isThrownBy(() -> new Calculation(operation, BigDecimal.ONE, missingOrExtraB))
                .withMessageStartingWith(operation.name() + " takes ");
    }

    @Test
    void namesTheOperandsEachArityTakes() {
        assertThat(new ArityMismatchException(Operation.SQRT)).hasMessage("SQRT takes one operand, a; omit b.");
        assertThat(new ArityMismatchException(ADD)).hasMessage("ADD takes two operands, a and b.");
    }

    @Test
    void checksArityBeforeOperandLimits() {
        assertThatExceptionOfType(ArityMismatchException.class)
                .isThrownBy(() -> new Calculation(Operation.SQRT, new BigDecimal("1E+1001"), BigDecimal.ONE));
    }

    @Test
    void aUnaryCalculationHasNoB() {
        Calculation calculation = new Calculation(Operation.NEGATE, new BigDecimal("1E+1000"), null);

        assertThat(calculation.b()).isNull();
        assertThat(calculation.result()).isEqualByComparingTo("-1E+1000");
    }

    static Stream<String> operandsAtTheLimits() {
        return Stream.of("1E+1000", "1E-1000", "-1E+1000", "9".repeat(100));
    }

    static Stream<String> operandsOnePastTheLimits() {
        return Stream.of("1E+1001", "1E-1001", "1" + "0".repeat(100), "1e999999999", "0E+1001");
    }

    @ParameterizedTest
    @MethodSource("operandsAtTheLimits")
    void acceptsOperandsAtTheLimitsPerBR1(String operand) {
        BigDecimal value = new BigDecimal(operand);

        assertThatNoException().isThrownBy(() -> new Calculation(ADD, value, BigDecimal.ONE));
        assertThatNoException().isThrownBy(() -> new Calculation(ADD, BigDecimal.ONE, value));
    }

    @ParameterizedTest
    @MethodSource("operandsOnePastTheLimits")
    void rejectsOperandsOnePastTheLimitsPerBR1(String operand) {
        BigDecimal value = new BigDecimal(operand);

        assertThatExceptionOfType(OperandOutOfRangeException.class)
                .isThrownBy(() -> new Calculation(ADD, value, BigDecimal.ONE))
                .withMessageStartingWith("Operand a ")
                .withMessageNotContaining(operand);
        assertThatExceptionOfType(OperandOutOfRangeException.class)
                .isThrownBy(() -> new Calculation(ADD, BigDecimal.ONE, value))
                .withMessageStartingWith("Operand b ")
                .withMessageNotContaining(operand);
    }

    // Math.abs(Integer.MIN_VALUE) is negative, so a scale check written with abs() would let this through.
    @Test
    void rejectsTheMostNegativeScalePerBR1() {
        BigDecimal scaleAtIntMin = new BigDecimal(BigInteger.ONE, Integer.MIN_VALUE);

        assertThatExceptionOfType(OperandOutOfRangeException.class)
                .isThrownBy(() -> new Calculation(ADD, scaleAtIntMin, BigDecimal.ONE));
    }

    @Test
    void reportsTheFirstOutOfRangeOperandPerBR1() {
        BigDecimal tooBig = new BigDecimal("1E+1001");

        assertThatExceptionOfType(OperandOutOfRangeException.class)
                .isThrownBy(() -> new Calculation(ADD, tooBig, tooBig))
                .withMessageStartingWith("Operand a ");
    }
}
