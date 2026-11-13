Poprawione. Druga rezerwacja tego samego lekarza zapisana w UTC (`09:10Z–09:20Z`) jest teraz odrzucana jako kolizja. Zmiana terminu (`isSlotAvailable`) zgłasza w tej sytuacji brak wolnego terminu.

**Przyczyna:** w `AppointmentService` obie metody, `ensureNoConflicts` i `isSlotAvailable`, przeliczały czas przez `toLocalDateTime().atZone(strefa kliniki)`. To zostawiało godzinę z żądania, a podmieniało offset. Przy strefie `Europe/Warsaw` czas `09:10Z` zamieniał się w `09:10+01:00`, czyli `08:10Z`. Zapytanie o nakładające się wizyty, które porównuje rzeczywiste chwile, nie znajdowało więc wizyty `09:00Z–09:30Z` (10:00–10:30 +01:00).

**Poprawka:** w obu metodach używam teraz `withZoneSameInstant(zone)` (`AppointmentService.java:100-101` i `:150-151`). Strefa kliniki zostaje, a chwila w czasie się nie zmienia. To zmiana 4 linii, bez zmian w schemacie i API.

**Zmienione pliki:**
- `backend/src/main/java/com/clinictraining/appointment/AppointmentService.java`
- `backend/src/test/java/com/clinictraining/appointment/AppointmentServiceTest.java` (nowy)

**Testy (najpierw czerwone, potem zielone):**
- Przed poprawką 2 z 3 testów padały:
  - `shouldRejectBookingWhenSameInstantIsSentInUtc`: oczekiwany wyjątek `IllegalStateException` nie został rzucony.
  - `shouldReportSlotUnavailableWhenSameInstantIsSentInUtc`: wynik `true` zamiast `false`.
  - Trzeci, `shouldBookWhenUtcSlotDoesNotOverlap`, przechodził i przechodzi dalej.
- Za pierwszym razem pierwszy test padał na NullPointerException, bo atrapa zapisu zwracała null. Dodałem jej stub, żeby czerwony wynik wprost pokazywał przyjętą rezerwację.
- W testach atrapa repozytorium sprawdza nakładanie się terminów po chwilach w czasie, tak jak baza. Prawdziwego zapytania na PostgreSQL nie uruchamiałem.
- Po poprawce: `AppointmentServiceTest` 3/3, a `./mvnw -B test` 112/112, `BUILD SUCCESS`. Testów integracyjnych `*IT` nie uruchamiałem, bo wymagają Dockera.
- `checkstyle:check` pokazuje tylko 2 znane naruszenia legacy (`ControllerLoggingAspect`, `InvoiceServiceTest`). Hook nie zgłosił nic dla zmienionych plików.

**Zgodność z zasadami z CLAUDE.md** (nie ma `.maister/docs/INDEX.md`, warto kiedyś uruchomić `/maister:init`):
- ✅ test regresji napisany i czerwony przed zmianą
- ✅ JUnit 5 + Mockito, nazwy `should...`
- ✅ 4 spacje, bez preview API
- ✅ zmienione tylko linie objęte błędem, bez masowego formatowania
- ✅ brak nowych naruszeń checkstyle (`ZoneId` nadal jest używany)
- ✅ `./mvnw -B test` na zielono

**Proponowany commit** (nic nie commitowałem):
```
fix(appointment): wykrywaj kolizje wizyt niezależnie od offsetu w żądaniu

ensureNoConflicts i isSlotAvailable przeliczały czas przez
toLocalDateTime().atZone(strefa kliniki), co przesuwało chwilę o różnicę
offsetów. Rezerwacja wysłana w UTC (toISOString) omijała kolizję
z wizytą zapisaną jako +01:00. Teraz używamy withZoneSameInstant.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
```