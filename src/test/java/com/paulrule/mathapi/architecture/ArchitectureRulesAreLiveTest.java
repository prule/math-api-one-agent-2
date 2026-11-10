package com.paulrule.mathapi.architecture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.paulrule.mathapi.adapter.in.web.fixture.WebType;
import com.paulrule.mathapi.application.fixture.UsesWeb;
import com.paulrule.mathapi.domain.fixture.UsesSpring;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

// The layers are still empty, so ArchitectureTest would pass even with a broken rule.
class ArchitectureRulesAreLiveTest {

    @Test
    void domainRuleRejectsADomainClassThatUsesSpring() {
        assertRejects(ArchitectureTest.DOMAIN_IS_FRAMEWORK_FREE, UsesSpring.class, UsesSpring.class);
    }

    @Test
    void applicationRuleRejectsAnApplicationClassThatUsesSpringWeb() {
        assertRejects(ArchitectureTest.APPLICATION_HAS_NO_WEB_OR_ADAPTERS, UsesWeb.class, UsesWeb.class);
    }

    @Test
    void layerRuleRejectsApplicationDependingOnWeb() {
        assertRejects(ArchitectureTest.LAYERS_DEPEND_INWARDS, UsesWeb.class, UsesWeb.class, WebType.class);
    }

    private static void assertRejects(ArchRule rule, Class<?> violator, Class<?>... toImport) {
        JavaClasses classes = new ClassFileImporter().importClasses(toImport);

        assertThatThrownBy(() -> rule.check(classes))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(violator.getName());
    }
}
