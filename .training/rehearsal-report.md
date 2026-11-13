# Próba techniczna 13 ćwiczeń: AI w SDLC, Bottega / Orange

Data: 2026-11-13. Worktree `cezarysanecki/orange-proba-13-cwiczen-cc`, start 12886dd (`source-marek-00-start`). Claude Code 2.1.311, model claude-opus-5-5, JDK 25.0.2 oraz 26, Node 26.13.0, Podman 5.5.2.
Świeże sesje uruchamia `.training/scripts/headless.sh` (`claude -p --setting-sources project,local --strict-mcp-config --tools … --no-session-persistence`). Czas to czas ściany sesji (zmierzony). Koszt to `total_cost_usd` raportowany przez CLI dla konta subskrypcyjnego. Szacunki dla grupy są oznaczone jako szacunki.
Pierwsze podejście do ćw. 11–12 przerwał limit sesji (dowody `*-try1-limit.*`); po resecie wykonano 11–13.

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
| 11 | Bez / ze standardami | PASS | A: 204 s pracy, $0.87; B: 159 s, $0.58 | `ex11/{criteria,result,standards}.md`, `ex11-variant-{A,B}.full-report.md`, `ex11-verify-{A,B}.txt` |
| 12 | Handoff / świeża sesja | PASS | 91 s, $0.25 | `.pipeline/handoff.md`, `ex12-handoff.*`, `ex12-diff.patch`, `ex12-verify-tests.txt` |
| 13 | Maister quick-bugfix | PASS | plan 38–39 s; impl. 123 s; łącznie $1.09 | `artifacts/ex13-gate-decision.md`, `ex13-maister-*.{jsonl,result.md}`, `ex13-maister-diff.patch`, `ex13-verify.txt` |

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

**11.** Warianty A i B uruchomione w świeżych sesjach `-p` na dwóch identycznych kopiach z f61f08b (scratchpad, poza projektem, żeby nie ładować `.training`). Model, narzędzia i allowlista są te same. Jedyne różnice to `STANDARDS.md` z jedną linią instrukcji oraz port.
- Kryteria C1–C7 zapisałem przed uruchomieniem (`ex11/criteria.md`). Sprawdziłem je sam: testy, build, introspekcja GraphQL i mutacje na żywym backendzie.
- Oba warianty spełniają wszystkie kryteria.
- Różnice jakościowe:
  - A znalazł istniejący ekran (`AppointmentDetailPage.tsx:83` czyta `appointmentType`) i nazwą pola go naprawił.
  - B trzymał się standardu („kod, nie opis”, „nie dodawaj pól do nieużywanych zapytań”): nazwał pole `appointmentTypeCode`, więc tego ekranu nie naprawił. Był szybszy i tańszy.
- Wniosek dotyczy treści standardu, a nie przewagi standardów: brakuje reguły „sprawdź istniejących konsumentów”. Jedna para prób nie pozwala na uogólnienia (`ex11/result.md`).

**12.** Handoff `.pipeline/handoff.md` przekazany świeżej sesji (Read/Edit/Glob/Grep/Bash z allowlistą).
- Sesja sama sprawdziła twierdzenia: commity się zgadzają, liczba testów 106 też, brak testu K2 potwierdzony.
- Wykryła też dwa problemy w samym handoffie: commit w nagłówku był nieaktualny (f61f08b zamiast HEAD aacb28b), a opis „drzewo czyste” był fałszywy.
- Wykonała następny krok, czyli test K2. Diff i wynik 109/0 sprawdziłem sam.
- Czego zabrakło w handoffie: ostrzeżenia o ścisłym Mockito (wczesny `return` daje `UnnecessaryStubbingException`) i lokalizacji kryterium K2. Zbędne były decyzje o F-03 do F-06 i duplikat informacji z CLAUDE.md.
- Uwaga: sesja zmieniła stub w `setUp` na `lenient()` dla całej klasy, co osłabia strict stubs w pozostałych testach. To temat na review.

