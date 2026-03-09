package com.clinictraining.medbackend.payments;

import com.clinictraining.billing.model.Invoice;
import com.clinictraining.billing.repository.InvoiceRepository;
import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentReconciliationJob {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationJob.class);

    private final InvoiceRepository invoiceRepository;

    public PaymentReconciliationJob(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Scheduled(cron = "${payments.reconciliation.cron:0 45 6 * * *}")
    public void flagOverdueInvoices() {
        ZonedDateTime now = ZonedDateTime.now();
        List<Invoice> invoices = invoiceRepository.findAll();
        invoices.stream()
                .filter(invoice -> invoice.getDueDate() != null)
                .filter(invoice -> invoice.getDueDate().isBefore(now))
                .filter(invoice -> invoice.getBalanceCents() != null && invoice.getBalanceCents() > 0)
                .limit(25)
                .forEach(this::markOverdue);
    }

    private void markOverdue(Invoice invoice) {
        String note = (invoice.getNotes() == null ? "" : invoice.getNotes() + "\n") +
                "Overdue as of " + ZonedDateTime.now() + " (auto-flagged)";
        invoice.setNotes(note);
        invoiceRepository.save(invoice);
        log.warn("Invoice {} is overdue with balance {}", invoice.getInvoiceNumber(), invoice.getBalanceCents());
    }
}
