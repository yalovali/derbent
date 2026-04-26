#!/usr/bin/env python3

from __future__ import annotations

import argparse
import datetime as _dt
import json
import os
from pathlib import Path
import re
import subprocess
import sys
from typing import Any


REPO_ROOT = Path(__file__).resolve().parent.parent
TASKS_ROOT = REPO_ROOT / "tasks" / "agents"

AGENT_NAMES = [
    "orchestrator",
    "analyzer",
    "pattern-designer",
    "coder",
    "verifier",
    "tester",
    "documenter",
    "todo-fix",
    "cleanup",
]


def _play_sound(kind: str) -> None:
    """Play a small completion/error sound (best-effort).

    Use DERBENT_SOUND_ENABLED=false to disable in CI or silent terminals.
    """

    if os.environ.get("DERBENT_SOUND_ENABLED", "true") != "true":
        return

    # Prefer freedesktop sounds when available; fall back to terminal bell.
    sound_map = {
        "start": "/usr/share/sounds/freedesktop/stereo/service-login.oga",
        "success": "/usr/share/sounds/freedesktop/stereo/complete.oga",
        # Use a longer sound for "all done" so it stands out from per-step completions.
        "all-done": "/usr/share/sounds/freedesktop/stereo/alarm-clock-elapsed.oga",
        "error": "/usr/share/sounds/freedesktop/stereo/dialog-error.oga",
    }
    path = sound_map.get(kind)
    if path and Path(path).exists():
        try:
            if subprocess.call(["paplay", path], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL) == 0:
                return
        except FileNotFoundError:
            pass

    # Terminal bell fallback (distinct patterns).
    if kind == "start":
        sys.stdout.write("\a")
    elif kind == "success":
        sys.stdout.write("\a\a")
    elif kind == "all-done":
        sys.stdout.write("\a\a\a\a\a")
    else:
        sys.stdout.write("\a\a\a")
    sys.stdout.flush()


def _slugify(value: str) -> str:
    value = value.strip().lower()
    value = re.sub(r"[^a-z0-9]+", "-", value)
    value = re.sub(r"^-+|-+$", "", value)
    return value or "task"


def _now_utc_iso_ms_z() -> str:
    dt = _dt.datetime.now(_dt.timezone.utc)
    return dt.strftime("%Y-%m-%dT%H:%M:%S.") + f"{dt.microsecond // 1000:03d}Z"


def _now_id() -> str:
    # keep filesystem-friendly id
    return _dt.datetime.now(_dt.timezone.utc).strftime("%Y%m%d-%H%M%S")


def _detect_profile(title: str, explicit: str | None) -> str:
    if explicit and explicit != "auto":
        return explicit

    t = title.lower()
    bab_markers = ["bab", "calimero", "gateway", "routing", "interfaces", "system metrics", "dns"]
    if any(m in t for m in bab_markers):
        return "bab"
    return "derbent"


