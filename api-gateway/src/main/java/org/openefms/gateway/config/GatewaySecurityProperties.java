package org.openefms.gateway.config;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oefms.gateway.security")
public record GatewaySecurityProperties(
        boolean enabled,
        URI jwkSetUri
) {
    private static final URI DEFAULT_JWK_SET_URI = URI.create("http://localhost:8085/oauth2/jwks");

    public GatewaySecurityProperties {
        jwkSetUri = jwkSetUri == null ? DEFAULT_JWK_SET_URI : jwkSetUri;
    }
}
