package com.clinictraining.billing.repository;

import com.clinictraining.billing.model.Encounter;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncounterRepository extends JpaRepository<Encounter, Long> {
    Optional<Encounter> findByEncounterCode(String encounterCode);
}
