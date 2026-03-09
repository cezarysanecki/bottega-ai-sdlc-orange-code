package com.clinictraining.billing.repository;

import com.clinictraining.billing.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
