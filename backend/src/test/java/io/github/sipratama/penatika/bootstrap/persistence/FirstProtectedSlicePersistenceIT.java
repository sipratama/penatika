package io.github.sipratama.penatika.bootstrap.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import io.github.sipratama.penatika.classroom.application.port.out.AcceptedCommandOutcomePersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.PairingGrantPersistencePort;
import io.github.sipratama.penatika.classroom.domain.AcceptedCommandOutcome;
import io.github.sipratama.penatika.classroom.domain.ClassroomLifecycleState;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.PairingGrant;
import io.github.sipratama.penatika.classroom.domain.Revision;
import io.github.sipratama.penatika.classroom.fixtures.AcceptedCommandOutcomeFixtureBuilder;
import io.github.sipratama.penatika.classroom.fixtures.ClassroomSessionFixtureBuilder;
import io.github.sipratama.penatika.classroom.fixtures.PairingGrantFixtureBuilder;
import io.github.sipratama.penatika.identity.application.port.out.ParticipantSessionPersistencePort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherBrowserSessionPersistencePort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherIdentityPersistencePort;
import io.github.sipratama.penatika.identity.domain.ExternalIdentityLink;
import io.github.sipratama.penatika.identity.domain.ParticipantSession;
import io.github.sipratama.penatika.identity.domain.TeacherAccount;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSession;
import io.github.sipratama.penatika.identity.fixtures.ExternalIdentityLinkFixtureBuilder;
import io.github.sipratama.penatika.identity.fixtures.ParticipantSessionFixtureBuilder;
import io.github.sipratama.penatika.identity.fixtures.TeacherAccountFixtureBuilder;
import io.github.sipratama.penatika.identity.fixtures.TeacherBrowserSessionFixtureBuilder;
import io.github.sipratama.penatika.lesson.application.port.out.LessonVersionPersistencePort;
import io.github.sipratama.penatika.lesson.domain.LessonVersion;
import io.github.sipratama.penatika.lesson.fixtures.LessonVersionFixtureBuilder;

@SpringBootTest
@Testcontainers
class FirstProtectedSlicePersistenceIT {

    @Container
    private static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(DockerImageName.parse("postgres:18.6-bookworm"));

    @DynamicPropertySource
    static void persistenceProperties(DynamicPropertyRegistry registry) {
        registry.add("penatika.persistence.enabled", () -> true);
        registry.add("penatika.persistence.jdbc-url", POSTGRES::getJdbcUrl);
        registry.add("penatika.persistence.username", POSTGRES::getUsername);
        registry.add("penatika.persistence.password", POSTGRES::getPassword);
        registry.add("penatika.persistence.migrations-enabled", () -> true);
    }

    @Autowired private JdbcClient jdbcClient;
    @Autowired private TeacherIdentityPersistencePort teacherIdentities;
    @Autowired private TeacherBrowserSessionPersistencePort browserSessions;
    @Autowired private ParticipantSessionPersistencePort participantSessions;
    @Autowired private LessonVersionPersistencePort lessonVersions;
    @Autowired private ClassroomSessionPersistencePort classroomSessions;
    @Autowired private PairingGrantPersistencePort pairingGrants;
    @Autowired private AcceptedCommandOutcomePersistencePort commandOutcomes;

    @BeforeEach
    void cleanProductTables() {
        jdbcClient.sql("""
                        TRUNCATE TABLE
                            classroom_accepted_command,
                            identity_participant_session,
                            classroom_pairing_grant,
                            classroom_session,
                            lesson_scene_block,
                            lesson_lesson_scene,
                            lesson_lesson_version,
                            lesson_lesson,
                            identity_teacher_browser_session,
                            identity_external_identity_link,
                            identity_teacher_account
                        CASCADE
                        """)
                .update();
    }

