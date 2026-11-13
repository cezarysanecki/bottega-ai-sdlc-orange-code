# Plan R-02: wydzielenie ustalania rodziny procedury w `BillingRulesEngine`

## Co zmieniam
Dodaję jedną prywatną metodę `resolveFamily(String procedureCode)`, która robi to samo co dziś: bierze rodzinę z `PROCEDURE_FAMILIES.get(code)`, a gdy jej nie ma, woła `deriveFamilyFromCode(code)`. Podstawiam ją w dwóch miejscach, gdzie ta logika jest dziś skopiowana w całości:
- `applyAdjustments`, `BillingRulesEngine.java:170-173`
- `applyTax`, `BillingRulesEngine.java:417-420`

## Co zostaje bez zmian (K3)
- `applyDiscounts:348`, zniżka `BULK_LAB`
- `applySurcharges:404`, dopłata `STAT_LAB`

Obie metody używają tylko `PROCEDURE_FAMILIES.get(...)`, bez fallbacku. Dla kodów spoza mapy (`LAB-HBA1C`) dostają `null`, więc nie naliczają ani zniżki, ani dopłaty. Gdyby przejęły `resolveFamily`, `LAB-HBA1C` z panelem 5 albo trybem stat dostałby inną kwotę. To jest finding F-05 i decyzja o nim zapada osobno. `deriveFamilyFromCode` też zostaje bez zmian.

## Test charakteryzujący (K1, K2), przed refaktoryzacją
- Nowa klasa `BillingRulesEngineCharacterizationTest` jako `@ParameterizedTest`. Macierz to 12 kodów z wymagania razem z `" LAB-CBC "` × 7 kontekstów: CA, TX, senior (65), weekend po godzinach, stat, panel 5, tax exempt.
- Dane muszą być deterministyczne:
  - stała data i godzina zamiast `ZonedDateTime.now()`, którego używa obecny `setUp`;
  - `procedureRepository.findByProcedureCode` zwraca `Optional.empty()`;
  - `applicationContext.getBean` zwraca `null`, więc skrypt doliczy stałe +12.34;
  - stuby oznaczone `lenient()`.
- Oczekiwane wartości leżą w `src/test/resources/legacy/billing-characterization.csv` (`kod;kontekst;kwota`).
- Wartości utrwalam bez liczenia ręcznego:
  1. Na starym kodzie uruchamiam `./mvnw -B -Dtest=BillingRulesEngineCharacterizationTest -Dcharacterization.record=true test`. Test w tym trybie zapisuje wyniki do CSV i niczego nie sprawdza.
  2. Przeglądam CSV pod kątem kwot zerowych i duplikatów, po czym commituję go razem z testem.
  3. Uruchamiam test w zwykłym trybie (porównanie `compareTo` z CSV) i potwierdzam, że jest zielony na starym kodzie.
- Dopiero potem robię refaktoryzację.

## Weryfikacja (K4)
- Test charakteryzujący zielony po zmianie, a CSV bez zmian (`git diff` pusty).
- `./mvnw -B test` zielone.
- Hook Checkstyle bez naruszeń w obu zmienionych plikach.

## Zatwierdzenie
- Status: ZATWIERDZONY z jedną zmianą: nie commitujesz (krok 2 bez commita), CSV zostaje w drzewie roboczym.
- Kto: prowadzący w próbie — SYMULACJA decyzji uczestnika, nie decyzja człowieka (2026-11-13).

