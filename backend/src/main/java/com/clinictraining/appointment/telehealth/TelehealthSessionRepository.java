package com.clinictraining.appointment.telehealth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelehealthSessionRepository extends JpaRepository<TelehealthSession, Long> {
    Optional<TelehealthSession> findByVendorSessionId(String vendorSessionId);
}
