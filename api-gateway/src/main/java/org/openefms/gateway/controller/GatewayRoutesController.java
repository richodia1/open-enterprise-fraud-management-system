package org.openefms.gateway.controller;

import java.util.List;
import org.openefms.gateway.config.GatewaySecurityProperties;
import org.openefms.gateway.filter.CorrelationIdFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway")
public class GatewayRoutesController {
    private static final List<GatewayRouteSummary> ROUTES = List.of(
            new GatewayRouteSummary("auth-service", "/auth/**, /oauth2/**, /.well-known/**, /userinfo, /login, /login/**", "Auth Service"),
            new GatewayRouteSummary("identity-service", "/api/identity/**", "Identity Service"),
            new GatewayRouteSummary("administration-service", "/api/admin/**", "Administration Service"),
            new GatewayRouteSummary("configuration-management-service", "/api/configuration/**", "Configuration Management Service"),
            new GatewayRouteSummary("ingestion-management-service", "/api/v1/ingestion/**", "Ingestion Management Service"),
            new GatewayRouteSummary("fraud-detection-service", "/api/v1/fraud/**", "Fraud Detection Service"),
            new GatewayRouteSummary("explainable-ai-service", "/api/explanations/**", "Explainable AI Service"),
            new GatewayRouteSummary("explainable-ai-service-v1", "/api/v1/explanations/**", "Explainable AI Service"),
            new GatewayRouteSummary(
                    "rule-management-service",
                    "/api/v1/rules/**, /api/v1/rule-categories/**, /api/v1/rule-escalation-users/**, /api/v1/rule-definitions/**, /api/v1/rule-audit-trails/**, /api/v1/rule-approvals/**",
                    "Rule Management Service"),
            new GatewayRouteSummary("case-management-service-v1", "/api/v1/cases, /api/v1/cases/**, /api/v1/alerts, /api/v1/alerts/**", "Case Management Service"),
            new GatewayRouteSummary("audit-logging-service-v1", "/api/v1/audit-events, /api/v1/audit-events/**", "Audit Logging Service"),
            new GatewayRouteSummary("list-management-service", "/api/lists/**", "List Management Service"),
            new GatewayRouteSummary("case-management-service", "/api/cases/**", "Case Management Service"),
            new GatewayRouteSummary("audit-logging-service", "/api/audit/**", "Audit Logging Service"),
            new GatewayRouteSummary("notification-management-service", "/api/notifications/**", "Notification Management Service"),
            new GatewayRouteSummary("report-management-service", "/api/reports/**", "Report Management Service"),
            new GatewayRouteSummary("gdpr-service", "/api/gdpr/**", "GDPR Service")
    );

    private final GatewaySecurityProperties securityProperties;

    public GatewayRoutesController(GatewaySecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @GetMapping("/routes")
    public GatewayRoutesResponse routes() {
        return new GatewayRoutesResponse(
                CorrelationIdFilter.CORRELATION_ID_HEADER,
                securityProperties.enabled(),
                ROUTES
        );
    }

    public record GatewayRoutesResponse(
            String correlationHeader,
            boolean jwtSecurityEnabled,
            List<GatewayRouteSummary> routes
    ) {
    }

    public record GatewayRouteSummary(
            String id,
            String path,
            String serviceBoundary
    ) {
    }
}
