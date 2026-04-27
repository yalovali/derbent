Telegram Copilot Bridge
======================

Overview
--------
Bu küçük servis Telegram üzerinden gelen komutları "Copilot" için prompt olarak kaydeder ve (konfigürasyona bağlı olarak) sistem üzerinde komutları yürütür. Güvenlik için izin listesi, whitelist ve admin bayrağı gereklidir.

Quickstart
----------
1. Edit config file: config/telegram_config.json
   - set telegram_token (Bot token)
   - add your Telegram numeric user id to allowed_user_ids and admin_user_ids
   - verify working_dir (default: /home/yasin/git/derbent)
   - exec_mode: "auto" (currently set to auto based on your choice)
   - allow_unsafe_exec: false (set true only for admin and with care)

2. Install dependencies (recommended virtualenv):

   python3 -m pip install --user -U python-telegram-bot

3. Make control script executable and start:

   chmod +x bin/telegram-botctl
   bin/telegram-botctl start

4. Check logs in logs/telegram_bot.log and logs/copilot_prompts.log

Commands
--------
- /prompt <text>  -> saves prompt to logs/copilot_prompts.log
- /run <cmd>      -> executes command subject to whitelist / admin override
- /logs <n>       -> admin only: tail logs
- /status         -> show service status

Security
--------
- By default only users in allowed_user_ids can interact.
- Admins (admin_user_ids) can bypass whitelist only if allow_unsafe_exec=true.
- Exec mode is set to auto per your choice; edit config to change to dry-run.

Notes
-----
- This template does NOT call external AI endpoints by default. To forward prompts to an assistant endpoint set copilot_endpoint in config (e.g., local endpoint) but validate credentials and privacy.
- Running with auto-exec is powerful and dangerous. Keep allowed_user_ids and admin_user_ids secure.

Next steps I can do for you
--------------------------
- Make the control script executable and start the service now
- Add systemd unit installation commands
- Wire an opt-in flow to enable allow_unsafe_exec per admin request

