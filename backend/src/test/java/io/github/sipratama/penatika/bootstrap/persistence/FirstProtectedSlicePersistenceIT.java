package io.github.sipratama.penatika.bootstrap.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.jayway.jsonpath.JsonPath;

import io.github.sipratama.penatika.classroom.adapter.out.security.Sha256PairingTokenVerifier;
import io.github.sipratama.penatika.classroom.adapter.in.http.ParticipantSessionCookies;
import io.github.sipratama.penatika.classroom.application.PairingGrantRejectedException;
import io.github.sipratama.penatika.classroom.application.CommandIdReuseConflictException;
import io.github.sipratama.penatika.classroom.application.StaleRevisionException;
import io.github.sipratama.penatika.classroom.application.model.ClassroomCommandRequest;
import io.github.sipratama.penatika.classroom.application.model.ClassroomCommandResult;
import io.github.sipratama.penatika.classroom.application.ParticipantRoleAlreadyActiveException;
import io.github.sipratama.penatika.classroom.application.model.EstablishedParticipant;
import io.github.sipratama.penatika.classroom.application.model.PresentedPairingToken;
import io.github.sipratama.penatika.classroom.application.model.RawPairingToken;
import io.github.sipratama.penatika.classroom.application.port.in.EstablishClassroomDisplayParticipantUseCase;
import io.github.sipratama.penatika.classroom.application.port.in.EstablishTeacherControllerParticipantUseCase;
import io.github.sipratama.penatika.classroom.application.port.in.ExecuteClassroomCommandUseCase;
import io.github.sipratama.penatika.classroom.application.port.out.AcceptedCommandOutcomePersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.ClassroomSessionPersistencePort;
import io.github.sipratama.penatika.classroom.application.port.out.DisplayMutationGatePort;
import io.github.sipratama.penatika.classroom.application.port.out.PairingGrantPersistencePort;
import io.github.sipratama.penatika.classroom.domain.AcceptedCommandOutcome;
import io.github.sipratama.penatika.classroom.domain.ClassroomLifecycleState;
import io.github.sipratama.penatika.classroom.domain.ClassroomSession;
import io.github.sipratama.penatika.classroom.domain.ClassroomSessionId;
import io.github.sipratama.penatika.classroom.domain.PairingGrant;
import io.github.sipratama.penatika.classroom.domain.PairingGrantId;
import io.github.sipratama.penatika.classroom.domain.PairingRole;
import io.github.sipratama.penatika.classroom.domain.Revision;
import io.github.sipratama.penatika.classroom.fixtures.AcceptedCommandOutcomeFixtureBuilder;
import io.github.sipratama.penatika.classroom.fixtures.ClassroomSessionFixtureBuilder;
import io.github.sipratama.penatika.classroom.fixtures.PairingGrantFixtureBuilder;
import io.github.sipratama.penatika.identity.application.TeacherSessionRequiredException;
import io.github.sipratama.penatika.identity.application.port.in.ParticipantSessionAuthorityUseCase;
import io.github.sipratama.penatika.identity.application.port.out.ParticipantSessionPersistencePort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherBrowserSessionPersistencePort;
import io.github.sipratama.penatika.identity.application.port.out.TeacherIdentityPersistencePort;
import io.github.sipratama.penatika.identity.adapter.in.security.TeacherSessionCookies;
import io.github.sipratama.penatika.identity.adapter.out.security.Sha256SecurityTokenVerifier;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;
import io.github.sipratama.penatika.identity.domain.ExternalIdentityLink;
import io.github.sipratama.penatika.identity.domain.ExternalIdentityLinkId;
import io.github.sipratama.penatika.identity.domain.ParticipantSession;
import io.github.sipratama.penatika.identity.domain.ClassroomSessionReference;
import io.github.sipratama.penatika.identity.domain.ParticipantRole;
import io.github.sipratama.penatika.identity.domain.ParticipantSessionId;
import io.github.sipratama.penatika.identity.domain.TeacherAccount;
import io.github.sipratama.penatika.identity.domain.TeacherAccountId;
import io.github.sipratama.penatika.identity.domain.TeacherAccountStatus;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSession;
import io.github.sipratama.penatika.identity.domain.TeacherBrowserSessionId;
import io.github.sipratama.penatika.identity.fixtures.ExternalIdentityLinkFixtureBuilder;
import io.github.sipratama.penatika.identity.fixtures.ParticipantSessionFixtureBuilder;
import io.github.sipratama.penatika.identity.fixtures.TeacherAccountFixtureBuilder;
import io.github.sipratama.penatika.identity.fixtures.TeacherBrowserSessionFixtureBuilder;
import io.github.sipratama.penatika.lesson.application.port.out.LessonVersionPersistencePort;
import io.github.sipratama.penatika.lesson.domain.Lesson;
import io.github.sipratama.penatika.lesson.domain.LessonId;
import io.github.sipratama.penatika.lesson.domain.LessonScene;
import io.github.sipratama.penatika.lesson.domain.LessonSceneId;
import io.github.sipratama.penatika.lesson.domain.LessonVersion;
import io.github.sipratama.penatika.lesson.domain.LessonVersionId;
import io.github.sipratama.penatika.lesson.domain.LessonVersionReadiness;
import io.github.sipratama.penatika.lesson.domain.SceneBlock;
import io.github.sipratama.penatika.lesson.domain.SceneBlockId;
import io.github.sipratama.penatika.lesson.domain.SceneBlockType;
import io.github.sipratama.penatika.lesson.fixtures.LessonVersionFixtureBuilder;

