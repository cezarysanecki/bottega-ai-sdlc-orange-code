# Analiza: AppointmentService#bookAppointment,ensureNoConflicts
- Cel: błędy przy rezerwacji wizyty i wykrywaniu kolizji terminów
- Zakres: backend/src/main/java/com/clinictraining/appointment/AppointmentService.java (`bookAppointment` l. 107–145, `ensureNoConflicts` l. 147–158); commit: ca55f0e; drzewo: zmienione (tylko `.training/`, `.claude/`, `.pipeline/` — kod backendu bez zmian)

Wywołujący: REST `POST /api/appointments` (`medbackend/web/AppointmentController.java:54`, JWT), GraphQL `createAppointment` (`medbackend/graphql/ClinicQueryResolver.java:134`, bez JWT; używany przez frontend `features/appointments/hooks/useBookAppointment.ts`). Brak testów `AppointmentService` w `backend/src/test`.

## Findings
### F-01 Sprawdzenie kolizji gubi offset żądania i szuka w przesuniętym oknie
- Lokalizacja: backend/src/main/java/com/clinictraining/appointment/AppointmentService.java:150-151
- Dowód: `start.toLocalDateTime().atZone(zone)` odrzuca offset wejścia i przykleja strefę `ClinicClock` (`Clock.systemDefaultZone()`, `shared/ClinicClock.java:17`), a zapis używa oryginalnego `start`/`end` (l. 134). Frontend wysyła `toISOString()` (UTC, `Z`) — `BookAppointmentPage.tsx:30-31`. Wejście: JVM w `Europe/Warsaw` (listopad, +01:00), istnieje wizyta lekarza `10:00Z–10:30Z`; rezerwacja tego samego lekarza `10:00Z–10:30Z` → zapytanie szuka w `10:00+01:00–10:30+01:00` = `09:00Z–09:30Z` → brak kolizji → druga wizyta zapisana (double booking). Odwrotnie: rezerwacja `11:00Z–11:30Z` (wolny termin) → okno `10:00Z–10:30Z` → fałszywa kolizja i odrzucenie.
- Oczekiwane zachowanie: porównanie tych samych chwil, które są zapisywane (kolumny `TIMESTAMP WITH TIME ZONE`, `V2__core_domain.sql:62-63`) — źródło: spójność z zapisem w l. 134 i z zapytaniem `findOverlappingAppointments`, które porównuje chwile.
- Skutek: dwie wizyty u jednego lekarza w tym samym czasie albo odrzucenie wolnego terminu; przesunięcie = różnica między offsetem klienta a strefą serwera.
- Osiągalność: GraphQL `createAppointment` z frontendu i REST `POST /api/appointments`; ten sam wzorzec w `isSlotAvailable` (l. 100-101) → `GET /api/appointments/check-availability` i `rescheduleAppointment`.
- Priorytet: P1 (każda rezerwacja z frontendu, gdy strefa JVM ≠ UTC; przy JVM w UTC i wejściu w `Z` błąd nie występuje)
- Weryfikacja: `shouldDetectConflictWhenRequestOffsetDiffersFromClinicZone` — `ClinicClock(Clock.fixed(..., ZoneId.of("Europe/Warsaw")))`, mock repozytorium; wywołanie `bookAppointment(..., 2026-11-20T10:00Z, 2026-11-20T10:30Z, null)`; oczekiwanie: `findOverlappingAppointments` wywołane z argumentami równymi (`isEqual`) `10:00Z` i `10:30Z` (dziś dostaje `09:00Z`/`09:30Z`). Wariant IT (Testcontainers): zapisz wizytę 10:00Z, druga rezerwacja tego samego slotu → oczekiwany `IllegalStateException`.

### F-02 Odwołane wizyty nadal blokują termin
- Lokalizacja: backend/src/main/java/com/clinictraining/appointment/repository/AppointmentRepository.java:21 (wołane z AppointmentService.java:153)
- Dowód: zapytanie filtruje tylko po `provider.id` i nakładaniu się czasu, bez `status`. Wejście: wizyta `10:00–10:30` → `PATCH /api/appointments/{id}/cancel` (status `CANCELLED`, `AppointmentService.java:176`) → ponowna rezerwacja `10:00–10:30` u tego lekarza → `IllegalStateException("Conflicting appointment detected")`.
- Oczekiwane zachowanie: nieustalone → Q-01 (sąsiedni kod traktuje tylko `SCHEDULED` jako aktywne, np. `listUpcomingAppointments` l. 62, ale brak wymagania).
- Skutek: termin po odwołaniu jest trwale niedostępny; `check-availability` też zwraca `available: false`.
- Osiągalność: cancel przez REST, rezerwacja przez GraphQL/REST.
- Priorytet: P1 (jeśli Q-01 potwierdzi, że odwołanie zwalnia termin)
- Weryfikacja: IT `shouldAllowBookingSlotOfCancelledAppointment` — zarezerwuj, odwołaj, zarezerwuj ten sam slot; oczekiwanie: druga rezerwacja zapisana.

