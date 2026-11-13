## Kryteria
- **K1: spełnione.** Macierz ma 12 kodów razy 7 kontekstów, czyli 84 przypadki (`BillingRulesEngineCharacterizationTest.java`, CODES/CONTEXTS, `matrix()`). Wszystkie wymagane kody są w macierzy, w tym `" LAB-CBC "`. Kody XR-101, LAB-HBA1C, TEL-200, PROC-XYZ i GEN-001 sprawdzają fallback w `applyAdjustments` i `applyTax`. Przeliczyłem ręcznie dwie wartości z CSV:
  - XR-101;CA: 125 × 1,0725 + 12,34 = 146,40 (`billing-characterization.csv:22`),
  - XR-101;STAT: mnożnik 1,35 w RADIOLOGY daje 193,32 (`billing-characterization.csv:26`).

  Usunięcie fallbacku zmieniłoby te wyniki, więc test nie przeszedłby bez poprawnego `resolveFamily`.
- **K2: nieustalone z kodu.** Test i CSV są nieśledzone, a historia commitów nie pokazuje kolejności. Pośrednio potwierdza to `.training/evidence/ex10-verify-old-code.txt` (84 testy, 0 błędów na „STARY KOD”), ale nie widać, z jakiej rewizji go uruchomiono.
- **K3: spełnione.** `applyDiscounts` (`BillingRulesEngine.java:345`) i `applySurcharges` (`BillingRulesEngine.java:401`) nadal wołają samo `PROCEDURE_FAMILIES.get`. CSV utrwala obecne zachowanie:
  - LAB-HBA1C;PANEL_5 = 55,78, bez BULK_LAB (`billing-characterization.csv:41`),
  - LAB-HBA1C;STAT = 79,91, bez STAT_LAB (`billing-characterization.csv:40`).

  Dodatkowo `.training/evidence/ex10-verify-mutation.txt` pokazuje, że po dodaniu fallbacku do `applyDiscounts` jeden test pada.
- **K4: nieustalone.** Nie uruchamiałem testów ani Checkstyle. Przejrzałem importy testu ręcznie i wszystkie są używane; importy w `BillingRulesEngine.java` się nie zmieniły.

## Błędy (pewne)
Brak błędów. `resolveFamily` (`BillingRulesEngine.java:485-491`) zachowuje się dokładnie jak oba usunięte fragmenty. Innych kopii pełnego wzorca w pliku nie ma.

## Hipotezy (niepotwierdzone)
- **RH-01, `BillingRulesEngineCharacterizationTest.java`, `writeRecording`.** Tryb `-Dcharacterization.record=true` nadpisuje CSV tylko tymi przypadkami, które się wykonały. Nagranie z filtrem pojedynczych przypadków zostawiłoby niepełny plik, a potem test zgłaszałby „No recorded value”. Do tego ścieżka `src/test/resources/...` jest względna, więc zadziała tylko przy katalogu roboczym `backend/`. Trzeba by sprawdzić nagrywanie z filtrem `-Dtest=...` i z innego katalogu.

## Uwagi poza zakresem
- W git status są zmienione pliki spoza diffu (`.claude/agents/implementer.md`, `.pipeline/*`). Nie należą do tej zmiany i nie powinny trafić do jej commita.