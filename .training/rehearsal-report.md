# Próba techniczna 13 ćwiczeń: AI w SDLC, Bottega / Orange

Data: 2026-11-13. Worktree `cezarysanecki/orange-proba-13-cwiczen-cc`, start 12886dd (`source-marek-00-start`). Claude Code 2.1.311, model claude-opus-5-5, JDK 25.0.2 oraz 26, Node 26.13.0, Podman 5.5.2.
Świeże sesje uruchamia `.training/scripts/headless.sh` (`claude -p --setting-sources project,local --strict-mcp-config --tools … --no-session-persistence`). Czas to czas ściany sesji (zmierzony). Koszt to `total_cost_usd` raportowany przez CLI dla konta subskrypcyjnego. Szacunki dla grupy są oznaczone jako szacunki.
**Próba przerwana: wyczerpał się limit sesji (reset 19:00).** Ćwiczenia 11–13 nie zostały wykonane.

| # | Ćwiczenie | Status | Zmierzone | Dowody |
|---|---|---|---|---|
| 1 | Rozmowa o aplikacji | PASS | 65 s, 27 narzędzi, $0.77 | `evidence/ex01-domain-conversation.result.md`, `ex01-api-verification.txt` |
| 2 | CLAUDE.md | PASS | polecenia ~1 min | `CLAUDE.md`, `ex02-command-verification.log`, `ex02-claude-md-check.result.md`, `ex02-frontend-test-node26.log` |
| 3 | Bugfix z Plan Mode | PASS | plan 34 s, red→green | `artifacts/ex03-*`, `ex03-plan-mode.*`, `ex03-red.log`, `ex03-green.log`, `ex03-diff.patch` |
| 4 | Skill analizy | PASS | 80–95 s/uruchomienie | `.claude/skills/analyze/SKILL.md`, `ex04-analysis-run1.md`, `ex04-skill-iteration.diff`, `ex04-analysis-case2.md`, `ex04-case2-F01-api-check.txt` |
| 5 | Subagent review | PASS | 47 s, $0.33 | `.claude/agents/reviewer.md`, `ex05-subagent-review.*`, `ex05-verify-RH01-nbsp.txt` |
| 6 | Wspólny pipeline | PASS | 93+38+123 s, ~$1.23 | `evidence/ex06-pipeline/`, `ex06-verify-red.log`, `ex06-verify-green-full.log` |
| 7 | LSP | PASS | 47 s, $0.16 | `ex07-lsp.*`, `ex07-grep-comparison.txt` |
| 8 | MCP (Context7) | PARTIAL | 36 s, $0.21 | `.mcp.json`, `ex08-mcp-context7.*` |
| 9 | Hook + Checkstyle | PASS | 26 s; hook ~5–8 s na plik | `backend/checkstyle.xml`, `.claude/hooks/java_lint.py`, `ex09-hook-script-tests.txt`, `ex09-hook-events-extract.txt`, `ex09-real-edit-nonjava.*` |
| 10 | Workflow + refaktoring | PASS | plan 28 s, impl. 152 s, review 51 s | `.pipeline/{requirement,plan,implementation,review,review-response}.md`, `ex10-verify-*.txt`, `ex10-final-verification.txt` |
| 11 | Bez / ze standardami | BLOCKED | — | `ex11/` (kryteria, prompty, standardy), `ex11-variant-{A,B}.result.md` |
| 12 | Handoff / świeża sesja | BLOCKED | — | `.pipeline/handoff.md`, `ex12-handoff.result.md` |
| 13 | Maister | NOT RUN | — | — |

## Wspólny przypadek domenowy
`BillingRulesEngine.calculateCharge`, czyli wycena procedur wołana z `InvoiceService.generateInvoice`.
- Hipoteza o `trim()` potwierdzona testem: `" LAB-CBC "` dawało 64.84 zamiast 49.88.
- Analiza znalazła NPE dla kodów `PROC*`: `InvoiceService` nie ustawia flag `sedation` ani `assistant`.
- Drugi przypadek, rezerwacja wizyty, dał błąd potwierdzony przez API: podwójna rezerwacja tego samego lekarza w tym samym czasie, gdy offset w żądaniu różni się od strefy JVM (`ex04-case2-F01-api-check.txt`). Frontend wysyła czas w UTC (`toISOString`), więc to dobry kandydat na wspólny przypadek dla grupy.

## Ćwiczenia

**1.** Świeża sesja miała tylko Read, Glob i Grep. Prompt jest w `.training/prompts/ex01.txt` i ma wymuszony format: proces, plik:linia, niewiadome.
- Potwierdzone w kodzie: `/appointments/new` → mutacja GraphQL → `ClinicQueryResolver` → `AppointmentService.bookAppointment`. Kolizje są sprawdzane tylko dla lekarza. Jedyna rola to `ROLE_USER`, a `/graphql` jest dostępny bez JWT.
- Potwierdzone przez API (backend demo na porcie 18180):
  - anonimowe `createAppointment` zwraca `SCHEDULED`;
  - kolizja kończy się `INTERNAL_ERROR` w logu "Detected 1 conflicting";
  - REST bez tokena zwraca 403.
