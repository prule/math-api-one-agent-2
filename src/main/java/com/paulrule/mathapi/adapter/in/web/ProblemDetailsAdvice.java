package com.paulrule.mathapi.adapter.in.web;

import com.paulrule.mathapi.domain.UnknownOperationException;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Every error is RFC 9457 Problem Details. The base class covers framework errors (404, 405, 415, …); the overrides
 * give invalid input a fixed detail so no parser or class name leaks.
 */
@RestControllerAdvice
class ProblemDetailsAdvice extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ProblemDetailsAdvice.class);

    // Spring 7 leaves type unset (omitted from JSON); the PRD requires every error to carry one.
    private static final URI ABOUT_BLANK = URI.create("about:blank");

    private static final String INVALID_REQUEST_DETAIL =
            "The request body must be JSON with operation, a and b; a and b must be decimal numbers.";

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(ex, invalidRequest(), headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(ex, invalidRequest(), headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(
            Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (body instanceof ProblemDetail problem && problem.getType() == null) {
            problem.setType(ABOUT_BLANK);
        }
        return super.createResponseEntity(body, headers, status, request);
    }

    @ExceptionHandler(UnknownOperationException.class)
    ProblemDetail unknownOperation(UnknownOperationException ex) {
        return problem(HttpStatus.BAD_REQUEST, "unknown-operation", "Unknown operation", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception ex) {
        log.error("Unexpected failure", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "The calculation could not be completed.");
        problem.setType(ABOUT_BLANK);
        return problem;
    }

    private static ProblemDetail invalidRequest() {
        return problem(HttpStatus.BAD_REQUEST, "invalid-request", "Invalid request", INVALID_REQUEST_DETAIL);
    }

    private static ProblemDetail problem(HttpStatus status, String type, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("/problems/" + type));
        problem.setTitle(title);
        return problem;
    }
}
