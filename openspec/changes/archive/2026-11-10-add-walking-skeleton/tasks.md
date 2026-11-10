# Tasks

## 1. Build scaffold

- [x] 1.1 Generate the Gradle 9.8.2 wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`) and add `settings.gradle.kts` (`rootProject.name = "math-api"`); verify `./gradlew --version` reports 9.8.2
- [x] 1.2 Write `build.gradle.kts`: Java 25 toolchain, Spring Boot 4.1.2 plugin and BOM platform, webmvc/validation/actuator starters, webmvc-test starter, ArchUnit 1.5.1, Spotless 8.11.0 with palantir-java-format 2.103.0 and ktlint for `*.gradle.kts`; verify `./gradlew dependencies --configuration runtimeClasspath` resolves
- [x] 1.3 Add `MathApiApplication` and a `package-info.java` in each of `domain`, `application`, `adapter.in.web`, `config`; verify `./gradlew compileJava` passes

## 2. Health endpoint (test-first)

- [x] 2.1 Write `ServiceHealthSmokeTest` (`@SpringBootTest`, random port, JDK `HttpClient`) covering every `service-health` scenario: health is `200` with `status` `UP` and no `components`/`details`; `/actuator` links only `self`, `health`, `health-path`; `/actuator/env`, `/actuator/beans`, `/actuator/info` are `404`; verify it passes on Boot defaults, then fails when `management.endpoints.web.exposure.include=*` is set as a test property (then remove it)
- [x] 2.2 Add `application.yaml` with only `spring.application.name: math-api`; verify `ServiceHealthSmokeTest` passes

## 3. Architecture rules (test-first)

- [x] 3.1 Add the test fixtures (a `domain` class with a Spring-typed field; an `application` class depending on Spring web and a `adapter.in.web` class; no stereotype annotations) and `ArchitectureRulesAreLiveTest` checking each of `ArchitectureTest`'s rule fields (not copies) against them and expecting an `AssertionError`; verify it fails to compile until the rule exists
- [x] 3.2 Write `ArchitectureTest` with the STANDARDS.md §2 rules as `static final ArchRule` fields (layered architecture with optional layers, domain framework ban incl. `tools.jackson`, application ban on Spring web and adapters); verify `ArchitectureTest` and `ArchitectureRulesAreLiveTest` pass

## 4. Formatting and CI

- [x] 4.1 Run `./gradlew spotlessApply`, then verify `./gradlew spotlessCheck` passes and fails after a deliberate formatting break (then revert)
- [x] 4.2 Add `.github/workflows/build.yml` (PRs and pushes to `main`; Temurin 25; `gradle/actions/setup-gradle`; `./gradlew build`; `contents: read`); verify the YAML parses
- [x] 4.3 Confirm `.gitignore` covers `.gradle`, `build/`, IDE files and `logs/` and that `git status` shows no build output after a build

## 5. Integration check

- [x] 5.1 Run `./gradlew clean build`; verify it passes with tests, ArchUnit and Spotless
- [x] 5.2 Docs: confirm no change is needed to PRD.md, DOMAIN-MODEL.md or STANDARDS.md (versions and CI already documented); if any pin moved during 1.2, update STANDARDS.md §1 and its change log

## Workflow follow-up

- Code review (ponytail and `/code-review`), archive, retrospective, PR and merge on green, per `CLAUDE.md`.
