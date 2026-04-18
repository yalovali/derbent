#!/bin/bash
# Derbent Verifier - Code Compliance Gate
# Default scope: only changed Java files (staged + working tree). Use --all for full scan.

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
cd "$PROJECT_ROOT"

if ! command -v rg >/dev/null 2>&1; then
  echo "❌ rg is required for this script"
  exit 2
fi

scan_all=false
run_compile=true

for arg in "$@"; do
  case "$arg" in
    --all) scan_all=true ;;
    --no-compile) run_compile=false ;;
  esac
done

collect_changed_java_files() {
  {
    git diff --name-only --diff-filter=ACMR
    git diff --cached --name-only --diff-filter=ACMR
  } | sort -u | rg '^src/main/java/.*\.java$' || true
}

collect_all_java_files() {
  find src/main/java -type f -name "*.java" | sort
}

mapfile -t files < <(collect_changed_java_files)
if $scan_all; then
  mapfile -t files < <(collect_all_java_files)
fi

if [ "${#files[@]}" -eq 0 ]; then
  echo "ℹ️  No changed Java files detected."
fi

echo "✅ Derbent verify-code gate"
echo "==========================="
echo "Scope: $([ "$scan_all" = true ] && echo all || echo changed)"
echo "Files: ${#files[@]}"
echo ""

violations=0

fail() {
  echo "❌ $1"
  violations=$((violations + 1))
}

pass() {
  echo "✅ $1"
}

check_autowired_fields() {
  if [ "${#files[@]}" -eq 0 ]; then
    pass "@Autowired field injection (skipped)"
    return
  fi

  # Field injection: @Autowired on fields (rough heuristic)
  # Excludes constructors by ignoring lines containing "public" with "(" on same/near lines.
  local matches
  matches=$(rg -n "@Autowired" "${files[@]}" || true)
  if [ -z "$matches" ]; then
    pass "No @Autowired usage"
    return
  fi

  echo "$matches"
  fail "Found @Autowired usage (constructor injection only)"
}

check_fully_qualified_derbent() {
  if [ "${#files[@]}" -eq 0 ]; then
    pass "Fully-qualified tech.derbent.* (skipped)"
    return
  fi

  local count
  count=$(rg -n "tech\.derbent\." "${files[@]}" \
    | rg -v "^[^:]+:[0-9]+:(import|package) " \
    | rg -v "System\.setProperty\(|Class\.forName\(|@MyMenu.*icon" \
    | wc -l)

  if [ "$count" -eq 0 ]; then
    pass "No fully-qualified tech.derbent.* references"
    return
  fi

  rg -n "tech\.derbent\." "${files[@]}" \
    | rg -v "^[^:]+:[0-9]+:(import|package) " \
    | rg -v "System\.setProperty\(|Class\.forName\(|@MyMenu.*icon" \
    | head -n 50

  fail "Found fully-qualified tech.derbent.* references (use imports)"
}

check_raw_types_heuristic() {
  if [ "${#files[@]}" -eq 0 ]; then
    pass "Raw types (skipped)"
    return
  fi

  # Heuristic: "extends CSomething" without "<" in same clause
  local matches
  matches=$(rg -n "extends C[A-Za-z0-9_]+\s*\{" "${files[@]}" || true)
  if [ -z "$matches" ]; then
    pass "No obvious raw-type extends detected"
    return
  fi

  echo "$matches" | head -n 50
  fail "Potential raw types detected (verify generics on base classes)"
}

check_entity_constants() {
  if [ "${#files[@]}" -eq 0 ]; then
    pass "Entity constants (skipped)"
    return
  fi

  local entity_files
  mapfile -t entity_files < <(printf "%s\n" "${files[@]}" | rg '/domain/.*\.java$' || true)
  if [ "${#entity_files[@]}" -eq 0 ]; then
    pass "Entity constants present (no domain entities changed)"
    return
  fi

  local missing=0
  for f in "${entity_files[@]}"; do
    if rg -q "public class C" "$f"; then
      for c in DEFAULT_COLOR DEFAULT_ICON ENTITY_TITLE_SINGULAR ENTITY_TITLE_PLURAL VIEW_NAME; do
        if ! rg -q "$c" "$f"; then
          echo "Missing $c: $f"
          missing=$((missing + 1))
        fi
      done
    fi
  done

  if [ "$missing" -eq 0 ]; then
    pass "Entity constants present (for changed domain entities)"
  else
    fail "Missing entity constants detected"
  fi
}

check_autowired_fields
check_fully_qualified_derbent
check_raw_types_heuristic
check_entity_constants

# Dialog layout rules (fast)
if [ -x .github/agents/verifier/scripts/check-dialog-layout-rules.sh ]; then
  echo ""
  echo "Running dialog layout overflow rules..."
  if ! bash .github/agents/verifier/scripts/check-dialog-layout-rules.sh; then
    violations=$((violations + 1))
  fi
fi

if $run_compile; then
  echo ""
  echo "Running compile gate (agents profile)..."
  if ! ./mvnw -q -Pagents -DskipTests clean compile; then
    violations=$((violations + 1))
  fi
fi

echo ""
if [ "$violations" -eq 0 ]; then
  echo "✅ PASS: verify-code gate"
  exit 0
fi

echo "❌ FAIL: $violations violation(s)"
exit 1
