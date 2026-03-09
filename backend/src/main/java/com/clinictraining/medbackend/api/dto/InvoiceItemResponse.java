package com.clinictraining.medbackend.api.dto;

import java.math.BigDecimal;

public class InvoiceItemResponse {
    private String lineItemCode;
    private String description;
    private Integer quantity;
    private BigDecimal unitAmount;
    private Integer legacyTaxCents;

    public String getLineItemCode() {
        return lineItemCode;
    }

    public void setLineItemCode(String lineItemCode) {
        this.lineItemCode = lineItemCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitAmount() {
        return unitAmount;
    }

    public void setUnitAmount(BigDecimal unitAmount) {
        this.unitAmount = unitAmount;
    }

    public Integer getLegacyTaxCents() {
        return legacyTaxCents;
    }

    public void setLegacyTaxCents(Integer legacyTaxCents) {
        this.legacyTaxCents = legacyTaxCents;
    }
}
