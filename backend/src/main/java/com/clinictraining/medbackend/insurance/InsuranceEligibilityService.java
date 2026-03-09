package com.clinictraining.medbackend.insurance;

import com.clinictraining.integration.InsuranceEligibilityClient;
import com.clinictraining.integration.InsuranceEligibilityPayload;
import com.clinictraining.patient.model.Patient;
import com.clinictraining.patient.repository.PatientRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InsuranceEligibilityService {

    private static final Logger log = LoggerFactory.getLogger(InsuranceEligibilityService.class);
    private static final Duration TTL = Duration.ofMinutes(5);

    private final InsuranceEligibilityClient eligibilityClient;
    private final PatientRepository patientRepository;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public InsuranceEligibilityService(InsuranceEligibilityClient eligibilityClient,
                                       PatientRepository patientRepository,
                                       ObjectMapper objectMapper,
                                       MeterRegistry meterRegistry) {
        this.eligibilityClient = eligibilityClient;
        this.patientRepository = patientRepository;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public InsuranceEligibilityView checkEligibility(String patientCode) {
        CacheEntry cached = cache.get(patientCode);
        if (cached != null && !cached.isExpired()) {
            InsuranceEligibilityView view = cached.toView();
            view.setFromCache(true);
            return view;
        }

        Patient patient = patientRepository.findByPatientCode(patientCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown patient " + patientCode));

        Optional<InsuranceEligibilityPayload> payload = eligibilityClient.checkEligibility(patientCode);
        meterRegistry.counter("insurance.checks", "patient", patientCode).increment();
        if (payload.isPresent()) {
            InsuranceEligibilityView view = toView(payload.get(), false);
            persistSnapshot(patient, payload.get());
            cache.put(patientCode, new CacheEntry(view));
            return view;
        }

        log.info("Falling back to stored eligibility snapshot for {}", patientCode);
        InsuranceEligibilityView fallback = patient.getEligibilitySnapshot() != null
                ? readSnapshot(patient.getPatientCode(), patient.getEligibilitySnapshot())
                : toUnknownView(patientCode);
        fallback.setFromCache(true);
        cache.put(patientCode, new CacheEntry(fallback));
        return fallback;
    }

    @Transactional
    public void invalidateCache(String patientCode) {
        cache.remove(patientCode);
        patientRepository.findByPatientCode(patientCode).ifPresent(patient -> {
            patient.setEligibilitySnapshot(null);
            patientRepository.save(patient);
        });
    }

    private void persistSnapshot(Patient patient, InsuranceEligibilityPayload payload) {
        try {
            patient.setEligibilitySnapshot(objectMapper.writeValueAsString(payload));
            patientRepository.save(patient);
        } catch (JsonProcessingException ex) {
            log.warn("Unable to persist eligibility snapshot for {}", patient.getPatientCode(), ex);
        }
    }

    private InsuranceEligibilityView readSnapshot(String patientCode, String snapshot) {
        try {
            InsuranceEligibilityPayload payload = objectMapper.readValue(snapshot, InsuranceEligibilityPayload.class);
            return toView(payload, true);
        } catch (Exception ex) {
            log.warn("Corrupted eligibility snapshot: {}", snapshot, ex);
            InsuranceEligibilityView view = toUnknownView(patientCode);
            view.setCoverageNotes("Snapshot parse failure");
            return view;
        }
    }

    private InsuranceEligibilityView toUnknownView(String patientCode) {
        InsuranceEligibilityView view = new InsuranceEligibilityView();
        view.setPatientCode(patientCode);
        view.setPlanCode("UNKNOWN");
        view.setStatus("UNKNOWN");
        view.setCopay(0);
        view.setDeductibleRemaining(0);
        view.setCoverageNotes("No eligibility data available");
        view.setFetchedAt(ZonedDateTime.now());
        view.setFromCache(true);
        return view;
    }

    private InsuranceEligibilityView toView(InsuranceEligibilityPayload payload, boolean fromCache) {
        InsuranceEligibilityView view = new InsuranceEligibilityView();
        view.setPatientCode(payload.getPatientCode());
        view.setPlanCode(payload.getPlanCode());
        view.setStatus(payload.getStatus());
        view.setCopay(payload.getCopay());
        view.setDeductibleRemaining(payload.getDeductibleRemaining());
        view.setCoverageNotes(payload.getCoverageNotes());
        view.setFromCache(fromCache);
        try {
            if (payload.getFetchedAt() != null) {
                view.setFetchedAt(ZonedDateTime.parse(payload.getFetchedAt()));
            } else {
                view.setFetchedAt(ZonedDateTime.now());
            }
        } catch (Exception ex) {
            view.setFetchedAt(ZonedDateTime.now());
        }
        return view;
    }

    private class CacheEntry {
        private final InsuranceEligibilityView view;
        private final ZonedDateTime cachedAt = ZonedDateTime.now();

        private CacheEntry(InsuranceEligibilityView view) {
            this.view = view;
        }

        private boolean isExpired() {
            return ZonedDateTime.now().isAfter(cachedAt.plus(TTL));
        }

        private InsuranceEligibilityView toView() {
            InsuranceEligibilityView copy = new InsuranceEligibilityView();
            copy.setPatientCode(view.getPatientCode());
            copy.setPlanCode(view.getPlanCode());
            copy.setStatus(view.getStatus());
            copy.setCopay(view.getCopay());
            copy.setDeductibleRemaining(view.getDeductibleRemaining());
            copy.setCoverageNotes(view.getCoverageNotes());
            copy.setFetchedAt(view.getFetchedAt());
            copy.setFromCache(view.isFromCache());
            return copy;
        }
    }
}
