package com.clinictraining.medbackend.payments;

import static com.clinictraining.medbackend.payments.builders.PaymentRequestBuilder.aPaymentRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clinictraining.billing.model.Invoice;
import com.clinictraining.billing.model.Payment;
import com.clinictraining.billing.repository.InvoiceRepository;
import com.clinictraining.billing.repository.PaymentRepository;
import com.clinictraining.patient.model.Patient;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for PaymentService.
 *
 * <p>These tests verify the business logic of payment processing using mocked dependencies.
 * Each test focuses on a specific scenario with clear, self-documenting constants.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    // Invoice amounts (in cents)
    private static final int BALANCE_10K = 10000;
    private static final int BALANCE_15K = 15000;
    private static final int BALANCE_5K = 5000;

    // Payment amounts (in cents)
    private static final int PARTIAL_PAYMENT_5K = 5000;
    private static final int PARTIAL_PAYMENT_7500 = 7500;
    private static final int FULL_PAYMENT_10K = 10000;

    // Expected results
    private static final int REMAINING_BALANCE_5K = 5000;
    private static final int ZERO_BALANCE = 0;

    // Status constants
    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_PAID = "PAID";

    // Payment methods
    private static final String METHOD_CASH = "CASH";
    private static final String METHOD_CREDIT_CARD = "CREDIT_CARD";
    private static final String METHOD_UNKNOWN = "UNKNOWN";

    // Invoice numbers
    private static final String INVOICE_1001 = "INV-1001";
    private static final String INVOICE_1002 = "INV-1002";
    private static final String INVOICE_1003 = "INV-1003";
    private static final String INVOICE_1004 = "INV-1004";
    private static final String INVOICE_1005 = "INV-1005";

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void recordPaymentUpdatesInvoiceBalance() {
        Invoice invoice = createSampleInvoice(INVOICE_1001, BALANCE_10K);
        PaymentRequest request = aPaymentRequest()
                .forInvoice(INVOICE_1001)
                .withAmount(PARTIAL_PAYMENT_5K)
                .build();

        when(invoiceRepository.findByInvoiceNumber(INVOICE_1001)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        Invoice result = paymentService.recordPayment(request);

        assertEquals(REMAINING_BALANCE_5K, result.getBalanceCents());
        assertEquals(STATUS_OPEN, result.getStatus());
        verify(paymentRepository).save(any(Payment.class));
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void recordPaymentMarksInvoiceAsPaidWhenFullyPaid() {
        Invoice invoice = createSampleInvoice(INVOICE_1002, BALANCE_10K);
        PaymentRequest request = aPaymentRequest()
                .forInvoice(INVOICE_1002)
                .withAmount(FULL_PAYMENT_10K)
                .build();

        when(invoiceRepository.findByInvoiceNumber(INVOICE_1002)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        Invoice result = paymentService.recordPayment(request);

        assertEquals(ZERO_BALANCE, result.getBalanceCents());
        assertEquals(STATUS_PAID, result.getStatus());
    }

    @Test
    void recordPaymentCreatesPaymentWithCorrectDetails() {
        Invoice invoice = createSampleInvoice(INVOICE_1003, BALANCE_15K);
        PaymentRequest request = aPaymentRequest()
                .forInvoice(INVOICE_1003)
                .withAmount(PARTIAL_PAYMENT_7500)
                .withMethod(METHOD_CREDIT_CARD)
                .withReference("REF-12345")
                .postedBy("admin-user")
                .build();

        when(invoiceRepository.findByInvoiceNumber(INVOICE_1003)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        paymentService.recordPayment(request);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();
        assertEquals(PARTIAL_PAYMENT_7500, savedPayment.getAmountCents());
        assertEquals(METHOD_CREDIT_CARD, savedPayment.getMethod());
        assertEquals("REF-12345", savedPayment.getPaymentReference());
        assertEquals("admin-user", savedPayment.getPostedBy());
        assertNotNull(savedPayment.getReceivedAt());
    }

    @Test
    void recordPaymentDefaultsMethodToUnknownWhenNull() {
        Invoice invoice = createSampleInvoice(INVOICE_1004, BALANCE_5K);
        PaymentRequest request = aPaymentRequest()
                .forInvoice(INVOICE_1004)
                .withAmount(BALANCE_5K)
                .withMethod(null)
                .build();

        when(invoiceRepository.findByInvoiceNumber(INVOICE_1004)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        paymentService.recordPayment(request);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        assertEquals(METHOD_UNKNOWN, paymentCaptor.getValue().getMethod());
    }

    @Test
    void recordPaymentDefaultsPostedByToApiUserWhenNull() {
        Invoice invoice = createSampleInvoice(INVOICE_1005, BALANCE_5K);
        PaymentRequest request = aPaymentRequest()
                .forInvoice(INVOICE_1005)
                .withAmount(BALANCE_5K)
                .postedBy(null)
                .build();

        when(invoiceRepository.findByInvoiceNumber(INVOICE_1005)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        paymentService.recordPayment(request);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        assertEquals("api-user", paymentCaptor.getValue().getPostedBy());
    }

    private Invoice createSampleInvoice(String invoiceNumber, int balanceCents) {
        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setPatient(patient);
        invoice.setStatus(STATUS_OPEN);
        invoice.setIssuedAt(ZonedDateTime.now());
        invoice.setTotalAmount(BigDecimal.valueOf(balanceCents / 100.0));
        invoice.setBalanceCents(balanceCents);
        return invoice;
    }
}
