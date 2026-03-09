package com.clinictraining.medbackend.telehealth;

import java.time.ZonedDateTime;

public class TelehealthSessionView {
    private String vendorSessionId;
    private String sessionStatus;
    private String failureReason;
    private ZonedDateTime startedAt;
    private ZonedDateTime endedAt;
    private ZonedDateTime lastSyncedAt;

    public String getVendorSessionId() {
        return vendorSessionId;
    }

    public void setVendorSessionId(String vendorSessionId) {
        this.vendorSessionId = vendorSessionId;
    }

    public String getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public ZonedDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(ZonedDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public ZonedDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(ZonedDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public ZonedDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(ZonedDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}
