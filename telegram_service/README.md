Telegram AI Agent Bridge
======================

Overview
--------
Bu servis Telegram üzerinden Codex, Copilot veya JSON'da tanımlanan başka AI CLI ajanlarına prompt gönderir. Güvenlik için izin listesi, whitelist, admin bayrağı ve token env değişkeni gereklidir.

Quickstart
----------
1. Set the Telegram token outside git:

   ```bash
   cat > telegram_service/.env <<'EOF'
   TELEGRAM_BOT_TOKEN=your-telegram-bot-token
   EOF
   chmod 600 telegram_service/.env
   ```

2. Edit `telegram_service/telegram_config.json`
   - keep `telegram_token_env` as the env var name; do not put the token in JSON
   - add your Telegram numeric user id to allowed_user_ids and admin_user_ids
   - verify working_dir (default: /home/yasin/git/derbent)
   - choose default_agent (`codex` or `copilot`)
   - add any number of `custom_prompts` you actually use
   - exec_mode: "auto" (currently set to auto based on your choice)
   - allow_unsafe_exec: false (set true only for admin and with care)

3. Install dependencies (recommended virtualenv):

   python3 -m pip install --user -U python-telegram-bot

4. Install or restart the systemd service:

   sudo ./telegram_service/install_service.sh
   sudo systemctl restart telegram_service.service

5. Check logs in `telegram_service/logs/telegram_bot.log` and `telegram_service/logs/ai_prompts.log`

Commands
--------
- /task <text>           -> starts a long-running background AI task with the active agent
- /task                  -> shows current task status
- /agent <agent>         -> changes the default AI agent
- /agent                 -> shows the current default agent status
- /status                -> shows the default agent and available agent names
- /top                   -> shows system stats and top processes
- /run <cmd>             -> executes command subject to whitelist / admin override
- /logs <n>              -> admin only: tail logs
- custom aliases         -> run JSON-defined prompts, for example `hi`

Security
--------
- The bot token must come from `TELEGRAM_BOT_TOKEN` or the env var named by `telegram_token_env`.
- `.env` is ignored by git; never commit Telegram tokens.
- By default only users in allowed_user_ids can interact.
- Admins (admin_user_ids) can bypass whitelist only if allow_unsafe_exec=true.
- Exec mode is set to auto per your choice; edit config to change to dry-run.
- Rotate the Telegram token if it was ever committed or shared.

Notes
-----
- `agents` is the reusable section for Codex, Copilot, or another CLI.
- `custom_prompts` can contain unlimited ready-to-run prompts with text and slash aliases.
- Running with auto-exec is powerful. Keep allowed_user_ids, admin_user_ids, and `.env` secure.