**13.** Maister z publicznego repo: HEAD = a1f05e0, wersja 2.2.6, czyli wersja referencyjna jest najnowsza. Sklonowany do `.training/plugins/maister` i ładowany przez `--plugin-dir`. Init potwierdza `maister@inline 2.2.6`; globalny 2.2.3 nie był ładowany.
- Komenda: `/maister:quick-bugfix`. Zgłoszenie: podwójna rezerwacja przez offset, finding F-01 z drugiego przypadku w ćw. 4. Praca w osobnej kopii repo.
- Bramki w kodzie skilla:
  - EnterPlanMode z ExitPlanMode jako obowiązkowa bramka planu;
  - AskUserQuestion przy eskalacji i przy „test nie zawodzi”;
  - TDD red/green, maksymalnie 3 iteracje.
- W `-p` nie ma narzędzi EnterPlanMode, ExitPlanMode ani AskUserQuestion. Maister zatrzymał się z planem i pytaniem w tekście, bez zmian w kodzie. Bramki nie obchodziłem.
- **Decyzję podjął człowiek** (użytkownik przez AskUserQuestion): „Obie metody” (`artifacts/ex13-gate-decision.md`).
- Po decyzji: drugi przebieg z zapisem sesji dał ten sam plan, potem `--resume` z decyzją człowieka.
- Wynik: `withZoneSameInstant` w `ensureNoConflicts` i `isSlotAvailable` oraz nowy `AppointmentServiceTest` (3 testy).
- Sprawdziłem sam:
  - po cofnięciu poprawki 2 z 3 testów czerwone;
  - po poprawce 112/112;
  - na żywym API (port 18183) rezerwacja 09:10Z na zajęty termin kończy się kolizją, przed poprawką była SCHEDULED.
- Porównanie z własnym workflow:
  - **Narzut i czas:** quick-bugfix to lekki wariant Maister. Bez `.maister/docs` (brak `/maister:init`) nie ma standardów; korzysta z CLAUDE.md i nie tworzy `.maister/tasks`. Czas i koszt są podobne do własnego pipeline z ćw. 6: Maister ~200 s pracy i $1.09 (z powtórzonym planem), własny pipeline ~254 s i $1.23. Ciężki wariant (`/maister:development`, 14 faz, wielu subagentów) nie był mierzony.
  - **Gotowe od razu:** bramka planu, TDD i eskalacja.
  - **Własny prosty workflow daje:** stały kontrakt `analysis.md` z ID, wybór findings przez człowieka i niezależnego reviewera.
  - **Alternatywy:** pojedynczy skill `analyze` albo reviewer jako subagent.
  - **Wniosek:** dla szkolenia pokazać Maister jako cięższe rozwiązanie z gotowymi bramkami. W `-p` bramki planu działają tylko „tekstowo”.
- **Do przećwiczenia z człowiekiem:** to samo w sesji interaktywnej (prawdziwe ExitPlanMode) oraz `/maister:init` (koszt nie mierzony).

## Ustalenia o narzędziach (ważne dla prowadzącego)
- `--setting-sources project,local` NIE wyłącza konektorów claude.ai: Todoist i Docs były widoczne w sesji. Trzeba dodać `--strict-mcp-config`.
- W nieufanym workspace `permissions.allow` z `.claude/settings.json` jest ignorowane, a hooki projektu się wykonują. Zaufanie jest zapisane dla ścieżki głównego repo w `~/.claude.json`, którego nie zmieniałem. Uprawnienia dla skilli trzeba dawać przez `allowed-tools` w skillu, a w headless przez `--allowedTools`.
- Reguły ścieżek dla zapisu działają jako `Edit(...)`. Reguła `Write(...)` nie zadziałała.

## Czas próby (agent)
Ćw. 1–10 razem z inwentarzem zajęły 16:53–17:26, ćw. 11–13 zajęły 19:01 do ok. 19:35. Pomiędzy była przerwa na limit sesji.
Świeże sesje `claude -p` to łącznie ok. 21 min pracy modelu i ok. $8.9 według CLI. Kosztu głównej sesji nie mierzyłem.
To nie jest czas grupy.

## Zgodność z programem Bottegi
Źródło: https://bottega.com.pl/szkolenie-AI-Assisted-SDLC, sprawdzone 2026-11-13. Strona podaje: „2 dni”, „60% praktyki / 40% teorii”, program jako ramę ustalaną z grupą. W terminie otwartym zajęcia trwają 9–17.
Wymóg: 2 × 405 min = 810 min zajęć, z czego 60% to 486 min praktyki uczestników.

