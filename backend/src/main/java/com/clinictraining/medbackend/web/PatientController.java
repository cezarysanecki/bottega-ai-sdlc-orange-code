package com.clinictraining.medbackend.web;

import com.clinictraining.medbackend.insurance.InsuranceEligibilityService;
import com.clinictraining.medbackend.insurance.InsuranceEligibilityView;
import com.clinictraining.patient.PatientDirectory;
import com.clinictraining.patient.PatientQueryService;
import com.clinictraining.patient.dto.PatientSummaryDto;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientDirectory patientDirectory;
    private final PatientQueryService patientQueryService;
    private final InsuranceEligibilityService insuranceEligibilityService;

    public PatientController(PatientDirectory patientDirectory,
                             PatientQueryService patientQueryService,
                             InsuranceEligibilityService insuranceEligibilityService) {
        this.patientDirectory = patientDirectory;
        this.patientQueryService = patientQueryService;
        this.insuranceEligibilityService = insuranceEligibilityService;
    }

    @GetMapping
    public ResponseEntity<List<PatientSummaryDto>> listPatients() {
        return ResponseEntity.ok(patientQueryService.listPatients());
    }

    @PostMapping("/{patientId}/eligibility/check")
    public ResponseEntity<InsuranceEligibilityView> checkEligibility(@PathVariable String patientId,
                                                                     @RequestBody(required = false) Map<String, Object> ignoredPayload) {
        return ResponseEntity.ok(insuranceEligibilityService.checkEligibility(patientId));
    }

    @DeleteMapping("/{patientId}/eligibility/cache")
    public ResponseEntity<Void> invalidateEligibility(@PathVariable String patientId) {
        insuranceEligibilityService.invalidateCache(patientId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<PatientSummaryDto> createPatient(@RequestBody Map<String, Object> request) {
        String patientCode = (String) request.get("patientCode");
        String firstName = (String) request.get("firstName");
        String lastName = (String) request.get("lastName");
        String dateOfBirthStr = (String) request.get("dateOfBirth");
        String gender = (String) request.get("gender");
        String insurancePlanCode = (String) request.get("insurancePlanCode");
        String email = (String) request.get("email");
        String primaryPhone = (String) request.get("primaryPhone");

        java.time.LocalDate dateOfBirth = dateOfBirthStr != null
                ? java.time.LocalDate.parse(dateOfBirthStr)
                : null;

        patientDirectory.registerPatient(
                patientCode, firstName, lastName, dateOfBirth, gender,
                insurancePlanCode, email, primaryPhone
        );

        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(patientQueryService.findPatient(patientCode));
    }

    @GetMapping("/{patientCode}")
    public ResponseEntity<PatientSummaryDto> getPatientDetail(@PathVariable String patientCode) {
        return ResponseEntity.ok(patientQueryService.findPatient(patientCode));
    }

    @PutMapping("/{patientCode}")
    public ResponseEntity<PatientSummaryDto> updatePatient(
            @PathVariable String patientCode,
            @RequestBody Map<String, Object> request) {

        String email = (String) request.get("email");
        String primaryPhone = (String) request.get("primaryPhone");
        String addressLine1 = (String) request.get("addressLine1");
        String addressLine2 = (String) request.get("addressLine2");
        String city = (String) request.get("city");
        String state = (String) request.get("state");
        String postalCode = (String) request.get("postalCode");
        String insurancePlanCode = (String) request.get("insurancePlanCode");

        patientDirectory.updatePatient(
                patientCode, email, primaryPhone, addressLine1, addressLine2,
                city, state, postalCode, insurancePlanCode
        );

        return ResponseEntity.ok(patientQueryService.findPatient(patientCode));
    }

    @PatchMapping("/{patientCode}/allergies")
    public ResponseEntity<PatientSummaryDto> updateAllergies(
            @PathVariable String patientCode,
            @RequestBody Map<String, String> payload) {

        String allergies = payload.get("allergies");
        patientDirectory.updateAllergies(patientCode, allergies);

        return ResponseEntity.ok(patientQueryService.findPatient(patientCode));
    }

}
