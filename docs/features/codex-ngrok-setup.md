# Codex Desktop Ngrok Preparation

Run these helpers on your desktop **before** opening a Codex CLI session so the sandbox can access your local Derbent services.

1. **Start the tunnels**

   ```bash
   ./scripts/codex-prep/start-ngrok-tunnels.sh
   ```

   This script:
   - Verifies `ngrok` is installed and your authtoken is configured.
   - Starts an HTTPS tunnel to `http://127.0.0.1:8080` (the Spring Boot app).
   - Starts a TCP tunnel to `127.0.0.1:5432` (PostgreSQL).

   Keep this terminal window open; the tunnels stay active until you press `Ctrl+C`.

   Override defaults when needed:

   ```bash
   APP_PORT=8181 PG_PORT=6432 ./scripts/codex-prep/start-ngrok-tunnels.sh
   ```

2. **Capture the public endpoints**

   In a second terminal, run:

   ```bash
   ./scripts/codex-prep/capture-ngrok-endpoints.sh
   ```

   The script queries the local ngrok API, extracts the public URLs, and writes them to `.codex/ngrok-endpoints.env`. Leave this file in the repository so Codex can read the latest endpoints.

3. **Share the values with Codex**

   Make the generated env file available to the assistant (for example, by committing it or leaving it in the workspace). Codex scripts can then read:

   - `CODEX_APP_BASE_URL` — HTTPS entrypoint to the running app.
   - `CODEX_DB_REMOTE_URL`, `CODEX_DB_HOST`, `CODEX_DB_PORT` — TCP forwarding details for PostgreSQL.

4. **Shutdown**

   After finishing the Codex session, stop the tunnels with `Ctrl+C` in the first terminal and delete `.codex/ngrok-endpoints.env` if you no longer need it.
