#!/usr/bin/env bash
set -uo pipefail

ROOT="${PWD}"
DAYS_BACK=90
MAX_POINTS=30
WORKTREE=".loc-worktree"
OUTPUT_DIR="."

CSV="loc-3months.csv"
DELTA_CSV="loc-3months-delta.csv"
DELTA_AVG_CSV="loc-3months-delta-avg.csv"

PNG_COMBINED="loc-growth.png"

NO_GRAPHS=false
NO_OPEN=false
LOC_ONLY=false
REPORT_ONLY=false
USE_COLOR=true
USE_UNICODE=true

EXCLUDES=(.git target node_modules venv dist build out .idea .gradle .mvn .vscode)

export LC_NUMERIC=en_US.UTF-8

usage() {
    cat <<'USAGE'
Usage: ./scripts/project-report.sh [options] [days]

Options:
  --days N        Days back for git/LOC metrics (default: 90)
  --max-points N  Max LOC snapshots (default: 30)
  --loc-only      Only run LOC growth section
  --report-only   Only run project report sections (no LOC growth)
  --no-graphs     Skip gnuplot PNG generation
  --no-open       Do not open PNGs with xdg-open
  --no-color      Disable ANSI colors
  -h, --help      Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --days)
            [[ $# -lt 2 ]] && { echo "ERROR: --days requires a value" >&2; exit 1; }
            DAYS_BACK="$2"
            shift 2
            ;;
        --max-points)
            [[ $# -lt 2 ]] && { echo "ERROR: --max-points requires a value" >&2; exit 1; }
            MAX_POINTS="$2"
            shift 2
            ;;
        --loc-only)
            LOC_ONLY=true
            shift
            ;;
        --report-only)
            REPORT_ONLY=true
            shift
            ;;
        --no-graphs)
            NO_GRAPHS=true
            shift
            ;;
        --no-open)
            NO_OPEN=true
            shift
            ;;
        --no-color)
            USE_COLOR=false
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            if [[ "$1" =~ ^[0-9]+$ ]]; then
                DAYS_BACK="$1"
                shift
            else
                echo "ERROR: Unknown argument: $1" >&2
                usage >&2
                exit 1
            fi
            ;;
    esac
done

if ${LOC_ONLY} && ${REPORT_ONLY}; then
    echo "ERROR: --loc-only and --report-only cannot be used together" >&2
    exit 1
fi

has() {
    command -v "$1" >/dev/null 2>&1
}

if has git && git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
    ROOT=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
    cd "$ROOT" || exit 1
    OUTPUT_DIR="."
fi

if [[ ! -t 1 || -n "${NO_COLOR:-}" ]]; then
    USE_COLOR=false
fi

case "${LC_ALL:-}${LC_CTYPE:-}${LANG:-}" in
    *UTF-8*|*utf8*)
        USE_UNICODE=true
        ;;
    *)
        USE_UNICODE=false
        ;;
 esac

if [[ -n "${NO_UNICODE:-}" ]]; then
    USE_UNICODE=false
fi

if ${USE_COLOR}; then
    RED=$'\033[0;31m'
    GREEN=$'\033[0;32m'
    YELLOW=$'\033[1;33m'
    BLUE=$'\033[0;34m'
    CYAN=$'\033[0;36m'
    MAGENTA=$'\033[0;35m'
    WHITE=$'\033[1;37m'
    GRAY=$'\033[0;90m'
    BOLD=$'\033[1m'
    NC=$'\033[0m'
else
    RED=""
    GREEN=""
    YELLOW=""
    BLUE=""
    CYAN=""
    MAGENTA=""
    WHITE=""
    GRAY=""
    BOLD=""
    NC=""
fi

if ${USE_UNICODE}; then
    BOX_TL="╔"; BOX_TR="╗"; BOX_BL="╚"; BOX_BR="╝"; BOX_H="═"; BOX_V="║"
    SECT_TL="┌"; SECT_TR="┐"; SECT_BL="└"; SECT_BR="┘"; SECT_H="─"; SECT_V="│"
    BAR_SOLID="█"; BAR_MID="▓"; BAR_LIGHT="░"
    ELLIPSIS="…"
    CHECK="✓"
    SPARKS=("▁" "▂" "▃" "▄" "▅" "▆" "▇" "█")
