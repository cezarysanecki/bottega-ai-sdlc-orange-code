package com.clinictraining.medbackend.api.dto;

import com.clinictraining.patient.dto.PatientSummaryDto;
import com.clinictraining.patient.dto.ProviderSummaryDto;
import java.time.ZonedDateTime;
import java.util.UUID;

public class AppointmentResponse {
    private Long id;
    private UUID appointmentUid;
    private String appointmentCode;
    private String status;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private PatientSummaryDto patient;
    private ProviderSummaryDto provider;
    private String roomCode;
    private String legacyNotes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getAppointmentUid() {
        return appointmentUid;
    }

    public void setAppointmentUid(UUID appointmentUid) {
        this.appointmentUid = appointmentUid;
    }

    public String getAppointmentCode() {
        return appointmentCode;
    }

    public void setAppointmentCode(String appointmentCode) {
        this.appointmentCode = appointmentCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
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

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getLegacyNotes() {
        return legacyNotes;
    }

    public void setLegacyNotes(String legacyNotes) {
        this.legacyNotes = legacyNotes;
    }
}
