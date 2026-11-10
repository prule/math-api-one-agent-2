# Retrospective: add-walking-skeleton

## Caught by review

Plan review (ponytail, on the planning artifacts):
1. **The spec overclaimed.** It said no operational endpoint other than health is exposed, but Boot also serves the `/actuator` index. Fixed by rewording the requirement and adding the scenario "Actuator index links only to health", so the spec matches what Boot really serves.
2. **The live test could check a copy of the rule.** Fixed in the design: each rule is a `static final ArchRule` in `ArchitectureTest`, and `ArchitectureRulesAreLiveTest` checks those same fields.
3. **The smoke-test client was left open.** Boot 4's `RestTestClient` would have needed an extra test module. Fixed by choosing the JDK `HttpClient`, so no dependency is added.
4. **The actuator settings repeated Boot's defaults.** Dropped from the plan. `application.yaml` holds only the application name, and the smoke test pins each default. Proven by the red run with `exposure.include=*` (4 of 5 checks fail).

Code review, ponytail:
5. **`build.gradle.kts` imported the Boot BOM twice.** The `testImplementation(platform(...))` line was removed, since the test classpath already inherits it from `implementation`. Build files have no test; `./gradlew build` still resolves every test dependency.

Code review, `/code-review` (standards and spec):
6. **The `@Component` fixture leaked into every `@SpringBootTest` context** (both axes caught it). `UsesSpring` now depends on Spring through a plain field, with no stereotype annotation. No test was added. A test asserting a test fixture is not a bean would only guard test code; the design now states the rule ("fixtures use plain fields, not stereotype annotations").
7. **Only the domain rule was proven to fire** (both axes; design.md claimed every rule was live). Added the fixtures `UsesWeb` and `WebType`, plus live tests for `APPLICATION_HAS_NO_WEB_OR_ADAPTERS` and `LAYERS_DEPEND_INWARDS`. Breaking either rule now fails its live test (checked).
8. **`ArchitectureTest` repeated package names as `..domain..` wildcards, and `..adapter..` matched any third-party package.** The rules now reuse the `DOMAIN` / `APPLICATION` constants and the full `com.paulrule.mathapi.adapter..` package. The live tests cover the change.
9. **The validation starter had no stated reason** (STANDARDS §1). design.md now says why: STANDARDS pins it, and item 2 needs it.
10. **The rule that nothing may access Config is stricter than STANDARDS §2.** Recorded in design.md as a deliberate choice.
11. **Unrequested `version = "0.0.1-SNAPSHOT"`.** Deleted.
12. **Mysterious name `json` for an `ObjectMapper`.** Renamed to `mapper`.

Left as is: config.yaml's "a task for each affected doc" did not cover the BACKLOG.md status edit. CLAUDE.md step 3 already requires that edit on every change, so a task for it would only repeat the rule.

## Surprises

- `which gradle` found nothing, so the wrapper was generated with the cached 9.8.2 distribution under `~/.gradle/wrapper/dists`. Cause: there is no system Gradle install on this machine.
- A `grep` exit code of 1 was briefly mistaken for a build failure, because grep's exit code masked the build's. Cause: the build output was piped through grep. The fix was to check the build's own exit code.

## Shortcuts taken

- `src/test/java/com/paulrule/mathapi/architecture/ArchitectureTest.java:20`: `withOptionalLayers(true)` and `allowEmptyShould(true)`. Limit: the rules pass on empty layers (the live tests make up for this meanwhile). Upgrade: remove both in backlog item 2, once every layer has a class.

## Lessons

- **Plan step:** when a requirement says "only X", check what the framework serves by default (here, the `/actuator` index) before writing the spec. Ponytail caught it this time; propose should catch it first.
- **Design step:** a goal such as "every rule is live" must have a task for each rule, not just one example. The gap between the goal and the tasks survived the plan review and was caught only by `/code-review`.
- **Test fixtures:** never put Spring stereotype annotations on fixtures under the `com.paulrule.mathapi` scan root; use plain fields to create the dependency.
- **Verification habit:** never pipe `./gradlew` through `grep` to judge success; check `$?` or the test-result XML.
