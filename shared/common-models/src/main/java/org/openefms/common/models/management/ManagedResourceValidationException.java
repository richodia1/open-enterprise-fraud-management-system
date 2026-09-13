package org.openefms.common.models.management;

import java.util.List;

public class ManagedResourceValidationException extends RuntimeException {
    private final List<String> errors;

    public ManagedResourceValidationException(List<String> errors) {
        super("Managed resource request is invalid");
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
