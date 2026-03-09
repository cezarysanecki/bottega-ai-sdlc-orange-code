-- Core clinical schema setup
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE TABLE insurance_plans (
    id SERIAL PRIMARY KEY,
    plan_code VARCHAR(32) NOT NULL UNIQUE,
    provider_name VARCHAR(120) NOT NULL,
    coverage_notes TEXT
);

CREATE TABLE providers (
    id SERIAL PRIMARY KEY,
    provider_code VARCHAR(32) NOT NULL UNIQUE,
    first_name VARCHAR(60) NOT NULL,
    last_name VARCHAR(60) NOT NULL,
    specialty VARCHAR(80) NOT NULL,
    npi VARCHAR(20),
    office_room VARCHAR(10),
    preferred_contact VARCHAR(64)
);

CREATE TABLE patients (
    id SERIAL PRIMARY KEY,
    patient_code VARCHAR(32) NOT NULL UNIQUE,
    first_name VARCHAR(60) NOT NULL,
    last_name VARCHAR(60) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(12),
    insurance_plan_id INTEGER REFERENCES insurance_plans(id),
    primary_phone VARCHAR(24),
    email VARCHAR(120),
    emergency_contact JSONB,
    allergies TEXT,
    preferred_language VARCHAR(40),
    address_line1 VARCHAR(120),
    address_line2 VARCHAR(120),
    city VARCHAR(80),
    state VARCHAR(40),
    postal_code VARCHAR(16)
);

CREATE TABLE rooms (
    id SERIAL PRIMARY KEY,
    room_code VARCHAR(16) NOT NULL UNIQUE,
    floor INTEGER,
    capacity INTEGER,
    equipment JSONB
);

CREATE TABLE appointment_types (
    id SERIAL PRIMARY KEY,
    type_code VARCHAR(24) NOT NULL UNIQUE,
    description VARCHAR(160)
);

CREATE TABLE appointments (
    id SERIAL PRIMARY KEY,
    appointment_uid UUID DEFAULT gen_random_uuid() NOT NULL,
    patient_id INTEGER NOT NULL REFERENCES patients(id),
    provider_id INTEGER NOT NULL REFERENCES providers(id),
    room_id INTEGER REFERENCES rooms(id),
    appointment_type_id INTEGER REFERENCES appointment_types(id),
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(24) NOT NULL,
    legacy_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_appointments_provider_time ON appointments(provider_id, start_time);
CREATE INDEX idx_appointments_patient_time ON appointments(patient_id, start_time);

CREATE TABLE encounters (
    id SERIAL PRIMARY KEY,
    appointment_id INTEGER NOT NULL REFERENCES appointments(id),
    encounter_code VARCHAR(32) NOT NULL UNIQUE,
    encounter_time TIMESTAMP WITH TIME ZONE NOT NULL,
    reason TEXT,
    summary TEXT
);

CREATE TABLE procedures (
    id SERIAL PRIMARY KEY,
    procedure_code VARCHAR(24) NOT NULL UNIQUE,
    description VARCHAR(160),
    default_cost NUMERIC(10,2) NOT NULL
);

CREATE TABLE encounter_procedures (
    encounter_id INTEGER NOT NULL REFERENCES encounters(id) ON DELETE CASCADE,
    procedure_id INTEGER NOT NULL REFERENCES procedures(id),
    quantity INTEGER NOT NULL DEFAULT 1,
    override_cost NUMERIC(10,2),
    PRIMARY KEY (encounter_id, procedure_id)
);

CREATE TABLE invoices (
    id SERIAL PRIMARY KEY,
    invoice_number VARCHAR(32) NOT NULL UNIQUE,
    encounter_id INTEGER,
    patient_id INTEGER NOT NULL REFERENCES patients(id),
    status VARCHAR(24) NOT NULL,
    issued_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    due_date TIMESTAMP WITH TIME ZONE,
    total_amount NUMERIC(10,2) NOT NULL,
    balance_cents INTEGER NOT NULL,
    notes TEXT
    -- TODO: add FK to encounters when legacy import job is retired.
);

CREATE TABLE invoice_items (
    id SERIAL PRIMARY KEY,
    invoice_id INTEGER NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    line_item_code VARCHAR(24) NOT NULL,
    description VARCHAR(160),
    quantity INTEGER NOT NULL,
    unit_amount NUMERIC(10,2) NOT NULL,
    legacy_tax_cents INTEGER
);

CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    invoice_id INTEGER NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    payment_reference VARCHAR(48),
    method VARCHAR(24) NOT NULL,
    amount_cents INTEGER NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    posted_by VARCHAR(60)
);

CREATE TABLE telehealth_sessions (
    id SERIAL PRIMARY KEY,
    appointment_id INTEGER REFERENCES appointments(id),
    vendor_session_id VARCHAR(64) UNIQUE,
    sessionStatus VARCHAR(24) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE,
    failure_reason TEXT
);

INSERT INTO appointment_types (type_code, description) VALUES
    ('CHECKUP', 'General checkup'),
    ('FOLLOWUP', 'Follow up visit'),
    ('TEL_VISIT', 'Telehealth visit')
ON CONFLICT (type_code) DO NOTHING;

INSERT INTO procedures (procedure_code, description, default_cost) VALUES
    ('XR-101', 'X-Ray, single view', 125.00),
    ('TEL-200', 'Telehealth consultation', 75.00),
    ('GEN-001', 'General consultation', 55.00)
ON CONFLICT (procedure_code) DO NOTHING;
