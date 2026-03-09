package com.clinictraining.integration;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.xml.transform.StringResult;

class TelehealthSoapClientTest {

    @Test
    void parsesReadyResponse() {
        WebServiceTemplate template = new StubWebServiceTemplate("""
                <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:th=\"http://telehealth.example.com/schema\">
                  <soapenv:Body>
                    <th:SessionStatusResponse>
                      <th:sessionId>TH-SESSION-99</th:sessionId>
                      <th:sessionStatus>READY</th:sessionStatus>
                      <th:failureReason></th:failureReason>
                    </th:SessionStatusResponse>
                  </soapenv:Body>
                </soapenv:Envelope>
                """);

        TelehealthSoapClient client = new TelehealthSoapClient(template, "http://example");

        Optional<TelehealthSoapResponse> response = client.fetchSession("TH-SESSION-99");

        Assertions.assertTrue(response.isPresent());
        Assertions.assertEquals("TH-SESSION-99", response.get().getSessionId());
        Assertions.assertEquals("READY", response.get().getStatus());
    }

    @Test
    void handlesMalformedResponse() {
        WebServiceTemplate template = new StubWebServiceTemplate("not-xml");
        TelehealthSoapClient client = new TelehealthSoapClient(template, "http://example");

        Optional<TelehealthSoapResponse> response = client.fetchSession("TH-FAIL");
        Assertions.assertTrue(response.isEmpty());
    }

    private static class StubWebServiceTemplate extends WebServiceTemplate {
        private final String response;

        StubWebServiceTemplate(String response) {
            this.response = response;
        }

        @Override
        public boolean sendSourceAndReceiveToResult(String uri, Source request, Result result) {
            if (result instanceof StringResult stringResult) {
                try {
                    Writer writer = stringResult.getWriter();
                    writer.write(response);
                    writer.flush();
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return true;
        }
    }
}
