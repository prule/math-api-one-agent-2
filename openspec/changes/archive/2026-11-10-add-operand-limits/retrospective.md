# Retrospective: add-operand-limits

## Caught by review

Plan review (ponytail, on the planning artifacts):
1. **The invalid-request detail didn't mention the new limits**, so a client rejected for size was told to send JSON with `a` and `b`. Fixed by adding the 16,384 and 1,000 limits to the one `INVALID_REQUEST_DETAIL` constant. `rejectsAnOversizedBodyStatingTheLimits` asserts the text.

Code review, ponytail, and `/code-review` spec axis (both found it independently):
2. **The BR-1 scale check let `Integer.MIN_VALUE` through.** The code used `Math.abs(operand.scale()) > 1000`, and `Math.abs(Integer.MIN_VALUE)` is negative, so `new BigDecimal(BigInteger.ONE, Integer.MIN_VALUE)` (also `new BigDecimal("1E+2147483648")`) passed BR-1. Echoing or adding it would have exhausted memory, which is exactly what this change exists to stop. Fixed with the two comparisons design.md had specified. `rejectsTheMostNegativeScalePerBR1` failed before the fix (checked) and passes after. The spec reviewer found Jackson 3.1.7 rejects every such string while parsing, so it was a hole in the domain rule rather than an attack reachable over HTTP. The domain must still hold for every caller.

Code review, `/code-review` (standards axis):
3. **The limits were written twice**, as `Calculation` constants and as literals in the exception message. The message now uses `Calculation.MAX_PRECISION` and `MAX_SCALE`; the existing message assertions cover it.
4. **Mysterious name:** `OperandOutOfRangeException(String operand)` held the name, not the Operand. Renamed to `operandName`.
5. **Duplicated padding logic** in two body-size tests. Extracted `validBodyPaddedTo(int)`, which also asserts its own length.

Left as is:
- **The 500 test no longer goes over HTTP**, so it doesn't check `application/problem+json` (spec axis). No HTTP input can reach it now; design.md accepts this.
- **`INVALID_REQUEST_DETAIL` repeats the values in `application.yaml`**, which Java and YAML can't share cheaply. The detail test pins the text.

## Surprises

- **BR-1 made the 500 path unreachable over HTTP.** Item 2's test reached it with `1e-2000000000 × 1e-2000000000`, which BR-1 now rejects as 400. The test broke as soon as the handler was added (task 2.2). Cause: design.md noted the handler was "probably unreachable" but planned nothing for its test or its spec scenario. Fixed by calling `ProblemDetailsAdvice.unexpected` directly and MODIFYing the "Unexpected failure" scenario to describe any unmapped exception.
- **I wrote `Math.abs` instead of the design's two comparisons.** That introduced finding 2. Cause: I rewrote the condition to be shorter while implementing, without checking the `int` edge.
- **Test data in annotations.** `@ValueSource` with `"9".repeat(100)` failed to compile, because annotation values must be compile-time constants. I switched to `@MethodSource`.
- **Fewer red request-limit cases than predicted.** Tasks.md said four would fail before the config; only two did (oversized body, long `operation`). Cause: Jackson 3's default 1,000-character number limit also applies when it turns a JSON string into a `BigDecimal`, so both 1,001-digit Operand forms were already rejected.
- **The timing test failed by status, not by timeout.** With BR-1 disabled (task 2.3), the huge-exponent request returned 500 well inside the second, because building the 2-billion-character Result failed fast. The test can fail, but its 1-second timeout has never fired.

## Shortcuts taken

None. This change removed the `Calculation.result()` shortcut carried since item 2, and the debt ledger is clean.

## Lessons

- **Apply step:** implement a planned condition exactly as written. "Simplifying" `x > M || x < -M` to `Math.abs(x) > M` is wrong for `int`, and two reviews were needed to catch it.
- **Test-writing habit:** every range check on an `int` gets a test at `Integer.MIN_VALUE` and `Integer.MAX_VALUE`, not only at the business limits.
- **Propose step:** when a change makes an error path unreachable, plan what happens to its test and spec scenario, instead of finding out from a failing test.
- **Propose step:** before predicting which tests start red, check the library's defaults (Jackson's number limit), so the tasks say the true red set.
