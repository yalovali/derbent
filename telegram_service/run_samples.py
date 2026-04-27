"""
Sample runner for telegram_service handlers.

This script imports TelegramService from telegram_bot, constructs fake Update and Context
objects, calls the handler methods (handle_prompt, handle_run, handle_status, handle_logs,
unknown_handler), prints outputs to the console, and attempts to verify that a prompts
log file exists after running handle_prompt.

This script is intentionally defensive so it can be used as a smoke test without
requiring a live Telegram bot or systemd service.
"""

import os
import sys
import time
import asyncio
from types import SimpleNamespace
import json

# Try importing TelegramService from common possible module locations.
try:
    # Preferred: repository top-level module telegram_bot
    from telegram_bot import TelegramService
except Exception:
    # Try relative import if this package is part of a package
    try:
        from .telegram_bot import TelegramService  # type: ignore
    except Exception as e:
        print("Failed to import TelegramService from telegram_bot:", e, file=sys.stderr)
        raise


class FakeUser:
    def __init__(self, id=12345, username="sample_user"):
        self.id = id
        self.username = username


class FakeMessage:
    def __init__(self, text="/start", from_user=None):
        self.text = text
        self.from_user = from_user or FakeUser()

    async def reply_text(self, txt: str):
        print('BOT_REPLY:', txt)


class FakeUpdate:
    def __init__(self, text="/start", user=None):
        self.effective_user = user or FakeUser()
        self.message = FakeMessage(text=text, from_user=self.effective_user)


class FakeContext:
    def __init__(self):
        # Handlers often expect args/kwargs or bot attribute; provide a simple namespace
        self.args = []
        self.bot = SimpleNamespace()


def ensure_prompts_log_exists(candidate_paths):
    for p in candidate_paths:
        if os.path.exists(p):
            return p
    return None


async def main():
    print("Creating TelegramService instance...")
    cfg_path = os.path.join(os.path.dirname(__file__), 'telegram_config.json')
    cfg = {}
    try:
        with open(cfg_path, 'r') as f:
            cfg = json.load(f)
    except Exception as e:
        print('Could not load config, using empty config:', e)
    svc = TelegramService(cfg)

    print("Preparing fake update/context objects...")
    upd_prompt = FakeUpdate(text="/prompt This is a test prompt")
    upd_run = FakeUpdate(text="/run sample_job")
    upd_status = FakeUpdate(text="/status sample_job")
    upd_logs = FakeUpdate(text="/logs sample_job")
    upd_unknown = FakeUpdate(text="/nope unknown")

    ctx = FakeContext()

    # Call handlers if they exist on the service. Print output or exceptions.
    handlers = [
        ("handle_prompt", upd_prompt),
        ("handle_run", upd_run),
        ("handle_status", upd_status),
        ("handle_logs", upd_logs),
        ("unknown_handler", upd_unknown),
    ]

    for name, upd in handlers:
        h = getattr(svc, name, None)
        print('\n--- Calling %s ---' % name)
        if not callable(h):
            print("Handler %s not found on TelegramService" % name)
            continue
        try:
            # Many handlers accept (update, context). Some return values, some send messages.
            result = h(upd, ctx)
            if asyncio.iscoroutine(result):
                await result
                print("Returned: coroutine awaited")
            else:
                print("Returned:", repr(result))
        except TypeError:
            # Maybe handler expects different signature; try calling with only update
            try:
                result = h(upd)
                if asyncio.iscoroutine(result):
                    await result
                    print("Returned (single-arg): coroutine awaited")
                else:
                    print("Returned (single-arg):", repr(result))
            except Exception as e:
                print("Handler raised:", type(e).__name__, e)
        except Exception as e:
            print("Handler raised:", type(e).__name__, e)

    # Try to discover a prompts log file. Many services write prompts to a file named prompts.log
    # or to a path exposed on the service instance. Check common locations.
    print("\n--- Verifying prompts log exists ---")
    candidate_paths = []

    # 1) If the service exposes an attribute with common names, check those
    for attr in ("prompts_log", "prompts_path", "prompts_file", "prompts_log_path"):
        if hasattr(svc, attr):
            val = getattr(svc, attr)
            if isinstance(val, str):
                candidate_paths.append(val)

    # 2) Common filenames in current directory
    for name in ("prompts.log", "prompts.txt", "prompts.json", "prompts.csv"):
        candidate_paths.append(os.path.join(os.getcwd(), name))

    # 3) Common filename next to this script / service package
    base_dir = os.path.dirname(__file__)
    candidate_paths.append(os.path.join(base_dir, "prompts.log"))

    found = ensure_prompts_log_exists(candidate_paths)
    if found:
        print("Prompts log found at:", found)
    else:
        print("No prompts log found in candidate locations:")
        for p in candidate_paths:
            print(" -", p)

    print("\nDone. Note: this script only simulates handler invocation and checks for a prompts log file.")


if __name__ == "__main__":
    asyncio.run(main())