@SpringBootTest
@Testcontainers
@Import(FirstProtectedSlicePersistenceIT.ExactRevisionDisplayGateConfiguration.class)
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
    @Autowired private EstablishTeacherControllerParticipantUseCase establishControllerParticipant;
    @Autowired private EstablishClassroomDisplayParticipantUseCase establishDisplayParticipant;
    @Autowired private ParticipantSessionAuthorityUseCase participantAuthority;
    @Autowired private ExecuteClassroomCommandUseCase executeClassroomCommand;
    @Autowired private ExactRevisionDisplayGate displayMutationGate;
    @Autowired private WebApplicationContext applicationContext;
    @Autowired private Clock clock;

    private final Sha256SecurityTokenVerifier tokenVerifier = new Sha256SecurityTokenVerifier();
    private final Sha256PairingTokenVerifier pairingTokenVerifier = new Sha256PairingTokenVerifier();
    private MockMvc mockMvc;

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
        displayMutationGate.clear();
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
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

    @Test
    void teacherSessionHttpContractRefreshesActivityAndRecoversCsrfMaterial() throws Exception {
        TeacherAccount teacher = createTeacher(
                "10000000-0000-0000-0000-000000000201",
                "10000000-0000-0000-0000-000000000202",
                "http-subject",
                TeacherAccountStatus.ACTIVE);
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        RawSecurityToken sessionToken = rawToken('H');
        RawSecurityToken csrfToken = rawToken('I');
        TeacherBrowserSession session = runtimeSession(
                "10000000-0000-0000-0000-000000000203",
                teacher,
                sessionToken,
                csrfToken,
                now.minusSeconds(300),
                now.minusSeconds(120),
                now.plusSeconds(600),
                now.plusSeconds(3600),
                null);
        browserSessions.create(session);

        MvcResult bootstrap = mockMvc.perform(get("/api/teacher-session")
                        .cookie(
                                cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, sessionToken),
                                cookie(TeacherSessionCookies.CSRF_RECOVERY_COOKIE_NAME, csrfToken)))
                .andReturn();

        assertThat(bootstrap.getResponse().getStatus()).isEqualTo(200);
        assertThat(bootstrap.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(bootstrap.getResponse().getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(bootstrap.getResponse().getContentAsString())
                .isEqualTo("{\"csrfToken\":\"" + csrfToken.expose() + "\"}");
        assertThat(bootstrap.getResponse().getHeaders("Set-Cookie")).isEmpty();

        TeacherBrowserSession refreshed = browserSessions.findById(session.id()).orElseThrow();
        assertThat(refreshed.lastActiveAt()).isAfter(session.lastActiveAt());
        assertThat(refreshed.absoluteExpiresAt()).isEqualTo(session.absoluteExpiresAt());
        assertThat(refreshed.idleExpiresAt())
                .isEqualTo(refreshed.lastActiveAt().plusSeconds(30 * 60));

        MvcResult repeated = mockMvc.perform(get("/api/teacher-session")
                        .cookie(
                                cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, sessionToken),
                                cookie(TeacherSessionCookies.CSRF_RECOVERY_COOKIE_NAME, csrfToken)))
                .andReturn();
        assertThat(repeated.getResponse().getContentAsString())
                .isEqualTo("{\"csrfToken\":\"" + csrfToken.expose() + "\"}");
        assertThat(repeated.getResponse().getHeaders("Set-Cookie")).isEmpty();

        RawSecurityToken recoverySessionToken = rawToken('J');
        RawSecurityToken originalRecoveryCsrf = rawToken('K');
        TeacherBrowserSession recoverySession = runtimeSession(
                "10000000-0000-0000-0000-000000000204",
                teacher,
                recoverySessionToken,
                originalRecoveryCsrf,
                now.minusSeconds(300),
                now.minusSeconds(120),
                now.plusSeconds(600),
                now.plusSeconds(3600),
                null);
        browserSessions.create(recoverySession);

        MvcResult recovered = mockMvc.perform(get("/api/teacher-session")
                        .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, recoverySessionToken)))
                .andReturn();
        String recoveredCsrf = JsonPath.read(recovered.getResponse().getContentAsString(), "$.csrfToken");
        assertThat(recovered.getResponse().getStatus()).isEqualTo(200);
        assertThat(recoveredCsrf)
                .hasSize(43)
                .matches("[A-Za-z0-9_-]{43}")
                .isNotEqualTo(originalRecoveryCsrf.expose())
                .isNotEqualTo(recoverySessionToken.expose());
        assertThat(browserSessions.findByCredentialVerifier(tokenVerifier.verifierFor(recoverySessionToken)))
                .isPresent();
        assertThat(browserSessions.findById(recoverySession.id()).orElseThrow().csrfVerifier())
                .isEqualTo(tokenVerifier.verifierFor(RawSecurityToken.fromEncoded(recoveredCsrf)));
        assertRecoveryCookie(recovered, recoveredCsrf);

        MvcResult stableRecovery = mockMvc.perform(get("/api/teacher-session")
                        .cookie(
                                cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, recoverySessionToken),
                                new jakarta.servlet.http.Cookie(
                                        TeacherSessionCookies.CSRF_RECOVERY_COOKIE_NAME, recoveredCsrf)))
                .andReturn();
        assertThat(stableRecovery.getResponse().getContentAsString())
                .isEqualTo("{\"csrfToken\":\"" + recoveredCsrf + "\"}");
        assertThat(stableRecovery.getResponse().getHeaders("Set-Cookie")).isEmpty();
    }

    @Test
    void everyInvalidOrNonPenatikaAuthorityGetsTheSameNonDisclosing401() throws Exception {
        TeacherAccount teacher = createTeacher(
                "10000000-0000-0000-0000-000000000211",
                "10000000-0000-0000-0000-000000000212",
                "invalid-session-subject",
                TeacherAccountStatus.ACTIVE);
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);

        assertTeacherSessionRequired(get("/api/teacher-session"));
        assertTeacherSessionRequired(get("/api/teacher-session")
                .cookie(new jakarta.servlet.http.Cookie(
                        TeacherSessionCookies.SESSION_COOKIE_NAME, "malformed")));
        assertTeacherSessionRequired(get("/api/teacher-session")
                .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, rawToken('U'))));
        assertTeacherSessionRequired(get("/api/teacher-session")
                .cookie(cookie(TeacherSessionCookies.CSRF_RECOVERY_COOKIE_NAME, rawToken('V'))));
        assertTeacherSessionRequired(get("/api/teacher-session")
                .header("Authorization", "Bearer upstream-oauth-token"));

        MockHttpSession oidcFrameworkSession = new MockHttpSession();
        oidcFrameworkSession.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new SecurityContextImpl(oidcAuthentication(now)));
        assertTeacherSessionRequired(get("/api/teacher-session").session(oidcFrameworkSession));

        RawSecurityToken revokedToken = rawToken('A');
        browserSessions.create(runtimeSession(
                "10000000-0000-0000-0000-000000000213",
                teacher,
                revokedToken,
                rawToken('B'),
                now.minusSeconds(3600),
                now.minusSeconds(300),
                now.plusSeconds(600),
                now.plusSeconds(3600),
                now.minusSeconds(1)));
        assertTeacherSessionRequired(get("/api/teacher-session")
                .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, revokedToken)));

        RawSecurityToken idleExpiredToken = rawToken('C');
        browserSessions.create(runtimeSession(
                "10000000-0000-0000-0000-000000000214",
                teacher,
                idleExpiredToken,
                rawToken('D'),
                now.minusSeconds(3600),
                now.minusSeconds(1800),
                now.minusSeconds(1),
                now.plusSeconds(3600),
                null));
        assertTeacherSessionRequired(get("/api/teacher-session")
                .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, idleExpiredToken)));

        RawSecurityToken absoluteExpiredToken = rawToken('E');
        browserSessions.create(runtimeSession(
                "10000000-0000-0000-0000-000000000215",
                teacher,
                absoluteExpiredToken,
                rawToken('F'),
                now.minusSeconds(9 * 3600),
                now.minusSeconds(300),
                now.plusSeconds(600),
                now.minusSeconds(1),
                null));
        assertTeacherSessionRequired(get("/api/teacher-session")
                .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, absoluteExpiredToken)));

        RawSecurityToken inactiveToken = rawToken('G');
        browserSessions.create(runtimeSession(
                "10000000-0000-0000-0000-000000000216",
                teacher,
                inactiveToken,
                rawToken('L'),
                now.minusSeconds(3600),
                now.minusSeconds(300),
                now.plusSeconds(600),
                now.plusSeconds(3600),
                null));
        jdbcClient.sql("UPDATE identity_teacher_account SET status = 'DISABLED' WHERE id = :id")
                .param("id", teacher.id().value())
                .update();
        assertTeacherSessionRequired(get("/api/teacher-session")
                .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, inactiveToken)));
    }

    @Test
    void teacherSessionPersistenceMakesRotationExpiryActivityRevocationAndCsrfCasDurable() {
        TeacherAccount firstTeacher = createTeacher(
                "10000000-0000-0000-0000-000000000221",
                "10000000-0000-0000-0000-000000000222",
                "first-rotation-subject",
                TeacherAccountStatus.ACTIVE);
        TeacherAccount secondTeacher = createTeacher(
                "10000000-0000-0000-0000-000000000223",
                "10000000-0000-0000-0000-000000000224",
                "second-rotation-subject",
                TeacherAccountStatus.ACTIVE);
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        RawSecurityToken oldCredential = rawToken('M');
        TeacherBrowserSession oldSession = runtimeSession(
                "10000000-0000-0000-0000-000000000225",
                firstTeacher,
                oldCredential,
                rawToken('N'),
                now.minusSeconds(3600),
                now.minusSeconds(300),
                now.plusSeconds(600),
                now.plusSeconds(3600),
                null);
        browserSessions.create(oldSession);
        TeacherBrowserSession independentSession = runtimeSession(
                "10000000-0000-0000-0000-000000000228",
                firstTeacher,
                rawToken('Z'),
                rawToken('0'),
                now.minusSeconds(1800),
                now.minusSeconds(120),
                now.plusSeconds(600),
                now.plusSeconds(3600),
                null);
        browserSessions.create(independentSession);

        RawSecurityToken newCredential = rawToken('P');
        RawSecurityToken originalCsrf = rawToken('Q');
        TeacherBrowserSession replacement = runtimeSession(
                "10000000-0000-0000-0000-000000000226",
                secondTeacher,
                newCredential,
                originalCsrf,
                now.minusSeconds(60),
                now.minusSeconds(60),
                now.plusSeconds(600),
                now.plusSeconds(1200),
                null);
        browserSessions.createReplacing(
                replacement, Optional.of(tokenVerifier.verifierFor(oldCredential)), now);

        assertThat(browserSessions.findByCredentialVerifier(tokenVerifier.verifierFor(newCredential)))
                .contains(replacement);
        assertThat(browserSessions.findByCredentialVerifier(tokenVerifier.verifierFor(oldCredential))
                        .orElseThrow()
                        .revokedAt())
                .isEqualTo(now);
        assertThat(browserSessions.findById(independentSession.id()).orElseThrow().revokedAt())
                .isNull();
        assertThat(jdbcClient.sql("""
                        SELECT credential_verifier
                        FROM identity_teacher_browser_session
                        WHERE id = :id
                        """)
                .param("id", replacement.id().value())
                .query(String.class)
                .single()
                .trim())
                .isEqualTo(tokenVerifier.verifierFor(newCredential))
                .isNotEqualTo(newCredential.expose());

        assertThat(browserSessions.refreshActivity(
                        replacement.id(), now, now.plusSeconds(30 * 60)))
                .isTrue();
        TeacherBrowserSession refreshed = browserSessions.findById(replacement.id()).orElseThrow();
        assertThat(refreshed.lastActiveAt()).isEqualTo(now);
        assertThat(refreshed.idleExpiresAt()).isEqualTo(replacement.absoluteExpiresAt());
        assertThat(refreshed.absoluteExpiresAt()).isEqualTo(replacement.absoluteExpiresAt());
        assertThat(browserSessions.refreshActivity(
                        replacement.id(), now.minusSeconds(1), now.plusSeconds(60)))
                .isFalse();

        RawSecurityToken replacementCsrf = rawToken('R');
        assertThat(browserSessions.replaceCsrfVerifierAndRefreshActivity(
                        replacement.id(),
                        tokenVerifier.verifierFor(originalCsrf),
                        tokenVerifier.verifierFor(replacementCsrf),
                        now.plusSeconds(1),
                        replacement.absoluteExpiresAt()))
                .isTrue();
        assertThat(browserSessions.replaceCsrfVerifierAndRefreshActivity(
                        replacement.id(),
                        tokenVerifier.verifierFor(originalCsrf),
                        tokenVerifier.verifierFor(rawToken('T')),
                        now.plusSeconds(2),
                        replacement.absoluteExpiresAt()))
                .isFalse();
        assertThat(browserSessions.findById(replacement.id()).orElseThrow().csrfVerifier())
                .isEqualTo(tokenVerifier.verifierFor(replacementCsrf));

        assertThat(browserSessions.revoke(replacement.id(), now.plusSeconds(3))).isTrue();
        assertThat(browserSessions.refreshActivity(
                        replacement.id(), now.plusSeconds(4), replacement.absoluteExpiresAt()))
                .isFalse();
        assertThat(browserSessions.replaceCsrfVerifierAndRefreshActivity(
                        replacement.id(),
                        tokenVerifier.verifierFor(replacementCsrf),
                        tokenVerifier.verifierFor(rawToken('W')),
                        now.plusSeconds(4),
                        replacement.absoluteExpiresAt()))
                .isFalse();

        TeacherBrowserSession expired = runtimeSession(
                "10000000-0000-0000-0000-000000000227",
                secondTeacher,
                rawToken('X'),
                rawToken('Y'),
                now.minusSeconds(3600),
                now.minusSeconds(1800),
                now.minusSeconds(1),
                now.plusSeconds(3600),
                null);
        browserSessions.create(expired);
        assertThat(browserSessions.refreshActivity(
                        expired.id(), now, now.plusSeconds(1800)))
                .isFalse();
    }

    @Test
    void classroomSessionStartReturnsExactContractAndPersistsInitialAuthority() throws Exception {
        TeacherAccount teacher = createTeacher(
                "10000000-0000-0000-0000-000000000301",
                "10000000-0000-0000-0000-000000000302",
                "classroom-start-subject",
                TeacherAccountStatus.ACTIVE);
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        SessionAuthority authority = createSessionAuthority(
                teacher,
                "10000000-0000-0000-0000-000000000303",
                'a',
                'b',
                now);
        LessonVersion lessonVersion = createLessonVersion(
                teacher,
                "20000000-0000-0000-0000-000000000301",
                "20000000-0000-0000-0000-000000000302",
                LessonVersionReadiness.CLASSROOM_READY,
                true);
        Instant requestStartedAt = clock.instant();

        MvcResult result = startClassroomSession(
                authority, lessonVersion.id().value().toString()).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        assertThat(result.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(result.getResponse().getHeader("Location")).isNull();
        Map<String, Object> body = JsonPath.read(result.getResponse().getContentAsString(), "$");
        assertThat(body).containsOnlyKeys("classroomSessionId", "lessonVersionId", "revision");
        assertThat(body.get("lessonVersionId")).isEqualTo(lessonVersion.id().value().toString());
        assertThat(body.get("revision")).isEqualTo(0);

        ClassroomSession persisted = classroomSessions.findById(new ClassroomSessionId(
                        UUID.fromString((String) body.get("classroomSessionId"))))
                .orElseThrow();
        assertThat(persisted.teacherAccountId()).isEqualTo(teacher.id().value());
        assertThat(persisted.lessonVersionId()).isEqualTo(lessonVersion.id().value());
        assertThat(persisted.lifecycleState()).isEqualTo(ClassroomLifecycleState.CREATED);
        assertThat(persisted.currentScenePosition()).isZero();
        assertThat(persisted.revision()).isEqualTo(new Revision(0));
        assertThat(persisted.startedAt()).isBetween(requestStartedAt, clock.instant());
        assertThat(browserSessions.findById(authority.session().id()).orElseThrow().lastActiveAt())
                .isAfter(authority.session().lastActiveAt());
        assertThat(rowCount("classroom_pairing_grant")).isZero();
        assertThat(rowCount("identity_participant_session")).isZero();
    }

    @Test
    void classroomSessionStartDistinguishesTeacherSessionAndCsrfFailures() throws Exception {
        TeacherAccount teacher = createTeacher(
                "10000000-0000-0000-0000-000000000311",
                "10000000-0000-0000-0000-000000000312",
                "classroom-authority-subject",
                TeacherAccountStatus.ACTIVE);
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        SessionAuthority authority = createSessionAuthority(
                teacher,
                "10000000-0000-0000-0000-000000000313",
                'c',
                'd',
                now);
        String request = "{\"lessonVersionId\":\"lesson_version_example_01\"}";

        assertProblem(mockMvc.perform(post("/api/classroom-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andReturn(), 401, "TEACHER_SESSION_REQUIRED");
        assertProblem(mockMvc.perform(post("/api/classroom-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
                        .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, rawToken('e'))))
                .andReturn(), 401, "TEACHER_SESSION_REQUIRED");

        assertProblem(mockMvc.perform(classroomSessionStartRequest(
                        authority, "lesson_version_example_01"))
                .andReturn(), 403, "CSRF_REJECTED");
        assertProblem(mockMvc.perform(classroomSessionStartRequest(
                                authority, "lesson_version_example_01")
                        .header("X-Penatika-CSRF", rawToken('f').expose()))
                .andReturn(), 403, "CSRF_REJECTED");
        assertProblem(mockMvc.perform(post("/api/classroom-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
                        .cookie(
                                cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, authority.sessionToken()),
                                cookie(TeacherSessionCookies.CSRF_RECOVERY_COOKIE_NAME, authority.csrfToken())))
                .andReturn(), 403, "CSRF_REJECTED");
        assertProblem(mockMvc.perform(classroomSessionStartRequest(
                                authority, "lesson_version_example_01")
                        .header("X-Penatika-CSRF", authority.sessionToken().expose()))
                .andReturn(), 403, "CSRF_REJECTED");

        assertThat(browserSessions.findById(authority.session().id()).orElseThrow().lastActiveAt())
                .isEqualTo(authority.session().lastActiveAt());
        assertThat(rowCount("classroom_session")).isZero();
    }

    @Test
    void classroomSessionStartRejectsEveryStructuralRequestFailureBeforeActivity() throws Exception {
        TeacherAccount teacher = createTeacher(
                "10000000-0000-0000-0000-000000000321",
                "10000000-0000-0000-0000-000000000322",
                "classroom-validation-subject",
                TeacherAccountStatus.ACTIVE);
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        SessionAuthority authority = createSessionAuthority(
                teacher,
                "10000000-0000-0000-0000-000000000323",
                'g',
                'h',
                now);
        List<String> invalidBodies = List.of(
                "{\"lessonVersionId\":",
                "{}",
                "[]",
                "{\"lessonVersionId\":null}",
                "{\"lessonVersionId\":\"\"}",
                "{\"lessonVersionId\":\"two words\"}",
                "{\"lessonVersionId\":\"" + "x".repeat(129) + "\"}",
                "{\"lessonVersionId\":\"lesson_version_example_01\",\"teacherAccountId\":\"forged\"}");

        for (String invalidBody : invalidBodies) {
            MvcResult result = mockMvc.perform(post("/api/classroom-sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody)
                            .cookie(cookie(
                                    TeacherSessionCookies.SESSION_COOKIE_NAME, authority.sessionToken()))
                            .header("X-Penatika-CSRF", authority.csrfToken().expose()))
                    .andReturn();
            assertProblem(result, 400, "REQUEST_VALIDATION_FAILED");
            assertThat(result.getResponse().getContentAsString())
                    .doesNotContain("two words", "forged", "x".repeat(129));
        }

        assertThat(browserSessions.findById(authority.session().id()).orElseThrow().lastActiveAt())
                .isEqualTo(authority.session().lastActiveAt());
        assertThat(rowCount("classroom_session")).isZero();
    }

    @Test
    void classroomSessionStartPreservesLessonOwnershipAndReadinessNonDisclosure() throws Exception {
        TeacherAccount teacher = createTeacher(
                "10000000-0000-0000-0000-000000000331",
                "10000000-0000-0000-0000-000000000332",
                "classroom-lesson-subject",
                TeacherAccountStatus.ACTIVE);
        TeacherAccount otherTeacher = createTeacher(
                "10000000-0000-0000-0000-000000000333",
                "10000000-0000-0000-0000-000000000334",
                "other-classroom-lesson-subject",
                TeacherAccountStatus.ACTIVE);
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        SessionAuthority authority = createSessionAuthority(
                teacher,
                "10000000-0000-0000-0000-000000000335",
                'i',
                'j',
                now);
        LessonVersion draft = createLessonVersion(
                teacher,
                "20000000-0000-0000-0000-000000000311",
                "20000000-0000-0000-0000-000000000312",
                LessonVersionReadiness.DRAFT,
                true);
        LessonVersion otherTeachersVersion = createLessonVersion(
                otherTeacher,
                "20000000-0000-0000-0000-000000000321",
                "20000000-0000-0000-0000-000000000322",
                LessonVersionReadiness.CLASSROOM_READY,
                true);
        LessonVersion incompleteReady = createLessonVersion(
                teacher,
                "20000000-0000-0000-0000-000000000331",
                "20000000-0000-0000-0000-000000000332",
                LessonVersionReadiness.CLASSROOM_READY,
                false);

        MvcResult opaqueId = startClassroomSession(
                authority, "lesson_version_example_01").andReturn();
        assertProblem(opaqueId, 404, "LESSON_VERSION_NOT_FOUND");
        assertThat(opaqueId.getResponse().getContentAsString())
                .doesNotContain("lesson_version_example_01", "UUID");

        String nonexistentId = "20000000-0000-0000-0000-000000000399";
        MvcResult nonexistent = startClassroomSession(authority, nonexistentId).andReturn();
        assertProblem(nonexistent, 404, "LESSON_VERSION_NOT_FOUND");
        assertThat(nonexistent.getResponse().getContentAsString()).doesNotContain(nonexistentId);

        String otherTeachersVersionId = otherTeachersVersion.id().value().toString();
        MvcResult unauthorized = startClassroomSession(
                authority, otherTeachersVersionId).andReturn();
        assertProblem(unauthorized, 404, "LESSON_VERSION_NOT_FOUND");
        assertThat(unauthorized.getResponse().getContentAsString())
                .doesNotContain(otherTeachersVersionId, otherTeacher.id().value().toString());
        assertProblem(startClassroomSession(authority, draft.id().value().toString()).andReturn(),
                409, "LESSON_VERSION_NOT_READY");
        assertProblem(startClassroomSession(
                        authority, incompleteReady.id().value().toString()).andReturn(),
                409, "LESSON_VERSION_NOT_READY");

        assertThat(rowCount("classroom_session")).isZero();
    }

    @Test
    void pairingGrantCreationHttpEnforcesContractAuthorityAndRequestedRoleAvailability() throws Exception {
        PairingFoundation foundation = createPairingFoundation('1', '2');

        IssuedGrant controller = issueGrant(
                foundation.authority(), foundation.classroomSession(), "TEACHER_CONTROLLER");
        IssuedGrant display = issueGrant(
                foundation.authority(), foundation.classroomSession(), "CLASSROOM_DISPLAY");

        assertThat(controller.token()).hasSize(43).matches("[A-Za-z0-9_-]{43}");
        assertThat(display.token()).hasSize(43).matches("[A-Za-z0-9_-]{43}");
        assertThat(display.token()).isNotEqualTo(controller.token());
        PairingGrant stored = pairingGrants.findByCredentialVerifier(pairingVerifier(controller.token()))
                .orElseThrow();
        assertThat(Duration.between(stored.issuedAt(), stored.expiresAt())).isEqualTo(Duration.ofMinutes(5));
        assertThat(stored.expiresAt()).isEqualTo(controller.expiresAt());

        List<String> invalidBodies = List.of(
                "{\"participantRole\":",
                "{}",
                "[]",
                "{\"participantRole\":null}",
                "{\"participantRole\":\"CONTROLLER\"}",
                "{\"participantRole\":\"CLASSROOM_DISPLAY\",\"extra\":true}");
        for (String invalidBody : invalidBodies) {
            assertProblem(mockMvc.perform(post(pairingGrantPath(foundation.classroomSession()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody)
                            .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()))
                            .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                    .andReturn(), 400, "REQUEST_VALIDATION_FAILED");
        }
        assertProblem(mockMvc.perform(post("/api/classroom-sessions/{id}/pairing-grants", "x".repeat(129))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"participantRole\":\"CLASSROOM_DISPLAY\"}")
                        .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()))
                        .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                .andReturn(), 400, "REQUEST_VALIDATION_FAILED");
        assertProblem(createPairingGrantRequest(
                        foundation.authority(), "opaque-classroom-id", "CLASSROOM_DISPLAY")
                .andReturn(), 404, "CLASSROOM_SESSION_NOT_FOUND");
        ClassroomSession canonicalAliasTarget = createClassroomSession(
                foundation.teacher(),
                foundation.lessonVersion(),
                UUID.fromString("00000001-0001-0001-0001-000000000001"));
        int grantCountBeforeAlias = rowCount("classroom_pairing_grant");
        assertProblem(createPairingGrantRequest(
                        foundation.authority(), "1-1-1-1-1", "CLASSROOM_DISPLAY")
                .andReturn(), 404, "CLASSROOM_SESSION_NOT_FOUND");
        assertThat(canonicalAliasTarget.id().value().toString())
                .isEqualTo("00000001-0001-0001-0001-000000000001");
        assertThat(rowCount("classroom_pairing_grant")).isEqualTo(grantCountBeforeAlias);
        TeacherAccount otherTeacher = createTeacher(
                "10000000-0000-0000-0000-000000000621",
                "10000000-0000-0000-0000-000000000622",
                "unauthorized-pairing-parent",
                TeacherAccountStatus.ACTIVE);
        LessonVersion otherLesson = createLessonVersion(
                otherTeacher,
                "20000000-0000-0000-0000-000000000621",
                "20000000-0000-0000-0000-000000000622",
                LessonVersionReadiness.CLASSROOM_READY,
                true);
        ClassroomSession unauthorizedParent = createClassroomSession(otherTeacher, otherLesson, UUID.randomUUID());
        assertProblem(createPairingGrantRequest(
                        foundation.authority(), unauthorizedParent.id().value().toString(), "CLASSROOM_DISPLAY")
                .andReturn(), 404, "CLASSROOM_SESSION_NOT_FOUND");

        jdbcClient.sql("UPDATE classroom_session SET lifecycle_state = 'FAILED' WHERE id = :id")
                .param("id", foundation.classroomSession().id().value())
                .update();
        assertProblem(createPairingGrantRequest(
                        foundation.authority(), foundation.classroomSession().id().value().toString(), "CLASSROOM_DISPLAY")
                .andReturn(), 409, "CLASSROOM_SESSION_NOT_PAIRABLE");
        jdbcClient.sql("UPDATE classroom_session SET lifecycle_state = 'CREATED' WHERE id = :id")
                .param("id", foundation.classroomSession().id().value())
                .update();

        ParticipantSession occupant = ParticipantSession.display(
                new io.github.sipratama.penatika.identity.domain.ParticipantSessionId(UUID.randomUUID()),
                new io.github.sipratama.penatika.identity.domain.ClassroomSessionReference(
                        foundation.classroomSession().id().value()),
                "9".repeat(64),
                clock.instant());
        assertThat(participantSessions.tryCreateActive(occupant)).isTrue();
        assertProblem(createPairingGrantRequest(
                        foundation.authority(), foundation.classroomSession().id().value().toString(), "CLASSROOM_DISPLAY")
                .andReturn(), 409, "PARTICIPANT_ROLE_ALREADY_ACTIVE");
        assertThat(createPairingGrantRequest(
                        foundation.authority(), foundation.classroomSession().id().value().toString(), "TEACHER_CONTROLLER")
                .andReturn().getResponse().getStatus()).isEqualTo(201);
    }

    @Test
    void pairingGrantRevocationIsParentAuthorizedRetrySafeAndNonDisclosing() throws Exception {
        PairingFoundation foundation = createPairingFoundation('3', '4');
        ClassroomSession otherParent = createClassroomSession(
                foundation.teacher(), foundation.lessonVersion(), UUID.randomUUID());
        IssuedGrant live = issueGrant(
                foundation.authority(), foundation.classroomSession(), "CLASSROOM_DISPLAY");

        assertThat(revokeGrant(foundation.authority(), foundation.classroomSession(), live.id())
                .andReturn().getResponse().getStatus()).isEqualTo(204);
        assertThat(revokeGrant(foundation.authority(), foundation.classroomSession(), live.id())
                .andReturn().getResponse().getStatus()).isEqualTo(204);
        assertThat(revokeGrant(foundation.authority(), foundation.classroomSession(), UUID.randomUUID().toString())
                .andReturn().getResponse().getStatus()).isEqualTo(204);
        assertThat(revokeGrant(foundation.authority(), foundation.classroomSession(), "opaque-grant-id")
                .andReturn().getResponse().getStatus()).isEqualTo(204);
        RawPairingToken aliasTargetToken = rawPairingToken('a');
        PairingGrant canonicalAliasTarget = PairingGrant.issue(
                new PairingGrantId(UUID.fromString("00000001-0001-0001-0001-000000000001")),
                foundation.classroomSession().id(),
                PairingRole.CLASSROOM_DISPLAY,
                pairingTokenVerifier.verifierFor(aliasTargetToken),
                clock.instant());
        pairingGrants.create(canonicalAliasTarget);
        assertThat(revokeGrant(foundation.authority(), foundation.classroomSession(), "1-1-1-1-1")
                .andReturn().getResponse().getStatus()).isEqualTo(204);
        assertThat(pairingGrants.findByCredentialVerifier(canonicalAliasTarget.credentialVerifier())
                        .orElseThrow().revokedAt())
                .isNull();
        assertProblem(mockMvc.perform(delete(pairingGrantRevocationPath(
                                foundation.classroomSession(), "x".repeat(129)))
                        .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()))
                        .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                .andReturn(), 400, "REQUEST_VALIDATION_FAILED");

        PairingGrant wrongParent = createGrant(otherParent, PairingRole.CLASSROOM_DISPLAY, 'w', clock.instant());
        assertThat(revokeGrant(foundation.authority(), foundation.classroomSession(), wrongParent.id().value().toString())
                .andReturn().getResponse().getStatus()).isEqualTo(204);
        assertThat(pairingGrants.findByCredentialVerifier(wrongParent.credentialVerifier()).orElseThrow().revokedAt())
                .isNull();

        PairingGrant consumed = createGrant(
                foundation.classroomSession(), PairingRole.CLASSROOM_DISPLAY, 'c', clock.instant().minusSeconds(30));
        pairingGrants.consumeByCredentialVerifier(consumed.credentialVerifier(), clock.instant()).orElseThrow();
        assertThat(revokeGrant(foundation.authority(), foundation.classroomSession(), consumed.id().value().toString())
                .andReturn().getResponse().getStatus()).isEqualTo(204);
        PairingGrant expired = createGrant(
                foundation.classroomSession(), PairingRole.CLASSROOM_DISPLAY, 'e', clock.instant().minusSeconds(301));
        assertThat(revokeGrant(foundation.authority(), foundation.classroomSession(), expired.id().value().toString())
                .andReturn().getResponse().getStatus()).isEqualTo(204);

        TeacherAccount otherTeacher = createTeacher(
                "10000000-0000-0000-0000-000000000631",
                "10000000-0000-0000-0000-000000000632",
                "unauthorized-revocation-parent",
                TeacherAccountStatus.ACTIVE);
        LessonVersion otherLesson = createLessonVersion(
                otherTeacher,
                "20000000-0000-0000-0000-000000000631",
                "20000000-0000-0000-0000-000000000632",
                LessonVersionReadiness.CLASSROOM_READY,
                true);
        ClassroomSession unauthorizedParent = createClassroomSession(otherTeacher, otherLesson, UUID.randomUUID());
        assertProblem(mockMvc.perform(delete(pairingGrantRevocationPath(
                                unauthorizedParent, UUID.randomUUID().toString()))
                        .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()))
                        .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                .andReturn(), 404, "CLASSROOM_SESSION_NOT_FOUND");

        assertProblem(mockMvc.perform(delete(pairingGrantRevocationPath(
                                foundation.classroomSession(), live.id())))
                .andReturn(), 401, "TEACHER_SESSION_REQUIRED");
        assertProblem(mockMvc.perform(delete(pairingGrantRevocationPath(
                                foundation.classroomSession(), live.id()))
                        .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()))
                        .header("X-Penatika-CSRF", rawToken('5').expose()))
                .andReturn(), 403, "CSRF_REJECTED");
    }

    @Test
    void participantEstablishmentHttpPersistsDistinctAuthorityAndBoundedSecureCookies() throws Exception {
        PairingFoundation foundation = createPairingFoundation('6', '7');
        IssuedGrant controllerGrant = issueGrant(
                foundation.authority(), foundation.classroomSession(), "TEACHER_CONTROLLER");
        IssuedGrant displayGrant = issueGrant(
                foundation.authority(), foundation.classroomSession(), "CLASSROOM_DISPLAY");

        MvcResult controller = mockMvc.perform(post("/api/teacher-controller-participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(redemptionBody(controllerGrant.token()))
                        .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()))
                        .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                .andReturn();
        assertEstablishedParticipant(controller, foundation.classroomSession(), "TEACHER_CONTROLLER");
        String controllerCredential = assertParticipantCookie(controller);
        assertThat(controllerCredential).isNotEqualTo(controllerGrant.token());
        ParticipantSession controllerSession = participantSessions
                .findByCredentialVerifier(tokenVerifier.verifierFor(RawSecurityToken.fromEncoded(controllerCredential)))
                .orElseThrow();
        assertThat(controllerSession.teacherAccountId()).isEqualTo(foundation.teacher().id());
        assertThat(controllerSession.teacherBrowserSessionId()).isEqualTo(foundation.authority().session().id());
        assertThat(Duration.between(controllerSession.createdAt(), controllerSession.expiresAt()))
                .isEqualTo(Duration.ofHours(8));

        MvcResult display = mockMvc.perform(post("/api/classroom-display-participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(redemptionBody(displayGrant.token())))
                .andReturn();
        assertEstablishedParticipant(display, foundation.classroomSession(), "CLASSROOM_DISPLAY");
        String displayCredential = assertParticipantCookie(display);
        ParticipantSession displaySession = participantSessions
                .findByCredentialVerifier(tokenVerifier.verifierFor(RawSecurityToken.fromEncoded(displayCredential)))
                .orElseThrow();
        assertThat(displaySession.teacherAccountId()).isNull();
        assertThat(displaySession.teacherBrowserSessionId()).isNull();
        assertThat(displayCredential).isNotEqualTo(displayGrant.token()).isNotEqualTo(controllerCredential);
        assertThat(Duration.between(displaySession.createdAt(), displaySession.expiresAt()))
                .isEqualTo(Duration.ofHours(8));

        ClassroomSession unchanged = classroomSessions.findById(foundation.classroomSession().id()).orElseThrow();
        assertThat(unchanged.lifecycleState()).isEqualTo(ClassroomLifecycleState.CREATED);
        assertThat(unchanged.currentScenePosition()).isZero();
        assertThat(unchanged.revision()).isEqualTo(new Revision(0));
    }

    @Test
    void participantEstablishmentFailuresRemainNonDisclosingAndDoNotSetParticipantCookie() throws Exception {
        PairingFoundation foundation = createPairingFoundation('8', '9');
        IssuedGrant controllerGrant = issueGrant(
                foundation.authority(), foundation.classroomSession(), "TEACHER_CONTROLLER");
        IssuedGrant displayGrant = issueGrant(
                foundation.authority(), foundation.classroomSession(), "CLASSROOM_DISPLAY");

        MvcResult noTeacher = mockMvc.perform(post("/api/teacher-controller-participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(redemptionBody(controllerGrant.token())))
                .andReturn();
        assertProblem(noTeacher, 401, "TEACHER_SESSION_REQUIRED");
        assertNoParticipantCookie(noTeacher);
        MvcResult badCsrf = mockMvc.perform(post("/api/teacher-controller-participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(redemptionBody(controllerGrant.token()))
                        .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()))
                        .header("X-Penatika-CSRF", rawToken('a').expose()))
                .andReturn();
        assertProblem(badCsrf, 403, "CSRF_REJECTED");
        assertNoParticipantCookie(badCsrf);

        MvcResult wrongRole = mockMvc.perform(post("/api/teacher-controller-participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(redemptionBody(displayGrant.token()))
                        .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()))
                        .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                .andReturn();
        assertProblem(wrongRole, 403, "PAIRING_GRANT_REJECTED");
        assertNoParticipantCookie(wrongRole);
        assertThat(pairingGrants.findByCredentialVerifier(pairingVerifier(displayGrant.token()))
                        .orElseThrow().consumedAt())
                .isNull();

        MvcResult broadUnknown = mockMvc.perform(post("/api/classroom-display-participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(redemptionBody("z".repeat(50))))
                .andReturn();
        assertProblem(broadUnknown, 403, "PAIRING_GRANT_REJECTED");
        assertNoParticipantCookie(broadUnknown);
        List<String> invalidBodies = List.of(
                "{\"pairingToken\":",
                "{}",
                "[]",
                "{\"pairingToken\":null}",
                redemptionBody("two words"),
                redemptionBody("z".repeat(513)),
                "{\"pairingToken\":\"valid\",\"extra\":true}");
        for (String invalidBody : invalidBodies) {
            MvcResult invalidWire = mockMvc.perform(post("/api/classroom-display-participants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andReturn();
            assertProblem(invalidWire, 400, "REQUEST_VALIDATION_FAILED");
            assertNoParticipantCookie(invalidWire);
        }

        PairingGrant expired = createGrant(
                foundation.classroomSession(), PairingRole.CLASSROOM_DISPLAY, 'A', clock.instant().minusSeconds(301));
        MvcResult expiredResult = mockMvc.perform(post("/api/classroom-display-participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(redemptionBody(rawPairingToken('A').expose())))
                .andReturn();
        assertProblem(expiredResult, 403, "PAIRING_GRANT_REJECTED");
        assertNoParticipantCookie(expiredResult);
        PairingGrant revoked = createGrant(
                foundation.classroomSession(), PairingRole.CLASSROOM_DISPLAY, 'B', clock.instant());
        pairingGrants.revoke(foundation.classroomSession().id(), revoked.id(), clock.instant());
        MvcResult revokedResult = mockMvc.perform(post("/api/classroom-display-participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(redemptionBody(rawPairingToken('B').expose())))
                .andReturn();
        assertProblem(revokedResult, 403, "PAIRING_GRANT_REJECTED");
        assertNoParticipantCookie(revokedResult);

        TeacherAccount otherTeacher = createTeacher(
                "10000000-0000-0000-0000-000000000641",
                "10000000-0000-0000-0000-000000000642",
                "cross-teacher-http",
                TeacherAccountStatus.ACTIVE);
        SessionAuthority otherAuthority = createSessionAuthority(
                otherTeacher,
                "10000000-0000-0000-0000-000000000643",
                'C',
                'D',
                clock.instant().truncatedTo(ChronoUnit.MICROS));
        MvcResult crossTeacher = mockMvc.perform(post("/api/teacher-controller-participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(redemptionBody(controllerGrant.token()))
                        .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, otherAuthority.sessionToken()))
                        .header("X-Penatika-CSRF", otherAuthority.csrfToken().expose()))
                .andReturn();
        assertProblem(crossTeacher, 403, "PAIRING_GRANT_REJECTED");
        assertNoParticipantCookie(crossTeacher);
        assertThat(pairingGrants.findByCredentialVerifier(pairingVerifier(controllerGrant.token()))
                        .orElseThrow().consumedAt())
                .isNull();

        MvcResult displaySuccess = mockMvc.perform(post("/api/classroom-display-participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(redemptionBody(displayGrant.token())))
                .andReturn();
        assertThat(displaySuccess.getResponse().getStatus()).isEqualTo(201);
        MvcResult replay = mockMvc.perform(post("/api/classroom-display-participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(redemptionBody(displayGrant.token())))
                .andReturn();
        assertProblem(replay, 403, "PAIRING_GRANT_REJECTED");
        assertNoParticipantCookie(replay);

        PairingGrant competingGrant = createGrant(
                foundation.classroomSession(), PairingRole.CLASSROOM_DISPLAY, 'r', clock.instant());
        MvcResult occupied = mockMvc.perform(post("/api/classroom-display-participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(redemptionBody(rawPairingToken('r').expose())))
                .andReturn();
        assertProblem(occupied, 409, "PARTICIPANT_ROLE_ALREADY_ACTIVE");
        assertNoParticipantCookie(occupied);
        assertThat(pairingGrants.findByCredentialVerifier(competingGrant.credentialVerifier())
                        .orElseThrow().consumedAt())
                .isNull();
    }

    @Test
    void teacherAuthorityLossInsideRedemptionReturns401AndRollsBackGrantClaim() throws Exception {
        PairingFoundation foundation = createPairingFoundation('b', 'd');
        IssuedGrant grant = issueGrant(
                foundation.authority(), foundation.classroomSession(), "TEACHER_CONTROLLER");
        jdbcClient.sql("""
                        CREATE FUNCTION ivs05_revoke_browser_on_claim() RETURNS trigger AS $$
                        BEGIN
                            UPDATE identity_teacher_browser_session
                            SET revoked_at = NEW.consumed_at
                            WHERE id = '%s'::uuid;
                            RETURN NEW;
                        END;
                        $$ LANGUAGE plpgsql
                        """.formatted(foundation.authority().session().id().value()))
                .update();
        jdbcClient.sql("""
                        CREATE TRIGGER ivs05_revoke_browser_on_claim
                        AFTER UPDATE OF consumed_at ON classroom_pairing_grant
                        FOR EACH ROW EXECUTE FUNCTION ivs05_revoke_browser_on_claim()
                        """)
                .update();
        try {
            MvcResult result = mockMvc.perform(post("/api/teacher-controller-participants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(redemptionBody(grant.token()))
                            .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()))
                            .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                    .andReturn();

            assertProblem(result, 401, "TEACHER_SESSION_REQUIRED");
            assertNoParticipantCookie(result);
            assertThat(pairingGrants.findByCredentialVerifier(pairingVerifier(grant.token()))
                            .orElseThrow().consumedAt())
                    .isNull();
            assertThat(browserSessions.findById(foundation.authority().session().id()).orElseThrow().revokedAt())
                    .isNull();
            assertThat(rowCount("identity_participant_session")).isZero();
        } finally {
            jdbcClient.sql("DROP TRIGGER IF EXISTS ivs05_revoke_browser_on_claim ON classroom_pairing_grant")
                    .update();
            jdbcClient.sql("DROP FUNCTION IF EXISTS ivs05_revoke_browser_on_claim()")
                    .update();
        }
    }

    @Test
    void redemptionTransactionRollsBackAllPostClaimAuthorizationAndRoleFailures() {
        PairingFoundation foundation = createPairingFoundation('f', 'g');

        PairingGrant wrongRole = createGrant(
                foundation.classroomSession(), PairingRole.CLASSROOM_DISPLAY, 'h', clock.instant());
        assertThatThrownBy(() -> establishControllerParticipant.establishController(
                        PresentedPairingToken.fromWire(rawPairingToken('h').expose()),
                        foundation.teacher().id().value(),
                        foundation.authority().session().id().value()))
                .isInstanceOf(PairingGrantRejectedException.class);
        assertUnconsumed(wrongRole);

        TeacherAccount otherTeacher = createTeacher(
                "10000000-0000-0000-0000-000000000611",
                "10000000-0000-0000-0000-000000000612",
                "cross-teacher",
                TeacherAccountStatus.ACTIVE);
        PairingGrant crossTeacher = createGrant(
                foundation.classroomSession(), PairingRole.TEACHER_CONTROLLER, 'i', clock.instant());
        assertThatThrownBy(() -> establishControllerParticipant.establishController(
                        PresentedPairingToken.fromWire(rawPairingToken('i').expose()),
                        otherTeacher.id().value(),
                        UUID.randomUUID()))
                .isInstanceOf(PairingGrantRejectedException.class);
        assertUnconsumed(crossTeacher);

        ClassroomSession failed = createClassroomSession(
                foundation.teacher(), foundation.lessonVersion(), UUID.randomUUID());
        jdbcClient.sql("UPDATE classroom_session SET lifecycle_state = 'FAILED' WHERE id = :id")
                .param("id", failed.id().value())
                .update();
        PairingGrant nonPairable = createGrant(failed, PairingRole.CLASSROOM_DISPLAY, 'j', clock.instant());
        assertThatThrownBy(() -> establishDisplayParticipant.establishDisplay(
                        PresentedPairingToken.fromWire(rawPairingToken('j').expose())))
                .isInstanceOf(PairingGrantRejectedException.class);
        assertUnconsumed(nonPairable);

        PairingGrant invalidTeacher = createGrant(
                foundation.classroomSession(), PairingRole.TEACHER_CONTROLLER, 'k', clock.instant());
        assertThatThrownBy(() -> establishControllerParticipant.establishController(
                        PresentedPairingToken.fromWire(rawPairingToken('k').expose()),
                        foundation.teacher().id().value(),
                        UUID.randomUUID()))
                .isInstanceOf(TeacherSessionRequiredException.class);
        assertUnconsumed(invalidTeacher);

        ClassroomSession occupiedSession = createClassroomSession(
                foundation.teacher(), foundation.lessonVersion(), UUID.randomUUID());
        ParticipantSession occupant = ParticipantSession.display(
                new io.github.sipratama.penatika.identity.domain.ParticipantSessionId(UUID.randomUUID()),
                new io.github.sipratama.penatika.identity.domain.ClassroomSessionReference(occupiedSession.id().value()),
                "8".repeat(64),
                clock.instant());
        assertThat(participantSessions.tryCreateActive(occupant)).isTrue();
        PairingGrant roleConflict = createGrant(
                occupiedSession, PairingRole.CLASSROOM_DISPLAY, 'l', clock.instant());
        assertThatThrownBy(() -> establishDisplayParticipant.establishDisplay(
                        PresentedPairingToken.fromWire(rawPairingToken('l').expose())))
                .isInstanceOf(ParticipantRoleAlreadyActiveException.class);
        assertUnconsumed(roleConflict);
    }

    @Test
    void concurrentRedemptionEnforcesSingleUseSameRoleAndIndependentRoles() throws Exception {
        PairingFoundation foundation = createPairingFoundation('m', 'n');
        ClassroomSession sameTokenSession = createClassroomSession(
                foundation.teacher(), foundation.lessonVersion(), UUID.randomUUID());
        PairingGrant sameToken = createGrant(
                sameTokenSession, PairingRole.CLASSROOM_DISPLAY, 'o', clock.instant());
        PresentedPairingToken samePresented = PresentedPairingToken.fromWire(rawPairingToken('o').expose());
        assertThat(runConcurrently(2, () -> displayOutcome(samePresented)))
                .containsExactlyInAnyOrder("SUCCESS", "REJECTED");
        assertThat(activeParticipantCount(sameTokenSession, PairingRole.CLASSROOM_DISPLAY)).isEqualTo(1);

        ClassroomSession sameRoleSession = createClassroomSession(
                foundation.teacher(), foundation.lessonVersion(), UUID.randomUUID());
        PairingGrant first = createGrant(sameRoleSession, PairingRole.CLASSROOM_DISPLAY, 'p', clock.instant());
        PairingGrant second = createGrant(sameRoleSession, PairingRole.CLASSROOM_DISPLAY, 'q', clock.instant());
        List<String> sameRoleOutcomes = runConcurrently(List.of(
                () -> displayOutcome(PresentedPairingToken.fromWire(rawPairingToken('p').expose())),
                () -> displayOutcome(PresentedPairingToken.fromWire(rawPairingToken('q').expose()))));
        assertThat(sameRoleOutcomes).containsExactlyInAnyOrder("SUCCESS", "ROLE_ACTIVE");
        assertThat(activeParticipantCount(sameRoleSession, PairingRole.CLASSROOM_DISPLAY)).isEqualTo(1);
        assertThat(List.of(first, second).stream()
                        .filter(grant -> pairingGrants.findByCredentialVerifier(grant.credentialVerifier())
                                .orElseThrow().consumedAt() == null)
                        .count())
                .isEqualTo(1);

        ClassroomSession differentRolesSession = createClassroomSession(
                foundation.teacher(), foundation.lessonVersion(), UUID.randomUUID());
        createGrant(differentRolesSession, PairingRole.TEACHER_CONTROLLER, 's', clock.instant());
        createGrant(differentRolesSession, PairingRole.CLASSROOM_DISPLAY, 't', clock.instant());
        List<String> differentRoleOutcomes = runConcurrently(List.of(
                () -> controllerOutcome(
                        PresentedPairingToken.fromWire(rawPairingToken('s').expose()), foundation),
                () -> displayOutcome(PresentedPairingToken.fromWire(rawPairingToken('t').expose()))));
        assertThat(differentRoleOutcomes).containsExactly("SUCCESS", "SUCCESS");
        assertThat(activeParticipantCount(differentRolesSession, PairingRole.TEACHER_CONTROLLER)).isEqualTo(1);
        assertThat(activeParticipantCount(differentRolesSession, PairingRole.CLASSROOM_DISPLAY)).isEqualTo(1);
    }

    @Test
    void exactExpiredParticipantIsUnusableAndLazyCleanupAllowsNewEightHourRepair() {
        PairingFoundation foundation = createPairingFoundation('u', 'v');
        Instant exactExpiry = clock.instant().truncatedTo(ChronoUnit.MICROS);
        RawSecurityToken oldCredential = rawToken('x');
        ParticipantSession expired = ParticipantSession.display(
                new io.github.sipratama.penatika.identity.domain.ParticipantSessionId(UUID.randomUUID()),
                new io.github.sipratama.penatika.identity.domain.ClassroomSessionReference(
                        foundation.classroomSession().id().value()),
                tokenVerifier.verifierFor(oldCredential),
                exactExpiry.minus(Duration.ofHours(8)));
        assertThat(expired.expiresAt()).isEqualTo(exactExpiry);
        assertThat(participantSessions.tryCreateActive(expired)).isTrue();
        assertThat(participantAuthority.resolve(oldCredential)).isEmpty();

        PairingGrant replacementGrant = createGrant(
                foundation.classroomSession(), PairingRole.CLASSROOM_DISPLAY, 'y', clock.instant());
        EstablishedParticipant replacement = establishDisplayParticipant.establishDisplay(
                PresentedPairingToken.fromWire(rawPairingToken('y').expose()));

        ParticipantSession stale = participantSessions.findByCredentialVerifier(expired.credentialVerifier()).orElseThrow();
        assertThat(stale.revokedAt()).isNotNull();
        ParticipantSession current = participantSessions
                .findByCredentialVerifier(tokenVerifier.verifierFor(replacement.participantCredential()))
                .orElseThrow();
        assertThat(current.id()).isNotEqualTo(expired.id());
        assertThat(Duration.between(current.createdAt(), current.expiresAt())).isEqualTo(Duration.ofHours(8));
        assertThat(pairingGrants.findByCredentialVerifier(replacementGrant.credentialVerifier())
                        .orElseThrow().consumedAt())
                .isNotNull();
    }

    @Test
    void invalidatedControllerOccupantIsCleanedBeforeAuthorizedRepair() throws Exception {
        PairingFoundation foundation = createPairingFoundation('E', 'F');
        ParticipantSession staleController = ParticipantSession.controller(
                new io.github.sipratama.penatika.identity.domain.ParticipantSessionId(UUID.randomUUID()),
                new io.github.sipratama.penatika.identity.domain.ClassroomSessionReference(
                        foundation.classroomSession().id().value()),
                "7".repeat(64),
                foundation.teacher().id(),
                foundation.authority().session().id(),
                clock.instant());
        assertThat(participantSessions.tryCreateActive(staleController)).isTrue();
        assertThat(browserSessions.revoke(foundation.authority().session().id(), clock.instant())).isTrue();

        SessionAuthority replacementAuthority = createSessionAuthority(
                foundation.teacher(),
                "10000000-0000-0000-0000-000000000604",
                'G',
                'H',
                clock.instant().truncatedTo(ChronoUnit.MICROS));
        IssuedGrant replacementGrant = issueGrant(
                replacementAuthority, foundation.classroomSession(), "TEACHER_CONTROLLER");
        MvcResult result = mockMvc.perform(post("/api/teacher-controller-participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(redemptionBody(replacementGrant.token()))
                        .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, replacementAuthority.sessionToken()))
                        .header("X-Penatika-CSRF", replacementAuthority.csrfToken().expose()))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        assertThat(participantSessions.findByCredentialVerifier(staleController.credentialVerifier())
                        .orElseThrow().revokedAt())
                .isNotNull();
        String newCredential = assertParticipantCookie(result);
        ParticipantSession replacement = participantSessions.findByCredentialVerifier(
                        tokenVerifier.verifierFor(RawSecurityToken.fromEncoded(newCredential)))
                .orElseThrow();
        assertThat(replacement.id()).isNotEqualTo(staleController.id());
        assertThat(replacement.teacherBrowserSessionId()).isEqualTo(replacementAuthority.session().id());
    }

    private PairingFoundation createPairingFoundation(char sessionCharacter, char csrfCharacter) {
        TeacherAccount teacher = createTeacher(
                "10000000-0000-0000-0000-000000000601",
                "10000000-0000-0000-0000-000000000602",
                "pairing-foundation",
                TeacherAccountStatus.ACTIVE);
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        SessionAuthority authority = createSessionAuthority(
                teacher,
                "10000000-0000-0000-0000-000000000603",
                sessionCharacter,
                csrfCharacter,
                now);
        LessonVersion lessonVersion = createLessonVersion(
                teacher,
                "20000000-0000-0000-0000-000000000601",
                "20000000-0000-0000-0000-000000000602",
                LessonVersionReadiness.CLASSROOM_READY,
                true);
        ClassroomSession classroomSession = createClassroomSession(
                teacher,
                lessonVersion,
                UUID.fromString("30000000-0000-0000-0000-000000000601"));
        return new PairingFoundation(teacher, authority, lessonVersion, classroomSession);
    }

    private ClassroomSession createClassroomSession(
            TeacherAccount teacher,
            LessonVersion lessonVersion,
            UUID classroomSessionId) {
        ClassroomSession session = ClassroomSession.start(
                new ClassroomSessionId(classroomSessionId),
                teacher.id().value(),
                lessonVersion.id().value(),
                clock.instant().truncatedTo(ChronoUnit.MICROS));
        classroomSessions.create(session);
        return session;
    }

    private IssuedGrant issueGrant(
            SessionAuthority authority,
            ClassroomSession classroomSession,
            String participantRole) throws Exception {
        MvcResult result = createPairingGrantRequest(
                        authority, classroomSession.id().value().toString(), participantRole)
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        assertThat(result.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(result.getResponse().getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(result.getResponse().getHeader("Location")).isNull();
        Map<String, Object> body = JsonPath.read(result.getResponse().getContentAsString(), "$");
        assertThat(body).containsOnlyKeys("pairingGrantId", "pairingToken", "participantRole", "expiresAt");
        assertThat(body.get("participantRole")).isEqualTo(participantRole);
        return new IssuedGrant(
                (String) body.get("pairingGrantId"),
                (String) body.get("pairingToken"),
                participantRole,
                Instant.parse((String) body.get("expiresAt")));
    }

    private org.springframework.test.web.servlet.ResultActions createPairingGrantRequest(
            SessionAuthority authority,
            String classroomSessionId,
            String participantRole) throws Exception {
        return mockMvc.perform(post("/api/classroom-sessions/{id}/pairing-grants", classroomSessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"participantRole\":\"" + participantRole + "\"}")
                .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, authority.sessionToken()))
                .header("X-Penatika-CSRF", authority.csrfToken().expose()));
    }

    private org.springframework.test.web.servlet.ResultActions revokeGrant(
            SessionAuthority authority,
            ClassroomSession classroomSession,
            String pairingGrantId) throws Exception {
        return mockMvc.perform(delete(pairingGrantRevocationPath(classroomSession, pairingGrantId))
                .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, authority.sessionToken()))
                .header("X-Penatika-CSRF", authority.csrfToken().expose()));
    }

    private static String pairingGrantPath(ClassroomSession classroomSession) {
        return "/api/classroom-sessions/" + classroomSession.id().value() + "/pairing-grants";
    }

    private static String pairingGrantRevocationPath(
            ClassroomSession classroomSession, String pairingGrantId) {
        return pairingGrantPath(classroomSession) + "/" + pairingGrantId;
    }

    @Test
    void controllerReconciliationAndCommandHttpEnforceDualAuthorityAndExactContract() throws Exception {
        Ivs06Foundation foundation = createIvs06Foundation('i', 'j', 'k');
        String sessionId = foundation.classroomSession().id().value().toString();

        MvcResult reconciliation = mockMvc.perform(get(
                                "/api/classroom-sessions/{id}/controller-state", sessionId)
                        .cookie(
                                cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()),
                                cookie(ParticipantSessionCookies.COOKIE_NAME, foundation.participantToken())))
                .andReturn();
        assertThat(reconciliation.getResponse().getStatus()).isEqualTo(200);
        assertThat(reconciliation.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(reconciliation.getResponse().getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(JsonPath.<Map<String, Object>>read(
                        reconciliation.getResponse().getContentAsString(), "$"))
                .containsOnlyKeys("classroomSessionId", "revision")
                .containsEntry("classroomSessionId", sessionId)
                .containsEntry("revision", 0);
        assertThat(browserSessions.findById(foundation.authority().session().id()).orElseThrow().lastActiveAt())
                .isEqualTo(foundation.authority().session().lastActiveAt());

        assertProblem(mockMvc.perform(get(
                                "/api/classroom-sessions/{id}/controller-state", sessionId)
                        .cookie(cookie(
                                ParticipantSessionCookies.COOKIE_NAME,
                                foundation.participantToken())))
                .andReturn(), 401, "TEACHER_SESSION_REQUIRED");
        assertProblem(mockMvc.perform(get("/api/classroom-sessions/{id}/controller-state", sessionId)
                        .cookie(cookie(
                                TeacherSessionCookies.SESSION_COOKIE_NAME,
                                foundation.authority().sessionToken())))
                .andReturn(), 403, "CONTROLLER_AUTHORITY_REQUIRED");
        assertProblem(mockMvc.perform(get("/api/classroom-sessions/{id}/controller-state", "two words")
                        .cookie(cookie(
                                TeacherSessionCookies.SESSION_COOKIE_NAME,
                                foundation.authority().sessionToken())))
                .andReturn(), 400, "REQUEST_VALIDATION_FAILED");
        assertProblem(mockMvc.perform(get("/api/classroom-sessions/{id}/controller-state", "x".repeat(129))
                        .cookie(cookie(
                                TeacherSessionCookies.SESSION_COOKIE_NAME,
                                foundation.authority().sessionToken())))
                .andReturn(), 400, "REQUEST_VALIDATION_FAILED");
        assertProblem(mockMvc.perform(get("/api/classroom-sessions/{id}/controller-state", "opaque-id")
                        .cookie(cookie(
                                TeacherSessionCookies.SESSION_COOKIE_NAME,
                                foundation.authority().sessionToken())))
                .andReturn(), 404, "CLASSROOM_SESSION_NOT_FOUND");
        assertProblem(mockMvc.perform(get(
                                "/api/classroom-sessions/{id}/controller-state", UUID.randomUUID())
                        .cookie(
                                cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()),
                                cookie(ParticipantSessionCookies.COOKIE_NAME, foundation.participantToken())))
                .andReturn(), 404, "CLASSROOM_SESSION_NOT_FOUND");
        assertProblem(mockMvc.perform(get("/api/classroom-sessions/{id}/controller-state", "1-1-1-1-1")
                        .cookie(cookie(
                                TeacherSessionCookies.SESSION_COOKIE_NAME,
                                foundation.authority().sessionToken())))
                .andReturn(), 404, "CLASSROOM_SESSION_NOT_FOUND");

        TeacherAccount otherTeacher = createTeacher(
                "10000000-0000-0000-0000-000000000711",
                "10000000-0000-0000-0000-000000000712",
                "ivs06-other-teacher",
                TeacherAccountStatus.ACTIVE);
        LessonVersion otherLesson = createLessonVersion(
                otherTeacher,
                "20000000-0000-0000-0000-000000000711",
                "20000000-0000-0000-0000-000000000712",
                LessonVersionReadiness.CLASSROOM_READY,
                true);
        ClassroomSession otherSession = createClassroomSession(otherTeacher, otherLesson, UUID.randomUUID());
        assertProblem(mockMvc.perform(get(
                                "/api/classroom-sessions/{id}/controller-state",
                                otherSession.id().value())
                        .cookie(
                                cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()),
                                cookie(ParticipantSessionCookies.COOKIE_NAME, foundation.participantToken())))
                .andReturn(), 404, "CLASSROOM_SESSION_NOT_FOUND");

        RawSecurityToken displayToken = rawToken('u');
        ParticipantSession displayParticipant = ParticipantSession.display(
                new ParticipantSessionId(UUID.randomUUID()),
                new ClassroomSessionReference(foundation.classroomSession().id().value()),
                tokenVerifier.verifierFor(displayToken),
                clock.instant());
        assertThat(participantSessions.tryCreateActive(displayParticipant)).isTrue();
        assertProblem(mockMvc.perform(get("/api/classroom-sessions/{id}/controller-state", sessionId)
                        .cookie(
                                cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()),
                                cookie(ParticipantSessionCookies.COOKIE_NAME, displayToken)))
                .andReturn(), 403, "CONTROLLER_AUTHORITY_REQUIRED");

        String commandBody = commandBody("command-http-1", 0);
        assertProblem(mockMvc.perform(post(
                                "/api/classroom-sessions/{id}/commands",
                                otherSession.id().value())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commandBody)
                        .cookie(
                                cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()),
                                cookie(ParticipantSessionCookies.COOKIE_NAME, foundation.participantToken()))
                        .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                .andReturn(), 404, "CLASSROOM_SESSION_NOT_FOUND");
        assertProblem(mockMvc.perform(post("/api/classroom-sessions/{id}/commands", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commandBody)
                        .cookie(cookie(
                                ParticipantSessionCookies.COOKIE_NAME,
                                foundation.participantToken()))
                        .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                .andReturn(), 401, "TEACHER_SESSION_REQUIRED");
        assertProblem(mockMvc.perform(post("/api/classroom-sessions/{id}/commands", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commandBody)
                        .cookie(
                                cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()),
                                cookie(ParticipantSessionCookies.COOKIE_NAME, foundation.participantToken()))
                        .header("X-Penatika-CSRF", rawToken('z').expose()))
                .andReturn(), 403, "CSRF_REJECTED");
        assertProblem(mockMvc.perform(post("/api/classroom-sessions/{id}/commands", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commandBody)
                        .cookie(cookie(
                                TeacherSessionCookies.SESSION_COOKIE_NAME,
                                foundation.authority().sessionToken()))
                        .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                .andReturn(), 403, "CONTROLLER_AUTHORITY_REQUIRED");
        assertProblem(mockMvc.perform(post("/api/classroom-sessions/{id}/commands", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commandBody)
                        .cookie(
                                cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()),
                                cookie(ParticipantSessionCookies.COOKIE_NAME, displayToken))
                        .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                .andReturn(), 403, "CONTROLLER_AUTHORITY_REQUIRED");
        assertProblem(mockMvc.perform(post("/api/classroom-sessions/{id}/commands", "x".repeat(129))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commandBody)
                        .cookie(cookie(
                                TeacherSessionCookies.SESSION_COOKIE_NAME,
                                foundation.authority().sessionToken()))
                        .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                .andReturn(), 400, "REQUEST_VALIDATION_FAILED");
        assertProblem(mockMvc.perform(post("/api/classroom-sessions/{id}/commands", "1-1-1-1-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commandBody)
                        .cookie(cookie(
                                TeacherSessionCookies.SESSION_COOKIE_NAME,
                                foundation.authority().sessionToken()))
                        .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                .andReturn(), 404, "CLASSROOM_SESSION_NOT_FOUND");

        List<String> invalidBodies = List.of(
                "{\"commandId\":",
                "[]",
                "{}",
                "{\"commandId\":\"a\",\"commandType\":\"DIRECT_ACTION\",\"action\":\"NEXT\"}",
                "{\"commandId\":\"a\",\"expectedRevision\":0,\"action\":\"NEXT\"}",
                "{\"commandId\":\"a\",\"expectedRevision\":0,\"commandType\":\"DIRECT_ACTION\"}",
                "{\"commandId\":\"a\",\"expectedRevision\":0,\"commandType\":\"DIRECT_ACTION\",\"action\":\"NEXT\",\"extra\":true}",
                commandBody("two words", 0),
                commandBody("x".repeat(129), 0),
                "{\"commandId\":\"a\",\"expectedRevision\":-1,\"commandType\":\"DIRECT_ACTION\",\"action\":\"NEXT\"}",
                "{\"commandId\":\"a\",\"expectedRevision\":9007199254740992,\"commandType\":\"DIRECT_ACTION\",\"action\":\"NEXT\"}",
                "{\"commandId\":\"a\",\"expectedRevision\":0.5,\"commandType\":\"DIRECT_ACTION\",\"action\":\"NEXT\"}");
        for (String invalidBody : invalidBodies) {
            assertProblem(mockMvc.perform(post("/api/classroom-sessions/{id}/commands", sessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody)
                            .cookie(
                                    cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()),
                                    cookie(ParticipantSessionCookies.COOKIE_NAME, foundation.participantToken()))
                            .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                    .andReturn(), 400, "REQUEST_VALIDATION_FAILED");
        }
        assertValidationField(mockMvc.perform(post("/api/classroom-sessions/{id}/commands", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commandId\":\"a\",\"expectedRevision\":0,\"commandType\":\"OTHER\",\"action\":\"NEXT\"}")
                        .cookie(cookie(
                                TeacherSessionCookies.SESSION_COOKIE_NAME,
                                foundation.authority().sessionToken())))
                .andReturn(), "commandType", "UNSUPPORTED_VALUE");
        assertValidationField(mockMvc.perform(post("/api/classroom-sessions/{id}/commands", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commandId\":\"a\",\"expectedRevision\":0,\"commandType\":\"DIRECT_ACTION\",\"action\":\"PREVIOUS\"}")
                        .cookie(cookie(
                                TeacherSessionCookies.SESSION_COOKIE_NAME,
                                foundation.authority().sessionToken())))
                .andReturn(), "action", "UNSUPPORTED_VALUE");

        String supplementaryCommandId = "\uD83D\uDE80".repeat(128);
        displayMutationGate.acknowledge(foundation.classroomSession().id(), new Revision(0));
        MvcResult accepted = mockMvc.perform(post("/api/classroom-sessions/{id}/commands", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commandBody(supplementaryCommandId, 0))
                        .cookie(
                                cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()),
                                cookie(ParticipantSessionCookies.COOKIE_NAME, foundation.participantToken()))
                        .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                .andReturn();
        assertThat(accepted.getResponse().getStatus()).isEqualTo(200);
        assertThat(JsonPath.<Map<String, Object>>read(accepted.getResponse().getContentAsString(), "$"))
                .containsOnlyKeys("classroomSessionId", "commandId", "resultingRevision")
                .containsEntry("classroomSessionId", sessionId)
                .containsEntry("commandId", supplementaryCommandId)
                .containsEntry("resultingRevision", 1);
        ClassroomSession advanced = classroomSessions.findById(foundation.classroomSession().id()).orElseThrow();
        assertThat(advanced.currentScenePosition()).isEqualTo(1);
        assertThat(advanced.revision()).isEqualTo(new Revision(1));
        assertThat(advanced.lifecycleState()).isEqualTo(foundation.classroomSession().lifecycleState());
        AcceptedCommandOutcome stored = commandOutcomes
                .findByCommandIdentity(foundation.classroomSession().id(), supplementaryCommandId)
                .orElseThrow();
        assertThat(stored.teacherAccountId()).isEqualTo(foundation.teacher().id().value());
        assertThat(stored.teacherBrowserSessionId()).isEqualTo(foundation.authority().session().id().value());
        assertThat(stored.controllerParticipantSessionId()).isEqualTo(foundation.participant().id().value());

        displayMutationGate.clear();
        MvcResult replay = mockMvc.perform(post("/api/classroom-sessions/{id}/commands", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commandBody(supplementaryCommandId, 0))
                        .cookie(
                                cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()),
                                cookie(ParticipantSessionCookies.COOKIE_NAME, foundation.participantToken()))
                        .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                .andReturn();
        assertThat(replay.getResponse().getStatus()).isEqualTo(200);
        assertThat(JsonPath.<Integer>read(replay.getResponse().getContentAsString(), "$.resultingRevision"))
                .isEqualTo(1);
        assertProblem(mockMvc.perform(post("/api/classroom-sessions/{id}/commands", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commandBody(supplementaryCommandId, 1))
                        .cookie(
                                cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()),
                                cookie(ParticipantSessionCookies.COOKIE_NAME, foundation.participantToken()))
                        .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                .andReturn(), 409, "COMMAND_ID_REUSE_CONFLICT");
        assertProblem(mockMvc.perform(post("/api/classroom-sessions/{id}/commands", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commandBody("unseen-stale", 0))
                        .cookie(
                                cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, foundation.authority().sessionToken()),
                                cookie(ParticipantSessionCookies.COOKIE_NAME, foundation.participantToken()))
                        .header("X-Penatika-CSRF", foundation.authority().csrfToken().expose()))
                .andReturn(), 409, "STALE_REVISION");
        assertThat(acceptedCommandCount(foundation.classroomSession())).isEqualTo(1);
    }

    @Test
    void postgresSerializesEquivalentAndCompetingCommandsAtMostOnce() throws Exception {
        Ivs06Foundation equivalent = createIvs06Foundation('l', 'm', 'n');
        displayMutationGate.acknowledge(equivalent.classroomSession().id(), new Revision(0));
        ClassroomCommandRequest sameRequest = commandRequest(equivalent, "concurrent-same", 0);

        List<ClassroomCommandResult> sameResults = runConcurrently(
                2, () -> executeClassroomCommand.execute(sameRequest));

        assertThat(sameResults).extracting(ClassroomCommandResult::resultingRevision)
                .containsExactly(1L, 1L);
        assertThat(classroomSessions.findById(equivalent.classroomSession().id()).orElseThrow().revision())
                .isEqualTo(new Revision(1));
        assertThat(acceptedCommandCount(equivalent.classroomSession())).isEqualTo(1);
        assertThatThrownBy(() -> executeClassroomCommand.execute(
                        commandRequest(equivalent, "concurrent-same", 1)))
                .isInstanceOf(CommandIdReuseConflictException.class);

        cleanProductTables();
        Ivs06Foundation competing = createIvs06Foundation('o', 'p', 'q');
        displayMutationGate.acknowledge(competing.classroomSession().id(), new Revision(0));
        List<String> outcomes = runConcurrently(List.of(
                () -> commandOutcome(commandRequest(competing, "command-a", 0)),
                () -> commandOutcome(commandRequest(competing, "command-b", 0))));
        assertThat(outcomes).containsExactlyInAnyOrder("SUCCESS", "STALE_REVISION");
        ClassroomSession durable = classroomSessions.findById(competing.classroomSession().id()).orElseThrow();
        assertThat(durable.currentScenePosition()).isEqualTo(1);
        assertThat(durable.revision()).isEqualTo(new Revision(1));
        assertThat(acceptedCommandCount(competing.classroomSession())).isEqualTo(1);
    }

    @Test
    void acceptedOutcomeFailureRollsBackTheClassroomMutation() {
        Ivs06Foundation foundation = createIvs06Foundation('r', 's', 't');
        displayMutationGate.acknowledge(foundation.classroomSession().id(), new Revision(0));
        jdbcClient.sql("""
                        CREATE FUNCTION test_reject_accepted_command() RETURNS trigger AS $$
                        BEGIN
                            IF NEW.command_id = 'force-rollback' THEN
                                RAISE EXCEPTION 'forced accepted outcome failure';
                            END IF;
                            RETURN NEW;
                        END;
                        $$ LANGUAGE plpgsql
                        """).update();
        jdbcClient.sql("""
                        CREATE TRIGGER test_reject_accepted_command_trigger
                        BEFORE INSERT ON classroom_accepted_command
                        FOR EACH ROW EXECUTE FUNCTION test_reject_accepted_command()
                        """).update();
        try {
            assertThatThrownBy(() -> executeClassroomCommand.execute(
                            commandRequest(foundation, "force-rollback", 0)))
                    .isInstanceOf(RuntimeException.class);
            ClassroomSession unchanged = classroomSessions
                    .findById(foundation.classroomSession().id())
                    .orElseThrow();
            assertThat(unchanged.currentScenePosition()).isZero();
            assertThat(unchanged.revision()).isEqualTo(new Revision(0));
            assertThat(acceptedCommandCount(foundation.classroomSession())).isZero();
        } finally {
            jdbcClient.sql("DROP TRIGGER test_reject_accepted_command_trigger ON classroom_accepted_command")
                    .update();
            jdbcClient.sql("DROP FUNCTION test_reject_accepted_command()")
                    .update();
        }
    }

    private Ivs06Foundation createIvs06Foundation(
            char sessionCharacter, char csrfCharacter, char participantCharacter) {
        TeacherAccount teacher = createTeacher(
                "10000000-0000-0000-0000-000000000701",
                "10000000-0000-0000-0000-000000000702",
                "ivs06-teacher",
                TeacherAccountStatus.ACTIVE);
        SessionAuthority authority = createSessionAuthority(
                teacher,
                "10000000-0000-0000-0000-000000000703",
                sessionCharacter,
                csrfCharacter,
                clock.instant().truncatedTo(ChronoUnit.MICROS));
        LessonVersionFixtureBuilder.Fixture lessonFixture = new LessonVersionFixtureBuilder()
                .withTeacherAccountId(teacher.id().value())
                .build();
        lessonVersions.createImmutableVersion(lessonFixture.lesson(), lessonFixture.version());
        ClassroomSession classroomSession = new ClassroomSessionFixtureBuilder()
                .withId(UUID.fromString("30000000-0000-0000-0000-000000000701"))
                .withReferences(teacher.id().value(), lessonFixture.version().id().value())
                .build();
        classroomSessions.create(classroomSession);
        RawSecurityToken participantToken = rawToken(participantCharacter);
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        ParticipantSession participant = ParticipantSession.controller(
                new ParticipantSessionId(UUID.fromString("10000000-0000-0000-0000-000000000704")),
                new ClassroomSessionReference(classroomSession.id().value()),
                tokenVerifier.verifierFor(participantToken),
                teacher.id(),
                authority.session().id(),
                now);
        assertThat(participantSessions.tryCreateActive(participant)).isTrue();
        return new Ivs06Foundation(
                teacher, authority, lessonFixture.version(), classroomSession, participant, participantToken);
    }

    private ClassroomCommandRequest commandRequest(
            Ivs06Foundation foundation, String commandId, long expectedRevision) {
        return new ClassroomCommandRequest(
                foundation.classroomSession().id().value(),
                commandId,
                expectedRevision,
                foundation.teacher().id().value(),
                foundation.authority().session().id().value(),
                Optional.of(foundation.participantToken()));
    }

    private String commandOutcome(ClassroomCommandRequest request) {
        try {
            executeClassroomCommand.execute(request);
            return "SUCCESS";
        } catch (StaleRevisionException exception) {
            return "STALE_REVISION";
        }
    }

    private int acceptedCommandCount(ClassroomSession classroomSession) {
        return jdbcClient.sql("""
                        SELECT count(*) FROM classroom_accepted_command
                        WHERE classroom_session_id = :classroomSessionId
                        """)
                .param("classroomSessionId", classroomSession.id().value())
                .query(Integer.class)
                .single();
    }

    private static String commandBody(String commandId, long expectedRevision) {
        return "{\"commandId\":\"" + commandId + "\",\"expectedRevision\":" + expectedRevision
                + ",\"commandType\":\"DIRECT_ACTION\",\"action\":\"NEXT\"}";
    }

    private PairingGrant createGrant(
            ClassroomSession classroomSession,
            PairingRole role,
            char tokenCharacter,
            Instant issuedAt) {
        RawPairingToken token = rawPairingToken(tokenCharacter);
        PairingGrant grant = PairingGrant.issue(
                new PairingGrantId(UUID.randomUUID()),
                classroomSession.id(),
                role,
                pairingTokenVerifier.verifierFor(token),
                issuedAt.truncatedTo(ChronoUnit.MICROS));
        pairingGrants.create(grant);
        return grant;
    }

    private String displayOutcome(PresentedPairingToken token) {
        try {
            establishDisplayParticipant.establishDisplay(token);
            return "SUCCESS";
        } catch (PairingGrantRejectedException exception) {
            return "REJECTED";
        } catch (ParticipantRoleAlreadyActiveException exception) {
            return "ROLE_ACTIVE";
        }
    }

    private String controllerOutcome(PresentedPairingToken token, PairingFoundation foundation) {
        try {
            establishControllerParticipant.establishController(
                    token,
                    foundation.teacher().id().value(),
                    foundation.authority().session().id().value());
            return "SUCCESS";
        } catch (PairingGrantRejectedException exception) {
            return "REJECTED";
        } catch (ParticipantRoleAlreadyActiveException exception) {
            return "ROLE_ACTIVE";
        }
    }

    private int activeParticipantCount(ClassroomSession session, PairingRole role) {
        return jdbcClient.sql("""
                        SELECT count(*)
                        FROM identity_participant_session
                        WHERE classroom_session_id = :classroomSessionId
                          AND participant_role = :participantRole
                          AND revoked_at IS NULL
                        """)
                .param("classroomSessionId", session.id().value())
                .param("participantRole", role.name())
                .query(Integer.class)
                .single();
    }

    private void assertUnconsumed(PairingGrant grant) {
        assertThat(pairingGrants.findByCredentialVerifier(grant.credentialVerifier())
                        .orElseThrow().consumedAt())
                .isNull();
    }

    private static void assertEstablishedParticipant(
            MvcResult result,
            ClassroomSession classroomSession,
            String participantRole) throws Exception {
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        assertThat(result.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(result.getResponse().getHeader("Location")).isNull();
        Map<String, Object> body = JsonPath.read(result.getResponse().getContentAsString(), "$");
        assertThat(body).containsOnlyKeys("classroomSessionId", "participantRole");
        assertThat(body.get("classroomSessionId")).isEqualTo(classroomSession.id().value().toString());
        assertThat(body.get("participantRole")).isEqualTo(participantRole);
    }

    private static String assertParticipantCookie(MvcResult result) {
        String header = result.getResponse().getHeaders("Set-Cookie").stream()
                .filter(value -> value.startsWith(ParticipantSessionCookies.COOKIE_NAME + "="))
                .findFirst()
                .orElseThrow();
        assertThat(header)
                .contains("Secure", "HttpOnly", "Path=/", "SameSite=Strict")
                .doesNotContain("Domain=");
        java.util.regex.Matcher maxAge = java.util.regex.Pattern.compile("Max-Age=(\\d+)").matcher(header);
        assertThat(maxAge.find()).isTrue();
        assertThat(Long.parseLong(maxAge.group(1))).isBetween(28_790L, 28_800L);
        int valueStart = ParticipantSessionCookies.COOKIE_NAME.length() + 1;
        return header.substring(valueStart, header.indexOf(';', valueStart));
    }

    private static void assertNoParticipantCookie(MvcResult result) {
        assertThat(result.getResponse().getHeaders("Set-Cookie"))
                .noneMatch(value -> value.startsWith(ParticipantSessionCookies.COOKIE_NAME + "="));
    }

    private static String redemptionBody(String pairingToken) {
        return "{\"pairingToken\":\"" + pairingToken + "\"}";
    }

    private String pairingVerifier(String token) {
        return pairingTokenVerifier.verifierFor(PresentedPairingToken.fromWire(token));
    }

    private static RawPairingToken rawPairingToken(char character) {
        return RawPairingToken.fromGenerated(String.valueOf(character).repeat(43));
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

    private org.springframework.test.web.servlet.ResultActions startClassroomSession(
            SessionAuthority authority, String lessonVersionId) throws Exception {
        return mockMvc.perform(classroomSessionStartRequest(authority, lessonVersionId)
                .header("X-Penatika-CSRF", authority.csrfToken().expose()));
    }

    private MockHttpServletRequestBuilder classroomSessionStartRequest(
            SessionAuthority authority, String lessonVersionId) {
        return post("/api/classroom-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lessonVersionId\":\"" + lessonVersionId + "\"}")
                .cookie(cookie(TeacherSessionCookies.SESSION_COOKIE_NAME, authority.sessionToken()));
    }

    private SessionAuthority createSessionAuthority(
            TeacherAccount teacher,
            String sessionId,
            char sessionCharacter,
            char csrfCharacter,
            Instant now) {
        RawSecurityToken sessionToken = rawToken(sessionCharacter);
        RawSecurityToken csrfToken = rawToken(csrfCharacter);
        TeacherBrowserSession session = runtimeSession(
                sessionId,
                teacher,
                sessionToken,
                csrfToken,
                now.minusSeconds(300),
                now.minusSeconds(120),
                now.plusSeconds(600),
                now.plusSeconds(3600),
                null);
        browserSessions.create(session);
        return new SessionAuthority(session, sessionToken, csrfToken);
    }

    private LessonVersion createLessonVersion(
            TeacherAccount teacher,
            String lessonIdValue,
            String versionIdValue,
            LessonVersionReadiness readiness,
            boolean includeContent) {
        LessonId lessonId = new LessonId(UUID.fromString(lessonIdValue));
        LessonVersionId versionId = new LessonVersionId(UUID.fromString(versionIdValue));
        Lesson lesson = new Lesson(lessonId, teacher.id().value(), Instant.parse("2026-09-11T00:00:00Z"));
        List<LessonScene> scenes = List.of();
        if (includeContent) {
            LessonSceneId sceneId = new LessonSceneId(incrementLastByte(versionId.value(), 1));
            scenes = List.of(new LessonScene(
                    sceneId,
                    versionId,
                    0,
                    List.of(new SceneBlock(
                            new SceneBlockId(incrementLastByte(versionId.value(), 2)),
                            sceneId,
                            0,
                            SceneBlockType.PLAIN_TEXT,
                            "Classroom-ready integration content"))));
        }
        LessonVersion version = new LessonVersion(
                versionId,
                lessonId,
                readiness,
                Instant.parse("2026-09-11T00:01:00Z"),
                scenes);
        lessonVersions.createImmutableVersion(lesson, version);
        return version;
    }

    private static UUID incrementLastByte(UUID value, long increment) {
        return new UUID(value.getMostSignificantBits(), value.getLeastSignificantBits() + increment);
    }

    private int rowCount(String table) {
        String query = switch (table) {
            case "classroom_pairing_grant" -> "SELECT count(*) FROM classroom_pairing_grant";
            case "identity_participant_session" -> "SELECT count(*) FROM identity_participant_session";
            case "classroom_session" -> "SELECT count(*) FROM classroom_session";
            default -> throw new IllegalArgumentException("Unsupported table");
        };
        return jdbcClient.sql(query).query(Integer.class).single();
    }

    private static void assertProblem(MvcResult result, int status, String code) throws Exception {
        assertThat(result.getResponse().getStatus()).isEqualTo(status);
        assertThat(result.getResponse().getContentType()).isEqualTo("application/problem+json");
        assertThat(JsonPath.<String>read(result.getResponse().getContentAsString(), "$.code"))
                .isEqualTo(code);
        assertThat(JsonPath.<Integer>read(result.getResponse().getContentAsString(), "$.status"))
                .isEqualTo(status);
    }

    private static void assertValidationField(
            MvcResult result, String field, String fieldCode) throws Exception {
        assertProblem(result, 400, "REQUEST_VALIDATION_FAILED");
        assertThat(JsonPath.<String>read(result.getResponse().getContentAsString(), "$.fieldErrors[0].field"))
                .isEqualTo(field);
        assertThat(JsonPath.<String>read(result.getResponse().getContentAsString(), "$.fieldErrors[0].code"))
                .isEqualTo(fieldCode);
    }

    private TeacherAccount createTeacher(
            String teacherId,
            String linkId,
            String subject,
            TeacherAccountStatus status) {
        TeacherAccount teacher = new TeacherAccount(
                new TeacherAccountId(UUID.fromString(teacherId)),
                status,
                Instant.parse("2026-01-01T00:00:00Z"));
        ExternalIdentityLink link = new ExternalIdentityLink(
                new ExternalIdentityLinkId(UUID.fromString(linkId)),
                teacher.id(),
                "https://identity.integration.test",
                subject,
                Instant.parse("2026-01-01T00:00:01Z"));
        teacherIdentities.create(teacher, link);
        return teacher;
    }

    private TeacherBrowserSession runtimeSession(
            String id,
            TeacherAccount teacher,
            RawSecurityToken credential,
            RawSecurityToken csrf,
            Instant createdAt,
            Instant lastActiveAt,
            Instant idleExpiresAt,
            Instant absoluteExpiresAt,
            Instant revokedAt) {
        return new TeacherBrowserSession(
                new TeacherBrowserSessionId(UUID.fromString(id)),
                teacher.id(),
                tokenVerifier.verifierFor(credential),
                tokenVerifier.verifierFor(csrf),
                createdAt,
                lastActiveAt,
                idleExpiresAt,
                absoluteExpiresAt,
                revokedAt);
    }

    private void assertTeacherSessionRequired(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult result = mockMvc.perform(request).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
        assertThat(result.getResponse().getContentType()).isEqualTo("application/problem+json");
        assertThat(result.getResponse().getContentAsString()).isEqualTo(
                "{\"type\":\"about:blank\",\"title\":\"Unauthorized\",\"status\":401,"
                        + "\"detail\":\"An authenticated Teacher session is required.\","
                        + "\"code\":\"TEACHER_SESSION_REQUIRED\"}");
        assertThat(result.getResponse().getHeaders("Set-Cookie"))
                .hasSize(2)
                .allSatisfy(header -> assertThat(header)
                        .contains("Max-Age=0", "Secure", "HttpOnly", "Path=/", "SameSite=Strict")
                        .doesNotContain("Domain="));
    }

    private static void assertRecoveryCookie(MvcResult result, String expectedValue) {
        assertThat(result.getResponse().getHeaders("Set-Cookie"))
                .singleElement()
                .satisfies(header -> assertThat(header)
                        .startsWith(TeacherSessionCookies.CSRF_RECOVERY_COOKIE_NAME + "=" + expectedValue)
                        .contains("Secure", "HttpOnly", "Path=/", "SameSite=Strict")
                        .doesNotContain("Domain="));
    }

    private static jakarta.servlet.http.Cookie cookie(String name, RawSecurityToken token) {
        return new jakarta.servlet.http.Cookie(name, token.expose());
    }

    private static RawSecurityToken rawToken(char character) {
        return RawSecurityToken.fromEncoded(String.valueOf(character).repeat(43));
    }

    private static OAuth2AuthenticationToken oidcAuthentication(Instant now) {
        OidcIdToken idToken = new OidcIdToken(
                "upstream-id-token",
                now.minusSeconds(30),
                now.plusSeconds(300),
                Map.of("iss", "https://identity.integration.test", "sub", "oidc-only-subject"));
        DefaultOidcUser user = new DefaultOidcUser(List.of(), idToken);
        return new OAuth2AuthenticationToken(user, user.getAuthorities(), "test");
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
            return futures.stream().map(FirstProtectedSlicePersistenceIT::getFuture).toList();
        }
    }

    private static <T> T getFuture(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for concurrent test operation", exception);
        } catch (java.util.concurrent.ExecutionException exception) {
            throw new IllegalStateException("Concurrent test operation failed", exception.getCause());
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ExactRevisionDisplayGateConfiguration {

        @Bean
        @Primary
        ExactRevisionDisplayGate exactRevisionDisplayGate() {
            return new ExactRevisionDisplayGate();
        }
    }

    static final class ExactRevisionDisplayGate implements DisplayMutationGatePort {

        private final Set<String> acknowledgements = ConcurrentHashMap.newKeySet();

        void acknowledge(ClassroomSessionId classroomSessionId, Revision revision) {
            acknowledgements.add(key(classroomSessionId, revision));
        }

        void clear() {
            acknowledgements.clear();
        }

        @Override
        public boolean isMutationPermitted(
                ClassroomSessionId classroomSessionId, Revision currentRevision) {
            return acknowledgements.contains(key(classroomSessionId, currentRevision));
        }

        private static String key(ClassroomSessionId classroomSessionId, Revision revision) {
            return classroomSessionId.value() + ":" + revision.value();
        }
    }

    private record Foundation(
            TeacherAccount teacher,
            ExternalIdentityLink identityLink,
            TeacherBrowserSession browserSession,
            LessonVersion lessonVersion,
            ClassroomSession classroomSession) {}

    private record SessionAuthority(
            TeacherBrowserSession session,
            RawSecurityToken sessionToken,
            RawSecurityToken csrfToken) {}

    private record PairingFoundation(
            TeacherAccount teacher,
            SessionAuthority authority,
            LessonVersion lessonVersion,
            ClassroomSession classroomSession) {}

    private record Ivs06Foundation(
            TeacherAccount teacher,
            SessionAuthority authority,
            LessonVersion lessonVersion,
            ClassroomSession classroomSession,
            ParticipantSession participant,
            RawSecurityToken participantToken) {}

    private record IssuedGrant(
            String id,
            String token,
            String participantRole,
            Instant expiresAt) {}
}
