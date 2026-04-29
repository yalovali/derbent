Telegram AI Agent Bridge
======================

Overview
--------
Bu servis Telegram üzerinden Codex, Copilot veya JSON'da tanımlanan başka AI CLI ajanlarına prompt gönderir. Güvenlik için izin listesi, whitelist, admin bayrağı ve token env değişkeni gereklidir.

Quickstart
----------
1. Set the bot token outside git:
   - `telegram_service/.env` içinde `TELEGRAM_BOT_TOKEN=...` tanımla
   - token değerini JSON'a koyma

2. Edit config file: telegram_service/telegram_config.json
   - keep telegram_token_env as the env var name
   - add your Telegram numeric user id to allowed_user_ids and admin_user_ids
   - verify working_dir (default: /home/yasin/git/derbent)
   - choose default_agent and configure agents.codex / agents.copilot
   - add custom_prompts aliases such as hi, durum, /durum, raporla
   - exec_mode: "auto" (currently set to auto based on your choice)
   - allow_unsafe_exec: false (set true only for admin and with care)

3. Install dependencies (recommended virtualenv):

   python3 -m pip install --user -U python-telegram-bot

4. Install or restart the service:

   sudo ./telegram_service/install_service.sh
   sudo systemctl restart telegram_service.service

5. Check logs in telegram_service/logs/telegram_bot.log and telegram_service/logs/ai_prompts.log

Commands
--------
- /prompt [agent] <text> -> sends a prompt to default or selected AI agent
- /agent <agent> <text>  -> sends a prompt to a specific AI agent
- /task [agent] <text>   -> starts a long-running background AI task
- /taskstatus            -> shows background task output
- /agents                -> lists configured agents
- /run <cmd>      -> executes command subject to whitelist / admin override
- /logs <n>       -> admin only: tail logs
- /status         -> show service status
- custom aliases  -> JSON-defined prompts, for example hi, durum, /durum, raporla

Security
--------
- Token must come from TELEGRAM_BOT_TOKEN or the env var named by telegram_token_env.
- By default only users in allowed_user_ids can interact.
- Admins (admin_user_ids) can bypass whitelist only if allow_unsafe_exec=true.
- Exec mode is set to auto per your choice; edit config to change to dry-run.

Notes
-----
- This service calls local AI CLI tools configured under agents.
- Running with auto-exec is powerful and dangerous. Keep allowed_user_ids and admin_user_ids secure.

Next steps I can do for you
--------------------------
- Make the control script executable and start the service now
- Add systemd unit installation commands
- Wire an opt-in flow to enable allow_unsafe_exec per admin request
