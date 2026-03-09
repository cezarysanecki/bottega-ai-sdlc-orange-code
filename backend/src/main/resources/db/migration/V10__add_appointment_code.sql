-- Add appointment_code column for human-friendly appointment identifiers
ALTER TABLE appointments ADD COLUMN appointment_code VARCHAR(20);

-- Create unique index on appointment_code (will be enforced after backfill)
CREATE INDEX idx_appointments_code ON appointments(appointment_code);

-- Backfill existing appointments with generated codes
-- Format: APPT-{sequential_number}
UPDATE appointments
SET appointment_code = 'APPT-' || LPAD(id::TEXT, 6, '0')
WHERE appointment_code IS NULL;

-- Make appointment_code NOT NULL and UNIQUE after backfill
ALTER TABLE appointments ALTER COLUMN appointment_code SET NOT NULL;
ALTER TABLE appointments ADD CONSTRAINT uk_appointment_code UNIQUE (appointment_code);
