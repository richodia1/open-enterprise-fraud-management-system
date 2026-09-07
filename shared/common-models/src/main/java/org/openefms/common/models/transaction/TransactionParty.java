package org.openefms.common.models.transaction;

import java.util.Map;

/**
 * Party reference for transaction contracts. Avoid raw PII where hashed or external references are enough.
 */
public record TransactionParty(
        String partyReference,
        String customerReference,
        String accountReference,
        String accountNumberHash,
        String institutionCode,
        String countryCode,
        Map<String, String> attributes
) {
    public TransactionParty {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
