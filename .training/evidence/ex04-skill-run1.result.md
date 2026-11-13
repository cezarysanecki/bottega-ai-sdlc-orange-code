Nie zapisałem `.pipeline/analysis.md`, bo nie dostałem zgody na zapis do tego pliku. Analiza jest gotowa. Gdy zezwolisz na zapis do `.pipeline/`, zapiszę plik bez zmian albo wkleję jego treść tutaj.

F/H/Q: 5 / 3 / 3

Najważniejszy jest F-01 (P2). W `BillingRulesEngine.java:286` i `:289` wywołania `context.requiresSedation()` i `context.requiresAssistant()` zakładają, że pola nie są puste. `InvoiceService` tych pól nie ustawia, więc `generateInvoice` dla każdej procedury z kodem zaczynającym się od `PROC` rzuca `NullPointerException` i faktura nie powstaje. Dziś zdarzy się to tylko wtedy, gdy taki kod zostanie dodany do tabeli `procedures` poza aplikacją, bo dane startowe i API go nie tworzą.