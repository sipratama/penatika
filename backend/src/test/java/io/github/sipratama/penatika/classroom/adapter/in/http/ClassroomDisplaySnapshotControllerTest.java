package io.github.sipratama.penatika.classroom.adapter.in.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.jayway.jsonpath.JsonPath;

import io.github.sipratama.penatika.classroom.application.DisplayAuthorityRequiredException;
import io.github.sipratama.penatika.classroom.application.DisplaySessionRequiredException;
import io.github.sipratama.penatika.classroom.application.model.ClassroomDisplayBlock;
import io.github.sipratama.penatika.classroom.application.model.ClassroomDisplayProjection;
import io.github.sipratama.penatika.classroom.application.model.ClassroomDisplayScene;
import io.github.sipratama.penatika.classroom.application.port.in.GetClassroomDisplaySnapshotUseCase;
import io.github.sipratama.penatika.identity.application.model.RawSecurityToken;

@ExtendWith(MockitoExtension.class)
class ClassroomDisplaySnapshotControllerTest {

    private static final String SESSION_ID = "30000000-0000-0000-0000-000000000901";
    private static final RawSecurityToken TOKEN = RawSecurityToken.fromEncoded("d".repeat(43));

    @Mock private GetClassroomDisplaySnapshotUseCase getSnapshot;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var controller = new ClassroomDisplaySnapshotController(
                getSnapshot, new ParticipantSessionCookies());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ClassroomSessionExceptionHandler())
                .build();
    }

    @Test
    void returnsNoStoreJsonWithTheExactClosedProjectionShape() throws Exception {
        when(getSnapshot.getSnapshot(eq(SESSION_ID), any()))
                .thenReturn(new ClassroomDisplayProjection(
                        "1.0",
                        SESSION_ID,
                        4,
                        new ClassroomDisplayScene(
                                UUID.randomUUID().toString(),
                                1,
                                List.of(new ClassroomDisplayBlock("PLAIN_TEXT", "A < B & \uD83E\uDDEE")))));

        MvcResult result = mockMvc.perform(get(
                                "/api/classroom-sessions/{id}/display-snapshot", SESSION_ID)
                        .cookie(new jakarta.servlet.http.Cookie(
                                ParticipantSessionCookies.COOKIE_NAME, TOKEN.expose())))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(result.getResponse().getContentType()).isEqualTo("application/json");
        assertThat(result.getResponse().getHeader("Cache-Control")).isEqualTo("no-store");
        var body = JsonPath.<java.util.Map<String, Object>>read(
                result.getResponse().getContentAsString(), "$");
        assertThat(body).containsOnlyKeys("schemaVersion", "classroomSessionId", "revision", "scene");
        assertThat(JsonPath.<java.util.Map<String, Object>>read(
                        result.getResponse().getContentAsString(), "$.scene"))
                .containsOnlyKeys("sceneId", "position", "blocks");
        assertThat(JsonPath.<java.util.Map<String, Object>>read(
                        result.getResponse().getContentAsString(), "$.scene.blocks[0]"))
                .containsOnlyKeys("type", "text");
    }

    @Test
    void rejectsWireInvalidOpaqueIdsBeforeApplicationAuthorization() throws Exception {
        for (String invalid : List.of("two words", "x".repeat(129))) {
            MvcResult result = mockMvc.perform(get(
                                    "/api/classroom-sessions/{id}/display-snapshot", invalid)
                            .cookie(new jakarta.servlet.http.Cookie(
                                    ParticipantSessionCookies.COOKIE_NAME, TOKEN.expose())))
                    .andReturn();
            assertThat(result.getResponse().getStatus()).isEqualTo(400);
            assertThat(JsonPath.<String>read(result.getResponse().getContentAsString(), "$.code"))
                    .isEqualTo("REQUEST_VALIDATION_FAILED");
        }
    }

    @Test
    void rejectsNoBreakSpaceInClassroomSessionIdBeforeCallingTheApplication() throws Exception {
        MvcResult result = mockMvc.perform(get(
                                "/api/classroom-sessions/{id}/display-snapshot", "abc\u00A0def")
                        .cookie(new jakarta.servlet.http.Cookie(
                                ParticipantSessionCookies.COOKIE_NAME, TOKEN.expose())))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(JsonPath.<String>read(result.getResponse().getContentAsString(), "$.code"))
                .isEqualTo("REQUEST_VALIDATION_FAILED");
        assertThat(JsonPath.<String>read(
                        result.getResponse().getContentAsString(), "$.fieldErrors[0].field"))
                .isEqualTo("classroomSessionId");
        verifyNoInteractions(getSnapshot);
    }

    @Test
    void mapsParticipantFailuresToContractedNonDisclosingProblems() throws Exception {
        when(getSnapshot.getSnapshot(SESSION_ID, Optional.empty()))
                .thenThrow(new DisplaySessionRequiredException());
        MvcResult missing = mockMvc.perform(get(
                        "/api/classroom-sessions/{id}/display-snapshot", SESSION_ID))
                .andReturn();
        assertThat(missing.getResponse().getStatus()).isEqualTo(401);
        assertThat(JsonPath.<String>read(missing.getResponse().getContentAsString(), "$.code"))
                .isEqualTo("DISPLAY_SESSION_REQUIRED");

        when(getSnapshot.getSnapshot(eq(SESSION_ID), any()))
                .thenThrow(new DisplayAuthorityRequiredException());
        MvcResult wrongRole = mockMvc.perform(get(
                                "/api/classroom-sessions/{id}/display-snapshot", SESSION_ID)
                        .cookie(new jakarta.servlet.http.Cookie(
                                ParticipantSessionCookies.COOKIE_NAME, TOKEN.expose())))
                .andReturn();
        assertThat(wrongRole.getResponse().getStatus()).isEqualTo(403);
        assertThat(JsonPath.<String>read(wrongRole.getResponse().getContentAsString(), "$.code"))
                .isEqualTo("DISPLAY_AUTHORITY_REQUIRED");
    }
}
