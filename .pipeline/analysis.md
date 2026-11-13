# Analiza: BillingRulesEngine#applyAdjustments, applyDiscounts
- Cel: błędy zachowania przy wycenie procedur wywoływanej z InvoiceService
- Zakres: backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java — `applyAdjustments` (l. 166–312), `applyDiscounts` (l. 314–361); commit: 19cd052; drzewo: zmienione (pliki szkoleniowe/pipeline poza `backend/`, kod w zakresie niezmieniony)

Kontekst wywołań: `InvoiceService.generateInvoice` (l. 43) ← `BillingController` (l. 83, REST), `ClinicQueryResolver.generateInvoice` (l. 146, GraphQL), `DataBootstrapRunner` (l. 152, start aplikacji). `InvoiceService.buildPricingContext` (l. 93–112) ustawia wyłącznie: `serviceDateTime`, `patientAge`, `state`, `selfPay`, `contractedInsurance`. Wszystkie pozostałe pola `PricingContext` są `null`. Procedury w DB z seedów: `XR-101`, `TEL-200`, `GEN-001` (V2__core_domain.sql:147); `EncounterService.addProcedures` przyjmuje tylko kody istniejące w tabeli `procedures`, a API nie ma endpointu tworzenia procedur.

## Findings
### F-01 NPE dla rodziny PROCEDURE przy kontekście z InvoiceService
- Lokalizacja: backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:286, :289
- Dowód: `if (context.requiresSedation())` / `if (context.requiresAssistant())` — metody zwracają `Boolean` (l. 581, 584), auto-unboxing `null` → `NullPointerException`. `InvoiceService.buildPricingContext` nigdy nie ustawia `sedation`/`assistant`. Wejście: encounter z procedurą `PROC-SUTURE` (lub dowolny kod z prefiksem `PROC`, rodzina z `deriveFamilyFromCode`, l. 501) i `overrideCost == null` → `calculateCharge` rzuca NPE → `generateInvoice` kończy się wyjątkiem, faktura nie powstaje.
- Oczekiwane zachowanie: brak flagi = brak dopłaty (jak `false`) — źródło: spójność z sąsiednim kodem; wszystkie inne flagi w obu metodach są czytane przez `Boolean.TRUE.equals(...)` (l. 176, 223, 226, 238, 244, 247, 324, 331).
- Skutek: błąd 500 z REST/GraphQL przy generowaniu faktury dla encountera z procedurą zabiegową; w bootstrapie faktura cicho pominięta (catch, l. 153).
- Osiągalność: `BillingController` / `ClinicQueryResolver` → `InvoiceService.generateInvoice` → `calculateCharge` → `applyAdjustments`. Wymaga procedury z kodem `PROC*` w tabeli `procedures` — brak w seedach i brak endpointu jej tworzenia (możliwa przez wpis w DB lub `encounters.csv`, `DataBootstrapRunner` l. 126–133 tworzy brakujące procedury).
- Priorytet: P2
- Weryfikacja: `BillingRulesEngineTest.shouldNotThrowForProcedureWhenSedationAndAssistantUnset` — `new PricingContext()` z ustawionymi tylko polami jak w `buildPricingContext` (`contractedInsurance=false`, `selfPay=true`, `state="CA"`), kod `PROC-SUTURE` → oczekiwanie: brak wyjątku i wynik równy wynikowi dla `sedation=false, assistant=false`. Uwaga: obecny `setUp` testów ustawia `sedation/assistant=false` (l. 58–59), co maskuje błąd.

### F-02 NPE w applyDiscounts, gdy `contractedInsurance` nie jest ustawione
- Lokalizacja: backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:338
- Dowód: `if (context.hasContractedInsurance())` — `Boolean` (l. 599) unboxowany bez null-checku. Wejście: `calculateCharge("99213", new PricingContext())` → NPE.
- Oczekiwane zachowanie: `null` traktowane jak `false` — źródło: spójność z l. 324 i 331 (`Boolean.TRUE.equals`).
- Skutek: wyjątek zamiast kwoty dla każdego wywołującego, który nie ustawi pola.
- Osiągalność: tylko bezpośrednie wywołanie — `InvoiceService` zawsze ustawia pole (l. 109).
- Priorytet: P3
- Weryfikacja: `shouldTreatMissingContractedInsuranceAsFalse` — `new PricingContext()`, kod `99213` → brak wyjątku, wynik równy wynikowi z `contractedInsurance=false`.

### F-03 Próg panelu laboratoryjnego ≥10 nieosiągalny
- Lokalizacja: backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:252–255
- Dowód: `if (panelSize >= 5) ... else if (panelSize >= 10)` — każda wartość ≥10 spełnia już pierwszy warunek; gałąź 0.85 to martwy kod. Wejście: `LAB-CBC`, `labPanelSize=12` → mnożnik 0.90 (jak dla 5).
- Oczekiwane zachowanie: nieustalone → Q-01
- Skutek: duże panele wyceniane jak średnie (jeśli intencją był wyższy rabat).
- Osiągalność: tylko bezpośrednie wywołanie — `InvoiceService` nie ustawia `labPanelSize`.
- Priorytet: P3
- Weryfikacja: `shouldApplyLargerPanelAdjustmentForTenOrMoreTests` — porównać wynik dla `labPanelSize=5` i `labPanelSize=10` (pozostałe pola identyczne); dziś wyniki są równe.

