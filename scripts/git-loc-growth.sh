#!/usr/bin/env bash
set -euo pipefail

############################################
# CONFIG
############################################
COMMITS=20          # kaç snapshot
STEP=50             # her kaç committe bir ölçüm
OUTPUT="loc-growth.csv"

EXCLUDES=(.git target node_modules venv dist build out .idea .gradle .mvn .vscode)

############################################
# HELPERS
############################################
build_prune_expr() {
  local expr=()
  for d in "${EXCLUDES[@]}"; do
    expr+=(-name "$d" -o)
  done
  unset 'expr[${#expr[@]}-1]'
  printf '%s\n' "${expr[@]}"
}
mapfile -t PRUNE_EXPR < <(build_prune_expr)

count_lines_nonempty() {
  awk 'NF{n++} END{print n+0}' "$@"
}
count_lines_total() {
  awk 'END{print NR+0}' "$@"
}

count_loc() {
  mapfile -t JAVA_FILES < <(
    find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f -name '*.java' -print
  )
  mapfile -t DOC_FILES < <(
    find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f \
      \( -name '*.md' -o -name '*.adoc' -o -name '*.rst' -o -name '*.txt' \) -print
  )
  mapfile -t CSS_FILES < <(
    find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f -name '*.css' -print
  )

  java=$(count_lines_nonempty "${JAVA_FILES[@]}")
  docs=$(count_lines_nonempty "${DOC_FILES[@]}")
  css=$(count_lines_nonempty "${CSS_FILES[@]}")

  total=$((java + docs + css))

  echo "$java,$docs,$css,$total"
}

############################################
# PREPARE
############################################
echo "ref,date,java_loc,docs_loc,css_loc,total_loc" > "$OUTPUT"

printf "\n📈 LOC GROWTH OVER TIME (commit-based)\n"
printf "===============================================================\n"
printf "%-10s %-12s %10s %10s %8s %10s\n" \
  "COMMIT" "DATE" "JAVA" "DOCS" "CSS" "TOTAL"
printf "---------------------------------------------------------------\n"

############################################
# SNAPSHOT LOOP
############################################
mapfile -t COMMITS_LIST < <(
  git rev-list --reverse HEAD | awk "NR % $STEP == 0 {print}" | head -n "$COMMITS"
)

for COMMIT in "${COMMITS_LIST[@]}"; do
  git checkout -q "$COMMIT"

  DATE=$(git show -s --format=%cs)
  LOC=$(count_loc)

  IFS=',' read -r JAVA DOCS CSS TOTAL <<< "$LOC"

  printf "%-10s %-12s %10d %10d %8d %10d\n" \
    "${COMMIT:0:8}" "$DATE" "$JAVA" "$DOCS" "$CSS" "$TOTAL"

  echo "${COMMIT:0:8},$DATE,$JAVA,$DOCS,$CSS,$TOTAL" >> "$OUTPUT"
done

git checkout -q -

printf "===============================================================\n"
printf "CSV written to: %s\n\n" "$OUTPUT"
