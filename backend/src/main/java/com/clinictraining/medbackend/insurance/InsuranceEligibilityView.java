package com.clinictraining.medbackend.insurance;

import java.time.ZonedDateTime;

public class InsuranceEligibilityView {
    private String patientCode;
    private String planCode;
    private String status;
    private Integer copay;
    private Integer deductibleRemaining;
    private String coverageNotes;
    private ZonedDateTime fetchedAt;
    private boolean fromCache;

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCopay() {
        return copay;
    }

    public void setCopay(Integer copay) {
        this.copay = copay;
    }

    public Integer getDeductibleRemaining() {
        return deductibleRemaining;
    }

    public void setDeductibleRemaining(Integer deductibleRemaining) {
        this.deductibleRemaining = deductibleRemaining;
    }

    public String getCoverageNotes() {
        return coverageNotes;
    }

    public void setCoverageNotes(String coverageNotes) {
        this.coverageNotes = coverageNotes;
    }

    public ZonedDateTime getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(ZonedDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }
}
