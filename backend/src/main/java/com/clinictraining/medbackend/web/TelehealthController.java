package com.clinictraining.medbackend.web;

import com.clinictraining.medbackend.telehealth.TelehealthService;
import com.clinictraining.medbackend.telehealth.TelehealthSessionView;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/telehealth")
public class TelehealthController {

    private final TelehealthService telehealthService;

    public TelehealthController(TelehealthService telehealthService) {
        this.telehealthService = telehealthService;
    }

    @GetMapping
    public ResponseEntity<List<TelehealthSessionView>> list() {
        return ResponseEntity.ok(telehealthService.refreshAll());
    }

    @PostMapping("/{vendorSessionId}/sync")
    public ResponseEntity<TelehealthSessionView> sync(@PathVariable String vendorSessionId) {
        return ResponseEntity.ok(telehealthService.syncSession(vendorSessionId));
    }

    @GetMapping("/{vendorSessionId}")
    public ResponseEntity<TelehealthSessionView> get(@PathVariable String vendorSessionId) {
        return ResponseEntity.ok(telehealthService.find(vendorSessionId));
    }
}
