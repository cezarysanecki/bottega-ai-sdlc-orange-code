package com.clinictraining.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import com.clinictraining.billing.repository.ProcedureRepository;
import com.clinictraining.legacy.BillingRulesEngine.PricingContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

/**
 * Characterization test for {@link BillingRulesEngine#calculateCharge}. Expected amounts are stored in
 * {@code src/test/resources/legacy/billing-characterization.csv} ({@code code;context;amount}).
 * Run with {@code -Dcharacterization.record=true} to (re)record the CSV from the current code without asserting.
 */
@ExtendWith(MockitoExtension.class)
class BillingRulesEngineCharacterizationTest {

    private static final String CSV_RESOURCE = "/legacy/billing-characterization.csv";
    private static final Path CSV_SOURCE_PATH = Paths.get("src/test/resources/legacy/billing-characterization.csv");
    private static final boolean RECORD = Boolean.getBoolean("characterization.record");

    private static final ZoneId ZONE = ZoneId.of("America/Los_Angeles");
    private static final ZonedDateTime WEEKDAY_BUSINESS_HOURS = ZonedDateTime.of(2026, 11, 11, 10, 0, 0, 0, ZONE);
    private static final ZonedDateTime SATURDAY_AFTER_HOURS = ZonedDateTime.of(2026, 11, 14, 20, 0, 0, 0, ZONE);

    private static final List<String> CODES = List.of(
            "99203", "99213", "XR-CHEST", "XR-101", "LAB-CBC", "LAB-HBA1C", "TEL-CONSULT",
            "TEL-200", "PROC-SUTURE", "PROC-XYZ", "GEN-001", " LAB-CBC ");

    private static final List<String> CONTEXTS = List.of(
            "CA", "TX", "SENIOR", "WEEKEND_AFTER_HOURS", "STAT", "PANEL_5", "TAX_EXEMPT");

    private static final Map<String, String> RECORDED = new LinkedHashMap<>();

    @Mock
    private ProcedureRepository procedureRepository;

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private BillingRulesEngine billingRulesEngine;

    @BeforeEach
    void setUp() {
        lenient().when(procedureRepository.findByProcedureCode(anyString())).thenReturn(Optional.empty());
        lenient().when(applicationContext.getBean(anyString())).thenReturn(null);
    }

    static Stream<Arguments> matrix() {
        List<Arguments> args = new ArrayList<>();
        for (String code : CODES) {
            for (String context : CONTEXTS) {
                args.add(Arguments.of(code, context));
            }
        }
        return args.stream();
    }

    @ParameterizedTest(name = "[{0}] x {1}")
    @MethodSource("matrix")
    void shouldKeepCalculatedChargeUnchanged(String code, String contextName) throws IOException {
        BigDecimal actual = billingRulesEngine.calculateCharge(code, context(contextName));

        if (RECORD) {
            RECORDED.put(key(code, contextName), actual.toPlainString());
            return;
        }

        String expected = loadExpected().get(key(code, contextName));
        assertNotNull(expected, "No recorded value for " + key(code, contextName));
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                "Charge changed for " + key(code, contextName) + ": expected " + expected + ", got " + actual);
    }

    @AfterAll
    static void writeRecording() throws IOException {
        if (!RECORD) {
            return;
        }
        int expectedCases = CODES.size() * CONTEXTS.size();
        if (RECORDED.size() != expectedCases) {
            throw new IllegalStateException("Recorded " + RECORDED.size() + " of " + expectedCases
                    + " cases; run the whole class from backend/ to re-record");
        }
        List<String> lines = new ArrayList<>();
        RECORDED.forEach((key, amount) -> lines.add(key + ";" + amount));
        Files.createDirectories(CSV_SOURCE_PATH.getParent());
        Files.write(CSV_SOURCE_PATH, lines, StandardCharsets.UTF_8);
    }

    private static String key(String code, String contextName) {
        return code + ";" + contextName;
    }

    private static Map<String, String> expectedCache;

    private static Map<String, String> loadExpected() throws IOException {
        if (expectedCache != null) {
            return expectedCache;
        }
        Map<String, String> expected = new LinkedHashMap<>();
        try (InputStream in = BillingRulesEngineCharacterizationTest.class.getResourceAsStream(CSV_RESOURCE)) {
            assertNotNull(in, "Missing resource " + CSV_RESOURCE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(";", -1);
                expected.put(key(parts[0], parts[1]), parts[2]);
            }
        }
        expectedCache = expected;
        return expected;
    }

    private static PricingContext context(String name) {
        PricingContext context = new PricingContext();
        context.setServiceDateTime(WEEKDAY_BUSINESS_HOURS);
        context.setPatientAge(40);
        context.setState("CA");
        context.setSelfPay(false);
        context.setContractedInsurance(false);
        context.setEmployeeDependent(false);
        context.setStatOrder(false);
        context.setUrgent(false);
        context.setTaxExempt(false);
        context.setFirstVisit(false);
        context.setPortable(false);
        context.setWithContrast(false);
        context.setFastingRequired(false);
        context.setFastingCompleted(false);
        context.setSedation(false);
        context.setAssistant(false);

        switch (name) {
            case "CA":
                break;
            case "TX":
                context.setState("TX");
                break;
            case "SENIOR":
                context.setPatientAge(65);
                break;
            case "WEEKEND_AFTER_HOURS":
                context.setServiceDateTime(SATURDAY_AFTER_HOURS);
                break;
            case "STAT":
                context.setStatOrder(true);
                break;
            case "PANEL_5":
                context.setLabPanelSize(5);
                break;
            case "TAX_EXEMPT":
                context.setTaxExempt(true);
                break;
            default:
                throw new IllegalArgumentException("Unknown context: " + name);
        }
        return context;
    }
}