def _write(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def cmd_new(args: argparse.Namespace) -> int:
    profile = _detect_profile(args.title, args.profile)
    task_id = args.id or f"{_now_id()}_{_slugify(args.title)[:48]}"

    task_dir = TASKS_ROOT / task_id
    if task_dir.exists():
        raise SystemExit(f"Task directory already exists: {task_dir}")

    (task_dir / "memory").mkdir(parents=True)
    (task_dir / "outputs").mkdir(parents=True)
    (task_dir / "logs").mkdir(parents=True)

    meta: dict[str, Any] = {
        "id": task_id,
        "title": args.title,
        "profile": profile,
        "createdAt": _now_utc_iso_ms_z(),
    }
    _write(task_dir / "meta.json", json.dumps(meta, indent=2) + "\n")

    _write(
        task_dir / "TASK.md",
        f"""# Task

<current_datetime>{_now_utc_iso_ms_z()}</current_datetime>

## Title
{args.title}

## Profile
{profile}

## Acceptance Criteria
- [ ] 

## Constraints
- [ ] Minimal diffs
- [ ] Profile separation (bab vs derbent)
- [ ] Follow AGENTS.md + .github/copilot-instructions.md
""",
    )

    for agent in AGENT_NAMES:
        _write(
            task_dir / "memory" / f"{agent}.md",
            f"""# {agent} memory

## Context
- Task: {args.title}
- Profile: {profile}

## Decisions

## Findings

## Next actions
""",
        )

    outputs = [
        ("10-analysis.md", "Analysis (Analyzer + Pattern Designer)"),
        ("20-design.md", "Design (Pattern Designer)"),
        ("30-implementation.md", "Implementation (Coder)"),
        ("40-verification.md", "Verification (Verifier)"),
        ("50-tests.md", "Tests (Tester)"),
        ("60-documentation.md", "Documentation (Documenter)"),
        ("70-todo.md", "TODO / Follow-ups (Todo-Fix)"),
        ("80-cleanup.md", "Cleanup Audit (Cleanup)"),
    ]
    for filename, title in outputs:
        _write(task_dir / "outputs" / filename, f"# {title}\n\n")

    print(str(task_dir))
    return 0


def _run(cmd: list[str], cwd: Path, log_file: Path) -> int:
    log_file.parent.mkdir(parents=True, exist_ok=True)
    with log_file.open("w", encoding="utf-8") as f:
        f.write(f"$ {' '.join(cmd)}\n\n")
        proc = subprocess.run(cmd, cwd=str(cwd), stdout=f, stderr=subprocess.STDOUT)
        return proc.returncode


def cmd_verify(args: argparse.Namespace) -> int:
    log_file = Path(args.log_file) if args.log_file else (REPO_ROOT / "tasks" / "agents" / "_last_build.log")

    _play_sound("start")

    commands: list[list[str]] = []
    if args.spotless_check:
        commands.append(["./mvnw", "-q", "-Pagents", "spotless:check"])
    commands.append(["./mvnw", "-q", "-Pagents", "-DskipTests", "clean", "compile"])

    rc = 0
    for cmd in commands:
        rc = _run(cmd, REPO_ROOT, log_file)
        if rc != 0:
            break

    print(str(log_file))
    _play_sound("all-done" if rc == 0 else "error")
    return rc


def cmd_selective_test(args: argparse.Namespace) -> int:
    script = REPO_ROOT / ".github" / "agents" / "verifier" / "scripts" / "test-selective.sh"
    if not script.exists():
        raise SystemExit(f"Missing script: {script}")

    log_file = Path(args.log_file) if args.log_file else (REPO_ROOT / "tasks" / "agents" / "_last_tests.log")
    cmd = ["bash", str(script), args.keyword]
    _play_sound("start")
    rc = _run(cmd, REPO_ROOT, log_file)
    print(str(log_file))
    _play_sound("all-done" if rc == 0 else "error")
    return rc


def cmd_kb(args: argparse.Namespace) -> int:
    script = REPO_ROOT / "scripts" / "kb_build.py"
    if not script.exists():
        raise SystemExit(f"Missing script: {script}")

    _play_sound("start")
    rc = subprocess.run([sys.executable, str(script)], cwd=str(REPO_ROOT)).returncode
    print(str(REPO_ROOT / "docs" / "knowledge" / "_generated"))
    _play_sound("all-done" if rc == 0 else "error")
    _play_sound("all-done" if rc == 0 else "error")
    return int(rc)


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(prog="agents", description="Derbent multi-agent task runner (task scaffolding + verification helpers)")
    sub = parser.add_subparsers(dest="cmd", required=True)

    p_new = sub.add_parser("new", help="Create a new task folder under tasks/agents/")
    p_new.add_argument("--title", required=True)
    p_new.add_argument("--profile", choices=["auto", "bab", "derbent"], default="auto")
    p_new.add_argument("--id", help="Optional explicit task id")
    p_new.set_defaults(func=cmd_new)

    p_verify = sub.add_parser("verify", help="Run fast build gates (spotless:check optional + compile)")
    p_verify.add_argument("--spotless-check", action="store_true")
    p_verify.add_argument("--log-file", help="Where to write build output")
    p_verify.set_defaults(func=cmd_verify)

    p_test = sub.add_parser("test", help="Run selective UI tests via verifier script")
    p_test.add_argument("keyword")
    p_test.add_argument("--log-file", help="Where to write test output")
    p_test.set_defaults(func=cmd_selective_test)

    p_kb = sub.add_parser("kb", help="Regenerate docs knowledge base indexes")
    p_kb.set_defaults(func=cmd_kb)

    args = parser.parse_args(argv)
    return int(args.func(args))


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
