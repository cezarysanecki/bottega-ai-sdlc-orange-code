package com.clinictraining.billing;

import com.clinictraining.appointment.model.Appointment;
import com.clinictraining.appointment.repository.AppointmentRepository;
import com.clinictraining.billing.model.Encounter;
import com.clinictraining.billing.model.EncounterProcedure;
import com.clinictraining.billing.model.EncounterProcedureId;
import com.clinictraining.billing.model.Procedure;
import com.clinictraining.billing.repository.EncounterRepository;
import com.clinictraining.billing.repository.ProcedureRepository;
import com.clinictraining.shared.ClinicClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EncounterService {

    private static final Logger log = LoggerFactory.getLogger(EncounterService.class);

    private final EncounterRepository encounterRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProcedureRepository procedureRepository;
    private final ClinicClock clinicClock;

    public EncounterService(EncounterRepository encounterRepository,
                            AppointmentRepository appointmentRepository,
                            ProcedureRepository procedureRepository,
                            ClinicClock clinicClock) {
        this.encounterRepository = encounterRepository;
        this.appointmentRepository = appointmentRepository;
        this.procedureRepository = procedureRepository;
        this.clinicClock = clinicClock;
    }

    @Transactional
    public Encounter createEncounterFromAppointment(Long appointmentId, String reason,
                                                     List<String> procedureCodes, String summary) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));

        String encounterCode = generateEncounterCode();

        Encounter encounter = new Encounter();
        encounter.setEncounterCode(encounterCode);
        encounter.setAppointment(appointment);
        encounter.setEncounterTime(clinicClock.now());
        encounter.setReason(reason != null ? reason : "Follow-up visit");
        encounter.setSummary(summary);

        Encounter saved = encounterRepository.save(encounter);

        if (procedureCodes != null && !procedureCodes.isEmpty()) {
            addProcedures(saved, procedureCodes);
        }

        log.info("Created encounter {} for appointment {}", encounterCode, appointmentId);

        return saved;
    }

    @Transactional
    public Encounter addProcedures(Encounter encounter, List<String> procedureCodes) {
        for (String code : procedureCodes) {
            Procedure procedure = procedureRepository.findByProcedureCode(code)
                    .orElseThrow(() -> new IllegalArgumentException("Procedure not found: " + code));

            EncounterProcedure ep = new EncounterProcedure();
            EncounterProcedureId epId = new EncounterProcedureId();
            epId.setEncounterId(encounter.getId());
            epId.setProcedureId(procedure.getId());
            ep.setId(epId);
            ep.setEncounter(encounter);
            ep.setProcedure(procedure);
            ep.setQuantity(1);

            encounter.addProcedure(ep);
        }

        return encounterRepository.save(encounter);
    }

    @Transactional
    public Encounter updateSummary(String encounterCode, String summary) {
        Encounter encounter = encounterRepository.findByEncounterCode(encounterCode)
                .orElseThrow(() -> new IllegalArgumentException("Encounter not found: " + encounterCode));

        encounter.setSummary(summary);

        return encounterRepository.save(encounter);
    }

    @Transactional(readOnly = true)
    public Encounter findByEncounterCode(String encounterCode) {
        return encounterRepository.findByEncounterCode(encounterCode)
                .orElseThrow(() -> new IllegalArgumentException("Encounter not found: " + encounterCode));
    }

    @Transactional(readOnly = true)
    public Page<Encounter> findAllEncounters(Pageable pageable) {
        return encounterRepository.findAll(pageable);
    }

    private String generateEncounterCode() {
        return "ENC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
