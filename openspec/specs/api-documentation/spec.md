# api-documentation Specification

## Purpose
Lets client developers discover and try the API from a generated OpenAPI 3.1 description that always matches the running code, including every Operation and every Problem Details error.

## Requirements

### Requirement: OpenAPI description and UI are served
With no profile active, the service SHALL serve an OpenAPI 3.1 JSON description at `GET /v3/api-docs` and an interactive UI at `GET /swagger-ui.html`, without authentication.

#### Scenario: Description is served
- **WHEN** a client sends `GET /v3/api-docs`
- **THEN** the response is `200` JSON whose `openapi` field starts with `3.1`

#### Scenario: UI is served
- **WHEN** a client sends `GET /swagger-ui.html`
- **THEN** the response is `200`, or a redirect to a page that responds `200`

### Requirement: API docs are not served under the prod profile
When the `prod` profile is active, the service SHALL NOT serve the OpenAPI description, the interactive UI or its static assets: each such request SHALL get `404` Problem Details. The API and the health endpoint SHALL keep working unchanged.

#### Scenario: Description is not served
- **WHEN** the `prod` profile is active and a client sends `GET /v3/api-docs`
- **THEN** the response is `404` with content type `application/problem+json`

#### Scenario: UI and its assets are not served
- **WHEN** the `prod` profile is active and a client sends `GET /swagger-ui.html`, `GET /swagger-ui/index.html` or `GET /webjars/swagger-ui/index.html`
- **THEN** each response is `404` with content type `application/problem+json`

#### Scenario: API and health still work
- **WHEN** the `prod` profile is active and a client sends `POST /api/v1/calculations` with `{"operation":"ADD","a":"0.1","b":"0.2"}`, then `GET /actuator/health`
- **THEN** the first response is `200` with `result` `"0.3"`, and the second is `200` with `status` `"UP"`

#### Scenario: Re-enabling only the description leaves the UI off
- **WHEN** the `prod` profile is active with the description switched back on through the library's own setting (`springdoc.api-docs.enabled=true`, e.g. set as `SPRINGDOC_API_DOCS_ENABLED=true`), and a client sends `GET /v3/api-docs` then `GET /swagger-ui/index.html`
- **THEN** the first response is `200` and the second is `404` Problem Details

### Requirement: Calculation request and response documented
The description SHALL document `POST /api/v1/calculations` with its request body (`operation`, `a`, `b` all required; `a` and `b` accept a JSON number or string; `operation` lists exactly the supported Operation names) and its `200` response as `application/json` with `operation`, `a`, `b` and `result` as strings.

#### Scenario: Allowed operations come from the domain
- **WHEN** the description is read
- **THEN** the request's `operation` enumerates exactly the supported Operations, in declaration order

#### Scenario: Operands accept number or string
- **WHEN** the description is read
- **THEN** the request's `a` and `b` each allow both `number` and `string`

#### Scenario: Success response is JSON strings
- **WHEN** the description is read
- **THEN** the `200` response has media type `application/json` (not `*/*`) and every property of its schema is a `string`

### Requirement: Every Problem Details response documented with examples
The description SHALL list, for `POST /api/v1/calculations`, the `400`, `415` and `422` responses as `application/problem+json`, with one example per Problem Details type the endpoint can return (listed in the first scenario). Each example SHALL be the whole body the API actually returns for that error, and the documented `ProblemDetail` schema SHALL list only the fields that appear on the wire.

#### Scenario: Error responses and examples
- **WHEN** the description is read
- **THEN** `400` has examples whose `type` is `/problems/invalid-request`, `/problems/unknown-operation` and `/problems/operand-out-of-range` (BR-1), and `422` has examples whose `type` is `/problems/division-by-zero` (BR-2), `/problems/invalid-exponent` (BR-5) and `/problems/result-out-of-range` (BR-6)

#### Scenario: Schema lists only wire fields
- **WHEN** the description is read
- **THEN** the `ProblemDetail` schema's properties are exactly `type`, `title`, `status`, `detail` and `instance`

#### Scenario: Unsupported media type documented
- **WHEN** the description is read
- **THEN** `POST /api/v1/calculations` also lists a `415` response as `application/problem+json`

#### Scenario: Examples match real responses
- **WHEN** the API is sent a request that fails with each documented type
- **THEN** the response body's `type`, `title`, `status`, `detail` and `instance` equal the documented example's

#### Scenario: Unknown-operation example lists exactly the supported Operations
- **WHEN** the description is read
- **THEN** the Operation names listed in the `unknown-operation` example's `detail` equal the supported Operations, in declaration order
