# Implementacja: R-02
## R-02
- Test: `BillingRulesEngineCharacterizationTest#shouldKeepCalculatedChargeUnchanged` (parametryzowany, 12 kodów × 7 kontekstów = 84 przypadki; oczekiwane wartości w `backend/src/test/resources/legacy/billing-characterization.csv`)
  - Nagranie na starym kodzie: `./mvnw -B -q -Dtest=BillingRulesEngineCharacterizationTest -Dcharacterization.record=true test` → CSV 84 wiersze, bez kwot zerowych i bez zduplikowanych kluczy.
  - Przed zmianą (tryb porównania, stary kod): `./mvnw -B -Dtest=BillingRulesEngineCharacterizationTest test` → Tests run: 84, Failures: 0, Errors: 0, BUILD SUCCESS (test charakteryzujący, ma przejść).
  - Po zmianie: Tests run: 84, Failures: 0, Errors: 0, BUILD SUCCESS — zielony.
- Zmiana: `backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:485` — nowa prywatna `resolveFamily(String)`; użyta w `applyAdjustments` (:170) i `applyTax` (:414). `applyDiscounts` (:345), `applySurcharges` (:401) i `deriveFamilyFromCode` bez zmian (K3).
- Checkstyle (hook PostToolUse): brak naruszeń w `BillingRulesEngine.java` i `BillingRulesEngineCharacterizationTest.java`.

## Testy końcowe
- `cd backend && ./mvnw -B test` → Tests run: 106, Failures: 0, Errors: 0, Skipped: 0, BUILD SUCCESS

## Odstępstwa od planu
- Brak commita CSV i testu (zgodnie ze statusem zatwierdzenia). W efekcie CSV jest nieśledzony i `git diff` na nim jest pusty z definicji; niezmienność zapewnia to, że tryb porównania nie zapisuje pliku.
