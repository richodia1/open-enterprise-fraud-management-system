package org.openefms.ingestionmanagement.transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openefms.common.models.transaction.TransactionAmount;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.openefms.common.models.transaction.TransactionParty;
import org.springframework.stereotype.Component;

@Component
class TransactionIngestionRequestValidator {
    private static final int MAX_REFERENCE_LENGTH = 128;
    private static final int MAX_ATTRIBUTE_KEY_LENGTH = 80;
    private static final int MAX_ATTRIBUTE_VALUE_LENGTH = 1_000;
    private static final int MAX_ATTRIBUTE_COUNT = 50;

    void validate(TransactionIngestionRequest request) {
        List<String> errors = new ArrayList<>();
        if (request == null) {
            errors.add("request body must not be null");
            throw new InvalidTransactionIngestionRequestException(errors);
        }

        requireNotNull(request.transactionId(), "transactionId", errors);
        requireText(request.tenantId(), "tenantId", errors);
        maxLength(request.tenantId(), "tenantId", MAX_REFERENCE_LENGTH, errors);
        requireNotNull(request.transactionType(), "transactionType", errors);
        requireNotNull(request.channel(), "channel", errors);
        requireNotNull(request.direction(), "direction", errors);
        requireNotNull(request.initiatedAt(), "initiatedAt", errors);
        validateTimestamp(request.initiatedAt(), errors);
        validateOptionalReference(request.externalReference(), "externalReference", errors);
        validateAmount(request.amount(), errors);
        validateParty(request.originator(), "originator", errors);
        validateParty(request.beneficiary(), "beneficiary", errors);
        validateAttributes(request.attributes(), "attributes", errors);

        if (!errors.isEmpty()) {
            throw new InvalidTransactionIngestionRequestException(errors);
        }
    }

    private void validateAmount(TransactionAmount amount, List<String> errors) {
        if (amount == null) {
            errors.add("amount must not be null");
            return;
        }
        requireNotNull(amount.value(), "amount.value", errors);
        if (amount.value() != null && amount.value().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("amount.value must be greater than zero");
        }
        requireText(amount.currency(), "amount.currency", errors);
        if (amount.currency() != null && !amount.currency().matches("[A-Z]{3}")) {
            errors.add("amount.currency must be a three-letter ISO 4217 code");
        }
    }

    private void validateParty(TransactionParty party, String fieldName, List<String> errors) {
        if (party == null) {
            return;
        }

        if (isBlank(party.partyReference())
                && isBlank(party.customerReference())
                && isBlank(party.accountReference())
                && isBlank(party.accountNumberHash())) {
            errors.add(fieldName + " must include at least one reference");
        }

        validateOptionalReference(party.partyReference(), fieldName + ".partyReference", errors);
        validateOptionalReference(party.customerReference(), fieldName + ".customerReference", errors);
        validateOptionalReference(party.accountReference(), fieldName + ".accountReference", errors);
        validateOptionalReference(party.accountNumberHash(), fieldName + ".accountNumberHash", errors);
        validateOptionalReference(party.institutionCode(), fieldName + ".institutionCode", errors);

        if (party.countryCode() != null && !party.countryCode().matches("[A-Z]{2}")) {
            errors.add(fieldName + ".countryCode must be a two-letter ISO 3166 country code");
        }

        validateAttributes(party.attributes(), fieldName + ".attributes", errors);
    }

    private void validateTimestamp(OffsetDateTime initiatedAt, List<String> errors) {
        if (initiatedAt == null) {
            return;
        }
        if (initiatedAt.getOffset() == null) {
            errors.add("initiatedAt must include an offset");
        }
    }

    private void validateAttributes(Map<String, String> attributes, String fieldName, List<String> errors) {
        if (attributes == null) {
            return;
        }
        if (attributes.size() > MAX_ATTRIBUTE_COUNT) {
            errors.add(fieldName + " must not contain more than " + MAX_ATTRIBUTE_COUNT + " entries");
        }
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (isBlank(entry.getKey())) {
                errors.add(fieldName + " must not contain blank keys");
            } else if (entry.getKey().length() > MAX_ATTRIBUTE_KEY_LENGTH) {
                errors.add(fieldName + " keys must not exceed " + MAX_ATTRIBUTE_KEY_LENGTH + " characters");
            }
            if (entry.getValue() == null) {
                errors.add(fieldName + " values must not be null");
            } else if (entry.getValue().length() > MAX_ATTRIBUTE_VALUE_LENGTH) {
                errors.add(fieldName + " values must not exceed " + MAX_ATTRIBUTE_VALUE_LENGTH + " characters");
            }
        }
    }

    private void validateOptionalReference(String value, String fieldName, List<String> errors) {
        if (value == null) {
            return;
        }
        requireText(value, fieldName, errors);
        maxLength(value, fieldName, MAX_REFERENCE_LENGTH, errors);
    }

    private void requireText(String value, String fieldName, List<String> errors) {
        if (isBlank(value)) {
            errors.add(fieldName + " must not be blank");
        }
    }

    private void requireNotNull(Object value, String fieldName, List<String> errors) {
        if (value == null) {
            errors.add(fieldName + " must not be null");
        }
    }

    private void maxLength(String value, String fieldName, int maxLength, List<String> errors) {
        if (value != null && value.length() > maxLength) {
            errors.add(fieldName + " must not exceed " + maxLength + " characters");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
