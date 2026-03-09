package com.clinictraining.medbackend.telehealth;

import com.clinictraining.appointment.telehealth.TelehealthSession;
import com.clinictraining.appointment.telehealth.TelehealthSessionRepository;
import com.clinictraining.integration.TelehealthSoapClient;
import com.clinictraining.integration.TelehealthSoapResponse;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TelehealthService {

    private static final Logger log = LoggerFactory.getLogger(TelehealthService.class);

    private final TelehealthSessionRepository telehealthSessionRepository;
    private final TelehealthSoapClient telehealthSoapClient;
    private final MeterRegistry meterRegistry;

    public TelehealthService(TelehealthSessionRepository telehealthSessionRepository,
                             TelehealthSoapClient telehealthSoapClient,
                             MeterRegistry meterRegistry) {
        this.telehealthSessionRepository = telehealthSessionRepository;
        this.telehealthSoapClient = telehealthSoapClient;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public TelehealthSessionView syncSession(String vendorSessionId) {
        TelehealthSession session = telehealthSessionRepository.findByVendorSessionId(vendorSessionId)
                .orElseGet(() -> {
                    TelehealthSession created = new TelehealthSession();
                    created.setVendorSessionId(vendorSessionId);
                    created.setSessionStatus("UNKNOWN");
                    created.setStartedAt(ZonedDateTime.now());
                    return created;
                });

        Optional<TelehealthSoapResponse> response = telehealthSoapClient.fetchSession(vendorSessionId);
        String vendorStatus = response.map(TelehealthSoapResponse::getStatus).orElse("UNKNOWN");
        String failureReason = response.map(TelehealthSoapResponse::getFailureReason).orElse("");
        meterRegistry.counter("telehealth.calls", "vendor", "soap", "result", vendorStatus).increment();

        String effectiveStatus = vendorStatus;
        if ("FAILED".equalsIgnoreCase(vendorStatus)) {
            log.warn("Vendor responded FAILED for {}, but keeping READY for legacy compatibility", vendorSessionId);
            effectiveStatus = "READY";
        }

        session.setSessionStatus(effectiveStatus);
        session.setFailureReason(failureReason);
        if ("READY".equalsIgnoreCase(effectiveStatus) && session.getEndedAt() == null) {
            session.setEndedAt(ZonedDateTime.now());
        }

        TelehealthSession saved = telehealthSessionRepository.save(session);
        TelehealthSessionView view = toView(saved);
        view.setLastSyncedAt(ZonedDateTime.now());
        return view;
    }

    @Transactional
    public List<TelehealthSessionView> refreshAll() {
        return telehealthSessionRepository.findAll().stream()
                .map(session -> syncSession(session.getVendorSessionId()))
                .toList();
    }

    @Transactional
    public TelehealthSessionView find(String vendorSessionId) {
        TelehealthSession session = telehealthSessionRepository.findByVendorSessionId(vendorSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + vendorSessionId));
        TelehealthSessionView view = toView(session);
        view.setLastSyncedAt(ZonedDateTime.now());
        return view;
    }

    private TelehealthSessionView toView(TelehealthSession session) {
        TelehealthSessionView view = new TelehealthSessionView();
        view.setVendorSessionId(session.getVendorSessionId());
        view.setSessionStatus(session.getSessionStatus());
        view.setFailureReason(session.getFailureReason());
        view.setStartedAt(session.getStartedAt());
        view.setEndedAt(session.getEndedAt());
        return view;
    }
}
