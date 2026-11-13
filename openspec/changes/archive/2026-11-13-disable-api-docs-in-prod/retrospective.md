# Retrospective: disable-api-docs-in-prod

## Caught by review

Plan review (ponytail, on the planning artifacts):
1. **The webjar 404 assertion could pass vacuously.** The versionless `/webjars/swagger-ui/index.html` works only because `webjars-locator-lite` is on the classpath; a wrong URL would 404 in every profile and the check would prove nothing about `add-mappings`. Fixed in tasks 1.1/1.2: the red run showed each URL served (200, 200, 302, 200) before the profile file existed, and removing `add-mappings` alone made exactly the webjar case fail.

Code review, ponytail:
2. **`springdoc.swagger-ui.enabled: false` had no test that failed without it.** Turning off api-docs already removes the UI, so deleting the line left the test green, yet PRD §3.3 says each part can be switched on its own. Added the nested `WithOnlyTheDescriptionReEnabled` case (prod with `springdoc.api-docs.enabled=true`: description 200, UI 404), a spec scenario and task 1.3; it fails without the line (checked).

Code review, `/code-review` standards axis:
3. **STANDARDS §6 had no row for profile tests**, though this change adds a profile `@SpringBootTest`. Added a "Profile" row; the STANDARDS change-log row says so.
4. **The proposal did not mention the nested re-enable case.** Proposal "What Changes" and "Impact" updated.
5. **The new scenario named the env var but the test sets the property.** Scenario now names `springdoc.api-docs.enabled=true`, with the env var as an example (relaxed binding makes them the same).

Left as is (standards smells, both judged acceptable by the reviewer): the nested class repeats the outer setup because it needs its own context; `stillServesTheApiAndHealth` repeats the smoke test on purpose, to prove the profile breaks nothing else.

Code review, `/code-review` spec axis:
6. **The nested 404 did not assert Problem Details**, which the requirement demands for each such request. Added the `application/problem+json` assertion and "Problem Details" to the scenario. This strengthens a check on behaviour the advice already had, so it cannot be made to fail without breaking the advice; not shown red.

## Surprises

- **One chained shell command stopped half-way.** `tail -2 a b` is invalid with two files, so the `&&` chain stopped after writing the change-log rows but before ticking task 2.1 and running the build. Cause: GNU `tail` needs `-n 2` for multiple files. Caught by the next status check, which showed 2 ticked tasks instead of 3.

## Shortcuts taken

None.

## Lessons

- **Propose step, for config-only changes:** for each property planned, ask "which test fails if this line is deleted?" in the plan itself; review found one property with no such test.
- **Apply habit:** run status-changing edits (ticking tasks) in their own command, not at the end of a long `&&` chain behind a read-only step that can fail.
