#!/usr/bin/env bash
set -euo pipefail

# Installs a systemd service unit for telegram_service.
# Usage:
#   ./install_service.sh
# The script expects a unit file named telegram_service.service to be located in the
# same directory as this script. It will copy it to /etc/systemd/system and enable/start it.
# For safety, if not run as root the script prints the commands to run with sudo instead of executing them.

SERVICE_NAME="telegram_service.service"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC="$SCRIPT_DIR/$SERVICE_NAME"
DST="/etc/systemd/system/$SERVICE_NAME"

if [ ! -f "$SRC" ]; then
  echo "Unit file not found: $SRC"
  echo "Please create or place $SERVICE_NAME next to this script and re-run."
  exit 1
fi

if [ "$EUID" -ne 0 ]; then
  echo "This script requires root to install the unit. Run the following commands with sudo:"
  echo
  echo "sudo cp \"$SRC\" \"$DST\""
  echo "sudo systemctl daemon-reload"
  echo "sudo systemctl enable $SERVICE_NAME"
  echo "sudo systemctl start $SERVICE_NAME"
  echo
  echo "If you prefer to inspect before running, run the commands above manually."
  exit 0
fi

# Running as root: perform installation
cp "$SRC" "$DST"
systemctl daemon-reload
systemctl enable "$SERVICE_NAME"
systemctl start "$SERVICE_NAME"

echo "$SERVICE_NAME installed and started."