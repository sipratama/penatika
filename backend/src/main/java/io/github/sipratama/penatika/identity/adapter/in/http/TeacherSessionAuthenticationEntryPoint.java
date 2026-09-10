package io.github.sipratama.penatika.identity.adapter.in.http;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import io.github.sipratama.penatika.identity.adapter.in.security.TeacherSessionCookies;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class TeacherSessionAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String BODY = """
            {"type":"about:blank","title":"Unauthorized","status":401,"detail":"An authenticated Teacher session is required.","code":"TEACHER_SESSION_REQUIRED"}
            """;

    private final TeacherSessionCookies cookies;

    public TeacherSessionAuthenticationEntryPoint(TeacherSessionCookies cookies) {
        this.cookies = cookies;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException) throws IOException {
        cookies.clear(response);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(BODY.strip());
    }
}
