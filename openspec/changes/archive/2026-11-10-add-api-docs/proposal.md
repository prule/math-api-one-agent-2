# Proposal

## Why

Clients can call `POST /api/v1/calculations` but have nothing to read except the PRD. PRD §3.3 promises OpenAPI 3.1 docs and an interactive UI; this is the last v1 item (backlog item 5).

## What Changes

- Add springdoc-openapi 3.2.0 (`springdoc-openapi-starter-webmvc-ui`, the release STANDARDS.md §1 pins for Spring Boot 4), serving `/v3/api-docs` (OpenAPI 3.1 JSON) and `/swagger-ui.html`.
- Document `POST /api/v1/calculations`: the request schema (Operands as number or string, `operation` limited to the supported names), the response schema (all strings, `application/json`), and every Problem Details response it returns (`400` with its three types, `415`, `422`) as `application/problem+json`, with one example per PRD-listed type.
- Values that come from the code are generated, so a new Operation appears in the docs with no doc edit: the allowed `operation` names in `config.OpenApiConfig`, and the error examples (including the unknown-operation detail that lists the names) by the advice itself, in `adapter.in.web.ProblemExamples`.
- `ApiDocumentationTest` asserts the documented path, request, response and error responses.

**Out of scope:** turning the docs off per environment (`prod` profile, item 5a); hand-written docs outside the generated spec; documenting generic framework errors (404, 405, 406) per operation; the `500` response (no request can reach it since BR-1).

## Capabilities

### New Capabilities
- `api-documentation`: the machine-readable OpenAPI description of the API and the interactive UI, and what they must say about each endpoint.

### Modified Capabilities

None.

## Impact

- New dependency: `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0` (justified: PRD §3.3; already pinned in STANDARDS.md §1).
- `adapter.in.web`: `@ApiResponse` annotations on the controller; `ProblemExamples`, an `OpenApiCustomizer` that adds the error examples by calling `ProblemDetailsAdvice` itself. `config`: `OpenApiConfig` (an `OpenApiCustomizer` for the domain values). No springdoc configuration.
- New `ApiDocumentationTest` (`@SpringBootTest`, MockMvc).
- Docs to update: STANDARDS.md §2 (diagram matched to the existing ArchUnit rule), §4 (where error examples come from), §6 (`ApiDocumentationTest` row), with a change-log row. PRD.md §3.3 already describes the endpoints; §1 already pins springdoc 3.2.0.
