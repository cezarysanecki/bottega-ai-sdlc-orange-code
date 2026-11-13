# Analiza: BillingRulesEngine#applyAdjustments,applyDiscounts
- Cel: błędy zachowania przy wycenie procedur wywoływanej z InvoiceService
- Zakres: backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java (`applyAdjustments` l.166–312, `applyDiscounts` l.314–361); kontekst wywołania: `billing/InvoiceService.java` (`generateInvoice`, `buildPricingContext`); commit: ca55f0e; drzewo: zmienione (tylko pliki `.training/`, `.claude/`, kod bez zmian)

## Findings
### F-01 NPE dla rodziny PROCEDURE, gdy `sedation`/`assistant` nie są ustawione
- Lokalizacja: backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:286 (oraz :289)
- Dowód: `if (context.requiresSedation())` rozpakowuje `Boolean sedation`. `InvoiceService.buildPricingContext` (InvoiceService.java:93–112) ustawia tylko `serviceDateTime`, `patientAge`, `state`, `selfPay`, `contractedInsurance`, więc `sedation == null`. Wejście: `calculateCharge("PROC-SUTURE", kontekst z buildPricingContext)` → `NullPointerException` (inne kody zaczynające się od `PROC` trafiają tu przez `deriveFamilyFromCode`, l.501).
- Skutek: `generateInvoice` dla encountera z dowolną procedurą `PROC*` bez `overrideCost` kończy się wyjątkiem; faktura nie powstaje.
- Osiągalność: InvoiceService.java:72 → `calculateCharge` → `applyAdjustments`. Seed (V2__core_domain.sql:147) nie zawiera kodów `PROC*`, ale silnik ma je w `LEGACY_PRICING_TABLE` (PROC-SUTURE/INJECTION/BIOPSY), więc wystarczy taka procedura w tabeli `procedures`. Istniejący `BillingRulesEngineTest` ustawia `setSedation(false)`/`setAssistant(false)` w `setUp` i maskuje błąd.
- Priorytet: P1
- Weryfikacja: `BillingRulesEngineTest.shouldPriceProcedureWhenSedationAndAssistantUnset` — nowy `PricingContext` wypełniony jak w `buildPricingContext` (serviceDateTime, patientAge=40, state="CA", selfPay=false, contractedInsurance=true), kod `"PROC-SUTURE"`; oczekiwanie: brak wyjątku i wynik > 0. Dziś: NPE.

### F-02 NPE w `applyDiscounts`, gdy `contractedInsurance` nie jest ustawione
- Lokalizacja: backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:338
- Dowód: `if (context.hasContractedInsurance())` rozpakowuje `Boolean`. Wejście: `new PricingContext()` z samym `serviceDateTime`, kod `"LAB-CBC"` → NPE (pozostałe flagi w tej metodzie używają `Boolean.TRUE.equals`).
- Skutek: awaria wyceny dla każdego kodu przy niepełnym kontekście.
- Osiągalność: tylko bezpośrednie wywołanie — InvoiceService zawsze ustawia `contractedInsurance` (InvoiceService.java:109).
- Priorytet: P3
- Weryfikacja: `shouldApplyDiscountsWhenContractedInsuranceUnset` — kontekst tylko z `serviceDateTime`, kod `"LAB-CBC"`; oczekiwanie: brak wyjątku.

### F-03 Martwa gałąź rabatu za panel ≥ 10 badań
- Lokalizacja: backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:252–255
- Dowód: `if (panelSize >= 5) ... else if (panelSize >= 10)` — druga gałąź jest nieosiągalna. Wejście: `LAB-CBC`, `labPanelSize=12` → mnożnik 0.90 zamiast zamierzonego (wg kolejności progów) 0.85.
- Skutek: zbyt wysoka cena dużych paneli laboratoryjnych.
- Osiągalność: tylko bezpośrednie wywołanie — InvoiceService nie ustawia `labPanelSize`.
- Priorytet: P3
- Weryfikacja: test porównujący `labPanelSize=5` i `labPanelSize=12` dla `LAB-CBC` (reszta kontekstu jak w `setUp`); oczekiwanie: wynik dla 12 < wynik dla 5. Dziś: równe.

