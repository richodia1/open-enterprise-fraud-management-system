package org.openefms.notificationmanagement.enums;

import org.openefms.common.models.management.ManagedResourceKind;

public enum NotificationManagementResourceType implements ManagedResourceKind {
    AUDIENCE_CONFIGURATIONS(
            "Audience Configurations",
            "notification-management:AudienceConfigurations",
            "Audience routing definitions for operational notifications."),
    AUDIENCE_ESCALATION_EMAILS(
            "Audience Escalation Emails",
            "notification-management:AudienceEscalationEmails",
            "Escalation recipient lists for unresolved fraud work."),
    NOTIFICATION_AUDIENCE_TYPES(
            "Notification Audience Types",
            "notification-management:NotificationAudienceTypes",
            "Audience type lookup values."),
    NOTIFICATION_CONFIGURATION_AUDITS(
            "Notification Configuration Audits",
            "notification-management:NotificationConfigurationAudits",
            "Audit records for notification configuration changes."),
    NOTIFICATION_CONFIGURATIONS(
            "Notification Configurations",
            "notification-management:NotificationConfigurations",
            "Notification delivery and routing settings."),
    NOTIFICATION_MESSAGE_FORMATS(
            "Notification Message Formats",
            "notification-management:NotificationMessageFormats",
            "Message format lookup values for email, SMS, and other channels."),
    NOTIFICATION_MODES(
            "Notification Modes",
            "notification-management:NotificationModes",
            "Delivery mode lookup values."),
    NOTIFICATION_SEND_AUDITS(
            "Notification Send Audits",
            "notification-management:NotificationSendAudits",
            "Send attempt and delivery audit records."),
    NOTIFICATION_TEMPLATES(
            "Notification Templates",
            "notification-management:NotificationTemplates",
            "Tenant notification template definitions."),
    TEMPLATE_VARIABLE_OPTIONS(
            "Template Variable Options",
            "notification-management:TemplateVariableOptions",
            "Template variable lookup values exposed to administrators.");

    private final String displayName;
    private final String legacyResource;
    private final String notes;

    NotificationManagementResourceType(String displayName, String legacyResource, String notes) {
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
