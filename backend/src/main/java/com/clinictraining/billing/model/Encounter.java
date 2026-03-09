package com.clinictraining.billing.model;

import com.clinictraining.appointment.model.Appointment;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "encounters")
public class Encounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "encounter_code", nullable = false, unique = true)
    private String encounterCode;

    @Column(name = "encounter_time", nullable = false)
    private ZonedDateTime encounterTime;

    @Column(name = "reason")
    private String reason;

    @Column(name = "summary")
    private String summary;

    @OneToMany(mappedBy = "encounter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EncounterProcedure> procedures = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public String getEncounterCode() {
        return encounterCode;
    }

    public void setEncounterCode(String encounterCode) {
        this.encounterCode = encounterCode;
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

    public List<EncounterProcedure> getProcedures() {
        return procedures;
    }

    public void setProcedures(List<EncounterProcedure> procedures) {
        this.procedures = procedures;
    }

    public void addProcedure(EncounterProcedure procedure) {
        procedure.setEncounter(this);
        this.procedures.add(procedure);
    }
}
