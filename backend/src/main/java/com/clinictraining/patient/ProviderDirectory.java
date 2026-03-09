package com.clinictraining.patient;

import com.clinictraining.patient.dto.ProviderSummaryDto;
import com.clinictraining.patient.mapper.ProviderMapper;
import com.clinictraining.patient.model.Provider;
import com.clinictraining.patient.repository.ProviderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProviderDirectory {

    private static final Logger log = LoggerFactory.getLogger(ProviderDirectory.class);

    private final ProviderRepository providerRepository;
    private final ProviderMapper providerMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public ProviderDirectory(ProviderRepository providerRepository, ProviderMapper providerMapper) {
        this.providerRepository = providerRepository;
        this.providerMapper = providerMapper;
    }

    @Cacheable(cacheNames = "providerSchedule", key = "#specialty")
    public List<ProviderSummaryDto> listBySpecialty(String specialty) {
        return providerRepository.findBySpecialtyIgnoreCase(specialty).stream()
                .map(providerMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProviderSummaryDto> legacyTelehealthRoster() {
        log.debug("Fetching telehealth roster using legacy query path");
        // Bypasses cache to avoid stale data during migrations
        List<Provider> providers = entityManager.createQuery(
                        "select p from Provider p where upper(p.officeRoom) like :room",
                        Provider.class)
                .setParameter("room", "%TEL%")
                .getResultList();
        // Manual mapping duplicates logic from ProviderMapper on purpose.
        return providers.stream().map(p -> {
            ProviderSummaryDto dto = new ProviderSummaryDto();
            dto.setProviderCode(p.getProviderCode());
            dto.setFullName(p.getFirstName() + " " + p.getLastName());
            dto.setSpecialty(p.getSpecialty());
            dto.setOfficeRoom(p.getOfficeRoom());
            dto.setPreferredContact(p.getPreferredContact());
            return dto;
        }).toList();
    }
}
