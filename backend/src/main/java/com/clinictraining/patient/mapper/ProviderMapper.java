package com.clinictraining.patient.mapper;

import com.clinictraining.patient.dto.ProviderSummaryDto;
import com.clinictraining.patient.model.Provider;
import org.springframework.stereotype.Component;

@Component
public class ProviderMapper {

    public ProviderSummaryDto toSummary(Provider provider) {
        ProviderSummaryDto dto = new ProviderSummaryDto();
        dto.setProviderCode(provider.getProviderCode());
        dto.setFullName(provider.getFirstName() + " " + provider.getLastName());
        dto.setSpecialty(provider.getSpecialty());
        dto.setOfficeRoom(provider.getOfficeRoom());
        dto.setPreferredContact(provider.getPreferredContact());
        dto.setNpi(provider.getNpi());
        return dto;
    }
}
