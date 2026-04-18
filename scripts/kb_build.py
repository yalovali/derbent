#!/usr/bin/env python3

from __future__ import annotations

import datetime as _dt
from dataclasses import dataclass
from pathlib import Path
import re
from typing import Iterable


def _now_utc_iso_ms_z() -> str:
    dt = _dt.datetime.now(_dt.timezone.utc)
    # Example: 2026-04-18T16:00:49.060Z
    return dt.strftime("%Y-%m-%dT%H:%M:%S.") + f"{dt.microsecond // 1000:03d}Z"


REPO_ROOT = Path(__file__).resolve().parent.parent
OUT_DIR = REPO_ROOT / "docs" / "knowledge" / "_generated"


@dataclass(frozen=True)
class DocItem:
    path: Path
    title: str


def _rel(p: Path) -> str:
    return p.relative_to(REPO_ROOT).as_posix()


def _first_heading(md_text: str) -> str | None:
    for line in md_text.splitlines():
        m = re.match(r"^#\s+(.+?)\s*$", line)
        if m:
            return m.group(1).strip()
    return None


def _read_title(path: Path) -> str:
    try:
        text = path.read_text(encoding="utf-8", errors="ignore")
    except Exception:
        return path.stem

    heading = _first_heading(text)
    return heading or path.stem


def _iter_markdown_files() -> Iterable[Path]:
    # docs/** (excluding docs/archive + docs/knowledge/_generated)
    for p in (REPO_ROOT / "docs").rglob("*.md"):
        rel = _rel(p)
        if rel.startswith("docs/archive/"):
            continue
        if rel.startswith("docs/knowledge/_generated/"):
            continue
        yield p

    # additional profile docs outside docs/
    extra_roots = [
        REPO_ROOT / "bab" / "docs",
        REPO_ROOT / "src" / "main" / "java" / "tech" / "derbent" / "bab",
    ]
    for root in extra_roots:
        if not root.exists():
            continue
        for p in root.rglob("*.md"):
            yield p

    # repo root *.md (curated by naming)
    for p in REPO_ROOT.glob("*.md"):
        if p.name in {"README.md", "LICENSE"}:
            continue
        yield p


def _category_for(rel: str) -> str:
    r = rel.lower()

    bab_markers = [
        "docs/bab/",
        "bab/docs/",
        "/tech/derbent/bab/",
        "calimero",
        "bab_",
    ]
    if any(m in r for m in bab_markers):
        return "bab"

    plm_markers = [
        "docs/features/",
        "docs/implementation/",
        "/tech/derbent/plm/",
        "kanban",
        "gantt",
        "storage",
        "activity",
        "meeting",
        "crm",
    ]
    if any(m in r for m in plm_markers):
        return "plm-derbent"

    api_markers = [
        "docs/architecture/",
        "docs/development/",
        "docs/standards/",
        "docs/components/",
        "docs/testing/",
        "docs/configuration/",
        "docs/patterns/",
        "/tech/derbent/api/",
        "coding-standards",
        "validation",
    ]
    if any(m in r for m in api_markers):
        return "api-common"

    return "misc"


def _write_index(category: str, items: list[DocItem]) -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)

    now = _now_utc_iso_ms_z()
    out_file = OUT_DIR / f"{category}.md"

    lines: list[str] = []
    lines.append(f"# Knowledge Base Index: {category}\n")
    lines.append(f"<current_datetime>{now}</current_datetime>\n")
    lines.append(f"Total: **{len(items)}**\n")

    # group by parent folder (relative)
    groups: dict[str, list[DocItem]] = {}
    for it in items:
        parent = _rel(it.path.parent)
        groups.setdefault(parent, []).append(it)

    def link_for(rel_path: str) -> str:
        # Generated index lives at: docs/knowledge/_generated/<file>.md
        # - docs/* links should be relative to docs/: ../../<path-without-docs/>
        # - repo-root links should be relative to repo root: ../../../<path>
        if rel_path.startswith("docs/"):
            return "../../" + rel_path.removeprefix("docs/")
        return "../../../" + rel_path

    for parent in sorted(groups.keys()):
        lines.append(f"## {parent}\n")
        for it in sorted(groups[parent], key=lambda x: x.title.lower()):
            rel_path = _rel(it.path)
            lines.append(f"- [{it.title}]({link_for(rel_path)})")
        lines.append("")

    out_file.write_text("\n".join(lines).rstrip() + "\n", encoding="utf-8")


def main() -> int:
    docs: list[DocItem] = []
    for p in _iter_markdown_files():
        rel = _rel(p)
        docs.append(DocItem(path=p, title=_read_title(p)))

    by_cat: dict[str, list[DocItem]] = {"api-common": [], "plm-derbent": [], "bab": [], "misc": []}
    for d in docs:
        by_cat[_category_for(_rel(d.path))].append(d)

    for cat, items in by_cat.items():
        _write_index(cat, items)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
