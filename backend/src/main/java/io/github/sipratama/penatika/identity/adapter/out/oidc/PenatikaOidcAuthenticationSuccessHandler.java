package io.github.sipratama.penatika.identity.adapter.out.oidc;

import java.io.IOException;
import java.time.Clock;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import io.github.sipratama.penatika.identity.adapter.in.security.TeacherSessionCookies;
import io.github.sipratama.penatika.identity.application.TeacherAuthenticationRejectedException;
import io.github.sipratama.penatika.identity.application.model.EstablishedTeacherSession;
import io.github.sipratama.penatika.identity.application.model.ExternalTeacherIdentity;
import io.github.sipratama.penatika.identity.application.port.in.EstablishTeacherBrowserSessionUseCase;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public final class PenatikaOidcAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final EstablishTeacherBrowserSessionUseCase establishSession;
    private final TeacherSessionCookies cookies;
    private final Clock clock;

    public PenatikaOidcAuthenticationSuccessHandler(
            EstablishTeacherBrowserSessionUseCase establishSession,
            TeacherSessionCookies cookies,
            Clock clock) {
        this.establishSession = establishSession;
        this.cookies = cookies;
        this.clock = clock;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        try {
            OidcUser oidcUser = oidcUser(authentication);
            EstablishedTeacherSession session = establishSession.establish(
                    new ExternalTeacherIdentity(
                            oidcUser.getIssuer().toString(), oidcUser.getSubject()),
                    cookies.readSessionCredential(request));
            cookies.setSessionCookies(
                    response,
                    session.sessionCredential(),
                    session.csrfToken(),
                    session.absoluteExpiresAt(),
                    clock.instant());
            clearTransientAuthority(request);
            response.sendRedirect("/");
        } catch (TeacherAuthenticationRejectedException | IllegalArgumentException exception) {
            clearTransientAuthority(request);
            cookies.clear(response);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private OidcUser oidcUser(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2)
                || !(oauth2.getPrincipal() instanceof OidcUser oidcUser)
                || oidcUser.getIssuer() == null
                || oidcUser.getSubject() == null) {
            throw new TeacherAuthenticationRejectedException();
        }
        return oidcUser;
    }

    private void clearTransientAuthority(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
            httpSession.invalidate();
        }
    }
}