else
    BOX_TL="+"; BOX_TR="+"; BOX_BL="+"; BOX_BR="+"; BOX_H="-"; BOX_V="|"
    SECT_TL="+"; SECT_TR="+"; SECT_BL="+"; SECT_BR="+"; SECT_H="-"; SECT_V="|"
    BAR_SOLID="#"; BAR_MID="#"; BAR_LIGHT="."
    ELLIPSIS="..."
    CHECK="OK"
    SPARKS=("." ":" "-" "=" "+" "*" "#" "#")
fi

HEADER_WIDTH=74
SECTION_WIDTH=74

repeat_char() {
    local char=$1 count=$2
    local out=""
    for ((i=0; i<count; i++)); do out+="$char"; done
    printf "%s" "$out"
}

clip_text() {
    local text=$1 max=$2
    if (( ${#text} > max )); then
        if (( max > 1 )); then
            printf "%s%s" "${text:0:$((max-1))}" "$ELLIPSIS"
        else
            printf "%s" "${text:0:max}"
        fi
    else
        printf "%s" "$text"
    fi
}

print_header() {
    local title=$1
    local line
    line=$(repeat_char "$BOX_H" "$HEADER_WIDTH")
    printf "%b%s%s%s%b\n" "$BLUE" "$BOX_TL" "$line" "$BOX_TR" "$NC"

    local content="  $title"
    local pad=$((HEADER_WIDTH - ${#content}))
    ((pad < 0)) && pad=0
    printf "%b%s%*s%b\n" "$BLUE$BOX_V$NC" "$WHITE$BOLD$content$NC" "$pad" "" "$BLUE$BOX_V$NC"

    printf "%b%s%s%s%b\n" "$BLUE" "$BOX_BL" "$line" "$BOX_BR" "$NC"
}

header_info() {
    local label=$1 value=$2
    printf "%b %s: %s%b\n" "$CYAN$SECT_V$NC" "$label" "$value" "$NC"
}

print_section() {
    local title=$1
    echo ""
    printf " %b%s%b \n" "$WHITE" "$title" "$NC"
    printf "%b%s%s%s%b\n" "$CYAN" "$SECT_TL" "$(repeat_char "$SECT_H" "$SECTION_WIDTH")" "$SECT_TR" "$NC"
}

section_line() {
    local text=$1
    local plain=${2:-$1}
    local pad=$((SECTION_WIDTH - ${#plain} - 1))
    ((pad < 0)) && pad=0
    printf "%b %s%*s%b\n" "$CYAN$SECT_V$NC" "$text" "$pad" "" "$CYAN$SECT_V$NC"
}

section_blank() {
    section_line "" ""
}

print_footer() {
    printf "%b%s%s%s%b\n" "$CYAN" "$SECT_BL" "$(repeat_char "$SECT_H" "$SECTION_WIDTH")" "$SECT_BR" "$NC"
}

draw_bar() {
    local value=$1 max=$2 width=${3:-25} fill=${4:-$BAR_MID} empty=${5:-$BAR_LIGHT}
    if [[ "$max" -le 0 ]]; then
        max=1
    fi
    local filled=$((value * width / max))
    ((filled > width)) && filled=$width
    local bar=""
    for ((i=0; i<filled; i++)); do bar+="$fill"; done
    for ((i=filled; i<width; i++)); do bar+="$empty"; done
    printf "%s" "$bar"
}

date_days_ago() {
    local days=$1
    date -d "$days days ago" +%Y-%m-%d 2>/dev/null || date -v-${days}d +%Y-%m-%d 2>/dev/null
}

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
    awk 'NF{n++} END{print n+0}' "$@" 2>/dev/null || printf '0\n'
}

count_lines_total() {
    if [[ "$#" -eq 0 ]]; then
        printf '0\n'
        return
    fi
    awk 'END{print NR+0}' "$@" 2>/dev/null || printf '0\n'
}

report_code_metrics() {
    print_section "📁 Codebase Overview"

    local file_count dir_count
    file_count=$(find_files | wc -l | tr -d ' ')
    dir_count=$(find_dirs | wc -l | tr -d ' ')
    if [[ "${dir_count}" -gt 0 ]]; then
        dir_count=$((dir_count - 1))
    fi

    mapfile -t JAVA_FILES < <(find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f -name '*.java' -print)
    mapfile -t JAVA_PROD_FILES < <(find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f -name '*.java' ! -path '*test*' -print)
    mapfile -t JAVA_TEST_FILES < <(find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f -name '*.java' \( -path '*test*' -o -path '*tests*' \) -print)
    mapfile -t DOC_FILES < <(find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f \( -name '*.md' -o -name '*.adoc' -o -name '*.rst' -o -name '*.txt' \) -print)
    mapfile -t CSS_FILES < <(find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f -name '*.css' -print)

    local java_lines_total java_lines_nonempty doc_lines_total doc_lines_nonempty css_lines_total css_lines_nonempty
    local java_prod_lines java_test_lines java_ratio_total

    java_lines_total=$(count_lines_total "${JAVA_FILES[@]}")
    java_lines_nonempty=$(count_lines_nonempty "${JAVA_FILES[@]}")

    java_prod_lines=$(count_lines_nonempty "${JAVA_PROD_FILES[@]}")
    java_test_lines=$(count_lines_nonempty "${JAVA_TEST_FILES[@]}")
    java_ratio_total=$((java_prod_lines + java_test_lines))

    doc_lines_total=$(count_lines_total "${DOC_FILES[@]}")
    doc_lines_nonempty=$(count_lines_nonempty "${DOC_FILES[@]}")
    css_lines_total=$(count_lines_total "${CSS_FILES[@]}")
    css_lines_nonempty=$(count_lines_nonempty "${CSS_FILES[@]}")

    local class_regex method_regex class_count method_count
    class_regex='^[[:space:]]*(public|protected|private|abstract|final|static)?[[:space:]]*(class|interface|enum|record)[[:space:]]+[A-Za-z_][A-Za-z0-9_]*'
    method_regex='^[[:space:]]*(@[A-Za-z_][A-Za-z0-9_]*[[:space:]]*)*([[:space:]]*(public|protected|private|static|final|abstract|synchronized|native|strictfp|default|sealed|non-sealed)[[:space:]]+)*[^;=]+[[:space:]]+[A-Za-z_][A-Za-z0-9_]*[[:space:]]*\([^;]*\)[[:space:]]*(\{|throws[[:space:]])'

    class_count=0
    method_count=0

    if has rg; then
        RG_EXCLUDES=()
        for dir in "${EXCLUDES[@]}"; do
            RG_EXCLUDES+=(--glob "!${dir}/**")
        done
        class_count=$(rg --no-heading -g '*.java' "${RG_EXCLUDES[@]}" "${class_regex}" . 2>/dev/null | wc -l | tr -d ' ')
        method_count=$(rg --no-heading -g '*.java' "${RG_EXCLUDES[@]}" "${method_regex}" . 2>/dev/null | wc -l | tr -d ' ')
    else
        GREP_EXCLUDES=()
        for dir in "${EXCLUDES[@]}"; do
            GREP_EXCLUDES+=(--exclude-dir="$dir")
        done
        class_count=$(grep -R -n -E "${GREP_EXCLUDES[@]}" --include='*.java' "${class_regex}" . 2>/dev/null | wc -l | tr -d ' ')
        method_count=$(grep -R -n -E "${GREP_EXCLUDES[@]}" --include='*.java' "${method_regex}" . 2>/dev/null | wc -l | tr -d ' ')
    fi

    local max_lines bar_prod bar_test bar_docs bar_css
    max_lines=$java_prod_lines
    if [[ "$java_test_lines" -gt "$max_lines" ]]; then max_lines=$java_test_lines; fi
    if [[ "$doc_lines_nonempty" -gt "$max_lines" ]]; then max_lines=$doc_lines_nonempty; fi
    if [[ "$css_lines_nonempty" -gt "$max_lines" ]]; then max_lines=$css_lines_nonempty; fi
    if [[ "$max_lines" -le 0 ]]; then max_lines=1; fi

    bar_prod=$(draw_bar "$java_prod_lines" "$max_lines" 20 "$BAR_SOLID" "$BAR_LIGHT")
    bar_test=$(draw_bar "$java_test_lines" "$max_lines" 20 "$BAR_SOLID" "$BAR_LIGHT")
    bar_docs=$(draw_bar "$doc_lines_nonempty" "$max_lines" 20 "$BAR_SOLID" "$BAR_LIGHT")
    bar_css=$(draw_bar "$css_lines_nonempty" "$max_lines" 20 "$BAR_SOLID" "$BAR_LIGHT")

    section_line "Java (prod):         ${#JAVA_PROD_FILES[@]} files  ${java_prod_lines} lines ${GREEN}${bar_prod}${NC}" "Java (prod):         ${#JAVA_PROD_FILES[@]} files  ${java_prod_lines} lines ${bar_prod}"
    section_line "Java (tests):        ${#JAVA_TEST_FILES[@]} files  ${java_test_lines} lines ${CYAN}${bar_test}${NC}" "Java (tests):        ${#JAVA_TEST_FILES[@]} files  ${java_test_lines} lines ${bar_test}"
    section_line "Documentation:       ${#DOC_FILES[@]} files  ${doc_lines_nonempty} lines ${MAGENTA}${bar_docs}${NC}" "Documentation:       ${#DOC_FILES[@]} files  ${doc_lines_nonempty} lines ${bar_docs}"
    section_line "CSS:                 ${#CSS_FILES[@]} files   ${css_lines_nonempty} lines ${GRAY}${bar_css}${NC}" "CSS:                 ${#CSS_FILES[@]} files   ${css_lines_nonempty} lines ${bar_css}"
    section_blank
    section_line "Total Java:          ${YELLOW}$((java_prod_lines + java_test_lines))${NC} lines" "Total Java:          $((java_prod_lines + java_test_lines)) lines"
    section_line "Classes:             ${CYAN}${class_count}${NC}" "Classes:             ${class_count}"
    section_line "Methods:             ${MAGENTA}${method_count}${NC}" "Methods:             ${method_count}"
    section_blank
    section_line "Files:               ${file_count}"
    section_line "Folders:             ${dir_count}"

    if [[ "${java_ratio_total}" -gt 0 ]]; then
        local test_pct prod_pct prod_w test_w
        test_pct=$((java_test_lines * 100 / java_ratio_total))
        prod_pct=$((100 - test_pct))
        prod_w=$((prod_pct * 35 / 100))
        test_w=$((35 - prod_w))

        local ratio=""
        for ((i=0; i<prod_w; i++)); do ratio+="$BAR_MID"; done
        for ((i=0; i<test_w; i++)); do ratio+="$BAR_MID"; done

        local ratio_plain="Code/Test Ratio:     ${ratio} ${prod_pct}%/${test_pct}%"
        local ratio_colored="Code/Test Ratio:     ${BLUE}${ratio:0:prod_w}${CYAN}${ratio:prod_w}${NC} ${BLUE}${prod_pct}%${NC}/${CYAN}${test_pct}%${NC}"
        section_line "$ratio_colored" "$ratio_plain"
    fi

    if ! has rg; then
        section_blank
        section_line "Note: rg not found; using grep fallback for class/method counts."
    fi

    print_footer
}

report_git_summary() {
    print_section "📈 Git Statistics"
    if ! has git || ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
        section_line "Git: Not a git repository or git not available."
        print_footer
        return
    fi

    local total_commits recent_commits total_authors first_commit last_commit
    total_commits=$(git rev-list --count HEAD 2>/dev/null || echo "0")
    recent_commits=$(git rev-list --count --since="${DAYS_BACK} days ago" HEAD 2>/dev/null || echo "0")
    total_authors=$(git shortlog -sne HEAD 2>/dev/null | wc -l | tr -d ' ')
    first_commit=$(git log --reverse --format="%cs" 2>/dev/null | head -1)
    last_commit=$(git log -1 --format="%cs" 2>/dev/null)

    section_line "Total commits:               ${total_commits}"
    section_line "Commits (${DAYS_BACK} days):           ${recent_commits}"
    section_line "Contributors:                ${total_authors}"
    section_line "First commit:                ${first_commit}"
    section_line "Last commit:                 ${last_commit}"

    print_footer
}

top_contributors() {
    print_section "👥 Top Contributors (${DAYS_BACK} days)"
    if ! has git || ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
        section_line "Git: Not a git repository or git not available."
        print_footer
        return
    fi

    local recent_commits max_author
    recent_commits=$(git rev-list --count --since="${DAYS_BACK} days ago" HEAD 2>/dev/null || echo "0")
    if [[ "$recent_commits" -eq 0 ]]; then recent_commits=1; fi

    max_author=$(git --no-pager shortlog -sne --since="${DAYS_BACK} days ago" HEAD 2>/dev/null | head -1 | awk '{print $1+0}')
    if [[ -z "$max_author" || "$max_author" -eq 0 ]]; then max_author=1; fi

    git --no-pager shortlog -sne --since="${DAYS_BACK} days ago" HEAD 2>/dev/null | head -6 | \
    while read -r count name; do
        local pct bar name_clip plain colored
        pct=$((count * 100 / recent_commits))
        bar=$(draw_bar "$count" "$max_author" 25 "$BAR_SOLID" "$BAR_LIGHT")
        name_clip=$(clip_text "$name" 32)
        plain=$(printf "%4d %s %-32s %3d%%" "$count" "$bar" "$name_clip" "$pct")
        colored=$(printf "%4d %b%s%b %b%-32s%b %3d%%" "$count" "$GREEN" "$bar" "$NC" "$YELLOW" "$name_clip" "$NC" "$pct")
        section_line "$colored" "$plain"
    done

    print_footer
}

weekly_activity() {
    print_section "📆 Weekly Activity"
    if ! has git || ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
        section_line "Git: Not a git repository or git not available."
        print_footer
        return
    fi

    local num_weeks
    num_weeks=$((DAYS_BACK / 7))
    if [[ "$num_weeks" -lt 1 ]]; then num_weeks=1; fi

    declare -a week_commits=()
    declare -a week_labels=()

    for i in $(seq $((num_weeks - 1)) -1 0); do
        local start_date end_date commits
        start_date=$(date_days_ago $((i * 7 + 7)))
        end_date=$(date_days_ago $((i * 7)))
        commits=$(git rev-list --count --since="$start_date" --until="$end_date" HEAD 2>/dev/null || echo 0)
        week_commits+=($commits)
        week_labels+=("W$((num_weeks - i))")
    done

    local max_week=1
    for c in "${week_commits[@]}"; do
        if [[ "$c" -gt "$max_week" ]]; then max_week=$c; fi
    done

    for row in $(seq 6 -1 1); do
        local line=""
        for c in "${week_commits[@]}"; do
            local h
            if [[ "$max_week" -eq 0 ]]; then h=0; else h=$((c * 6 / max_week)); fi
            if [[ "$h" -ge "$row" ]]; then
                line+="${BAR_MID}${BAR_MID} "
            else
                line+="   "
            fi
        done
        section_line "  ${GREEN}${line}${NC}" "  ${line}"
    done

    local lbls=""
    local nums=""
    for lbl in "${week_labels[@]}"; do lbls+="$lbl "; done
    for c in "${week_commits[@]}"; do nums+="$c "; done

    section_line "  ${lbls}" "  ${lbls}"
    section_line "  ${nums}" "  ${nums}"

    print_footer
}

weekly_loc_growth() {
    print_section "📈 LOC Growth (Weekly)"
    if ! has git || ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
        section_line "Git: Not a git repository or git not available."
        print_footer
        return
    fi

    local num_weeks
    num_weeks=$((DAYS_BACK / 7))
    if [[ "$num_weeks" -lt 1 ]]; then num_weeks=1; fi

    declare -a week_net=()
    declare -a week_labels=()

    for i in $(seq $((num_weeks - 1)) -1 0); do
        local start_date end_date stats wa wr wnet
        start_date=$(date_days_ago $((i * 7 + 7)))
        end_date=$(date_days_ago $((i * 7)))

        stats=$(git log --since="$start_date" --until="$end_date" --pretty=tformat: --numstat -- '*.java' '*.kt' 2>/dev/null | \
            awk '{ a += $1; r += $2 } END { printf "%d %d", a+0, r+0 }')
        wa=$(echo "$stats" | cut -d' ' -f1)
        wr=$(echo "$stats" | cut -d' ' -f2)
        wnet=$((wa - wr))

        week_net+=($wnet)
        week_labels+=("W$((num_weeks - i))")
    done

    local max_net=1
    for n in "${week_net[@]}"; do
        local abs_n=${n#-}
        if [[ "$abs_n" -gt "$max_net" ]]; then max_net=$abs_n; fi
    done

    local idx=0
    for n in "${week_net[@]}"; do
        local lbl bar_len bar abs_n
        lbl="${week_labels[$idx]}"
        if [[ "$n" -ge 0 ]]; then
            bar_len=$((n * 20 / max_net))
            ((bar_len > 20)) && bar_len=20
            bar=""
            for ((j=0; j<bar_len; j++)); do bar+="$BAR_SOLID"; done
            section_line "${lbl}  +$(printf '%5d' "$n") ${GREEN}${bar}${NC}" "${lbl}  +$(printf '%5d' "$n") ${bar}"
        else
            abs_n=${n#-}
            bar_len=$((abs_n * 20 / max_net))
            ((bar_len > 20)) && bar_len=20
            bar=""
            for ((j=0; j<bar_len; j++)); do bar+="$BAR_SOLID"; done
            section_line "${lbl}  $(printf '%6d' "$n") ${RED}${bar}${NC}" "${lbl}  $(printf '%6d' "$n") ${bar}"
        fi
        ((idx++))
    done

    print_footer
}

daily_activity() {
    print_section "📅 Daily Activity (Last 14 days)"
    if ! has git || ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
        section_line "Git: Not a git repository or git not available."
        print_footer
        return
    fi

    local days=14
    declare -a daily_commits=()
    for i in $(seq $((days - 1)) -1 0); do
        local date_str commits
        date_str=$(date_days_ago "$i")
        commits=$(git rev-list --count --after="$date_str 00:00" --before="$date_str 23:59:59" HEAD 2>/dev/null || echo 0)
        daily_commits+=($commits)
    done

    local max_daily=1
    for c in "${daily_commits[@]}"; do
        if [[ "$c" -gt "$max_daily" ]]; then max_daily=$c; fi
    done

    local spark=""
    for c in "${daily_commits[@]}"; do
        local h
        if [[ "$max_daily" -gt 0 ]]; then
            h=$((c * 7 / max_daily))
        else
            h=0
        fi
        spark+="${SPARKS[$h]}"
    done

    section_line "Commits: ${GREEN}${spark}${NC}" "Commits: ${spark}"
    section_blank
    section_line "By Day of Week:" "By Day of Week:"

    declare -a dow_counts=(0 0 0 0 0 0 0)
    declare -a dow_names=("Sun" "Mon" "Tue" "Wed" "Thu" "Fri" "Sat")

    while read -r dow; do
        [[ "$dow" =~ ^[0-6]$ ]] && dow_counts[$dow]=$((dow_counts[$dow] + 1))
    done < <(git log --since="${DAYS_BACK} days ago" --format='%ad' --date=format:'%w' 2>/dev/null)

    local max_dow=1
    for c in "${dow_counts[@]}"; do
        if [[ "$c" -gt "$max_dow" ]]; then max_dow=$c; fi
    done

    for i in $(seq 0 6); do
        local c bar_len bar
        c=${dow_counts[$i]}
        bar_len=$((c * 20 / max_dow))
        ((bar_len > 20)) && bar_len=20
        bar=""
        for ((j=0; j<bar_len; j++)); do bar+="$BAR_MID"; done
        section_line "  ${dow_names[$i]}  $(printf '%3d' "$c") ${CYAN}${bar}${NC}" "  ${dow_names[$i]}  $(printf '%3d' "$c") ${bar}"
    done

    print_footer
}

count_loc() {
    mapfile -t PROD < <(
        find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f \
            \( -name '*.java' -o -name '*.kt' \) \
            ! -path '*test*' -print
    )

    mapfile -t TEST < <(
        find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f \
            \( -name '*.java' -o -name '*.kt' \) \
            \( -path '*test*' -o -path '*tests*' \) -print
    )

    local prod test
    prod=$(count_lines_nonempty "${PROD[@]}")
    test=$(count_lines_nonempty "${TEST[@]}")

    echo "${prod},${test},$((prod + test))"
}

loc_growth() {
    print_section "📈 LOC Growth (Snapshots)"

    if ! has git || ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
        section_line "Git: Not a git repository or git not available."
        print_footer
        return
    fi

    mapfile -t COMMITS < <(git rev-list --since="${DAYS_BACK} days ago" --reverse HEAD)
    local total
    total=${#COMMITS[@]}

    if [[ "$total" -eq 0 ]]; then
        section_line "Commits: No commits in the last ${DAYS_BACK} days."
        print_footer
        return
    fi

    local step snapshots
    step=$(( total / MAX_POINTS ))
    (( step < 1 )) && step=1
    snapshots=0

    echo "date,commit,prod_loc,test_loc,total_loc" > "$CSV"

    rm -rf "$WORKTREE" >/dev/null 2>&1 || true

    for ((i=0; i<total; i+=step)); do
        local commit date loc prod test total_loc
        commit="${COMMITS[$i]}"

        if ! git worktree add -q "$WORKTREE" "$commit" >/dev/null 2>&1; then
            continue
        fi

        pushd "$WORKTREE" >/dev/null
        date=$(git show -s --format=%cs)
        loc=$(count_loc)
        popd >/dev/null

        git worktree remove -f "$WORKTREE" >/dev/null 2>&1 || true
        rm -rf "$WORKTREE" >/dev/null 2>&1 || true

        IFS=',' read -r prod test total_loc <<< "$loc"

        printf "%s,%s,%s,%s,%s\n" "$date" "${commit:0:7}" "$prod" "$test" "$total_loc" >> "$CSV"
        snapshots=$((snapshots + 1))
    done

    section_line "LOC snapshots written:   ${CSV} (${snapshots} points)"

    echo "date,delta_loc" > "$DELTA_CSV"
    awk -F',' 'NR==2 {prev=$5; next} NR>2 {print $1 "," ($5-prev); prev=$5}' "$CSV" >> "$DELTA_CSV"

    echo "date,avg_delta" > "$DELTA_AVG_CSV"
    awk -F',' 'NR>1 {vals[NR]=$2; sum+=$2; if (NR>5) sum-=vals[NR-5]; if (NR>=6) print $1 "," sum/5}' "$DELTA_CSV" >> "$DELTA_AVG_CSV"

    print_footer

    print_section "📊 Generating PNG Graphs"

    if ${NO_GRAPHS}; then
        section_line "Graphs: Skipped (--no-graphs)"
        print_footer
        return
    fi

    if has gnuplot; then
        gnuplot <<GNUPLOT_EOF
set datafile separator ","
set terminal pngcairo size 1400,1000
set grid
set xdata time
set timefmt "%Y-%m-%d"
set format x "%b %d"
set decimal locale
set format y "%'.0f"

set output "${PNG_COMBINED}"
set multiplot layout 2,1 title "LOC Growth Overview (Last ${DAYS_BACK} days)"

set title "Total LOC Growth"
set xlabel "Date"
set ylabel "Lines of Code"
plot "${CSV}" using 1:5 with linespoints lw 3 title "Total LOC"

set title "LOC Velocity (Delta LOC Between Snapshots)"
set xlabel "Date"
set ylabel "LOC Change"
plot \
  "${DELTA_CSV}" using 1:2 with impulses lw 4 title "Delta LOC", \
  "${DELTA_AVG_CSV}" using 1:2 with lines lw 3 title "Velocity Trend (MA)"

unset multiplot
GNUPLOT_EOF

        section_line "${CHECK} ${PNG_COMBINED}"

        if ! ${NO_OPEN} && has xdg-open; then
            section_blank
            section_line "Opening LOC growth graph..."
            xdg-open "$PNG_COMBINED" >/dev/null 2>&1 &
        fi
    else
        section_line "Warning: gnuplot not installed (PNG skipped)"
    fi

    print_footer
}

if ${LOC_ONLY}; then
    print_header "📊 DERBENT LOC REPORT (Last ${DAYS_BACK} days)"
else
    print_header "📊 DERBENT PROJECT METRICS (Last ${DAYS_BACK} days)"
fi

header_info "Directory" "$ROOT"
header_info "Date" "$(date '+%Y-%m-%d %H:%M')"
header_info "Output" "$OUTPUT_DIR"

echo ""

if ! ${REPORT_ONLY}; then
    loc_growth
fi

if ! ${LOC_ONLY}; then
    report_git_summary
    report_code_metrics
    top_contributors
    weekly_activity
    weekly_loc_growth
    daily_activity
fi
