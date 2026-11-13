Typ GraphQL `Appointment` ma teraz pole `appointmentType: String` z kodem typu wizyty (np. `CHECKUP`, `TEL_VISIT`). Działa w mutacji `createAppointment`, co sprawdziłem na żywym backendzie. Zmiany nie wprowadziłem do commita.

**Dwie rzeczy, o których warto wiedzieć:**
- **W GraphQL nie ma zapytania, które zwraca wizyty.** Typ `Appointment` zwraca tylko `createAppointment`, a `appointmentDigest` zwraca listę napisów. Lista i szczegóły wizyt w frontendzie idą przez REST (`/api/appointments`).
- **REST też dostał to pole.** REST i GraphQL używają tej samej klasy odpowiedzi, więc REST zaczął zwracać `appointmentType`. Strona szczegółów wizyty (`AppointmentDetailPage.tsx:83`) już czytała to pole, ale backend go nie wysyłał, więc zawsze pokazywała „-". Teraz pokaże prawdziwy typ. Jeśli wolisz zmianę tylko w GraphQL, mogę ją tak zawęzić.

**Zgodność ze starszymi klientami:** pole jest tylko dodane i może być puste (`null`), gdy wizyta nie ma typu. Mutacja z dotychczasowym zestawem pól, bez `appointmentType`, odpowiada tak jak wcześniej.

## Zmienione pliki
Backend:
- `src/main/resources/graphql/schema.graphqls`: nowe pole `appointmentType: String` w `Appointment`.
- `medbackend/api/dto/AppointmentResponse.java`: pole `appointmentType` z getterem i setterem.
- `medbackend/api/ApiMapper.java`: wpisuje kod typu wizyty albo `null`, gdy typu brak. Resolvera GraphQL nie trzeba było zmieniać, bo już zwraca tę klasę odpowiedzi.
- Nowy test `medbackend/api/ApiMapperTest.java`: `shouldMapAppointmentTypeCode`, `shouldLeaveAppointmentTypeNullWhenMissing`.
- Nowy test `medbackend/graphql/GraphQlSchemaTest.java`: sprawdza, że w `schema.graphqls` jest `Appointment.appointmentType` typu `String`, który może być pusty.

Frontend:
- `hooks/useBookAppointment.ts`: mutacja pobiera `appointmentType`, typ odpowiedzi ma nowe pole.
- `hooks/useAppointments.ts`: `appointmentType?: string` w interfejsie `Appointment`.

## Uruchomione polecenia i wyniki
| Polecenie | Wynik |
|---|---|
| `./mvnw -B -Dtest='ApiMapperTest,GraphQlSchemaTest' test` (przed zmianą) | ❌ błąd kompilacji, brak `getAppointmentType()`, czyli test regresji zawodzi zgodnie z oczekiwaniem |
| `./mvnw -B test` (po zmianie) | ✅ 109 testów, 0 błędów, BUILD SUCCESS |
| `./mvnw -B checkstyle:check` | tylko 2 znane naruszenia legacy (`ControllerLoggingAspect`, `InvoiceServiceTest`), żadnych nowych |
| `npm run build` | ✅ zbudowany |
| `npm run lint` | ✅ 0 błędów, 13 ostrzeżeń, tak jak przed zmianą |
| `npm test -- --run` | ⚠️ **nie uruchomiony**, zabrakło zgody na polecenie |
| Backend demo na porcie 18181 + `curl` do `/graphql` | ✅ `createAppointment` z `TEL_VISIT` zwraca `"appointmentType":"TEL_VISIT"`; bez podanego typu zwraca `"CHECKUP"` (domyślny); mutacja ze starym zestawem pól działa bez zmian |
| `curl` do `GET /api/appointments` z tokenem JWT | ✅ każda wizyta ma `appointmentType` |

## Do zrobienia przez Ciebie
- Uruchom testy frontendu: `NODE_OPTIONS=--no-experimental-webstorage npm test -- --run` w `frontend/med-frontend/`.
- Zatrzymaj backend demo na porcie 18181, bo nadal działa; zabrakło zgody na `pkill`. Na przykład: `pkill -f "server.port=18181"`.
- Podczas sprawdzania utworzyłem w demo trzy wizyty testowe (id 3–5). Baza jest wbudowana w backend demo, więc znikną po jego zatrzymaniu.

<!-- results: [(48, 0.8420177999999998, 203816), (1, 0.8748891999999998, 3854)] -->
