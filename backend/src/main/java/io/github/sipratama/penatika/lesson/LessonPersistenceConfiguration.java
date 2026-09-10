package io.github.sipratama.penatika.lesson;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

import io.github.sipratama.penatika.lesson.adapter.out.persistence.postgres.PostgresLessonVersionPersistenceAdapter;
import io.github.sipratama.penatika.lesson.application.port.out.LessonVersionPersistencePort;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public class LessonPersistenceConfiguration {

    @Bean
    LessonVersionPersistencePort lessonVersionPersistencePort(
            JdbcClient jdbcClient, TransactionTemplate transactionTemplate) {
        return new PostgresLessonVersionPersistenceAdapter(jdbcClient, transactionTemplate);
    }
}
