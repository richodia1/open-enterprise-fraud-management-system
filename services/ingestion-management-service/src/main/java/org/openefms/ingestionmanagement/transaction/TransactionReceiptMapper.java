package org.openefms.ingestionmanagement.transaction;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.openefms.common.models.transaction.TransactionParty;
import org.springframework.stereotype.Component;

@Component
class TransactionReceiptMapper {
    ReceivedTransactionEntity toEntity(
            TransactionReceiptRecord record,
            TransactionIngestionRequest transaction) {
        ReceivedTransactionEntity entity = new ReceivedTransactionEntity();
        applyRecord(entity, record);
        applyParty(entity, "originator", transaction.originator());
        applyParty(entity, "beneficiary", transaction.beneficiary());
        return entity;
    }

    TransactionReceiptRecord toRecord(ReceivedTransactionEntity entity) {
        return new TransactionReceiptRecord(
                entity.transactionId(),
                entity.externalReference(),
                entity.tenantId(),
                entity.transactionType(),
                entity.channel(),
                entity.direction(),
                entity.amountValue(),
                entity.currency(),
                entity.initiatedAt(),
                entity.acceptedAt(),
                entity.status(),
                entity.eventType(),
                entity.eventId(),
                entity.correlationId(),
                entity.traceId(),
                entity.publishedAt(),
                fromText(entity.attributesText()));
    }

    void applyRecord(ReceivedTransactionEntity entity, TransactionReceiptRecord record) {
        entity.transactionId(record.transactionId());
        entity.externalReference(record.externalReference());
        entity.tenantId(record.tenantId());
        entity.transactionType(record.transactionType());
        entity.channel(record.channel());
        entity.direction(record.direction());
        entity.amountValue(record.amount());
        entity.currency(record.currency());
        entity.initiatedAt(record.initiatedAt());
        entity.acceptedAt(record.acceptedAt());
        entity.status(record.status());
        entity.eventType(record.eventType());
        entity.eventId(record.eventId());
        entity.correlationId(record.correlationId());
        entity.traceId(record.traceId());
        entity.publishedAt(record.publishedAt());
        entity.attributesText(toText(record.attributes()));
    }

    private void applyParty(ReceivedTransactionEntity entity, String role, TransactionParty party) {
        if (party == null) {
            return;
        }
        if ("originator".equals(role)) {
            entity.originatorPartyReference(party.partyReference());
            entity.originatorCustomerReference(party.customerReference());
            entity.originatorAccountReference(party.accountReference());
            entity.originatorAccountNumberHash(party.accountNumberHash());
            entity.originatorInstitutionCode(party.institutionCode());
            entity.originatorCountryCode(party.countryCode());
            return;
        }

        entity.beneficiaryPartyReference(party.partyReference());
        entity.beneficiaryCustomerReference(party.customerReference());
        entity.beneficiaryAccountReference(party.accountReference());
        entity.beneficiaryAccountNumberHash(party.accountNumberHash());
        entity.beneficiaryInstitutionCode(party.institutionCode());
        entity.beneficiaryCountryCode(party.countryCode());
    }

    private String toText(Map<String, String> attributes) {
        Properties properties = new Properties();
        Map<String, String> source = attributes == null ? Map.of() : attributes;
        source.forEach((key, value) -> {
            if (key != null && value != null) {
                properties.setProperty(key, value);
            }
        });

        try {
            StringWriter writer = new StringWriter();
            properties.store(writer, null);
            return writer.toString();
        } catch (IOException exception) {
            throw new TransactionReceiptPersistenceException("Transaction receipt attributes could not be serialized.", exception);
        }
    }

    private Map<String, String> fromText(String text) {
        if (text == null || text.isBlank()) {
            return Map.of();
        }

        Properties properties = new Properties();
        try {
            properties.load(new StringReader(text));
        } catch (IOException exception) {
            throw new TransactionReceiptPersistenceException("Transaction receipt attributes could not be deserialized.", exception);
        }

        Map<String, String> attributes = new LinkedHashMap<>();
        properties.stringPropertyNames().stream()
                .sorted()
                .forEach(name -> attributes.put(name, properties.getProperty(name)));
        return attributes;
    }
}
