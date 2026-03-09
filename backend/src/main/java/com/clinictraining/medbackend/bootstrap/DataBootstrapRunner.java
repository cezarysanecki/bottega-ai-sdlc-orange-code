package com.clinictraining.medbackend.bootstrap;

import com.clinictraining.appointment.model.Appointment;
import com.clinictraining.appointment.repository.AppointmentRepository;
import com.clinictraining.billing.InvoiceService;
import com.clinictraining.billing.model.Encounter;
import com.clinictraining.billing.model.EncounterProcedure;
import com.clinictraining.billing.model.EncounterProcedureId;
import com.clinictraining.billing.model.Procedure;
import com.clinictraining.billing.repository.EncounterRepository;
import com.clinictraining.billing.repository.ProcedureRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class DataBootstrapRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataBootstrapRunner.class);

    private final ObjectMapper objectMapper;
    private final AppointmentRepository appointmentRepository;
    private final EncounterRepository encounterRepository;
    private final ProcedureRepository procedureRepository;
    private final InvoiceService invoiceService;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public DataBootstrapRunner(ObjectMapper objectMapper,
                               AppointmentRepository appointmentRepository,
                               EncounterRepository encounterRepository,
                               ProcedureRepository procedureRepository,
                               InvoiceService invoiceService,
                               NamedParameterJdbcTemplate jdbcTemplate,
                               TransactionTemplate transactionTemplate) {
        this.objectMapper = objectMapper;
        this.appointmentRepository = appointmentRepository;
        this.encounterRepository = encounterRepository;
        this.procedureRepository = procedureRepository;
        this.invoiceService = invoiceService;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void run(String... args) {
        seedSqlFile("seed/001_base_data.sql", "Base data (patients, providers, rooms)");
        seedSqlFile("seed/002_appointments.sql", "Appointments and related data");
        seedEncountersFromCsv();
        seedTelehealthFromJson();
    }

    private void seedSqlFile(String resourcePath, String description) {
        try {
            org.springframework.core.io.Resource resource = new ClassPathResource(resourcePath);
            java.sql.Connection conn = jdbcTemplate.getJdbcTemplate().getDataSource().getConnection();
            org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(conn, resource);
            log.info("{} seeded successfully from {}", description, resourcePath);
        } catch (Exception ex) {
            log.error("Failed to seed {}: {}", description, ex.getMessage(), ex);
        }
    }

    private void seedEncountersFromCsv() {
        try (InputStream stream = new ClassPathResource("seed/encounters.csv").getInputStream()) {
            List<String> lines = new java.io.BufferedReader(new java.io.InputStreamReader(stream))
                    .lines()
                    .toList();
            lines.stream().skip(1).forEach(this::processEncounterRowSafe);
            log.info("Encounter seeding completed");
        } catch (IOException ex) {
            log.info("Unable to seed encounters: {}", ex.getMessage());
        }
    }

    private void processEncounterRowSafe(String row) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                processEncounterRow(row);
            });
        } catch (Exception ex) {
            log.warn("Failed to process encounter row (continuing): {}", ex.getMessage());
        }
    }

    private void processEncounterRow(String row) {
        String[] tokens = row.split(",");
        if (tokens.length < 6) {
            log.warn("Skipping malformed encounter row: {}", row);
            return;
        }
        String encounterCode = tokens[0].trim();
        String patientCode = tokens[1].trim();
        String providerCode = tokens[2].trim();
        String procedureCode = tokens[3].trim();
        int quantity = Integer.parseInt(tokens[4].trim());
        String summary = tokens[5].trim();

        Optional<Encounter> existing = encounterRepository.findByEncounterCode(encounterCode);
        if (existing.isPresent()) {
            log.debug("Encounter {} already present", encounterCode);
            return;
        }

        Appointment appointment = appointmentRepository.findFirstByPatient_PatientCodeOrderBySlotStartDesc(patientCode);
        if (appointment == null) {
            log.warn("No appointment found for patient {} to attach encounter {}", patientCode, encounterCode);
            return;
        }

        Procedure procedure = procedureRepository.findByProcedureCode(procedureCode)
                .orElseGet(() -> {
                    Procedure fallback = new Procedure();
                    fallback.setProcedureCode(procedureCode);
                    fallback.setDescription("Imported procedure");
                    fallback.setDefaultCost(new BigDecimal("42.00"));
                    return procedureRepository.save(fallback);
                });

        Encounter encounter = new Encounter();
        encounter.setEncounterCode(encounterCode);
        encounter.setAppointment(appointment);
        encounter.setEncounterTime(appointment.getSlot().getEnd());
        encounter.setReason("Seeded data for " + providerCode);
        encounter.setSummary(summary);

        EncounterProcedure encounterProcedure = new EncounterProcedure();
        encounterProcedure.setId(new EncounterProcedureId());
        encounterProcedure.setProcedure(procedure);
        encounterProcedure.setQuantity(quantity);
        encounter.addProcedure(encounterProcedure);

        encounterRepository.save(encounter);

        // Invoice generation is optional - don't fail if it doesn't work
        try {
            invoiceService.generateInvoice(encounterCode);
        } catch (Exception ex) {
            log.debug("Invoice generation skipped for {} during bootstrap: {}", encounterCode, ex.getMessage());
        }
    }

    private void seedTelehealthFromJson() {
        try (InputStream stream = new ClassPathResource("seed/telehealth-sessions.json").getInputStream()) {
            List<Map<String, Object>> payload = objectMapper.readValue(stream, new TypeReference<>() {});
            payload.forEach(this::upsertTelehealthRow);
            log.info("Telehealth seeding completed");
        } catch (IOException ex) {
            log.info("Telehealth seed skipped: {}", ex.getMessage());
        }
    }

    private void upsertTelehealthRow(Map<String, Object> row) {
        String vendorSessionId = (String) row.get("vendorSessionId");
        Number offset = (Number) row.getOrDefault("startOffsetMinutes", 0);
        String status = (String) row.getOrDefault("status", "READY");
        String failure = (String) row.getOrDefault("failureReason", null);

        ZonedDateTime startTime = ZonedDateTime.now().plusMinutes(offset.longValue());
        Timestamp startTimestamp = Timestamp.from(startTime.toInstant());

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("vendorSessionId", vendorSessionId)
                .addValue("sessionStatus", status)
                .addValue("failureReason", failure)
                .addValue("startedAt", startTimestamp);

        try {
            jdbcTemplate.update(
                    "INSERT INTO telehealth_sessions(vendor_session_id, sessionStatus, started_at, failure_reason) " +
                            "VALUES(:vendorSessionId, :sessionStatus, :startedAt, :failureReason) " +
                            "ON CONFLICT (vendor_session_id) DO UPDATE SET sessionStatus = EXCLUDED.sessionStatus, " +
                            "failure_reason = EXCLUDED.failure_reason",
                    params);
        } catch (Exception ex) {
            log.info("Telemetry seed failed for session {}", vendorSessionId, ex);
        }
    }
}
