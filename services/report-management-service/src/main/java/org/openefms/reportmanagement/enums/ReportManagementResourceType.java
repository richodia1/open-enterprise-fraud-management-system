package org.openefms.reportmanagement.enums;

import org.openefms.common.models.management.ManagedResourceKind;

public enum ReportManagementResourceType implements ManagedResourceKind {
    CLOSED_CASES(
            "Closed Cases",
            "report-management:ClosedCases",
            "Closed-case report views and exported summaries."),
    DASHBOARD_METRICS(
            "Dashboard Metrics",
            "report-management:Dashboard",
            "General dashboard metric definitions."),
    EVENT_TRENDS(
            "Event Trends",
            "report-management:EventTrend",
            "Event trend report definitions."),
    FRAUD_DASHBOARD_METRICS(
            "Fraud Dashboard Metrics",
            "report-management:FraudDashboard",
            "Fraud-specific dashboard metric definitions."),
    GENERATED_REPORTS(
            "Generated Reports",
            "report-management:GeneratedReports",
            "Generated report records and downloadable output metadata."),
    LIST_REPORTS(
            "List Reports",
            "report-management:ListReport",
            "Reports over configured lists and list usage.");

    private final String displayName;
    private final String legacyResource;
    private final String notes;

    ReportManagementResourceType(String displayName, String legacyResource, String notes) {
        this.displayName = displayName;
        this.legacyResource = legacyResource;
        this.notes = notes;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public String legacyResource() {
        return legacyResource;
    }

    @Override
    public String notes() {
        return notes;
    }
}
