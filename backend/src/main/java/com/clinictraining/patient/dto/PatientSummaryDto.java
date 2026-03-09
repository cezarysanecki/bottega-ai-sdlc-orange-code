package com.clinictraining.patient.dto;

import java.time.LocalDate;

public class PatientSummaryDto {
    private String patientCode;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String insurancePlanCode;
    private String insuranceProvider;

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getInsurancePlanCode() {
        return insurancePlanCode;
    }

    public void setInsurancePlanCode(String insurancePlanCode) {
        this.insurancePlanCode = insurancePlanCode;
    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(String insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
    }
}
