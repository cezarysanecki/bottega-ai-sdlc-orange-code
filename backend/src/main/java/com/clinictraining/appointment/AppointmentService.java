package com.clinictraining.appointment;

import com.clinictraining.appointment.model.Appointment;
import com.clinictraining.appointment.model.AppointmentSlot;
import com.clinictraining.appointment.model.AppointmentStatus;
import com.clinictraining.appointment.model.AppointmentType;
import com.clinictraining.appointment.model.Room;
import com.clinictraining.appointment.repository.AppointmentRepository;
import com.clinictraining.appointment.repository.AppointmentTypeRepository;
import com.clinictraining.appointment.repository.RoomRepository;
import com.clinictraining.patient.PatientQueryService;
import com.clinictraining.patient.repository.ProviderRepository;
import com.clinictraining.shared.ClinicClock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;
    private final RoomRepository roomRepository;
    private final PatientQueryService patientQueryService;
    private final ProviderRepository providerRepository;
    private final ClinicClock clinicClock;
    private final MeterRegistry meterRegistry;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               AppointmentTypeRepository appointmentTypeRepository,
                               RoomRepository roomRepository,
                               PatientQueryService patientQueryService,
                               ProviderRepository providerRepository,
                               ClinicClock clinicClock,
                               MeterRegistry meterRegistry) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentTypeRepository = appointmentTypeRepository;
        this.roomRepository = roomRepository;
        this.patientQueryService = patientQueryService;
        this.providerRepository = providerRepository;
        this.clinicClock = clinicClock;
        this.meterRegistry = meterRegistry;
    }

    @Transactional(readOnly = true)
    public List<String> listUpcomingAppointments() {
        Timer.Sample sample = Timer.start(meterRegistry);
        ZonedDateTime anchor = clinicClock.now();
        try {
        return appointmentRepository.findByStatus(AppointmentStatus.SCHEDULED).stream()
                .filter(appointment -> appointment.getSlot() != null && appointment.getSlot().getEnd().isAfter(anchor))
                .sorted((a, b) -> a.getSlot().getStart().compareTo(b.getSlot().getStart()))
                .limit(20)
                .map(this::formatPreview)
                .collect(Collectors.toList());
        } finally {
            sample.stop(meterRegistry.timer("appointments.service.duration"));
        }
    }

    @Transactional(readOnly = true)
    public Page<Appointment> findAppointments(AppointmentStatus status, Pageable pageable) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            if (status != null) {
                return appointmentRepository.findByStatusWithDetails(status, pageable);
            } else {
                return appointmentRepository.findAllWithDetails(pageable);
            }
        } finally {
            sample.stop(meterRegistry.timer("appointments.service.duration"));
        }
    }

    @Transactional(readOnly = true)
    public Appointment findByAppointmentCode(String appointmentCode) {
        return appointmentRepository.findByAppointmentCode(appointmentCode)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentCode));
    }

    @Transactional(readOnly = true)
    public boolean isSlotAvailable(String providerCode, ZonedDateTime start, ZonedDateTime end) {
        var provider = providerRepository.findByProviderCode(providerCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown provider " + providerCode));

        ZoneId zone = clinicClock.now().getZone();

        ZonedDateTime normalizedStart = start.toLocalDateTime().atZone(zone);
        ZonedDateTime normalizedEnd = end.toLocalDateTime().atZone(zone);

        var conflicts = appointmentRepository.findOverlappingAppointments(provider.getId(), normalizedStart, normalizedEnd);
        return conflicts.isEmpty();
    }

    @Transactional
    public Appointment bookAppointment(String patientCode,
                                       String providerCode,
                                       String appointmentTypeCode,
                                       ZonedDateTime start,
                                       ZonedDateTime end,
                                       String roomCode) {

        var patient = patientQueryService.findEntity(patientCode);
        var provider = providerRepository.findByProviderCode(providerCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown provider " + providerCode));
        AppointmentType appointmentType = appointmentTypeRepository.findByTypeCode(appointmentTypeCode)
                .orElseGet(() -> appointmentTypeRepository.findByTypeCode("CHECKUP")
                        .orElseThrow(() -> new IllegalStateException("Appointment type reference data missing")));

        Room room = roomCode == null ? null : roomRepository.findByRoomCode(roomCode)
                .orElse(null); // optional room; telehealth visits may not map cleanly

        ensureNoConflicts(provider, start, end);

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setProvider(provider);
            appointment.setAppointmentType(appointmentType);
            appointment.setRoom(room);
            appointment.setSlot(new AppointmentSlot(start, end));
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            appointment.setLegacyNotes("Created via API " + UUID.randomUUID());

            Appointment persisted = appointmentRepository.save(appointment);
            persisted.getPatient().getFirstName();
            persisted.getProvider().getFirstName();
            return persisted;
        } finally {
            sample.stop(meterRegistry.timer("appointments.service.duration"));
        }
    }

    private void ensureNoConflicts(com.clinictraining.patient.model.Provider provider, ZonedDateTime start, ZonedDateTime end) {
        ZoneId zone = clinicClock.now().getZone();

        ZonedDateTime normalizedStart = start.toLocalDateTime().atZone(zone);
        ZonedDateTime normalizedEnd = end.toLocalDateTime().atZone(zone);

        var conflicts = appointmentRepository.findOverlappingAppointments(provider.getId(), normalizedStart, normalizedEnd);
        if (!conflicts.isEmpty()) {
            log.warn("Detected {} conflicting appointments for provider {}", conflicts.size(), provider.getId());
            throw new IllegalStateException("Conflicting appointment detected");
        }
    }

    private String formatPreview(Appointment appointment) {
        String patientName = safeName(appointment.getPatient().getFirstName(), appointment.getPatient().getLastName());
        String providerName = appointment.getProvider().getLastName();
        String slot = appointment.getSlot() == null ? "unscheduled" : appointment.getSlot().getStart().toString();
        return patientName + " with Dr. " + providerName + " on " + slot;
    }

    private String safeName(String first, String last) {
        return (first == null ? "Unknown" : first) + " " + (last == null ? "" : last);
    }

    @Transactional
    public Appointment cancelAppointment(Long appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));

        appointment.setStatus(AppointmentStatus.CANCELLED);

        if (reason != null) {
            String existingNotes = appointment.getLegacyNotes();
            String updatedNotes = (existingNotes != null ? existingNotes + "\n" : "")
                    + "Cancelled: " + reason + " at " + clinicClock.now();
            appointment.setLegacyNotes(updatedNotes);
        }

        appointment.setUpdatedAt(clinicClock.now());
        Appointment saved = appointmentRepository.save(appointment);

        log.info("Appointment {} cancelled", appointmentId);
        meterRegistry.counter("appointments.cancelled.count").increment();

        return saved;
    }

    @Transactional
    public Appointment updateStatus(Long appointmentId, AppointmentStatus newStatus, String notes) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));

        AppointmentStatus oldStatus = appointment.getStatus();

        appointment.setStatus(newStatus);

        if (notes != null) {
            String existingNotes = appointment.getLegacyNotes();
            String updatedNotes = (existingNotes != null ? existingNotes + "\n" : "")
                    + "Status changed from " + oldStatus + " to " + newStatus + ": " + notes;
            appointment.setLegacyNotes(updatedNotes);
        }

        appointment.setUpdatedAt(clinicClock.now());

        if (newStatus == AppointmentStatus.COMPLETED && oldStatus != AppointmentStatus.COMPLETED) {
            log.info("Appointment {} completed", appointmentId);
        }

        Appointment saved = appointmentRepository.save(appointment);
        meterRegistry.counter("appointments.status_changed.count").increment();

        return saved;
    }

    @Transactional
    public Appointment rescheduleAppointment(Long appointmentId, ZonedDateTime newStart,
                                             ZonedDateTime newEnd, String roomCode) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));

        var provider = appointment.getProvider();

        if (!isSlotAvailable(provider.getProviderCode(), newStart, newEnd)) {
            throw new IllegalStateException("Provider not available for new time slot");
        }

        ZonedDateTime oldStart = appointment.getSlot().getStart();
        appointment.setSlot(new AppointmentSlot(newStart, newEnd));

        if (roomCode != null) {
            Room room = roomRepository.findByRoomCode(roomCode)
                    .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomCode));
            appointment.setRoom(room);
        }

        String note = "Rescheduled from " + oldStart + " to " + newStart + " at " + clinicClock.now();
        String existingNotes = appointment.getLegacyNotes();
        appointment.setLegacyNotes((existingNotes != null ? existingNotes + "\n" : "") + note);

        appointment.setUpdatedAt(clinicClock.now());

        Appointment saved = appointmentRepository.save(appointment);
        meterRegistry.counter("appointments.rescheduled.count").increment();

        return saved;
    }
}
