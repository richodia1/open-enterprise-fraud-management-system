package org.openefms.gateway.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oefms.gateway.cors")
public record GatewayCorsProperties(
        List<String> allowedOrigins
) {
    private static final List<String> DEFAULT_ALLOWED_ORIGINS = List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173"
    );

    public GatewayCorsProperties {
        List<String> configuredOrigins = allowedOrigins == null || allowedOrigins.isEmpty()
                ? DEFAULT_ALLOWED_ORIGINS
                : normalizeOrigins(allowedOrigins);
        allowedOrigins = configuredOrigins.isEmpty() ? DEFAULT_ALLOWED_ORIGINS : configuredOrigins;
    }

    private static List<String> normalizeOrigins(List<String> origins) {
        return origins.stream()
                .flatMap(origin -> Arrays.stream(origin.split(",")))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .distinct()
                .toList();
    }
}
