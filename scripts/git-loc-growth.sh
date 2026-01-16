#!/usr/bin/env bash
set -euo pipefail

############################################
# CONFIG
############################################
DAYS_BACK=90
MAX_POINTS=30
WORKTREE=".loc-worktree"

CSV="loc-3months.csv"
DELTA_CSV="loc-3months-delta.csv"
DELTA_AVG_CSV="loc-3months-delta-avg.csv"

PNG_TOTAL="loc-total-growth.png"
PNG_DELTA="loc-velocity.png"
PNG_TEST="loc-test-growth.png"

export LC_NUMERIC=en_US.UTF-8

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
# LOC COUNT
############################################
count_lines_nonempty() {
  awk 'NF{n++} END{print n+0}' "$@"
}

count_loc() {
  # PROD
  mapfile -t PROD < <(
    find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f \
      \( -name '*.java' -o -name '*.kt' \) \
      ! -path '*test*' -print
  )

  # TEST
  mapfile -t TEST < <(
    find . \( "${PRUNE_EXPR[@]}" \) -prune -o -type f \
      \( -name '*.java' -o -name '*.kt' \) \
      \( -path '*test*' -o -path '*tests*' \) -print
  )

  prod=$(count_lines_nonempty "${PROD[@]}")
  test=$(count_lines_nonempty "${TEST[@]}")

  echo "$prod,$test,$((prod+test))"
}

############################################
# COMMITS (LAST 3 MONTHS)
############################################
mapfile -t COMMITS < <(
  git rev-list --since="$DAYS_BACK days ago" --reverse HEAD
)

TOTAL=${#COMMITS[@]}
STEP=$(( TOTAL / MAX_POINTS ))
(( STEP < 1 )) && STEP=1

############################################
# PREPARE
############################################
echo "date,commit,prod_loc,test_loc,total_loc" > "$CSV"

printf "\n📈 LOC METRICS – LAST 3 MONTHS (FOCUSED)\n"
printf "%s\n" "===================================================================="
printf "%-12s %-8s %'12s %'12s %'12s\n" "DATE" "COMMIT" "PROD_LOC" "TEST_LOC" "TOTAL_LOC"
printf "%s\n" "--------------------------------------------------------------------"

rm -rf "$WORKTREE"

############################################
# SNAPSHOT LOOP
############################################
for ((i=0;i<TOTAL;i+=STEP)); do
  COMMIT="${COMMITS[$i]}"
  git worktree add -q "$WORKTREE" "$COMMIT"

  pushd "$WORKTREE" >/dev/null
  DATE=$(git show -s --format=%cs)
  LOC=$(count_loc)
  popd >/dev/null

  git worktree remove -f "$WORKTREE"

  IFS=',' read -r PROD TEST TOTAL_LOC <<< "$LOC"

  printf "%-12s %-8s %'12d %'12d %'12d\n" \
    "$DATE" "${COMMIT:0:7}" "$PROD" "$TEST" "$TOTAL_LOC"

  echo "$DATE,${COMMIT:0:7},$PROD,$TEST,$TOTAL_LOC" >> "$CSV"
done

printf "%s\n" "===================================================================="
printf "CSV written: %s\n" "$CSV"

############################################
# VELOCITY CSV
############################################
echo "date,delta_loc" > "$DELTA_CSV"
awk -F',' '
NR==2 {prev=$5; next}
NR>2  {print $1 "," ($5-prev); prev=$5}
' "$CSV" >> "$DELTA_CSV"

############################################
# MOVING AVERAGE (WINDOW=5)
############################################
echo "date,avg_delta" > "$DELTA_AVG_CSV"
awk -F',' '
NR>1 {
  vals[NR]=$2
  sum+=$2
  if (NR>5) sum-=vals[NR-5]
  if (NR>=6) print $1 "," sum/5
}
' "$DELTA_CSV" >> "$DELTA_AVG_CSV"

############################################
# GNUPLOT (SAFE)
############################################
if command -v gnuplot >/dev/null 2>&1; then

gnuplot <<EOF
set datafile separator ","
set terminal pngcairo size 1400,800
set grid
set xdata time
set timefmt "%Y-%m-%d"
set format x "%b %d"
set decimal locale
set format y "%'.0f"

# 1) TOTAL LOC
set output "${PNG_TOTAL}"
set title "Total LOC Growth (Last 3 Months)"
set xlabel "Date"
set ylabel "Lines of Code"
plot "${CSV}" using 1:5 with linespoints lw 3 title "Total LOC"

# 2) LOC VELOCITY
set output "${PNG_DELTA}"
set title "LOC Velocity (Δ LOC Between Snapshots)"
set ylabel "LOC Change"
plot \
  "${DELTA_CSV}" using 1:2 with impulses lw 4 title "Δ LOC", \
  "${DELTA_AVG_CSV}" using 1:2 with lines lw 3 title "Velocity Trend (MA)"

# 3) TEST LOC
set output "${PNG_TEST}"
set title "Test Code Growth (Testing Effort)"
set ylabel "Test LOC"
plot "${CSV}" using 1:4 with linespoints lw 3 title "Test LOC"

EOF

echo "PNG generated:"
echo " - $PNG_TOTAL"
echo " - $PNG_DELTA"
echo " - $PNG_TEST"

else
  echo "WARNING: gnuplot not installed (PNG skipped)"
fi

echo

############################################
# OPEN PNG FILES (DESKTOP)
############################################
if command -v xdg-open >/dev/null 2>&1; then
  echo "Opening generated graphs..."
  xdg-open "$PNG_TOTAL" >/dev/null 2>&1 &
  xdg-open "$PNG_DELTA" >/dev/null 2>&1 &
  xdg-open "$PNG_TEST"  >/dev/null 2>&1 &
else
  echo "NOTE: xdg-open not found, cannot auto-open PNG files."
fi
