package com.clinictraining.billing;

import com.clinictraining.billing.model.Procedure;
import com.clinictraining.billing.repository.ProcedureRepository;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BillingCalculator {

    private static final Logger log = LoggerFactory.getLogger(BillingCalculator.class);

    private final ProcedureRepository procedureRepository;

    public BillingCalculator(ProcedureRepository procedureRepository) {
        this.procedureRepository = procedureRepository;
    }

    public BigDecimal estimateCharge(String procedureCode) {
        if (procedureCode == null) {
            return BigDecimal.ZERO;
        }

        return procedureRepository.findByProcedureCode(procedureCode)
                .map(Procedure::getDefaultCost)
                .map(cost -> {
                    log.debug("Resolved price for {} from reference table", procedureCode);
                    return cost;
                })
                .orElseGet(() -> {
                    log.warn("Procedure {} missing from reference data, using legacy fallback", procedureCode);
                    return legacyFallback(procedureCode);
                });
    }

    private BigDecimal legacyFallback(String procedureCode) {
        if (procedureCode.startsWith("XR")) {
            return new BigDecimal("125.00");
        }
        if (procedureCode.startsWith("TEL")) {
            return new BigDecimal("75.00");
        }
        return new BigDecimal("50.00");
    }
}
