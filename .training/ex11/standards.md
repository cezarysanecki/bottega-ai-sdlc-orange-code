# Standardy zespołu: kontrakt API i testy (backend + frontend)

## Kontrakt GraphQL i REST
- `backend/src/main/resources/graphql/schema.graphqls` jest kontraktem. Zmiany tylko addytywne: nie zmieniaj nazw, typów ani nullowalności istniejących pól.
- Nowe pole w typie wyjściowym jest nullable (bez `!`), chyba że wymaganie mówi inaczej.
- REST i GraphQL zwracają te same DTO z `medbackend/api/dto`, wypełniane w `medbackend/api/ApiMapper`. Nowe pole dodajesz raz: DTO + mapper; GraphQL odczytuje je przez nazwę gettera.
- Kody słownikowe (typ wizyty, status) zwracamy jako kod (`CHECKUP`), nie opis.

## Testy
- Każda zmiana mapowania ma test jednostkowy `ApiMapper` (JUnit 5, bez Springa), także dla wartości `null` w encji.
- Zmiana schematu: uruchom backend demo i wykonaj zapytanie GraphQL z nowym polem; wynik wklej do raportu.
- `./mvnw -B test` w `backend/` musi przejść.

## Frontend
- Jeśli frontend ma typ TS dla tego obiektu (`src/features/*/hooks/*.ts`), dodaj nowe pole jako opcjonalne. Nie dodawaj pola do zapytań, których UI go nie używa.
- `npm run build` w `frontend/med-frontend/` musi przejść.

## Raport końcowy
- Zmienione pliki, uruchomione polecenia i ich wyniki, otwarte kwestie.
