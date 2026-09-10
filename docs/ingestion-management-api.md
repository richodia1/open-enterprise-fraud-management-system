# Ingestion Management API

The Ingestion Management Service owns the first external transaction intake boundary.

This branch adds request validation and prepares a transport-ready `TransactionReceivedEvent`. It does not publish to RabbitMQ, write to a database, or trigger downstream fraud processing yet.

## Endpoint

```text
POST /api/v1/ingestion/transactions
```

Through the API Gateway, the same public path is used:

```text
POST /api/v1/ingestion/transactions
```

## Request Body

The endpoint accepts the shared `TransactionIngestionRequest` contract from `common-models`.

Example:

```json
{
  "transactionId": "118f3bd4-b208-44ff-b1ff-447c78d80d02",
  "externalReference": "BANK-TXN-001",
  "tenantId": "tenant-a",
  "transactionType": "TRANSFER",
  "channel": "MOBILE",
  "direction": "OUTBOUND",
  "amount": {
    "value": 25.00,
    "currency": "GBP"
  },
  "originator": {
    "partyReference": "originator-001",
    "customerReference": "customer-001",
    "accountReference": "account-001",
    "accountNumberHash": "account-hash-001",
    "institutionCode": "BANK",
    "countryCode": "GB",
    "attributes": {}
  },
  "beneficiary": {
    "partyReference": "beneficiary-001",
    "customerReference": "customer-002",
    "accountReference": "account-002",
    "accountNumberHash": "account-hash-002",
    "institutionCode": "BANK",
    "countryCode": "GB",
    "attributes": {}
  },
  "initiatedAt": "2026-09-11T08:58:00Z",
  "attributes": {}
}
```

## Response

Successful requests return `202 Accepted`.

```json
{
  "transactionId": "118f3bd4-b208-44ff-b1ff-447c78d80d02",
  "externalReference": "BANK-TXN-001",
  "tenantId": "tenant-a",
  "status": "RECEIVED",
  "eventType": "transaction.received",
  "correlationId": "correlation-1",
  "acceptedAt": "2026-09-11T09:00:00Z",
  "attributes": {
    "eventPublication": "prepared"
  }
}
```

The service returns or generates `X-Correlation-Id` for traceability.

## Validation

The first validation layer requires:

- `transactionId`
- `tenantId`
- `transactionType`
- `channel`
- `direction`
- `amount.value` greater than zero
- `amount.currency` as a three-letter ISO 4217 code
- `initiatedAt`

When party details are supplied, the service validates that each party has at least one reference and that country codes use a two-letter ISO 3166 format.

## Event Preparation

The service prepares this event envelope for the next RabbitMQ publisher branch:

```text
EventEnvelope<TransactionReceivedEvent>
```

The prepared message uses:

| Field | Value |
| --- | --- |
| Exchange | `oefms.events` |
| Routing key | `transaction.received` |
| Source service | `ingestion-management-service` |
| Event version | `1` |
