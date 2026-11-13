package com.paulrule.mathapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import com.paulrule.mathapi.domain.Operation;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.json.AbstractJsonContentAssert;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
class ApiDocumentationTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void servesAnOpenApi31Description() {
        assertThat(mvc.get().uri("/v3/api-docs").exchange())
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.openapi")
                .asString()
                .startsWith("3.1");
    }

    @Test
    void servesTheInteractiveUi() {
        MvcTestResult result = mvc.get().uri("/swagger-ui.html").exchange();
        String target = result.getResponse().getRedirectedUrl();

        assertThat(mvc.get().uri(target == null ? "/swagger-ui.html" : target).exchange())
                .hasStatusOk();
    }

    @Test
    void enumeratesExactlyTheSupportedOperationsInOrder() {
        apiDocs()
                .extractingPath("$.components.schemas.CalculationRequest.properties.operation.enum")
                .asArray()
                .containsExactly(
                        Arrays.stream(Operation.values()).map(Operation::name).toArray());
    }

    @Test
    void acceptsOperandsAsNumberOrString() {
        for (String operand : new String[] {"a", "b"}) {
            apiDocs()
                    .extractingPath("$.components.schemas.CalculationRequest.properties." + operand + ".type")
                    .asArray()
                    .containsExactlyInAnyOrder("number", "string");
        }
        apiDocs()
                .extractingPath("$.components.schemas.CalculationRequest.required")
                .asArray()
                .containsExactlyInAnyOrder("operation", "a"); // b is optional (BR-9)
    }

    @Test
    void documentsTheSuccessResponseAsJsonStrings() {
        apiDocs()
                .extractingPath(CALCULATE + ".responses.200.content['application/json'].schema.$ref")
                .isEqualTo("#/components/schemas/CalculationResponse");
        apiDocs().extractingPath(CALCULATE + ".responses.200.content").asMap().containsOnlyKeys("application/json");
        apiDocs()
                .extractingPath("$.components.schemas.CalculationResponse.properties[*].type")
                .asArray()
                .hasSize(4)
                .containsOnly("string");
    }

    @Test
    void documentsThatAUnaryResponseHasNoB() {
        apiDocs()
                .extractingPath("$.components.schemas.CalculationResponse.properties.b.description")
                .isEqualTo("Absent for a unary Operation");
    }

    @Test
    void documentsExactlyTheStatusesTheEndpointReturns() {
        apiDocs().extractingPath(CALCULATE + ".responses").asMap().containsOnlyKeys("200", "400", "415", "422");
    }

    // Jackson writes ProblemDetail's extra properties as top-level fields, never as a "properties" object.
    @Test
    void problemSchemaHasOnlyWireFields() {
        apiDocs()
                .extractingPath("$.components.schemas.ProblemDetail.properties")
                .asMap()
                .containsOnlyKeys("type", "title", "status", "detail", "instance");
    }

    @Test
    void documentsEveryProblemDetailsResponse() {
        for (String status : new String[] {"400", "415", "422"}) {
            apiDocs()
                    .extractingPath(CALCULATE + ".responses." + status + ".content")
                    .asMap()
                    .containsOnlyKeys("application/problem+json");
        }
        apiDocs()
                .extractingPath(CALCULATE + ".responses.400.content['application/problem+json'].examples[*].value.type")
                .asArray()
                .containsExactlyInAnyOrder(
                        "/problems/invalid-request", "/problems/unknown-operation", "/problems/operand-out-of-range");
        apiDocs()
                .extractingPath(CALCULATE + ".responses.422.content['application/problem+json'].examples[*].value.type")
                .asArray()
                .containsExactlyInAnyOrder(
                        "/problems/division-by-zero",
                        "/problems/invalid-exponent",
                        "/problems/negative-square-root",
                        "/problems/result-out-of-range");
    }

    @ParameterizedTest(name = "{1}")
    @CsvSource(
            delimiter = '|',
            value = {
                "400 | invalid-request      | {\"operation\": \"ADD\", \"a\": \"abc\", \"b\": 1}",
                "400 | unknown-operation    | {\"operation\": \"NOT_AN_OPERATION\", \"a\": 1, \"b\": 2}",
                "400 | operand-out-of-range | {\"operation\": \"ADD\", \"a\": 1, \"b\": \"1E+1001\"}",
                "422 | division-by-zero     | {\"operation\": \"DIVIDE\", \"a\": 1, \"b\": 0}",
                "422 | invalid-exponent     | {\"operation\": \"POWER\", \"a\": 2, \"b\": 0.5}",
                "422 | result-out-of-range  | {\"operation\": \"POWER\", \"a\": 100, \"b\": 999}",
                "422 | negative-square-root | {\"operation\": \"SQRT\", \"a\": -1}",
            })
    void documentedExampleIsTheRealResponse(String status, String type, String request) throws Exception {
        String examplePath = CALCULATE + ".responses." + status + ".content['application/problem+json'].examples['"
                + type + "'].value";
        Map<?, ?> example = JsonPath.read(apiDocsJson(), examplePath);

        MvcTestResult response = mvc.post()
                .uri("/api/v1/calculations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .exchange();

        assertThat(response).hasStatus(Integer.parseInt(status));
        for (String field : new String[] {"type", "title", "status", "detail", "instance"}) {
            assertThat(response).bodyJson().extractingPath("$." + field).isEqualTo(example.get(field));
        }
    }

    // The example is built once at startup; this proves it lists every Operation, not a stale set. Parsed, not
    // rebuilt, so adding an Operation needs no edit here.
    @Test
    void unknownOperationExampleListsExactlyTheSupportedOperations() throws Exception {
        String detail = JsonPath.read(
                apiDocsJson(),
                CALCULATE
                        + ".responses.400.content['application/problem+json'].examples['unknown-operation'].value.detail");
        String list = detail.substring(detail.indexOf(": ") + 2, detail.length() - 1);

        assertThat(list.split(", "))
                .containsExactly(
                        Arrays.stream(Operation.values()).map(Operation::name).toArray(String[]::new));
    }

    private String apiDocsJson() throws Exception {
        return mvc.get().uri("/v3/api-docs").exchange().getResponse().getContentAsString();
    }

    private static final String CALCULATE = "$.paths['/api/v1/calculations'].post";

    private AbstractJsonContentAssert<?> apiDocs() {
        return assertThat(mvc.get().uri("/v3/api-docs").exchange())
                .hasStatusOk()
                .bodyJson();
    }
}
