package com.clinictraining.integration;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.xml.transform.StringResult;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
public class TelehealthSoapClient {

    private static final Logger log = LoggerFactory.getLogger(TelehealthSoapClient.class);
    private final WebServiceTemplate webServiceTemplate;
    private final String endpoint;

    public TelehealthSoapClient(WebServiceTemplate webServiceTemplate,
                                @Value("${integration.telehealth.soap-endpoint:http://telehealth-mock:8080/soap/session}") String endpoint) {
        this.webServiceTemplate = webServiceTemplate;
        this.endpoint = endpoint;
    }

    public Optional<TelehealthSoapResponse> fetchSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }

        try {
            String requestXml = buildRequest(sessionId.trim());
            Source source = new StreamSource(new StringReader(requestXml));
            StringResult result = new StringResult();
            webServiceTemplate.sendSourceAndReceiveToResult(endpoint, source, result);
            String xml = result.toString();
            return parseResponse(xml);
        } catch (Exception ex) {
            log.warn("Failed to query telehealth SOAP endpoint for session {}", sessionId, ex);
            return Optional.empty();
        }
    }

    private String buildRequest(String sessionId) {
        return """
                <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:th=\"http://telehealth.example.com/schema\">
                  <soapenv:Header/>
                  <soapenv:Body>
                    <th:SessionStatusRequest>
                      <th:sessionId>%s</th:sessionId>
                    </th:SessionStatusRequest>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(sessionId);
    }

    private Optional<TelehealthSoapResponse> parseResponse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document document = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));

            String sessionId = readNode(document, "sessionId");
            String status = readNode(document, "sessionStatus");
            String failure = readNode(document, "failureReason");

            TelehealthSoapResponse response = new TelehealthSoapResponse();
            response.setSessionId(sessionId);
            response.setStatus(status == null ? "UNKNOWN" : status.trim());
            response.setFailureReason(failure == null ? "" : failure.trim());
            return Optional.of(response);
        } catch (Exception ex) {
            log.error("Unable to parse SOAP response: {}", truncate(xml), ex);
            return Optional.empty();
        }
    }

    private String readNode(Document document, String localName) {
        NodeList list = document.getElementsByTagNameNS("http://telehealth.example.com/schema", localName);
        if (list == null || list.getLength() == 0) {
            list = document.getElementsByTagName(localName);
        }
        if (list == null || list.getLength() == 0) {
            return null;
        }
        return list.item(0).getTextContent();
    }

    private String truncate(String xml) {
        if (xml == null) {
            return "<empty>";
        }
        byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= 512) {
            return xml;
        }
        return new String(bytes, 0, 512, StandardCharsets.UTF_8) + "...";
    }
}
