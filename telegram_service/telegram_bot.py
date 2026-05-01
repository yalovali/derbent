#!/usr/bin/env python3
"""
Telegram AI Agent Bot

Komutlar:
- /prompt [agent] <mesaj> : Varsayılan veya seçilen AI ajanını çalıştırır.
- /task [agent] <mesaj>   : Uzun AI işini arka planda çalıştırır, timeout yoktur.
- /taskstatus             : Arka plan task çıktısının son halini gösterir.
- /run <komut>            : Lokal Ubuntu shell komutu çalıştırır.
- hazır komutlar          : telegram_config.json içindeki custom_prompts ile tanımlanır.
- normal mesaj            : Bot adı veya ajan adı geçerse varsayılan AI ajanına gönderilir.

Desteklenen ajan tipleri:
1. CLI ajanı (varsayılan): executable + args ile subprocess/PTY üzerinden çalışır.
   Örnekler: claude (Claude Code CLI), codex, copilot
2. Claude Agent SDK (api_type: "claude"): Anthropic Python SDK üzerinden doğrudan
   API çağrısı yapar — subprocess yok, PTY yok, daha hızlı ve güvenilir.
   Gereksinimler: pip install anthropic  ve  ANTHROPIC_API_KEY ortam değişkeni.

PTY notu:
Bazı AI CLI araçları manuel terminalde izin isteyebildiği için pexpect ile PTY
kullanıyoruz. Bu, normal subprocess pipe kullanımına göre interactive davranışa
daha yakındır. SDK tabanlı Claude ajanı bu sorunu tamamen ortadan kaldırır.
"""

import asyncio
import json
import logging
import logging.handlers
import os
import pexpect
import re
import shlex
import shutil
import sys
import time
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple

# Optional: Anthropic SDK for direct Claude API calls (api_type: "claude" agents).
# Install with: pip install anthropic
try:
    import anthropic as _anthropic_sdk
    _ANTHROPIC_AVAILABLE = True
except ImportError:
    _anthropic_sdk = None  # type: ignore[assignment]
    _ANTHROPIC_AVAILABLE = False

from telegram import Update, ReplyKeyboardMarkup
from telegram.ext import (
    ApplicationBuilder,
    ContextTypes,
    MessageHandler,
    CommandHandler,
    filters,
)

ROOT = Path(__file__).resolve().parent
CONFIG_PATH = Path(os.getenv("TELEGRAM_BOT_CONFIG", str(ROOT / "telegram_config.json"))).expanduser()

BOT_LOG = "telegram_bot.log"
PROMPT_LOG = "ai_prompts.log"
TASK_LOG = "ai_task.log"

# Agent types that use SDK/API calls instead of subprocess execution.
_SUPPORTED_API_TYPES = frozenset({"claude"})


class ConfigurationError(Exception):
    """Raised when configuration is missing or unsafe for product use."""


def normalize_alias(value: str) -> str:
    """Treat 'durum' and '/durum' as the same configurable command key."""
    return value.strip().lower().lstrip("/").split("@", 1)[0]


def is_valid_telegram_command(value: str) -> bool:
    return bool(re.fullmatch(r"[A-Za-z0-9_]{1,32}", value))


def load_env_file(path: Path):
    """Load local KEY=VALUE secrets without overriding real environment values."""
    if not path.exists():
        return

    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        value = value.strip().strip('"').strip("'")
        os.environ.setdefault(key.strip(), value)


