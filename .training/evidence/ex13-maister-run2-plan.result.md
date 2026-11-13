Analiza gotowa — przyczyna potwierdzona w kodzie. Przedstawiam plan poprawki do akceptacji (narzędzie EnterPlanMode nie jest dostępne w tej sesji, więc plan jest poniżej).

## Bug Analysis

**Root Cause**: `AppointmentService.ensureNoConflicts` (`backend/src/main/java/com/clinictraining/appointment/AppointmentService.java:150-151`) wykonuje `start.toLocalDateTime().atZone(zone)`. To odrzuca offset z żądania i przykleja strefę zegara kliniki, więc zmienia się chwila w czasie. Dla `2026-12-02T09:10Z` przy strefie kliniki `Europe/Warsaw` wynik to `09:10+01:00`, czyli `08:10Z`. Zakres `08:10Z–08:20Z` nie nachodzi na istniejącą wizytę `09:00Z–09:30Z` (10:00–10:30 +01:00), więc zapytanie `findOverlappingAppointments` (`AppointmentRepository.java:21`, porównuje chwile) nic nie zwraca i rezerwacja przechodzi.
Ten sam błąd jest w `isSlotAvailable` (`AppointmentService.java:100-101`), z którego korzysta `rescheduleAppointment`. Zmiana terminu ma więc tę samą dziurę.

**Affected Files**:
- `backend/src/main/java/com/clinictraining/appointment/AppointmentService.java`
- nowy plik: `backend/src/test/java/com/clinictraining/appointment/AppointmentServiceTest.java`

## Proposed Fix

W obu metodach zamieniam `toLocalDateTime().atZone(zone)` na `withZoneSameInstant(zone)`. Strefa kliniki zostaje zachowana, a chwila w czasie się nie zmienia. To zmiana w 4 liniach, bez zmian w schemacie ani API.

## Test Strategy

Nowy test jednostkowy `AppointmentServiceTest` (JUnit 5 + Mockito), z `ClinicClock` ustawionym na `Clock.fixed(..., Europe/Warsaw)`. Mock `findOverlappingAppointments` będzie działał jak prawdziwa reguła nakładania się, porównując `Instant` z istniejącą wizytą PROV-001 10:00–10:30 +01:00.
- `shouldRejectBookingWhenSameInstantIsSentInUtc`: `bookAppointment(..., 09:10Z, 09:20Z)` ma rzucić `IllegalStateException` i nie wywołać `save`. Na obecnym kodzie test powinien zawieść (RED).
- `shouldReportSlotUnavailableWhenSameInstantIsSentInUtc`: ten sam scenariusz dla `isSlotAvailable`, oczekiwane `false`.
- Kontrola: rezerwacja `10:30Z–10:40Z`, która nie koliduje, ma przejść.

## Applicable Standards

Nie ma standardów Maister (brak `.maister/docs/INDEX.md`). Stosuję `CLAUDE.md`:
- Java: 4 spacje, bez preview API
- testy JUnit 5 + Mockito, nazwy `should...`
- najpierw test regresji, który zawodzi
- nie formatować masowo kodu
- checkstyle `UnusedImports`

## Standards Compliance Checklist

- [ ] Test regresji napisany i czerwony przed zmianą (CLAUDE.md)
- [ ] JUnit 5 + Mockito, nazwy `should...` (CLAUDE.md)
- [ ] 4 spacje, bez preview API (CLAUDE.md)
- [ ] Zmiana ograniczona do linii objętych błędem, bez masowego formatowania (CLAUDE.md)
- [ ] `checkstyle:check` bez nowych naruszeń, w tym nieużywanego importu `ZoneId` (CLAUDE.md)
- [ ] `./mvnw -B test` na zielono

**Akceptujesz plan, razem z poprawką `isSlotAvailable`/zmiany terminu? Mogę też ograniczyć się tylko do `ensureNoConflicts`.**