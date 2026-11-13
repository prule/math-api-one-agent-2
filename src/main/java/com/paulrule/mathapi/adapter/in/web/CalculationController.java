package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.application.PerformCalculation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalculationController {

    static final String PATH = "/api/v1/calculations";

    private final PerformCalculation performCalculation;

    CalculationController(PerformCalculation performCalculation) {
        this.performCalculation = performCalculation;
    }

    @ApiResponse(
            responseCode = "200",
            description = "The Calculation's Result",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CalculationResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request, unknown operation or operand out of range",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "415",
            description = "Content-Type is not application/json",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "422",
            description = "Valid input that breaks a business rule; one example per type",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping(PATH)
    CalculationResponse calculate(@Valid @RequestBody CalculationRequest request) {
        return CalculationResponse.of(performCalculation.perform(request.operation(), request.a(), request.b()));
    }
}
