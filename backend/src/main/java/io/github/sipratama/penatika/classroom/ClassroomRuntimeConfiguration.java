package io.github.sipratama.penatika.classroom;

import java.security.SecureRandom;
import java.time.Clock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import io.github.sipratama.penatika.classroom.adapter.in.http.ParticipantSessionCookies;
import io.github.sipratama.penatika.classroom.adapter.out.security.SecureRandomPairingTokenGenerator;
import io.github.sipratama.penatika.classroom.adapter.out.security.Sha256PairingTokenVerifier;
import io.github.sipratama.penatika.classroom.adapter.out.synchronization.FailClosedDisplayMutationGate;
import io.github.sipratama.penatika.classroom.adapter.out.transaction.spring.TransactionalClassroomCommandUseCase;
import io.github.sipratama.penatika.classroom.adapter.out.transaction.spring.TransactionalPairingGrantRedemptionUseCase;
import io.github.sipratama.penatika.classroom.application.ClassroomCommandApplicationService;
import io.github.sipratama.penatika.classroom.application.ControllerAuthorityApplicationService;
import io.github.sipratama.penatika.classroom.application.ControllerReconciliationApplicationService;
import io.github.sipratama.penatika.classroom.application.PairingGrantApplicationService;
import io.github.sipratama.penatika.classroom.application.PairingGrantRedemptionApplicationService;
import io.github.sipratama.penatika.classroom.application.StartClassroomSessionApplicationService;
import io.github.sipratama.penatika.classroom.application.port.out.AcceptedCommandOutcomePersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.DisplayMutationGatePort;
import io.github.sipratama.penatika.classroom.application.port.out.PairingGrantPersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.PairingTokenGeneratorPort;
import io.github.sipratama.penatika.classroom.application.port.out.PairingTokenVerifierPort;
import io.github.sipratama.penatika.identity.application.port.in.ParticipantSessionAuthorityUseCase;
import io.github.sipratama.penatika.identity.application.port.in.RecordTeacherSessionActivityUseCase;
import io.github.sipratama.penatika.lesson.application.port.in.ResolveNextLessonSceneUseCase;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public class ClassroomRuntimeConfiguration {

    @Bean
    StartClassroomSessionApplicationService startClassroomSessionApplicationService(
            ClassroomSessionPersistencePort classroomSessions,
            Clock clock) {
        return new StartClassroomSessionApplicationService(classroomSessions, clock);
    }

    @Bean
    PairingTokenGeneratorPort pairingTokenGenerator() {
        return new SecureRandomPairingTokenGenerator(new SecureRandom());
    }

    @Bean
    PairingTokenVerifierPort pairingTokenVerifier() {
        return new Sha256PairingTokenVerifier();
    }

    @Bean
    ParticipantSessionCookies participantSessionCookies() {
        return new ParticipantSessionCookies();
    }

    @Bean
    PairingGrantApplicationService pairingGrantApplicationService(
            ClassroomSessionPersistencePort classroomSessions,
            PairingGrantPersistencePort pairingGrants,
            ParticipantSessionAuthorityUseCase participantSessions,
            PairingTokenGeneratorPort tokenGenerator,
            PairingTokenVerifierPort tokenVerifier,
            Clock clock) {
        return new PairingGrantApplicationService(
                classroomSessions,
                pairingGrants,
                participantSessions,
                tokenGenerator,
                tokenVerifier,
                clock);
    }

    @Bean
    PairingGrantRedemptionApplicationService pairingGrantRedemptionApplicationService(
            PairingGrantPersistencePort pairingGrants,
            ClassroomSessionPersistencePort classroomSessions,
            ParticipantSessionAuthorityUseCase participantSessions,
            PairingTokenVerifierPort tokenVerifier,
            Clock clock) {
        return new PairingGrantRedemptionApplicationService(
                pairingGrants, classroomSessions, participantSessions, tokenVerifier, clock);
    }

    @Bean
    TransactionalPairingGrantRedemptionUseCase transactionalPairingGrantRedemptionUseCase(
            PairingGrantRedemptionApplicationService delegate,
            TransactionTemplate transactionTemplate) {
        return new TransactionalPairingGrantRedemptionUseCase(delegate, transactionTemplate);
    }

    @Bean
    DisplayMutationGatePort displayMutationGate() {
        return new FailClosedDisplayMutationGate();
    }

    @Bean
    ControllerAuthorityApplicationService controllerAuthorityApplicationService(
            ParticipantSessionAuthorityUseCase participantSessions) {
        return new ControllerAuthorityApplicationService(participantSessions);
    }

    @Bean
    ControllerReconciliationApplicationService controllerReconciliationApplicationService(
            ClassroomSessionPersistencePort classroomSessions,
            ControllerAuthorityApplicationService controllerAuthority,
            RecordTeacherSessionActivityUseCase teacherActivity) {
        return new ControllerReconciliationApplicationService(
                classroomSessions, controllerAuthority, teacherActivity);
    }

    @Bean
    ClassroomCommandApplicationService classroomCommandApplicationService(
            ClassroomSessionPersistencePort classroomSessions,
            AcceptedCommandOutcomePersistencePort acceptedCommands,
            DisplayMutationGatePort displayMutationGate,
            ResolveNextLessonSceneUseCase lessonNavigation,
            ControllerAuthorityApplicationService controllerAuthority,
            Clock clock) {
        return new ClassroomCommandApplicationService(
                classroomSessions,
                acceptedCommands,
                displayMutationGate,
                lessonNavigation,
                controllerAuthority,
                clock);
    }

    @Bean
    TransactionalClassroomCommandUseCase transactionalClassroomCommandUseCase(
            ClassroomCommandApplicationService delegate,
            TransactionTemplate transactionTemplate) {
        return new TransactionalClassroomCommandUseCase(delegate, transactionTemplate);
    }
}
