package com.clinictraining.medbackend.web;

import com.clinictraining.medbackend.api.ApiMapper;
import com.clinictraining.medbackend.api.dto.InvoiceResponse;
import com.clinictraining.medbackend.payments.PaymentRequest;
import com.clinictraining.medbackend.payments.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final ApiMapper apiMapper;

    public PaymentController(PaymentService paymentService, ApiMapper apiMapper) {
        this.paymentService = paymentService;
        this.apiMapper = apiMapper;
    }

    @PostMapping
    public ResponseEntity<InvoiceResponse> record(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(apiMapper.toResponse(paymentService.recordPayment(request)));
    }
}