### F-04 Martwa gałąź dopłaty za telewizytę > 60 min
- Lokalizacja: backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:277–281
- Dowód: `else if (minutes > 45) ... else if (minutes > 60)` — gałąź 1.50 nieosiągalna. Wejście: `TEL-CONSULT`, `duration=90` → mnożnik 1.25 zamiast 1.50.
- Skutek: zaniżona cena długich telekonsultacji.
- Osiągalność: tylko bezpośrednie wywołanie — InvoiceService nie ustawia `duration`.
- Priorytet: P3
- Weryfikacja: test porównujący `duration=50` i `duration=90` dla `TEL-CONSULT`; oczekiwanie: wynik dla 90 > wynik dla 50. Dziś: równe.

### F-05 Rabat BULK_LAB pomija kody laboratoryjne spoza tabeli statycznej
- Lokalizacja: backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:345–346
- Dowód: `applyDiscounts` bierze rodzinę tylko z `PROCEDURE_FAMILIES.get(procedureCode)`, bez `deriveFamilyFromCode` (w przeciwieństwie do `applyAdjustments`, l.167–170). Wejście: `"LAB-HBA1C"` (z bazy), `labPanelSize=5` → brak mnożnika 0.85, podczas gdy `"LAB-CBC"` z tym samym kontekstem go dostaje.
- Skutek: niespójna wycena tych samych rodzajów badań zależnie od tego, czy kod jest w tabeli zaszytej w kodzie.
- Osiągalność: tylko bezpośrednie wywołanie — InvoiceService nie ustawia `labPanelSize`.
- Priorytet: P3
- Weryfikacja: test z mockiem `procedureRepository.findByProcedureCode("LAB-HBA1C")` zwracającym procedurę z `defaultCost=35.00`; porównać z `LAB-CBC` (35.00) przy `labPanelSize=5`; oczekiwanie: równe wyniki. Dziś: LAB-HBA1C droższy.

## Hipotezy
- H-01 Z InvoiceService `applyAdjustments` praktycznie nigdy nie zmienia ceny: kontekst nie zawiera `firstVisit`, `complexityLevel`, `timeSinceLastVisit`, `telehealthPlatform`, `duration`, `statOrder` itd. (np. seedowany `TEL-200` jest wyceniany jak VIDEO bez względu na typ wizyty). Brakuje wiedzy, czy te dane mają pochodzić z `Encounter`/`Appointment` (np. tryb telehealth wizyty).
- H-02 Wartości tekstowe (`complexityLevel`, `telehealthPlatform`, `anesthesiaType`) są porównywane wielkością liter; `"high"` lub nieznana wartość jest cicho ignorowana. Do potwierdzenia: skąd te wartości przychodzą i w jakim formacie.

## Pytania
- Q-01 Czy rabaty mają się składać multiplikatywnie bez limitu? Senior 65+ z ubezpieczeniem kontraktowym i statusem pracownika dostaje 0.90·0.75·0.80 = 0.54 ceny; z InvoiceService każdy pacjent dostaje albo SELF_PAY (0.95), albo INSURANCE_CONTRACTED (0.80).
- Q-02 Czy panel ≥ 5 badań ma dostać obie obniżki: 0.90 w `applyAdjustments` (l.253) i BULK_LAB 0.85 w `applyDiscounts` (l.346)?
- Q-03 Jaki ma być zakres `customDiscountPercent`? Wartość > 100 daje ujemną cenę, ujemna — podwyżkę (l.353–358); brak walidacji.

## Poza zakresem
- `InvoiceService.generateInvoice`: `setLegacyTaxCents(183)` na sztywno (l.78) i `balanceCents` przez `intValue()` z obcięciem (l.84).
- `applySurcharges`, `applyTax`, `applyLegacyScript` — nieanalizowane.
- `DataBootstrapRunner` tworzy brakujące procedury z kosztem 42.00 (l.128–132) — wpływ na wycenę niesprawdzony.
