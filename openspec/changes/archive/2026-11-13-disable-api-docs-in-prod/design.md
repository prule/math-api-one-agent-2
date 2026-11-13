# Design

## Context

Springdoc 3.2.0 registers `/v3/api-docs` and `/swagger-ui.html` / `/swagger-ui/**` itself; its swagger-ui assets come from the `org.webjars:swagger-ui` jar, which Spring Boot's default static-resource handler also serves at `/webjars/**`. `ProblemDetailsAdvice` extends `ResponseEntityExceptionHandler`, so a request no handler matches (`NoResourceFoundException` / `NoHandlerFoundException`) already becomes `404` Problem Details with `type` `about:blank`.

## Goals / Non-Goals

**Goals:** docs off under `prod` using only the libraries' own switches; one test proving it.

**Non-Goals:** a custom `mathapi.docs.enabled` flag or `@ConditionalOnProperty` beans (STANDARDS §8 forbids duplicating library switches).

## Decisions

- **Three properties in `application-prod.yaml`.** `springdoc.api-docs.enabled=false` removes the description endpoint (and springdoc's OpenAPI beans, so `OpenApiConfig` / `ProblemExamples` customizers simply never run); `springdoc.swagger-ui.enabled=false` removes the UI and its `/swagger-ui/**` handler; `spring.web.resources.add-mappings=false` removes the default `/webjars/**` and `/**` static handlers, which would otherwise still serve the swagger-ui webjar. The app has no static content of its own, so nothing else is lost. Alternative: exclude the webjar from the prod artifact — rejected, the same jar serves dev and prod.
- **Test hits the endpoints, not the properties.** `ApiDocumentationDisabledTest` is a `@SpringBootTest` with `@AutoConfigureMockMvc` and `@ActiveProfiles("prod")`, like `ApiDocumentationTest`, because springdoc and static handlers are not in the web slice. It also does one calculation and one health check, proving the profile breaks nothing else.
- **`OpenApiConfig` beans stay unconditional.** They are plain `OpenApiCustomizer` beans; with springdoc's api-docs off nothing calls them. Making them conditional would be a custom switch.

## Risks / Trade-offs

- [A future static page would be blocked under `prod` by `add-mappings=false`] → STANDARDS §8 already records why it is set; whoever adds static content revisits it, and the test would still pass, so the comment in the yaml is the guard.
- [A third `@SpringBootTest` context slows the build slightly] → accepted; STANDARDS §8 requires an `@ActiveProfiles` test per profile file.
