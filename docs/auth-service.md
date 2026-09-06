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

This module is only a service shell. It has no users, clients, entities, repositories, controllers, or auth flow configuration yet.
