"""
Sandbox helpers (optional imports for future expansion). Currently used by telegram_bot for whitelist checking and safe operations.
"""
import shlex
from typing import List


def split_command(cmd: str) -> List[str]:
    return shlex.split(cmd)


def is_safe_command(cmd_list: List[str], whitelist: List[str]) -> bool:
    if not cmd_list:
        return False
    return cmd_list[0] in whitelist
