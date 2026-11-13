Plan zapisany w `.pipeline/plan.md`. Dowody z analizy nadal się zgadzają na commicie 19cd052. Kodu nie zmieniałem, plan czeka na Twoje zatwierdzenie.

- **F-01:** test `shouldNotThrowForProcedureWhenSedationAndAssistantUnset` dla `PROC-SUTURE` z `sedation` i `assistant` ustawionymi na `null`; poprawka to `Boolean.TRUE.equals(...)` w `BillingRulesEngine.java:286` i `:289`.
- **F-02:** test `shouldTreatMissingContractedInsuranceAsFalse` dla `99213` z `contractedInsurance` ustawionym na `null`; poprawka to `Boolean.TRUE.equals(...)` w `BillingRulesEngine.java:338`.
- **Weryfikacja:** `./mvnw -B -Dtest=BillingRulesEngineTest test` przed zmianą ma pokazać 2 nieudane testy, po zmianie wszystkie mają przejść. Potem `./mvnw -B test` musi zakończyć się BUILD SUCCESS.