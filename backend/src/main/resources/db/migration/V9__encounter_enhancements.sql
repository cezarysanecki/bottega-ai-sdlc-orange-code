-- Enhance encounter tables for proper workflow
-- Add encounter type and improve audit trail

ALTER TABLE encounters ADD COLUMN IF NOT EXISTS encounter_type VARCHAR(50) DEFAULT 'IN_PERSON';
ALTER TABLE encounters ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT now();
ALTER TABLE encounters ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT now();

-- BUG(ENC-128): created_by field missing - should track which provider/user created encounter

-- Add notes field to encounter_procedures for detailed documentation
ALTER TABLE encounter_procedures ADD COLUMN IF NOT EXISTS notes VARCHAR(500);

-- Add index for faster encounter lookups by appointment
CREATE INDEX IF NOT EXISTS idx_encounters_appointment ON encounters(appointment_id);

-- Add unique constraint to prevent duplicate encounters per appointment
-- ALTER TABLE encounters ADD CONSTRAINT unique_appointment_encounter UNIQUE (appointment_id);

COMMENT ON COLUMN encounters.encounter_type IS 'Type of encounter: IN_PERSON, TELEHEALTH, PHONE';
COMMENT ON TABLE appointment_status_history IS 'Audit trail for appointment status changes';
