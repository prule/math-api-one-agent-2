package com.paulrule.mathapi.domain.fixture;

import org.springframework.context.ApplicationContext;

/** Breaks the domain rule on purpose so {@code ArchitectureRulesAreLiveTest} can prove the rule fires. */
public class UsesSpring {
    // A plain field, not a stereotype annotation, so component scanning never turns this into a bean.
    ApplicationContext context;
}
