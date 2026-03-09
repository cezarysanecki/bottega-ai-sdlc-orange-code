package com.clinictraining.integration;

import static com.clinictraining.integration.builders.InvoiceTestBuilder.anInvoice;
import static com.clinictraining.integration.builders.PatientTestBuilder.aPatient;
import static com.clinictraining.medbackend.payments.builders.PaymentRequestBuilder.aPaymentRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.clinictraining.billing.model.Invoice;
import com.clinictraining.billing.repository.InvoiceRepository;
import com.clinictraining.billing.repository.PaymentRepository;
import com.clinictraining.medbackend.payments.PaymentRequest;
import com.clinictraining.medbackend.payments.PaymentService;
import com.clinictraining.patient.model.Patient;
import com.clinictraining.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for PaymentService.
 *
 * <p>These tests verify the end-to-end payment processing flow with a real PostgreSQL database
 * (provided by Testcontainers). Each test runs in a transaction that is automatically rolled back,
 * ensuring test isolation.
 */
class PaymentServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PatientRepository patientRepository;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        // No deleteAll() needed - @Transactional automatically rolls back after each test
        testPatient = patientRepository.save(
                aPatient()
                        .withName("Jane", "Smith")
                        .build()
        );
    }

    @Test
    void recordPaymentIntegrationTest() {
        Invoice invoice = invoiceRepository.save(
                anInvoice()
                        .withNumber("INV-IT-001")
                        .forPatient(testPatient)
                        .withBalance(10000)
                        .build()
        );

        PaymentRequest request = aPaymentRequest()
                .forInvoice("INV-IT-001")
                .withAmount(5000)
                .withMethod("CREDIT_CARD")
                .withReference("REF-IT-001")
                .postedBy("integration-test")
                .build();

        Invoice result = paymentService.recordPayment(request);

        assertNotNull(result);
        assertEquals(5000, result.getBalanceCents());
        assertEquals("OPEN", result.getStatus());
        assertEquals(1, result.getPayments().size());

        Invoice refreshedInvoice = invoiceRepository.findByInvoiceNumber("INV-IT-001").orElseThrow();
        assertEquals(5000, refreshedInvoice.getBalanceCents());
        assertEquals(1, refreshedInvoice.getPayments().size());
    }

    @Test
    void recordFullPaymentMarksInvoiceAsPaid() {
        Invoice invoice = invoiceRepository.save(
                anInvoice()
                        .withNumber("INV-IT-002")
                        .forPatient(testPatient)
                        .withBalance(10000)
                        .build()
        );

        PaymentRequest request = aPaymentRequest()
                .forInvoice("INV-IT-002")
                .withAmount(10000)
                .withMethod("CASH")
                .postedBy("test-user")
                .build();

        Invoice result = paymentService.recordPayment(request);

        assertEquals(0, result.getBalanceCents());
        assertEquals("PAID", result.getStatus());

        Invoice refreshedInvoice = invoiceRepository.findByInvoiceNumber("INV-IT-002").orElseThrow();
        assertEquals("PAID", refreshedInvoice.getStatus());
        assertEquals(1, refreshedInvoice.getPayments().size());
    }

    @Test
    void recordMultiplePaymentsOnSameInvoice() {
        Invoice invoice = invoiceRepository.save(
                anInvoice()
                        .withNumber("INV-IT-003")
                        .forPatient(testPatient)
                        .withBalance(15000)
                        .build()
        );

        PaymentRequest request1 = aPaymentRequest()
                .forInvoice("INV-IT-003")
                .withAmount(5000)
                .withMethod("CREDIT_CARD")
                .postedBy("user1")
                .build();

        PaymentRequest request2 = aPaymentRequest()
                .forInvoice("INV-IT-003")
                .withAmount(5000)
                .withMethod("CASH")
                .postedBy("user2")
                .build();

        PaymentRequest request3 = aPaymentRequest()
                .forInvoice("INV-IT-003")
                .withAmount(5000)
                .withMethod("CHECK")
                .postedBy("user3")
                .build();

        paymentService.recordPayment(request1);
        paymentService.recordPayment(request2);
        Invoice result = paymentService.recordPayment(request3);

        assertEquals(0, result.getBalanceCents());
        assertEquals("PAID", result.getStatus());

        Invoice refreshedInvoice = invoiceRepository.findByInvoiceNumber("INV-IT-003").orElseThrow();
        assertEquals(3, refreshedInvoice.getPayments().size());
        assertEquals(0, refreshedInvoice.getBalanceCents());
    }

    @Test
    void recordPartialPaymentKeepsInvoiceOpen() {
        Invoice invoice = invoiceRepository.save(
                anInvoice()
                        .withNumber("INV-IT-004")
                        .forPatient(testPatient)
                        .withBalance(20000)
                        .build()
        );

        PaymentRequest request = aPaymentRequest()
                .forInvoice("INV-IT-004")
                .withAmount(8000)
                .withMethod("CREDIT_CARD")
                .postedBy("test-user")
                .build();

        Invoice result = paymentService.recordPayment(request);

        assertEquals(12000, result.getBalanceCents());
        assertEquals("OPEN", result.getStatus());

        Invoice refreshedInvoice = invoiceRepository.findByInvoiceNumber("INV-IT-004").orElseThrow();
        assertEquals("OPEN", refreshedInvoice.getStatus());
        assertEquals(12000, refreshedInvoice.getBalanceCents());
    }

    @Test
    void recordPaymentUpdatesInvoiceNotes() {
        Invoice invoice = invoiceRepository.save(
                anInvoice()
                        .withNumber("INV-IT-005")
                        .forPatient(testPatient)
                        .withBalance(10000)
                        .withNotes("Initial notes")
                        .build()
        );

        PaymentRequest request = aPaymentRequest()
                .forInvoice("INV-IT-005")
                .withAmount(10000)
                .withMethod("WIRE_TRANSFER")
                .postedBy("admin")
                .build();

        Invoice result = paymentService.recordPayment(request);

        assertNotNull(result.getNotes());
        assertEquals("PAID", result.getStatus());

        Invoice refreshedInvoice = invoiceRepository.findByInvoiceNumber("INV-IT-005").orElseThrow();
        assertNotNull(refreshedInvoice.getNotes());
    }
}
