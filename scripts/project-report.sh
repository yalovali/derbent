#!/usr/bin/env bash
set -euo pipefail

ROOT="${PWD}"
EXCLUDES=(.git target node_modules venv dist build out .idea .gradle .mvn .vscode)

has() {
    command -v "$1" >/dev/null 2>&1
}

COLOR_RESET=""
COLOR_TITLE=""
COLOR_LABEL=""
COLOR_VALUE=""
COLOR_WARN=""

build_prune_expr() {
    local expr=()
    for dir in "${EXCLUDES[@]}"; do
        expr+=(-name "$dir" -o)
    done
    unset 'expr[${#expr[@]}-1]'
    printf '%s\n' "${expr[@]}"
}

mapfile -t PRUNE_EXPR < <(build_prune_expr)

find_files() {
    find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f -print
}

find_dirs() {
    find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type d -print
}

count_lines_nonempty() {
    if [[ "$#" -eq 0 ]]; then
        printf '0\n'
        return
    fi
    awk 'NF{n++} END{print n+0}' "$@"
}

count_lines_total() {
    if [[ "$#" -eq 0 ]]; then
        printf '0\n'
        return
    fi
    awk 'END{print NR+0}' "$@"
}

HEADER_LINE="============================================================"
printf "%s\n" "${HEADER_LINE}"
printf "PROJECT REPORT\n"
printf "%s\n" "${HEADER_LINE}"
printf "ROOT:\t%s\n" "${ROOT}"
printf "%s\n" "${HEADER_LINE}"

file_count=$(find_files | wc -l | tr -d ' ')
dir_count=$(find_dirs | wc -l | tr -d ' ')
if [[ "${dir_count}" -gt 0 ]]; then
    dir_count=$((dir_count - 1))
fi

mapfile -t JAVA_FILES < <(find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f -name '*.java' -print)
mapfile -t DOC_FILES < <(find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f \( -name '*.md' -o -name '*.adoc' -o -name '*.rst' -o -name '*.txt' \) -print)
mapfile -t CSS_FILES < <(find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f -name '*.css' -print)

java_lines_total=$(count_lines_total "${JAVA_FILES[@]}")
java_lines_nonempty=$(count_lines_nonempty "${JAVA_FILES[@]}")
doc_lines_total=$(count_lines_total "${DOC_FILES[@]}")
doc_lines_nonempty=$(count_lines_nonempty "${DOC_FILES[@]}")
css_lines_total=$(count_lines_total "${CSS_FILES[@]}")
css_lines_nonempty=$(count_lines_nonempty "${CSS_FILES[@]}")

class_regex='^[[:space:]]*(public|protected|private|abstract|final|static)?[[:space:]]*(class|interface|enum|record)[[:space:]]+[A-Za-z_][A-Za-z0-9_]*'
method_regex='^[[:space:]]*(@[A-Za-z_][A-Za-z0-9_]*[[:space:]]*)*([[:space:]]*(public|protected|private|static|final|abstract|synchronized|native|strictfp|default|sealed|non-sealed)[[:space:]]+)*[^;=]+[[:space:]]+[A-Za-z_][A-Za-z0-9_]*[[:space:]]*\([^;]*\)[[:space:]]*(\{|throws[[:space:]])'

class_count=0
method_count=0

if has rg; then
    RG_EXCLUDES=()
    for dir in "${EXCLUDES[@]}"; do
        RG_EXCLUDES+=(--glob "!${dir}/**")
    done
    class_count=$(rg --no-heading -g '*.java' "${RG_EXCLUDES[@]}" "${class_regex}" . | wc -l | tr -d ' ')
    method_count=$(rg --no-heading -g '*.java' "${RG_EXCLUDES[@]}" "${method_regex}" . | wc -l | tr -d ' ')
else
    GREP_EXCLUDES=()
    for dir in "${EXCLUDES[@]}"; do
        GREP_EXCLUDES+=(--exclude-dir="$dir")
    done
    class_count=$(grep -R -n -E "${GREP_EXCLUDES[@]}" --include='*.java' "${class_regex}" . | wc -l | tr -d ' ')
    method_count=$(grep -R -n -E "${GREP_EXCLUDES[@]}" --include='*.java' "${method_regex}" . | wc -l | tr -d ' ')
fi

printf "CODE METRICS\n"
printf "%s\n" "${HEADER_LINE}"
printf "Java files:\t%s\n" "${#JAVA_FILES[@]}"
printf "Java lines:\t%s (non-empty), %s (total)\n" "${java_lines_nonempty}" "${java_lines_total}"
printf "Classes:\t%s\n" "${class_count}"
printf "Methods:\t%s\n" "${method_count}"
printf "Docs files:\t%s\n" "${#DOC_FILES[@]}"
printf "Docs lines:\t%s (non-empty), %s (total)\n" "${doc_lines_nonempty}" "${doc_lines_total}"
printf "CSS files:\t%s\n" "${#CSS_FILES[@]}"
printf "CSS lines:\t%s (non-empty), %s (total)\n" "${css_lines_nonempty}" "${css_lines_total}"
printf "Files:\t\t%s\n" "${file_count}"
printf "Folders:\t%s\n" "${dir_count}"
printf "%s\n" "${HEADER_LINE}"

if ! has rg; then
    printf "Note:\trg not found; using grep fallback for class/method counts.\n"
fi

printf "GIT COMMITS (GROUPED)\n"
printf "%s\n" "${HEADER_LINE}"
if has git && git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
    if has awk; then
        git shortlog -sne HEAD | awk '{count=$1; $1=""; $2=""; line=$0; lower=tolower(line); if (lower ~ /yasin|yalovali/) yasin+=count; else if (lower ~ /codex/) codex+=count; else if (lower ~ /copilot/) copilot+=count; else others+=count} END {printf "Yasin:\t%d\nCodex:\t%d\nCopilot:\t%d\nOthers:\t%d\n", yasin+0, codex+0, copilot+0, others+0}'
    else
        printf "Grouped summary:\tN/A (awk not available)\n"
    fi
else
    printf "Git:\tNot a git repository or git not available.\n"
fi

printf "%s\n" "${HEADER_LINE}"
