package com.clinictraining.billing;

import com.clinictraining.appointment.model.Appointment;
import com.clinictraining.billing.model.Encounter;
import com.clinictraining.billing.model.EncounterProcedure;
import com.clinictraining.billing.model.Invoice;
import com.clinictraining.billing.model.InvoiceItem;
import com.clinictraining.billing.repository.EncounterRepository;
import com.clinictraining.billing.repository.InvoiceRepository;
import com.clinictraining.legacy.BillingRulesEngine;
import com.clinictraining.legacy.BillingRulesEngine.PricingContext;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final EncounterRepository encounterRepository;
    private final InvoiceRepository invoiceRepository;
    private final BillingRulesEngine billingRulesEngine;
    private final MeterRegistry meterRegistry;

    public InvoiceService(EncounterRepository encounterRepository,
                          InvoiceRepository invoiceRepository,
                          BillingRulesEngine billingRulesEngine,
                          MeterRegistry meterRegistry) {
        this.encounterRepository = encounterRepository;
        this.invoiceRepository = invoiceRepository;
        this.billingRulesEngine = billingRulesEngine;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public Invoice generateInvoice(String encounterCode) {
        Timer.Sample sample = Timer.start(meterRegistry);
        Encounter encounter = encounterRepository.findByEncounterCode(encounterCode)
                .orElseThrow(() -> new IllegalArgumentException("Encounter not found: " + encounterCode));

        Appointment appointment = encounter.getAppointment();
        if (appointment == null) {
            throw new IllegalStateException("Encounter missing appointment link");
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invoice.setEncounter(encounter);
        invoice.setPatient(appointment.getPatient());
        invoice.setStatus("OPEN");
        invoice.setIssuedAt(ZonedDateTime.now());
        invoice.setDueDate(ZonedDateTime.now().plusDays(30));
        invoice.setNotes("Generated via InvoiceService");

        PricingContext context = buildPricingContext(encounter, appointment);

        BigDecimal total = BigDecimal.ZERO;
        for (EncounterProcedure encounterProcedure : encounter.getProcedures()) {
            InvoiceItem item = new InvoiceItem();
            item.setLineItemCode(encounterProcedure.getProcedure().getProcedureCode());
            item.setDescription(encounterProcedure.getProcedure().getDescription());
            item.setQuantity(encounterProcedure.getQuantity());
            BigDecimal unit = encounterProcedure.getOverrideCost();
            if (unit == null) {
                unit = billingRulesEngine.calculateCharge(
                        encounterProcedure.getProcedure().getProcedureCode(),
                        context
                );
            }
            item.setUnitAmount(unit);
            item.setLegacyTaxCents(183);
            invoice.addItem(item);
            total = total.add(unit.multiply(BigDecimal.valueOf(encounterProcedure.getQuantity())));
        }

        invoice.setTotalAmount(total);
        invoice.setBalanceCents(invoice.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());

        try {
            return invoiceRepository.save(invoice);
        } finally {
            sample.stop(meterRegistry.timer("billing_service_duration"));
        }
    }

    private PricingContext buildPricingContext(Encounter encounter, Appointment appointment) {
        PricingContext context = new PricingContext();

        context.setServiceDateTime(encounter.getEncounterTime());

        if (appointment.getPatient() != null) {
            context.setPatientAge(calculateAge(appointment.getPatient().getDateOfBirth()));
        }

        if (appointment.getPatient() != null && appointment.getPatient().getAddress() != null) {
            context.setState(appointment.getPatient().getAddress().getState());
        }

        context.setSelfPay(appointment.getPatient() != null &&
                          appointment.getPatient().getInsurancePlan() == null);

        context.setContractedInsurance(appointment.getPatient() != null &&
                                       appointment.getPatient().getInsurancePlan() != null);

        return context;
    }

    private Integer calculateAge(java.time.LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        }
        return java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
    }
}
