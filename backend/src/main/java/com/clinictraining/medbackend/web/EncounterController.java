package com.clinictraining.medbackend.web;

import com.clinictraining.billing.EncounterService;
import com.clinictraining.billing.model.Encounter;
import com.clinictraining.medbackend.api.ApiMapper;
import com.clinictraining.medbackend.api.dto.EncounterResponse;
import com.clinictraining.medbackend.api.dto.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/encounters")
public class EncounterController {

    private final EncounterService encounterService;
    private final ApiMapper apiMapper;

    public EncounterController(EncounterService encounterService, ApiMapper apiMapper) {
        this.encounterService = encounterService;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<EncounterResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Encounter> encountersPage = encounterService.findAllEncounters(pageable);

        List<EncounterResponse> responses = encountersPage.getContent().stream()
                .map(apiMapper::toResponse)
                .collect(Collectors.toList());

        PagedResponse<EncounterResponse> pagedResponse = new PagedResponse<>(
                responses,
                encountersPage.getNumber(),
                encountersPage.getSize(),
                encountersPage.getTotalElements(),
                encountersPage.getTotalPages()
        );

        return ResponseEntity.ok(pagedResponse);
    }

    @PostMapping
    public ResponseEntity<EncounterResponse> create(@RequestBody Map<String, Object> request) {
        Long appointmentId = ((Number) request.get("appointmentId")).longValue();
        String reason = (String) request.get("reason");
        String summary = (String) request.get("summary");

        @SuppressWarnings("unchecked")
        List<String> procedureCodes = (List<String>) request.get("procedureCodes");

        Encounter encounter = encounterService.createEncounterFromAppointment(
                appointmentId,
                reason,
                procedureCodes,
                summary
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiMapper.toResponse(encounter));
    }

    @GetMapping("/{encounterCode}")
    public ResponseEntity<EncounterResponse> get(@PathVariable String encounterCode) {
        Encounter encounter = encounterService.findByEncounterCode(encounterCode);
        return ResponseEntity.ok(apiMapper.toResponse(encounter));
    }

    @PatchMapping("/{encounterCode}/summary")
    public ResponseEntity<EncounterResponse> updateSummary(
            @PathVariable String encounterCode,
            @RequestBody Map<String, String> payload) {

        String summary = payload.get("summary");
        Encounter encounter = encounterService.updateSummary(encounterCode, summary);

        return ResponseEntity.ok(apiMapper.toResponse(encounter));
    }
}
