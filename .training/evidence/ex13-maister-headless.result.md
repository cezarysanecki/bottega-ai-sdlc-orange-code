Krótki status: potwierdziłem przyczynę w kodzie. Brak `.maister/docs/` (nie ma standardów Maister), a narzędzie `EnterPlanMode` jest tu niedostępne, więc plan przedstawiam poniżej i czekam na Twoją akceptację, zanim zmienię kod.

## Bug Analysis

**Root Cause**: `AppointmentService.java:150-151` (i ten sam wzorzec w `isSlotAvailable`, `:100-101`):
```java
ZonedDateTime normalizedStart = start.toLocalDateTime().atZone(zone);
```
`toLocalDateTime()` odrzuca offset, a `atZone(zone)` dokleja strefę kliniki do tej samej godziny „na zegarze”. W efekcie `09:10Z` zamienia się w `09:10+01:00` (czyli `08:10Z`), a więc przesuwa się o godzinę. Zapytanie `findOverlappingAppointments` (`AppointmentRepository.java:21`, `start < :end and end > :start`) porównuje wtedy 08:10–08:20Z z istniejącą wizytą 09:00–09:30Z, nie znajduje kolizji i rezerwacja dostaje status SCHEDULED.
To samo dzieje się w `isSlotAvailable`, z którego korzysta `rescheduleAppointment`, więc przy przekładaniu wizyty kolizja też może przejść niezauważona.

**Affected Files**:
- `backend/src/main/java/com/clinictraining/appointment/AppointmentService.java`
- nowy plik `backend/src/test/java/com/clinictraining/appointment/AppointmentServiceTest.java`

**Complexity**: nie ma powodu do eskalacji. Zmiana dotyczy jednego pliku, bez migracji i bez kodu związanego z bezpieczeństwem.

## Proposed Fix
W obu metodach zastąpić `x.toLocalDateTime().atZone(zone)` przez `x.withZoneSameInstant(zone)`. Chwila w czasie zostaje ta sama, zmienia się tylko strefa, w której jest zapisana, więc intencja „normalizuj do strefy kliniki” zostaje zachowana. Nie ruszam innych miejsc w kodzie.

## Test Strategy
Nowa klasa `AppointmentServiceTest` (JUnit 5 + Mockito). Używa `ClinicClock` z ustalonym zegarem w `Europe/Warsaw` i `SimpleMeterRegistry`. Mock `findOverlappingAppointments` symuluje zapytanie SQL: zwraca istniejącą wizytę PROV-001 (2026-12-02 10:00–10:30 +01:00), jeśli przekazany przedział nakłada się na nią w sensie chwil (`Instant`).
- `shouldRejectBookingWhenUtcSlotOverlapsExistingAppointment`: wywołuje `bookAppointment` z 09:10Z–09:20Z i oczekuje `IllegalStateException` oraz braku `save`. Na obecnym kodzie test powinien **zawieść**.
- `shouldReportSlotUnavailableWhenUtcSlotOverlapsExistingAppointment`: wywołuje `isSlotAvailable` z tym samym slotem i oczekuje `false`. Na obecnym kodzie też powinien zawieść.
- Kontrola poprawności: slot UTC, który nie nakłada się na wizytę (np. 09:30Z–09:40Z), ma zostać przyjęty.

Potem uruchomię `./mvnw -B -Dtest=AppointmentServiceTest test`, pełne `./mvnw -B test` i lint przez checkstyle (hook).

## Applicable Standards
No Maister standards found. Consider running `/maister:init`. Stosuję konwencje z `CLAUDE.md`.

## Standards Compliance Checklist
- [ ] Najpierw test regresji, który zawodzi, dopiero potem zmiana (CLAUDE.md)
- [ ] JUnit 5 + Mockito, nazwy testów `should...` (CLAUDE.md)
- [ ] Java: 4 spacje, bez preview API (CLAUDE.md)
- [ ] Zmieniam tylko to, czego dotyczy zadanie, bez masowego formatowania (CLAUDE.md)
- [ ] Brak nieużywanych importów (checkstyle `UnusedImports`)

Akceptujesz plan? Mam objąć poprawką również `isSlotAvailable` (przekładanie wizyt), czy tylko `ensureNoConflicts`?