package org.openefms.ingestionmanagement.messaging;

public interface TransactionEventPublisher {
    public void publish(TransactionIngestionMessage message);
}
