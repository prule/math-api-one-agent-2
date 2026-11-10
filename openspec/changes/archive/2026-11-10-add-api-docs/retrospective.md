# Retrospective: add-api-docs

## Caught by review

Plan review (ponytail, on the planning artifacts):
1. **The endpoint returns 415, but the plan left it undocumented.** STANDARDS §4 says every status an endpoint returns needs an `@ApiResponse`. Fixed by adding a `415` `application/problem+json` response with no example (its type is `about:blank`), a spec scenario, and coverage in `documentsEveryProblemDetailsResponse` plus `documentsExactlyTheStatusesTheEndpointReturns`.

Code review, ponytail and `/code-review` standards axis:
2. **A hand-written `"application/problem+json"` constant, plus a duplicate literal.** Replaced by Spring's `MediaType.APPLICATION_PROBLEM_JSON_VALUE` in both places. The endpoint path is now one `CalculationController.PATH` constant, used by `@PostMapping` and by `ProblemExamples`.

Code review, `/code-review` (spec axis):
3. **The examples weren't the whole body.** The real response has `instance`, and the test compared only four fields. `ProblemExamples` now sets `instance`, and `documentedExampleIsTheRealResponse` compares all five fields; it failed before the fix (checked).
4. **The `ProblemDetail` schema documented a `properties` map that never appears on the wire**, because Jackson flattens it into top-level fields. `ProblemExamples` removes it, and `problemSchemaHasOnlyWireFields` failed before the fix (checked).
5. **The proposal still said the examples come from `config.OpenApiConfig`**, contradicting the design and STANDARDS §4. The proposal and STANDARDS §4 now name `ProblemExamples` for the examples.

Code review, `/code-review` (standards axis):
6. **The spec cited no BR ids** (openspec/config.yaml). It now cites BR-1 and BR-2 for operand-out-of-range and division-by-zero.
7. **Task 4.1 stated a false layer rule** ("`config` may use web"). Corrected.
8. **STANDARDS §2 said `config ──► all`, but the ArchUnit rule built in item 1 forbids `config → adapter.in.web`.** STANDARDS §2 now describes the rule as built, with a change-log row.
9. **A second `@SpringBootTest` that STANDARDS §6 didn't mention.** STANDARDS §6 now has an `ApiDocumentationTest` row saying why: springdoc isn't in the web slice.
10. **Needless visibility and repetition.** `ProblemExamples.all()` became a private `examples()`, and the repeated `a`/`b` assertion became a loop. Both are refactors, covered by the existing tests.

Left as is: the message chains through springdoc's model (that's its shape); parsing the example key from the type URI; fetching `/v3/api-docs` once per assertion (cheap; it's cached).

## Surprises

- **ArchUnit rejected the planned design** (`config.OpenApiConfig` calling a public `ProblemExamples`). Cause: item 1's layer rule forbids any layer accessing `Web`, which is stricter than STANDARDS §2. The planning step read §2, not the rule. Fixed by keeping the rule (CLAUDE.md: never weaken an ArchUnit rule) and moving the examples customizer into `adapter.in.web`. STANDARDS §2 was corrected after review.
- **The planned `springdoc.override-with-generic-response: false` wasn't needed.** Springdoc 3.2.0 added no responses from the advice to this operation. Cause: the plan assumed springdoc 1.x/2.x behaviour without checking. The setting was dropped, and a test pins the exact set of statuses.
- **Test-writing slips:** two `ApiDocumentationTest` compile errors (`assertThat` wrapped around `bodyJson()`, and `bodyJson()` returning `AbstractJsonContentAssert<?>`). A JsonPath filter (`[?()]`) on the `examples` object found nothing, because filters apply to arrays. Cause: unfamiliarity with Spring 7's AssertJ MockMvc API and JsonPath's map semantics.
- **Two edits silently didn't apply.** Two scripted text edits stopped on an exact-match assert after `spotlessApply` had reformatted the target lines. They were re-done with line-level substitutions.

## Shortcuts taken

None.

## Lessons

- **Propose step:** check the ArchUnit rules in `src/test/.../architecture/`, not only STANDARDS §2, before placing a class in a layer; the two had drifted since item 1.
- **Propose step:** verify a library setting changes anything (one quick run of the default output) before planning it.
- **Spec writing:** when a requirement says "the body the API returns", the test must compare the whole body, not a chosen subset of fields.
- **Apply habit:** after `spotlessApply`, re-read a file before a scripted exact-match edit, or edit with the Edit tool against the current text.
