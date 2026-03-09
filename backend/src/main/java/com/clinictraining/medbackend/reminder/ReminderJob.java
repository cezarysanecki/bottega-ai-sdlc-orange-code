package com.clinictraining.medbackend.reminder;

import com.clinictraining.appointment.model.Appointment;
import com.clinictraining.appointment.model.AppointmentStatus;
import com.clinictraining.appointment.reminder.ReminderLog;
import com.clinictraining.appointment.reminder.ReminderLogRepository;
import com.clinictraining.appointment.repository.AppointmentRepository;
import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderJob {

    private static final Logger log = LoggerFactory.getLogger(ReminderJob.class);

    private final AppointmentRepository appointmentRepository;
    private final ReminderLogRepository reminderLogRepository;
    private final ReminderQueue reminderQueue;

    public ReminderJob(AppointmentRepository appointmentRepository,
                       ReminderLogRepository reminderLogRepository,
                       ReminderQueue reminderQueue) {
        this.appointmentRepository = appointmentRepository;
        this.reminderLogRepository = reminderLogRepository;
        this.reminderQueue = reminderQueue;
    }

    @Scheduled(cron = "${reminder.job.cron:0 0 21 * * *}")
    @Transactional
    public void dispatchReminders() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime windowEnd = now.plusDays(1);

        List<Appointment> upcoming = appointmentRepository.findScheduledBetween(AppointmentStatus.SCHEDULED, now, windowEnd);
        for (Appointment appointment : upcoming) {
            try {
                ReminderMessage message = buildMessage(appointment);
                reminderQueue.enqueue(message);
                reminderLogRepository.save(buildLog(appointment, message));
            } catch (Exception ex) {
                log.warn("Failed to enqueue reminder for appointment {}", appointment.getId(), ex);
            }
        }
        log.info("Reminder job queued {} reminders", upcoming.size());
    }

    private ReminderMessage buildMessage(Appointment appointment) {
        String patientCode = appointment.getPatient() != null ? appointment.getPatient().getPatientCode() : "UNKNOWN";
        String providerCode = appointment.getProvider() != null ? appointment.getProvider().getProviderCode() : "UNKNOWN";
        ZonedDateTime scheduledFor = appointment.getSlot() != null ? appointment.getSlot().getStart() : ZonedDateTime.now();
        String message = "Reminder: appointment for " + patientCode + " with provider " + providerCode + " at " + scheduledFor;
        return new ReminderMessage(appointment.getId(), patientCode, providerCode, scheduledFor, message);
    }

    private ReminderLog buildLog(Appointment appointment, ReminderMessage message) {
        ReminderLog logEntry = new ReminderLog();
        logEntry.setAppointment(appointment);
        logEntry.setPatientCode(message.getPatientCode());
        logEntry.setProviderCode(message.getProviderCode());
        logEntry.setScheduledFor(message.getScheduledFor());
        logEntry.setSentAt(ZonedDateTime.now());
        logEntry.setMessage(message.getMessage());
        return logEntry;
    }
}
