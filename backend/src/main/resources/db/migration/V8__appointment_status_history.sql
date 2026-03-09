-- Track appointment status changes for audit purposes
CREATE TABLE appointment_status_history (
    id SERIAL PRIMARY KEY,
    appointment_id INTEGER NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    from_status VARCHAR(24) NOT NULL,
    to_status VARCHAR(24) NOT NULL,
    transitioned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    transitioned_by VARCHAR(60),  -- TODO: FK to users table when auth is complete
    notes VARCHAR(500)
);

CREATE INDEX idx_appointment_status_history_appointment
    ON appointment_status_history(appointment_id, transitioned_at DESC);

-- BUG(APPT-127): Missing index on transitioned_at for time-based queries
