package com.clinictraining.appointment.repository;

import com.clinictraining.appointment.model.Appointment;
import com.clinictraining.appointment.model.AppointmentStatus;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByAppointmentUid(UUID appointmentUid);

    Optional<Appointment> findByAppointmentCode(String appointmentCode);

    @Query("select a from Appointment a where a.provider.id = :providerId and ((a.slot.start < :end) and (a.slot.end > :start))")
    List<Appointment> findOverlappingAppointments(@Param("providerId") Long providerId,
                                                   @Param("start") ZonedDateTime start,
                                                   @Param("end") ZonedDateTime end);

    List<Appointment> findByStatus(AppointmentStatus status);

    Appointment findFirstByPatient_PatientCodeOrderBySlotStartDesc(String patientCode);

    @Query("select a from Appointment a where a.status = :status and a.slot.start between :start and :end")
    List<Appointment> findScheduledBetween(@Param("status") AppointmentStatus status,
                                           @Param("start") ZonedDateTime start,
                                           @Param("end") ZonedDateTime end);

    @Query("select a from Appointment a left join fetch a.patient left join fetch a.provider left join fetch a.room order by a.slot.start desc")
    Page<Appointment> findAllWithDetails(Pageable pageable);

    @Query("select a from Appointment a left join fetch a.patient left join fetch a.provider left join fetch a.room where a.status = :status order by a.slot.start desc")
    Page<Appointment> findByStatusWithDetails(@Param("status") AppointmentStatus status, Pageable pageable);
}
