package com.clinictraining.patient;

import com.clinictraining.patient.model.InsurancePlan;
import com.clinictraining.patient.model.Patient;
import com.clinictraining.patient.repository.InsurancePlanRepository;
import com.clinictraining.patient.repository.PatientRepository;
import com.clinictraining.shared.ClinicClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class PatientDirectory {

    private static final Logger log = LoggerFactory.getLogger(PatientDirectory.class);

    @Autowired
    private ClinicClock clinicClock;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private InsurancePlanRepository insurancePlanRepository;

    @Transactional
    public Patient registerPatient(String patientCode, String firstName, String lastName,
                                    LocalDate dateOfBirth, String gender, String insurancePlanCode,
                                    String email, String primaryPhone) {

        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth is required");
        }

        Patient patient = new Patient();
        patient.setPatientCode(patientCode);
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setDateOfBirth(dateOfBirth);
        patient.setGender(gender);
        patient.setEmail(email);
        patient.setPrimaryPhone(primaryPhone);

        if (insurancePlanCode != null) {
            InsurancePlan plan = insurancePlanRepository.findByPlanCode(insurancePlanCode)
                    .orElseThrow(() -> new IllegalArgumentException("Insurance plan not found: " + insurancePlanCode));
            patient.setInsurancePlan(plan);
        }

        Patient saved = patientRepository.save(patient);

        log.info("Registered new patient: {}", patientCode);

        return saved;
    }

    @Transactional
    public Patient updatePatient(String patientCode, String email, String primaryPhone,
                                  String addressLine1, String addressLine2, String city,
                                  String state, String postalCode, String insurancePlanCode) {

        Patient patient = patientRepository.findByPatientCode(patientCode)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientCode));

        if (email != null) patient.setEmail(email);
        if (primaryPhone != null) patient.setPrimaryPhone(primaryPhone);

        if (addressLine1 != null) patient.getAddress().setLine1(addressLine1);
        if (addressLine2 != null) patient.getAddress().setLine2(addressLine2);
        if (city != null) patient.getAddress().setCity(city);
        if (state != null) patient.getAddress().setState(state);
        if (postalCode != null) patient.getAddress().setPostalCode(postalCode);

        if (insurancePlanCode != null) {
            InsurancePlan plan = insurancePlanRepository.findByPlanCode(insurancePlanCode)
                    .orElseThrow(() -> new IllegalArgumentException("Insurance plan not found: " + insurancePlanCode));
            patient.setInsurancePlan(plan);
        }

        return patientRepository.save(patient);
    }

    @Transactional
    public Patient updateAllergies(String patientCode, String allergies) {
        Patient patient = patientRepository.findByPatientCode(patientCode)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientCode));

        patient.setAllergies(allergies);

        log.warn("Updated allergies for patient {} - should trigger clinical review", patientCode);

        return patientRepository.save(patient);
    }
}
