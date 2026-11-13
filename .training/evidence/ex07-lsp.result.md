Wszystkie trzy wywołania LSP zadziałały za pierwszym razem, więc nie trzeba było ich powtarzać.

**1. findReferences** na `calculateCharge` (`BillingRulesEngine.java:106:23`): 21 referencji w 4 plikach
- `backend/src/main/java/com/clinictraining/billing/InvoiceService.java:72`
- `backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:106` (sama deklaracja)
- `backend/src/test/java/com/clinictraining/billing/InvoiceServiceTest.java:106`, `:144`, `:146`
- `backend/src/test/java/com/clinictraining/legacy/BillingRulesEngineTest.java:68`, `:77`, `:88`, `:101`, `:108`, `:120`, `:130`, `:134`, `:152`, `:162`, `:163`, `:171`, `:175`, `:183`, `:186`, `:200`

**2. incomingCalls**: 16 metod wywołujących
- `InvoiceService.java`: `generateInvoice(String)` (l. 42), wywołanie w 72
- `InvoiceServiceTest.java`:
  - `shouldGenerateInvoiceForEncounterWithSingleProcedure` (l. 99), wywołanie w 106
  - `shouldGenerateInvoiceForEncounterWithMultipleProcedures` (l. 126), wywołania w 144 i 146
- `BillingRulesEngineTest.java`:
  - `shouldCalculateBasicOfficeVisitCharge` (l. 66), wywołanie w 68
  - `shouldCalculateLabProcedureCharge` (l. 75), wywołanie w 77
  - `shouldApplySeniorDiscountForPatientsOver65` (l. 84), wywołanie w 88
  - `shouldApplyWeekendSurchargeForSaturdayService` (l. 94), wywołania w 101 i 108
  - `shouldApplyAfterHoursSurchargeForLateService` (l. 113), wywołanie w 120
  - `shouldApplyStateSalesTaxForLabProcedures` (l. 126), wywołania w 130 i 134
  - `shouldHandleMultipleDiscountsAndSurcharges` (l. 142), wywołanie w 152
  - `shouldPriceCodeWithSurroundingWhitespaceSameAsTrimmedCode` (l. 158), wywołania w 162 i 163
  - `shouldNotThrowForProcedureWhenSedationAndAssistantUnset` (l. 168), wywołanie w 171
  - `get()` (lambda, l. 175), wywołanie w 175
  - `shouldTreatMissingContractedInsuranceAsFalse` (l. 180), wywołanie w 183
  - `get()` (lambda, l. 186), wywołanie w 186
  - `shouldUseProcedureRepositoryWhenAvailable` (l. 191), wywołanie w 200

**3. hover** na `requiresSedation` (`BillingRulesEngine.java:581`):
```
Boolean com.clinictraining.legacy.BillingRulesEngine.PricingContext.requiresSedation()
```
Metoda zwraca `Boolean` (obiekt, może być `null`), a nie prymitywny `boolean`.

Grep znalazłby każde wystąpienie napisu „calculateCharge”, także w komentarzach, dokumentacji i innych metodach o tej samej nazwie, a LSP zwraca tylko wywołania właśnie tej metody i dodatkowo podaje, która metoda (łącznie z lambdami `get()`) je wykonuje.