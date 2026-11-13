# Proposal

## Why

Item 5 made `/v3/api-docs` and Swagger UI always on. PRD §3.3 and STANDARDS §8 say they are on by default for developers but off under the `prod` profile, so an environment reachable beyond developers does not advertise the API's internals or serve the Swagger UI bundle. Nothing implements that yet (backlog item 5a).

## What Changes

- Add `src/main/resources/application-prod.yaml`, activated by `SPRING_PROFILES_ACTIVE=prod`, that sets only library switches: `springdoc.api-docs.enabled: false`, `springdoc.swagger-ui.enabled: false` and `spring.web.resources.add-mappings: false` (otherwise Spring's default static-resource handler still serves the swagger-ui webjar under `/webjars/**`). Each line commented with why.
- Add `ApiDocumentationDisabledTest` (`@SpringBootTest` + `@ActiveProfiles("prod")`) proving `/v3/api-docs`, `/swagger-ui.html`, `/swagger-ui/index.html` and the webjar return `404` Problem Details, while `POST /api/v1/calculations` and `GET /actuator/health` still work; a nested case proves that re-enabling only the description (`springdoc.api-docs.enabled=true`) leaves the UI off.
- Default (no profile) behaviour is unchanged; `ApiDocumentationTest` keeps proving it.

**Out of scope:** any custom property duplicating a library's own switch (single settings can still be flipped with Spring's standard environment variables, e.g. `SPRINGDOC_API_DOCS_ENABLED=true`); any other prod tuning (logging, actuator, server settings); authentication.

## Capabilities

### New Capabilities

None.

### Modified Capabilities
- `api-documentation`: the "OpenAPI description and UI are served" requirement becomes default-only; a new requirement says they are not served under the `prod` profile.

## Impact

- New file `application-prod.yaml`; new test `ApiDocumentationDisabledTest`. No production Java code, no new dependency.
- Docs: PRD.md §3.3 and STANDARDS.md §8 already describe this behaviour and name the test; each gets a change-log row; STANDARDS.md §6 gains a row for profile tests. DOMAIN-MODEL.md: none.
