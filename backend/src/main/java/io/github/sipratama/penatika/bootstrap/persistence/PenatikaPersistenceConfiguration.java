package io.github.sipratama.penatika.bootstrap.persistence;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.sipratama.penatika.bootstrap.configuration.PenatikaPersistenceProperties;
import io.github.sipratama.penatika.bootstrap.configuration.PenatikaProperties;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public class PenatikaPersistenceConfiguration {

    @Bean
    DataSource penatikaDataSource(PenatikaProperties properties) {
        PenatikaPersistenceProperties persistence = properties.getPersistence();
        persistence.validateForActivation();

        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(persistence.getJdbcUrl())
                .username(persistence.getUsername())
                .password(persistence.getPassword())
                .build();
    }

    @Bean(initMethod = "migrate")
    @ConditionalOnProperty(
            prefix = "penatika.persistence",
            name = "migrations-enabled",
            havingValue = "true")
    Flyway penatikaFlyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
    }
}
