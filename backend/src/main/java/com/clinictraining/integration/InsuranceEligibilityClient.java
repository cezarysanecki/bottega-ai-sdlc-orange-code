package com.clinictraining.integration;

import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class InsuranceEligibilityClient {

    private static final Logger log = LoggerFactory.getLogger(InsuranceEligibilityClient.class);

    private final WebClient webClient;

    public InsuranceEligibilityClient(WebClient.Builder builder,
                                      @Value("${integration.insurance.base-url:http://insurance-mock:8081}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Optional<InsuranceEligibilityPayload> checkEligibility(String patientCode) {
        try {
            return webClient.post()
                    .uri("/eligibility")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(new EligibilityRequest(patientCode)))
                    .retrieve()
                    .bodyToMono(InsuranceEligibilityPayload.class)
                    .timeout(Duration.ofSeconds(3))
                    .onErrorResume(ex -> {
                        log.warn("Eligibility request failed for {}", patientCode, ex);
                        return Mono.empty();
                    })
                    .blockOptional();
        } catch (Exception ex) {
            log.warn("Eligibility check crashed for {}", patientCode, ex);
            return Optional.empty();
        }
    }

    private record EligibilityRequest(String patientCode) { }
}