### Szacunek dla grupy (SZACUNEK, nie pomiar)
„Praktyka” to aktywna praca uczestnika: pisanie promptów, sprawdzanie wyników, testy, poprawki. Czas oczekiwania na model zmierzyłem w próbie i podaję go osobno.

| Blok | Ćwiczenia | Teoria/omówienie | Praktyka | w tym oczekiwanie na model (zmierzone w próbie) |
|---|---|---|---|---|
| D1 B1 (105) | mapa SDLC, demo, ćw. 1 | 70 | 35 | ~1 min |
| D1 B2 (105) | ćw. 2 CLAUDE.md | 45 | 60 | <1 min (+ ~1 min mvnw) |
| D1 B3 (105) | ćw. 3 Plan Mode + bugfix | 35 | 70 | ~1 min |
| D1 B4 (90) | ćw. 4 skill, ćw. 5 reviewer | 25 | 65 | ~4 min |
| D2 B1 (105) | recap, ćw. 6 pipeline, ćw. 7 LSP, ćw. 8 MCP | 25 | 80 | ~6 min (+ indeksowanie jdtls) |
| D2 B2 (105) | ćw. 9 hook + Checkstyle | 35 | 70 | <1 min (+ 5–8 s hooka na edycję) |
| D2 B3 (105) | ćw. 10 workflow, ćw. 11 standardy, ćw. 12 handoff | 20 | 85 | ~10 min |
| D2 B4 (90) | ćw. 13 Maister + porównanie | 40 | 50 | ~4 min |
| **Razem** | | **295** | **515** (63,6%) | **~28 min** |

- 515 min to więcej niż wymagane 486, ale zapas wynosi tylko 29 min. Po odjęciu czasu oczekiwania na model zostaje ~487 min, czyli na granicy wymogu.
- Dzień 1 ma 230 min praktyki (57%). Wymóg spełnia dopiero dzień 2: 285 min (70%).
- Największe ryzyka:
  - pierwsze pobranie zależności Maven (2–5 min);
  - instalacja jdtls;
  - port 8080 (frontend ma zaszyty adres);
  - Node 26 wymaga obejścia;
  - Docker dla testów `*IT`.

  Każde z nich może zjeść zapas, dlatego potrzebny jest wcześniejszy setup: JDK, Docker, `npm ci`, jdtls i jeden przebieg `./mvnw -B test`.

### Rozpiska bloków: teoria (T) i praktyka (P)
**Dzień 1** (T175 / P230)

| Godzina | Typ | Min | Treść |
|---|---|---|---|
| 09:00–09:35 | T | 35 | Mapa SDLC, procesy uczestników, pokaz aplikacji |
| 09:35–10:10 | P | 35 | Ćw. 1: rozmowa o aplikacji |
| 10:10–10:45 | T | 35 | Omówienie ćw. 1; model, agent, harness, kontekst |
| 11:00–11:25 | T | 25 | Kontekst; CLAUDE.md, settings, skille, agenci |
| 11:25–12:25 | P | 60 | Ćw. 2: CLAUDE.md |
| 12:25–12:45 | T | 20 | Zmyślone polecenia, granice CLAUDE.md |
| 13:30–13:50 | T | 20 | Wymaganie, kryteria, Plan Mode |
| 13:50–15:00 | P | 70 | Ćw. 3: bugfix z planem |
| 15:00–15:15 | T | 15 | Omówienie red/green i diffu |
| 15:30–15:45 | T | 15 | Skill a subagent, kontrakt artefaktu |
| 15:45–16:20 | P | 35 | Ćw. 4: skill analizy |
| 16:20–16:50 | P | 30 | Ćw. 5: subagent review |
| 16:50–17:00 | T | 10 | Podsumowanie dnia |

**Dzień 2** (T120 / P285)

