# Retrospective: add-divide

## Caught by review

Plan review (ponytail, on the planning artifacts):
1. **DIVIDE's edge cases were planned in both `OperationTest` and `CalculationTest`.** Fixed in tasks.md by following item 2's split: one exact `apply` case and the zero cases in `OperationTest`, and every rounding and normalisation case only in `CalculationTest`'s table.
2. **The modified spec scenario listed every Operation name**, so items 6 and 7 would each need a spec delta just to extend the list. Reworded to "a `detail` naming every supported Operation".

Code review, ponytail:
3. **`listsDivideAmongTheSupportedOperations` only checked the test's own constant**, never the message. Deleted. Replaced by finding 5's parsing test.

Code review, `/code-review` (spec axis):
4. **The HTTP rounding scenarios were tested only in the domain**, so 35-digit string Operands never went through `BigDecimal` binding. Added the `HALF_EVEN` tie (`…015 ÷ 10`) as a web-slice row.
5. **Nothing proved the real `detail` names DIVIDE** (also standards axis and ponytail). See finding 6.

Code review, `/code-review` (standards axis):
6. **Both tests copied the production join** (`Arrays.stream(...).collect(joining(", "))`), so a wrong separator or order would still pass. `OperationTest.listsEverySupportedOperationInDeclarationOrder` now parses the real message and compares its list with `Operation.values()`. Breaking the separator to `","` makes it fail (checked). The web test compares `detail` with the domain exception's own message.
7. **Duplicate `0 ÷ 0` tests.** Folded into the parameterised zero cases at both levels.

Left as is: the `CalculationTest` method rename to `performsPerBR3AndNormalisesPerBR4` (the reviewers agreed it fits STANDARDS §6). The spec reviewer's note that proposal.md said "Docs to update: none" while BACKLOG.md changed: the backlog status edit is part of the workflow, not a doc change.

## Surprises

None. Every test that was meant to fail first (compile errors in task 1.1; five 422 cases in task 2.1, which returned 500) did, and every test passed once the code was written. Production code needed exactly the three changes the backlog predicted.

## Shortcuts taken

None added. The existing `src/main/java/com/paulrule/mathapi/domain/Calculation.java:15` shortcut (no operand limits, item 4) still applies, now to DIVIDE as well. Extreme scales in `divide` reach the 500 handler.

## Lessons

- **Test-writing habit:** a test must never rebuild its expected value with the same expression the code uses. Parse or compare against the real output. Both test files had this in tasks.md, and only `/code-review` caught it.
- **Propose step:** scenarios should describe a set of values ("every supported Operation"), not list today's members, when the set grows with the backlog. The plan review caught it this time.
- **Tasks step:** a rule changes the most at the HTTP edge when Operands arrive as strings, so keep one web-level case for it even when the domain table is the home of the rounding cases.
