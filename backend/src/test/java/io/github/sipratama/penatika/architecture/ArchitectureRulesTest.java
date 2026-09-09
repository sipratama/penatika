package io.github.sipratama.penatika.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import io.github.sipratama.penatika.architecture.fixtures.adapter.InvalidAdapterDependency;
import io.github.sipratama.penatika.architecture.fixtures.application.InvalidApplicationDependency;
import io.github.sipratama.penatika.architecture.fixtures.application.InvalidPersistenceDependency;
import io.github.sipratama.penatika.architecture.fixtures.classroom.application.InvalidCrossModuleDependency;
import io.github.sipratama.penatika.architecture.fixtures.controller.GlobalController;
import io.github.sipratama.penatika.architecture.fixtures.domain.InvalidDomainDependency;
import io.github.sipratama.penatika.architecture.fixtures.identity.domain.InternalIdentityType;
import io.github.sipratama.penatika.architecture.fixtures.persistence.GlobalPersistence;
import org.junit.jupiter.api.Test;

class ArchitectureRulesTest {

    private static final String PRODUCTION_ROOT = "io.github.sipratama.penatika";
    private static final String FIXTURE_ROOT = PRODUCTION_ROOT + ".architecture.fixtures";

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(PRODUCTION_ROOT);

    @Test
    void domainRemainsFrameworkLightAndInward() {
        domainRule(PRODUCTION_ROOT).check(PRODUCTION_CLASSES);
    }

    @Test
    void applicationDoesNotDependOnAdaptersBootstrapOrSpring() {
        applicationRule(PRODUCTION_ROOT).check(PRODUCTION_CLASSES);
    }

    @Test
    void businessCoreDoesNotDependOnPersistenceTechnology() {
        persistenceTechnologyRule(PRODUCTION_ROOT).check(PRODUCTION_CLASSES);
    }

    @Test
    void businessModuleInternalsRemainPrivate() {
        moduleInternalRules(PRODUCTION_ROOT).forEach(rule -> rule.check(PRODUCTION_CLASSES));
    }

    @Test
    void globalTechnicalLayerPackagesAreRejected() {
        globalTechnicalLayerRule(PRODUCTION_ROOT).check(PRODUCTION_CLASSES);
    }

    @Test
    void deliberatelyInvalidTestFixturesProveEveryRuleDetectsViolations() {
        JavaClasses fixtures = new ClassFileImporter().importClasses(
                InvalidDomainDependency.class,
                InvalidApplicationDependency.class,
                InvalidPersistenceDependency.class,
                InvalidAdapterDependency.class,
                InvalidCrossModuleDependency.class,
                InternalIdentityType.class,
                GlobalController.class,
                GlobalPersistence.class);

        assertThat(domainRule(FIXTURE_ROOT).evaluate(fixtures).hasViolation()).isTrue();
        assertThat(applicationRule(FIXTURE_ROOT).evaluate(fixtures).hasViolation()).isTrue();
        assertThat(persistenceTechnologyRule(FIXTURE_ROOT).evaluate(fixtures).hasViolation()).isTrue();
        assertThat(moduleInternalRules(FIXTURE_ROOT))
                .anySatisfy(rule -> assertThat(rule.evaluate(fixtures).hasViolation()).isTrue());
        assertThat(globalTechnicalLayerRule(FIXTURE_ROOT).evaluate(fixtures).hasViolation()).isTrue();
    }

    private static ArchRule domainRule(String rootPackage) {
        return noClasses()
                .that().resideInAPackage(rootPackage + "..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        rootPackage + "..application..",
                        rootPackage + "..adapter..",
                        rootPackage + "..bootstrap..")
                .allowEmptyShould(true);
    }

    private static ArchRule applicationRule(String rootPackage) {
        return noClasses()
                .that().resideInAPackage(rootPackage + "..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        rootPackage + "..adapter..",
                        rootPackage + "..bootstrap..")
                .allowEmptyShould(true);
    }

    private static ArchRule persistenceTechnologyRule(String rootPackage) {
        return noClasses()
                .that().resideInAnyPackage(
                        rootPackage + "..domain..",
                        rootPackage + "..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "java.sql..",
                        "javax.sql..",
                        "org.springframework.jdbc..",
                        "org.flywaydb..",
                        "org.postgresql..",
                        "com.zaxxer.hikari..")
                .allowEmptyShould(true);
    }

    private static java.util.List<ArchRule> moduleInternalRules(String rootPackage) {
        return java.util.List.of("identity", "lesson", "classroom").stream()
                .map(module -> noClasses()
                        .that().resideOutsideOfPackage(rootPackage + "." + module + "..")
                        .should().dependOnClassesThat().resideInAnyPackage(
                                rootPackage + "." + module + ".domain..",
                                rootPackage + "." + module + ".adapter..")
                        .allowEmptyShould(true))
                .toList();
    }

    private static ArchRule globalTechnicalLayerRule(String rootPackage) {
        return noClasses()
                .should().resideInAnyPackage(
                        rootPackage + ".controller..",
                        rootPackage + ".service..",
                        rootPackage + ".repository..",
                        rootPackage + ".entity..",
                        rootPackage + ".common..",
                        rootPackage + ".persistence..")
                .allowEmptyShould(true);
    }
}