def load_config() -> dict:
    if not CONFIG_PATH.exists():
        raise ConfigurationError(
            f"Config file not found: {CONFIG_PATH}\n"
            "Set TELEGRAM_BOT_CONFIG or create telegram_service/telegram_config.json."
        )
    try:
        return json.loads(CONFIG_PATH.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        raise ConfigurationError(f"Invalid JSON in {CONFIG_PATH}: {exc}") from exc


def setup_logging(log_dir: Path, bot_log: str = BOT_LOG):
    log_dir.mkdir(parents=True, exist_ok=True)

    logger = logging.getLogger()
    logger.setLevel(logging.INFO)

    formatter = logging.Formatter("%(asctime)s %(levelname)s %(name)s - %(message)s")

    file_handler = logging.handlers.RotatingFileHandler(
        log_dir / bot_log,
        maxBytes=5 * 1024 * 1024,
        backupCount=5,
    )
    file_handler.setFormatter(formatter)

    console_handler = logging.StreamHandler()
    console_handler.setFormatter(formatter)

    logger.handlers.clear()
    logger.addHandler(file_handler)
    logger.addHandler(console_handler)
    # httpx logs Telegram API URLs, which include the bot token. Keep those out of logs.
    logging.getLogger("httpx").setLevel(logging.WARNING)


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


class TelegramAIAgentBot:
    def __init__(self, config: dict):
        self.config = config

        load_env_file(ROOT / config.get("env_file", ".env"))

        self.log_dir = ROOT / config.get("log_dir", "logs")
        self.bot_log_name = config.get("bot_log", BOT_LOG)
        self.prompt_log_name = config.get("prompt_log", PROMPT_LOG)
        self.task_log_name = config.get("task_log", TASK_LOG)
        setup_logging(self.log_dir, self.bot_log_name)

        self.logger = logging.getLogger("telegram_ai_agent_bot")

        self.working_dir = config.get("working_dir", str(ROOT))
        self.allowed_users = set(config.get("allowed_user_ids", []))
        self.admin_users = set(config.get("admin_user_ids", []))

        self.exec_mode = config.get("exec_mode", "dry-run")
        self.allow_unsafe = bool(config.get("allow_unsafe_exec", False))
        self.whitelist = config.get("whitelist_commands", [])

        self.timeout = int(config.get("timeout", 3600))
        self.pty_idle_timeout = int(config.get("pty_idle_timeout", 15))

        self.token_env = config.get("telegram_token_env", "TELEGRAM_BOT_TOKEN")
        self.default_agent = normalize_alias(config.get("default_agent", "codex"))
        self.bot_names = [
            normalize_alias(name)
            for name in config.get("bot_names", ["bot", "agent", "codex"])
        ]

        self.agents = self.load_agents()
        self.agent_aliases = self.build_agent_aliases()
        self.custom_prompts = self.load_custom_prompts()
        self.alias_to_prompt = self.build_prompt_aliases()
        self.custom_command_names = self.build_custom_command_names()
        self.validate_config()

        self.task_process: Optional[pexpect.spawn] = None
        self.task_started_at: Optional[float] = None
        self.task_prompt: Optional[str] = None
        self.task_agent: Optional[str] = None
        self.task_log_path = self.log_dir / self.task_log_name
        self._agent_busy: bool = False
        # asyncio.Task handle for SDK-based Claude agent background tasks.
        self._running_api_task: Optional[asyncio.Task] = None

    def load_agents(self) -> Dict[str, Dict[str, Any]]:
        agents = self.config.get("agents")
        if isinstance(agents, dict) and agents:
            return {normalize_alias(key): value for key, value in agents.items()}

        # Backward-compatible fallback for older configs. New configs should use agents.
        copilot_path = self.config.get("copilot_path") or shutil.which("copilot") or "copilot"
        return {
            "copilot": {
                "display_name": "Copilot",
                "executable": copilot_path,
                "args": self.config.get("copilot_args", ["-p"]),
                "interactive": bool(self.config.get("copilot_interactive", True)),
                "model": self.config.get("model", ""),
                "prompt_as_arg": True,
                "auto_answer_permission": bool(
                    self.config.get("copilot_auto_answer_permission", True)
                ),
                "permission_answer": self.config.get("copilot_permission_answer", "y"),
            }
        }

    def build_agent_aliases(self) -> Dict[str, str]:
        aliases: Dict[str, str] = {}
        for key, agent in self.agents.items():
            aliases[key] = key
            for alias in agent.get("aliases", []):
                aliases[normalize_alias(alias)] = key
        return aliases

    def load_custom_prompts(self) -> Dict[str, Dict[str, Any]]:
        raw_prompts = self.config.get("custom_prompts", {})
        if not isinstance(raw_prompts, dict):
            raise ConfigurationError("custom_prompts must be a JSON object.")

        prompts: Dict[str, Dict[str, Any]] = {}
        for key, value in raw_prompts.items():
            if isinstance(value, str):
                value = {"prompt": value}
            if not isinstance(value, dict):
                raise ConfigurationError(f"custom_prompts.{key} must be an object or string.")
            prompt_key = normalize_alias(key)
            prompt = dict(value)
            prompt.setdefault("aliases", [prompt_key])
            prompt.setdefault("agent", self.default_agent)
            prompt.setdefault("mode", "prompt")
            prompts[prompt_key] = prompt
        return prompts

    def build_prompt_aliases(self) -> Dict[str, str]:
        aliases: Dict[str, str] = {}
        for key, prompt in self.custom_prompts.items():
            aliases[key] = key
            for alias in prompt.get("aliases", []):
                aliases[normalize_alias(alias)] = key
        return aliases

    def build_custom_command_names(self) -> List[str]:
        return sorted(
            {
                alias
                for alias in self.alias_to_prompt.keys()
                if is_valid_telegram_command(alias)
            }
        )

    def validate_config(self):
        errors: List[str] = []

        legacy_token = self.config.get("telegram_token")
        if legacy_token and legacy_token != "YOUR_TELEGRAM_BOT_TOKEN":
            errors.append(
                "telegram_token must not be stored in JSON. Move it to "
                f"{self.config.get('telegram_token_env', 'TELEGRAM_BOT_TOKEN')} in .env."
            )

        if not all(isinstance(item, int) for item in self.allowed_users):
            errors.append("allowed_user_ids must contain numeric Telegram user IDs.")
        if not all(isinstance(item, int) for item in self.admin_users):
            errors.append("admin_user_ids must contain numeric Telegram user IDs.")
        if not self.allowed_users:
            errors.append("allowed_user_ids is empty; add your Telegram numeric user ID.")
        if self.exec_mode not in {"auto", "dry-run"}:
            errors.append("exec_mode must be 'auto' or 'dry-run'.")
        if self.default_agent not in self.agents:
            errors.append(f"default_agent '{self.default_agent}' is not defined in agents.")

        for key, agent in self.agents.items():
            if not isinstance(agent, dict):
                errors.append(f"agents.{key} must be an object.")
                continue
            api_type = agent.get("api_type", "")
            if api_type:
                # SDK/API agent — no executable needed, but api_type must be known.
                if api_type not in _SUPPORTED_API_TYPES:
                    errors.append(
                        f"agents.{key}.api_type '{api_type}' is not supported. "
                        f"Supported: {', '.join(sorted(_SUPPORTED_API_TYPES))}"
                    )
                elif api_type == "claude" and not _ANTHROPIC_AVAILABLE:
                    errors.append(
                        f"agents.{key} uses api_type 'claude' but the anthropic package "
                        "is not installed. Run: pip install anthropic"
                    )
            else:
                # CLI agent — executable is required.
                if not agent.get("executable"):
                    errors.append(f"agents.{key}.executable is required.")
                elif not self.find_executable(key):
                    errors.append(
                        f"agents.{key}.executable not found: {agent.get('executable')}. "
                        "Set an absolute path or add its directory to path_prepend."
                    )
                if not isinstance(agent.get("args", []), list):
                    errors.append(f"agents.{key}.args must be a JSON array.")

        for key, prompt in self.custom_prompts.items():
            if not prompt.get("prompt"):
                errors.append(f"custom_prompts.{key}.prompt is required.")
            if normalize_alias(prompt.get("agent", self.default_agent)) not in self.agent_aliases:
                errors.append(f"custom_prompts.{key}.agent is not a configured agent.")
            if prompt.get("mode", "prompt") not in {"prompt", "task"}:
                errors.append(f"custom_prompts.{key}.mode must be 'prompt' or 'task'.")

        if errors:
            raise ConfigurationError("Configuration errors:\n- " + "\n- ".join(errors))

    def path_prepend(self, agent_key: Optional[str] = None) -> List[str]:
        paths: List[str] = []
        if self.config.get("node_bin_dir"):
            paths.append(str(self.config["node_bin_dir"]))
        for item in self.config.get("path_prepend", []):
            paths.append(str(Path(item).expanduser()))
        if agent_key and agent_key in self.agents:
            for item in self.agents[agent_key].get("path_prepend", []):
                paths.append(str(Path(item).expanduser()))
        return paths

    def path_env(self, agent_key: Optional[str] = None) -> str:
        parts = self.path_prepend(agent_key)
        parts.append(os.environ.get("PATH", ""))
        return ":".join(part for part in parts if part)

    def find_executable(self, agent_key: str) -> Optional[str]:
        executable = str(self.agents[agent_key].get("executable", "")).strip()
        expanded = os.path.expandvars(os.path.expanduser(executable))
        if "/" in expanded:
            return expanded if Path(expanded).exists() else None
        return shutil.which(expanded, path=self.path_env(agent_key))

    def resolve_agent_key(self, value: Optional[str]) -> str:
        if not value:
            return self.default_agent
        key = normalize_alias(value)
        if key not in self.agent_aliases:
            raise ConfigurationError(
                f"Unknown AI agent '{value}'. Available: {', '.join(sorted(self.agents))}"
            )
        return self.agent_aliases[key]

    def build_env(self, agent_key: Optional[str] = None) -> dict:
        """
        systemd altında PATH genelde kısadır.
        path_prepend ekleyerek /usr/bin/env node hatasını çözeriz.
        """
        env = os.environ.copy()
        env["PATH"] = self.path_env(agent_key)
        env["HOME"] = self.config.get("home", str(Path.home()))
        for key, value in self.config.get("env", {}).items():
            env[str(key)] = str(value)
        if agent_key and agent_key in self.agents:
            for key, value in self.agents[agent_key].get("env", {}).items():
                env[str(key)] = str(value)
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

    def build_safe_prompt(self, agent_key: str, text: str) -> str:
        """
        AI ajanına verilen gerçek prompt burada hazırlanır.
        safety_prompt global veya agent config dosyasından gelir.
        """
        agent = self.agents[agent_key]
        safety_prompt = agent.get("safety_prompt", self.config.get("safety_prompt", "")).strip()
        return f"{safety_prompt} {text}".strip()

    def build_agent_command(
        self,
        agent_key: str,
        text: str,
        include_prompt_as_arg: bool = True,
    ) -> List[str]:
        """
        Config'den seçilen AI ajanının komut satırını üretir.

        Prompt argümanının nereye ekleneceği ve model parametresi JSON ile
        kontrol edilir; böylece Codex, Copilot veya başka CLI ajanları aynı
        bot üzerinden çalışabilir.
        """
        agent = self.agents[agent_key]
        executable = self.find_executable(agent_key)
        if not executable:
            raise ConfigurationError(f"Executable for agent '{agent_key}' is not available.")

        safe_prompt = self.build_safe_prompt(agent_key, text)
        args = [executable]

        model = str(agent.get("model", "")).strip()
        if model:
            args += [agent.get("model_arg", "--model"), model]

        args += [str(item) for item in agent.get("args", [])]

        if include_prompt_as_arg and agent.get("prompt_as_arg", True):
            args.append(safe_prompt)

        return args

    async def run_process_with_timeout(
        self,
        args: List[str],
        cwd: str,
        timeout: int,
        env: Optional[dict] = None,
    ) -> dict:
        """
        Klasik subprocess runner.
        /run shell komutları ve non-interactive mod için kullanılır.
        """
        try:
            proc = await asyncio.create_subprocess_exec(
                *args,
                cwd=cwd,
                env=env or self.build_env(),
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

    async def run_agent_pty(self, agent_key: str, text: str, timeout: int) -> dict:
        """
        AI ajanını pseudo-terminal içinde çalıştırır.

        Neden gerekli?
        - Manuel terminalde AI CLI izin sorabiliyor.
        - Normal subprocess pipe ile çalışınca bazı CLI'lar TTY görmez.
        - TTY olmayınca bazı tool/shell izinleri alınamaz.
        - pexpect.spawn ile ajan gerçek terminale yakın ortamda çalışır.

        Bu fonksiyon permission/allow/continue gibi sorulara config'e göre otomatik cevap verebilir.
        """
        agent = self.agents[agent_key]
        cmd = self.build_agent_command(agent_key, text, include_prompt_as_arg=True)
        command_line = " ".join(shlex.quote(x) for x in cmd)
        env = self.build_env(agent_key)
        display_name = agent.get("display_name", agent_key)

        def _run():
            output_parts: List[str] = []

            try:
                child = pexpect.spawn(
                    command_line,
                    cwd=agent.get("working_dir", self.working_dir),
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

                        if agent.get("auto_answer_permission", False):
                            answer = agent.get("permission_answer", "y")
                            child.sendline(answer)
                            output_parts.append(f"\n[AUTO-ANSWERED: {answer}]\n")
                            continue

                        child.close(force=True)
                        return {
                            "rc": -1,
                            "stdout": "".join(output_parts),
                            "stderr": f"{display_name} asked permission but auto-answer is disabled.",
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

    async def run_claude_api_agent(self, agent_key: str, text: str) -> dict:
        """
        Claude Agent SDK üzerinden doğrudan API çağrısı yapar.

        Neden bu yöntem?
        - subprocess veya PTY gerektirmez — daha hızlı ve kararlı.
        - PTY/CLI ajanlarında yaşanan TTY ve izin sorunları yoktur.
        - System prompt, model ve max_tokens agent config'inden okunur.
        - ANTHROPIC_API_KEY ortam değişkeni veya api_key_env ile belirtilen
          env değişkeni üzerinden kimlik doğrulaması yapılır.

        Gereksinimler:
          pip install anthropic
          ANTHROPIC_API_KEY=<your-key>  (veya .env dosyasında)
        """
        if not _ANTHROPIC_AVAILABLE:
            return {
                "rc": -1,
                "stdout": "",
                "stderr": "anthropic SDK yüklü değil. Yükle: pip install anthropic",
            }

        agent = self.agents[agent_key]
        api_key_env = agent.get("api_key_env", "ANTHROPIC_API_KEY")
        api_key = os.getenv(api_key_env)
        if not api_key:
            return {
                "rc": -1,
                "stdout": "",
                "stderr": f"API anahtarı eksik: {api_key_env} ortam değişkeni tanımlı değil.",
            }

        model = agent.get("model", "claude-sonnet-4-6")
        max_tokens = int(agent.get("max_tokens", 8096))
        # Agent-level system_prompt varsa kullan; yoksa global safety_prompt'a dön.
        system = agent.get("system_prompt", self.config.get("safety_prompt", "")).strip()

        self.logger.info("Claude API call agent=%s model=%s", agent_key, model)

        try:
            client = _anthropic_sdk.Anthropic(api_key=api_key)
            kwargs: Dict[str, Any] = {
                "model": model,
                "max_tokens": max_tokens,
                "messages": [{"role": "user", "content": text}],
            }
            if system:
                kwargs["system"] = system

            response = await asyncio.to_thread(lambda: client.messages.create(**kwargs))
            result_text = response.content[0].text if response.content else ""
            return {"rc": 0, "stdout": result_text, "stderr": ""}

        except Exception as exc:
            self.logger.exception("Claude API call failed agent=%s", agent_key)
            return {"rc": -1, "stdout": "", "stderr": str(exc)}

    async def watch_api_task(self, agent_key: str, text: str):
        """
        Claude API ajanını arka planda çalıştırır; sonucu task log dosyasına yazar.
        PTY tabanlı watch_pty_task'ın SDK karşılığıdır.
        """
        agent = self.agents[agent_key]
        display_name = agent.get("display_name", agent_key)

        with self.task_log_path.open("a", encoding="utf-8", errors="replace") as log:
            try:
                result = await self.run_claude_api_agent(agent_key, text)
                if result["rc"] == 0:
                    log.write(result["stdout"])
                else:
                    log.write(f"[ERROR] {result['stderr']}\n")
                log.write("\n[TASK FINISHED]\n")
                log.flush()
            except asyncio.CancelledError:
                log.write(f"\n[TASK CANCELLED by user]\n")
                log.flush()
                raise
            except Exception as exc:
                log.write(f"\n[TASK WATCHER ERROR] {exc}\n")
                log.flush()

        self.logger.info("Background API task finished agent=%s", agent_key)

    async def post_init(self, application):
        """
        Bot baslatildiginda calisir.
        / yazildiginda cikan komut listesini set eder.
        """
        from telegram import BotCommand

        command_map = {
            "help": "Yardım ve komut listesi",
            "status": "Servis durumu",
            "agents": "Tanımlı AI ajanlarını göster",
            "prompt": "Varsayılan veya seçilen AI ajanına prompt",
            "agent": "Belirli AI ajanına prompt gönder",
            "task": "Arka plan AI task",
            "taskstatus": "Çalışan task durumu",
            "cancel": "Çalışan taskı iptal et",
            "run": "Shell komutu çalıştır",
            "logs": "Bot loglarını göster (admin)",
            "config": "Kullanıcı erişimini yönet (admin)",
            "pull": "Git pull çalıştır",
        }
        if "durum" not in self.custom_command_names:
            command_map["durum"] = "Çalışan task durumu"
        for command_name in self.custom_command_names:
            prompt = self.custom_prompts[self.alias_to_prompt[command_name]]
            command_map[command_name] = prompt.get("description", "Hazır AI prompt")

        commands = [
            BotCommand(name, description[:256])
            for name, description in command_map.items()
        ]
        await application.bot.set_my_commands(commands)
        self.logger.info("Bot commands registered: %s", ", ".join(command_map))

    async def show_suggestions(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        keyboard = [
            ["/help", "/status"],
            ["/prompt ", "/task "],
            ["/taskstatus", "/cancel"],
            ["/agents", "/logs"],
        ]
        custom_buttons = [f"/{name}" for name in self.custom_command_names[:6]]
        keyboard.extend(custom_buttons[i : i + 2] for i in range(0, len(custom_buttons), 2))
        reply_markup = ReplyKeyboardMarkup(keyboard, one_time_keyboard=True, resize_keyboard=True)
        await update.message.reply_text("Komut seçebilirsin:", reply_markup=reply_markup)

    async def start(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return
        await update.message.reply_text("Telegram AI Agent Bot aktif. /help yazabilirsin.")

    async def help(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        custom_lines = []
        for command_name in self.custom_command_names:
            prompt = self.custom_prompts[self.alias_to_prompt[command_name]]
            custom_lines.append(
                f"/{command_name} - {prompt.get('description', 'Hazır AI prompt')}"
            )
        custom_text = "\n".join(custom_lines) if custom_lines else "Tanımlı hazır prompt yok."

        msg = (
            "/help - yardım\n"
            "/status - servis durumu\n"
            "/agents - tanımlı AI ajanları\n"
            "/prompt [agent] <text> - kısa AI prompt\n"
            "/agent <agent> <text> - belirli AI ajanına prompt\n"
            "/task [agent] <text> - uzun işi arka planda başlatır\n"
            "/taskstatus - çalışan task durumunu ve son logları gösterir\n"
            "/cancel - çalışan task'ı durdurur\n"
            "/run <command> - lokal shell komutu çalıştırır\n"
            "/logs <lines> - bot loglarını okur, sadece admin\n\n"
            "Hazır promptlar:\n"
            f"{custom_text}\n\n"
            "Normal mesajlarda bot adı veya ajan adı geçerse varsayılan ajan çalışır.\n"
            + self.config.get("task_reminder", "")
        )
        await update.message.reply_text(msg)

    async def status(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        task_state = "none"
        if self.task_process:
            task_state = "pty:running" if self.task_process.isalive() else "pty:finished"
        elif self._running_api_task:
            task_state = "api:running" if not self._running_api_task.done() else "api:finished"

        await update.message.reply_text(
            f"Running\n"
            f"working_dir={self.working_dir}\n"
            f"exec_mode={self.exec_mode}\n"
            f"default_agent={self.default_agent}\n"
            f"agents={', '.join(sorted(self.agents))}\n"
            f"anthropic_sdk={'available' if _ANTHROPIC_AVAILABLE else 'not installed'}\n"
            f"timeout={self.timeout}\n"
            f"token_env={self.token_env} ({'set' if os.getenv(self.token_env) else 'missing'})\n"
            f"path_prepend={', '.join(self.path_prepend())}\n"
            f"task={task_state}"
        )

    async def agents_command(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        lines = []
        for key, agent in sorted(self.agents.items()):
            display = agent.get("display_name", key)
            args = " ".join(str(item) for item in agent.get("args", []))
            lines.append(f"{key}: {display}\n  executable={agent.get('executable')}\n  args={args}")
        await update.message.reply_text("\n".join(lines))

    def parse_agent_prefix(self, args: List[str]) -> Tuple[str, str]:
        if args and normalize_alias(args[0].rstrip(":")) in self.agent_aliases:
            agent_key = self.resolve_agent_key(args[0].rstrip(":"))
            return agent_key, " ".join(args[1:]).strip()
        return self.default_agent, " ".join(args).strip()

    def parse_agent_prefix_text(self, text: str) -> Tuple[str, str]:
        parts = text.strip().split(maxsplit=1)
        if parts and normalize_alias(parts[0].rstrip(":")) in self.agent_aliases:
            agent_key = self.resolve_agent_key(parts[0].rstrip(":"))
            return agent_key, parts[1].strip() if len(parts) > 1 else ""
        return self.default_agent, text.strip()

    async def prompt(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        agent_key, text = self.parse_agent_prefix(context.args)
        if not text:
            await update.message.reply_text("Usage: /prompt [agent] <text>")
            return

        await self.send_to_agent(update, text, agent_key)

    async def agent_prompt(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        if len(context.args) < 2:
            await update.message.reply_text("Usage: /agent <agent> <text>")
            return

        try:
            agent_key = self.resolve_agent_key(context.args[0])
        except ConfigurationError as exc:
            await update.message.reply_text(str(exc))
            return

        await self.send_to_agent(update, " ".join(context.args[1:]).strip(), agent_key)

    async def normal_message(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        text = update.message.text.strip()
        if not text:
            return

        lower = text.lower()
        alias = normalize_alias(text)

        if alias in self.alias_to_prompt:
            await self.run_custom_prompt(update, alias)
            return

        triggers = [
            normalize_alias(item)
            for item in self.config.get(
                "always_reply_triggers", ["help", "yardim"]
            )
        ]

        if alias in triggers:
            await self.help(update, context)
            return

        if not any(name in lower for name in self.bot_names + list(self.agent_aliases)):
            return

        agent_key, prompt_text = self.parse_agent_prefix_text(text)
        if not prompt_text:
            await update.message.reply_text("Prompt boş. Örn: codex proje durumunu özetle")
            return
        await self.send_to_agent(update, prompt_text, agent_key)

    async def custom_prompt_command(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        command_name = normalize_alias(update.message.text.split()[0])
        await self.run_custom_prompt(update, command_name)

    async def run_custom_prompt(self, update: Update, alias: str):
        prompt_key = self.alias_to_prompt.get(normalize_alias(alias))
        if not prompt_key:
            await update.message.reply_text("Bilinmeyen hazır prompt.")
            return

        prompt_config = self.custom_prompts[prompt_key]
        agent_key = self.resolve_agent_key(prompt_config.get("agent", self.default_agent))
        prompt_text = prompt_config["prompt"]
        mode = prompt_config.get("mode", "prompt")

        if mode == "task":
            await self.start_background_task(update, prompt_text, agent_key)
        else:
            await self.send_to_agent(update, prompt_text, agent_key)

    async def send_to_agent(self, update: Update, text: str, agent_key: str):
        api_task_running = self._running_api_task and not self._running_api_task.done()
        if self._agent_busy or (self.task_process and self.task_process.isalive()) or api_task_running:
            await update.message.reply_text(
                "Zaten çalışan bir ajan var. /taskstatus ile kontrol et veya /cancel ile durdur."
            )
            return

        uid = update.effective_user.id
        username = update.effective_user.username
        agent = self.agents[agent_key]
        display_name = agent.get("display_name", agent_key)

        append_json_log(
            self.log_dir / self.prompt_log_name,
            {
                "ts": time.time(),
                "mode": "prompt",
                "agent": agent_key,
                "user_id": uid,
                "username": username,
                "prompt": text,
            },
        )

        self.logger.info("AI prompt from %s (%s), agent=%s", username, uid, agent_key)
        self._agent_busy = True
        await update.message.reply_text(f"{display_name} çalışıyor...")

        try:
            api_type = agent.get("api_type", "")
            if api_type == "claude":
                # Claude Agent SDK: doğrudan API çağrısı, subprocess/PTY yok.
                result = await self.run_claude_api_agent(agent_key, text)
            elif agent.get("interactive", False):
                result = await self.run_agent_pty(agent_key, text, self.timeout)
            else:
                args = self.build_agent_command(agent_key, text, include_prompt_as_arg=True)
                result = await self.run_process_with_timeout(
                    args=args,
                    cwd=agent.get("working_dir", self.working_dir),
                    timeout=self.timeout,
                    env=self.build_env(agent_key),
                )
        finally:
            self._agent_busy = False

        rc = result["rc"]
        stdout = result["stdout"].strip()
        stderr = result["stderr"].strip()

        self.logger.info(
            "AI agent finished agent=%s rc=%s stdout_len=%s stderr_len=%s",
            agent_key,
            rc,
            len(stdout),
            len(stderr),
        )

        if rc != 0:
            output = stderr or stdout or "Boş cevap."
            reply = f"[{display_name} hata RC={rc}]\n{output}"
        else:
            reply = stdout or stderr or "Boş cevap."

        for part in split_telegram(reply):
            await update.message.reply_text(part)

    async def task(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        """
        Uzun AI işini arka planda PTY ile başlatır.
        Timeout uygulanmaz; /taskstatus ile log okunur.
        """
        if not await self.require_allowed(update):
            return

        agent_key, text = self.parse_agent_prefix(context.args)
        if not text:
            await update.message.reply_text("Usage: /task [agent] <text>")
            return

        await self.start_background_task(update, text, agent_key)

    async def start_background_task(self, update: Update, text: str, agent_key: str):
        api_task_running = self._running_api_task and not self._running_api_task.done()
        if self._agent_busy or (self.task_process and self.task_process.isalive()) or api_task_running:
            await update.message.reply_text(
                "Zaten çalışan bir ajan var. /taskstatus ile kontrol et veya /cancel ile durdur."
            )
            return

        uid = update.effective_user.id
        username = update.effective_user.username
        agent = self.agents[agent_key]
        display_name = agent.get("display_name", agent_key)

        append_json_log(
            self.log_dir / self.prompt_log_name,
            {
                "ts": time.time(),
                "mode": "task",
                "agent": agent_key,
                "user_id": uid,
                "username": username,
                "prompt": text,
            },
        )

        self.task_log_path.write_text("", encoding="utf-8")
        self.task_prompt = text
        self.task_agent = agent_key
        self.task_started_at = time.time()

        api_type = agent.get("api_type", "")
        if api_type == "claude":
            # Claude Agent SDK: asyncio.Task olarak arka planda çalışır.
            self.logger.info("Starting background Claude API task agent=%s", agent_key)
            self._running_api_task = asyncio.create_task(
                self.watch_api_task(agent_key, text)
            )
            await update.message.reply_text(
                f"{display_name} API task başladı.\n"
                "/taskstatus ile son çıktıyı görebilirsin."
            )
            return

        # PTY tabanlı CLI ajan: pexpect.spawn ile çalıştırılır.
        cmd = self.build_agent_command(agent_key, text, include_prompt_as_arg=True)
        command_line = " ".join(shlex.quote(x) for x in cmd)

        self.logger.info("Starting background AI PTY task agent=%s", agent_key)

        try:
            child = pexpect.spawn(
                command_line,
                cwd=agent.get("working_dir", self.working_dir),
                env=self.build_env(agent_key),
                encoding="utf-8",
                timeout=self.pty_idle_timeout,
            )

            self.task_process = child

            await update.message.reply_text(
                f"{display_name} task başladı.\nPTY PID={child.pid}\n"
                "/taskstatus ile son çıktıyı görebilirsin."
            )

            asyncio.create_task(self.watch_pty_task(child, agent_key))

        except Exception as e:
            self.logger.exception("Failed to start PTY task")
            await update.message.reply_text(f"Task başlatılamadı: {e}")

    async def watch_pty_task(self, child: pexpect.spawn, agent_key: str):
        """
        Arka plan AI PTY task çıktısını log dosyasına yazar.
        İzin sorularına otomatik cevap verir.
        """
        agent = self.agents[agent_key]
        display_name = agent.get("display_name", agent_key)
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
                        if agent.get("auto_answer_permission", False):
                            answer = agent.get("permission_answer", "y")
                            child.sendline(answer)
                            log.write(f"\n[AUTO-ANSWERED: {answer}]\n")
                            log.flush()
                            continue

                        log.write(
                            f"\n{display_name} asked permission but auto-answer is disabled.\n"
                        )
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

        self.logger.info("Background PTY task finished agent=%s", agent_key)

    async def durum(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        has_pty = bool(self.task_process)
        has_api = bool(self._running_api_task)

        if not has_pty and not has_api:
            await update.message.reply_text("Henüz task yok.")
            return

        if has_api:
            state = "running" if not self._running_api_task.done() else "finished"
        else:
            state = "running" if self.task_process.isalive() else "finished"

        elapsed = ""
        if self.task_started_at:
            elapsed = f"{int(time.time() - self.task_started_at)}s"

        last = tail_file(self.task_log_path, max_chars=3400)
        display_name = self.agents.get(self.task_agent or "", {}).get(
            "display_name", self.task_agent or "unknown"
        )

        reply = (
            f"Task durum: {state}\n"
            f"Agent: {display_name}\n"
            f"Süre: {elapsed}\n"
            f"Prompt: {self.task_prompt}\n\n"
            f"--- Son çıktı ---\n{last}"
        )

        for part in split_telegram(reply):
            await update.message.reply_text(part)

    async def cancel(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        # Claude API task (asyncio.Task) iptal etme.
        if self._running_api_task and not self._running_api_task.done():
            self._running_api_task.cancel()
            await update.message.reply_text("API task durduruldu.")
            return

        if not self.task_process or not self.task_process.isalive():
            await update.message.reply_text("Çalışan task yok.")
            return

        self.task_process.close(force=True)
        await update.message.reply_text("Task durduruldu.")

    async def run(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        """
        Lokal Ubuntu komutu çalıştırır.
        Bu AI prompt değildir; gerçek shell execution buradadır.
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
            self.log_dir / self.prompt_log_name,
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
            await update.message.reply_text(
                "Command denied by whitelist. Update whitelist_commands or use an admin account."
            )
            return

        if self.exec_mode == "dry-run":
            await update.message.reply_text(f"DRY-RUN: {cmd}")
            return

        await update.message.reply_text(f"Executing: {cmd}")

        result = await self.run_process_with_timeout(
            args=args,
            cwd=self.working_dir,
            timeout=self.timeout,
            env=self.build_env(),
        )

        rc = result["rc"]
        stdout = result["stdout"].strip()
        stderr = result["stderr"].strip()

        reply = f"RC={rc}\n\n--- STDOUT ---\n{stdout}\n\n--- STDERR ---\n{stderr}"

        for part in split_telegram(reply):
            await update.message.reply_text(part)

    async def config_command(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        """
        Admin-only config editor.

        Usage:
          /config add reply           — reply yapılan kişiyi ekle
          /config remove reply        — reply yapılan kişiyi çıkar
          /config add userid <id>     — ID ile ekle
          /config remove userid <id>  — ID ile çıkar
          /config list userids        — listeyi göster
        """
        if not await self.require_allowed(update):
            return

        uid = update.effective_user.id
        if not self.is_admin(uid):
            await update.message.reply_text("Only admins can use /config.")
            return

        args = context.args
        if not args:
            await update.message.reply_text(
                "Kullanım:\n"
                "/config add reply           — reply yapılan kişiyi ekle\n"
                "/config remove reply        — reply yapılan kişiyi çıkar\n"
                "/config add userid <id>     — ID ile ekle\n"
                "/config remove userid <id>  — ID ile çıkar\n"
                "/config list userids        — listeyi göster"
            )
            return

        action = args[0].lower()

        if action == "list" and len(args) >= 2 and args[1].lower() == "userids":
            ids = sorted(self.config.get("allowed_user_ids", []))
            await update.message.reply_text(
                "allowed_user_ids:\n" + "\n".join(str(i) for i in ids)
            )
            return

        if action == "add" and len(args) >= 2 and args[1].lower() == "reply":
            replied = update.message.reply_to_message
            if not replied or not replied.from_user:
                await update.message.reply_text(
                    "Bir mesaja reply yaparak /config add reply yaz."
                )
                return
            target_id = replied.from_user.id
            target_name = replied.from_user.full_name
            ids: list = self.config.setdefault("allowed_user_ids", [])
            if target_id in ids:
                await update.message.reply_text(
                    f"{target_name} ({target_id}) zaten whitelist'te."
                )
                return
            ids.append(target_id)
            self.allowed_users.add(target_id)
            CONFIG_PATH.write_text(
                json.dumps(self.config, ensure_ascii=False, indent=2), encoding="utf-8"
            )
            self.logger.info("config: add userid=%s name=%s by admin=%s", target_id, target_name, uid)
            await update.message.reply_text(
                f"{target_name} ({target_id}) whitelist'e eklendi."
            )
            return

        if action == "remove" and len(args) >= 2 and args[1].lower() == "reply":
            replied = update.message.reply_to_message
            if not replied or not replied.from_user:
                await update.message.reply_text(
                    "Bir mesaja reply yaparak /config remove reply yaz."
                )
                return
            target_id = replied.from_user.id
            target_name = replied.from_user.full_name
            ids = self.config.setdefault("allowed_user_ids", [])
            if target_id not in ids:
                await update.message.reply_text(
                    f"{target_name} ({target_id}) whitelist'te değil."
                )
                return
            ids.remove(target_id)
            self.allowed_users.discard(target_id)
            CONFIG_PATH.write_text(
                json.dumps(self.config, ensure_ascii=False, indent=2), encoding="utf-8"
            )
            self.logger.info("config: remove userid=%s name=%s by admin=%s", target_id, target_name, uid)
            await update.message.reply_text(
                f"{target_name} ({target_id}) whitelist'ten çıkarıldı."
            )
            return

        if action in ("add", "remove") and len(args) >= 3 and args[1].lower() == "userid":
            try:
                target_id = int(args[2])
            except ValueError:
                await update.message.reply_text("User ID sayı olmalı.")
                return

            ids = self.config.setdefault("allowed_user_ids", [])

            if action == "add":
                if target_id in ids:
                    await update.message.reply_text(f"{target_id} zaten whitelist'te.")
                    return
                ids.append(target_id)
                self.allowed_users.add(target_id)
                verb = "eklendi"
            else:
                if target_id not in ids:
                    await update.message.reply_text(f"{target_id} whitelist'te değil.")
                    return
                ids.remove(target_id)
                self.allowed_users.discard(target_id)
                verb = "çıkarıldı"

            CONFIG_PATH.write_text(
                json.dumps(self.config, ensure_ascii=False, indent=2), encoding="utf-8"
            )
            self.logger.info("config: %s userid=%s by admin=%s", verb, target_id, uid)
            await update.message.reply_text(f"{target_id} whitelist'e {verb}.")
            return

        await update.message.reply_text(
            "Kullanım:\n"
            "/config add reply           — reply yapılan kişiyi ekle\n"
            "/config remove reply        — reply yapılan kişiyi çıkar\n"
            "/config add userid <id>     — ID ile ekle\n"
            "/config remove userid <id>  — ID ile çıkar\n"
            "/config list userids        — listeyi göster"
        )

    async def gitpull(self, update: Update, context: ContextTypes.DEFAULT_TYPE):
        if not await self.require_allowed(update):
            return

        await update.message.reply_text(f"git pull çalıştırılıyor: {self.working_dir}")

        result = await self.run_process_with_timeout(
            args=["git", "pull"],
            cwd=self.working_dir,
            timeout=60,
            env=self.build_env(),
        )

        rc = result["rc"]
        stdout = result["stdout"].strip()
        stderr = result["stderr"].strip()
        reply = f"RC={rc}\n{stdout}" + (f"\n{stderr}" if stderr else "")

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

        path = self.log_dir / self.bot_log_name
        if not path.exists():
            await update.message.reply_text("No logs yet.")
            return

        lines = path.read_text(errors="replace").splitlines()[-n:]
        text = "\n".join(lines) or "No recent logs."

        for part in split_telegram(text):
            await update.message.reply_text(part)

    def build_app(self):
        token = os.getenv(self.token_env)
        if not token:
            raise ConfigurationError(
                f"Missing Telegram bot token env var: {self.token_env}\n"
                f"Create {ROOT / self.config.get('env_file', '.env')} with:\n"
                f"{self.token_env}=<your-token>\n"
                "Do not store the token in telegram_config.json."
            )

        app = ApplicationBuilder().token(token).post_init(self.post_init).build()

        if self.custom_command_names:
            app.add_handler(CommandHandler(self.custom_command_names, self.custom_prompt_command))

        app.add_handler(CommandHandler("start", self.start))
        app.add_handler(CommandHandler("help", self.help))
        app.add_handler(CommandHandler("status", self.status))
        app.add_handler(CommandHandler("agents", self.agents_command))
        app.add_handler(CommandHandler("prompt", self.prompt))
        app.add_handler(CommandHandler("agent", self.agent_prompt))
        app.add_handler(CommandHandler("task", self.task))
        app.add_handler(CommandHandler("taskstatus", self.durum))
        if "durum" not in self.custom_command_names:
            app.add_handler(CommandHandler("durum", self.durum))
        app.add_handler(CommandHandler("cancel", self.cancel))
        app.add_handler(CommandHandler("run", self.run))
        app.add_handler(CommandHandler("logs", self.logs))
        app.add_handler(CommandHandler("config", self.config_command))
        app.add_handler(CommandHandler("pull", self.gitpull))

        # Kullanıcı sadece "/" yazdığında öneri butonlarını gösterir.
        app.add_handler(MessageHandler(filters.Regex("^/$"), self.show_suggestions))

        app.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, self.normal_message))

        return app


def main():
    try:
        config = load_config()
        bot = TelegramAIAgentBot(config)
        if "--check-config" in sys.argv:
            if not os.getenv(bot.token_env):
                raise ConfigurationError(
                    f"Missing Telegram bot token env var: {bot.token_env}"
                )
            print(
                "Configuration OK: "
                f"default_agent={bot.default_agent}, "
                f"agents={','.join(sorted(bot.agents))}, "
                f"custom_commands={','.join(bot.custom_command_names) or 'none'}"
            )
            return
        app = bot.build_app()
    except ConfigurationError as e:
        print(str(e), file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"Failed to start Telegram AI Agent Bot: {e}", file=sys.stderr)
        sys.exit(1)

    bot.logger.info("Starting Telegram AI Agent Bot")
    app.run_polling(poll_interval=bot.config.get("poll_interval", 1.0))


if __name__ == "__main__":
    main()
