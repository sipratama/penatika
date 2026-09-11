package io.github.sipratama.penatika.identity;

import java.security.SecureRandom;
import java.time.Clock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.sipratama.penatika.identity.adapter.out.security.SecureRandomSecurityTokenGenerator;
import io.github.sipratama.penatika.identity.adapter.out.security.Sha256SecurityTokenVerifier;
import io.github.sipratama.penatika.identity.application.ParticipantSessionApplicationService;
import io.github.sipratama.penatika.identity.application.TeacherBrowserSessionApplicationService;
import io.github.sipratama.penatika.identity.application.port.out.ParticipantSessionPersistencePort;
import io.github.sipratama.penatika.identity.application.port.out.SecurityTokenGeneratorPort;
import io.github.sipratama.penatika.identity.application.port.out.SecurityTokenVerifierPort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherBrowserSessionPersistencePort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherIdentityPersistencePort;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public class IdentityRuntimeConfiguration {

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    Clock penatikaClock() {
        return Clock.systemUTC();
    }

    @Bean
    SecurityTokenGeneratorPort securityTokenGenerator() {
        return new SecureRandomSecurityTokenGenerator(new SecureRandom());
    }

    @Bean
    SecurityTokenVerifierPort securityTokenVerifier() {
        return new Sha256SecurityTokenVerifier();
    }

    @Bean
    TeacherBrowserSessionApplicationService teacherBrowserSessionApplicationService(
            TeacherIdentityPersistencePort teacherIdentities,
            TeacherBrowserSessionPersistencePort browserSessions,
            SecurityTokenGeneratorPort tokenGenerator,
            SecurityTokenVerifierPort tokenVerifier,
            Clock clock) {
        return new TeacherBrowserSessionApplicationService(
                teacherIdentities, browserSessions, tokenGenerator, tokenVerifier, clock);
    }

    @Bean
    ParticipantSessionApplicationService participantSessionApplicationService(
            ParticipantSessionPersistencePort participantSessions,
            TeacherIdentityPersistencePort teacherIdentities,
            TeacherBrowserSessionPersistencePort browserSessions,
            SecurityTokenGeneratorPort tokenGenerator,
            SecurityTokenVerifierPort tokenVerifier,
            Clock clock) {
        return new ParticipantSessionApplicationService(
                participantSessions,
                teacherIdentities,
                browserSessions,
                tokenGenerator,
                tokenVerifier,
                clock);
    }

}
