package com.clinictraining.billing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EncounterProcedureId implements Serializable {

    @Column(name = "encounter_id")
    private Long encounterId;

    @Column(name = "procedure_id")
    private Long procedureId;

    public Long getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(Long encounterId) {
        this.encounterId = encounterId;
    }

    public Long getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(Long procedureId) {
        this.procedureId = procedureId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncounterProcedureId that = (EncounterProcedureId) o;
        return Objects.equals(encounterId, that.encounterId) &&
                Objects.equals(procedureId, that.procedureId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encounterId, procedureId);
    }
}
