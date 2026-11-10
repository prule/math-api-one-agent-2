package com.paulrule.mathapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
                .hasMessage("Unknown operation '" + name + "'. Supported operations: ADD, SUBTRACT, MULTIPLY.");
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
}
