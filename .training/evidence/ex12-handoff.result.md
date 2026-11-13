Test K2 jest dodany i przechodzi. Nic nie commitowałem.

## 1. Weryfikacja handoffu
- **Commity W-01, F-01/F-02 i R-02 się zgadzają:** `ca55f0e`, `f9e46c0`, `f61f08b` są w historii, a plik CSV leży w `backend/src/test/resources/legacy/`.
- **Brak testu K2 się zgadza.** Wymaganie jest w `.training/artifacts/ex03-requirement.md:13`, kod obsługuje te przypadki w `BillingRulesEngine.java:110`, ale żaden test tego nie sprawdzał.
- **Liczba 106 testów się zgadza:** teraz jest 109, czyli 106 i 3 nowe przypadki.
- **Commit w nagłówku jest nieaktualny.** Handoff podaje `f61f08b`, a HEAD to `aacb28b` (commit z raportem). Drzewo też nie jest „czyste poza .training/ i .pipeline/”: wszystkie zmiany są w `.training/`, ale to zmodyfikowane, usunięte i nieśledzone pliki dowodów, nie czyste drzewo.

## 2. Wykonany krok
W `BillingRulesEngineTest.java` dodałem test `shouldReturnZeroForMissingOrBlankProcedureCode`. Sprawdza on wartości `null`, `""` i `"   "` i oczekuje `BigDecimal.ZERO`.

Przy pierwszym uruchomieniu test nie przeszedł, i to nie z powodu kodu produkcyjnego. Przy pustym kodzie metoda kończy się wcześnie i nie woła `getBean`, a Mockito w trybie ścisłym zgłasza wtedy nieużyty stub z `@BeforeEach` (`UnnecessaryStubbingException`). Dlatego ten stub oznaczyłem jako `lenient()`.

Wyniki:
- klasa `BillingRulesEngineTest`: 14 testów, 0 błędów;
- `./mvnw -B test`: 109 testów, 0 błędów, BUILD SUCCESS;
- hook Checkstyle nie zgłosił naruszeń w edytowanym pliku.

## 3. Ocena handoffu
**Brakowało:**
- Informacji, że wczesny `return` w połączeniu ze ścisłym Mockito wywali test. Dało się to przewidzieć, a „powinien przejść od razu” wprowadzało w błąd.
- Gdzie zapisane jest kryterium K2. Musiałem szukać pliku `ex03-requirement.md`.
- Aktualnego HEAD (`aacb28b`) i rzetelnego opisu stanu drzewa.

**Zbędne:**
- Decyzje o F-03 do F-06 i o NBSP oraz ryzyko strefy czasowej. Nie dotyczą tego kroku, choć mogą się przydać później.
- Wynik `checkstyle:check` z dwoma znanymi naruszeniami. To samo jest już w CLAUDE.md.