---
name: implement
description: Uruchamia subagenta implementer na zatwierdzonym .pipeline/plan.md i pokazuje wynik z .pipeline/implementation.md.
disable-model-invocation: true
---

Uruchom subagenta `implementer`. Przekaż mu tylko: „Wykonaj .pipeline/plan.md, kontekst w .pipeline/analysis.md”.
Po powrocie przeczytaj `.pipeline/implementation.md` i sam uruchom `git diff --stat`, żeby potwierdzić, że zmienione pliki zgadzają się z planem. Wypisz: ID, wynik red/green, wynik pełnych testów, zgodność diffu z planem.
