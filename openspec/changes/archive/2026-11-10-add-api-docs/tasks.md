# Tasks

## 1. Dependency and served docs (test-first)

- [x] 1.1 Write `ApiDocumentationTest` (`@SpringBootTest`, `@AutoConfigureMockMvc`) with the "description is served" (`openapi` starts with `3.1`) and "UI is served" scenarios; verify it fails
- [x] 1.2 Add `springdoc-openapi-starter-webmvc-ui:3.2.0` to `build.gradle.kts`; verify both tests pass

## 2. Request and response documentation (test-first)

- [x] 2.1 Add tests: `operation` enum equals `Operation.values()` names in order; `a` and `b` allow `number` and `string`; all three required; `200` is `application/json` with only `string` properties; verify they fail
- [x] 2.2 Add the `200` `@ApiResponse` and `config.OpenApiConfig` (enum and number-or-string); verify the tests pass (no `override-with-generic-response` needed: springdoc adds no generic responses here)

## 3. Problem Details documentation (test-first)

- [x] 3.1 Add tests: `400`, `415` and `422` are `application/problem+json`; `400` and `422` carry the four examples by `type`; for each type, post a real failing request and assert `type`, `title`, `status`, `detail` equal the example; verify they fail
- [x] 3.2 Add the `400`/`415`/`422` `@ApiResponse`s and `ProblemExamples` (the web adapter's examples customizer); verify the tests pass

## 4. Integration check

- [x] 4.1 Run `./gradlew clean build`; verify it passes (ArchUnit included: `config` may use application and domain, nothing may use `adapter.in.web` or `config`)
- [x] 4.2 Docs: STANDARDS.md §2 diagram matches the ArchUnit rule, §4 names `ProblemExamples` for error examples, §6 lists `ApiDocumentationTest`, with a change-log row; PRD.md and DOMAIN-MODEL.md unchanged

## Workflow follow-up

- Code review (ponytail and `/code-review`), archive, retrospective, PR and merge on green, per `CLAUDE.md`.