    @Test
    void representativeAdaptersRoundTripOrderedFirstSliceState() {
        Foundation foundation = createFoundation();

        assertThat(teacherIdentities.findTeacherAccount(foundation.teacher().id()))
                .contains(foundation.teacher());
        assertThat(teacherIdentities.findExternalIdentity(
                        foundation.identityLink().issuer(), foundation.identityLink().subject()))
                .contains(foundation.identityLink());
        assertThat(browserSessions.findByCredentialVerifier(foundation.browserSession().credentialVerifier()))
                .contains(foundation.browserSession());

        LessonVersion loadedVersion = lessonVersions.findClassroomReadyForTeacher(
                        foundation.lessonVersion().id(), foundation.teacher().id().value())
                .orElseThrow();
        assertThat(loadedVersion.scenes()).extracting(scene -> scene.position()).containsExactly(0L, 1L);
        assertThat(loadedVersion.scenes().getFirst().blocks())
                .extracting(block -> block.plainText())
                .containsExactly("Deterministic first classroom scene");

        assertThat(classroomSessions.findById(foundation.classroomSession().id()))
                .contains(foundation.classroomSession());

        PairingGrant grant = new PairingGrantFixtureBuilder()
                .withClassroomSessionId(foundation.classroomSession().id().value())
                .build();
        pairingGrants.create(grant);
        assertThat(pairingGrants.findByCredentialVerifier(grant.credentialVerifier())).contains(grant);

        ParticipantSession display = new ParticipantSessionFixtureBuilder()
                .withClassroomSessionId(foundation.classroomSession().id().value())
                .build();
        assertThat(participantSessions.tryCreateActive(display)).isTrue();
        assertThat(participantSessions.findByCredentialVerifier(display.credentialVerifier())).contains(display);
    }

