# Clinic platform: wizyty, encountery, faktury, płatności

Aplikacja szkoleniowa przychodni: pacjent rezerwuje wizytę u lekarza (stacjonarnie lub telehealth), po wizycie powstaje encounter z procedurami, z niego faktura, do faktury księguje się płatności.

## Mapa
- `backend/` – Spring Boot 3.2.5, Java (target 22), Maven wrapper, PostgreSQL + Flyway (`src/main/resources/db/migration`).
  - Domena: `com.clinictraining.{appointment,patient,billing,legacy,integration}`.
  - API: REST w `medbackend/web/*Controller.java` (wymaga JWT poza `/api/auth/token`), GraphQL w `medbackend/graphql/ClinicQueryResolver.java` + `src/main/resources/graphql/schema.graphqls` (bez JWT), klienci SOAP/HTTP w `integration/`.
  - Wycena procedur: `legacy/BillingRulesEngine.calculateCharge`, wołana z `billing/InvoiceService.generateInvoice`.
- `frontend/med-frontend/` – React 18, TypeScript, Vite 5, Ant Design, React Query, Vitest. Funkcje w `src/features/<obszar>/`.
- `ops/` – mocki usług zewnętrznych, `docs/` – architektura, konwencje, runbook.

## Polecenia (sprawdzone 2026-11-13)
Backend, katalog `backend/`, JDK 22–26 (sprawdzone 25 i 26):
- `./mvnw -B test` – testy jednostkowe (19). Nie uruchamia klas `*IT`.
- `./mvnw -B -Dtest=BillingRulesEngineTest test` – jedna klasa testów.
- `./mvnw -B -Dtest=PaymentServiceIT test` – testy integracyjne, wymagają działającego Dockera/Podmana (Testcontainers).
- `./mvnw -B spring-boot:run -Dspring-boot.run.profiles=demo -DskipTests` – backend z wbudowanym PostgreSQL na 8080; inny port: dodaj `-Dspring-boot.run.arguments=--server.port=18180`.

Frontend, katalog `frontend/med-frontend/`, Node 18+:
- `npm ci --no-audit --no-fund`, `npm run build`, `npm run lint` (obecnie 0 błędów, 13 ostrzeżeń).
- `npm test -- --run` – Vitest jednorazowo. Na Node 25+ dodaj `NODE_OPTIONS=--no-experimental-webstorage` (kolizja globalnego localStorage z jsdom).

## Konwencje
- Java: 4 spacje, bez preview API. Testy jednostkowe JUnit 5 + Mockito, nazwy `should...`.
- Poprawka błędu = najpierw test regresji, który zawodzi, potem zmiana.
- Zmiana pola w GraphQL wymaga zmiany `schema.graphqls`, resolvera i zapytań frontendu w `src/features/*/hooks`.
- Frontend ma zaszyty adres `http://localhost:8080` (`src/shared/api/fetchClient.ts`, `src/shared/api/graphql/client.ts`); proxy Vite go nie zmienia.
- Nie formatuj masowo kodu legacy (`legacy/`, `tools/legacy/`). Zmieniaj tylko to, czego dotyczy zadanie.
