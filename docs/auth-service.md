# Auth Service

The Auth Service is the planned authentication and authorization boundary for OEFMS.

It mirrors the role of the previous auth-server concept from the reference system, but this scaffold is Java 21 and Spring Boot based.

## Planned Responsibilities

- User authentication.
- Client application registration.
- Role and permission integration.
- OAuth2/OIDC token issuance.
- Signing-key and certificate management.
- Service-to-service authentication support.
- Health, observability, and audit integration.

## Current State

This module now has a local foundation configuration for:

- OAuth2/OIDC authorization-server endpoints.
- A single in-memory bootstrap user.
- A single in-memory bootstrap OAuth2 client.
- Local signing key generation for development tokens.
- A safe metadata endpoint that does not return passwords or client secrets.

It still has no user tables, client tables, entities, repositories, migrations, or production identity lifecycle.

## Local Foundation Endpoint

The local foundation metadata endpoint is:

```text
GET /auth/foundation
```

It returns the configured issuer, authorization endpoints, bootstrap username, roles, permissions, client id, scopes, redirect URIs, and token TTL settings. It does not return the bootstrap password or client secret.

## Local OAuth2 Endpoints

| Endpoint | Purpose |
| --- | --- |
| `/oauth2/authorize` | Authorization endpoint for browser-based authorization-code flow. |
| `/oauth2/token` | Token endpoint for authorization-code, refresh-token, and client-credentials flows. |
| `/oauth2/jwks` | JSON Web Key Set endpoint for token verification. |
| `/oauth2/introspect` | Token introspection endpoint. |
| `/oauth2/revoke` | Token revocation endpoint. |
| `/userinfo` | OIDC user info endpoint. |

## Local Bootstrap User

| Setting | Default |
| --- | --- |
| Username | `oefms-admin` |
| Password | `openefms-admin` |
| Roles | `PLATFORM_ADMIN`, `FRAUD_ANALYST` |
| Permissions | `oefms.read`, `oefms.write`, `fraud.decision.read`, `fraud.case.write` |

Override these values with:

```text
OEFMS_AUTH_BOOTSTRAP_USERNAME
OEFMS_AUTH_BOOTSTRAP_PASSWORD
OEFMS_AUTH_BOOTSTRAP_ROLES
OEFMS_AUTH_BOOTSTRAP_PERMISSIONS
```

## Local Bootstrap Client

| Setting | Default |
| --- | --- |
| Client ID | `oefms-platform` |
| Client Secret | `openefms-platform-secret` |
| Scopes | `openid`, `profile`, `oefms.read`, `oefms.write`, `fraud.decision.read`, `fraud.case.write` |
| Redirect URI | `http://127.0.0.1:8080/login/oauth2/code/oefms-platform` |
| Post Logout Redirect URI | `http://127.0.0.1:8080/` |
| Access Token TTL | `PT15M` |
| Refresh Token TTL | `PT8H` |
| Require PKCE | `true` |
| Require Authorization Consent | `true` |

Override these values with:

```text
OEFMS_AUTH_CLIENT_ID
OEFMS_AUTH_CLIENT_SECRET
OEFMS_AUTH_CLIENT_NAME
OEFMS_AUTH_CLIENT_REDIRECT_URIS
OEFMS_AUTH_CLIENT_POST_LOGOUT_REDIRECT_URIS
OEFMS_AUTH_CLIENT_SCOPES
OEFMS_AUTH_CLIENT_ACCESS_TOKEN_TTL
OEFMS_AUTH_CLIENT_REFRESH_TOKEN_TTL
OEFMS_AUTH_CLIENT_REQUIRE_PROOF_KEY
OEFMS_AUTH_CLIENT_REQUIRE_AUTHORIZATION_CONSENT
```

## Production Direction

The current bootstrap configuration is only for local development and early service integration. Production Auth work should later add:

- Persistent users and clients.
- Password and credential policy.
- Refresh-token storage and revocation strategy.
- Multi-tenant role and permission mapping.
- Service-to-service credentials.
- Key rotation and external key management.
- Audit events for login, logout, token issuance, revocation, and client changes.
