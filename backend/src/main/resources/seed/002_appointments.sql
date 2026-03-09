INSERT INTO appointments(patient_id, provider_id, room_id, appointment_type_id, appointment_code, start_time, end_time, status, legacy_notes)
SELECT p.id, pr.id, r.id, at.id,
       'APPT-SEED001',
       NOW() + interval '4 hours',
       NOW() + interval '5 hours',
       'SCHEDULED',
       'Imported from legacy EMR'
FROM patients p
JOIN providers pr ON pr.provider_code = 'PROV-001'
JOIN rooms r ON r.room_code = 'A201'
JOIN appointment_types at ON at.type_code = 'CHECKUP'
WHERE p.patient_code = 'PAT-1001'
ON CONFLICT DO NOTHING;

INSERT INTO appointments(patient_id, provider_id, room_id, appointment_type_id, appointment_code, start_time, end_time, status, legacy_notes)
SELECT p.id, pr.id, r.id, at.id,
       'APPT-SEED002',
       NOW() + interval '1 day 2 hours',
       NOW() + interval '1 day 3 hours',
       'SCHEDULED',
       'Telehealth onboarding call'
FROM patients p
JOIN providers pr ON pr.provider_code = 'PROV-002'
JOIN rooms r ON r.room_code = 'TEL-LOUNGE'
JOIN appointment_types at ON at.type_code = 'TEL_VISIT'
WHERE p.patient_code = 'PAT-1002'
ON CONFLICT DO NOTHING;

INSERT INTO encounters(appointment_id, encounter_code, encounter_time, reason, summary)
SELECT a.id, 'ENC-2001', a.start_time + interval '45 minutes', 'Routine Checkup', 'Vitals taken, labs pending'
FROM appointments a
JOIN patients p ON p.patient_code = 'PAT-1001'
WHERE a.patient_id = p.id
ON CONFLICT (encounter_code) DO NOTHING;

INSERT INTO encounter_procedures(encounter_id, procedure_id, quantity)
SELECT e.id, pr.id, 1
FROM encounters e
JOIN procedures pr ON pr.procedure_code = 'GEN-001'
WHERE e.encounter_code = 'ENC-2001'
ON CONFLICT DO NOTHING;

INSERT INTO invoices(invoice_number, encounter_id, patient_id, status, issued_at, due_date, total_amount, balance_cents, notes)
SELECT 'INV-9001', e.id, a.patient_id, 'OPEN', NOW(), NOW() + interval '30 days', 67.34, 6734, 'Includes mysterious legacy fee'
FROM encounters e
JOIN appointments a ON a.id = e.appointment_id
WHERE e.encounter_code = 'ENC-2001'
ON CONFLICT (invoice_number) DO NOTHING;

INSERT INTO invoice_items(invoice_id, line_item_code, description, quantity, unit_amount, legacy_tax_cents)
SELECT i.id, 'GEN-001', 'General consultation', 1, 55.00, 234
FROM invoices i
WHERE i.invoice_number = 'INV-9001'
ON CONFLICT DO NOTHING;

INSERT INTO invoice_items(invoice_id, line_item_code, description, quantity, unit_amount, legacy_tax_cents)
SELECT i.id, 'ADJ-LEG', 'Legacy adjustment fee', 1, 12.34, NULL
FROM invoices i
WHERE i.invoice_number = 'INV-9001'
ON CONFLICT DO NOTHING;

INSERT INTO payments(invoice_id, payment_reference, method, amount_cents, received_at, posted_by)
SELECT i.id, 'PAY-1001', 'CREDIT_CARD', 2500, NOW() - interval '2 days', 'import-script'
FROM invoices i
WHERE i.invoice_number = 'INV-9001'
ON CONFLICT DO NOTHING;

INSERT INTO telehealth_sessions(appointment_id, vendor_session_id, sessionStatus, started_at, ended_at, failure_reason)
SELECT a.id, 'TH-SESSION-42', 'READY', NOW() + interval '1 day 1 hour', NULL, NULL
FROM appointments a
JOIN appointment_types at ON at.id = a.appointment_type_id AND at.type_code = 'TEL_VISIT'
ON CONFLICT (vendor_session_id) DO NOTHING;
