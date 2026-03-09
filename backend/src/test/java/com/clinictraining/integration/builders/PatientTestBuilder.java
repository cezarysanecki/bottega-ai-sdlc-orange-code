package com.clinictraining.integration.builders;

import com.clinictraining.patient.model.Patient;
import java.time.LocalDate;

/**
 * Test data builder for Patient entities used in integration tests.
 *
 * <p>Provides a fluent API for creating Patient instances with sensible defaults.
 * Uses unique patient codes based on nanoTime to avoid conflicts between tests.
 *
 * <p>Example usage:
 * <pre>
 * Patient patient = aPatient()
 *     .withName("John", "Doe")
 *     .withEmail("john.doe@example.com")
 *     .build();
 * </pre>
 */
public class PatientTestBuilder {

    private String patientCode = "PAT-TEST-" + System.nanoTime();
    private String firstName = "John";
    private String lastName = "Doe";
    private LocalDate dateOfBirth = LocalDate.of(1980, 1, 1);
    private String gender = "M";
    private String primaryPhone = "555-0100";
    private String email = "test@example.com";
    private String zoneId = "America/New_York";

    /**
     * Creates a new builder with default values.
     */
    public static PatientTestBuilder aPatient() {
        return new PatientTestBuilder();
    }

    /**
     * Sets the patient code.
     */
    public PatientTestBuilder withCode(String patientCode) {
        this.patientCode = patientCode;
        return this;
    }

    /**
     * Sets the patient's first and last name.
     */
    public PatientTestBuilder withName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        return this;
    }

    /**
     * Sets the patient's date of birth.
     */
    public PatientTestBuilder bornOn(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    /**
     * Sets the patient's gender.
     */
    public PatientTestBuilder withGender(String gender) {
        this.gender = gender;
        return this;
    }

    /**
     * Sets the patient's phone number.
     */
    public PatientTestBuilder withPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
        return this;
    }

    /**
     * Sets the patient's email address.
     */
    public PatientTestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Sets the patient's timezone.
     */
    public PatientTestBuilder inZone(String zoneId) {
        this.zoneId = zoneId;
        return this;
    }

    /**
     * Builds the Patient with the configured values.
     * Note: This creates the entity but does not persist it. Call repository.save() separately.
     */
    public Patient build() {
        Patient patient = new Patient();
        patient.setPatientCode(patientCode);
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setDateOfBirth(dateOfBirth);
        patient.setGender(gender);
        patient.setPrimaryPhone(primaryPhone);
        patient.setEmail(email);
        patient.setZoneId(zoneId);
        return patient;
    }
}
