Problem występuje. Nie zapisałem pliku planu, bo w tej sesji narzędzie Write jest wyłączone, więc plan jest tutaj.

**Przyczyna.** `calculateCharge` przycina kod tylko na potrzeby sprawdzenia, czy jest pusty (`BillingRulesEngine.java:107`). Dalej idzie nieprzycięty `procedureCode`. Dla `" LAB-CBC "` przy kontekście CA, dzień roboczy, 10:00:
- kod nie trafia do cennika ani do repozytorium, a `startsWith("LAB")` zwraca false, więc cena bazowa to 50.00;
- rodzina wychodzi `UNKNOWN`, więc podatek to 5%.

Wynik wynosi 64.84 zamiast 49.88 (wyliczone ręcznie, bez uruchamiania testów).

**Zmiana.** Po sprawdzeniu pustego kodu dodać jedną linię: `procedureCode = procedureCode.trim();`, przed `resolveBaseCharge`. Sprawdzenie zostaje bez zmian (K2). Wielkość liter i znaki wewnątrz kodu się nie zmieniają (K3).

**Test regresji.** `BillingRulesEngineTest.shouldPriceCodeWithSurroundingWhitespaceSameAsTrimmedCode`
- Wejście: `basicContext` ze stałą datą w dzień roboczy, np. `2026-11-11T10:00Z`, oraz kody `" LAB-CBC "` i `"LAB-CBC"`.
- Oczekiwanie: `assertEquals(calculateCharge("LAB-CBC", ctx), calculateCharge(" LAB-CBC ", ctx))`, czyli 49.88.
- Przed poprawką zawiedzie, bo wyjdzie 64.84 zamiast 49.88: kod ze spacjami dostaje cenę zapasową i stawkę podatku dla `UNKNOWN`.

**Polecenia.** W katalogu `backend/`:
1. `./mvnw -B -Dtest=BillingRulesEngineTest test`: test najpierw czerwony, po poprawce zielony.
2. `./mvnw -B test`: sprawdza K4.

**Ryzyka.**
- Repozytorium i skrypt legacy dostaną przycięty kod. To zamierzone, ale kody zapisane w bazie ze spacjami przestaną być znajdowane.
- `setUp` ustawia datę przez `ZonedDateTime.now()`, więc wyniki zależą od dnia uruchomienia. Dlatego nowy test ustawia stałą datę.