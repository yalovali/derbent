#!/bin/bash

# Start ngrok tunnels so Codex running in the sandbox can reach the local
# Derbent application (HTTP) and PostgreSQL instance. Run this on the desktop
# before starting a Codex CLI session and keep it alive in its own terminal.

set -euo pipefail

# Resolve repository root to support running the script from any location.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# Default ports/hosts can be overridden before invoking the script, e.g.:
#   APP_PORT=8181 PG_PORT=5433 ./start-ngrok-tunnels.sh
APP_HOST="${APP_HOST:-127.0.0.1}"
APP_PORT="${APP_PORT:-8080}"
PG_HOST="${PG_HOST:-127.0.0.1}"
PG_PORT="${PG_PORT:-5432}"
NGROK_BIN="${NGROK_BIN:-ngrok}"
CONFIG_DIR="${CONFIG_DIR:-${REPO_ROOT}/.codex}"
CONFIG_FILE="${CONFIG_DIR}/ngrok.yml"

echo "🛠  Preparing ngrok tunnels for Derbent Codex session"

if ! command -v "${NGROK_BIN}" >/dev/null 2>&1; then
	echo "❌ ngrok binary not found. Install from https://ngrok.com/download and ensure it is on PATH." >&2
	exit 1
fi

# Validate ngrok token configuration (warn if missing).
if ! "${NGROK_BIN}" config check >/dev/null 2>&1; then
	if [[ -z "${NGROK_AUTHTOKEN:-}" ]]; then
		cat >&2 <<'EOF'
⚠️  ngrok authtoken not configured.
    Run `ngrok config add-authtoken <token>` once (token available in your ngrok dashboard)
    or export NGROK_AUTHTOKEN before running this script.
EOF
		exit 1
	fi
fi

mkdir -p "${CONFIG_DIR}"

echo "📄 Writing temporary ngrok configuration to ${CONFIG_FILE}"

{
	echo "version: 2"
	if [[ -n "${NGROK_AUTHTOKEN:-}" ]]; then
		echo "authtoken: ${NGROK_AUTHTOKEN}"
	fi
	cat <<EOF
tunnels:
  derbent-app:
    proto: http
    addr: ${APP_HOST}:${APP_PORT}
    schemes: [https]
  derbent-postgres:
    proto: tcp
    addr: ${PG_HOST}:${PG_PORT}
EOF
} >"${CONFIG_FILE}"

echo ""
echo "🚀 Launching ngrok with HTTP (${APP_HOST}:${APP_PORT}) and TCP (${PG_HOST}:${PG_PORT}) tunnels"
echo "    👉 Leave this terminal open; press Ctrl+C when Codex work is finished."
echo ""

exec "${NGROK_BIN}" start --config "${CONFIG_FILE}" --all
