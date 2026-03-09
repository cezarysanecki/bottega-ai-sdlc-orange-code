package com.clinictraining.appointment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.ZonedDateTime;

@Embeddable
public class AppointmentSlot {

    @Column(name = "start_time", nullable = false)
    private ZonedDateTime start;

    @Column(name = "end_time", nullable = false)
    private ZonedDateTime end;

    public AppointmentSlot() {
    }

    public AppointmentSlot(ZonedDateTime start, ZonedDateTime end) {
        this.start = start;
        this.end = end;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

    public ZonedDateTime getEnd() {
        return end;
    }

    public void setEnd(ZonedDateTime end) {
        this.end = end;
    }
}
