package org.openefms.gateway.web;

import org.openefms.gateway.config.GatewaySecurityProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/gateway")
class GatewayRoutesController {
    private static final List<GatewayRouteSummary> ROUTES = List.of(
            new GatewayRouteSummary("auth-service", "/auth/**, /oauth2/**, /.well-known/**, /userinfo, /login, /login/**", "Auth Service"),
            new GatewayRouteSummary("identity-service", "/api/identity/**", "Identity Service"),
            new GatewayRouteSummary("administration-service", "/api/admin/**", "Administration Service"),
            new GatewayRouteSummary("configuration-management-service", "/api/configuration/**", "Configuration Management Service"),
            new GatewayRouteSummary("ingestion-management-service", "/api/ingestion/**", "Ingestion Management Service"),
            new GatewayRouteSummary("fraud-detection-service", "/api/v1/fraud/**", "Fraud Detection Service"),
            new GatewayRouteSummary("explainable-ai-service", "/api/explanations/**", "Explainable AI Service"),
            new GatewayRouteSummary("rule-management-service", "/api/rules/**", "Rule Management Service"),
            new GatewayRouteSummary("list-management-service", "/api/lists/**", "List Management Service"),
            new GatewayRouteSummary("case-management-service", "/api/cases/**", "Case Management Service"),
            new GatewayRouteSummary("audit-logging-service", "/api/audit/**", "Audit Logging Service"),
            new GatewayRouteSummary("notification-management-service", "/api/notifications/**", "Notification Management Service"),
            new GatewayRouteSummary("report-management-service", "/api/reports/**", "Report Management Service"),
            new GatewayRouteSummary("gdpr-service", "/api/gdpr/**", "GDPR Service")
    );

    private final GatewaySecurityProperties securityProperties;

    GatewayRoutesController(GatewaySecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @GetMapping("/routes")
    GatewayRoutesResponse routes() {
        return new GatewayRoutesResponse(
                CorrelationIdFilter.CORRELATION_ID_HEADER,
                securityProperties.enabled(),
                ROUTES
        );
    }

    record GatewayRoutesResponse(
            String correlationHeader,
            boolean jwtSecurityEnabled,
            List<GatewayRouteSummary> routes
    ) {
    }

    record GatewayRouteSummary(
            String id,
            String path,
            String serviceBoundary
    ) {
    }
}
