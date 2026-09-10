package io.github.sipratama.penatika.bootstrap.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Throwables.getStackTrace;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.github.sipratama.penatika.bootstrap.persistence.PenatikaPersistenceConfiguration;

class PenatikaConfigurationTests {

    private static final String UNUSED_JDBC_URL = "jdbc:postgresql://127.0.0.1:1/unused";
    private static final String CREDENTIAL_MARKER = "configuration-test-marker";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PenatikaConfiguration.class, PenatikaPersistenceConfiguration.class);

    @Test
    void persistenceDisabledStartsWithoutDatabaseConfiguration() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(PenatikaProperties.class).getEnvironment())
                    .isEqualTo(PenatikaEnvironment.LOCAL);
            assertThat(context.getBeansOfType(DataSource.class)).isEmpty();
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOCAL", "PILOT", "PROD"})
    void supportedEnvironmentValuesBind(String environment) {
        contextRunner
                .withPropertyValues("penatika.environment=" + environment)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(PenatikaProperties.class).getEnvironment().name())
                            .isEqualTo(environment);
                });
    }

    @Test
    void unknownEnvironmentIsRejected() {
        contextRunner
                .withPropertyValues("penatika.environment=UNKNOWN")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("penatika.environment")
                            .hasStackTraceContaining("UNKNOWN");
                });
    }

    @Test
    void persistenceEnabledWithoutJdbcUrlFailsClearly() {
        assertMissingProperty(
                "penatika.persistence.jdbc-url",
                "penatika.persistence.username=ignored",
                "penatika.persistence.password=" + CREDENTIAL_MARKER);
    }

    @Test
    void persistenceEnabledWithoutUsernameFailsClearly() {
        assertMissingProperty(
                "penatika.persistence.username",
                "penatika.persistence.jdbc-url=" + UNUSED_JDBC_URL,
                "penatika.persistence.password=" + CREDENTIAL_MARKER);
    }

    @Test
    void persistenceEnabledWithoutPasswordFailsClearly() {
        assertMissingProperty(
                "penatika.persistence.password",
                "penatika.persistence.jdbc-url=" + UNUSED_JDBC_URL,
                "penatika.persistence.username=ignored");
    }

    @Test
    void persistencePropertiesDoNotExposePasswordThroughToString() {
        contextRunner
                .withPropertyValues("penatika.persistence.password=marker-value")
                .run(context -> assertThat(context.getBean(PenatikaProperties.class)
                                .getPersistence()
                                .toString())
                        .doesNotContain("marker-value"));
    }

    private void assertMissingProperty(String missingProperty, String... completeProperties) {
        String[] properties = new String[completeProperties.length + 1];
        properties[0] = "penatika.persistence.enabled=true";
        System.arraycopy(completeProperties, 0, properties, 1, completeProperties.length);

        contextRunner.withPropertyValues(properties).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasStackTraceContaining(missingProperty);
            assertThat(getStackTrace(context.getStartupFailure())).doesNotContain(CREDENTIAL_MARKER);
        });
    }
}
