---
name: analyze
description: Analiza wskazanego fragmentu kodu przed zmianą. Czyta kod i testy w zadanym zakresie i zapisuje .pipeline/analysis.md według stałego kontraktu (findings z ID, dowodem, skutkiem, priorytetem, weryfikacją; osobno hipotezy i pytania). Użyj, gdy trzeba ustalić, co w danym miejscu jest błędem, zanim powstanie plan.
argument-hint: "<zakres: plik, klasa#metoda albo katalog> [cel analizy]"
arguments: [scope, goal]
allowed-tools: Read Grep Glob Bash(git rev-parse:*) Bash(git status:*) Edit(/.pipeline/**)
---

Zakres: $scope
Cel: $goal (jeśli pusty: znaleźć błędy zachowania w zakresie)

## Jak pracować
1. Ustal commit: `git rev-parse --short HEAD` i czy drzewo jest czyste (`git status --porcelain`).
2. Przeczytaj kod w zakresie, jego wywołujących (Grep po nazwie) i istniejące testy. Wyjdź poza zakres tylko po to, by sprawdzić, czy ścieżka jest osiągalna.
3. Finding to błąd, który potrafisz wskazać w kodzie: konkretne wejście → konkretny zły wynik. Jeśli nie umiesz podać wejścia albo nie wiesz, jakie zachowanie jest oczekiwane, to hipoteza albo pytanie, nie finding.
4. Nie licz wyników „na oko”. Jeśli dowód zależy od liczby, opisz, jak ją policzyć testem.
5. Nie zmieniaj kodu. Zapisujesz wyłącznie `.pipeline/analysis.md` (nadpisz poprzedni).

## Kontrakt `.pipeline/analysis.md`
```
# Analiza: <zakres>
- Cel: ...
- Zakres: <pliki/metody>; commit: <sha>; drzewo: czyste|zmienione
## Findings
### F-01 <krótki tytuł>
- Lokalizacja: <ścieżka>:<linia>
- Dowód: <fragment kodu lub ścieżka wywołań; wejście → wynik>
- Skutek: <co widzi użytkownik/dane>
- Osiągalność: <skąd wywoływane w produkcji albo "tylko bezpośrednie wywołanie">
- Priorytet: P1 (błędny wynik/awaria na realnej ścieżce) | P2 (realne, ale wąskie) | P3 (porządek, martwy kod)
- Weryfikacja: <test, który to pokaże: nazwa, wejście, oczekiwanie>
## Hipotezy
- H-01 <co podejrzewasz> — czego brakuje do potwierdzenia
## Pytania
- Q-01 <decyzja wymagająca człowieka, np. oczekiwane zachowanie>
## Poza zakresem
- <rzeczy zauważone, nieanalizowane>
```
Numeruj od 01 w każdym pliku. Maks. 7 findings; jeśli jest ich więcej, zostaw najważniejsze i wpisz liczbę pominiętych.

Na koniec odpowiedz w 3 linijkach: ścieżka pliku, liczba F/H/Q, najważniejszy finding.
