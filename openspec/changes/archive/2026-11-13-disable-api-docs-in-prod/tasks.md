# Tasks

## 1. Prod profile turns the docs off

- [x] 1.1 Write `ApiDocumentationDisabledTest` (`@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("prod")`) covering every scenario of "API docs are not served under the prod profile": `404` `application/problem+json` for `/v3/api-docs`, `/swagger-ui.html`, `/swagger-ui/index.html`, `/webjars/swagger-ui/index.html`; calculation `0.1 + 0.2 = 0.3` and health `UP` still `200`. Verify it fails with `./gradlew test --tests '*ApiDocumentationDisabledTest'`, and that each of the four URLs is actually served (200, or a redirect for `/swagger-ui.html`) before the profile file exists, so no 404 assertion passes vacuously.
- [x] 1.2 Add `src/main/resources/application-prod.yaml` with `springdoc.api-docs.enabled: false`, `springdoc.swagger-ui.enabled: false`, `spring.web.resources.add-mappings: false`, each commented with why. Verify 1.1 passes and `ApiDocumentationTest` (no profile) still passes; then remove `add-mappings` once and confirm the webjar assertion (and only it) fails, then restore it.

- [x] 1.3 (code review) Prove the UI switch is independent: under `prod` with `springdoc.api-docs.enabled=true`, `/v3/api-docs` is 200 and `/swagger-ui/index.html` 404; verify it fails without `springdoc.swagger-ui.enabled: false`.

## 2. Docs

- [x] 2.1 Add a change-log row for `disable-api-docs-in-prod` to docs/PRD.md (§3.3 already states the behaviour) and docs/STANDARDS.md (§8 already names the profile and test). Verify both §3.3 and §8 match what was built.
- [x] 2.2 Run `./gradlew clean build` and verify it passes (tests, ArchUnit, Spotless).

## Workflow follow-up

- Both code reviews, archive, retrospective, PR, merge on green (CLAUDE.md).
