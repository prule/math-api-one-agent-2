## MODIFIED Requirements

### Requirement: OpenAPI description and UI are served
With no profile active, the service SHALL serve an OpenAPI 3.1 JSON description at `GET /v3/api-docs` and an interactive UI at `GET /swagger-ui.html`, without authentication.

#### Scenario: Description is served
- **WHEN** a client sends `GET /v3/api-docs`
- **THEN** the response is `200` JSON whose `openapi` field starts with `3.1`

#### Scenario: UI is served
- **WHEN** a client sends `GET /swagger-ui.html`
- **THEN** the response is `200`, or a redirect to a page that responds `200`

## ADDED Requirements

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
