# Design

## Context

Empty repo apart from the docs. STANDARDS.md §1 pins every version (Gradle 9.8.2, Spring Boot 4.1.2, Spotless 8.11.0, palantir-java-format 2.103.0, ArchUnit 1.5.1) and §2 defines the layers and dependency rules that ArchUnit must enforce. Java 25 (Temurin) and Gradle 9.8.2 are available locally.

## Goals / Non-Goals

**Goals:**
- `./gradlew build` runs tests, ArchUnit and Spotless checks, locally and in CI.
- ArchUnit fails the build when `domain` imports Spring (the backlog's done criterion), and every other §2 rule is live from day one.

**Non-Goals:**
- Any class in `domain`, `application` or `adapter.in.web` beyond `package-info.java`.
- Actuator features beyond health (metrics, info, probes groups).

## Decisions

### Build: plain Spring Boot Gradle plugin, no dependency-management plugin
Use `org.springframework.boot` 4.1.2 with `platform(SpringBootPlugin.BOM_COORDINATES)` for versions. The `io.spring.dependency-management` plugin is not needed with Gradle's native platform support, so it is left out.

Starters: `spring-boot-starter-webmvc`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`; tests use `spring-boot-starter-webmvc-test` (Boot 4 splits test support per starter), `com.tngtech.archunit:archunit-junit5:1.5.1`, and `junit-platform-launcher` at runtime. Only add a further `-test` starter if a test needs it. `spring-boot-starter-validation` has no user yet; it is added now because STANDARDS.md §1 pins it as part of the stack and item 2 validates the request body.

### Formatting: Spotless with palantir-java-format and ktlint, wired into `check`
Spotless's `check` task is already a dependency of `check`, so `./gradlew build` fails on unformatted code. `spotlessApply` fixes it.

### Architecture tests: layered architecture plus explicit framework bans
One `ArchitectureTest`, each rule a `static final ArchRule` field so other tests can reuse the exact rule object, (`@AnalyzeClasses(packages = "com.paulrule.mathapi", importOptions = DoNotIncludeTests.class)`) with:
- `layeredArchitecture()`: `Web` may not be accessed by any layer; `Application` only by `Web` and `Config`; `Domain` only by `Application`, `Web` and `Config`; `Config` by nothing (stricter than STANDARDS.md §2, which only says config may depend on all layers; nothing should depend on wiring).
- `noClasses().that().resideInAPackage("..domain..").should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..", "com.fasterxml..", "tools.jackson..")` (Jackson 3 moved to `tools.jackson`).
- `noClasses().that().resideInAPackage("..application..").should().dependOnClassesThat().resideInAnyPackage("org.springframework.web..", "..adapter..")`.

The layers are empty now, so the layered rule uses `withOptionalLayers(true)` and the `noClasses` rules `allowEmptyShould(true)`. Both are shortcuts to remove once every layer has a class (backlog item 2 says so).

### Proving the rules are live
Empty layers mean the rules pass trivially, which would hide a broken rule. `ArchitectureRulesAreLiveTest` checks each of `ArchitectureTest`'s rule fields (the same objects, never copies) against test-only fixtures that break it, and expects an `AssertionError`: a `domain` class with a Spring-typed field, and an `application` class depending on Spring web and on a `adapter.in.web` class. Fixtures use plain fields, not stereotype annotations, so component scanning in `@SpringBootTest` never makes them beans. The fixture lives under `src/test` so production analysis (which excludes tests) never sees it.

### Smoke test: full context on a random port
`@SpringBootTest(webEnvironment = RANDOM_PORT)` with the JDK `java.net.http.HttpClient` against `@LocalServerPort`. Boot 4's `RestTestClient` would need an extra test module for a running server; the JDK client needs no dependency. This proves the context starts and the health endpoint is served over real HTTP, and the same test covers the "not exposed" scenarios. One class, per STANDARDS.md §6 ("Smoke: one").

### Actuator exposure
No configuration: Boot's defaults already expose only `health` over HTTP (plus the `/actuator` index linking to it) and hide health details and components. `ServiceHealthSmokeTest` pins each of these, so a changed default fails the build. `application.yaml` holds only `spring.application.name`.

### CI
`.github/workflows/build.yml`: checkout, `actions/setup-java` (Temurin 25), `gradle/actions/setup-gradle` (caching and wrapper validation), `./gradlew build`. `permissions: contents: read`.

## Risks / Trade-offs

- [Boot 4.1.2 or plugin versions differ from what STANDARDS.md pins or are unavailable] → resolve during apply; if a pin must move, update STANDARDS.md §1 and its change log in this change.
- [Optional-layer shortcuts hide a broken rule] → `ArchitectureRulesAreLiveTest` proves the domain rule fires; item 2 removes the shortcuts.
- [`application/vnd.spring-boot.actuator.v3+json` vs `application/json`] → the spec accepts either, since clients choose with `Accept`.
