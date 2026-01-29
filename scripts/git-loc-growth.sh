#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Keep as a thin alias to the unified report script.
exec "${SCRIPT_DIR}/project-report.sh" "$@"
