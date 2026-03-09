-- Development seed data. Intentionally sparse and slightly inconsistent.
INSERT INTO insurance_plans(plan_code, provider_name, coverage_notes)
VALUES
  ('HMO-ALPHA', 'Alpha Health', 'Requires referrals, legacy import pending'),
  ('PPO-BETA', 'Beta Care', 'Offers telehealth coverage (verify annually)')
ON CONFLICT (plan_code) DO NOTHING;

INSERT INTO providers(provider_code, first_name, last_name, specialty, npi, office_room, preferred_contact, zone_id)
VALUES
  ('PROV-001', 'Maya', 'Singh', 'Internal Medicine', '1234567890', 'A201', 'msingh@medlink.local', 'America/Los_Angeles'),
  ('PROV-002', 'Evan', 'Lee', 'Family Medicine', '0987654321', 'TEL-LOUNGE', 'sms:+15551234567', 'America/New_York')
ON CONFLICT (provider_code) DO NOTHING;

INSERT INTO rooms(room_code, floor, capacity, equipment)
VALUES
  ('A201', 2, 1, '{"devices": ["EKG", "Vitals"]}'),
  ('TEL-LOUNGE', 5, 2, '{"note": "Shared telehealth booth"}')
ON CONFLICT (room_code) DO NOTHING;

INSERT INTO patients(patient_code, first_name, last_name, date_of_birth, gender, insurance_plan_id, primary_phone, email, emergency_contact, allergies, preferred_language, zone_id)
SELECT 'PAT-1001', 'Jordan', 'Miles', '1985-02-14', 'female', id, '+15558675309', 'jordan.miles@example.com', '{"name": "Sam Miles", "phone": "+15557654321"}', 'Peanuts', 'English', NULL
FROM insurance_plans WHERE plan_code = 'PPO-BETA'
ON CONFLICT (patient_code) DO NOTHING;

INSERT INTO patients(patient_code, first_name, last_name, date_of_birth, gender, insurance_plan_id, primary_phone, email, emergency_contact, allergies, preferred_language, zone_id)
SELECT 'PAT-1002', 'Luis', 'Garcia', '1992-09-03', 'male', id, '+15559871234', 'luis.garcia@example.com', '{"name": "Maria Garcia", "phone": "+15550987654"}', 'Penicillin', 'Spanish', 'America/Chicago'
FROM insurance_plans WHERE plan_code = 'HMO-ALPHA'
ON CONFLICT (patient_code) DO NOTHING;

