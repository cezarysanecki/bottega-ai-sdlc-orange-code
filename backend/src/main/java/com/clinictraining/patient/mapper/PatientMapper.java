package com.clinictraining.patient.mapper;

import com.clinictraining.patient.dto.PatientSummaryDto;
import com.clinictraining.patient.model.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(target = "insurancePlanCode", source = "insurancePlan.planCode")
    @Mapping(target = "insuranceProvider", source = "insurancePlan.providerName")
    PatientSummaryDto toSummary(Patient patient);
}
