# Plan: F-01 F-02
- Commit bazowy: 19cd052 (`backend/` bez zmian względem HEAD; dowody z analizy potwierdzone: l. 286, 289, 338, gettery `Boolean` l. 581, 584, 599)
- Źródło: .pipeline/analysis.md

## Kroki
### F-01 NPE dla rodziny PROCEDURE przy kontekście z InvoiceService
- Test regresji (najpierw, ma zawieść): `BillingRulesEngineTest#shouldNotThrowForProcedureWhenSedationAndAssistantUnset` — `basicContext` z `setUp`, następnie `setServiceDateTime(ZonedDateTime.parse("2026-11-11T10:00:00Z"))`; najpierw `expected = calculateCharge("PROC-SUTURE", basicContext)` (sedation/assistant = `false`), potem `setSedation(null)`, `setAssistant(null)` i `actual = assertDoesNotThrow(() -> calculateCharge("PROC-SUTURE", basicContext))`; oczekiwanie: `assertEquals(expected, actual)`. Dziś: NPE na l. 286. `PROC-SUTURE` jest w `LEGACY_PRICING_TABLE` (l. 57), mock repozytorium niepotrzebny.
- Zmiana: `backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:286` — `if (context.requiresSedation())` → `if (Boolean.TRUE.equals(context.requiresSedation()))`; `:289` — `if (context.requiresAssistant())` → `if (Boolean.TRUE.equals(context.requiresAssistant()))`.
- Poza zakresem: `InvoiceService.buildPricingContext` (nie dopisywać ustawiania flag), getterów `PricingContext` (nie zmieniać typu na `boolean`), `setUp` istniejących testów, pozostałych gałęzi `applyAdjustments` (F-03, F-04), formatowania pliku legacy.

### F-02 NPE w applyDiscounts, gdy `contractedInsurance` nie jest ustawione
- Test regresji (najpierw, ma zawieść): `BillingRulesEngineTest#shouldTreatMissingContractedInsuranceAsFalse` — `basicContext` z `setUp` (contractedInsurance = `false`), `setServiceDateTime(ZonedDateTime.parse("2026-11-11T10:00:00Z"))`; `expected = calculateCharge("99213", basicContext)`, potem `setContractedInsurance(null)` i `actual = assertDoesNotThrow(() -> calculateCharge("99213", basicContext))`; oczekiwanie: `assertEquals(expected, actual)`. Dziś: NPE na l. 338. Kontekst z `setUp` zamiast gołego `new PricingContext()`, żeby test izolował l. 338 (pozostałe pola `null` mogłyby rzucić gdzie indziej).
- Zmiana: `backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:338` — `if (context.hasContractedInsurance())` → `if (Boolean.TRUE.equals(context.hasContractedInsurance()))`.
- Poza zakresem: mapowanie `insurancePlan != null` → contracted w `InvoiceService` (H-02/Q-04), kumulacja rabatów, BULK_LAB (F-05), `customDiscountPercent` (F-06).

## Weryfikacja
- `cd backend && ./mvnw -B -Dtest=BillingRulesEngineTest test` przed zmianą: 2 nowe testy FAIL (NPE), pozostałe PASS; po zmianie: wszystkie PASS.
- `cd backend && ./mvnw -B test` — BUILD SUCCESS, brak nowych porażek.
- `git diff --stat` — zmienione tylko `BillingRulesEngine.java` (3 linie) i `BillingRulesEngineTest.java`.

## Zatwierdzenie
- Status: CZEKA NA ZATWIERDZENIE
