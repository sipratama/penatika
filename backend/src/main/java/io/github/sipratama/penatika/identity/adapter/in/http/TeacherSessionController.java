package io.github.sipratama.penatika.identity.adapter.in.http;

import java.time.Clock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.sipratama.penatika.identity.adapter.in.security.TeacherSessionCookies;
import io.github.sipratama.penatika.identity.adapter.in.security.TeacherSessionPrincipal;
import io.github.sipratama.penatika.identity.application.model.TeacherSessionBootstrap;
import io.github.sipratama.penatika.identity.application.port.in.BootstrapTeacherBrowserSessionUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@ConditionalOnProperty(prefix = "penatika.persistence", name = "enabled", havingValue = "true")
public final class TeacherSessionController {

    private final BootstrapTeacherBrowserSessionUseCase bootstrapSession;
    private final TeacherSessionCookies cookies;
    private final Clock clock;

    public TeacherSessionController(
            BootstrapTeacherBrowserSessionUseCase bootstrapSession,
            TeacherSessionCookies cookies,
            Clock clock) {
        this.bootstrapSession = bootstrapSession;
        this.cookies = cookies;
        this.clock = clock;
    }

    @GetMapping(path = "/api/teacher-session", produces = "application/json")
    public ResponseEntity<TeacherSessionResponse> getTeacherSession(
            @AuthenticationPrincipal TeacherSessionPrincipal principal,
            HttpServletRequest request,
            HttpServletResponse response) {
        TeacherSessionBootstrap bootstrap = bootstrapSession.bootstrap(
                principal.authenticatedSession(), cookies.readCsrfRecoveryToken(request));
        if (bootstrap.recoveryCookieReplaced()) {
            cookies.setCsrfRecoveryCookie(
                    response, bootstrap.csrfToken(), bootstrap.absoluteExpiresAt(), clock.instant());
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new TeacherSessionResponse(bootstrap.csrfToken().expose()));
    }
}
