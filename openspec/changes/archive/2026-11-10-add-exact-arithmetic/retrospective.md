# Retrospective: add-exact-arithmetic

## Caught by review

Plan review (ponytail, on the planning artifacts):
1. **Short inputs could escape Problem Details.** The design skipped a 500 handler because "no code path throws an unexpected exception", but `1e-2000000000 × 1e-2000000000` throws `ArithmeticException` (scale underflow). Fixed by adding an `@ExceptionHandler(Exception)` that returns 500 Problem Details with a fixed detail, plus the spec scenario "Unexpected failure", which `reportsAnUnexpectedFailureWithoutInternals` tests. The CPU and memory risk of unbounded Operands was recorded as open until item 4.
2. **`PerformCalculationTest` would only repeat other tests.** Dropped from the plan; the web-slice test runs the real use case.

Code review, ponytail:
3. **The no-limits risk had no `shortcut:` marker in the code.** Added at `Calculation.result()`, naming item 4 (BR-1) as the trigger. It's a comment, so there's no test.
4. **`CalculationTest` had a test that only exercised `BigDecimal.toPlainString`.** Deleted; the web test covers plain notation end to end.

Code review, `/code-review` (spec axis):
5. **Framework errors 404 and 406 had no test.** Added `rejectsAnUnknownPath` and `rejectsAnUnacceptableResponseType`. Both assert `type` `about:blank`, so they fail without the `createResponseEntity` override.
6. **design.md didn't record the `about:blank` override.** Recorded, with the reason: Spring 7 leaves `type` unset, and `@ExceptionHandler` return values bypass the hook.
7. **The resource risk was understated.** Echoing one Operand such as `1e999999999` already builds a billion-character string, and an `OutOfMemoryError` bypasses the 500 handler. design.md Risks now names both.
8. **What happens to a numeric `operation` was unspecified.** Recorded in design.md: `123` becomes the name `"123"`, so the client gets unknown-operation. This was observed during apply.

Code review, `/code-review` (standards axis):
9. **A new client-visible 500 wasn't in the PRD** (STANDARDS §4, CLAUDE.md keep-docs-current). PRD §3.2 now says errors outside its table, framework errors and `500` included, use `type` `about:blank`, and the PRD change log has a row for it. It's a doc, so there's no test.
10. **Test names didn't cite BR-3 and BR-4** (STANDARDS §3 SHOULD). Renamed `addsExactlyPerBR3` and its siblings, and `returnsTheExactResultNormalisedPerBR4`.
11. **Javadoc only restated what a class is** (STANDARDS §5). Removed from `Calculation` and `PerformCalculation`.

Judgement calls left as they are: the two identical override bodies in `ProblemDetailsAdvice` (the framework's override points force them); `UnknownOperationException` building the names from `Operation.values()` (extract `Operation.names()` when item 5 needs the list too); the one-line use case (STANDARDS §2 requires it).

## Surprises

- **`type` was missing from every framework-error body.** The first run of the 405, 415 and 500 tests failed because the JSON had no `type`. Cause: Spring Framework 7's `ProblemDetail.type` defaults to null, not `about:blank`, so Jackson leaves it out. Fixed in `ProblemDetailsAdvice.createResponseEntity` and the 500 handler.
- **One planned invalid-request case was wrong.** The case `"operation": 1` was written into `CalculationControllerTest`, but Jackson turns a number into a String field's text, so the request reaches the Operation lookup. Cause: Jackson's default scalar coercion. The case was removed from the test before the run, since the spec only requires invalid-request for non-numeric Operands, and the behaviour is recorded in design.md.

## Shortcuts taken

- `src/main/java/com/paulrule/mathapi/domain/Calculation.java:15`: no operand limits. Limit: huge exponents exhaust CPU and memory, in the arithmetic and in echoing an Operand. Upgrade: item 4 (BR-1). The service must not be deployed before then.

Item 1's shortcut (ArchUnit optional layers and `allowEmptyShould`) was removed in this change, as planned.

## Lessons

- **Propose step:** before writing "no code path throws", try the extremes of every input type. Here, `BigDecimal` scale near `Integer.MIN_VALUE` and `MAX_VALUE` breaks it. The plan review caught it, but propose should have.
- **Propose step:** when the PRD says every error has a field, check the framework's default for it (Spring 7: no `type`) and plan the override, instead of finding it as a failing test.
- **Test-writing habit:** before adding an invalid-input case, check that the spec calls that input invalid; `"operation": 1` was a guess, not spec.
- **Code review:** framework-error coverage should list every status the base handler maps (404, 405, 406, 415), not just the two the spec names.
