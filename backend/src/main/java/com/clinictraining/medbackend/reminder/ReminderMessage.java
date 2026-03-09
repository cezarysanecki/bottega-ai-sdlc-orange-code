package com.clinictraining.medbackend.reminder;

import java.time.ZonedDateTime;

public class ReminderMessage {
    private final Long appointmentId;
    private final String patientCode;
    private final String providerCode;
    private final ZonedDateTime scheduledFor;
    private final String message;

    public ReminderMessage(Long appointmentId, String patientCode, String providerCode,
                           ZonedDateTime scheduledFor, String message) {
        this.appointmentId = appointmentId;
        this.patientCode = patientCode;
        this.providerCode = providerCode;
        this.scheduledFor = scheduledFor;
        this.message = message;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public ZonedDateTime getScheduledFor() {
        return scheduledFor;
    }

    public String getMessage() {
        return message;
    }
}
