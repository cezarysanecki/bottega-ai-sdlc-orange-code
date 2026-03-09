package com.clinictraining.integration.builders;

import com.clinictraining.billing.model.Invoice;
import com.clinictraining.patient.model.Patient;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Test data builder for Invoice entities used in integration tests.
 *
 * <p>Provides a fluent API for creating Invoice instances with sensible defaults.
 *
 * <p>Example usage:
 * <pre>
 * Invoice invoice = anInvoice()
 *     .withNumber("INV-123")
 *     .forPatient(testPatient)
 *     .withBalance(10000)
 *     .build();
 * </pre>
 */
public class InvoiceTestBuilder {

    private String invoiceNumber = "INV-TEST-001";
    private Patient patient;
    private String status = "OPEN";
    private ZonedDateTime issuedAt = ZonedDateTime.now();
    private Integer balanceCents = 10000;
    private String notes;

    /**
     * Creates a new builder with default values.
     */
    public static InvoiceTestBuilder anInvoice() {
        return new InvoiceTestBuilder();
    }

    /**
     * Sets the invoice number.
     */
    public InvoiceTestBuilder withNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
        return this;
    }

    /**
     * Sets the patient for this invoice.
     */
    public InvoiceTestBuilder forPatient(Patient patient) {
        this.patient = patient;
        return this;
    }

    /**
     * Sets the invoice status (e.g., OPEN, PAID).
     */
    public InvoiceTestBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * Sets the invoice issued date.
     */
    public InvoiceTestBuilder issuedAt(ZonedDateTime issuedAt) {
        this.issuedAt = issuedAt;
        return this;
    }

    /**
     * Sets the invoice balance in cents.
     */
    public InvoiceTestBuilder withBalance(Integer balanceCents) {
        this.balanceCents = balanceCents;
        return this;
    }

    /**
     * Sets the invoice notes.
     */
    public InvoiceTestBuilder withNotes(String notes) {
        this.notes = notes;
        return this;
    }

    /**
     * Builds the Invoice with the configured values.
     * Note: This creates the entity but does not persist it. Call repository.save() separately.
     */
    public Invoice build() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setPatient(patient);
        invoice.setStatus(status);
        invoice.setIssuedAt(issuedAt);
        invoice.setTotalAmount(BigDecimal.valueOf(balanceCents / 100.0));
        invoice.setBalanceCents(balanceCents);
        invoice.setNotes(notes);
        return invoice;
    }
}
