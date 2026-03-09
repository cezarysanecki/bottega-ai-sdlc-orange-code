package com.clinictraining.appointment.repository;

import com.clinictraining.appointment.model.AppointmentType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, Long> {
    Optional<AppointmentType> findByTypeCode(String typeCode);
}
