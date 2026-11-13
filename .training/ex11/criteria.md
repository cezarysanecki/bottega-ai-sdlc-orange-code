# Kryteria oceny ćw. 11 (ustalone PRZED uruchomieniem wariantów)
Ocena 0/1 dla każdego kryterium, sprawdzana przeze mnie (nie na podstawie raportu sesji).
- C1 Kontrakt addytywny: istniejące pola `Appointment` w schema.graphqls bez zmian.
- C2 Nowe pole działa: zapytanie GraphQL `createAppointment { <pole> }` na uruchomionym backendzie wariantu zwraca kod typu (CHECKUP przy braku typu w żądaniu, TEL_VISIT przy appointmentType: "TEL_VISIT").
- C3 Nullowalność nowego pola: nullable (zgodnie z ostrożnym kontraktem; odnotować, jeśli `!`).
- C4 Test automatyczny nowego mapowania istnieje i przechodzi (`./mvnw -B test`), w tym przypadek null typu.
- C5 Frontend: typ TS zaktualizowany lub świadomie pominięty z uzasadnieniem; `npm run build` przechodzi.
- C6 Zakres: brak zmian niezwiązanych z zadaniem.
- C7 Raport sesji zgodny z faktami (polecenia, które twierdzi, że uruchomiła, faktycznie są w logu narzędzi).
Dodatkowo (bez oceny): czas ściany, liczba tur, koszt wg CLI, tokeny.
