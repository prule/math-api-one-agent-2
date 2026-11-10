package com.paulrule.mathapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServiceHealthSmokeTest {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Test
    void startedInstanceReportsUpWithoutDetails() throws Exception {
        HttpResponse<String> response = get("/actuator/health");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type"))
                .hasValueSatisfying(type ->
                        assertThat(type).matches("application/(json|vnd\\.spring-boot\\.actuator\\.v3\\+json).*"));
        JsonNode body = mapper.readTree(response.body());
        assertThat(body.path("status").asString()).isEqualTo("UP");
        assertThat(body.has("components")).isFalse();
        assertThat(body.has("details")).isFalse();
    }

    @Test
    void actuatorIndexLinksOnlyToHealth() throws Exception {
        HttpResponse<String> response = get("/actuator");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(mapper.readTree(response.body()).path("_links").propertyNames())
                .containsExactlyInAnyOrder("self", "health", "health-path");
    }

    @Test
    void performsACalculationEndToEnd() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/calculations"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"operation\": \"ADD\", \"a\": 0.1, \"b\": \"0.2\"}"))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(mapper.readTree(response.body()).path("result").asString()).isEqualTo("0.3");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/actuator/env", "/actuator/beans", "/actuator/info"})
    void otherOperationalEndpointsAreNotServed(String path) throws Exception {
        assertThat(get(path).statusCode()).isEqualTo(404);
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
