package io.github.sipratama.penatika.lesson;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.sipratama.penatika.lesson.application.ClassroomStartLessonVersionApplicationService;
import io.github.sipratama.penatika.lesson.application.ClassroomLessonNavigationApplicationService;
import io.github.sipratama.penatika.lesson.application.ClassroomLessonSceneApplicationService;
import io.github.sipratama.penatika.lesson.application.port.out.LessonVersionPersistencePort;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public class LessonRuntimeConfiguration {

    @Bean
    ClassroomStartLessonVersionApplicationService classroomStartLessonVersionApplicationService(
            LessonVersionPersistencePort lessonVersions) {
        return new ClassroomStartLessonVersionApplicationService(lessonVersions);
    }

    @Bean
    ClassroomLessonNavigationApplicationService classroomLessonNavigationApplicationService(
            LessonVersionPersistencePort lessonVersions) {
        return new ClassroomLessonNavigationApplicationService(lessonVersions);
    }

    @Bean
    ClassroomLessonSceneApplicationService classroomLessonSceneApplicationService(
            LessonVersionPersistencePort lessonVersions) {
        return new ClassroomLessonSceneApplicationService(lessonVersions);
    }
}
