package org.openefms.common.models.management;

public class ManagedResourceNotFoundException extends RuntimeException {
    public ManagedResourceNotFoundException(String resourceType, String id) {
        super(resourceType + " resource " + id + " was not found");
    }
}
