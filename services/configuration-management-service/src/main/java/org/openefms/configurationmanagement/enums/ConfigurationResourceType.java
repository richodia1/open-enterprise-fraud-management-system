package org.openefms.configurationmanagement.enums;

import org.openefms.common.models.management.ManagedResourceKind;

public enum ConfigurationResourceType implements ManagedResourceKind {
    APPROVALS(
            "Approvals",
            "configuration-management:Approvals",
            "Approval records for configuration changes."),
    BANK_FRAUD_DESK_EMAILS(
            "Bank Fraud Desk Emails",
            "configuration-management:BankFraudDeskEmails",
            "Fraud desk email recipients and resolution settings."),
    CASE_ASSIGNMENT_POLICIES(
            "Case Assignment Policies",
            "configuration-management:CaseAssignmentPolicies",
            "Tenant role policies that control who can receive fraud case assignments."),
    CASE_ESCALATION_POLICIES(
            "Case Escalation Policies",
            "configuration-management:CaseEscalationPolicies",
            "Tenant role policies that control who can receive escalated fraud cases."),
    CHANNELS(
            "Channels",
            "configuration-management:Channels",
            "Tenant channel definitions used when normalising events."),
    DATA_POINTS(
            "Data Points",
            "configuration-management:DataPoints",
            "Tenant-level data point definitions available to rules and payload mapping."),
    EVENT_TYPES(
            "Event Types",
            "configuration-management:EventTypes",
            "Tenant event types for transaction and non-transaction fraud monitoring."),
    FRAUD_TYPES(
            "Fraud Types",
            "configuration-management:FraudTypes",
            "Fraud category definitions used by cases, rules, and reports."),
    GLOBAL_CHANNEL_TYPES(
            "Global Channel Types",
            "configuration-management:GlobalChannelTypes",
            "Platform-level channel type lookup values."),
    GLOBAL_DATA_POINTS(
            "Global Data Points",
            "configuration-management:GlobalDataPoints",
            "Platform-level reusable data point definitions."),
    GLOBAL_EVENT_TYPES(
            "Global Event Types",
            "configuration-management:GlobalEventTypes",
            "Platform-level reusable event type definitions."),
    PAYLOAD_PROPERTIES(
            "Payload Properties",
            "configuration-management:PayloadProperties",
            "Payload mapping properties for inbound events and transaction attributes.");

    private final String displayName;
    private final String legacyResource;
    private final String notes;

    ConfigurationResourceType(String displayName, String legacyResource, String notes) {
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
