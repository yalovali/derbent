#!/usr/bin/env bash
set -euo pipefail

AUTHOR="Yasin YILMAZ"

DAYS=14
WEEKS=8
MONTHS=6

EXCLUDES=(.git target node_modules venv dist build out .idea .gradle .mvn .vscode)

build_prune_expr() {
  local expr=()
  for d in "${EXCLUDES[@]}"; do
    expr+=(-name "$d" -o)
  done
  unset 'expr[${#expr[@]}-1]'
  printf '%s\n' "${expr[@]}"
}

mapfile -t PRUNE_EXPR < <(build_prune_expr)

count_loc() {
  find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f \
    \( -name '*.java' -o -name '*.cpp' -o -name '*.h' -o -name '*.hpp' -o -name '*.py' -o -name '*.js' -o -name '*.ts' \) \
    -print0 \
  | awk -v RS='\0' '
      {
        while ((getline line < $0) > 0) {
          tot++
          if (line ~ /[^[:space:]]/) ne++
        }
        close($0)
      }
      END {
        printf "Total LOC (non-empty): %d\nTotal LOC (all)      : %d\n", ne, tot
      }'
}


print_git_summary() {
  local SINCE="$1"
  local LABEL="$2"

  git log \
    --since="$SINCE" \
    --author="$AUTHOR" \
    --no-merges \
    --pretty=tformat: \
    --numstat \
  | awk -v label="$LABEL" '
      { add+=$1; del+=$2 }
      END {
        printf "%-12s | Added: %-7d Deleted: %-7d Net: %-7d\n",
               label, add+0, del+0, (add-del)+0
      }'
}

echo "======================================================"
echo " GIT CODE CHANGE + TOTAL LOC REPORT"
echo " Author    : $AUTHOR"
echo " Generated : $(date)"
echo "======================================================"
echo

echo "📌 CURRENT PROJECT SIZE"
echo "------------------------------------------------------"
count_loc
echo

echo "📅 DAILY"
echo "------------------------------------------------------"
for ((i=0;i<DAYS;i++)); do
  DAY=$(date -d "$i day ago" +"%Y-%m-%d")
  print_git_summary "$DAY 00:00" "$DAY"
done
echo

echo "📆 WEEKLY"
echo "------------------------------------------------------"
for ((i=0;i<WEEKS;i++)); do
  START=$(date -d "$i week ago" +"%Y-%m-%d")
  print_git_summary "$START" "Week -$i"
done
echo

echo "🗓️ MONTHLY"
echo "------------------------------------------------------"
for ((i=0;i<MONTHS;i++)); do
  START=$(date -d "$i month ago" +"%Y-%m")
  print_git_summary "$START" "Month -$i"
done

echo
echo "======================================================"

