package com.clinictraining.patient.repository;

import com.clinictraining.patient.model.Provider;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderRepository extends JpaRepository<Provider, Long> {

    Optional<Provider> findByProviderCode(String providerCode);

    List<Provider> findBySpecialtyIgnoreCase(String specialty);
}
