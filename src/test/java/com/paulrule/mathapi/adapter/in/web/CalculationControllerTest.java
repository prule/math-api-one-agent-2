package com.paulrule.mathapi.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.paulrule.mathapi.config.UseCaseConfig;
import com.paulrule.mathapi.domain.UnknownOperationException;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@WebMvcTest(CalculationController.class)
@Import(UseCaseConfig.class)
class CalculationControllerTest {

    private static final String PATH = "/api/v1/calculations";
    private static final MediaType PROBLEM = MediaType.APPLICATION_PROBLEM_JSON;

    @Autowired
    private MockMvcTester mvc;

    @ParameterizedTest(name = "{0}")
    @CsvSource(
            delimiter = '|',
            value = {
                "{\"operation\": \"ADD\", \"a\": 0.1, \"b\": 0.2}                      | 0.3",
                "{\"operation\": \"SUBTRACT\", \"a\": \"2\", \"b\": \"5.5\"}           | -3.5",
                "{\"operation\": \"MULTIPLY\", \"a\": \"1.25\", \"b\": \"-0.004\"}     | -0.005",
                "{\"operation\": \"ADD\", \"a\": \"12345678901234567890.12345678901234567890\", \"b\": \"0.00000000000000000001\"} | 12345678901234567890.12345678901234567891",
                "{\"operation\": \"ADD\", \"a\": \"1.50\", \"b\": \"1.50\"}            | 3",
                "{\"operation\": \"SUBTRACT\", \"a\": \"1.5\", \"b\": \"1.50\"}        | 0",
                "{\"operation\": \"MULTIPLY\", \"a\": \"-7.25\", \"b\": \"0\"}         | 0",
                "{\"operation\": \"MULTIPLY\", \"a\": 1e3, \"b\": \"1\"}               | 1000",
                "{\"operation\": \"DIVIDE\", \"a\": 1, \"b\": \"3\"}                 | 0.3333333333333333333333333333333333",
                "{\"operation\": \"DIVIDE\", \"a\": 1, \"b\": 4}                       | 0.25",
                "{\"operation\": \"DIVIDE\", \"a\": \"100\", \"b\": \"0.5\"}       | 200",
                "{\"operation\": \"DIVIDE\", \"a\": \"10000000000000000000000000000000015\", \"b\": \"10\"} | 1000000000000000000000000000000002",
                "{\"operation\": \"POWER\", \"a\": 2, \"b\": 10}                  | 1024",
                "{\"operation\": \"POWER\", \"a\": 2, \"b\": -2}                  | 0.25",
                "{\"operation\": \"POWER\", \"a\": 3, \"b\": -1}                  | 0.3333333333333333333333333333333333",
                "{\"operation\": \"POWER\", \"a\": -2, \"b\": 3}                  | -8",
                "{\"operation\": \"POWER\", \"a\": 2, \"b\": 200}                 | 1606938044258990275541962092341163000000000000000000000000000",
                "{\"operation\": \"POWER\", \"a\": 0, \"b\": 0}                   | 1",
                "{\"operation\": \"POWER\", \"a\": 2, \"b\": \"3.0\"}           | 8",
                "{\"operation\": \"POWER\", \"a\": \"1.00\", \"b\": 999}        | 1",
                "{\"operation\": \"MODULO\", \"a\": -7, \"b\": 3}                 | -1",
                "{\"operation\": \"MODULO\", \"a\": 7, \"b\": -3}                 | 1",
                "{\"operation\": \"MODULO\", \"a\": 5.5, \"b\": 2}                | 1.5",
                "{\"operation\": \"MODULO\", \"a\": -7.5, \"b\": 2}               | -1.5",
                "{\"operation\": \"MODULO\", \"a\": 6, \"b\": 3}                  | 0",
                "{\"operation\": \"MODULO\", \"a\": \"1E+1000\", \"b\": 7}      | 4",
                "{\"operation\": \"SQRT\", \"a\": 4}                                | 2",
                "{\"operation\": \"SQRT\", \"a\": \"0.25\"}                       | 0.5",
                "{\"operation\": \"SQRT\", \"a\": 2}                                | 1.414213562373095048801688724209698",
                "{\"operation\": \"SQRT\", \"a\": 10}                               | 3.162277660168379331998893544432719",
                "{\"operation\": \"SQRT\", \"a\": \"1.00000000000000000000000000000000100000000000000000000000000000000025\"} | 1",
                "{\"operation\": \"SQRT\", \"a\": \"1.00000000000000000000000000000000300000000000000000000000000000000225\"} | 1.000000000000000000000000000000002",
                "{\"operation\": \"SQRT\", \"a\": 0}                                | 0",
                "{\"operation\": \"SQRT\", \"a\": \"-0\"}                         | 0",
                "{\"operation\": \"SQRT\", \"a\": \"0.00\"}                       | 0",
                "{\"operation\": \"NEGATE\", \"a\": 5}                              | -5",
                "{\"operation\": \"NEGATE\", \"a\": \"-2.50\"}                    | 2.5",
                "{\"operation\": \"NEGATE\", \"a\": 0}                              | 0",
                "{\"operation\": \"NEGATE\", \"a\": \"-0\"}                       | 0",
                "{\"operation\": \"NEGATE\", \"a\": \"0.00\"}                     | 0",
                "{\"operation\": \"NEGATE\", \"a\": 5, \"b\": null}               | -5",
            })
    void returnsTheExactResultNormalisedPerBR4(String body, String result) {
        assertThat(post(body))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.result")
                .isEqualTo(result);
    }

