package org.openefms.ingestionmanagement.transaction;

import org.openefms.common.events.rabbitmq.RabbitMqHeaders;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/ingestion/transactions")
class TransactionIngestionController {
    private final TransactionIngestionService ingestionService;

    TransactionIngestionController(TransactionIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<TransactionIngestionResponse> ingest(
            @RequestBody TransactionIngestionRequest request,
            @RequestHeader(value = RabbitMqHeaders.CORRELATION_ID, required = false) String correlationId,
            @RequestHeader(value = RabbitMqHeaders.TRACE_ID, required = false) String traceId) {
        TransactionIngestionResult result = ingestionService.receive(request, correlationId, traceId);
        return ResponseEntity
                .accepted()
                .header(RabbitMqHeaders.CORRELATION_ID, result.response().correlationId())
                .body(result.response());
    }
}
