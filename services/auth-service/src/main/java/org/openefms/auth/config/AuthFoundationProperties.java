package org.openefms.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "oefms.auth")
public record AuthFoundationProperties(
        String issuer,
        Bootstrap bootstrap
) {
    private static final String DEFAULT_ISSUER = "http://localhost:8085";

    public AuthFoundationProperties {
        issuer = StringUtils.hasText(issuer) ? issuer : DEFAULT_ISSUER;
        bootstrap = bootstrap == null ? Bootstrap.defaults() : bootstrap;
    }

    public record Bootstrap(
            BootstrapUser user,
            BootstrapClient client
    ) {
        static Bootstrap defaults() {
            return new Bootstrap(BootstrapUser.defaults(), BootstrapClient.defaults());
        }

        public Bootstrap {
            user = user == null ? BootstrapUser.defaults() : user;
            client = client == null ? BootstrapClient.defaults() : client;
        }
    }

    public record BootstrapUser(
            String username,
            String password,
            List<String> roles,
            List<String> permissions
    ) {
        static BootstrapUser defaults() {
            return new BootstrapUser(
                    "oefms-admin",
                    "openefms-admin",
                    List.of("PLATFORM_ADMIN", "FRAUD_ANALYST"),
                    List.of("oefms.read", "oefms.write", "fraud.decision.read", "fraud.case.write")
            );
        }

        public BootstrapUser {
            username = StringUtils.hasText(username) ? username : "oefms-admin";
            password = StringUtils.hasText(password) ? password : "openefms-admin";
            roles = normalise(roles, List.of("PLATFORM_ADMIN", "FRAUD_ANALYST"));
            permissions = normalise(permissions, List.of("oefms.read", "oefms.write", "fraud.decision.read", "fraud.case.write"));
        }
    }

    public record BootstrapClient(
            String clientId,
            String clientSecret,
            String clientName,
            List<String> redirectUris,
            List<String> postLogoutRedirectUris,
            List<String> scopes,
            Duration accessTokenTtl,
            Duration refreshTokenTtl,
            boolean requireProofKey,
            boolean requireAuthorizationConsent
    ) {
        static BootstrapClient defaults() {
            return new BootstrapClient(
                    "oefms-platform",
                    "openefms-platform-secret",
                    "OEFMS Platform Client",
                    List.of("http://127.0.0.1:8080/login/oauth2/code/oefms-platform"),
                    List.of("http://127.0.0.1:8080/"),
                    List.of("openid", "profile", "oefms.read", "oefms.write", "fraud.decision.read", "fraud.case.write"),
                    Duration.ofMinutes(15),
                    Duration.ofHours(8),
                    true,
                    true
            );
        }

        public BootstrapClient {
            clientId = StringUtils.hasText(clientId) ? clientId : "oefms-platform";
            clientSecret = StringUtils.hasText(clientSecret) ? clientSecret : "openefms-platform-secret";
            clientName = StringUtils.hasText(clientName) ? clientName : "OEFMS Platform Client";
            redirectUris = normalise(redirectUris, List.of("http://127.0.0.1:8080/login/oauth2/code/oefms-platform"));
            postLogoutRedirectUris = normalise(postLogoutRedirectUris, List.of("http://127.0.0.1:8080/"));
            scopes = normalise(scopes, List.of("openid", "profile", "oefms.read", "oefms.write", "fraud.decision.read", "fraud.case.write"));
            accessTokenTtl = accessTokenTtl == null ? Duration.ofMinutes(15) : accessTokenTtl;
            refreshTokenTtl = refreshTokenTtl == null ? Duration.ofHours(8) : refreshTokenTtl;
        }
    }

    private static List<String> normalise(List<String> values, List<String> defaults) {
        if (values == null || values.isEmpty()) {
            return defaults;
        }
        List<String> normalised = values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        return normalised.isEmpty() ? defaults : normalised;
    }
}
