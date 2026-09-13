package org.openefms.ingestionmanagement.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.openefms.common.events.EventEnvelope;
import org.openefms.common.events.EventMetadata;
import org.openefms.common.events.EventTypes;
import org.openefms.common.events.rabbitmq.RabbitMqTopology;
import org.openefms.common.events.transaction.TransactionReceivedEvent;
import org.openefms.common.models.transaction.TransactionAmount;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.openefms.common.models.transaction.TransactionParty;
import org.openefms.ingestionmanagement.dto.TransactionIngestionResponse;
import org.openefms.ingestionmanagement.entity.TransactionReceiptRecord;
import org.openefms.ingestionmanagement.enums.TransactionReceiptStatus;
import org.openefms.ingestionmanagement.messaging.IngestionRabbitMqProperties;
import org.openefms.ingestionmanagement.messaging.TransactionEventPublisher;
import org.openefms.ingestionmanagement.messaging.TransactionIngestionMessage;
import org.openefms.ingestionmanagement.repository.TransactionReceiptRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionIngestionService {
    static final String RECEIVED_STATUS = "RECEIVED";

    private static final int TRANSACTION_RECEIVED_EVENT_VERSION = 1;
    private static final String SOURCE_SERVICE = "ingestion-management-service";

    private final TransactionIngestionRequestValidator validator;
    private final TransactionReceiptRepository receiptRepository;
    private final TransactionEventPublisher publisher;
    private final IngestionRabbitMqProperties rabbitMqProperties;
    private final Clock clock;

    public TransactionIngestionService(
            TransactionIngestionRequestValidator validator,
            TransactionReceiptRepository receiptRepository,
            TransactionEventPublisher publisher,
            IngestionRabbitMqProperties rabbitMqProperties,
            Clock clock) {
        this.validator = validator;
        this.receiptRepository = receiptRepository;
        this.publisher = publisher;
        this.rabbitMqProperties = rabbitMqProperties;
        this.clock = clock;
    }

    public TransactionIngestionResult receive(
            TransactionIngestionRequest request,
            String correlationId,
            String traceId) {
        TransactionIngestionRequest normalisedRequest = normalise(request);
        validator.validate(normalisedRequest);

        String resolvedCorrelationId = resolveCorrelationId(correlationId);
        OffsetDateTime acceptedAt = OffsetDateTime.now(clock);
        EventMetadata metadata = eventMetadata(normalisedRequest, resolvedCorrelationId, textOrNull(traceId), acceptedAt);
        EventEnvelope<TransactionReceivedEvent> envelope = new EventEnvelope<>(
                metadata,
                new TransactionReceivedEvent(normalisedRequest));

        receiptRepository.saveReceived(receipt(normalisedRequest, metadata, acceptedAt), normalisedRequest);
        TransactionIngestionMessage message = new TransactionIngestionMessage(
                rabbitMqProperties.exchange(),
                RabbitMqTopology.routingKeyFor(EventTypes.TRANSACTION_RECEIVED),
                envelope);
        publisher.publish(message);
        receiptRepository.markPublished(normalisedRequest.transactionId());

        return new TransactionIngestionResult(response(normalisedRequest, resolvedCorrelationId, acceptedAt), message);
    }

    public List<TransactionReceiptRecord> receipts(String tenantId) {
        String normalisedTenantId = trimToEmpty(tenantId);
        if (normalisedTenantId.isBlank()) {
            throw new InvalidTransactionIngestionRequestException(List.of("tenantId must not be blank"));
        }
        return receiptRepository.findByTenantId(normalisedTenantId);
    }

    public TransactionReceiptRecord receipt(UUID transactionId) {
        return receiptRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionReceiptNotFoundException(transactionId));
    }

    private EventMetadata eventMetadata(
            TransactionIngestionRequest request,
            String correlationId,
            String traceId,
            OffsetDateTime acceptedAt) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("ingestionStatus", RECEIVED_STATUS);
        if (request.externalReference() != null) {
            attributes.put("externalReference", request.externalReference());
        }

        return new EventMetadata(
                UUID.randomUUID(),
                EventTypes.TRANSACTION_RECEIVED,
                TRANSACTION_RECEIVED_EVENT_VERSION,
                acceptedAt,
                SOURCE_SERVICE,
                request.tenantId(),
                correlationId,
                null,
                traceId,
                attributes);
    }

    private TransactionIngestionResponse response(
            TransactionIngestionRequest request,
            String correlationId,
            OffsetDateTime acceptedAt) {
        return new TransactionIngestionResponse(
                request.transactionId(),
                request.externalReference(),
                request.tenantId(),
                RECEIVED_STATUS,
                EventTypes.TRANSACTION_RECEIVED,
                correlationId,
                acceptedAt,
                Map.of("eventPublication", "published"));
    }

    private TransactionReceiptRecord receipt(
            TransactionIngestionRequest request,
            EventMetadata metadata,
            OffsetDateTime acceptedAt) {
        return new TransactionReceiptRecord(
                request.transactionId(),
                request.externalReference(),
                request.tenantId(),
                request.transactionType(),
                request.channel(),
                request.direction(),
                request.amount().value(),
                request.amount().currency(),
                request.initiatedAt(),
                acceptedAt,
                TransactionReceiptStatus.RECEIVED,
                metadata.eventType(),
                metadata.eventId(),
                metadata.correlationId(),
                metadata.traceId(),
                null,
                request.attributes());
    }

    private TransactionIngestionRequest normalise(TransactionIngestionRequest request) {
        if (request == null) {
            return null;
        }

        return new TransactionIngestionRequest(
                request.transactionId(),
                textOrNull(request.externalReference()),
                trimToEmpty(request.tenantId()),
                request.transactionType(),
                request.channel(),
                request.direction(),
                normaliseAmount(request.amount()),
                normaliseParty(request.originator()),
                normaliseParty(request.beneficiary()),
                request.initiatedAt(),
                normaliseAttributes(request.attributes()));
    }

    private TransactionAmount normaliseAmount(TransactionAmount amount) {
        if (amount == null) {
            return null;
        }
        String currency = amount.currency() == null ? null : amount.currency().trim().toUpperCase(Locale.ROOT);
        return new TransactionAmount(amount.value(), currency);
    }

    private TransactionParty normaliseParty(TransactionParty party) {
        if (party == null) {
            return null;
        }
        String countryCode = party.countryCode() == null
                ? null
                : party.countryCode().trim().toUpperCase(Locale.ROOT);
        return new TransactionParty(
                textOrNull(party.partyReference()),
                textOrNull(party.customerReference()),
                textOrNull(party.accountReference()),
                textOrNull(party.accountNumberHash()),
                textOrNull(party.institutionCode()),
                textOrNull(countryCode),
                normaliseAttributes(party.attributes()));
    }

    private Map<String, String> normaliseAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Map.of();
        }
        Map<String, String> normalised = new LinkedHashMap<>();
        attributes.forEach((key, value) -> normalised.put(textOrEmpty(key), textOrEmpty(value)));
        return normalised;
    }

    private String resolveCorrelationId(String correlationId) {
        String normalisedCorrelationId = textOrNull(correlationId);
        return normalisedCorrelationId == null ? UUID.randomUUID().toString() : normalisedCorrelationId;
    }

    private String textOrNull(String value) {
        String trimmed = textOrEmpty(value);
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToEmpty(String value) {
        return textOrEmpty(value);
    }

    private String textOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