### F-04 Próg telehealth >60 min nieosiągalny
- Lokalizacja: backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:277–280
- Dowód: `else if (minutes > 45) ... else if (minutes > 60)` — każda wartość >60 spełnia już `> 45`; mnożnik 1.50 to martwy kod. Wejście: `TEL-CONSULT`, `duration=90` → mnożnik 1.25.
- Oczekiwane zachowanie: nieustalone → Q-01
- Skutek: długie konsultacje telehealth wyceniane jak 46-minutowe.
- Osiągalność: tylko bezpośrednie wywołanie — `InvoiceService` nie ustawia `duration` (ani `telehealthPlatform`), więc dla `TEL-200` z seedów cała gałąź TELEHEALTH jest bez efektu.
- Priorytet: P3
- Weryfikacja: `shouldApplyLongSessionAdjustmentAboveSixtyMinutes` — porównać wynik dla `duration=50` i `duration=90`; dziś są równe.

### F-05 Rabat BULK_LAB pomija kody LAB spoza tabeli statycznej
- Lokalizacja: backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:345–346
- Dowód: `applyDiscounts` bierze rodzinę tylko z `PROCEDURE_FAMILIES.get(procedureCode)`, bez fallbacku `deriveFamilyFromCode`, którego używają `applyAdjustments` (l. 168–170) i `applyTax` (l. 415–417). Wejście: kod `LAB-HBA1C` (z DB), `labPanelSize=6` → `applyAdjustments` stosuje 0.90 jako LABORATORY, `applyDiscounts` nie stosuje 0.85 (rodzina `null`); dla `LAB-CBC` przy tych samych danych oba mnożniki działają.
- Oczekiwane zachowanie: ta sama rodzina co w `applyAdjustments`/`applyTax` — źródło: spójność z sąsiednim kodem (o ile podwójny rabat jest zamierzony, zob. H-01).
- Skutek: różna cena dla dwóch procedur laboratoryjnych przy tych samych danych.
- Osiągalność: tylko bezpośrednie wywołanie — `InvoiceService` nie ustawia `labPanelSize`.
- Priorytet: P3
- Weryfikacja: `shouldApplyBulkLabDiscountForDerivedLabFamily` — mock `procedureRepository.findByProcedureCode("LAB-HBA1C")` z `defaultCost=35.00`, porównać z `LAB-CBC` (35.00) przy `labPanelSize=6` → oczekiwanie: równe kwoty.

### F-06 Rabat procentowy bez walidacji zakresu
- Lokalizacja: backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:353–358
- Dowód: mnożnik `1 - p/100` bez ograniczeń. Wejście: `customDiscountPercent=150`, `99213` → mnożnik −0.5 → ujemna kwota przed podatkiem; `customDiscountPercent=-20` → narzut 1.2.
- Oczekiwane zachowanie: nieustalone → Q-02
- Skutek: możliwa ujemna pozycja faktury.
- Osiągalność: tylko bezpośrednie wywołanie — `InvoiceService` nie ustawia `customDiscountPercent`.
- Priorytet: P3
- Weryfikacja: `shouldRejectOrClampCustomDiscountOutsideZeroToHundred` — `customDiscountPercent=150` → oczekiwanie wg decyzji z Q-02 (wyjątek albo kwota ≥ 0).

## Hipotezy
- H-01 Podwójne naliczanie dla laboratorium: `labPanelSize>=5` daje 0.90 w `applyAdjustments` (l. 252) i 0.85 BULK_LAB w `applyDiscounts` (l. 346); `statOrder` daje 1.40 w `applyAdjustments` (l. 244) i 1.40 STAT_LAB w `applySurcharges` (l. 400–406). Brakuje wymagania, czy to dwie osobne reguły, czy duplikat (→ Q-03). Dziś nieosiągalne z `InvoiceService`.
- H-02 Każdy pacjent z dowolnym planem ubezpieczeniowym dostaje rabat INSURANCE_CONTRACTED 0.80, bo `InvoiceService` (l. 109–110) mapuje `insurancePlan != null` na „contracted”; senior z ubezpieczeniem dostaje 0.90×0.80. Osiągalne z seedów (PAT z planami PPO-BETA/HMO-ALPHA). Brakuje informacji, czy wszystkie plany są kontraktowane i czy rabaty mają się kumulować (→ Q-04). Przyczyna leży w `InvoiceService`, nie w `applyDiscounts`.

## Pytania
- Q-01 Czy progi `labPanelSize>=10` → 0.85 i `duration>60` → 1.50 mają obowiązywać (wtedy kolejność warunków jest błędem), czy gałęzie należy usunąć?
- Q-02 Jaki dopuszczalny zakres `customDiscountPercent` (0–100?) i co przy wartości spoza zakresu: wyjątek czy obcięcie?
- Q-03 Czy rabat za duży panel i dopłata STAT dla laboratorium mają być naliczane raz czy dwukrotnie (adjustment + discount/surcharge)?
- Q-04 Czy każdy plan ubezpieczeniowy oznacza kontrakt (rabat 20%) i czy rabaty SENIOR/EMPLOYEE/INSURANCE mają się kumulować multiplikatywnie, czy obowiązuje najwyższy?

## Poza zakresem
- `applyLegacyScript` (l. 465–482): skrypt Groovy ma `adjust(BigDecimal)`, a wywołanie przekazuje 3 argumenty → wyjątek połykany, zawsze fallback `+12.34`; dodatkowo `adjusted.getClass()` przy `null` (l. 475) → NPE połykany.
- `InvoiceService.calculateAge` liczy wiek względem `LocalDate.now()`, nie daty usługi — wpływa na rabat SENIOR przy fakturowaniu wstecz.
- `InvoiceService`: `setLegacyTaxCents(183)` na sztywno; `balanceCents` przez `intValue()` obcina grosze.
- `applySurcharges`: `hour > 18` nie obejmuje 18:xx jako po godzinach (do weryfikacji wymagań).
- `applyAdjustments`: nieznane wartości `complexityLevel`/`telehealthPlatform`/`anesthesiaType` są cicho ignorowane (brak `default`).
