package com.clinictraining.medbackend.web;

import com.clinictraining.patient.ProviderDirectory;
import com.clinictraining.patient.dto.ProviderSummaryDto;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    private final ProviderDirectory providerDirectory;

    public ProviderController(ProviderDirectory providerDirectory) {
        this.providerDirectory = providerDirectory;
    }

    @GetMapping
    public ResponseEntity<List<ProviderSummaryDto>> listProviders(@RequestParam(required = false) String specialty) {
        if (specialty == null || specialty.isBlank()) {
            return ResponseEntity.ok(providerDirectory.legacyTelehealthRoster());
        }
        return ResponseEntity.ok(providerDirectory.listBySpecialty(specialty));
    }
}