| Godzina | Typ | Min | Treść |
|---|---|---|---|
| 09:00–09:10 | T | 10 | Recap |
| 09:10–09:55 | P | 45 | Ćw. 6: wspólny pipeline |
| 09:55–10:05 | T | 10 | LSP, grep, linter; MCP |
| 10:05–10:25 | P | 20 | Ćw. 7: LSP |
| 10:25–10:40 | P | 15 | Ćw. 8: MCP |
| 10:40–10:45 | T | 5 | Wnioski |
| 11:00–11:25 | T | 25 | Hooki, PostToolUse nie cofa zmian, bezpieczeństwo skryptu |
| 11:25–12:35 | P | 70 | Ćw. 9: hook + linter Java |
| 12:35–12:45 | T | 10 | Omówienie |
| 13:30–13:40 | T | 10 | Workflow SDLC, refaktoring bez zmiany zachowania |
| 13:40–14:25 | P | 45 | Ćw. 10: workflow + refaktoring |
| 14:25–14:50 | P | 25 | Ćw. 11: bez/ze standardami |
| 14:50–15:05 | P | 15 | Ćw. 12: świeża sesja / handoff |
| 15:05–15:15 | T | 10 | Porównanie standardów, handoff |
| 15:30–15:45 | T | 15 | Maister: bramki, koszt |
| 15:45–16:35 | P | 50 | Ćw. 13: Maister |
| 16:35–17:00 | T | 25 | Porównanie z własnym workflow, zamknięcie |

Razem T295 / P515 (szacunek).

### Uproszczenia, żeby zmieścić się w blokach
- **Ćw. 11:** pary zamiast dwóch sesji na osobę. Połowa grupy robi wariant A, połowa B, na tej samej bazie i z tym samym promptem. Porównanie robimy na forum. Oszczędza to ok. 15 min i od razu daje więcej niż jedną parę prób.
- **Ćw. 7 i 8:** jdtls i `.mcp.json` przygotowane wcześniej. Na zajęciach tylko jedno wywołanie LSP i jedno pytanie do Context7.
- **Ćw. 6:** kontrakt skilla `analyze` z ćw. 4 używany bez zmian, bez kolejnej iteracji.
- **Ćw. 13:** tylko `/maister:quick-bugfix` na jednym zgłoszeniu, bez `/maister:init` i bez `development`. Cięższe warianty prowadzący pokazuje jako demo.

### Rozbieżności z ramą programu
- **Ćwiczenia wykraczają poza ramę:** LSP, hooki, skille i Maister nie występują w ramie (Maister jest w osobnym programie „AI-SDLC - Claude + Maister”). Wymaga to uzgodnienia w analizie przed szkoleniem.
- **Punkty ramy bez osobnego ćwiczenia:**
  - @notation;
  - Context poisoning;
  - Context Window Management;
  - STATUS.md (częściowo pokrywa go handoff z ćw. 12);
  - Todo Lists;
  - Batch operations;
  - Version control z AI;
  - Effective Prompting;
  - kultura zespołu.

  Większość można wpleść w omówienia. Version control i @notation warto wpleść w ćw. 3 i 6, np. commit z opisem przez Claude i odwołania `@plik` w promptach.

## Rekomendacje i uproszczenia
- Wspólny scenariusz: BillingRulesEngine (W-01 → F-01/F-02 → R-02) daje czytelne red/green w kilka minut. Przypadek „podwójna rezerwacja przez offset” warto mieć jako bardziej realistyczny wariant P1.
- Dzień 2, blok 1: LSP przez lokalny jdtls trzeba przygotować przed szkoleniem, bo pobranie i indeksowanie trwa. Najlepiej przygotować skrypt instalacyjny. Context7 pokazać jako MCP do dokumentacji, z uczciwym brakiem pokrycia.
- Hook dokłada ~5–8 s do każdej edycji Javy (mvnw). Dla grupy wystarczy jedna reguła.
- Czasy grupy to szacunki; pomiar wymaga próby z ludźmi.
- Prowadzący musi przećwiczyć z człowiekiem: interaktywny Plan Mode i zatwierdzanie planu, dialog zaufania workspace, Maister w sesji interaktywnej i `/maister:init`.
- W headless `CLAUDE_CODE_PLUGIN_DIRS` z otoczenia (Orca) dokłada plugin `orca-agent-view` do każdej sesji. Na laptopach uczestników tego nie będzie.

## Reset
`git reset --hard 12886dd` w tym worktree, usunięcie `.training/` i `.pipeline/`. Kopie ćw. 11 i 13 leżą w scratchpadzie sesji (poprawka Maister nie trafiła do worktree; diff w `ex13-maister-diff.patch`). Backend 18180 (mój proces) zatrzymany.
