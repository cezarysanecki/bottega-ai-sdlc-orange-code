`.pipeline/analysis.md`
Wynik: 6 findings, 2 hipotezy, 4 pytania.
Najważniejszy jest F-01 (P2). Dla procedury z kodem `PROC*` faktura generowana z `InvoiceService` kończy się `NullPointerException`, bo flagi `sedation` i `assistant` nie są ustawiane, a kod czyta je z auto-unboxingiem (`BillingRulesEngine.java:286,289`). Takiej procedury nie ma w seedach.