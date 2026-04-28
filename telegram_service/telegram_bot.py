#!/usr/bin/env python3
"""
Telegram Copilot Bot

Yapı:
- /prompt <mesaj>  : Copilot'u pseudo-terminal (PTY) ile çalıştırır.
- /task <mesaj>    : Uzun Copilot işini arka planda çalıştırır, timeout yoktur.
- /durum           : Arka plan task çıktısının son halini gösterir.
- /run <komut>     : Lokal Ubuntu shell komutu çalıştırır.
- selam/merhaba/hi : Kullanım bilgisini döner.
- normal mesaj     : Sadece bot adı geçerse Copilot'a gönderilir.

Not:
Copilot manuel terminalde izin isteyebildiği için burada pexpect ile PTY kullanıyoruz.
Bu, normal subprocess pipe kullanımına göre Copilot'un interactive davranışına daha yakındır.
"""

import asyncio
import json
import logging
import logging.handlers
import os
import pexpect
import shlex
import shutil
import sys
import time
from pathlib import Path
from typing import List, Optional

from telegram import Update
from telegram.ext import (
    ApplicationBuilder,
    ContextTypes,
    MessageHandler,
    CommandHandler,
    filters,
)

ROOT = Path(__file__).resolve().parent
CONFIG_PATH = ROOT / "telegram_config.json"

BOT_LOG = "telegram_bot.log"
PROMPT_LOG = "copilot_prompts.log"
TASK_LOG = "copilot_task.log"


def load_config() -> dict:
    if not CONFIG_PATH.exists():
        raise FileNotFoundError(f"Config file not found: {CONFIG_PATH}")
    return json.loads(CONFIG_PATH.read_text())


def setup_logging(log_dir: Path):
    log_dir.mkdir(parents=True, exist_ok=True)

    logger = logging.getLogger()
    logger.setLevel(logging.INFO)

    formatter = logging.Formatter("%(asctime)s %(levelname)s %(name)s - %(message)s")

    file_handler = logging.handlers.RotatingFileHandler(
        log_dir / BOT_LOG,
        maxBytes=5 * 1024 * 1024,
        backupCount=5,
    )
    file_handler.setFormatter(formatter)

    console_handler = logging.StreamHandler()
    console_handler.setFormatter(formatter)

    logger.handlers.clear()
    logger.addHandler(file_handler)
    logger.addHandler(console_handler)


def append_json_log(path: Path, data: dict):
    with path.open("a", encoding="utf-8") as f:
        f.write(json.dumps(data, ensure_ascii=False) + "\n")


def split_telegram(text: str, limit: int = 3900) -> List[str]:
    if not text:
        return ["Boş cevap."]
    return [text[i : i + limit] for i in range(0, len(text), limit)]


def tail_file(path: Path, max_chars: int = 3500) -> str:
    if not path.exists():
        return "Henüz task log yok."
    text = path.read_text(errors="replace")
    return text[-max_chars:] if text else "Task log boş."


def is_allowed_by_whitelist(cmd: List[str], whitelist: List[str]) -> bool:
    return bool(cmd) and cmd[0] in whitelist