    @Test
    void databaseRejectsDuplicateExternalIdentityCredentialAndInvalidForeignKey() {
        Foundation foundation = createFoundation();

        TeacherAccount secondTeacher = new TeacherAccountFixtureBuilder()
                .withId(UUID.fromString("10000000-0000-0000-0000-000000000011"))
                .build();
        ExternalIdentityLink duplicateIdentity = new ExternalIdentityLinkFixtureBuilder()
                .withId(UUID.fromString("10000000-0000-0000-0000-000000000012"))
                .withTeacherAccountId(secondTeacher.id())
                .withIdentity(foundation.identityLink().issuer(), foundation.identityLink().subject())
                .build();
        assertThatThrownBy(() -> teacherIdentities.create(secondTeacher, duplicateIdentity))
                .isInstanceOf(DataIntegrityViolationException.class);

        TeacherBrowserSession duplicateCredential = new TeacherBrowserSessionFixtureBuilder()
                .withId(UUID.fromString("10000000-0000-0000-0000-000000000013"))
                .withTeacherAccountId(foundation.teacher().id())
                .withVerifiers(foundation.browserSession().credentialVerifier(), "9".repeat(64))
                .build();
        assertThatThrownBy(() -> browserSessions.create(duplicateCredential))
                .isInstanceOf(DataIntegrityViolationException.class);

        ParticipantSession missingClassroom = new ParticipantSessionFixtureBuilder()
                .withId(UUID.fromString("10000000-0000-0000-0000-000000000014"))
                .withClassroomSessionId(UUID.fromString("30000000-0000-0000-0000-000000000099"))
                .withVerifier("a".repeat(64))
                .build();
        assertThatThrownBy(() -> participantSessions.tryCreateActive(missingClassroom))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void pairingGrantExpiresExactlyFiveMinutesAndCanBeConsumedOnlyOnceConcurrently() throws Exception {
        Foundation foundation = createFoundation();
        PairingGrant grant = new PairingGrantFixtureBuilder()
                .withClassroomSessionId(foundation.classroomSession().id().value())
                .build();
        pairingGrants.create(grant);

        assertThatThrownBy(() -> jdbcClient.sql("""
                                INSERT INTO classroom_pairing_grant (
                                    id, classroom_session_id, participant_role, credential_verifier,
                                    issued_at, expires_at)
                                VALUES (:id, :sessionId, 'CLASSROOM_DISPLAY', :verifier, :issuedAt, :expiresAt)
                                """)
                        .param("id", UUID.randomUUID())
                        .param("sessionId", foundation.classroomSession().id().value())
                        .param("verifier", "b".repeat(64))
                        .param("issuedAt", java.time.OffsetDateTime.ofInstant(grant.issuedAt(), java.time.ZoneOffset.UTC))
                        .param("expiresAt", java.time.OffsetDateTime.ofInstant(
                                grant.issuedAt().plusSeconds(301), java.time.ZoneOffset.UTC))
                        .update())
                .isInstanceOf(DataIntegrityViolationException.class);

        Instant claimTime = grant.issuedAt().plusSeconds(10);
        List<Boolean> claimed = runConcurrently(2, () -> pairingGrants
                .consumeByCredentialVerifier(grant.credentialVerifier(), claimTime)
                .isPresent());
        assertThat(claimed).containsExactlyInAnyOrder(true, false);
        assertThat(pairingGrants.revoke(
                        foundation.classroomSession().id(), grant.id(), claimTime.plusSeconds(1)))
                .isFalse();
    }

    @Test
    void pairingGrantRevocationRequiresItsParentClassroomSession() {
        Foundation foundation = createFoundation();
        ClassroomSession otherSession = new ClassroomSessionFixtureBuilder()
                .withId(UUID.fromString("30000000-0000-0000-0000-000000000011"))
                .withReferences(
                        foundation.teacher().id().value(), foundation.lessonVersion().id().value())
                .build();
        classroomSessions.create(otherSession);

        PairingGrant grant = new PairingGrantFixtureBuilder()
                .withClassroomSessionId(foundation.classroomSession().id().value())
                .build();
        pairingGrants.create(grant);
        Instant revokedAt = grant.issuedAt().plusSeconds(10);

        assertThat(pairingGrants.revoke(otherSession.id(), grant.id(), revokedAt)).isFalse();
        assertThat(pairingGrants.findByCredentialVerifier(grant.credentialVerifier())).contains(grant);
        assertThat(pairingGrants.revoke(foundation.classroomSession().id(), grant.id(), revokedAt))
                .isTrue();

        PairingGrant revoked = pairingGrants
                .findByCredentialVerifier(grant.credentialVerifier())
                .orElseThrow();
        assertThat(revoked.classroomSessionId()).isEqualTo(foundation.classroomSession().id());
        assertThat(revoked.revokedAt()).isEqualTo(revokedAt);
        assertThat(pairingGrants.revoke(
                        foundation.classroomSession().id(), grant.id(), revokedAt.plusSeconds(1)))
                .isFalse();
    }

    @Test
    void databaseEnforcesIndependentSingleActiveControllerAndDisplaySlotsUnderConcurrency()
            throws Exception {
        Foundation foundation = createFoundation();

        ParticipantSession controllerOne = controller(
                foundation, "10000000-0000-0000-0000-000000000021", "c");
        ParticipantSession controllerTwo = controller(
                foundation, "10000000-0000-0000-0000-000000000022", "d");
        assertThat(runConcurrently(
                        List.of(
                                () -> participantSessions.tryCreateActive(controllerOne),
                                () -> participantSessions.tryCreateActive(controllerTwo))))
                .containsExactlyInAnyOrder(true, false);

        ParticipantSession displayOne = display(
                foundation, "10000000-0000-0000-0000-000000000023", "e");
        ParticipantSession displayTwo = display(
                foundation, "10000000-0000-0000-0000-000000000024", "f");
        assertThat(runConcurrently(
                        List.of(
                                () -> participantSessions.tryCreateActive(displayOne),
                                () -> participantSessions.tryCreateActive(displayTwo))))
                .containsExactlyInAnyOrder(true, false);

        Integer activeRoles = jdbcClient.sql("""
                        SELECT count(*)
                        FROM identity_participant_session
                        WHERE classroom_session_id = :sessionId AND revoked_at IS NULL
                        """)
                .param("sessionId", foundation.classroomSession().id().value())
                .query(Integer.class)
                .single();
        assertThat(activeRoles).isEqualTo(2);
    }

    @Test
    void unrelatedParticipantCredentialAndIdCollisionsRemainDatabaseErrors() {
        Foundation foundation = createFoundation();
        ParticipantSession display = display(
                foundation, "10000000-0000-0000-0000-000000000025", "1");
        assertThat(participantSessions.tryCreateActive(display)).isTrue();
        assertThat(participantSessions.revoke(display.id(), display.createdAt().plusSeconds(1))).isTrue();

        ParticipantSession duplicateCredential = display(
                foundation, "10000000-0000-0000-0000-000000000026", "1");
        assertThatThrownBy(() -> participantSessions.tryCreateActive(duplicateCredential))
                .isInstanceOf(DataIntegrityViolationException.class);

        ParticipantSession duplicateId = display(
                foundation, "10000000-0000-0000-0000-000000000025", "2");
        assertThatThrownBy(() -> participantSessions.tryCreateActive(duplicateId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void acceptedCommandIdentityIsResolvedByConcurrentCompetingInserts() throws Exception {
        Foundation foundation = createFoundation();
        ParticipantSession controller = controller(
                foundation, "10000000-0000-0000-0000-000000000031", "7");
        assertThat(participantSessions.tryCreateActive(controller)).isTrue();

        AcceptedCommandOutcome original = new AcceptedCommandOutcomeFixtureBuilder()
                .withContext(
                        foundation.classroomSession().id().value(),
                        foundation.teacher().id().value(),
                        foundation.browserSession().id().value(),
                        controller.id().value())
                .build();
        AcceptedCommandOutcome duplicate = new AcceptedCommandOutcomeFixtureBuilder()
                .withId(UUID.fromString("30000000-0000-0000-0000-000000000099"))
                .withCommandId(original.commandId())
                .withContext(
                        foundation.classroomSession().id().value(),
                        foundation.teacher().id().value(),
                        foundation.browserSession().id().value(),
                        controller.id().value())
                .build();

        List<Boolean> outcomes = runConcurrently(List.of(
                () -> commandOutcomes.saveIfAbsent(original),
                () -> commandOutcomes.saveIfAbsent(duplicate)));
        assertThat(outcomes).containsExactlyInAnyOrder(true, false);

        Integer acceptedRows = jdbcClient.sql("""
                        SELECT count(*)
                        FROM classroom_accepted_command
                        WHERE classroom_session_id = :classroomSessionId
                          AND command_id = :commandId
                        """)
                .param("classroomSessionId", foundation.classroomSession().id().value())
                .param("commandId", original.commandId())
                .query(Integer.class)
                .single();
        assertThat(acceptedRows).isEqualTo(1);

        AcceptedCommandOutcome loaded = commandOutcomes
                .findByCommandIdentity(foundation.classroomSession().id(), original.commandId())
                .orElseThrow();
        assertThat(List.of(original, duplicate)).contains(loaded);
    }

    @Test
    void unrelatedAcceptedCommandIdCollisionRemainsDatabaseError() {
        Foundation foundation = createFoundation();
        ParticipantSession controller = controller(
                foundation, "10000000-0000-0000-0000-000000000032", "8");
        assertThat(participantSessions.tryCreateActive(controller)).isTrue();

        AcceptedCommandOutcome original = new AcceptedCommandOutcomeFixtureBuilder()
                .withContext(
                        foundation.classroomSession().id().value(),
                        foundation.teacher().id().value(),
                        foundation.browserSession().id().value(),
                        controller.id().value())
                .build();
        AcceptedCommandOutcome duplicateId = new AcceptedCommandOutcomeFixtureBuilder()
                .withCommandId("command-002")
                .withContext(
                        foundation.classroomSession().id().value(),
                        foundation.teacher().id().value(),
                        foundation.browserSession().id().value(),
                        controller.id().value())
                .build();

        assertThat(commandOutcomes.saveIfAbsent(original)).isTrue();
        assertThatThrownBy(() -> commandOutcomes.saveIfAbsent(duplicateId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void revisionAwareUpdateRejectsStaleExpectedRevisionAndKeepsPositionSeparate() {
        Foundation foundation = createFoundation();

        assertThat(classroomSessions.updatePositionIfRevisionMatches(
                        foundation.classroomSession().id(),
                        new Revision(0),
                        1,
                        new Revision(1),
                        ClassroomLifecycleState.ACTIVE))
                .isTrue();
        assertThat(classroomSessions.updatePositionIfRevisionMatches(
                        foundation.classroomSession().id(),
                        new Revision(0),
                        2,
                        new Revision(1),
                        ClassroomLifecycleState.ACTIVE))
                .isFalse();

        ClassroomSession updated = classroomSessions.findById(foundation.classroomSession().id()).orElseThrow();
        assertThat(updated.currentScenePosition()).isEqualTo(1);
        assertThat(updated.revision()).isEqualTo(new Revision(1));
    }

    private Foundation createFoundation() {
        TeacherAccount teacher = new TeacherAccountFixtureBuilder().build();
        ExternalIdentityLink link = new ExternalIdentityLinkFixtureBuilder()
                .withTeacherAccountId(teacher.id())
                .build();
        teacherIdentities.create(teacher, link);

        TeacherBrowserSession browserSession = new TeacherBrowserSessionFixtureBuilder()
                .withTeacherAccountId(teacher.id())
                .build();
        browserSessions.create(browserSession);

        LessonVersionFixtureBuilder.Fixture lessonFixture = new LessonVersionFixtureBuilder()
                .withTeacherAccountId(teacher.id().value())
                .build();
        lessonVersions.createImmutableVersion(lessonFixture.lesson(), lessonFixture.version());

        ClassroomSession classroomSession = new ClassroomSessionFixtureBuilder()
                .withReferences(teacher.id().value(), lessonFixture.version().id().value())
                .build();
        classroomSessions.create(classroomSession);
        return new Foundation(teacher, link, browserSession, lessonFixture.version(), classroomSession);
    }

    private ParticipantSession controller(Foundation foundation, String id, String verifierCharacter) {
        return new ParticipantSessionFixtureBuilder()
                .withId(UUID.fromString(id))
                .withClassroomSessionId(foundation.classroomSession().id().value())
                .withVerifier(verifierCharacter.repeat(64))
                .asController(foundation.teacher().id(), foundation.browserSession().id())
                .build();
    }

    private ParticipantSession display(Foundation foundation, String id, String verifierCharacter) {
        return new ParticipantSessionFixtureBuilder()
                .withId(UUID.fromString(id))
                .withClassroomSessionId(foundation.classroomSession().id().value())
                .withVerifier(verifierCharacter.repeat(64))
                .build();
    }

    private static <T> List<T> runConcurrently(int count, Callable<T> operation) throws Exception {
        return runConcurrently(IntStream.range(0, count).mapToObj(index -> operation).toList());
    }

    private static <T> List<T> runConcurrently(List<Callable<T>> operations) throws Exception {
        CountDownLatch ready = new CountDownLatch(operations.size());
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(operations.size())) {
            List<Future<T>> futures = operations.stream()
                    .map(operation -> executor.submit(() -> {
                        ready.countDown();
                        start.await();
                        return operation.call();
                    }))
                    .toList();
            ready.await();
            start.countDown();
            return futures.stream().map(FirstProtectedSlicePersistenceIT::get).toList();
        }
    }

    private static <T> T get(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for concurrent test operation", exception);
        } catch (java.util.concurrent.ExecutionException exception) {
            throw new IllegalStateException("Concurrent test operation failed", exception.getCause());
        }
    }

    private record Foundation(
            TeacherAccount teacher,
            ExternalIdentityLink identityLink,
            TeacherBrowserSession browserSession,
            LessonVersion lessonVersion,
            ClassroomSession classroomSession) {}
}
