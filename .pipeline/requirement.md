# R-02: refaktoryzacja ustalania rodziny procedury (bez zmiany zachowania)
Zgłaszający: SYMULACJA (prowadzący w roli uczestnika), 2026-11-13.

Problem: w `BillingRulesEngine` logika „rodzina z PROCEDURE_FAMILIES, a jak brak, to deriveFamilyFromCode” jest skopiowana w kilku metodach.
Cel: jedna prywatna metoda dla tej logiki tam, gdzie dziś jest ona używana w pełnej postaci.

Kryteria:
- K1: wyniki `calculateCharge` identyczne przed i po dla macierzy: kody {99203, 99213, XR-CHEST, XR-101, LAB-CBC, LAB-HBA1C, TEL-CONSULT, TEL-200, PROC-SUTURE, PROC-XYZ, GEN-001, " LAB-CBC "} × co najmniej 4 konteksty (CA, TX, senior, weekend/after-hours, stat, panel 5, tax exempt).
- K2: test charakteryzujący dodany PRZED refaktoryzacją i zielony na starym kodzie.
- K3: miejsca, które dziś używają tylko `PROCEDURE_FAMILIES.get(...)` (bez fallbacku), zostają bez zmiany zachowania (to znany finding F-05, osobna decyzja).
- K4: `./mvnw -B test` zielone; hook Checkstyle bez naruszeń w zmienionych plikach.
Poza zakresem: poprawki F-03..F-06, zmiany API, formatowanie.
