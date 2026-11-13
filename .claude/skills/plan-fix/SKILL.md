---
name: plan-fix
description: Plan poprawki wybranych findings z .pipeline/analysis.md. Zapisuje .pipeline/plan.md. Użyj po analizie i po wyborze ID przez człowieka.
argument-hint: "<ID findings, np. F-01 F-03>"
allowed-tools: Read Grep Glob Bash(git rev-parse:*) Edit(/.pipeline/**)
---

Wybrane findings: $ARGUMENTS

1. Przeczytaj `.pipeline/analysis.md`. Jeśli któregoś ID nie ma albo ma otwarte pytanie (Q-xx) w polu „Oczekiwane zachowanie”, zatrzymaj się i wypisz pytanie zamiast planu.
2. Sprawdź w kodzie, czy dowód z analizy nadal jest aktualny (commit mógł się zmienić).
3. Zapisz `.pipeline/plan.md`:
```
# Plan: <ID...>
- Commit bazowy: <sha>
- Źródło: .pipeline/analysis.md
## Kroki
### <ID> <tytuł>
- Test regresji (najpierw, ma zawieść): <klasa#metoda, wejście, oczekiwanie>
- Zmiana: <plik:linia, co dokładnie>
- Poza zakresem: <czego nie ruszać>
## Weryfikacja
- <polecenie z CLAUDE.md, oczekiwany wynik>
## Zatwierdzenie
- Status: CZEKA NA ZATWIERDZENIE
```
Nie zmieniaj kodu. Na koniec wypisz ścieżkę planu i kroki w 3 linijkach.
