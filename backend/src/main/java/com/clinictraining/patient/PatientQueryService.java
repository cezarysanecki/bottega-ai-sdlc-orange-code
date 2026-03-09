package com.clinictraining.patient;

import com.clinictraining.patient.dto.PatientSummaryDto;
import com.clinictraining.patient.mapper.PatientMapper;
import com.clinictraining.patient.model.Patient;
import com.clinictraining.patient.repository.PatientRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PatientQueryService {

    private static final Logger log = LoggerFactory.getLogger(PatientQueryService.class);

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public PatientQueryService(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    public List<PatientSummaryDto> listPatients() {
        log.debug("Listing patients via repository");
        return patientRepository.findAll().stream()
                .map(patientMapper::toSummary)
                .toList();
    }

    @Cacheable(cacheNames = "patientSchedule", key = "#patientCode")
    public PatientSummaryDto findPatient(String patientCode) {
        return patientRepository.findByPatientCode(patientCode)
                .map(patientMapper::toSummary)
                .orElseThrow(() -> new IllegalArgumentException("Unknown patient code " + patientCode));
    }

    public Patient findEntity(String patientCode) {
        return patientRepository.findByPatientCode(patientCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown patient code " + patientCode));
    }
}
