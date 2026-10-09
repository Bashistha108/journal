package com.journal.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class LocalOnlyFilter extends OncePerRequestFilter {

    @Value("${tj.allowed-hosts:localhost,127.0.0.1}")
    private List<String> allowedHosts;

    @Value("${tj.allowed-origins:http://localhost:5173}")
    private List<String> allowedOrigins;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String host = request.getHeader("Host");
        if (host != null) {
            String hostName = host.split(":")[0];
            if (!allowedHosts.contains(hostName)) {
                reject(response, "Invalid Host header");
                return;
            }
        }

        String origin = request.getHeader("Origin");
        if (origin != null && !allowedOrigins.contains(origin)) {
            reject(response, "Invalid Origin header");
            return;
        }

        String method = request.getMethod();
        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method) && !"OPTIONS".equalsIgnoreCase(method)) {
            String clientHeader = request.getHeader("X-TJ-Client");
            if (clientHeader == null || clientHeader.trim().isEmpty()) {
                reject(response, "Missing X-TJ-Client header on state-changing request");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of(
            "code", "FORBIDDEN_ORIGIN",
            "message", message
        )));
    }
}
