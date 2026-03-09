package com.clinictraining.patient;

import static org.mockito.Mockito.when;

import com.clinictraining.patient.dto.PatientSummaryDto;
import com.clinictraining.patient.mapper.PatientMapper;
import com.clinictraining.patient.model.InsurancePlan;
import com.clinictraining.patient.model.Patient;
import com.clinictraining.patient.repository.PatientRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientQueryServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientQueryService patientQueryService;

    @Test
    void listPatientsReturnsSummaries() {
        Patient patient = samplePatient();
        PatientSummaryDto expectedDto = createSummaryDto();

        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientMapper.toSummary(patient)).thenReturn(expectedDto);

        List<PatientSummaryDto> summaries = patientQueryService.listPatients();

        Assertions.assertEquals(1, summaries.size());
        Assertions.assertEquals("PAT-1001", summaries.get(0).getPatientCode());
    }

    @Test
    void findPatientMapsInsurance() {
        Patient patient = samplePatient();
        PatientSummaryDto expectedDto = createSummaryDto();

        when(patientRepository.findByPatientCode("PAT-1001")).thenReturn(Optional.of(patient));
        when(patientMapper.toSummary(patient)).thenReturn(expectedDto);

        PatientSummaryDto summary = patientQueryService.findPatient("PAT-1001");
        Assertions.assertEquals("PAT-1001", summary.getPatientCode());
        Assertions.assertEquals("PPO-BETA", summary.getInsurancePlanCode());
        Assertions.assertEquals("Beta Care", summary.getInsuranceProvider());
    }

    private Patient samplePatient() {
        InsurancePlan plan = new InsurancePlan();
        plan.setPlanCode("PPO-BETA");
        plan.setProviderName("Beta Care");

        Patient patient = new Patient();
        patient.setPatientCode("PAT-1001");
        patient.setFirstName("Jordan");
        patient.setLastName("Miles");
        patient.setDateOfBirth(LocalDate.of(1985, 2, 14));
        patient.setInsurancePlan(plan);
        patient.setAllergies("Peanuts");
        return patient;
    }

    private PatientSummaryDto createSummaryDto() {
        PatientSummaryDto dto = new PatientSummaryDto();
        dto.setPatientCode("PAT-1001");
        dto.setFirstName("Jordan");
        dto.setLastName("Miles");
        dto.setDateOfBirth(LocalDate.of(1985, 2, 14));
        dto.setInsurancePlanCode("PPO-BETA");
        dto.setInsuranceProvider("Beta Care");
        return dto;
    }
}
