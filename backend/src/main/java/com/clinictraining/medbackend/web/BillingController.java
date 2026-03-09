package com.clinictraining.medbackend.web;

import com.clinictraining.billing.BillingCalculator;
import com.clinictraining.billing.InvoiceService;
import com.clinictraining.billing.model.Invoice;
import com.clinictraining.billing.repository.InvoiceRepository;
import com.clinictraining.integration.PdfExportClient;
import com.clinictraining.integration.TelehealthSoapClient;
import com.clinictraining.medbackend.api.ApiMapper;
import com.clinictraining.medbackend.api.dto.InvoiceResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/invoices")
public class BillingController {

    private final BillingCalculator billingCalculator;
    private final TelehealthSoapClient telehealthSoapClient;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final ApiMapper apiMapper;
    private final PdfExportClient pdfExportClient;

    public BillingController(BillingCalculator billingCalculator,
                             TelehealthSoapClient telehealthSoapClient,
                             InvoiceRepository invoiceRepository,
                             InvoiceService invoiceService,
                             ApiMapper apiMapper,
                             PdfExportClient pdfExportClient) {
        this.billingCalculator = billingCalculator;
        this.telehealthSoapClient = telehealthSoapClient;
        this.invoiceRepository = invoiceRepository;
        this.invoiceService = invoiceService;
        this.apiMapper = apiMapper;
        this.pdfExportClient = pdfExportClient;
    }

    @GetMapping("/outstanding")
    public ResponseEntity<Map<String, Object>> outstanding() {
        BigDecimal openBalance = invoiceRepository.findAll().stream()
                .filter(invoice -> "OPEN".equalsIgnoreCase(invoice.getStatus()))
                .map(invoice -> BigDecimal.valueOf(invoice.getBalanceCents()).movePointLeft(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Legacy integration ping to show dependency
        telehealthSoapClient.fetchSession("SESSION-42");
        return ResponseEntity.ok(Map.of("amount", openBalance));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<InvoiceResponse>> list() {
        List<InvoiceResponse> invoices = invoiceRepository.findAll().stream()
                .map(apiMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{invoiceNumber}")
    @Transactional(readOnly = true)
    public ResponseEntity<InvoiceResponse> findByNumber(@PathVariable String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .map(apiMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/generate")
    public ResponseEntity<InvoiceResponse> generate(@RequestBody Map<String, String> payload) {
        String encounterCode = payload.getOrDefault("encounterCode", "");
        Invoice invoice = invoiceService.generateInvoice(encounterCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiMapper.toResponse(invoice));
    }

    @PostMapping("/export")
    public ResponseEntity<Map<String, Object>> export(@RequestBody Map<String, String> payload) {
        String invoiceNumber = payload.getOrDefault("invoiceNumber", "");
        return ResponseEntity.ok(pdfExportClient.exportInvoice(invoiceNumber)
                .blockOptional()
                .map(response -> Map.<String, Object>of(
                        "invoiceNumber", invoiceNumber,
                        "downloadUrl", response.getDownloadUrl(),
                        "token", response.getToken()))
                .orElse(Map.of("invoiceNumber", invoiceNumber, "status", "PENDING")));
    }
}
