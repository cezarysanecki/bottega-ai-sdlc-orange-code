Typ GraphQL `Appointment` ma nowe pole `appointmentTypeCode: String` (nullable). Zwraca kod typu wizyty, np. `CHECKUP` czy `TEL_VISIT`, a nie jego opis. Testy backendu przechodzą, frontend się buduje, a na backendzie demo pole zwraca poprawne wartości.

## Zmienione pliki
- `backend/src/main/resources/graphql/schema.graphqls`: dopisane jedno pole w typie `Appointment`. Żadne istniejące pole się nie zmieniło.
- `backend/src/main/java/com/clinictraining/medbackend/api/dto/AppointmentResponse.java`: nowe pole z getterem i setterem. Resolvera nie zmieniałem, bo GraphQL odczytuje pole przez getter.
- `backend/src/main/java/com/clinictraining/medbackend/api/ApiMapper.java`: przepisuje kod typu z encji. Gdy wizyta nie ma typu, pole jest `null`.
- `backend/src/test/java/com/clinictraining/medbackend/api/ApiMapperTest.java` (nowy): dwa testy, `shouldMapAppointmentTypeCode` i `shouldMapNullAppointmentTypeCodeWhenTypeMissing`.
- `frontend/med-frontend/src/features/appointments/hooks/useAppointments.ts`: w interfejsie `Appointment` dodane opcjonalne pole `appointmentTypeCode?: string`.

## Polecenia i wyniki
| Polecenie | Wynik |
|---|---|
| `./mvnw -B -Dtest=ApiMapperTest test` przed zmianą | nie skompilował się, brak `getAppointmentTypeCode()` (test najpierw zawodzi) |
| `./mvnw -B test` | BUILD SUCCESS, 108 testów, 0 błędów |
| hook checkstyle na edytowanych plikach Java | brak naruszeń |
| `npm run build` | sukces, jest tylko istniejące wcześniej ostrzeżenie o rozmiarze paczki |
| `./mvnw -B spring-boot:run -Dspring-boot.run.profiles=demo -DskipTests -Dspring-boot.run.arguments=--server.port=18182` | uruchomiony, po sprawdzeniu zatrzymany |

Zapytanie GraphQL z nowym polem: dwie wizyty, jedna z typem `TEL_VISIT`, druga bez podanego typu:
```json
{"data":{"a":{"id":"3","status":"SCHEDULED","appointmentTypeCode":"TEL_VISIT"},"b":{"id":"4","appointmentTypeCode":"CHECKUP"}}}
```
Stary zestaw pól, którego dziś używa frontend, działa jak wcześniej:
```json
{"data":{"createAppointment":{"id":"5","appointmentUid":"ee3ec948-...","status":"SCHEDULED","startTime":"2026-12-03T10:00Z","endTime":"2026-12-03T10:30Z","roomCode":null}}}
```
REST `GET /api/appointments` z tokenem JWT odpowiedział HTTP 200 i zwrócił `appointmentTypeCode` (`CHECKUP`, `CHECKUP`, `TEL_VISIT`). REST i GraphQL używają tego samego DTO, więc REST też dostał to pole.

## Otwarte kwestie
- **W GraphQL nie ma zapytania o listę wizyt.** Typ `Appointment` zwraca tylko mutacja `createAppointment`. `appointmentDigest` to lista tekstów, nie obiektów wizyt. Teraz pole widać więc w mutacji i w REST. Jeśli frontend ma pobierać wizyty przez GraphQL, trzeba dodać nowe zapytanie, ale to osobne zadanie.
- **Nie dodałem pola do mutacji we frontendzie** (`useBookAppointment.ts`). Interfejs użytkownika jeszcze z niego nie korzysta, a standard zespołu zabrania dodawać do zapytań pola, których UI nie używa. Wystarczy je dopisać, gdy UI zacznie go potrzebować.
- Nic nie jest zacommitowane.

<!-- results: [(32, 0.5565975999999998, 159450), (1, 0.5821287999999998, 2440)] -->
