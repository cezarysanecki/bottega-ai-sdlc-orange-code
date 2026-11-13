# Wymaganie W-01: kod procedury z białymi znakami

Status: zatwierdzone w próbie przez prowadzącego (SYMULACJA decyzji uczestnika/PO, nie decyzja człowieka).

Kontekst: `BillingRulesEngine.calculateCharge` waliduje `procedureCode.trim()`, ale dalsze kroki
(cennik, rodzina procedury, podatek, repozytorium) używają kodu w oryginalnej postaci.

Wymaganie: kod z białymi znakami na początku lub końcu (np. `" LAB-CBC "`) jest wyceniany
tak samo jak ten sam kod bez tych znaków, przy tym samym `PricingContext`.

Kryteria akceptacji:
- K1: `calculateCharge(" LAB-CBC ", ctx)` == `calculateCharge("LAB-CBC", ctx)`.
- K2: `null`, `""`, `"   "` nadal zwracają `BigDecimal.ZERO`.
- K3: wielkość liter i znaki wewnątrz kodu bez zmian (to poza zakresem).
- K4: wszystkie dotychczasowe testy backendu przechodzą.

Poza zakresem: normalizacja w `InvoiceService`, w bazie i w API.
