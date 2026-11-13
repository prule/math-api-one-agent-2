package com.paulrule.mathapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class OperationTest {

    @ParameterizedTest
    @EnumSource(Operation.class)
    void findsEachOperationByItsExactName(Operation operation) {
        assertThat(Operation.named(operation.name())).isSameAs(operation);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"add", "Add", "NOT_AN_OPERATION", "", " ADD"})
    void rejectsAnyOtherNameListingTheSupportedOnes(String name) {
        assertThatThrownBy(() -> Operation.named(name))
                .isInstanceOf(UnknownOperationException.class)
                .hasMessageStartingWith("Unknown operation '" + name + "'. Supported operations: ")
                .hasMessageEndingWith(".");
    }

    // Parses the message rather than rebuilding it, so separator and order are checked without pinning the names
    // (adding an Operation must not edit this test, STANDARDS.md §3).
    @Test
    void listsEverySupportedOperationInDeclarationOrder() {
        String message = new UnknownOperationException("x").getMessage();
        String list = message.substring(message.indexOf(": ") + 2, message.length() - 1);

        assertThat(list.split(", "))
                .containsExactly(
                        Arrays.stream(Operation.values()).map(Operation::name).toArray(String[]::new));
    }

    @Test
    void addsExactlyPerBR3() {
        assertThat(Operation.ADD.apply(new BigDecimal("0.1"), new BigDecimal("0.2")))
                .isEqualByComparingTo("0.3");
    }

    @Test
    void subtractsExactlyPerBR3() {
        assertThat(Operation.SUBTRACT.apply(new BigDecimal("2"), new BigDecimal("5.5")))
                .isEqualByComparingTo("-3.5");
    }

    @Test
    void multipliesExactlyPerBR3() {
        assertThat(Operation.MULTIPLY.apply(new BigDecimal("1.25"), new BigDecimal("-0.004")))
                .isEqualByComparingTo("-0.005");
    }

    @Test
    void dividesPerBR3() {
        assertThat(Operation.DIVIDE.apply(BigDecimal.ONE, new BigDecimal("3")))
                .isEqualByComparingTo("0.3333333333333333333333333333333333");
    }

    @ParameterizedTest(name = "{0} ÷ {1}")
    @CsvSource({"1, 0", "1, 0.00", "1, -0", "1, 0E+3", "0, 0"})
    void rejectsDivisionByEveryZeroPerBR2(String a, String zero) {
        assertThatExceptionOfType(DivisionByZeroException.class)
                .isThrownBy(() -> Operation.DIVIDE.apply(new BigDecimal(a), new BigDecimal(zero)));
    }

    @ParameterizedTest(name = "{0} ^ {1} = {2}")
    @CsvSource({
        "2, 10, 1024",
        "2, -2, 0.25",
        "3, -1, 0.3333333333333333333333333333333333",
        "-2, 3, -8",
        "2, 200, 1606938044258990275541962092341163000000000000000000000000000",
        "0, 0, 1",
        "2, 3.0, 8",
        "1.00, 999, 1",
        "0E-1000, 2, 0"
    })
    void raisesToAnIntegerPowerRoundedOncePerBR3AndBR5(String a, String b, String result) {
        assertThat(apply(Operation.POWER, a, b).stripTrailingZeros().toPlainString())
                .isEqualTo(result);
    }

    @ParameterizedTest(name = "{0} ^ {1}")
    @CsvSource({"2, 0.5", "2, 1000", "2, -1000", "2, 1E+1000", "0, -1000"})
    void rejectsAnExponentThatIsNotAnIntegerWithinBoundsPerBR5(String a, String b) {
        assertThatExceptionOfType(InvalidExponentException.class)
                .isThrownBy(() -> apply(Operation.POWER, a, b))
                .withMessage("The exponent must be an integer between -999 and 999.");
    }

    @ParameterizedTest(name = "{0} ^ {1}")
    @CsvSource({"0, -1", "0.00, -1"})
    void rejectsZeroToANegativePowerPerBR2(String a, String b) {
        assertThatExceptionOfType(DivisionByZeroException.class).isThrownBy(() -> apply(Operation.POWER, a, b));
    }

    @ParameterizedTest(name = "{0} ^ {1}")
    @CsvSource({"100, 999", "0.01, 999", "100, -999", "0.01, -999"})
    void rejectsAPowerWhoseScaleIsOutOfRangePerBR6(String a, String b) {
        assertThatExceptionOfType(ResultOutOfRangeException.class)
                .isThrownBy(() -> apply(Operation.POWER, a, b))
                .withMessage("The result's scale must be between -1000 and 1000.");
    }

    @ParameterizedTest(name = "b = {0}")
    @ValueSource(strings = {"999", "-999"})
    void raisesTheWorstOperandsWithinOneSecond(String b) {
        String nearOne = "1." + "0".repeat(98) + "1";
        String nines = "9".repeat(100);
        assertThat(assertTimeoutPreemptively(Duration.ofSeconds(1), () -> apply(Operation.POWER, nearOne, b)))
                .isNotNull();
        assertTimeoutPreemptively(
                Duration.ofSeconds(1),
                () -> assertThatExceptionOfType(ResultOutOfRangeException.class)
                        .isThrownBy(() -> apply(Operation.POWER, nines, b)));
    }

    @ParameterizedTest(name = "{0} mod {1} = {2}")
    @CsvSource({"-7, 3, -1", "7, -3, 1", "5.5, 2, 1.5", "-7.5, 2, -1.5", "6, 3, 0", "1E+1000, 7, 4"})
    void takesTheRemainderTruncatedTowardZeroPerBR7(String a, String b, String result) {
        assertThat(apply(Operation.MODULO, a, b).stripTrailingZeros().toPlainString())
                .isEqualTo(result);
    }

    @ParameterizedTest(name = "7 mod {0}")
    @ValueSource(strings = {"0", "0.00", "-0"})
    void rejectsModuloByEveryZeroPerBR2(String zero) {
        assertThatExceptionOfType(DivisionByZeroException.class).isThrownBy(() -> apply(Operation.MODULO, "7", zero));
    }

    private static BigDecimal apply(Operation operation, String a, String b) {
        return operation.apply(new BigDecimal(a), new BigDecimal(b));
    }
}
