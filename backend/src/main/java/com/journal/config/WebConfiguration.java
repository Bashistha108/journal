package com.journal.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Configuration
public class WebConfiguration {

    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static class RequestIdFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            String requestId = UUID.randomUUID().toString();
            request.setAttribute("requestId", requestId);
            response.setHeader("X-Request-Id", requestId);
            filterChain.doFilter(request, response);
        }
    }
}