- Ograniczenie środowiska: frontend ma zaszyte `http://localhost:8080` (`fetchClient.ts`, `graphql/client.ts`), więc proxy Vite nic nie zmienia. Każdy uczestnik potrzebuje wolnego portu 8080.

**2.** CLAUDE.md zawiera tylko sprawdzone polecenia.
- `./mvnw -B test` przechodzi także na Java 26. Wcześniejsza wskazówka o Javie 26 jest nieaktualna.
- `PaymentServiceIT` przechodzi: 5 testów w 21 s na Podmanie.
- `verify` przechodzi. `npm run lint` daje 0 błędów i 13 ostrzeżeń.
- Na Node 26 `npm test -- --run` przechodzi tylko z `NODE_OPTIONS=--no-experimental-webstorage`.
- Usunięte zmyślenia i treści, które szybko się dezaktualizują:
  - polecenie checkstyle, zanim istniało;
  - liczba testów, bo reviewer w ćw. 5 powtórzył nieaktualne „19”;
  - „Node 25+”, bo sprawdzony był tylko Node 26.
- Granica CLAUDE.md: świeża sesja podała polecenia z CLAUDE.md, ale niczego nie sprawdziła. CLAUDE.md to kontekst, a nie egzekwowanie zasad.
- Ścieżki według dokumentacji (2026-11-13):
  - `.claude/skills/<n>/SKILL.md`
  - `.claude/agents/<n>.md`
  - `.claude/settings.json` (uprawnienia i hooki)
  - `.mcp.json`

**3.** Faktyczny Plan Mode: zdarzenie init w pliku jsonl ma `permissionMode: plan`.
- Plan trafnie przewidział wynik 64.84 / 49.88.
- Wymaganie W-01 i jego zatwierdzenie to SYMULACJA decyzji uczestnika, a nie decyzja człowieka.
- Red: `expected <49.88> but was <64.84>`. Green: 9/9, pełna regresja 20/20.
- W trybie `-p` nie było ExitPlanMode, więc plan powstał jako tekst odpowiedzi.
- **Do przećwiczenia z człowiekiem:** interaktywne Shift+Tab, zatwierdzenie planu w UI.

**4.** Skill przyjmuje argumenty `$scope $goal` i zapisuje do pliku przez `allowed-tools: Edit(/.pipeline/**)`.
- Iteracja: dodane pole „Oczekiwane zachowanie + źródło / Q-xx” i ostrzejsze definicje P1/P2/P3. Po zmianie martwy kod (F-03/F-04) odsyła do pytania i nie zgaduje „zamierzonej” wartości.
- Drugi przypadek (AppointmentService) potwierdza, że kontrakt nie jest dopasowany tylko do pierwszego.
- Linie z findings sprawdziłem ręcznie.

**5.** Subagent `reviewer` ma narzędzia Read, Glob i Grep. W jsonl widać wywołanie `Agent(subagent_type=reviewer)` i jego narzędzia z `parent_tool_use_id`.
- Raport rozdziela kryteria, błędy (brak) i hipotezy.
- Sprawdziłem RH-01: `trim()` nie usuwa NBSP, ale proponowane `strip()` też go nie usuwa. Hipoteza była trafna, a rekomendacja błędna.

**6.** Pipeline: `/analyze` → `selection.md` (SYMULACJA) → `/plan-fix F-01 F-02` → zatwierdzenie (SYMULACJA) → `/implement`, czyli subagent `implementer`.
- Implementer odwołuje się do ID findings.
- Red/green sprawdziłem sam przez cofnięcie zmiany: 2 NPE, potem 22/22.

**7.** Natywne LSP Claude Code: plugin `jdtls-lsp` (claude-plugins-official@099387b) ładowany przez `--plugin-dir`.
- jdtls 1.64.0 (snapshot) leży w `.training/tools`, bez brew i bez zmian w HOME. Wrapper ustawia JDK 25 i katalog `-data`.
- findReferences zwrócił 21 referencji, incomingCalls 16 metod, hover pokazał `Boolean requiresSedation()`.
- grep dał tu te same 21 linii. Różnica byłaby widoczna dopiero przy przeciążeniach lub częstych nazwach.

**8. PARTIAL.** Context7 (`https://mcp.context7.com/mcp`, bez klucza) połączył się. Wysłałem tylko nazwy bibliotek i pytania, bez kodu.
- Brak: Context7 nie ma dokumentacji maven-checkstyle-plugin, tylko checkstyle.org. Część parametrów agent podał „z wiedzy” i sam to oznaczył.
- Zweryfikowałem je w oficjalnym check-mojo (3.6.0) i w Maven Central, potem zastosowałem w ćw. 9.
- Serena nie była testowana.

