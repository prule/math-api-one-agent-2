package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.domain.DivisionByZeroException;
import com.paulrule.mathapi.domain.InvalidExponentException;
import com.paulrule.mathapi.domain.NegativeSquareRootException;
import com.paulrule.mathapi.domain.OperandOutOfRangeException;
import com.paulrule.mathapi.domain.ResultOutOfRangeException;
import com.paulrule.mathapi.domain.UnknownOperationException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.net.URI;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

/**
 * Documents the calculations endpoint's errors as clients see them: one example per Problem Details type, produced
 * by {@link ProblemDetailsAdvice} itself (STANDARDS.md §4), and a {@code ProblemDetail} schema with only the fields
 * on the wire.
 */
@Component
class ProblemExamples implements OpenApiCustomizer {

    private final ProblemDetailsAdvice advice;

    ProblemExamples(ProblemDetailsAdvice advice) {
        this.advice = advice;
    }

    @Override
    public void customise(OpenAPI openApi) {
        // Jackson writes ProblemDetail's extra properties as top-level fields, so the "properties" map never appears.
        openApi.getComponents()
                .getSchemas()
                .get("ProblemDetail")
                .getProperties()
                .remove("properties");

        ApiResponses responses =
                openApi.getPaths().get(CalculationController.PATH).getPost().getResponses();
        for (ProblemDetail problem : examples()) {
            String type = problem.getType().toString();
            responses
                    .get(String.valueOf(problem.getStatus()))
                    .getContent()
                    .get(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .addExamples(type.substring(type.lastIndexOf('/') + 1), new Example().value(problem));
        }
    }

    private List<ProblemDetail> examples() {
        List<ProblemDetail> examples = List.of(
                ProblemDetailsAdvice.invalidRequest(),
                advice.unknownOperation(new UnknownOperationException("NOT_AN_OPERATION")),
                advice.operandOutOfRange(new OperandOutOfRangeException("b")),
                advice.divisionByZero(new DivisionByZeroException()),
                advice.invalidExponent(new InvalidExponentException()),
                advice.negativeSquareRoot(new NegativeSquareRootException()),
                advice.resultOutOfRange(new ResultOutOfRangeException()));
        // Spring fills instance from the request path; set it so each example is the whole real body.
        examples.forEach(problem -> problem.setInstance(URI.create(CalculationController.PATH)));
        return examples;
    }
}
