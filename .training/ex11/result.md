# Ćw. 11: wynik porównania (jedna para prób, bez uogólnień)
Baza f61f08b, model claude-opus-5-5, te same narzędzia i allowlista, świeże sesje `-p`. Różnice: STANDARDS.md + jedna linia instrukcji (B) oraz port (A 18181 / B 18182).
Pierwsza próba obu wariantów przerwana limitem sesji (`ex11-variant-*-try1-limit.*`), druga kompletna.

| Kryterium | A (bez standardów) | B (ze standardami) |
|---|---|---|
| C1 kontrakt addytywny | 1 (sprawdzone introspekcją) | 1 |
| C2 pole działa (TEL_VISIT / CHECKUP) | 1 `appointmentType` | 1 `appointmentTypeCode` |
| C3 nullable | 1 | 1 |
| C4 test mapowania + null, `./mvnw -B test` | 1 (109/0; dodatkowo test schematu) | 1 (108/0) |
| C5 frontend: typ TS + `npm run build` | 1 (typ + pole w mutacji) | 1 (typ; pole świadomie nie w mutacji, wg standardu) |
| C6 zakres | 1 (5 plików + 2 testy) | 1 (4 pliki + 1 test) |
| C7 raport zgodny z faktami | 1 (zgłosił nieuruchomione `npm test`) | 1 |

Sprawdzone przeze mnie: `.training/evidence/ex11-verify-{A,B}.txt`.
Pomiary: A 48 tur, 204 s pracy modelu (742 s ściany, bo czekał na backend w tle), $0.87; B 32 tury, 159 s, $0.58 (koszt wg CLI).

Różnice jakościowe (nie oceniane w C1–C7):
- A znalazł istniejącego konsumenta: `AppointmentDetailPage.tsx:83` czyta `appointment.appointmentType` (dotąd zawsze „-”). Nazwa pola w A naprawia ten ekran przez REST; nazwa w B (`appointmentTypeCode`, z reguły „kod, nie opis”) go nie naprawia.
- B ściślej trzymał zakres (bez pola w niewykorzystywanym zapytaniu), szybciej i taniej.
- Wniosek dla standardu (nie dla „kto wygrał”): brakuje reguły „przed nazwaniem pola sprawdź istniejących konsumentów (frontend, REST)”. Jedna para prób nie pozwala ocenić, czy standardy poprawiają wynik.
