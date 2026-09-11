package org.openefms.ingestionmanagement.transaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.openefms.common.models.transaction.TransactionChannel;
import org.openefms.common.models.transaction.TransactionDirection;
import org.openefms.common.models.transaction.TransactionType;

@Entity
@Table(name = "received_transactions")
class ReceivedTransactionEntity {
    @Id
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "external_reference", length = 128)
    private String externalReference;

    @Column(name = "tenant_id", nullable = false, length = 128)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 64)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 64)
    private TransactionChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 64)
    private TransactionDirection direction;

    @Column(name = "amount_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal amountValue;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "originator_party_reference", length = 128)
    private String originatorPartyReference;

    @Column(name = "originator_customer_reference", length = 128)
    private String originatorCustomerReference;

    @Column(name = "originator_account_reference", length = 128)
    private String originatorAccountReference;

    @Column(name = "originator_account_number_hash", length = 256)
    private String originatorAccountNumberHash;

    @Column(name = "originator_institution_code", length = 64)
    private String originatorInstitutionCode;

    @Column(name = "originator_country_code", length = 2)
    private String originatorCountryCode;

    @Column(name = "beneficiary_party_reference", length = 128)
    private String beneficiaryPartyReference;

    @Column(name = "beneficiary_customer_reference", length = 128)
    private String beneficiaryCustomerReference;

    @Column(name = "beneficiary_account_reference", length = 128)
    private String beneficiaryAccountReference;

    @Column(name = "beneficiary_account_number_hash", length = 256)
    private String beneficiaryAccountNumberHash;

    @Column(name = "beneficiary_institution_code", length = 64)
    private String beneficiaryInstitutionCode;

    @Column(name = "beneficiary_country_code", length = 2)
    private String beneficiaryCountryCode;

    @Column(name = "initiated_at", nullable = false)
    private OffsetDateTime initiatedAt;

    @Column(name = "accepted_at", nullable = false)
    private OffsetDateTime acceptedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TransactionReceiptStatus status;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "correlation_id", nullable = false, length = 128)
    private String correlationId;

    @Column(name = "trace_id", length = 128)
    private String traceId;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "attributes_text", nullable = false, columnDefinition = "text")
    private String attributesText;

    protected ReceivedTransactionEntity() {
    }

    UUID transactionId() {
        return transactionId;
    }

    void transactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    String externalReference() {
        return externalReference;
    }

    void externalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    String tenantId() {
        return tenantId;
    }

    void tenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    TransactionType transactionType() {
        return transactionType;
    }

    void transactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    TransactionChannel channel() {
        return channel;
    }

    void channel(TransactionChannel channel) {
        this.channel = channel;
    }

    TransactionDirection direction() {
        return direction;
    }

    void direction(TransactionDirection direction) {
        this.direction = direction;
    }

    BigDecimal amountValue() {
        return amountValue;
    }

    void amountValue(BigDecimal amountValue) {
        this.amountValue = amountValue;
    }

    String currency() {
        return currency;
    }

    void currency(String currency) {
        this.currency = currency;
    }

    String originatorPartyReference() {
        return originatorPartyReference;
    }

    void originatorPartyReference(String originatorPartyReference) {
        this.originatorPartyReference = originatorPartyReference;
    }

    String originatorCustomerReference() {
        return originatorCustomerReference;
    }

    void originatorCustomerReference(String originatorCustomerReference) {
        this.originatorCustomerReference = originatorCustomerReference;
    }

    String originatorAccountReference() {
        return originatorAccountReference;
    }

    void originatorAccountReference(String originatorAccountReference) {
        this.originatorAccountReference = originatorAccountReference;
    }

    String originatorAccountNumberHash() {
        return originatorAccountNumberHash;
    }

    void originatorAccountNumberHash(String originatorAccountNumberHash) {
        this.originatorAccountNumberHash = originatorAccountNumberHash;
    }

    String originatorInstitutionCode() {
        return originatorInstitutionCode;
    }

    void originatorInstitutionCode(String originatorInstitutionCode) {
        this.originatorInstitutionCode = originatorInstitutionCode;
    }

    String originatorCountryCode() {
        return originatorCountryCode;
    }

    void originatorCountryCode(String originatorCountryCode) {
        this.originatorCountryCode = originatorCountryCode;
    }

    String beneficiaryPartyReference() {
        return beneficiaryPartyReference;
    }

    void beneficiaryPartyReference(String beneficiaryPartyReference) {
        this.beneficiaryPartyReference = beneficiaryPartyReference;
    }

    String beneficiaryCustomerReference() {
        return beneficiaryCustomerReference;
    }

    void beneficiaryCustomerReference(String beneficiaryCustomerReference) {
        this.beneficiaryCustomerReference = beneficiaryCustomerReference;
    }

    String beneficiaryAccountReference() {
        return beneficiaryAccountReference;
    }

    void beneficiaryAccountReference(String beneficiaryAccountReference) {
        this.beneficiaryAccountReference = beneficiaryAccountReference;
    }

    String beneficiaryAccountNumberHash() {
        return beneficiaryAccountNumberHash;
    }

    void beneficiaryAccountNumberHash(String beneficiaryAccountNumberHash) {
        this.beneficiaryAccountNumberHash = beneficiaryAccountNumberHash;
    }

    String beneficiaryInstitutionCode() {
        return beneficiaryInstitutionCode;
    }

    void beneficiaryInstitutionCode(String beneficiaryInstitutionCode) {
        this.beneficiaryInstitutionCode = beneficiaryInstitutionCode;
    }

    String beneficiaryCountryCode() {
        return beneficiaryCountryCode;
    }

    void beneficiaryCountryCode(String beneficiaryCountryCode) {
        this.beneficiaryCountryCode = beneficiaryCountryCode;
    }

    OffsetDateTime initiatedAt() {
        return initiatedAt;
    }

    void initiatedAt(OffsetDateTime initiatedAt) {
        this.initiatedAt = initiatedAt;
    }

    OffsetDateTime acceptedAt() {
        return acceptedAt;
    }

    void acceptedAt(OffsetDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    TransactionReceiptStatus status() {
        return status;
    }

    void status(TransactionReceiptStatus status) {
        this.status = status;
    }

    String eventType() {
        return eventType;
    }

    void eventType(String eventType) {
        this.eventType = eventType;
    }

    UUID eventId() {
        return eventId;
    }

    void eventId(UUID eventId) {
        this.eventId = eventId;
    }

    String correlationId() {
        return correlationId;
    }

    void correlationId(String correlationId) {
        this.correlationId = correlationId;
    }

    String traceId() {
        return traceId;
    }

    void traceId(String traceId) {
        this.traceId = traceId;
    }

    OffsetDateTime publishedAt() {
        return publishedAt;
    }

    void publishedAt(OffsetDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    String attributesText() {
        return attributesText;
    }

    void attributesText(String attributesText) {
        this.attributesText = attributesText;
    }
}
