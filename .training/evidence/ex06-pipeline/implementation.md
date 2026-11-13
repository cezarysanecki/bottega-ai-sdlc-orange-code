# Implementacja: F-01 F-02
## F-01
- Test: BillingRulesEngineTest#shouldNotThrowForProcedureWhenSedationAndAssistantUnset — przed zmianą: `AssertionFailedError: Unexpected exception thrown: java.lang.NullPointerException: Cannot invoke "java.lang.Boolean.booleanValue()" because the return value of "...PricingContext.requiresSedation()" is null` (at BillingRulesEngine.applyAdjustments(BillingRulesEngine.java:286)); klasa: Tests run: 10, Failures: 1 — po zmianie: zielony (Tests run: 10, Failures: 0, Errors: 0)
- Zmiana: backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:286, :289 — `Boolean.TRUE.equals(context.requiresSedation())`, `Boolean.TRUE.equals(context.requiresAssistant())`
## F-02
- Test: BillingRulesEngineTest#shouldTreatMissingContractedInsuranceAsFalse — przed zmianą: `NullPointerException: Cannot invoke "java.lang.Boolean.booleanValue()" because the return value of "...PricingContext.hasContractedInsurance()" is null` (at BillingRulesEngine.applyDiscounts(BillingRulesEngine.java:338)); klasa: Tests run: 11, Failures: 1 — po zmianie: zielony (Tests run: 11, Failures: 0, Errors: 0)
- Zmiana: backend/src/main/java/com/clinictraining/legacy/BillingRulesEngine.java:338 — `Boolean.TRUE.equals(context.hasContractedInsurance())`
## Testy końcowe
- `cd backend && ./mvnw -B test` → Tests run: 22, Failures: 0, Errors: 0, Skipped: 0, BUILD SUCCESS
## Odstępstwa od planu
- Dodano import `assertDoesNotThrow` w BillingRulesEngineTest (wymagany przez testy z planu).
- Krok weryfikacji `git diff --stat` nie został uruchomiony (polecenie wymagało zatwierdzenia, którego nie uzyskano). Zmiany edytowane wyłącznie w BillingRulesEngine.java (3 linie) i BillingRulesEngineTest.java.
