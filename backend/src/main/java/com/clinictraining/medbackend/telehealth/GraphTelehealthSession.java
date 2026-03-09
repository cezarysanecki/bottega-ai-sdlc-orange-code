package com.clinictraining.medbackend.telehealth;

import java.time.ZonedDateTime;

public class GraphTelehealthSession {
    private final TelehealthSessionView delegate;
    private final ZonedDateTime capturedAt;

    public GraphTelehealthSession(TelehealthSessionView delegate) {
        this.delegate = delegate;
        this.capturedAt = delegate.getLastSyncedAt();
    }

    public String getVendorSessionId() {
        return delegate.getVendorSessionId();
    }

    public String getStatus() {
        return delegate.getSessionStatus();
    }

    public String getFailureReason() {
        return delegate.getFailureReason();
    }

    public ZonedDateTime getStartedAt() {
        return delegate.getStartedAt();
    }

    public ZonedDateTime getEndedAt() {
        return delegate.getEndedAt();
    }

    public ZonedDateTime getLastSyncedAt() {
        return capturedAt;
    }
}
