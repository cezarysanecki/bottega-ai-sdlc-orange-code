# Reakcja na review R-02 (.pipeline/review.md)
- K2 „nieustalone”: zweryfikowane osobno — test na kodzie z HEAD przed refaktoryzacją: 84/0 (`.training/evidence/ex10-verify-old-code.txt`); mutacja F-05 wykrywana (`ex10-verify-mutation.txt`).
- K4 „nieustalone” (reviewer nie uruchamia testów): `./mvnw -B test` 106/0, Checkstyle legacy/* OK (`ex10-final-verification.txt`).
- RH-01 (nagrywanie częściowe nadpisuje CSV): przyjęte jako ryzyko; dodano strażnika liczby przypadków w `writeRecording`. Scenariusza częściowego nie odtworzono — filtr surefire po metodzie uruchamia całą macierz (84), więc strażnik nie został wywołany. Ścieżka względna: zostaje, udokumentowana w komunikacie.
- Poza zakresem (pliki spoza diffu): commit R-02 rozdzielony od plików próby nie jest wymagany w tej próbie; zaznaczone w raporcie.
