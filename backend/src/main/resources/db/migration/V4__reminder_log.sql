CREATE TABLE IF NOT EXISTS reminder_log (
    id SERIAL PRIMARY KEY,
    appointment_id INTEGER REFERENCES appointments(id),
    patient_code VARCHAR(32),
    provider_code VARCHAR(32),
    scheduled_for TIMESTAMP WITH TIME ZONE,
    sent_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    message TEXT
);
