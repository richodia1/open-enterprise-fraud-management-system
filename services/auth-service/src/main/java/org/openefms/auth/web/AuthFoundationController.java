package org.openefms.auth.web;

import org.openefms.auth.config.AuthFoundationProperties;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/auth/foundation")
class AuthFoundationController {
    private final AuthFoundationProperties properties;
    private final AuthorizationServerSettings authorizationServerSettings;

    AuthFoundationController(
            AuthFoundationProperties properties,
            AuthorizationServerSettings authorizationServerSettings
    ) {
        this.properties = properties;
        this.authorizationServerSettings = authorizationServerSettings;
    }

    @GetMapping
    AuthFoundationResponse foundation() {
        AuthFoundationProperties.BootstrapUser user = properties.bootstrap().user();
        AuthFoundationProperties.BootstrapClient client = properties.bootstrap().client();
        return new AuthFoundationResponse(
                authorizationServerSettings.getIssuer(),
                new AuthEndpoints(
                        authorizationServerSettings.getAuthorizationEndpoint(),
                        authorizationServerSettings.getTokenEndpoint(),
                        authorizationServerSettings.getJwkSetEndpoint(),
                        authorizationServerSettings.getTokenIntrospectionEndpoint(),
                        authorizationServerSettings.getTokenRevocationEndpoint(),
                        authorizationServerSettings.getOidcUserInfoEndpoint()
                ),
                new BootstrapUserSummary(
                        user.username(),
                        user.roles(),
                        user.permissions()
                ),
                new BootstrapClientSummary(
                        client.clientId(),
                        client.clientName(),
                        client.redirectUris(),
                        client.postLogoutRedirectUris(),
                        client.scopes(),
                        client.accessTokenTtl(),
                        client.refreshTokenTtl(),
                        client.requireProofKey(),
                        client.requireAuthorizationConsent()
                )
        );
    }

    record AuthFoundationResponse(
            String issuer,
            AuthEndpoints endpoints,
            BootstrapUserSummary bootstrapUser,
            BootstrapClientSummary bootstrapClient
    ) {
    }

    record AuthEndpoints(
            String authorizationEndpoint,
            String tokenEndpoint,
            String jwkSetEndpoint,
            String tokenIntrospectionEndpoint,
            String tokenRevocationEndpoint,
            String oidcUserInfoEndpoint
    ) {
    }

    record BootstrapUserSummary(
            String username,
            List<String> roles,
            List<String> permissions
    ) {
    }

    record BootstrapClientSummary(
            String clientId,
            String clientName,
            List<String> redirectUris,
            List<String> postLogoutRedirectUris,
            List<String> scopes,
            Duration accessTokenTtl,
            Duration refreshTokenTtl,
            boolean requireProofKey,
            boolean requireAuthorizationConsent
    ) {
    }
}
