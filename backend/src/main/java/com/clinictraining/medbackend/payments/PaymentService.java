package com.clinictraining.medbackend.payments;

import com.clinictraining.billing.model.Invoice;
import com.clinictraining.billing.model.Payment;
import com.clinictraining.billing.repository.InvoiceRepository;
import com.clinictraining.billing.repository.PaymentRepository;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(InvoiceRepository invoiceRepository, PaymentRepository paymentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Invoice recordPayment(PaymentRequest request) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(request.getInvoiceNumber())
                .orElseThrow(() -> new IllegalArgumentException("Unknown invoice " + request.getInvoiceNumber()));

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmountCents(request.getAmountCents());
        payment.setMethod(request.getMethod() == null ? "UNKNOWN" : request.getMethod());
        payment.setPaymentReference(request.getPaymentReference());
        payment.setPostedBy(request.getPostedBy() == null ? "api-user" : request.getPostedBy());
        payment.setReceivedAt(ZonedDateTime.now());

        paymentRepository.save(payment);
        invoice.addPayment(payment);

        int newBalance = invoice.getBalanceCents() - Math.abs(request.getAmountCents() == null ? 0 : request.getAmountCents());
        invoice.setBalanceCents(newBalance);
        if (newBalance <= 0) {
            invoice.setStatus("PAID");
        }
        invoice.setNotes((invoice.getNotes() == null ? "" : invoice.getNotes() + "\n") +
                "Payment received " + payment.getAmountCents() + " cents via " + payment.getMethod());

        log.info("Recorded payment {} for invoice {}", payment.getAmountCents(), request.getInvoiceNumber());
        return invoiceRepository.save(invoice);
    }
}
