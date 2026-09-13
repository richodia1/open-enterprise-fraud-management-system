package org.openefms.ingestionmanagement.controller;

import java.util.List;
import java.util.UUID;
import org.openefms.common.events.rabbitmq.RabbitMqHeaders;
import org.openefms.common.models.transaction.TransactionIngestionRequest;
import org.openefms.ingestionmanagement.dto.TransactionIngestionResponse;
import org.openefms.ingestionmanagement.entity.TransactionReceiptRecord;
import org.openefms.ingestionmanagement.service.TransactionIngestionResult;
import org.openefms.ingestionmanagement.service.TransactionIngestionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/ingestion/transactions")
public class TransactionIngestionController {
    private final TransactionIngestionService ingestionService;

    public TransactionIngestionController(TransactionIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransactionIngestionResponse> ingest(
            @RequestBody TransactionIngestionRequest request,
            @RequestHeader(value = RabbitMqHeaders.CORRELATION_ID, required = false) String correlationId,
            @RequestHeader(value = RabbitMqHeaders.TRACE_ID, required = false) String traceId) {
        TransactionIngestionResult result = ingestionService.receive(request, correlationId, traceId);
        return ResponseEntity
                .accepted()
                .header(RabbitMqHeaders.CORRELATION_ID, result.response().correlationId())
                .body(result.response());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TransactionReceiptRecord> receipts(@RequestParam String tenantId) {
        return ingestionService.receipts(tenantId);
    }

    @GetMapping(path = "/{transactionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TransactionReceiptRecord receipt(@PathVariable UUID transactionId) {
        return ingestionService.receipt(transactionId);
    }
}
