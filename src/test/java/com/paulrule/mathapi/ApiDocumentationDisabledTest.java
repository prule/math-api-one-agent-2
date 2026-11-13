package com.paulrule.mathapi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** The {@code prod} profile (application-prod.yaml) turns off the API docs, the UI and its webjar. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
class ApiDocumentationDisabledTest {

    @Autowired
    private MockMvcTester mvc;

    @ParameterizedTest
    @ValueSource(
            strings = {"/v3/api-docs", "/swagger-ui.html", "/swagger-ui/index.html", "/webjars/swagger-ui/index.html"})
    void doesNotServeTheDocs(String path) {
        assertThat(mvc.get().uri(path).exchange())
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void stillServesTheApiAndHealth() {
        assertThat(mvc.post()
                        .uri("/api/v1/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operation\":\"ADD\",\"a\":\"0.1\",\"b\":\"0.2\"}")
                        .exchange())
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.result")
                .isEqualTo("0.3");
        assertThat(mvc.get().uri("/actuator/health").exchange())
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.status")
                .isEqualTo("UP");
    }

    /** PRD §3.3: each part has its own switch, so re-enabling the description in prod leaves the UI off. */
    @Nested
    @SpringBootTest(properties = "springdoc.api-docs.enabled=true")
    @AutoConfigureMockMvc
    @ActiveProfiles("prod")
    class WithOnlyTheDescriptionReEnabled {

        @Autowired
        private MockMvcTester mvc;

        @Test
        void servesTheDescriptionButNotTheUi() {
            assertThat(mvc.get().uri("/v3/api-docs").exchange()).hasStatusOk();
            assertThat(mvc.get().uri("/swagger-ui/index.html").exchange())
                    .hasStatus(HttpStatus.NOT_FOUND)
                    .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
        }
    }
}
