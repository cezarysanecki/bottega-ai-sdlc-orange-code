# Handoff: wycena procedur w BillingRulesEngine
- Data: 2026-11-13. Commit: f61f08b (branch cezarysanecki/orange-proba-13-cwiczen-cc). Drzewo: czyste poza .training/ i .pipeline/.

## Cel
Uporządkować błędy wyceny w `backend/.../legacy/BillingRulesEngine.java` małymi krokami z testem regresji.

## Stan (zrobione, commit)
- W-01: kod procedury normalizowany przez `trim()` przed wyceną — ca55f0e.
- F-01/F-02 (z `.training/evidence/ex06-pipeline/analysis.md`): flagi `sedation`, `assistant`, `contractedInsurance` równe null traktowane jak false — f9e46c0.
- R-02: `resolveFamily()` w applyAdjustments/applyTax + test charakteryzujący 84 przypadki (CSV w `backend/src/test/resources/legacy/`) — f61f08b.

## Decyzje
- F-05 (BULK_LAB/STAT_LAB bez fallbacku rodziny) świadomie NIE zmienione; test charakteryzujący utrwala obecne kwoty — zmiana wymaga decyzji PO.
- F-03, F-04, F-06 czekają na pytania Q-01/Q-02 w analizie.
- NBSP w kodzie nie jest usuwany (ani trim, ani strip) — poza zakresem W-01.

## Testy wykonane
- `cd backend && ./mvnw -B test` → 106 testów, 0 błędów (2026-11-13).
- `./mvnw -B checkstyle:check` → 2 znane naruszenia legacy (patrz CLAUDE.md).

## Otwarte ryzyka
- Kryterium K2 z W-01 (null, "", "   " → 0) nie ma testu; reviewer (RH-02) to zgłosił.
- Test charakteryzujący zależy od strefy America/Los_Angeles ustawionej w teście, nie od strefy maszyny.

## Następny krok
Dodaj w `BillingRulesEngineTest` sparametryzowany test K2: `null`, `""`, `"   "` → `BigDecimal.ZERO`. Test powinien przejść od razu (to test zabezpieczający, nie regresji). Uruchom klasę i pełne testy.
