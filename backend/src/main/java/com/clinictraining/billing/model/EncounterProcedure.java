package com.clinictraining.billing.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "encounter_procedures")
public class EncounterProcedure {

    @EmbeddedId
    private EncounterProcedureId id = new EncounterProcedureId();

    @MapsId("encounterId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id")
    private Encounter encounter;

    @MapsId("procedureId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id")
    private Procedure procedure;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "override_cost")
    private BigDecimal overrideCost;

    public EncounterProcedureId getId() {
        return id;
    }

    public void setId(EncounterProcedureId id) {
        this.id = id;
    }

    public Encounter getEncounter() {
        return encounter;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
        this.id.setEncounterId(encounter != null ? encounter.getId() : null);
    }

    public Procedure getProcedure() {
        return procedure;
    }

    public void setProcedure(Procedure procedure) {
        this.procedure = procedure;
        this.id.setProcedureId(procedure != null ? procedure.getId() : null);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getOverrideCost() {
        return overrideCost;
    }

    public void setOverrideCost(BigDecimal overrideCost) {
        this.overrideCost = overrideCost;
    }
}
