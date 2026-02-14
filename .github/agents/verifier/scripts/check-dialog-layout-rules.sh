#!/bin/bash
# Dialog layout overflow rule checker
# Enforces safe width/overflow patterns for dialog CDiv content

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
cd "$PROJECT_ROOT"

if ! command -v rg >/dev/null 2>&1; then
  echo "❌ rg is required for this script"
  exit 2
fi

violations=0

echo "🔎 Checking dialog layout overflow rules..."
echo ""

# Rule 1: Scrollable dialog result/text areas should use createScrollableResultArea()
# If a dialog file creates CDiv and uses overflow/max-height directly without helper, flag it.
while IFS= read -r file; do
  if rg -q "new CDiv\\(" "$file" && rg -q "overflow-y|max-height" "$file" && ! rg -q "createScrollableResultArea\\(" "$file"; then
    echo "❌ Rule 1 violation: $file"
    echo "   Dialog has custom CDiv scroll/height styling without createScrollableResultArea(...)"
    violations=$((violations + 1))
  fi
done < <(find src/main/java -type f -name "*.java" \( -path "*/dialogs/*" -o -path "*/view/dialog/*" \) | sort)

# Rule 2: Raw Div containers in dialogs with width + padding/border must set box-sizing/min-width safeguards
while IFS= read -r file; do
  if rg -q "new Div\\(" "$file" \
    && rg -q "setWidthFull\\(|setWidth\\(\"100%\"" "$file" \
    && rg -q "set\\(\"padding\"|set\\(\"border\"" "$file" \
    && ! rg -q "box-sizing|min-width\", \"0\"" "$file"; then
    echo "❌ Rule 2 violation: $file"
    echo "   Raw Div with width+padding/border found without box-sizing/min-width safeguards"
    violations=$((violations + 1))
  fi
done < <(find src/main/java -type f -name "*.java" \( -path "*/dialogs/*" -o -path "*/view/dialog/*" \) | sort)

echo ""
if [ "$violations" -eq 0 ]; then
  echo "✅ PASS: No dialog layout overflow rule violations found"
  exit 0
fi

echo "❌ FAIL: Found $violations dialog layout overflow rule violation(s)"
exit 1
