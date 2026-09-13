package org.openefms.ingestionmanagement.enums;

import org.openefms.common.models.management.ManagedResourceKind;

public enum IngestionManagementResourceType implements ManagedResourceKind {
    DATA_SOURCES(
            "Data Sources",
            "ingestion-management:DataSources",
            "External and internal data sources feeding the ingestion pipeline."),
    INGESTION_JOBS(
            "Ingestion Jobs",
            "ingestion-management:IngestionJobs",
            "Scheduled or manual ingestion job definitions."),
    INGESTION_JOB_LEASES(
            "Ingestion Job Leases",
            "ingestion-management:IngestionJobLeases",
            "Runtime job lease records used to coordinate workers."),
    INGESTION_JOB_OUTPUT_STREAM_SETTINGS(
            "Ingestion Job Output Stream Settings",
            "ingestion-management:IngestionJobOutputStreamSettings",
            "Output stream settings for jobs that publish normalised events."),
    INGESTION_JOB_RUN_QUERY_LOGS(
            "Ingestion Job Run Query Logs",
            "ingestion-management:IngestionJobRunQueryLogs",
            "Execution query logs captured for ingestion job runs."),
    INGESTION_JOB_TEST_RUNS(
            "Ingestion Job Test Runs",
            "ingestion-management:IngestionJobTest",
            "Contract placeholder for validating ingestion jobs before activation.");

    private final String displayName;
    private final String legacyResource;
    private final String notes;

    IngestionManagementResourceType(String displayName, String legacyResource, String notes) {
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
