package org.openefms.common.models.management;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ManagedResourceStoreTest {
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-13T01:00:00Z"), ZoneOffset.UTC);

    @Test
    void listsResourceTypesAndSeedRecords() {
        ManagedResourceStore<SampleResourceType> store = new ManagedResourceStore<>(SampleResourceType.class, FIXED_CLOCK);

        assertEquals(2, store.resourceTypes().size());
        assertEquals(1, store.list(SampleResourceType.CHANNELS, "global", null, null).size());
        assertEquals("channels", store.list(SampleResourceType.CHANNELS, "global", null, null).getFirst().code());
    }

    @Test
    void createsReplacesAndDeletesManagedResources() {
        ManagedResourceStore<SampleResourceType> store = new ManagedResourceStore<>(SampleResourceType.class, FIXED_CLOCK);

        ManagedResourceRecord created = store.create(
                SampleResourceType.CHANNELS,
                new ManagedResourceRequest(
                        "tenant-a",
                        "mobile",
                        "Mobile Banking",
                        "Mobile banking channel.",
                        true,
                        Map.of("channelCode", "MOBILE"),
                        "admin"));

        assertEquals("tenant-a", created.tenantId());
        assertEquals("mobile", created.code());
        assertEquals("Mobile Banking", store.get(SampleResourceType.CHANNELS, created.id()).name());

        ManagedResourceRecord replaced = store.replace(
                SampleResourceType.CHANNELS,
                created.id(),
                new ManagedResourceRequest(
                        "tenant-a",
                        "mobile-app",
                        "Mobile App",
                        "Updated channel.",
                        false,
                        Map.of(),
                        "approver"));

        assertEquals(created.id(), replaced.id());
        assertEquals("mobile-app", replaced.code());
        assertEquals("approver", replaced.updatedBy());

        store.delete(SampleResourceType.CHANNELS, created.id());

        assertThrows(ManagedResourceNotFoundException.class, () -> store.get(SampleResourceType.CHANNELS, created.id()));
    }

    @Test
    void rejectsInvalidAndDuplicateResources() {
        ManagedResourceStore<SampleResourceType> store = new ManagedResourceStore<>(SampleResourceType.class, FIXED_CLOCK);

        assertThrows(
                ManagedResourceValidationException.class,
                () -> store.create(SampleResourceType.CHANNELS, new ManagedResourceRequest("tenant-a", null, null, null, true, null, null)));

        store.create(SampleResourceType.CHANNELS, request("tenant-a", "ussd"));

        ManagedResourceConflictException exception = assertThrows(
                ManagedResourceConflictException.class,
                () -> store.create(SampleResourceType.CHANNELS, request("tenant-a", "USSD")));

        assertTrue(exception.getMessage().contains("already exists"));
    }

    private ManagedResourceRequest request(String tenantId, String code) {
        return new ManagedResourceRequest(tenantId, code, "USSD", "USSD channel.", true, Map.of(), "admin");
    }

    private enum SampleResourceType implements ManagedResourceKind {
        CHANNELS("Channels", "configuration-management:Channels", "Channel definitions."),
        FRAUD_TYPES("Fraud Types", "configuration-management:FraudTypes", "Fraud type definitions.");

        private final String displayName;
        private final String legacyResource;
        private final String notes;

        SampleResourceType(String displayName, String legacyResource, String notes) {
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
}
