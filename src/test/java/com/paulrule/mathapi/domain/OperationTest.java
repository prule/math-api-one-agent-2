package com.paulrule.mathapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
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
}
