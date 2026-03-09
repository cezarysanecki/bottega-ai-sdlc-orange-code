package com.clinictraining.medbackend.web;

import com.clinictraining.appointment.AppointmentService;
import com.clinictraining.appointment.model.Appointment;
import com.clinictraining.appointment.model.AppointmentStatus;
import com.clinictraining.medbackend.api.ApiMapper;
import com.clinictraining.medbackend.api.dto.AppointmentRequest;
import com.clinictraining.medbackend.api.dto.AppointmentResponse;
import com.clinictraining.medbackend.api.dto.PagedResponse;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentService appointmentService;
    private final ApiMapper apiMapper;

    public AppointmentController(AppointmentService appointmentService, ApiMapper apiMapper) {
        this.appointmentService = appointmentService;
        this.apiMapper = apiMapper;
    }

    @GetMapping("/preview")
    public ResponseEntity<List<String>> previewUpcoming() {
        log.info("Previewing upcoming appointments");
        return ResponseEntity.ok(appointmentService.listUpcomingAppointments());
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@RequestBody AppointmentRequest request) {
        log.info("Creating appointment for patient {}", request.getPatientCode());
        Appointment appointment = appointmentService.bookAppointment(
                request.getPatientCode(),
                request.getProviderCode(),
                request.getAppointmentType(),
                request.getStartTime(),
                request.getEndTime(),
                request.getRoomCode()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiMapper.toResponse(appointment));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<AppointmentResponse>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {

        log.info("Fetching appointments - page: {}, size: {}, status: {}", page, size, status);

        Pageable pageable = PageRequest.of(page, size);
        AppointmentStatus appointmentStatus = status != null ? AppointmentStatus.valueOf(status.toUpperCase()) : null;

        Page<Appointment> appointmentsPage = appointmentService.findAppointments(appointmentStatus, pageable);

        List<AppointmentResponse> responses = appointmentsPage.getContent().stream()
                .map(apiMapper::toResponse)
                .collect(Collectors.toList());

        PagedResponse<AppointmentResponse> pagedResponse = new PagedResponse<>(
                responses,
                appointmentsPage.getNumber(),
                appointmentsPage.getSize(),
                appointmentsPage.getTotalElements(),
                appointmentsPage.getTotalPages()
        );

        return ResponseEntity.ok(pagedResponse);
    }

    @GetMapping("/{appointmentCode}")
    public ResponseEntity<AppointmentResponse> getByCode(@PathVariable String appointmentCode) {
        log.info("Fetching appointment by code: {}", appointmentCode);
        Appointment appointment = appointmentService.findByAppointmentCode(appointmentCode);
        return ResponseEntity.ok(apiMapper.toResponse(appointment));
    }

    @GetMapping("/check-availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @RequestParam String providerCode,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        log.info("Checking availability for provider {} from {} to {}", providerCode, startTime, endTime);

        ZonedDateTime start = ZonedDateTime.parse(startTime);
        ZonedDateTime end = ZonedDateTime.parse(endTime);

        boolean available = appointmentService.isSlotAvailable(providerCode, start, end);

        return ResponseEntity.ok(Map.of(
                "available", available,
                "providerCode", providerCode,
                "startTime", startTime,
                "endTime", endTime
        ));
    }

    @PatchMapping("/{appointmentId}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(
            @PathVariable Long appointmentId,
            @RequestBody(required = false) Map<String, String> payload) {
        log.info("Cancelling appointment {}", appointmentId);

        String reason = payload != null ? payload.get("reason") : null;
        Appointment appointment = appointmentService.cancelAppointment(appointmentId, reason);

        return ResponseEntity.ok(apiMapper.toResponse(appointment));
    }

    @PatchMapping("/{appointmentId}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> payload) {
        log.info("Updating appointment {} status to {}", appointmentId, payload.get("status"));

        AppointmentStatus newStatus = AppointmentStatus.valueOf(payload.get("status").toUpperCase());
        String notes = payload.get("notes");

        Appointment appointment = appointmentService.updateStatus(appointmentId, newStatus, notes);

        return ResponseEntity.ok(apiMapper.toResponse(appointment));
    }

    @PutMapping("/{appointmentId}/reschedule")
    public ResponseEntity<AppointmentResponse> reschedule(
            @PathVariable Long appointmentId,
            @RequestBody AppointmentRequest request) {
        log.info("Rescheduling appointment {} to {}", appointmentId, request.getStartTime());

        Appointment appointment = appointmentService.rescheduleAppointment(
                appointmentId,
                request.getStartTime(),
                request.getEndTime(),
                request.getRoomCode()
        );

        return ResponseEntity.ok(apiMapper.toResponse(appointment));
    }
}
