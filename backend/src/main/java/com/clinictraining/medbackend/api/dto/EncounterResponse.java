package com.clinictraining.medbackend.api.dto;

import com.clinictraining.patient.dto.PatientSummaryDto;
import com.clinictraining.patient.dto.ProviderSummaryDto;
import java.time.ZonedDateTime;
import java.util.List;

public class EncounterResponse {
    private Long id;
    private String encounterCode;
    private Long appointmentId;
    private String appointmentCode;
    private ZonedDateTime encounterTime;
    private String reason;
    private String summary;
    private PatientSummaryDto patient;
    private ProviderSummaryDto provider;
    private List<EncounterProcedureResponse> procedures;

    public static class EncounterProcedureResponse {
        private String procedureCode;
        private String description;
        private Integer quantity;

        public String getProcedureCode() {
            return procedureCode;
        }

        public void setProcedureCode(String procedureCode) {
            this.procedureCode = procedureCode;
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
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEncounterCode() {
        return encounterCode;
    }

    public void setEncounterCode(String encounterCode) {
        this.encounterCode = encounterCode;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getAppointmentCode() {
        return appointmentCode;
    }

    public void setAppointmentCode(String appointmentCode) {
        this.appointmentCode = appointmentCode;
    }

    public PatientSummaryDto getPatient() {
        return patient;
    }

    public void setPatient(PatientSummaryDto patient) {
        this.patient = patient;
    }

    public ProviderSummaryDto getProvider() {
        return provider;
    }

    public void setProvider(ProviderSummaryDto provider) {
        this.provider = provider;
    }

    public ZonedDateTime getEncounterTime() {
        return encounterTime;
    }

    public void setEncounterTime(ZonedDateTime encounterTime) {
        this.encounterTime = encounterTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<EncounterProcedureResponse> getProcedures() {
        return procedures;
    }

    public void setProcedures(List<EncounterProcedureResponse> procedures) {
        this.procedures = procedures;
    }
}
