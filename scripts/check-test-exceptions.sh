#!/bin/bash
set -euo pipefail

if [ "$#" -eq 0 ]; then
    echo "Usage: $0 <command> [args...]" >&2
    exit 2
fi

LOG_FILE="/tmp/derbent-test-exceptions.log"
rm -f "$LOG_FILE"

"$@" 2>&1 | tee "$LOG_FILE"
exit "${PIPESTATUS[0]}"
