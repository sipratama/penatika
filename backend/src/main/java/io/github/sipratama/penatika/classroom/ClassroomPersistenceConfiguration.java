package io.github.sipratama.penatika.classroom;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

import io.github.sipratama.penatika.classroom.adapter.out.persistence.postgres.PostgresAcceptedCommandOutcomePersistenceAdapter;
import io.github.sipratama.penatika.classroom.adapter.out.persistence.postgres.PostgresClassroomSessionPersistenceAdapter;
import io.github.sipratama.penatika.classroom.adapter.out.persistence.postgres.PostgresPairingGrantPersistenceAdapter;
import io.github.sipratama.penatika.classroom.application.port.out.AcceptedCommandOutcomePersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.PairingGrantPersistencePort;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public class ClassroomPersistenceConfiguration {

    @Bean
    ClassroomSessionPersistencePort classroomSessionPersistencePort(JdbcClient jdbcClient) {
        return new PostgresClassroomSessionPersistenceAdapter(jdbcClient);
    }

    @Bean
    PairingGrantPersistencePort pairingGrantPersistencePort(JdbcClient jdbcClient) {
        return new PostgresPairingGrantPersistenceAdapter(jdbcClient);
    }

    @Bean
    AcceptedCommandOutcomePersistencePort acceptedCommandOutcomePersistencePort(JdbcClient jdbcClient) {
        return new PostgresAcceptedCommandOutcomePersistenceAdapter(jdbcClient);
    }
}
