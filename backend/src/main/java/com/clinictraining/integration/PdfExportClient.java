package com.clinictraining.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PdfExportClient {

    private static final Logger log = LoggerFactory.getLogger(PdfExportClient.class);

    private final WebClient webClient;

    public PdfExportClient(WebClient.Builder builder,
                           @Value("${integration.pdf.base-url:http://pdf-export-mock:8082}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<PdfExportResponse> exportInvoice(String invoiceNumber) {
        return webClient.post()
                .uri("/export")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new Request(invoiceNumber)))
                .retrieve()
                .bodyToMono(PdfExportResponse.class)
                .doOnError(ex -> log.warn("PDF export failed for {}", invoiceNumber, ex));
    }

    private record Request(String invoiceNumber) { }
}
