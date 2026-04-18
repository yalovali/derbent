#!/usr/bin/env python3

from __future__ import annotations

import datetime as _dt
from pathlib import Path
import re


def _now_utc_iso_ms_z() -> str:
    dt = _dt.datetime.now(_dt.timezone.utc)
    return dt.strftime("%Y-%m-%dT%H:%M:%S.") + f"{dt.microsecond // 1000:03d}Z"


REPO_ROOT = Path(__file__).resolve().parent.parent
OUT_FILE = REPO_ROOT / "docs" / "knowledge" / "_generated" / "cleanup-suggestions.md"


def _rel(p: Path) -> str:
    return p.relative_to(REPO_ROOT).as_posix()


def _suggest_target(rel: str) -> str:
    name = Path(rel).name
    upper = name.upper()

    if upper.startswith("BAB_") or "CALIMERO" in upper:
        return "docs/bab/"
    if upper.startswith("VALIDATION_") or "VALIDATION" in upper:
        return "docs/architecture/"  # rules/patterns
    if upper.startswith("TESTING_") or "PLAYWRIGHT" in upper:
        return "docs/testing/"
    if upper.startswith("EMAIL_"):
        return "docs/features/"  # feature family (can later split)
    if upper.startswith("MENU_"):
        return "docs/archive/"  # historical/menu migrations often archived

    # Default: archive root-level historical summaries unless deliberately curated
    return "docs/archive/"


def main() -> int:
    OUT_FILE.parent.mkdir(parents=True, exist_ok=True)

    now = _now_utc_iso_ms_z()

    keep_root = {
        "README.md",
        "AGENTS.md",
        "CODE_OF_CONDUCT.md",
        "CONTRIBUTING.md",
        "SECURITY.md",
        "LICENSE",
    }
    root_md = [p for p in REPO_ROOT.glob("*.md") if p.name not in keep_root]
    root_md.sort(key=lambda p: p.name.lower())

    lines: list[str] = []
    lines.append("# Cleanup suggestions (root-level markdown)\n")
    lines.append(f"<current_datetime>{now}</current_datetime>\n")
    lines.append("This is an **audit report only**. No files are moved automatically.\n")
    lines.append("## Suggested moves\n")

    for p in root_md:
        rel = _rel(p)
        target_dir = _suggest_target(rel)
        lines.append(f"- `{rel}` → `{target_dir}`")
        lines.append(f"  - cmd: `git mv {rel} {target_dir}`")

    lines.append("\n## Notes\n")
    lines.append("- Prefer moving docs into `docs/**` over keeping them at repo root.")
    lines.append("- If a doc is truly obsolete, move it under `docs/archive/**` (do not delete).")

    OUT_FILE.write_text("\n".join(lines).rstrip() + "\n", encoding="utf-8")
    print(str(OUT_FILE))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
