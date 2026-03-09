-- Add timezone support for providers and patients
-- This enables multi-location providers and telehealth with proper timezone handling

-- Add zone_id to providers (required for all providers)
ALTER TABLE providers
ADD COLUMN zone_id VARCHAR(50) NOT NULL DEFAULT 'America/Los_Angeles';

-- Remove default after initial population
ALTER TABLE providers
ALTER COLUMN zone_id DROP DEFAULT;

-- Add zone_id to patients (optional, mainly for telehealth)
ALTER TABLE patients
ADD COLUMN zone_id VARCHAR(50);

-- Add index for common queries
CREATE INDEX idx_providers_zone_id ON providers(zone_id);
CREATE INDEX idx_patients_zone_id ON patients(zone_id);
