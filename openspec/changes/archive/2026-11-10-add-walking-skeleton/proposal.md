# Proposal

## Why

Nothing exists yet but docs. Every later backlog item should only add behaviour, so the build, the layer rules, formatting and CI have to be in place and proven first, before any Calculation code is written.

## What Changes

- Gradle 9.8.2 Kotlin DSL build with the wrapper committed, Java 25 toolchain, Spring Boot 4.1.2 (webmvc, validation, actuator), group `com.paulrule`.
- Base package `com.paulrule.mathapi` with the four STANDARDS.md §2 layer packages (`domain`, `application`, `adapter.in.web`, `config`), each holding only a `package-info.java`.
- Spotless 8.11.0 with palantir-java-format 2.103.0 (Java) and ktlint (`*.gradle.kts`), checked by `./gradlew build`.
- ArchUnit 1.5.1 tests enforcing the STANDARDS.md §2 dependency rules, plus a test proving the rules actually fail on a violation.
- One `@SpringBootTest` smoke test: the context starts and `GET /actuator/health` returns `UP`.
- Actuator exposes only `health` (and the `/actuator` index linking to it) over HTTP, using Boot's defaults pinned by tests.
- GitHub Actions workflow `.github/workflows/build.yml` running `./gradlew build` on every PR and push to `main`.
- `.gitignore` already covers Gradle, IDE files and `logs/`; this change confirms it rather than rewriting it.

**Out of scope:** any Calculation behaviour (Operations, `POST /api/v1/calculations`, Problem Details mapping), OpenAPI docs and Swagger UI, the `prod` profile.

## Capabilities

### New Capabilities
- `service-health`: whether a running instance reports itself available, and which operational endpoints it exposes.

### Modified Capabilities

None.

## Impact

- New build files: `settings.gradle.kts`, `build.gradle.kts`, `gradlew`, `gradlew.bat`, `gradle/wrapper/*`.
- New source: `MathApiApplication`, four `package-info.java`, `application.yaml`.
- New tests: smoke test, ArchUnit rules and the test that proves them live.
- New CI workflow.
- Docs to update: none. STANDARDS.md already pins every version used here and already describes the CI workflow; PRD.md §3.3 already lists `GET /actuator/health`; DOMAIN-MODEL.md is untouched.
