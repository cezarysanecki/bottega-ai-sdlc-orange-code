## Kryteria
- K1: spełnione. `BillingRulesEngine.java:111` przycina kod przed wszystkimi dalszymi krokami (cennik, repozytorium, rodzina, rabaty, dopłaty, podatek, skrypt legacy). Sprawdza to test `BillingRulesEngineTest.java:158-165`. Ma stałą datę 2026-11-11 10:00Z: to środa w godzinach pracy, nie święto, więc wynik nie zależy od zegara.
- K2: spełnione w kodzie, bez testu. Strażnik w `BillingRulesEngine.java:107` się nie zmienił i nadal zwraca `ZERO` dla `null`, `""` i `"   "`. Żaden test nie sprawdza tych trzech wejść.
- K3: spełnione. `trim()` (`:111`) usuwa tylko znaki ≤ U+0020 na początku i końcu kodu. Wielkość liter i znaki w środku kodu zostają bez zmian.
- K4: nieustalone, bo nie uruchamiałem testów. Ze statycznej analizy żaden istniejący test nie podaje kodu ze spacjami. `InvoiceServiceTest` mockuje silnik (`InvoiceServiceTest.java:106,144-146`), więc ta zmiana na niego nie wpływa.

## Błędy (pewne)
Brak błędów.

Nowy test zawodzi bez poprawki. Wyliczenie: kod `" LAB-CBC "` nie jest w cenniku, a mock repozytorium zwraca `Optional.empty()`. Warunek `startsWith("LAB")` jest fałszywy przez wiodącą spację, więc baza wynosi 50.00 przy rodzinie UNKNOWN i podatku 5%. Daje to 52.50 + 12.34 = 64.84. Dla przyciętego kodu wychodzi 35.00 × 1.0725, czyli 37.54 + 12.34 = 49.88. Wyniki się różnią, więc test pilnuje poprawki.

## Hipotezy (niepotwierdzone)
- RH-01 `BillingRulesEngine.java:111`: `trim()` nie usuwa białych znaków Unicode, np. NBSP (U+00A0). Kod `"\u00A0LAB-CBC"` nadal dostanie cenę 50.00 + 5%. Wymaganie mówi ogólnie o „białych znakach”, a jedyny przykład to zwykła spacja. Żeby rozstrzygnąć, potrzebna jest decyzja PO, czy NBSP i pokrewne znaki są w zakresie. Jeśli tak, wystarczy `strip()` i test z `\u00A0`.
- RH-02 Brak testów regresji dla K2. Kryterium nie jest dziś złamane, ale przyszła zmiana strażnika przeszłaby niezauważona. Wystarczy sparametryzowany test dla `null`, `""` i `"   "` z oczekiwanym `BigDecimal.ZERO`.

## Uwagi poza zakresem
- Ponowne przypisanie parametru (`procedureCode = procedureCode.trim()`) działa poprawnie. Czytelniej byłoby użyć lokalnej zmiennej `code`, ale to kosmetyka w kodzie legacy.
- Linia 107 nadal wywołuje `trim()` osobno, więc kod jest przycinany dwa razy. Nie ma to wpływu na wynik.

Pliki:
- /Users/cezarysanecki/orca/workspaces/training-ai-sdlc-orange-demo-app/orange-proba-13-cwiczen-cc/backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java
- /Users/cezarysanecki/orca/workspaces/training-ai-sdlc-orange-demo-app/orange-proba-13-cwiczen-cc/backend/src/test/java/com/clinictraining/legacy/BillingRulesEngineTest.java

---

Tak, dwie rzeczy trzeba sprawdzić uruchomieniem testów: K4 (`./mvnw -B test` z wynikiem 19/19) oraz to, że nowy test zawodzi bez poprawki. Reviewer wyliczył to ręcznie i niczego nie uruchamiał.