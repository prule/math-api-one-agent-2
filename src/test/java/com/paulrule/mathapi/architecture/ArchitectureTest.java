package com.paulrule.mathapi.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** STANDARDS.md §2 dependency rules. */
@AnalyzeClasses(packages = "com.paulrule.mathapi", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String DOMAIN = "com.paulrule.mathapi.domain..";
    private static final String APPLICATION = "com.paulrule.mathapi.application..";
    private static final String WEB = "com.paulrule.mathapi.adapter.in.web..";
    private static final String CONFIG = "com.paulrule.mathapi.config..";

    // shortcut: optional layers and allowEmptyShould let the rules pass while layers are empty;
    // remove both once every layer has a class (backlog item 2).
    @ArchTest
    static final ArchRule LAYERS_DEPEND_INWARDS = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .withOptionalLayers(true)
            .layer("Domain")
            .definedBy(DOMAIN)
            .layer("Application")
            .definedBy(APPLICATION)
            .layer("Web")
            .definedBy(WEB)
            .layer("Config")
            .definedBy(CONFIG)
            .whereLayer("Web")
            .mayNotBeAccessedByAnyLayer()
            .whereLayer("Config")
            .mayNotBeAccessedByAnyLayer()
            .whereLayer("Application")
            .mayOnlyBeAccessedByLayers("Web", "Config")
            .whereLayer("Domain")
            .mayOnlyBeAccessedByLayers("Application", "Web", "Config");

    @ArchTest
    static final ArchRule DOMAIN_IS_FRAMEWORK_FREE = noClasses()
            .that()
            .resideInAPackage(DOMAIN)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta..", "com.fasterxml..", "tools.jackson..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule APPLICATION_HAS_NO_WEB_OR_ADAPTERS = noClasses()
            .that()
            .resideInAPackage(APPLICATION)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework.web..", "com.paulrule.mathapi.adapter..")
            .allowEmptyShould(true);
}
