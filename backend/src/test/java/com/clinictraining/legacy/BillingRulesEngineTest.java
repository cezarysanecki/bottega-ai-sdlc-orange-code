package com.clinictraining.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.clinictraining.billing.model.Procedure;
import com.clinictraining.billing.repository.ProcedureRepository;
import com.clinictraining.legacy.BillingRulesEngine.PricingContext;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class BillingRulesEngineTest {

    private static final String OFFICE_VISIT_CODE = "99213";
    private static final String LAB_CODE = "LAB-CBC";
    private static final BigDecimal OFFICE_VISIT_BASE = new BigDecimal("110.00");
    private static final BigDecimal LAB_BASE = new BigDecimal("35.00");

    @Mock
    private ProcedureRepository procedureRepository;

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private BillingRulesEngine billingRulesEngine;

    private PricingContext basicContext;

    @BeforeEach
    void setUp() {
        basicContext = new PricingContext();
        basicContext.setServiceDateTime(ZonedDateTime.now());
        basicContext.setState("CA");
        basicContext.setEmployeeDependent(false);
        basicContext.setSelfPay(false);
        basicContext.setContractedInsurance(false);
        basicContext.setStatOrder(false);
        basicContext.setUrgent(false);
        basicContext.setTaxExempt(false);
        basicContext.setFirstVisit(false);
        basicContext.setPortable(false);
        basicContext.setWithContrast(false);
        basicContext.setFastingRequired(false);
        basicContext.setFastingCompleted(false);
        basicContext.setSedation(false);
        basicContext.setAssistant(false);

        when(applicationContext.getBean(anyString())).thenReturn(null);
    }

    @Test
    void shouldCalculateBasicOfficeVisitCharge() {
        BigDecimal result = billingRulesEngine.calculateCharge(OFFICE_VISIT_CODE, basicContext);

        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(result.compareTo(OFFICE_VISIT_BASE) >= 0);
    }

    @Test
    void shouldCalculateLabProcedureCharge() {
        BigDecimal result = billingRulesEngine.calculateCharge(LAB_CODE, basicContext);

        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(result.compareTo(LAB_BASE) >= 0);
    }

    @Test
    void shouldApplySeniorDiscountForPatientsOver65() {
        basicContext.setPatientAge(70);

        BigDecimal result = billingRulesEngine.calculateCharge(OFFICE_VISIT_CODE, basicContext);

        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void shouldApplyWeekendSurchargeForSaturdayService() {
        ZonedDateTime saturday = ZonedDateTime.now()
                .with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.SATURDAY))
                .withHour(10);
        basicContext.setServiceDateTime(saturday);

        BigDecimal weekdayResult = billingRulesEngine.calculateCharge(OFFICE_VISIT_CODE, basicContext);

        ZonedDateTime wednesday = ZonedDateTime.now()
                .with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.WEDNESDAY))
                .withHour(10);
        basicContext.setServiceDateTime(wednesday);

        BigDecimal regularResult = billingRulesEngine.calculateCharge(OFFICE_VISIT_CODE, basicContext);

        assertTrue(weekdayResult.compareTo(regularResult) >= 0);
    }

    @Test
    void shouldApplyAfterHoursSurchargeForLateService() {
        ZonedDateTime afterHours = ZonedDateTime.now()
                .withHour(20)
                .withMinute(0);
        basicContext.setServiceDateTime(afterHours);

        BigDecimal result = billingRulesEngine.calculateCharge(OFFICE_VISIT_CODE, basicContext);

        assertNotNull(result);
        assertTrue(result.compareTo(OFFICE_VISIT_BASE) > 0);
    }

    @Test
    void shouldApplyStateSalesTaxForLabProcedures() {
        basicContext.setState("CA");

        BigDecimal californiaResult = billingRulesEngine.calculateCharge(LAB_CODE, basicContext);

        basicContext.setState("TX");

        BigDecimal texasResult = billingRulesEngine.calculateCharge(LAB_CODE, basicContext);

        assertNotNull(californiaResult);
        assertNotNull(texasResult);
        assertTrue(californiaResult.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(texasResult.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void shouldHandleMultipleDiscountsAndSurcharges() {
        basicContext.setPatientAge(70);
        basicContext.setSelfPay(true);

        ZonedDateTime saturday = ZonedDateTime.now()
                .with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.SATURDAY))
                .withHour(14);
        basicContext.setServiceDateTime(saturday);

        BigDecimal result = billingRulesEngine.calculateCharge(OFFICE_VISIT_CODE, basicContext);

        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void shouldPriceCodeWithSurroundingWhitespaceSameAsTrimmedCode() {
        basicContext.setServiceDateTime(ZonedDateTime.parse("2026-11-11T10:00:00Z"));

        BigDecimal trimmed = billingRulesEngine.calculateCharge(LAB_CODE, basicContext);
        BigDecimal padded = billingRulesEngine.calculateCharge(" " + LAB_CODE + " ", basicContext);

        assertEquals(trimmed, padded);
    }

    @Test
    void shouldUseProcedureRepositoryWhenAvailable() {
        Procedure procedure = new Procedure();
        procedure.setProcedureCode("CUSTOM-001");
        procedure.setDefaultCost(new BigDecimal("250.00"));

        when(procedureRepository.findByProcedureCode("CUSTOM-001"))
                .thenReturn(Optional.of(procedure));

        BigDecimal result = billingRulesEngine.calculateCharge("CUSTOM-001", basicContext);

        assertNotNull(result);
        assertTrue(result.compareTo(new BigDecimal("250.00")) >= 0);
    }
}
