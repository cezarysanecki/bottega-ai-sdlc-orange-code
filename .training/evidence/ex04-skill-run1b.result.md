`.pipeline/analysis.md`
5 F / 2 H / 3 Q
Najważniejszy jest F-01 (P1): `InvoiceService` nie ustawia w kontekście wyceny pól `sedation` ani `assistant`, więc dla każdego kodu `PROC*` bez `overrideCost` linia `BillingRulesEngine.java:286` rzuca `NullPointerException` i faktura nie powstaje. Istniejące testy tego nie łapią, bo w `setUp` ustawiają te flagi na `false`.