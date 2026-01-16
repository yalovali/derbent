#!/usr/bin/env bash
set -euo pipefail

############################################
# CONFIG
############################################
DAYS_BACK=90
MAX_POINTS=30                 # grafikte kaç nokta olsun
WORKTREE=".loc-worktree"

CSV="loc-3months.csv"
PNG_TOTAL="loc-total.png"
PNG_STACK="loc-stacked.png"
PNG_DELTA="loc-delta.png"
PNG_RATIO="loc-ratio.png"

EXCLUDES=(.git target node_modules venv dist build out .idea .gradle .mvn .vscode)

############################################
# PRUNE
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

############################################
# LOC
############################################
count_lines_nonempty() {
  awk 'NF{n++} END{print n+0}' "$@"
}

count_loc() {
  mapfile -t JAVA < <(find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f -name '*.java' -print)
  mapfile -t DOCS < <(find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f \( -name '*.md' -o -name '*.adoc' -o -name '*.rst' -o -name '*.txt' \) -print)
  mapfile -t CSS  < <(find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f -name '*.css' -print)

  j=$(count_lines_nonempty "${JAVA[@]}")
  d=$(count_lines_nonempty "${DOCS[@]}")
  c=$(count_lines_nonempty "${CSS[@]}")

  echo "$j,$d,$c,$((j+d+c))"
}

############################################
# COMMITS (last 3 months)
############################################
mapfile -t COMMITS < <(
  git rev-list --since="$DAYS_BACK days ago" --reverse HEAD
)

TOTAL_COMMITS=${#COMMITS[@]}
STEP=$(( TOTAL_COMMITS / MAX_POINTS ))
(( STEP < 1 )) && STEP=1

############################################
# PREPARE
############################################
echo "date,commit,java_loc,docs_loc,css_loc,total_loc" > "$CSV"

printf "\n📈 LOC METRICS – LAST 3 MONTHS\n"
printf "%s\n" "===================================================================="
printf "%-12s %-8s %10s %10s %8s %10s\n" "DATE" "COMMIT" "JAVA" "DOCS" "CSS" "TOTAL"
printf "%s\n" "--------------------------------------------------------------------"

rm -rf "$WORKTREE"

############################################
# SNAPSHOT LOOP
############################################
for ((i=0;i<TOTAL_COMMITS;i+=STEP)); do
  COMMIT="${COMMITS[$i]}"
  git worktree add -q "$WORKTREE" "$COMMIT"

  pushd "$WORKTREE" >/dev/null
  DATE=$(git show -s --format=%cs)
  LOC=$(count_loc)
  popd >/dev/null

  git worktree remove -f "$WORKTREE"

  IFS=',' read -r JAVA DOCS CSS TOTAL <<< "$LOC"

  printf "%-12s %-8s %10d %10d %8d %10d\n" \
    "$DATE" "${COMMIT:0:7}" "$JAVA" "$DOCS" "$CSS" "$TOTAL"

  echo "$DATE,${COMMIT:0:7},$JAVA,$DOCS,$CSS,$TOTAL" >> "$CSV"
done

printf "%s\n" "===================================================================="
printf "CSV written: %s\n" "$CSV"

############################################
# GNUPLOT GRAPHS
############################################
if command -v gnuplot >/dev/null 2>&1; then

gnuplot <<EOF
set datafile separator ","
set terminal pngcairo size 1400,800
set grid
set key left top
set xdata time
set timefmt "%Y-%m-%d"
set format x "%b %d"

# 1️⃣ TOTAL LOC
set output "${PNG_TOTAL}"
set title "Total LOC Growth (Last 3 Months)"
set xlabel "Date"
set ylabel "Lines of Code"
plot "${CSV}" using 1:6 with linespoints lw 3 title "Total LOC"

# 2️⃣ STACKED AREA
set output "${PNG_STACK}"
set title "LOC Composition (Java / Docs / CSS)"
set style fill solid 0.7
set ylabel "Lines of Code"
plot \
  "${CSV}" using 1:3 with filledcurves x1 title "Java", \
  "${CSV}" using 1:(\$3+\$4) with filledcurves x1 title "Docs", \
  "${CSV}" using 1:(\$3+\$4+\$5) with filledcurves x1 title "CSS"

# 3️⃣ DAILY DELTA
set output "${PNG_DELTA}"
set title "LOC Velocity (Change Between Snapshots)"
set ylabel "LOC Change"
plot "${CSV}" using 1:(column(6)-prev=prev, prev=column(6)) with impulses lw 2 title "Δ LOC"

# 4️⃣ JAVA / DOCS RATIO
set output "${PNG_RATIO}"
set title "Java vs Docs Ratio"
set ylabel "Java / Docs"
plot "${CSV}" using 1:(\$3/\$4) with linespoints lw 2 title "Ratio"

EOF

echo "PNG generated:"
echo " - $PNG_TOTAL"
echo " - $PNG_STACK"
echo " - $PNG_DELTA"
echo " - $PNG_RATIO"

else
  echo "WARNING: gnuplot not installed (PNG skipped)"
fi

echo
