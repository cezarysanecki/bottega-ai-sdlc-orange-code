package com.clinictraining.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.clinictraining.appointment.model.Appointment;
import com.clinictraining.billing.model.Encounter;
import com.clinictraining.billing.model.EncounterProcedure;
import com.clinictraining.billing.model.EncounterProcedureId;
import com.clinictraining.billing.model.Invoice;
import com.clinictraining.billing.model.Procedure;
import com.clinictraining.billing.repository.EncounterRepository;
import com.clinictraining.billing.repository.InvoiceRepository;
import com.clinictraining.legacy.BillingRulesEngine;
import com.clinictraining.patient.model.Patient;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    private static final String ENCOUNTER_CODE = "ENC-12345678";
    private static final String INVOICE_NUMBER_PREFIX = "INV-";
    private static final String PROCEDURE_CODE_OFFICE_VISIT = "99213";
    private static final String PROCEDURE_DESC = "Office visit - established patient";
    private static final BigDecimal PROCEDURE_CHARGE = new BigDecimal("110.00");

    @Mock
    private EncounterRepository encounterRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private BillingRulesEngine billingRulesEngine;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private MeterRegistry.Config meterRegistryConfig;

    @Mock
    private io.micrometer.core.instrument.Clock clock;

    @Mock
    private Timer timer;

    @InjectMocks
    private InvoiceService invoiceService;

    private Encounter encounter;
    private Appointment appointment;
    private Patient patient;
    private Procedure procedure;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setPatient(patient);

        encounter = new Encounter();
        encounter.setId(1L);
        encounter.setEncounterCode(ENCOUNTER_CODE);
        encounter.setAppointment(appointment);
        encounter.setEncounterTime(ZonedDateTime.now());
        encounter.setProcedures(new ArrayList<>());

        procedure = new Procedure();
        procedure.setId(1L);
        procedure.setProcedureCode(PROCEDURE_CODE_OFFICE_VISIT);
        procedure.setDescription(PROCEDURE_DESC);
        procedure.setDefaultCost(PROCEDURE_CHARGE);

        when(meterRegistry.config()).thenReturn(meterRegistryConfig);
        when(meterRegistryConfig.clock()).thenReturn(clock);
        when(clock.monotonicTime()).thenReturn(0L, 1000000000L);
        when(meterRegistry.timer("billing_service_duration")).thenReturn(timer);
    }

    @Test
    void shouldGenerateInvoiceForEncounterWithSingleProcedure() {
        EncounterProcedure encounterProcedure = createEncounterProcedure(encounter, procedure, 1);
        encounter.getProcedures().add(encounterProcedure);

        when(encounterRepository.findByEncounterCode(ENCOUNTER_CODE))
                .thenReturn(Optional.of(encounter));
        when(billingRulesEngine.calculateCharge(eq(PROCEDURE_CODE_OFFICE_VISIT), any()))
                .thenReturn(PROCEDURE_CHARGE);

        Invoice savedInvoice = new Invoice();
        savedInvoice.setInvoiceNumber(INVOICE_NUMBER_PREFIX + "TEST");
        savedInvoice.setPatient(patient);
        savedInvoice.setEncounter(encounter);
        savedInvoice.setTotalAmount(PROCEDURE_CHARGE);
        savedInvoice.setBalanceCents(11000);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        Invoice result = invoiceService.generateInvoice(ENCOUNTER_CODE);

        assertNotNull(result);
        assertEquals(patient, result.getPatient());
        assertEquals(encounter, result.getEncounter());
        assertEquals(PROCEDURE_CHARGE, result.getTotalAmount());
        assertEquals(11000, result.getBalanceCents());
    }

    @Test
    void shouldGenerateInvoiceForEncounterWithMultipleProcedures() {
        EncounterProcedure encounterProcedure1 = createEncounterProcedure(encounter, procedure, 1);
        encounter.getProcedures().add(encounterProcedure1);

        Procedure labProcedure = new Procedure();
        labProcedure.setId(2L);
        labProcedure.setProcedureCode("LAB-CBC");
        labProcedure.setDescription("Complete blood count");
        labProcedure.setDefaultCost(new BigDecimal("35.00"));

        EncounterProcedure encounterProcedure2 = createEncounterProcedure(encounter, labProcedure, 1);
        encounter.getProcedures().add(encounterProcedure2);

        BigDecimal expectedTotal = new BigDecimal("145.00");

        when(encounterRepository.findByEncounterCode(ENCOUNTER_CODE))
                .thenReturn(Optional.of(encounter));
        when(billingRulesEngine.calculateCharge(eq(PROCEDURE_CODE_OFFICE_VISIT), any()))
                .thenReturn(PROCEDURE_CHARGE);
        when(billingRulesEngine.calculateCharge(eq("LAB-CBC"), any()))
                .thenReturn(new BigDecimal("35.00"));

        Invoice savedInvoice = new Invoice();
        savedInvoice.setInvoiceNumber(INVOICE_NUMBER_PREFIX + "MULTI");
        savedInvoice.setPatient(patient);
        savedInvoice.setEncounter(encounter);
        savedInvoice.setTotalAmount(expectedTotal);
        savedInvoice.setBalanceCents(14500);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        Invoice result = invoiceService.generateInvoice(ENCOUNTER_CODE);

        assertNotNull(result);
        assertEquals(expectedTotal, result.getTotalAmount());
        assertEquals(14500, result.getBalanceCents());
    }

    private EncounterProcedure createEncounterProcedure(Encounter enc, Procedure proc, int quantity) {
        EncounterProcedure ep = new EncounterProcedure();
        EncounterProcedureId id = new EncounterProcedureId();
        id.setEncounterId(enc.getId());
        id.setProcedureId(proc.getId());
        ep.setId(id);
        ep.setEncounter(enc);
        ep.setProcedure(proc);
        ep.setQuantity(quantity);
        return ep;
    }
}
