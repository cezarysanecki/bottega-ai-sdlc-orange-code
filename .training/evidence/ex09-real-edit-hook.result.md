Dodałem Javadoc nad `calculateCharge` (`BillingRulesEngine.java:105-108`). Poza tym usunąłem z tego pliku jeden nieużywany import, bo zgłosił go hook Checkstyle. Niczego więcej nie zmieniałem.

Feedback po każdej edycji:

1. **Po dodaniu Javadoca** hook `java_lint.py` zablokował edycję błędem Checkstyle: `BillingRulesEngine.java:[17,8] (imports) UnusedImports: Unused import - java.util.List.` Zmiana była już zapisana. To naruszenie istniało w pliku już wcześniej, Javadoc go nie spowodował. `List` nigdzie w pliku nie jest używane, więc usunięcie importu to mała poprawka w tym samym pliku.
2. **Po usunięciu `import java.util.List;`** hook zwrócił: „Checkstyle: brak naruszeń w com/clinictraining/legacy/BillingRulesEngine.java.”

Testów (`./mvnw -B test`) nie uruchamiałem. Zmieniłem tylko komentarz i nieużywany import, więc działanie kodu jest takie samo.