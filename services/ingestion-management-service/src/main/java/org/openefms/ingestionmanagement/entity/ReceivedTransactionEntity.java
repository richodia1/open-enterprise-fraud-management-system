package org.openefms.ingestionmanagement.entity;

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
import org.openefms.common.persistence.entity.TenantScopedEventAttributesEntity;
import org.openefms.ingestionmanagement.enums.TransactionReceiptStatus;

@Entity
@Table(name = "received_transactions")
public class ReceivedTransactionEntity extends TenantScopedEventAttributesEntity {
    @Id
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "external_reference", length = 128)
    private String externalReference;

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

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    public ReceivedTransactionEntity() {
    }

    public UUID transactionId() {
        return transactionId;
    }

    public void transactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public String externalReference() {
        return externalReference;
    }

    public void externalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public TransactionType transactionType() {
        return transactionType;
    }

    public void transactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionChannel channel() {
        return channel;
    }

    public void channel(TransactionChannel channel) {
        this.channel = channel;
    }

    public TransactionDirection direction() {
        return direction;
    }

    public void direction(TransactionDirection direction) {
        this.direction = direction;
    }

    public BigDecimal amountValue() {
        return amountValue;
    }

    public void amountValue(BigDecimal amountValue) {
        this.amountValue = amountValue;
    }

    public String currency() {
        return currency;
    }

    public void currency(String currency) {
        this.currency = currency;
    }

    public String originatorPartyReference() {
        return originatorPartyReference;
    }

    public void originatorPartyReference(String originatorPartyReference) {
        this.originatorPartyReference = originatorPartyReference;
    }

    public String originatorCustomerReference() {
        return originatorCustomerReference;
    }

    public void originatorCustomerReference(String originatorCustomerReference) {
        this.originatorCustomerReference = originatorCustomerReference;
    }

    public String originatorAccountReference() {
        return originatorAccountReference;
    }

    public void originatorAccountReference(String originatorAccountReference) {
        this.originatorAccountReference = originatorAccountReference;
    }

    public String originatorAccountNumberHash() {
        return originatorAccountNumberHash;
    }

    public void originatorAccountNumberHash(String originatorAccountNumberHash) {
        this.originatorAccountNumberHash = originatorAccountNumberHash;
    }

    public String originatorInstitutionCode() {
        return originatorInstitutionCode;
    }

    public void originatorInstitutionCode(String originatorInstitutionCode) {
        this.originatorInstitutionCode = originatorInstitutionCode;
    }

    public String originatorCountryCode() {
        return originatorCountryCode;
    }

    public void originatorCountryCode(String originatorCountryCode) {
        this.originatorCountryCode = originatorCountryCode;
    }

    public String beneficiaryPartyReference() {
        return beneficiaryPartyReference;
    }

    public void beneficiaryPartyReference(String beneficiaryPartyReference) {
        this.beneficiaryPartyReference = beneficiaryPartyReference;
    }

    public String beneficiaryCustomerReference() {
        return beneficiaryCustomerReference;
    }

    public void beneficiaryCustomerReference(String beneficiaryCustomerReference) {
        this.beneficiaryCustomerReference = beneficiaryCustomerReference;
    }

    public String beneficiaryAccountReference() {
        return beneficiaryAccountReference;
    }

    public void beneficiaryAccountReference(String beneficiaryAccountReference) {
        this.beneficiaryAccountReference = beneficiaryAccountReference;
    }

    public String beneficiaryAccountNumberHash() {
        return beneficiaryAccountNumberHash;
    }

    public void beneficiaryAccountNumberHash(String beneficiaryAccountNumberHash) {
        this.beneficiaryAccountNumberHash = beneficiaryAccountNumberHash;
    }

    public String beneficiaryInstitutionCode() {
        return beneficiaryInstitutionCode;
    }

    public void beneficiaryInstitutionCode(String beneficiaryInstitutionCode) {
        this.beneficiaryInstitutionCode = beneficiaryInstitutionCode;
    }

    public String beneficiaryCountryCode() {
        return beneficiaryCountryCode;
    }

    public void beneficiaryCountryCode(String beneficiaryCountryCode) {
        this.beneficiaryCountryCode = beneficiaryCountryCode;
    }

    public OffsetDateTime initiatedAt() {
        return initiatedAt;
    }

    public void initiatedAt(OffsetDateTime initiatedAt) {
        this.initiatedAt = initiatedAt;
    }

    public OffsetDateTime acceptedAt() {
        return acceptedAt;
    }

    public void acceptedAt(OffsetDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public TransactionReceiptStatus status() {
        return status;
    }

    public void status(TransactionReceiptStatus status) {
        this.status = status;
    }

    public OffsetDateTime publishedAt() {
        return publishedAt;
    }

    public void publishedAt(OffsetDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

}
