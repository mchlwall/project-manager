#!/usr/bin/env bash
# Usage:   ./scripts/run-suite.sh <suite> [extra maven args]
# Example: BASE_URL=http://localhost:8080 ./scripts/run-suite.sh smoke
# Suites are JUnit @Tag names; "all" runs every test. Add new suites to the list below.
set -euo pipefail

SUITE="${1:?Usage: $0 <smoke|unit|all> [extra maven args]}"
shift

case "$SUITE" in
  all) TAG_ARGS=() ;;
  smoke | unit) TAG_ARGS=("-Dgroups=$SUITE") ;;
  *)
    echo "Unknown suite '$SUITE'. Known suites: smoke, unit, all" >&2
    exit 1
    ;;
esac

API_TESTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

exec "$API_TESTS_DIR/../mvnw" -B -f "$API_TESTS_DIR/pom.xml" clean test ${TAG_ARGS[@]+"${TAG_ARGS[@]}"} "$@"
