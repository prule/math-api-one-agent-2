# Design

## Context

`CalculationController` (`POST /api/v1/calculations`) takes `CalculationRequest(String operation, BigDecimal a, BigDecimal b)` and returns `CalculationResponse` (four Strings). `ProblemDetailsAdvice` builds every error body. STANDARDS.md §4 requires an explicit `@ApiResponse` per status (including the 200 media type), a `application/problem+json` example per Problem Details type, documented domain values generated in `config.OpenApiConfig`, and an `ApiDocumentationTest`.

## Goals / Non-Goals

**Goals:**
- Docs that cannot drift from the code: names and error examples are produced by the code that serves them.

**Non-Goals:**
- `@Schema` descriptions on every field, or a hand-written API guide.
- Documenting the unreachable `500` or the generic framework errors per operation.

## Decisions

### Dependency
`implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0")`. Springdoc 3.x targets Spring Boot 4 and emits OpenAPI 3.1 by default. No other dependency.

### Structure in annotations, domain values in a customizer
- `CalculationController.calculate` gets `@ApiResponse`s: `200` (`application/json`, `CalculationResponse`), `400`, `415` and `422` (`application/problem+json`, schema `ProblemDetail`). `415` (wrong `Content-Type`) has no example: its `type` is `about:blank`, not a PRD-listed type, and STANDARDS.md §4 requires an `@ApiResponse` for every status the endpoint returns. `404`, `405` and `406` are not specific to this operation and are left out. These describe shape only, so nothing domain-specific is copied into annotations.
- No `springdoc.override-with-generic-response` setting: checked during apply, springdoc 3.2.0 adds no responses from the advice to this operation, so the explicit annotations are already the only source. `ApiDocumentationTest` pins the exact set of statuses (`200`, `400`, `415`, `422`) so a generic response cannot creep in.
- Two `OpenApiCustomizer`s, one per layer:
  1. `config.OpenApiConfig` (domain values, STANDARDS.md §4): sets `CalculationRequest.operation` to `enum` = `Operation.values()` names, in order, and `CalculationRequest.a`/`.b` to OpenAPI 3.1 `type: [number, string]` (springdoc maps `BigDecimal` to `number` only);
  2. `adapter.in.web.ProblemExamples` (a `@Component`): adds one named example per Problem Details type to the `400` and `422` `application/problem+json` media types.

### Examples come from the advice itself
`ProblemExamples` builds the example bodies by calling `ProblemDetailsAdvice`'s handler methods with real domain exceptions: `unknownOperation(new UnknownOperationException("NOT_AN_OPERATION"))`, `operandOutOfRange(new OperandOutOfRangeException("b"))`, `divisionByZero(new DivisionByZeroException())`, and the advice's `invalidRequest()`. It lives in `adapter.in.web` next to the advice, so both stay package-private and nothing rebuilds a type, title or detail. The unknown-operation example therefore lists every Operation automatically (STANDARDS.md §4).

The plan first put this step in `config.OpenApiConfig`, reaching `ProblemExamples` as a public class. `ArchitectureTest` rejected it: its layer rule lets no layer access `Web`, which is stricter than STANDARDS.md §2 (`config ──► all`). The rule was kept (CLAUDE.md: never weaken an ArchUnit rule to get green) and the step moved into the web adapter instead; STANDARDS.md §2 is corrected to describe the rule as built. `ProblemExamples` also sets each example's `instance` (Spring fills it from the request path) so the example is the whole body, and removes the `properties` map from springdoc's `ProblemDetail` schema, since Jackson writes those as top-level fields.

Alternative considered: `@ExampleObject` literals in annotations. Rejected: the unknown-operation detail would copy the Operation list, which STANDARDS.md §4 forbids, and the other details would drift from the advice.

### `ApiDocumentationTest`
`@SpringBootTest` + `@AutoConfigureMockMvc` (MockMvc, mock environment): springdoc's auto-configuration is not part of the `@WebMvcTest` slice. This is a second full context next to `ServiceHealthSmokeTest`; STANDARDS.md §6's "one" applies to the smoke test, and STANDARDS.md §4 names this test. It reads `/v3/api-docs` with JSON-path assertions for every scenario, and for "examples match real responses" posts one failing request per type and compares `type`, `title`, `status`, `detail` with the documented example.

## Risks / Trade-offs

- [springdoc 3.2.0 and Boot 4.1.2 / Jackson 3 incompatibility] → the first `ApiDocumentationTest` run proves it; if a newer 3.x is required, update STANDARDS.md §1 and its change log in this change.
- [springdoc names the components differently from `CalculationRequest`/`CalculationResponse`/`ProblemDetail`] → the customizer looks schemas up by those names; the test fails loudly if a name differs.
- [Swagger UI webjar is served under `/swagger-ui/**` and goes through our 404 Problem Details handling for missing assets] → harmless; item 5a turns it off in `prod`.