### F-03 Brak walidacji przedziału czasu w REST; odwrócony przedział omija detekcję kolizji
- Lokalizacja: backend/src/main/java/com/clinictraining/appointment/AppointmentService.java:125,134 (walidacja tylko w `ClinicQueryResolver.java:131`)
- Dowód: `bookAppointment` nie sprawdza `start < end`. Przez REST: `start=11:00`, `end=10:00` przy istniejącej wizycie `10:00–11:00` → warunek `a.start < :end AND a.end > :start` = `10:00 < 10:00` → fałsz → brak kolizji → zapis slotu z `end < start`. GraphQL odrzuca `end < start`, ale przepuszcza `end == start` (slot zerowej długości). `start == null` lub `end == null` (pola nieobowiązkowe w DTO) → `NullPointerException` w l. 150 → HTTP 500 (brak `@ExceptionHandler` w `medbackend`).
- Oczekiwane zachowanie: odrzucenie `end <= start` i braku czasu jako błąd wejścia — źródło: spójność z walidacją w `ClinicQueryResolver.java:131-133`; czy `end == start` jest dopuszczalne: nieustalone → Q-02.
- Skutek: w bazie wizyty z ujemnym czasem trwania, nakładające się na inne; brak komunikatu dla klienta.
- Osiągalność: REST `POST /api/appointments` (z JWT); `end == start` także przez GraphQL.
- Priorytet: P2 (wymaga ręcznie spreparowanego żądania; frontend RangePicker wymusza kolejność)
- Weryfikacja: `shouldRejectBookingWhenEndBeforeStart` — `bookAppointment(..., 11:00Z, 10:00Z, null)`; oczekiwanie: `IllegalArgumentException`, `save` nie wywołane.

### F-04 Nieznany typ wizyty i nieznany gabinet są po cichu zastępowane
- Lokalizacja: backend/src/main/java/com/clinictraining/appointment/AppointmentService.java:118-123
- Dowód: `appointmentTypeCode="TYPO"` → `orElseGet(... "CHECKUP")` → wizyta zapisana jako CHECKUP. `roomCode="R-999"` (nieistniejący) → `orElse(null)` → wizyta bez gabinetu. `rescheduleAppointment` dla tego samego przypadku rzuca `IllegalArgumentException("Room not found")` (l. 238-239).
- Oczekiwane zachowanie: nieustalone → Q-03 (komentarz l. 123 sugeruje celowość dla telehealth, ale niespójne z `rescheduleAppointment`).
- Skutek: błędny typ wizyty (wpływa na dalszy encounter/fakturę) lub brak gabinetu bez informacji dla użytkownika.
- Osiągalność: GraphQL/REST z dowolnym kodem typu/gabinetu.
- Priorytet: P2
- Weryfikacja: `shouldRejectUnknownAppointmentType` — mock `findByTypeCode("TYPO")` → empty; oczekiwanie (po Q-03): wyjątek zamiast zapisu z CHECKUP.

## Hipotezy
- H-01 Wyścig check-then-insert: dwie równoległe rezerwacje tego samego slotu przejdą `ensureNoConflicts` i obie się zapiszą (domyślna izolacja READ COMMITTED, brak blokady i brak constraintu `EXCLUDE` w migracjach). Brakuje: testu współbieżnego na PostgreSQL potwierdzającego oba zapisy.
- H-02 Kolizja zgłaszana jako `IllegalStateException` bez handlera kończy się HTTP 500 zamiast 409 (REST) i ogólnym błędem w GraphQL. Brakuje: uruchomienia endpointu i sprawdzenia statusu/treści odpowiedzi.

## Pytania
- Q-01 Czy wizyty `CANCELLED` (i ewentualnie `NO_SHOW`) mają zwalniać termin lekarza? Które statusy liczą się jako kolizja?
- Q-02 Czy wizyta z `end == start` jest dopuszczalna? Czy jest minimalna/maksymalna długość wizyty?
- Q-03 Czy nieznany `appointmentType`/`roomCode` ma być błędem (jak w `rescheduleAppointment`), czy fallback do CHECKUP / brak gabinetu jest zamierzony?
- Q-04 Czy kolizje mają obejmować też gabinet (dwóch lekarzy w jednym `roomCode`) i pacjenta (dwie wizyty pacjenta naraz)? Dziś sprawdzany jest tylko lekarz.

## Poza zakresem
- `rescheduleAppointment` (l. 230): `isSlotAvailable` nie wyklucza samej przesuwanej wizyty → przesunięcie o np. 15 min nakładające się na własny stary termin jest odrzucane.
- `isSlotAvailable` (l. 100-101) ma ten sam błąd strefy co F-01.
- `Appointment.onCreate` używa `ZonedDateTime.now()` zamiast `ClinicClock`.