**9.** Checkstyle 14.3.0 / plugin 3.6.0 z jedną regułą `UnusedImports`. Na całym kodzie: 3 naruszenia legacy, po ćwiczeniu 2.
- Hook w Pythonie odczytuje `tool_input.file_path`, akceptuje tylko `backend/src/{main,test}/java/*.java`, sprawdza nazwę wzorcem i wywołuje mvnw bez powłoki.
- Fixture'y: plik poprawny daje 0, naruszenie daje 2 i stderr, `.md` jest pomijany, ścieżka `;touch $(…)` jest pomijana i nic się nie wykonuje.
- Rzeczywista edycja przez Claude: hook zwrócił exit 2 z `UnusedImports java.util.List`, Claude usunął import, ponowny hook przeszedł czysto. Edycja `.md` dała hook z exit 0 bez komunikatu.
- Uwaga: model nazwał to „zablokował”, ale PostToolUse niczego nie cofa ani nie blokuje.

**10.** R-02 przeszedł cały workflow: plan w Plan Mode, zatwierdzenie (SYMULACJA), implementer, test charakteryzujący, review subagenta, poprawka, weryfikacja końcowa.
- Test charakteryzujący: 84 przypadki z CSV nagranego na starym kodzie.
- Niezależnie sprawdzone: stary kod daje 84/0, mutacja F-05 jest wykrywana, wynik końcowy 106/0, Checkstyle OK.
- Mój błąd jako „prowadzącego”: zgoda „bez commita” osłabiła kryterium `git diff CSV`. Zauważył to implementer.
- Strażnik przed nagraniem częściowym jest dodany, ale scenariusz nie został odtworzony.

**11. BLOCKED** (limit sesji). Przygotowane: dwie identyczne kopie z f61f08b, różniące się tylko `STANDARDS.md`; prompty A i B różnią się jedną linią instrukcji i portem; kryteria C1–C7 ustalone przed uruchomieniem. Obie sesje skończyły się natychmiast komunikatem o limicie, więc porównania nie ma.

**12. BLOCKED** (limit). `.pipeline/handoff.md` jest gotowy, a następny krok to test K2. Świeża sesja nie ruszyła.

**13. NOT RUN.** Maister nie był pobrany ani uruchomiony. Globalnie zainstalowany jest Maister 2.2.3 (scope user). Do ćwiczenia trzeba sklonować wersję referencyjną do `.training/` i uruchomić quick-bugfix. Bramki AskUserQuestion wymagają człowieka.

## Ustalenia o narzędziach (ważne dla prowadzącego)
- `--setting-sources project,local` NIE wyłącza konektorów claude.ai: Todoist i Docs były widoczne w sesji. Trzeba dodać `--strict-mcp-config`.
- W nieufanym workspace `permissions.allow` z `.claude/settings.json` jest ignorowane, a hooki projektu się wykonują. Zaufanie jest zapisane dla ścieżki głównego repo w `~/.claude.json`, którego nie zmieniałem. Uprawnienia dla skilli trzeba dawać przez `allowed-tools` w skillu, a w headless przez `--allowedTools`.
- Reguły ścieżek dla zapisu działają jako `Edit(...)`. Reguła `Write(...)` nie zadziałała.

## Rekomendacje i uproszczenia
- Wspólny scenariusz: BillingRulesEngine (W-01 → F-01/F-02 → R-02) daje czytelne red/green w kilka minut. Przypadek „podwójna rezerwacja przez offset” warto mieć jako bardziej realistyczny wariant P1.
- Dzień 2, blok 1: LSP przez lokalny jdtls trzeba przygotować przed szkoleniem, bo pobranie i indeksowanie trwa. Najlepiej przygotować skrypt instalacyjny. Context7 pokazać jako MCP do dokumentacji, z uczciwym brakiem pokrycia.
- Hook dokłada ~5–8 s do każdej edycji Javy (mvnw). Dla grupy wystarczy jedna reguła.
- Nie zweryfikowano strony Bottegi (60% praktyki) ani bilansu 486 minut. Brak pomiarów czasu pracy uczestników: czasy powyżej to czas modelu.
- Prowadzący musi przećwiczyć z człowiekiem: interaktywny Plan Mode i zatwierdzanie planu, dialog zaufania workspace, bramki Maister, ćw. 11–13.

## Reset
`git reset --hard 12886dd` w tym worktree, usunięcie `.training/` i `.pipeline/`. Kopie ćw. 11 leżą w scratchpadzie sesji. Backend 18180 (mój proces) zatrzymany.