    @Test
    void echoesOperandsAsReceivedInPlainNotation() {
        assertThat(post("{\"operation\": \"ADD\", \"a\": 1e2, \"b\": \"1.50\"}"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .isStrictlyEqualTo("{\"operation\": \"ADD\", \"a\": \"100\", \"b\": \"1.50\", \"result\": \"101.5\"}");
    }

    @Test
    void rejectsAnUnknownOperationListingTheSupportedOnes() {
        MvcTestResult result = post("{\"operation\": \"NOT_AN_OPERATION\", \"a\": 1, \"b\": 2}");

        assertProblem(result, 400, "/problems/unknown-operation");
        assertThat(result)
                .bodyJson()
                .extractingPath("$.detail")
                .isEqualTo(new UnknownOperationException("NOT_AN_OPERATION").getMessage());
    }

    @Test
    void operationNamesAreCaseSensitive() {
        assertProblem(post("{\"operation\": \"add\", \"a\": 1, \"b\": 2}"), 400, "/problems/unknown-operation");
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{\"operation\": \"ADD\", \"a\": 1,",
                "",
                "[]",
                "{\"operation\": \"ADD\", \"a\": 1}",
                "{\"a\": 1, \"b\": 2}",
                "{\"operation\": \"ADD\", \"a\": null, \"b\": 2}",
                "{\"operation\": \"ADD\", \"a\": \"abc\", \"b\": 1}",
                "{\"operation\": \"ADD\", \"a\": \"\", \"b\": 1}",
                "{\"operation\": \"ADD\", \"a\": true, \"b\": 1}",
                "{\"operation\": \"ADD\", \"a\": {}, \"b\": 1}",
                "{\"operation\": \"ADD\", \"a\": [1], \"b\": 1}",
                "{\"operation\": \"NOPE\", \"b\": 1}",
                "{\"operation\": \"ADD\", \"b\": 1}",
            })
    void rejectsAnInvalidRequestBeforeLookingAtTheOperation(String body) {
        assertProblem(post(body), 400, "/problems/invalid-request");
    }

    @ParameterizedTest(name = "{0} ÷ {1}")
    @CsvSource({"1, 0", "1, '\"0.00\"'", "1, '\"-0\"'", "1, '\"0E+3\"'", "0, 0"})
    void rejectsDivisionByZeroPerBR2(String a, String zero) {
        assertProblem(
                post("{\"operation\": \"DIVIDE\", \"a\": " + a + ", \"b\": " + zero + "}"),
                422,
                "/problems/division-by-zero");
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(
            delimiter = '|',
            value = {
                "{\"operation\": \"MODULO\", \"a\": 7, \"b\": 0}",
                "{\"operation\": \"MODULO\", \"a\": 7, \"b\": \"-0.0\"}",
                "{\"operation\": \"POWER\", \"a\": \"0.00\", \"b\": -1}",
            })
    void rejectsModuloByZeroAndZeroToANegativePowerPerBR2(String body) {
        assertProblem(post(body), 422, "/problems/division-by-zero");
    }

    @ParameterizedTest(name = "2 ^ {0}")
    @ValueSource(strings = {"0.5", "1000", "-1000"})
    void rejectsAnInvalidExponentPerBR5(String b) {
        assertProblem(
                post("{\"operation\": \"POWER\", \"a\": 2, \"b\": " + b + "}"), 422, "/problems/invalid-exponent");
    }

    @ParameterizedTest(name = "1.001 ^ {0}")
    @ValueSource(strings = {"999", "-999"})
    void acceptsAnExponentAtTheBoundPerBR5(String b) {
        assertThat(post("{\"operation\": \"POWER\", \"a\": 1.001, \"b\": " + b + "}"))
                .hasStatusOk();
    }

    @ParameterizedTest(name = "{0} ^ {1}")
    @CsvSource({"100, 999", "'\"0.01\"', 999", "100, -999", "'\"0.01\"', -999"})
    void rejectsAPowerResultOutOfRangePerBR6(String a, String b) {
        assertProblem(
                post("{\"operation\": \"POWER\", \"a\": " + a + ", \"b\": " + b + "}"),
                422,
                "/problems/result-out-of-range");
    }

    @ParameterizedTest(name = "b = {0}")
    @ValueSource(strings = {"999", "-999"})
    void raisesTheWorstOperandsWithinOneSecond(String b) {
        String nearOne = "\"1." + "0".repeat(98) + "1\"";
        String nines = "\"" + "9".repeat(100) + "\"";
        assertThat(assertTimeoutPreemptively(
                        Duration.ofSeconds(1),
                        () -> post("{\"operation\": \"POWER\", \"a\": " + nearOne + ", \"b\": " + b + "}")))
                .hasStatusOk();
        assertProblem(
                assertTimeoutPreemptively(
                        Duration.ofSeconds(1),
                        () -> post("{\"operation\": \"POWER\", \"a\": " + nines + ", \"b\": " + b + "}")),
                422,
                "/problems/result-out-of-range");
    }

    @Test
    void echoesAUnaryCalculationWithoutB() {
        assertThat(post("{\"operation\": \"NEGATE\", \"a\": \"-2.50\"}"))
                .hasStatusOk()
                .bodyJson()
                .isStrictlyEqualTo("{\"operation\": \"NEGATE\", \"a\": \"-2.50\", \"result\": \"2.5\"}");
    }

    @Test
    void takesTheSquareRootOfTheLargestOperand() {
        assertThat(post("{\"operation\": \"SQRT\", \"a\": \"1E+1000\"}"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.result")
                .isEqualTo("1" + "0".repeat(500));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(
            delimiter = '|',
            value = {
                "{\"operation\": \"SQRT\", \"a\": 4, \"b\": 0}       | SQRT takes one operand, a; omit b.",
                "{\"operation\": \"NEGATE\", \"a\": 4, \"b\": \"1\"} | NEGATE takes one operand, a; omit b.",
                "{\"operation\": \"ADD\", \"a\": 1}                    | ADD takes two operands, a and b.",
                "{\"operation\": \"ADD\", \"a\": 1, \"b\": null}     | ADD takes two operands, a and b.",
            })
    void rejectsTheWrongNumberOfOperandsPerBR9(String body, String detail) {
        MvcTestResult result = post(body);

        assertProblem(result, 400, "/problems/invalid-request");
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo(detail);
    }

    @ParameterizedTest(name = "√{0}")
    @ValueSource(strings = {"-1", "\"-0.01\""})
    void rejectsTheSquareRootOfANegativePerBR8(String a) {
        assertProblem(post("{\"operation\": \"SQRT\", \"a\": " + a + "}"), 422, "/problems/negative-square-root");
    }

    @ParameterizedTest(name = "b = {0}")
    @MethodSource("operandsOnePastEachLimit")
    void rejectsAnOperandOutOfRangeWithoutEchoingItPerBR1(String operand) {
        MvcTestResult result = post("{\"operation\": \"ADD\", \"a\": 1, \"b\": \"" + operand + "\"}");

        assertProblem(result, 400, "/problems/operand-out-of-range");
        assertThat(result)
                .bodyJson()
                .extractingPath("$.detail")
                .asString()
                .startsWith("Operand b ")
                .doesNotContain(operand);
    }

    static Stream<String> operandsOnePastEachLimit() {
        return Stream.of("1E+1001", "1E-1001", "1" + "0".repeat(100));
    }

    @Test
    void acceptsOperandsAtTheLimitsPerBR1() {
        assertThat(post("{\"operation\": \"ADD\", \"a\": \"1E+1000\", \"b\": \"1E-1000\"}"))
                .hasStatusOk();
        assertThat(post("{\"operation\": \"ADD\", \"a\": \"" + "9".repeat(100) + "\", \"b\": 1}"))
                .hasStatusOk();
    }

    @Test
    void reportsTheFirstOutOfRangeOperandPerBR1() {
        assertThat(post("{\"operation\": \"ADD\", \"a\": \"1E+1001\", \"b\": \"1E-1001\"}"))
                .bodyJson()
                .extractingPath("$.detail")
                .asString()
                .startsWith("Operand a ");
    }

    @Test
    void rejectsHugeExponentsImmediatelyPerBR1() {
        MvcTestResult result = assertTimeoutPreemptively(
                Duration.ofSeconds(1),
                () -> post("{\"operation\": \"MULTIPLY\", \"a\": \"1e999999999\", \"b\": \"1e999999999\"}"));

        assertProblem(result, 400, "/problems/operand-out-of-range");
    }

    @Test
    void rejectsAnOversizedBodyStatingTheLimits() {
        MvcTestResult result = post(validBodyPaddedTo(16_385));

        assertProblem(result, 400, "/problems/invalid-request");
        assertThat(result)
                .bodyJson()
                .extractingPath("$.detail")
                .asString()
                .contains("16,384 characters")
                .contains("1,000");
    }

    @Test
    void acceptsABodyAtTheSizeLimit() {
        assertThat(post(validBodyPaddedTo(16_384))).hasStatusOk();
    }

    // Pads inside the object so the parser must read every character.
    private static String validBodyPaddedTo(int length) {
        String body = "{\"operation\": \"ADD\", \"a\": 1, \"b\": 2}";
        String padded = "{" + " ".repeat(length - body.length()) + body.substring(1);
        assertThat(padded).hasSize(length);
        return padded;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overlongValues")
    void rejectsAnOverlongValueWhileParsing(String name, String body) {
        assertProblem(post(body), 400, "/problems/invalid-request");
    }

    static Stream<Arguments> overlongValues() {
        String digits = "1".repeat(1_001);
        return Stream.of(
                Arguments.of("number Operand", "{\"operation\": \"ADD\", \"a\": " + digits + ", \"b\": 1}"),
                Arguments.of("string Operand", "{\"operation\": \"ADD\", \"a\": \"" + digits + "\", \"b\": 1}"),
                Arguments.of("operation", "{\"operation\": \"" + "A".repeat(1_001) + "\", \"a\": 1, \"b\": 1}"));
    }

    @Test
    void parsesAStringAtTheLengthLimitThenAppliesBR1() {
        assertProblem(
                post("{\"operation\": \"ADD\", \"a\": \"" + "1".repeat(1_000) + "\", \"b\": 1}"),
                400,
                "/problems/operand-out-of-range");
    }

    @Test
    void reportsAnUnknownOperationBeforeOperandLimits() {
        assertProblem(
                post("{\"operation\": \"NOPE\", \"a\": \"1E+1001\", \"b\": 1}"), 400, "/problems/unknown-operation");
    }

    @Test
    void reportsOperandLimitsBeforeDivisionByZero() {
        assertProblem(
                post("{\"operation\": \"DIVIDE\", \"a\": \"1E+1001\", \"b\": 0}"),
                400,
                "/problems/operand-out-of-range");
    }

    @Test
    void reportsAnUnknownOperationBeforeTheOperandCount() {
        assertProblem(post("{\"operation\": \"NOPE\", \"a\": 1}"), 400, "/problems/unknown-operation");
    }

    @Test
    void reportsTheOperandCountBeforeOperandLimits() {
        assertProblem(
                post("{\"operation\": \"SQRT\", \"a\": \"1E+1001\", \"b\": 1}"), 400, "/problems/invalid-request");
    }

    @Test
    void reportsOperandLimitsBeforeANegativeSquareRoot() {
        assertProblem(post("{\"operation\": \"SQRT\", \"a\": \"-1E+1001\"}"), 400, "/problems/operand-out-of-range");
    }

    @Test
    void reportsOperandLimitsBeforeAnInvalidExponent() {
        assertProblem(
                post("{\"operation\": \"POWER\", \"a\": \"1E+1001\", \"b\": 0.5}"),
                400,
                "/problems/operand-out-of-range");
    }

    @Test
    void reportsAnInvalidExponentBeforeDivisionByZero() {
        assertProblem(post("{\"operation\": \"POWER\", \"a\": 0, \"b\": -1000}"), 422, "/problems/invalid-exponent");
    }

    // BR-1 rejects every input known to reach the 500 handler, so it is called directly.
    @Test
    void reportsAnUnexpectedFailureWithoutInternals() {
        ProblemDetail problem =
                new ProblemDetailsAdvice().unexpected(new IllegalStateException("secret internal state"));

        assertThat(problem.getStatus()).isEqualTo(500);
        assertThat(problem.getType()).hasToString("about:blank");
        assertThat(problem.getTitle()).isNotBlank();
        assertThat(problem.getDetail()).isEqualTo("The calculation could not be completed.");
    }

    @Test
    void rejectsAnUnsupportedMediaType() {
        MvcTestResult result = mvc.post()
                .uri(PATH)
                .contentType(MediaType.TEXT_PLAIN)
                .content("ADD 1 2")
                .exchange();

        assertProblem(result, 415, "about:blank");
    }

    @Test
    void rejectsAnUnknownPath() {
        assertProblem(mvc.get().uri("/api/v1/nope").exchange(), 404, "about:blank");
    }

    @Test
    void rejectsAnUnacceptableResponseType() {
        MvcTestResult result = mvc.post()
                .uri(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .content("{\"operation\": \"ADD\", \"a\": 1, \"b\": 2}")
                .exchange();

        assertProblem(result, 406, "about:blank");
    }

    @Test
    void rejectsAnUnsupportedMethod() {
        assertProblem(mvc.get().uri(PATH).exchange(), 405, "about:blank");
    }

    private MvcTestResult post(String body) {
        return mvc.post()
                .uri(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();
    }

    /** Problem Details shape (RFC 9457) with no Java class or package name in the detail. */
    private static void assertProblem(MvcTestResult result, int status, String type) {
        assertThat(result).hasStatus(status).hasContentTypeCompatibleWith(PROBLEM);
        assertThat(result).bodyJson().extractingPath("$.type").isEqualTo(type);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(status);
        assertThat(result).bodyJson().extractingPath("$.title").asString().isNotBlank();
        assertThat(result)
                .bodyJson()
                .extractingPath("$.detail")
                .asString()
                .isNotBlank()
                .doesNotContainPattern("(java|tools|com|org)\\.[a-z]")
                .doesNotContain("Exception");
    }
}
