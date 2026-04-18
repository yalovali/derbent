#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
python3 "$SCRIPT_DIR/kb_audit.py"

echo "✅ Generated docs/knowledge/_generated/cleanup-suggestions.md"