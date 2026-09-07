package org.openefms.gateway.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorrelationIdFilter extends OncePerRequestFilter {
    static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = resolveCorrelationId(request);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        MDC.put("correlationId", correlationId);
        try {
            filterChain.doFilter(new CorrelationIdRequestWrapper(request, correlationId), response);
        } finally {
            MDC.remove("correlationId");
        }
    }

    private static String resolveCorrelationId(HttpServletRequest request) {
        String headerValue = request.getHeader(CORRELATION_ID_HEADER);
        if (StringUtils.hasText(headerValue) && headerValue.trim().length() <= 128) {
            return headerValue.trim();
        }
        return UUID.randomUUID().toString();
    }

    private static boolean isCorrelationIdHeader(String name) {
        return CORRELATION_ID_HEADER.equalsIgnoreCase(name);
    }

    private static final class CorrelationIdRequestWrapper extends HttpServletRequestWrapper {
        private final String correlationId;

        private CorrelationIdRequestWrapper(HttpServletRequest request, String correlationId) {
            super(request);
            this.correlationId = correlationId;
        }

        @Override
        public String getHeader(String name) {
            if (isCorrelationIdHeader(name)) {
                return correlationId;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (isCorrelationIdHeader(name)) {
                return Collections.enumeration(List.of(correlationId));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> headerNames = new ArrayList<>(Collections.list(super.getHeaderNames()));
            boolean alreadyPresent = headerNames.stream().anyMatch(CorrelationIdFilter::isCorrelationIdHeader);
            if (!alreadyPresent) {
                headerNames.add(CORRELATION_ID_HEADER);
            }
            return Collections.enumeration(headerNames);
        }
    }
}
