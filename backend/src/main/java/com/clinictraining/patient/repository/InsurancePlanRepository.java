package com.clinictraining.patient.repository;

import com.clinictraining.patient.model.InsurancePlan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, Long> {

    Optional<InsurancePlan> findByPlanCode(String planCode);
}
