package org.openefms.ingestionmanagement.transaction;

interface TransactionEventPublisher {
    void publish(TransactionIngestionMessage message);
}
