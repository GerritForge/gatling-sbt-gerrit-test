#!/bin/bash

if [[ "$1" == "" ]] || [[ "$2" == "" ]] || [[ "$3" == "" ]]; then
  echo "Compare p50 and p95 latency stats from two Gatling result directories"
  echo "Use: $0 <path1> <path2> <output.csv>"
  echo "  e.g. $0 /results/3.8 /results/4.0 comparison.csv"
  echo "  Each path must contain target/gatling/<sim-dir>/simulation.log"
  exit 1
fi

DIR1="$1"
DIR2="$2"
OUTPUT="$3"

LABEL1=$(basename "$DIR1")
LABEL2=$(basename "$DIR2")

TMP1=$(mktemp)
TMP2=$(mktemp)

extract_stats() {
  local dir="$1"
  local out="$2"
  local logs=("$dir"/target/gatling/*/simulation.log)

  if [[ ${#logs[@]} -eq 0 ]] || [[ ! -f "${logs[0]}" ]]; then
    echo "ERROR: no simulation.log found under $dir/target/gatling/" >&2
    exit 1
  fi

  cat "${logs[@]}" | grep REQUEST | grep OK | awk -F'\t' '{print $3","$5-$4}' | \
    datamash -R2 -t ',' --sort --group 1 perc:50 2 perc:95 2 > "$out"
}

extract_stats "$DIR1" "$TMP1"
extract_stats "$DIR2" "$TMP2"

# Warn about requests present in only one directory
join -t ',' -v 1 "$TMP1" "$TMP2" | cut -d',' -f1 | while read -r req; do
  echo "WARNING: request '$req' found only in $DIR1 -- excluded from output" >&2
done
join -t ',' -v 2 "$TMP1" "$TMP2" | cut -d',' -f1 | while read -r req; do
  echo "WARNING: request '$req' found only in $DIR2 -- excluded from output" >&2
done

{
  echo "request,p50_${LABEL1},p50_${LABEL2},p95_${LABEL1},p95_${LABEL2}"
  # joined columns: request,p50_1,p95_1,p50_2,p95_2 -> rearrange to request,p50_1,p50_2,p95_1,p95_2
  join -t ',' "$TMP1" "$TMP2" | awk -F',' '{print $1","$2","$4","$3","$5}'
} > "$OUTPUT"

rm -f "$TMP1" "$TMP2"

echo "Written to $OUTPUT"
