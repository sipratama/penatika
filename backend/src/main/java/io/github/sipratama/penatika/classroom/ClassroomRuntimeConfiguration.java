package io.github.sipratama.penatika.classroom;

import java.time.Clock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.sipratama.penatika.classroom.application.StartClassroomSessionApplicationService;
import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public class ClassroomRuntimeConfiguration {

    @Bean
    StartClassroomSessionApplicationService startClassroomSessionApplicationService(
            ClassroomSessionPersistencePort classroomSessions,
            Clock clock) {
        return new StartClassroomSessionApplicationService(classroomSessions, clock);
    }
}
