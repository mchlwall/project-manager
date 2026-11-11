#!/usr/bin/env bash
# Run a test class or method several times in a row before merging it, to catch flakiness.
# Usage:   ./scripts/run-stability-check.sh <surefire -Dtest filter> [runs, default 3]
# Example: ./scripts/run-stability-check.sh 'HealthTests#healthReturnsOk' 10
set -euo pipefail

TEST_FILTER="${1:?Usage: $0 <test-filter> [runs]}"
RUNS="${2:-3}"
[[ "$RUNS" =~ ^[1-9][0-9]*$ ]] || { echo "runs must be a positive integer" >&2; exit 1; }

API_TESTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MVNW=("$API_TESTS_DIR/../mvnw" -B -q -f "$API_TESTS_DIR/pom.xml")

"${MVNW[@]}" test-compile

for i in $(seq 1 "$RUNS"); do
  echo "=== Run $i of $RUNS ==="
  if OUTPUT=$("${MVNW[@]}" surefire:test -Dtest="$TEST_FILTER" 2>&1); then
    echo "Run $i: PASSED"
  else
    echo "$OUTPUT" | tail -40
    echo "Stability check FAILED on run $i/$RUNS" >&2
    exit 1
  fi
done
echo "All $RUNS run(s) passed."
