package io.github.sipratama.penatika.bootstrap.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import io.github.sipratama.penatika.bootstrap.configuration.PenatikaProperties;

@SpringBootTest
@Testcontainers
class PostgreSqlPersistenceSmokeIT {

    private static final DockerImageName POSTGRES_IMAGE =
            DockerImageName.parse("postgres:18.6-bookworm");

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(POSTGRES_IMAGE);

    @DynamicPropertySource
    static void persistenceProperties(DynamicPropertyRegistry registry) {
        registry.add("penatika.persistence.enabled", () -> true);
        registry.add("penatika.persistence.jdbc-url", POSTGRES::getJdbcUrl);
        registry.add("penatika.persistence.username", POSTGRES::getUsername);
        registry.add("penatika.persistence.password", POSTGRES::getPassword);
        registry.add("penatika.persistence.migrations-enabled", () -> true);
    }

    @Autowired
    private PenatikaProperties properties;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private Flyway flyway;

    @Test
    void persistenceMechanismWorksAgainstPostgreSql18() throws Exception {
        assertThat(properties.getPersistence().isEnabled()).isTrue();
        assertThat(properties.getPersistence().getJdbcUrl()).isEqualTo(POSTGRES.getJdbcUrl());
        assertThat(properties.getPersistence().getUsername()).isEqualTo(POSTGRES.getUsername());
        assertThat(MessageDigest.isEqual(
                        properties.getPersistence().getPassword().getBytes(StandardCharsets.UTF_8),
                        POSTGRES.getPassword().getBytes(StandardCharsets.UTF_8)))
                .isTrue();

        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(2)).isTrue();
            assertThat(connection.getMetaData().getDatabaseMajorVersion()).isEqualTo(18);
        }

        assertThat(jdbcClient.sql("SELECT 1").query(Integer.class).single()).isEqualTo(1);
        assertThat(flyway.info().applied()).isEmpty();
        assertThatCode(flyway::validate).doesNotThrowAnyException();
        assertThatCode(flyway::migrate).doesNotThrowAnyException();
        assertThatCode(flyway::validate).doesNotThrowAnyException();
        assertThat(flyway.info().applied()).isEmpty();

        List<String> productTables = jdbcClient.sql("""
                        SELECT table_name
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name <> 'flyway_schema_history'
                        ORDER BY table_name
                        """)
                .query(String.class)
                .list();
        assertThat(productTables).isEmpty();
    }
}
