package com.clinictraining.medbackend.payments.builders;

import com.clinictraining.medbackend.payments.PaymentRequest;

/**
 * Test data builder for PaymentRequest objects.
 *
 * <p>Provides a fluent API for creating PaymentRequest instances in tests with sensible defaults.
 * Only override values that are relevant to the specific test scenario.
 *
 * <p>Example usage:
 * <pre>
 * PaymentRequest request = aPaymentRequest()
 *     .forInvoice("INV-123")
 *     .withAmount(10000)
 *     .withMethod("CREDIT_CARD")
 *     .build();
 * </pre>
 */
public class PaymentRequestBuilder {

    private String invoiceNumber = "INV-TEST-001";
    private Integer amountCents = 10000;
    private String method = "CASH";
    private String paymentReference = "REF-TEST-001";
    private String postedBy = "test-user";

    /**
     * Creates a new builder with default values.
     */
    public static PaymentRequestBuilder aPaymentRequest() {
        return new PaymentRequestBuilder();
    }

    /**
     * Sets the invoice number for this payment.
     */
    public PaymentRequestBuilder forInvoice(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
        return this;
    }

    /**
     * Sets the payment amount in cents.
     */
    public PaymentRequestBuilder withAmount(Integer amountCents) {
        this.amountCents = amountCents;
        return this;
    }

    /**
     * Sets the payment method (e.g., CASH, CREDIT_CARD, CHECK).
     */
    public PaymentRequestBuilder withMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * Sets the payment reference number.
     */
    public PaymentRequestBuilder withReference(String paymentReference) {
        this.paymentReference = paymentReference;
        return this;
    }

    /**
     * Sets who posted/recorded this payment.
     */
    public PaymentRequestBuilder postedBy(String postedBy) {
        this.postedBy = postedBy;
        return this;
    }

    /**
     * Builds the PaymentRequest with the configured values.
     */
    public PaymentRequest build() {
        PaymentRequest request = new PaymentRequest();
        request.setInvoiceNumber(invoiceNumber);
        request.setAmountCents(amountCents);
        request.setMethod(method);
        request.setPaymentReference(paymentReference);
        request.setPostedBy(postedBy);
        return request;
    }
}
