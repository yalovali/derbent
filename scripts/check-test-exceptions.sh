#!/usr/bin/env bash

set -euo pipefail

LOG_FILE="${DERBENT_TEST_EXCEPTIONS_LOG:-/tmp/derbent-test-exceptions.log}"
EXCEPTION_PATTERNS="${EXCEPTION_PATTERNS:-\\bERROR\\b|Exception|CRITICAL|FATAL|BindingException|RuntimeException}"

: >"$LOG_FILE"

# Run the given command and capture output to a stable log file.
"$@" 2>&1 | tee "$LOG_FILE"
CMD_STATUS=${PIPESTATUS[0]}

# Fail if the command failed or if we detected exception patterns.
if [ "$CMD_STATUS" -ne 0 ]; then
  exit "$CMD_STATUS"
fi

if grep -En "$EXCEPTION_PATTERNS" "$LOG_FILE" >/dev/null; then
  echo "Detected exception/error patterns in $LOG_FILE:" >&2
  grep -En "$EXCEPTION_PATTERNS" "$LOG_FILE" | tail -n 200 >&2
  exit 1
fi
