**1. Aplikacja**
To platforma dla przychodni do umawiania wizyt pacjentów u lekarzy (providers) w gabinetach lub zdalnie (telehealth). Po wizycie powstaje encounter z procedurami, z niego faktura, a do faktury księguje się płatności.

**2. Procesy i role**
- Rejestracja pacjenta i sprawdzanie jego ubezpieczenia.
- Wizyty: rezerwacja, sprawdzenie wolnego terminu, przełożenie, anulowanie, zmiana statusu (np. potwierdzona, zakończona, „no-show”).
- Encounter po wizycie, następnie faktura i płatność. W tle działa uzgadnianie płatności (`PaymentReconciliationJob`).
- Sesje telehealth synchronizowane z zewnętrznym dostawcą przez SOAP.
- Przypomnienia o wizytach (`ReminderJob`, tabela `reminder_log`).
- Audyt: tabele `audit_events` i `api_calls`.

Role: w kodzie jest tylko jedna, `ROLE_USER`, nadawana każdemu z poprawnym JWT (`JwtAuthenticationFilter.java:41`). Nie ma podziału na recepcję, lekarza czy księgowość.

**3. Technologie potwierdzone w plikach**
- Backend (`pom.xml`): Java 22, Spring Boot 3.2.5 (Web, Security, GraphQL, Data JPA, Actuator, WebFlux, AOP, Cache), Flyway, PostgreSQL i embedded-postgres, Caffeine, MapStruct, Spring-WS (SOAP), jjwt, Groovy, Testcontainers.
- Frontend (`package.json`): React 18, TypeScript, Vite, Ant Design, React Query, Redux Toolkit, React Router 7, axios, graphql, jQuery z FullCalendar 3, moment/dayjs, Vitest, MSW.

**4. Proces: rezerwacja wizyty**
1. Ekran `/appointments/new` to `BookAppointmentPage` (`frontend/med-frontend/src/app/routes.tsx:51`). Formularz wywołuje `handleSubmit`, który domyślnie ustawia typ wizyty na `CHECKUP` (`BookAppointmentPage.tsx:21-33`).
2. Hook wysyła mutację GraphQL `createAppointment` (`hooks/useBookAppointment.ts:5-16`, `:43`) na `http://localhost:8080/graphql` z nagłówkiem Bearer (`shared/api/graphql/client.ts:1`, `:29`). Rezerwacja nie idzie przez REST.
3. Endpoint `/graphql` jest dostępny bez logowania (`SecurityConfig.java:22`).
4. Resolver sprawdza, czy koniec jest po początku (`ClinicQueryResolver.java:128-133`), i wywołuje serwis (`:134`).
5. `AppointmentService.bookAppointment` (`AppointmentService.java:108`) pobiera pacjenta, lekarza, typ wizyty (przy nieznanym typie wraca do `CHECKUP`) i opcjonalny gabinet (`:115-123`).
6. Kolizje terminów są sprawdzane tylko dla lekarza, nie dla gabinetu (`:147-157`). Zapytanie: `AppointmentRepository.java:21`.
7. Serwis ustawia status `SCHEDULED` i zapisuje wizytę (`AppointmentService.java:135-138`). Encja przed zapisem dostaje UID i kod `APPT-xxxxxxxx` (`Appointment.java:164-175`).
8. Dane trafiają do tabeli `appointments` (`Appointment.java:23`, `V2__core_domain.sql:55`). Wywołanie `save` przechwytuje aspekt audytu i zapisuje zdarzenie do `audit_events` (`AuditRepositoryAspect.java:22`, `:40`).

Ten sam serwis obsługuje też endpoint REST `POST /api/appointments` (`AppointmentController.java:51-62`), ale tamten wymaga JWT.

**5. Niepotwierdzone / niewiadome**
- Nie sprawdziłem, czy tabela `appointment_status_history` (`V8`) jest gdziekolwiek zapisywana. W serwisie zmiany statusu trafiają tylko do pola tekstowego `legacy_notes`.
- Nie wiem, czy brak uwierzytelniania na `/graphql` jest celowy. Wygląda na lukę.
- Normalizacja strefy czasowej przy sprawdzaniu kolizji (`AppointmentService.java:150`) gubi oryginalną strefę. Przy różnych strefach może to dawać błędne wyniki, ale tego nie testowałem.
- Nie zweryfikowałem, czy działa endpoint `/api/auth/token` ani skąd brać dane logowania.
- Nie przeczytałem `docs/*.md`, plików `pom-old.xml` i `legacy-context.xml` ani panelu `/legacy`.
- Nie potwierdziłem, z jakimi systemami zewnętrznymi łączą się klienci SOAP, ubezpieczeniowy i PDF ani czy w repo są ich mocki.