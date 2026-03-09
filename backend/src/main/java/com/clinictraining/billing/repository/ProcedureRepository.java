package com.clinictraining.billing.repository;

import com.clinictraining.billing.model.Procedure;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcedureRepository extends JpaRepository<Procedure, Long> {
    Optional<Procedure> findByProcedureCode(String procedureCode);
}
