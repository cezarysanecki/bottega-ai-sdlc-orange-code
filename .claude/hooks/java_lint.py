"""PostToolUse: po edycji pliku *.java w backend/src uruchamia Checkstyle tylko dla tego pliku.

Wejście: JSON zdarzenia na stdin. Naruszenia -> exit 2 + stderr (Claude widzi komunikat;
zmiana już jest zapisana, hook jej nie cofa). Inne pliki -> exit 0 bez działania.
"""
import json
import os
import re
import subprocess
import sys

event = json.load(sys.stdin)
path = (event.get("tool_input") or {}).get("file_path") or ""
project = os.path.realpath(os.environ.get("CLAUDE_PROJECT_DIR") or event.get("cwd") or ".")
backend = os.path.join(project, "backend")

if not path.endswith(".java"):
    sys.exit(0)
path = os.path.realpath(os.path.join(event.get("cwd") or project, path))

source_root = None
for root in ("src/main/java", "src/test/java"):
    candidate = os.path.join(backend, root) + os.sep
    if path.startswith(candidate):
        source_root = candidate
if source_root is None:
    sys.exit(0)

relative = path[len(source_root):]
if not re.fullmatch(r"[A-Za-z0-9_/.$-]+\.java", relative):
    print(f"java-lint: pominięto plik o nietypowej nazwie: {relative}", file=sys.stderr)
    sys.exit(0)

result = subprocess.run(
    ["./mvnw", "-B", "-q", "checkstyle:check", f"-Dcheckstyle.includes={relative}"],
    cwd=backend, capture_output=True, text=True, timeout=180,
)
if result.returncode == 0:
    print(json.dumps({"hookSpecificOutput": {"hookEventName": "PostToolUse",
        "additionalContext": f"Checkstyle: brak naruszeń w {relative}."}}))
    sys.exit(0)

violations = [line for line in result.stdout.splitlines() if "[UnusedImports]" in line or ".java:" in line and "[ERROR]" in line]
message = "\n".join(dict.fromkeys(violations)) or result.stdout[-1500:]
print(f"Checkstyle zgłosił naruszenia w {relative} (zmiana jest już zapisana):\n{message}", file=sys.stderr)
sys.exit(2)
