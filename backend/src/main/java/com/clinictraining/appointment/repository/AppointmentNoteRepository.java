package com.clinictraining.appointment.repository;

import com.clinictraining.appointment.model.AppointmentNote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentNoteRepository extends JpaRepository<AppointmentNote, Long> {
    List<AppointmentNote> findByAppointment_Id(Long appointmentId);
}
