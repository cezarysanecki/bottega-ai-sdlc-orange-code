package com.clinictraining.medbackend.web;

import com.clinictraining.billing.model.Procedure;
import com.clinictraining.billing.repository.ProcedureRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/procedures")
public class ProcedureController {

    private final ProcedureRepository procedureRepository;

    public ProcedureController(ProcedureRepository procedureRepository) {
        this.procedureRepository = procedureRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProcedureDto>> getAllProcedures() {
        List<Procedure> procedures = procedureRepository.findAll();

        List<ProcedureDto> dtos = procedures.stream()
                .map(p -> new ProcedureDto(
                        p.getProcedureCode(),
                        p.getDescription(),
                        p.getDefaultCost()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    public static class ProcedureDto {
        private String procedureCode;
        private String description;
        private BigDecimal defaultCost;

        public ProcedureDto(String procedureCode, String description, BigDecimal defaultCost) {
            this.procedureCode = procedureCode;
            this.description = description;
            this.defaultCost = defaultCost;
        }

        public String getProcedureCode() {
            return procedureCode;
        }

        public void setProcedureCode(String procedureCode) {
            this.procedureCode = procedureCode;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getDefaultCost() {
            return defaultCost;
        }

        public void setDefaultCost(BigDecimal defaultCost) {
            this.defaultCost = defaultCost;
        }
    }
}
