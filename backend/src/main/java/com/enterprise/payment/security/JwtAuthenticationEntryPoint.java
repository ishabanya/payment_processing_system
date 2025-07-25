package com.enterprise.payment.security;

import com.enterprise.payment.dto.response.ApiResponse;
import com.enterprise.payment.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        String errorId = UUID.randomUUID().toString();
        log.warn("Unauthorized access attempt - Error ID: {}, Path: {}, Message: {}", 
                errorId, request.getRequestURI(), authException.getMessage());

        ErrorResponse error = new ErrorResponse(
                errorId,
                "UNAUTHORIZED",
                "Authentication required",
                OffsetDateTime.now(),
                request.getRequestURI(),
                null
        );

        ApiResponse<Object> apiResponse = new ApiResponse<>(
                false,
                "Authentication required",
                null,
                error,
                null,
                null
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Add CORS headers
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With");
        
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}