class TelegramCopilotBot:
    def __init__(self, config: dict):
        self.config = config

        self.log_dir = ROOT / config.get("log_dir", "logs")
        setup_logging(self.log_dir)

        self.logger = logging.getLogger("telegram_copilot_bot")

        self.working_dir = config.get("working_dir", str(ROOT))
        self.allowed_users = set(config.get("allowed_user_ids", []))
        self.admin_users = set(config.get("admin_user_ids", []))

        self.exec_mode = config.get("exec_mode", "dry-run")
        self.allow_unsafe = bool(config.get("allow_unsafe_exec", False))
        self.whitelist = config.get("whitelist_commands", [])

        self.timeout = int(config.get("timeout", 3600))
        self.pty_idle_timeout = int(config.get("pty_idle_timeout", 15))

        self.bot_names = [
            name.lower()
            for name in config.get("bot_names", ["copilot", "agent", "bot", "lova"])
        ]

        self.copilot_path = config.get("copilot_path") or shutil.which("copilot") or "copilot"

        # Model ve argümanlar config'te ayrı tutulur.
        # Örnek:
        # "model": "gpt-5-mini"
        # "copilot_args": ["--allow-all", "--no-ask-user", "-p"]
        self.model = config.get("model", "").strip()
        self.copilot_args = config.get("copilot_args", ["-p"])

        self.task_process: Optional[pexpect.spawn] = None
        self.task_started_at: Optional[float] = None
        self.task_prompt: Optional[str] = None
        self.task_log_path = self.log_dir / TASK_LOG

    def build_env(self) -> dict:
        """
        systemd altında PATH genelde kısadır.
        node_bin_dir ekleyerek /usr/bin/env node hatasını çözeriz.
        """
        env = os.environ.copy()
        node_bin_dir = self.config.get("node_bin_dir")
        if node_bin_dir:
            env["PATH"] = f"{node_bin_dir}:{env.get('PATH', '')}"
        env["HOME"] = self.config.get("home", str(Path.home()))
        return env

    def user_allowed(self, user_id: int) -> bool:
        return user_id in self.allowed_users

    def is_admin(self, user_id: int) -> bool:
        return user_id in self.admin_users

    async def require_allowed(self, update: Update) -> bool:
        uid = update.effective_user.id
        if not self.user_allowed(uid):
            await update.message.reply_text("Unauthorized.")
            self.logger.warning("Unauthorized access attempt user_id=%s", uid)
            return False
        return True

    def build_safe_prompt(self, text: str) -> str:
        """
        Copilot'a verilen gerçek prompt burada hazırlanır.
        safety_prompt config dosyasından gelir.
        """
        safety_prompt = self.config.get("safety_prompt", "").strip()
        return f"{safety_prompt} {text}".strip()

    def build_copilot_command(self, text: str, include_prompt_as_arg: bool = True) -> List[str]:
        """
        Copilot komutunu üretir.

        Bazı Copilot sürümlerinde:
          copilot --model gpt-5-mini -p "prompt"

        Bazılarında model veya prompt yerleşimi farklı olabilir.
        Bu yapı config ile esnek bırakıldı.
        """
        safe_prompt = self.build_safe_prompt(text)

        args = [self.copilot_path]

        if self.model:
            args += ["--model", self.model]

        args += self.copilot_args

        if include_prompt_as_arg:
            args.append(safe_prompt)

        return args

    async def run_process_with_timeout(self, args: List[str], cwd: str, timeout: int) -> dict:
        """
        Klasik subprocess runner.
        /run shell komutları ve non-interactive mod için kullanılır.
        """
        try:
            proc = await asyncio.create_subprocess_exec(
                *args,
                cwd=cwd,
                env=self.build_env(),
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
            )

            try:
                stdout, stderr = await asyncio.wait_for(proc.communicate(), timeout=timeout)
            except asyncio.TimeoutError:
                proc.kill()
                await proc.communicate()
                return {"rc": -1, "stdout": "", "stderr": "Timed out"}

            return {
                "rc": proc.returncode,
                "stdout": stdout.decode(errors="replace"),
                "stderr": stderr.decode(errors="replace"),
            }

        except FileNotFoundError:
            return {
                "rc": -1,
                "stdout": "",
                "stderr": (
                    f"Executable not found: {args[0]}\n"
                    "Fix: check path and PATH in telegram_config.json."
                ),
            }
        except Exception as e:
            return {"rc": -1, "stdout": "", "stderr": str(e)}

    async def run_copilot_pty(self, text: str, timeout: int) -> dict:
        """
        Copilot'u pseudo-terminal içinde çalıştırır.

        Neden gerekli?
        - Manuel terminalde Copilot izin sorabiliyor.
        - Normal subprocess pipe ile çalışınca Copilot TTY görmez.
        - TTY olmayınca bazı tool/shell izinleri alınamaz.
        - pexpect.spawn ile Copilot gerçek terminale yakın ortamda çalışır.

        Bu fonksiyon permission/allow/continue gibi sorulara config'e göre otomatik cevap verebilir.
        """
        cmd = self.build_copilot_command(text, include_prompt_as_arg=True)
        command_line = " ".join(shlex.quote(x) for x in cmd)
        env = self.build_env()

        def _run():
            output_parts: List[str] = []

            try:
                child = pexpect.spawn(
                    command_line,
                    cwd=self.working_dir,
                    env=env,
                    encoding="utf-8",
                    timeout=self.pty_idle_timeout,
                )
            except Exception as e:
                return {"rc": -1, "stdout": "", "stderr": str(e)}

            started = time.time()

            patterns = [
                r"(?i)permission.*\?",
                r"(?i)allow.*\?",
                r"(?i)continue.*\?",
                r"(?i)proceed.*\?",
                r"(?i)y/n",
                r"(?i)\[y/N\]",
                r"(?i)\(y/n\)",
                pexpect.EOF,
                pexpect.TIMEOUT,
            ]

            while True:
                if time.time() - started > timeout:
                    child.close(force=True)
                    return {
                        "rc": -1,
                        "stdout": "".join(output_parts),
                        "stderr": "Timed out",
                    }

                try:
                    idx = child.expect(patterns, timeout=self.pty_idle_timeout)
                    output_parts.append(child.before or "")

                    if idx in [0, 1, 2, 3, 4, 5, 6]:
                        output_parts.append(child.after or "")

                        if self.config.get("copilot_auto_answer_permission", True):
                            answer = self.config.get("copilot_permission_answer", "y")
                            child.sendline(answer)
                            output_parts.append(f"\n[AUTO-ANSWERED: {answer}]\n")
                            continue

                        child.close(force=True)
                        return {
                            "rc": -1,
                            "stdout": "".join(output_parts),
                            "stderr": "Copilot asked permission but auto-answer is disabled.",
                        }

                    if idx == 7:
                        output_parts.append(child.before or "")
                        return {
                            "rc": child.exitstatus if child.exitstatus is not None else 0,
                            "stdout": "".join(output_parts),
                            "stderr": "",
                        }

                    if idx == 8:
                        output_parts.append(child.before or "")
                        continue

                except pexpect.EOF:
                    output_parts.append(child.before or "")
                    return {
                        "rc": child.exitstatus if child.exitstatus is not None else 0,
                        "stdout": "".join(output_parts),
                        "stderr": "",
                    }
                except Exception as e:
                    child.close(force=True)
                    return {
                        "rc": -1,
                        "stdout": "".join(output_parts),
                        "stderr": str(e),
                    }

        return await asyncio.to_thread(_run)

    async def start(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return
        await update.message.reply_text("Telegram Copilot Bot aktif. /help yazabilirsin.")

    async def help(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        msg = (
            "/help - yardım\n"
            "/status - servis durumu\n"
            "/prompt <text> - Copilot'a kısa prompt gönderir\n"
            "/task <text> - Copilot'u PTY ile arka planda çalıştırır, timeout yok\n"
            "/durum - çalışan task durumunu ve son logları gösterir\n"
            "/cancel - çalışan task'ı durdurur\n"
            "/run <command> - lokal shell komutu çalıştırır\n"
            "/logs <lines> - bot loglarını okur, sadece admin\n\n"
            "Normal mesajlarda bot adı geçerse Copilot'a gönderilir. Örn: bot pwd çalıştır\n"
            + self.config.get("task_reminder", "")
        )
        await update.message.reply_text(msg)

    async def status(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        task_state = "none"
        if self.task_process:
            task_state = "running" if self.task_process.isalive() else "finished"

        await update.message.reply_text(
            f"Running\n"
            f"working_dir={self.working_dir}\n"
            f"exec_mode={self.exec_mode}\n"
            f"timeout={self.timeout}\n"
            f"copilot_path={self.copilot_path}\n"
            f"model={self.model}\n"
            f"copilot_args={' '.join(self.copilot_args)}\n"
            f"node_bin_dir={self.config.get('node_bin_dir')}\n"
            f"task={task_state}"
        )

    async def prompt(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        text = " ".join(context.args).strip()
        if not text:
            await update.message.reply_text("Usage: /prompt <text>")
            return

        await self.send_to_copilot(update, text)

    async def normal_message(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        text = update.message.text.strip()
        if not text:
            return

        lower = text.lower()

        triggers = [
            item.lower()
            for item in self.config.get(
                "always_reply_triggers", ["selam", "merhaba", "hi", "hello"]
            )
        ]

        if any(t in lower for t in triggers):
            await update.message.reply_text(
                "Merhaba. Kullanım:\n"
                "/prompt <komut> - kısa Copilot işi\n"
                "/task <komut> - uzun işi arka planda başlatır\n"
                "/durum - son task çıktısını gösterir\n"
                "/cancel - çalışan task'ı durdurur\n"
                "/run <komut> - lokal shell komutu çalıştırır\n\n"
                + self.config.get("task_reminder", "")
            )
            return

        if not any(name in lower for name in self.bot_names):
            return

        await self.send_to_copilot(update, text)

    async def send_to_copilot(self, update: Update, text: str):
        uid = update.effective_user.id
        username = update.effective_user.username

        append_json_log(
            self.log_dir / PROMPT_LOG,
            {
                "ts": time.time(),
                "mode": "prompt",
                "user_id": uid,
                "username": username,
                "prompt": text,
            },
        )

        self.logger.info("Copilot prompt from %s (%s): %s", username, uid, text)

        await update.message.reply_text("Copilot çalışıyor...")

        if self.config.get("copilot_interactive", True):
            result = await self.run_copilot_pty(text, self.timeout)
        else:
            args = self.build_copilot_command(text, include_prompt_as_arg=True)
            result = await self.run_process_with_timeout(
                args=args,
                cwd=self.working_dir,
                timeout=self.timeout,
            )

        rc = result["rc"]
        stdout = result["stdout"].strip()
        stderr = result["stderr"].strip()

        self.logger.info(
            "Copilot finished rc=%s stdout_len=%s stderr_len=%s",
            rc,
            len(stdout),
            len(stderr),
        )

        output = stdout or stderr or "Boş cevap."
        reply = f"RC={rc}\n\n{output}"

        for part in split_telegram(reply):
            await update.message.reply_text(part)

    async def task(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        """
        Uzun Copilot işini arka planda PTY ile başlatır.
        Timeout uygulanmaz; /durum ile log okunur.
        """
        if not await self.require_allowed(update):
            return

        text = " ".join(context.args).strip()
        if not text:
            await update.message.reply_text("Usage: /task <text>")
            return

        if self.task_process and self.task_process.isalive():
            await update.message.reply_text(
                "Zaten çalışan bir task var. /durum ile kontrol et veya /cancel ile durdur."
            )
            return

        uid = update.effective_user.id
        username = update.effective_user.username

        append_json_log(
            self.log_dir / PROMPT_LOG,
            {
                "ts": time.time(),
                "mode": "task",
                "user_id": uid,
                "username": username,
                "prompt": text,
            },
        )

        self.task_log_path.write_text("", encoding="utf-8")
        self.task_prompt = text
        self.task_started_at = time.time()

        cmd = self.build_copilot_command(text, include_prompt_as_arg=True)
        command_line = " ".join(shlex.quote(x) for x in cmd)

        self.logger.info("Starting background Copilot PTY task: %s", command_line)

        try:
            child = pexpect.spawn(
                command_line,
                cwd=self.working_dir,
                env=self.build_env(),
                encoding="utf-8",
                timeout=self.pty_idle_timeout,
            )

            self.task_process = child

            await update.message.reply_text(
                f"Task başladı.\nPTY PID={child.pid}\n/durum ile son çıktıyı görebilirsin."
            )

            asyncio.create_task(self.watch_pty_task(child))

        except Exception as e:
            self.logger.exception("Failed to start PTY task")
            await update.message.reply_text(f"Task başlatılamadı: {e}")

    async def watch_pty_task(self, child: pexpect.spawn):
        """
        Arka plan Copilot PTY task çıktısını log dosyasına yazar.
        İzin sorularına otomatik cevap verir.
        """
        patterns = [
            r"(?i)permission.*\?",
            r"(?i)allow.*\?",
            r"(?i)continue.*\?",
            r"(?i)proceed.*\?",
            r"(?i)y/n",
            r"(?i)\[y/N\]",
            r"(?i)\(y/n\)",
            pexpect.EOF,
            pexpect.TIMEOUT,
        ]

        with self.task_log_path.open("a", encoding="utf-8", errors="replace") as log:
            while True:
                try:
                    idx = await asyncio.to_thread(
                        child.expect, patterns, self.pty_idle_timeout
                    )

                    if child.before:
                        log.write(child.before)
                        log.flush()

                    if idx in [0, 1, 2, 3, 4, 5, 6]:
                        if child.after:
                            log.write(child.after)
                        if self.config.get("copilot_auto_answer_permission", True):
                            answer = self.config.get("copilot_permission_answer", "y")
                            child.sendline(answer)
                            log.write(f"\n[AUTO-ANSWERED: {answer}]\n")
                            log.flush()
                            continue

                        log.write("\nCopilot asked permission but auto-answer is disabled.\n")
                        child.close(force=True)
                        break

                    if idx == 7:
                        if child.before:
                            log.write(child.before)
                        log.write("\n[TASK FINISHED]\n")
                        log.flush()
                        break

                    if idx == 8:
                        if child.before:
                            log.write(child.before)
                            log.flush()
                        continue

                except Exception as e:
                    log.write(f"\n[TASK WATCHER ERROR] {e}\n")
                    log.flush()
                    try:
                        child.close(force=True)
                    except Exception:
                        pass
                    break

        self.logger.info("Background PTY task finished")

    async def durum(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        if not self.task_process:
            await update.message.reply_text("Henüz task yok.")
            return

        state = "running" if self.task_process.isalive() else "finished"

        elapsed = ""
        if self.task_started_at:
            elapsed = f"{int(time.time() - self.task_started_at)}s"

        last = tail_file(self.task_log_path, max_chars=3400)

        reply = (
            f"Task durum: {state}\n"
            f"Süre: {elapsed}\n"
            f"Prompt: {self.task_prompt}\n\n"
            f"--- Son çıktı ---\n{last}"
        )

        for part in split_telegram(reply):
            await update.message.reply_text(part)

    async def cancel(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        if not self.task_process or not self.task_process.isalive():
            await update.message.reply_text("Çalışan task yok.")
            return

        self.task_process.close(force=True)
        await update.message.reply_text("Task durduruldu.")

    async def run(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        """
        Lokal Ubuntu komutu çalıştırır.
        Bu Copilot değildir; gerçek shell execution buradadır.
        """
        if not await self.require_allowed(update):
            return

        uid = update.effective_user.id
        username = update.effective_user.username
        cmd = " ".join(context.args).strip()

        if not cmd:
            await update.message.reply_text("Usage: /run <command>")
            return

        append_json_log(
            self.log_dir / PROMPT_LOG,
            {
                "ts": time.time(),
                "mode": "run",
                "user_id": uid,
                "username": username,
                "prompt": cmd,
            },
        )

        self.logger.info("Run requested by %s (%s): %s", username, uid, cmd)

        args = shlex.split(cmd)
        allowed = is_allowed_by_whitelist(args, self.whitelist)

        if not allowed and not (self.allow_unsafe and self.is_admin(uid)):
            await update.message.reply_text("Command denied by whitelist.")
            return

        if self.exec_mode == "dry-run":
            await update.message.reply_text(f"DRY-RUN: {cmd}")
            return

        await update.message.reply_text(f"Executing: {cmd}")

        result = await self.run_process_with_timeout(
            args=args,
            cwd=self.working_dir,
            timeout=self.timeout,
        )

        rc = result["rc"]
        stdout = result["stdout"].strip()
        stderr = result["stderr"].strip()

        reply = f"RC={rc}\n\n--- STDOUT ---\n{stdout}\n\n--- STDERR ---\n{stderr}"

        for part in split_telegram(reply):
            await update.message.reply_text(part)

    async def logs(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        uid = update.effective_user.id
        if not self.is_admin(uid):
            await update.message.reply_text("Only admins can read logs.")
            return

        n = 100
        if context.args:
            try:
                n = int(context.args[0])
            except ValueError:
                pass

        path = self.log_dir / BOT_LOG
        if not path.exists():
            await update.message.reply_text("No logs yet.")
            return

        lines = path.read_text(errors="replace").splitlines()[-n:]
        text = "\n".join(lines) or "No recent logs."

        for part in split_telegram(text):
            await update.message.reply_text(part)

    def build_app(self):
        token = os.getenv("TELEGRAM_BOT_TOKEN") or self.config.get("telegram_token")
        if not token:
            raise RuntimeError("telegram_token missing in config or TELEGRAM_BOT_TOKEN env")

        app = ApplicationBuilder().token(token).build()

        app.add_handler(CommandHandler("start", self.start))
        app.add_handler(CommandHandler("help", self.help))
        app.add_handler(CommandHandler("status", self.status))
        app.add_handler(CommandHandler("prompt", self.prompt))
        app.add_handler(CommandHandler("task", self.task))
        app.add_handler(CommandHandler("durum", self.durum))
        app.add_handler(CommandHandler("cancel", self.cancel))
        app.add_handler(CommandHandler("run", self.run))
        app.add_handler(CommandHandler("logs", self.logs))

        app.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, self.normal_message))

        return app


def main():
    try:
        config = load_config()
    except Exception as e:
        print(f"Failed to load config: {e}")
        sys.exit(1)

    bot = TelegramCopilotBot(config)

    try:
        app = bot.build_app()
    except Exception as e:
        bot.logger.exception("Failed to build app: %s", e)
        print("Failed to build Telegram app; check config and TELEGRAM_BOT_TOKEN")
        sys.exit(1)

    bot.logger.info("Starting Telegram Copilot Bot")
    app.run_polling(poll_interval=bot.config.get("poll_interval", 1.0))


if __name__ == "__main__":
    main()
