package com.paulrule.mathapi.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.paulrule.mathapi.config.UseCaseConfig;
import com.paulrule.mathapi.domain.UnknownOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
                "{\"operation\": \"NOPE\", \"a\": 1}",
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

    @Test
    void reportsAnUnexpectedFailureWithoutInternals() {
        assertProblem(
                post("{\"operation\": \"MULTIPLY\", \"a\": \"1e-2000000000\", \"b\": \"1e-2000000000\"}"),
                500,
                "about:blank");
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
