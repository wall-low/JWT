package com.web_site.JWT.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web_site.JWT.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Отвечает на запросы без действующего токена.
 *
 * По умолчанию Spring Security отдаёт 403 Forbidden, но по смыслу это 401:
 * клиент не представился, а не «представился и ему отказано».
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse body = ErrorResponse.of(401, "Требуется действующий токен доступа");

        objectMapper.writeValue(response.getWriter(), body);
    }
}
