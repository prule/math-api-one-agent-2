package com.paulrule.mathapi.config;

import com.paulrule.mathapi.domain.Operation;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Puts values that come from the domain into the generated docs, so they never drift (STANDARDS.md §4). */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenApiCustomizer domainValues() {
        return openApi -> {
            Map<String, Schema> request = openApi.getComponents()
                    .getSchemas()
                    .get("CalculationRequest")
                    .getProperties();
            request.get("operation")
                    .setEnum(Arrays.stream(Operation.values())
                            .map(Operation::name)
                            .toList());
            // springdoc maps BigDecimal to number only; the API also accepts strings.
            request.put("a", numberOrString());
            request.put("b", numberOrString());
        };
    }

    private static Schema<Object> numberOrString() {
        return new Schema<>().types(Set.of("number", "string"));
    }
}
