package io.github.sipratama.penatika.identity;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

import io.github.sipratama.penatika.identity.adapter.out.persistence.postgres.PostgresParticipantSessionPersistenceAdapter;
import io.github.sipratama.penatika.identity.adapter.out.persistence.postgres.PostgresTeacherBrowserSessionPersistenceAdapter;
import io.github.sipratama.penatika.identity.adapter.out.persistence.postgres.PostgresTeacherIdentityPersistenceAdapter;
import io.github.sipratama.penatika.identity.application.port.out.ParticipantSessionPersistencePort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherBrowserSessionPersistencePort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherIdentityPersistencePort;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public class IdentityPersistenceConfiguration {

    @Bean
    TeacherIdentityPersistencePort teacherIdentityPersistencePort(
            JdbcClient jdbcClient, TransactionTemplate transactionTemplate) {
        return new PostgresTeacherIdentityPersistenceAdapter(jdbcClient, transactionTemplate);
    }

    @Bean
    TeacherBrowserSessionPersistencePort teacherBrowserSessionPersistencePort(JdbcClient jdbcClient) {
        return new PostgresTeacherBrowserSessionPersistenceAdapter(jdbcClient);
    }

    @Bean
    ParticipantSessionPersistencePort participantSessionPersistencePort(JdbcClient jdbcClient) {
        return new PostgresParticipantSessionPersistenceAdapter(jdbcClient);
    }
}
