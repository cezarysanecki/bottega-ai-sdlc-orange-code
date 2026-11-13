package com.clinictraining.legacy;

import com.clinictraining.billing.model.Procedure;
import com.clinictraining.billing.repository.ProcedureRepository;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class BillingRulesEngine {

    private static final Logger log = LoggerFactory.getLogger(BillingRulesEngine.class);

    private final ProcedureRepository procedureRepository;
    private final ApplicationContext applicationContext;

    private static final Map<String, BigDecimal> LEGACY_PRICING_TABLE = new HashMap<>();
    private static final Map<String, String> PROCEDURE_FAMILIES = new HashMap<>();
    private static final Map<String, BigDecimal> DISCOUNT_RULES = new HashMap<>();
    private static final Map<String, BigDecimal> SURCHARGE_RULES = new HashMap<>();

    static {
        LEGACY_PRICING_TABLE.put("99201", new BigDecimal("75.00"));
        LEGACY_PRICING_TABLE.put("99202", new BigDecimal("110.00"));
        LEGACY_PRICING_TABLE.put("99203", new BigDecimal("145.00"));
        LEGACY_PRICING_TABLE.put("99204", new BigDecimal("210.00"));
        LEGACY_PRICING_TABLE.put("99205", new BigDecimal("280.00"));
        LEGACY_PRICING_TABLE.put("99211", new BigDecimal("45.00"));
        LEGACY_PRICING_TABLE.put("99212", new BigDecimal("75.00"));
        LEGACY_PRICING_TABLE.put("99213", new BigDecimal("110.00"));
        LEGACY_PRICING_TABLE.put("99214", new BigDecimal("165.00"));
        LEGACY_PRICING_TABLE.put("99215", new BigDecimal("220.00"));
        LEGACY_PRICING_TABLE.put("XR-CHEST", new BigDecimal("125.00"));
        LEGACY_PRICING_TABLE.put("XR-ABD", new BigDecimal("145.00"));
        LEGACY_PRICING_TABLE.put("XR-SPINE", new BigDecimal("165.00"));
        LEGACY_PRICING_TABLE.put("XR-EXTREMITY", new BigDecimal("95.00"));
        LEGACY_PRICING_TABLE.put("LAB-CBC", new BigDecimal("35.00"));
        LEGACY_PRICING_TABLE.put("LAB-CMP", new BigDecimal("45.00"));
        LEGACY_PRICING_TABLE.put("LAB-LIPID", new BigDecimal("55.00"));
        LEGACY_PRICING_TABLE.put("LAB-UA", new BigDecimal("25.00"));
        LEGACY_PRICING_TABLE.put("LAB-CULTURE", new BigDecimal("75.00"));
        LEGACY_PRICING_TABLE.put("TEL-FOLLOWUP", new BigDecimal("75.00"));
        LEGACY_PRICING_TABLE.put("TEL-CONSULT", new BigDecimal("125.00"));
        LEGACY_PRICING_TABLE.put("TEL-URGENT", new BigDecimal("150.00"));
        LEGACY_PRICING_TABLE.put("PROC-SUTURE", new BigDecimal("175.00"));
        LEGACY_PRICING_TABLE.put("PROC-INJECTION", new BigDecimal("85.00"));
        LEGACY_PRICING_TABLE.put("PROC-BIOPSY", new BigDecimal("225.00"));

        PROCEDURE_FAMILIES.put("99201", "OFFICE_VISIT_NEW");
        PROCEDURE_FAMILIES.put("99202", "OFFICE_VISIT_NEW");
        PROCEDURE_FAMILIES.put("99203", "OFFICE_VISIT_NEW");
        PROCEDURE_FAMILIES.put("99204", "OFFICE_VISIT_NEW");
        PROCEDURE_FAMILIES.put("99205", "OFFICE_VISIT_NEW");
        PROCEDURE_FAMILIES.put("99211", "OFFICE_VISIT_ESTABLISHED");
        PROCEDURE_FAMILIES.put("99212", "OFFICE_VISIT_ESTABLISHED");
        PROCEDURE_FAMILIES.put("99213", "OFFICE_VISIT_ESTABLISHED");
        PROCEDURE_FAMILIES.put("99214", "OFFICE_VISIT_ESTABLISHED");
        PROCEDURE_FAMILIES.put("99215", "OFFICE_VISIT_ESTABLISHED");
        PROCEDURE_FAMILIES.put("XR-CHEST", "RADIOLOGY");
        PROCEDURE_FAMILIES.put("XR-ABD", "RADIOLOGY");
        PROCEDURE_FAMILIES.put("XR-SPINE", "RADIOLOGY");
        PROCEDURE_FAMILIES.put("XR-EXTREMITY", "RADIOLOGY");
        PROCEDURE_FAMILIES.put("LAB-CBC", "LABORATORY");
        PROCEDURE_FAMILIES.put("LAB-CMP", "LABORATORY");
        PROCEDURE_FAMILIES.put("LAB-LIPID", "LABORATORY");
        PROCEDURE_FAMILIES.put("LAB-UA", "LABORATORY");
        PROCEDURE_FAMILIES.put("LAB-CULTURE", "LABORATORY");
        PROCEDURE_FAMILIES.put("TEL-FOLLOWUP", "TELEHEALTH");
        PROCEDURE_FAMILIES.put("TEL-CONSULT", "TELEHEALTH");
        PROCEDURE_FAMILIES.put("TEL-URGENT", "TELEHEALTH");
        PROCEDURE_FAMILIES.put("PROC-SUTURE", "PROCEDURE");
        PROCEDURE_FAMILIES.put("PROC-INJECTION", "PROCEDURE");
        PROCEDURE_FAMILIES.put("PROC-BIOPSY", "PROCEDURE");

        DISCOUNT_RULES.put("BULK_LAB", new BigDecimal("0.85"));
        DISCOUNT_RULES.put("SENIOR", new BigDecimal("0.90"));
        DISCOUNT_RULES.put("EMPLOYEE", new BigDecimal("0.75"));
        DISCOUNT_RULES.put("INSURANCE_CONTRACTED", new BigDecimal("0.80"));
        DISCOUNT_RULES.put("SELF_PAY", new BigDecimal("0.95"));

        SURCHARGE_RULES.put("AFTER_HOURS", new BigDecimal("1.25"));
        SURCHARGE_RULES.put("WEEKEND", new BigDecimal("1.35"));
        SURCHARGE_RULES.put("HOLIDAY", new BigDecimal("1.50"));
        SURCHARGE_RULES.put("URGENT", new BigDecimal("1.20"));
        SURCHARGE_RULES.put("STAT_LAB", new BigDecimal("1.40"));
    }

    public BillingRulesEngine(ProcedureRepository procedureRepository,
                              ApplicationContext applicationContext) {
        this.procedureRepository = procedureRepository;
        this.applicationContext = applicationContext;
    }

    /**
     * Calculates the charge for a procedure. The procedure code is normalized with {@code trim()};
     * a {@code null} or blank code yields {@link BigDecimal#ZERO}.
     */
    public BigDecimal calculateCharge(String procedureCode, PricingContext context) {
        if (procedureCode == null || procedureCode.trim().isEmpty()) {
            log.warn("Empty procedure code provided");
            return BigDecimal.ZERO;
        }
        procedureCode = procedureCode.trim();

        BigDecimal baseCharge = resolveBaseCharge(procedureCode);

        if (baseCharge.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("No pricing found for procedure: {}", procedureCode);
            return BigDecimal.ZERO;
        }

        BigDecimal adjusted = applyAdjustments(procedureCode, baseCharge, context);

        BigDecimal withDiscounts = applyDiscounts(procedureCode, adjusted, context);

        BigDecimal withSurcharges = applySurcharges(procedureCode, withDiscounts, context);

        BigDecimal withTax = applyTax(procedureCode, withSurcharges, context);

        BigDecimal rounded = roundToNearestCent(withTax);

        BigDecimal final_amount = applyLegacyScript(procedureCode, rounded, context);

        log.debug("Calculated charge for {}: {} -> {} -> {} -> {} -> {} -> {}",
                procedureCode, baseCharge, adjusted, withDiscounts, withSurcharges, withTax, final_amount);

        return final_amount;
    }

    private BigDecimal resolveBaseCharge(String procedureCode) {
        if (LEGACY_PRICING_TABLE.containsKey(procedureCode)) {
            return LEGACY_PRICING_TABLE.get(procedureCode);
        }

        Optional<Procedure> procedure = procedureRepository.findByProcedureCode(procedureCode);
        if (procedure.isPresent()) {
            BigDecimal cost = procedure.get().getDefaultCost();
            if (cost != null && cost.compareTo(BigDecimal.ZERO) > 0) {
                return cost;
            }
        }

        if (procedureCode.startsWith("XR")) {
            return new BigDecimal("125.00");
        } else if (procedureCode.startsWith("LAB")) {
            return new BigDecimal("45.00");
        } else if (procedureCode.startsWith("TEL")) {
            return new BigDecimal("75.00");
        } else if (procedureCode.startsWith("PROC")) {
            return new BigDecimal("150.00");
        } else if (procedureCode.startsWith("992")) {
            return new BigDecimal("110.00");
        }

        return new BigDecimal("50.00");
    }

    private BigDecimal applyAdjustments(String procedureCode, BigDecimal base, PricingContext context) {
        String family = resolveFamily(procedureCode);

        BigDecimal adjusted = base;

        switch (family) {
            case "OFFICE_VISIT_NEW":
                if (Boolean.TRUE.equals(context.isFirstVisit())) {
                    adjusted = adjusted.multiply(new BigDecimal("1.15"));
                }
                if (context.getComplexityLevel() != null) {
                    switch (context.getComplexityLevel()) {
                        case "LOW":
                            adjusted = adjusted.multiply(new BigDecimal("0.90"));
                            break;
                        case "MODERATE":
                            break;
                        case "HIGH":
                            adjusted = adjusted.multiply(new BigDecimal("1.20"));
                            break;
                        case "VERY_HIGH":
                            adjusted = adjusted.multiply(new BigDecimal("1.40"));
                            break;
                    }
                }
                break;

            case "OFFICE_VISIT_ESTABLISHED":
                if (context.getTimeSinceLastVisit() != null) {
                    int daysSince = context.getTimeSinceLastVisit();
                    if (daysSince < 30) {
                        adjusted = adjusted.multiply(new BigDecimal("0.85"));
                    } else if (daysSince > 365) {
                        adjusted = adjusted.multiply(new BigDecimal("1.10"));
                    }
                }
                if (context.getComplexityLevel() != null) {
                    switch (context.getComplexityLevel()) {
                        case "LOW":
                            adjusted = adjusted.multiply(new BigDecimal("0.85"));
                            break;
                        case "MODERATE":
                            break;
                        case "HIGH":
                            adjusted = adjusted.multiply(new BigDecimal("1.25"));
                            break;
                        case "VERY_HIGH":
                            adjusted = adjusted.multiply(new BigDecimal("1.50"));
                            break;
                    }
                }
                break;

            case "RADIOLOGY":
                if (Boolean.TRUE.equals(context.isPortable())) {
                    adjusted = adjusted.multiply(new BigDecimal("1.30"));
                }
                if (Boolean.TRUE.equals(context.isWithContrast())) {
                    adjusted = adjusted.multiply(new BigDecimal("1.45"));
                }
                if (context.getNumberOfViews() != null) {
                    int views = context.getNumberOfViews();
                    if (views == 1) {
                        adjusted = adjusted.multiply(new BigDecimal("0.80"));
                    } else if (views == 2) {
                    } else if (views >= 3) {
                        adjusted = adjusted.multiply(new BigDecimal("1.20"));
                    }
                }
                if (Boolean.TRUE.equals(context.isStatOrder())) {
                    adjusted = adjusted.multiply(new BigDecimal("1.35"));
                }
                break;

            case "LABORATORY":
                if (Boolean.TRUE.equals(context.isStatOrder())) {
                    adjusted = adjusted.multiply(new BigDecimal("1.40"));
                }
                if (Boolean.TRUE.equals(context.isFastingRequired()) && Boolean.TRUE.equals(context.isFastingCompleted())) {
                    adjusted = adjusted.multiply(new BigDecimal("1.05"));
                }
                if (context.getLabPanelSize() != null) {
                    int panelSize = context.getLabPanelSize();
                    if (panelSize >= 5) {
                        adjusted = adjusted.multiply(new BigDecimal("0.90"));
                    } else if (panelSize >= 10) {
                        adjusted = adjusted.multiply(new BigDecimal("0.85"));
                    }
                }
                break;

            case "TELEHEALTH":
                if (context.getTelehealthPlatform() != null) {
                    switch (context.getTelehealthPlatform()) {
                        case "VIDEO":
                            break;
                        case "PHONE":
                            adjusted = adjusted.multiply(new BigDecimal("0.85"));
                            break;
                        case "CHAT":
                            adjusted = adjusted.multiply(new BigDecimal("0.70"));
                            break;
                    }
                }
                if (context.getDuration() != null) {
                    int minutes = context.getDuration();
                    if (minutes < 15) {
                        adjusted = adjusted.multiply(new BigDecimal("0.75"));
                    } else if (minutes > 45) {
                        adjusted = adjusted.multiply(new BigDecimal("1.25"));
                    } else if (minutes > 60) {
                        adjusted = adjusted.multiply(new BigDecimal("1.50"));
                    }
                }
                break;

            case "PROCEDURE":
                if (Boolean.TRUE.equals(context.requiresSedation())) {
                    adjusted = adjusted.multiply(new BigDecimal("1.40"));
                }
                if (Boolean.TRUE.equals(context.requiresAssistant())) {
                    adjusted = adjusted.multiply(new BigDecimal("1.20"));
                }
                if (context.getAnesthesiaType() != null) {
                    switch (context.getAnesthesiaType()) {
                        case "LOCAL":
                            adjusted = adjusted.multiply(new BigDecimal("1.10"));
                            break;
                        case "REGIONAL":
                            adjusted = adjusted.multiply(new BigDecimal("1.30"));
                            break;
                        case "GENERAL":
                            adjusted = adjusted.multiply(new BigDecimal("1.60"));
                            break;
                    }
                }
                break;

            default:
                log.debug("No specific adjustments for family: {}", family);
        }

        return adjusted;
    }

    private BigDecimal applyDiscounts(String procedureCode, BigDecimal amount, PricingContext context) {
        BigDecimal discounted = amount;

        if (context.getPatientAge() != null && context.getPatientAge() >= 65) {
            BigDecimal seniorDiscount = DISCOUNT_RULES.get("SENIOR");
            if (seniorDiscount != null) {
                discounted = discounted.multiply(seniorDiscount);
            }
        }

        if (Boolean.TRUE.equals(context.isEmployeeDependent())) {
            BigDecimal employeeDiscount = DISCOUNT_RULES.get("EMPLOYEE");
            if (employeeDiscount != null) {
                discounted = discounted.multiply(employeeDiscount);
            }
        }

        if (Boolean.TRUE.equals(context.isSelfPay())) {
            BigDecimal selfPayDiscount = DISCOUNT_RULES.get("SELF_PAY");
            if (selfPayDiscount != null) {
                discounted = discounted.multiply(selfPayDiscount);
            }
        }

        if (Boolean.TRUE.equals(context.hasContractedInsurance())) {
            BigDecimal insuranceDiscount = DISCOUNT_RULES.get("INSURANCE_CONTRACTED");
            if (insuranceDiscount != null) {
                discounted = discounted.multiply(insuranceDiscount);
            }
        }

        String family = PROCEDURE_FAMILIES.get(procedureCode);
        if ("LABORATORY".equals(family) && context.getLabPanelSize() != null && context.getLabPanelSize() >= 5) {
            BigDecimal bulkDiscount = DISCOUNT_RULES.get("BULK_LAB");
            if (bulkDiscount != null) {
                discounted = discounted.multiply(bulkDiscount);
            }
        }

        if (context.getCustomDiscountPercent() != null) {
            BigDecimal customDiscount = BigDecimal.ONE.subtract(
                    context.getCustomDiscountPercent().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
            );
            discounted = discounted.multiply(customDiscount);
        }

        return discounted;
    }

    private BigDecimal applySurcharges(String procedureCode, BigDecimal amount, PricingContext context) {
        BigDecimal surcharged = amount;

        if (context.getServiceDateTime() != null) {
            ZonedDateTime serviceTime = context.getServiceDateTime();
            int hour = serviceTime.getHour();
            DayOfWeek dayOfWeek = serviceTime.getDayOfWeek();

            if (hour < 7 || hour > 18) {
                BigDecimal afterHours = SURCHARGE_RULES.get("AFTER_HOURS");
                if (afterHours != null) {
                    surcharged = surcharged.multiply(afterHours);
                }
            }

            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                BigDecimal weekend = SURCHARGE_RULES.get("WEEKEND");
                if (weekend != null) {
                    surcharged = surcharged.multiply(weekend);
                }
            }

            if (isHoliday(serviceTime.toLocalDate())) {
                BigDecimal holiday = SURCHARGE_RULES.get("HOLIDAY");
                if (holiday != null) {
                    surcharged = surcharged.multiply(holiday);
                }
            }
        }

        if (Boolean.TRUE.equals(context.isUrgent())) {
            BigDecimal urgent = SURCHARGE_RULES.get("URGENT");
            if (urgent != null) {
                surcharged = surcharged.multiply(urgent);
            }
        }

        if (Boolean.TRUE.equals(context.isStatOrder())) {
            String family = PROCEDURE_FAMILIES.get(procedureCode);
            if ("LABORATORY".equals(family)) {
                BigDecimal statLab = SURCHARGE_RULES.get("STAT_LAB");
                if (statLab != null) {
                    surcharged = surcharged.multiply(statLab);
                }
            }
        }

        return surcharged;
    }

    private BigDecimal applyTax(String procedureCode, BigDecimal amount, PricingContext context) {
        String family = resolveFamily(procedureCode);

        BigDecimal taxRate = BigDecimal.ZERO;

        switch (family) {
            case "OFFICE_VISIT_NEW":
            case "OFFICE_VISIT_ESTABLISHED":
            case "TELEHEALTH":
                taxRate = BigDecimal.ZERO;
                break;
            case "RADIOLOGY":
            case "LABORATORY":
                if (context.getState() != null) {
                    switch (context.getState()) {
                        case "CA":
                            taxRate = new BigDecimal("0.0725");
                            break;
                        case "TX":
                            taxRate = new BigDecimal("0.0625");
                            break;
                        case "NY":
                            taxRate = new BigDecimal("0.08");
                            break;
                        case "FL":
                            taxRate = new BigDecimal("0.06");
                            break;
                        default:
                            taxRate = new BigDecimal("0.05");
                    }
                } else {
                    taxRate = new BigDecimal("0.05");
                }
                break;
            case "PROCEDURE":
                taxRate = new BigDecimal("0.03");
                break;
            default:
                taxRate = new BigDecimal("0.05");
        }

        if (Boolean.TRUE.equals(context.isTaxExempt())) {
            taxRate = BigDecimal.ZERO;
        }

        BigDecimal taxAmount = amount.multiply(taxRate);
        return amount.add(taxAmount);
    }

    private BigDecimal applyLegacyScript(String procedureCode, BigDecimal amount, PricingContext context) {
        try {
            Object script = applicationContext.getBean("legacyBillingAdjuster");
            if (script != null) {
                Object adjusted = InvokerHelper.invokeMethod(script, "adjust", new Object[]{amount, procedureCode, context});
                if (adjusted instanceof BigDecimal) {
                    return (BigDecimal) adjusted;
                } else if (adjusted instanceof Number) {
                    return new BigDecimal(adjusted.toString());
                }
                log.warn("Legacy script returned unexpected type: {}", adjusted.getClass());
            }
        } catch (Exception ex) {
            log.debug("Legacy script not available or failed: {}", ex.getMessage());
        }

        return amount.add(new BigDecimal("12.34"));
    }

    private BigDecimal roundToNearestCent(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String resolveFamily(String procedureCode) {
        String family = PROCEDURE_FAMILIES.get(procedureCode);
        if (family == null) {
            family = deriveFamilyFromCode(procedureCode);
        }
        return family;
    }

    private String deriveFamilyFromCode(String procedureCode) {
        if (procedureCode.startsWith("992")) {
            if (procedureCode.startsWith("9920")) {
                return "OFFICE_VISIT_NEW";
            } else {
                return "OFFICE_VISIT_ESTABLISHED";
            }
        } else if (procedureCode.startsWith("XR")) {
            return "RADIOLOGY";
        } else if (procedureCode.startsWith("LAB")) {
            return "LABORATORY";
        } else if (procedureCode.startsWith("TEL")) {
            return "TELEHEALTH";
        } else if (procedureCode.startsWith("PROC")) {
            return "PROCEDURE";
        }
        return "UNKNOWN";
    }

    private boolean isHoliday(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        if (month == 1 && day == 1) return true;
        if (month == 7 && day == 4) return true;
        if (month == 12 && day == 25) return true;
        if (month == 11 && day >= 22 && day <= 28 && date.getDayOfWeek() == DayOfWeek.THURSDAY) return true;

        return false;
    }

    public static class PricingContext {
        private Boolean firstVisit;
        private String complexityLevel;
        private Integer timeSinceLastVisit;
        private Boolean portable;
        private Boolean withContrast;
        private Integer numberOfViews;
        private Boolean statOrder;
        private Boolean fastingRequired;
        private Boolean fastingCompleted;
        private Integer labPanelSize;
        private String telehealthPlatform;
        private Integer duration;
        private Boolean sedation;
        private Boolean assistant;
        private String anesthesiaType;
        private Integer patientAge;
        private Boolean employeeDependent;
        private Boolean selfPay;
        private Boolean contractedInsurance;
        private BigDecimal customDiscountPercent;
        private ZonedDateTime serviceDateTime;
        private Boolean urgent;
        private String state;
        private Boolean taxExempt;

        public Boolean isFirstVisit() { return firstVisit; }
        public void setFirstVisit(Boolean firstVisit) { this.firstVisit = firstVisit; }

        public String getComplexityLevel() { return complexityLevel; }
        public void setComplexityLevel(String complexityLevel) { this.complexityLevel = complexityLevel; }

        public Integer getTimeSinceLastVisit() { return timeSinceLastVisit; }
        public void setTimeSinceLastVisit(Integer timeSinceLastVisit) { this.timeSinceLastVisit = timeSinceLastVisit; }

        public Boolean isPortable() { return portable; }
        public void setPortable(Boolean portable) { this.portable = portable; }

        public Boolean isWithContrast() { return withContrast; }
        public void setWithContrast(Boolean withContrast) { this.withContrast = withContrast; }

        public Integer getNumberOfViews() { return numberOfViews; }
        public void setNumberOfViews(Integer numberOfViews) { this.numberOfViews = numberOfViews; }

        public Boolean isStatOrder() { return statOrder; }
        public void setStatOrder(Boolean statOrder) { this.statOrder = statOrder; }

        public Boolean isFastingRequired() { return fastingRequired; }
        public void setFastingRequired(Boolean fastingRequired) { this.fastingRequired = fastingRequired; }

        public Boolean isFastingCompleted() { return fastingCompleted; }
        public void setFastingCompleted(Boolean fastingCompleted) { this.fastingCompleted = fastingCompleted; }

        public Integer getLabPanelSize() { return labPanelSize; }
        public void setLabPanelSize(Integer labPanelSize) { this.labPanelSize = labPanelSize; }

        public String getTelehealthPlatform() { return telehealthPlatform; }
        public void setTelehealthPlatform(String telehealthPlatform) { this.telehealthPlatform = telehealthPlatform; }

        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }

        public Boolean requiresSedation() { return sedation; }
        public void setSedation(Boolean sedation) { this.sedation = sedation; }

        public Boolean requiresAssistant() { return assistant; }
        public void setAssistant(Boolean assistant) { this.assistant = assistant; }

        public String getAnesthesiaType() { return anesthesiaType; }
        public void setAnesthesiaType(String anesthesiaType) { this.anesthesiaType = anesthesiaType; }

        public Integer getPatientAge() { return patientAge; }
        public void setPatientAge(Integer patientAge) { this.patientAge = patientAge; }

        public Boolean isEmployeeDependent() { return employeeDependent; }
        public void setEmployeeDependent(Boolean employeeDependent) { this.employeeDependent = employeeDependent; }

        public Boolean isSelfPay() { return selfPay; }
        public void setSelfPay(Boolean selfPay) { this.selfPay = selfPay; }

        public Boolean hasContractedInsurance() { return contractedInsurance; }
        public void setContractedInsurance(Boolean contractedInsurance) { this.contractedInsurance = contractedInsurance; }

        public BigDecimal getCustomDiscountPercent() { return customDiscountPercent; }
        public void setCustomDiscountPercent(BigDecimal customDiscountPercent) { this.customDiscountPercent = customDiscountPercent; }

        public ZonedDateTime getServiceDateTime() { return serviceDateTime; }
        public void setServiceDateTime(ZonedDateTime serviceDateTime) { this.serviceDateTime = serviceDateTime; }

        public Boolean isUrgent() { return urgent; }
        public void setUrgent(Boolean urgent) { this.urgent = urgent; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public Boolean isTaxExempt() { return taxExempt; }
        public void setTaxExempt(Boolean taxExempt) { this.taxExempt = taxExempt; }
    }
}
