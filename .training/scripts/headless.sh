#!/usr/bin/env bash
# Świeża, nieinteraktywna sesja Claude Code z ograniczonymi narzędziami.
# Użycie: headless.sh <nazwa-dowodu> <narzędzia, np. "Read,Glob,Grep"> <plik-z-promptem> [dodatkowe flagi claude]
set -euo pipefail
name="$1"; tools="$2"; prompt_file="$3"; shift 3
root="$(git rev-parse --show-toplevel)"
out="$root/.training/evidence/$name"
start=$(date +%s)
claude -p "$(cat "$prompt_file")" \
  --setting-sources project,local \
  --tools "$tools" \
  --no-session-persistence --strict-mcp-config \
  --output-format stream-json --verbose \
  "$@" < /dev/null > "$out.jsonl" 2> "$out.stderr" || echo "claude exit=$?" >> "$out.stderr"
end=$(date +%s)
python3 -I - "$out.jsonl" "$out" "$((end-start))" <<'PY'
import json, sys
path, out, sec = sys.argv[1], sys.argv[2], sys.argv[3]
tools, result = [], None
for line in open(path):
    try: ev = json.loads(line)
    except ValueError: continue
    if ev.get("type") == "assistant":
        for c in ev["message"].get("content", []):
            if c.get("type") == "tool_use":
                tools.append({"tool": c["name"], "input": c["input"]})
    if ev.get("type") == "result":
        result = ev
open(out + ".result.md", "w").write((result or {}).get("result") or "BRAK WYNIKU")
summary = {"wall_sec": int(sec), "tool_calls": tools}
if result:
    for k in ("num_turns", "duration_ms", "total_cost_usd", "is_error", "usage", "modelUsage"):
        summary[k] = result.get(k)
json.dump(summary, open(out + ".summary.json", "w"), indent=1, ensure_ascii=False)
print(f"{out}: sec={sec} turns={summary.get('num_turns')} cost_usd={summary.get('total_cost_usd')} tool_calls={len(tools)}")
PY
