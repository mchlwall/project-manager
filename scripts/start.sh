#!/usr/bin/env bash
# Usage:   ./scripts/start.sh [docker|java]
# Starts the app, waits until it is healthy, then opens it in the browser.
#   docker (default): runs in the background; stop with `docker compose down`.
#   java:             runs in this terminal; stop with Ctrl+C.
set -euo pipefail

MODE="${1:-docker}"
URL="http://localhost:8080"
TIMEOUT_SECONDS=120

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

wait_for_health() {
  echo "Waiting for $URL/health ..."
  for ((i = 0; i < TIMEOUT_SECONDS; i++)); do
    if curl -fs "$URL/health" >/dev/null; then
      return 0
    fi
    sleep 1
  done
  echo "App did not become healthy within ${TIMEOUT_SECONDS}s." >&2
  return 1
}

open_browser() {
  if command -v open >/dev/null; then
    open "$URL"
  elif command -v xdg-open >/dev/null; then
    xdg-open "$URL" >/dev/null 2>&1
  else
    echo "App is running at $URL"
  fi
}

case "$MODE" in
  docker)
    docker compose up --build -d
    wait_for_health
    open_browser
    echo "Running at $URL. Stop with: docker compose down"
    ;;
  java)
    ./mvnw spring-boot:run &
    APP_PID=$!
    trap 'kill "$APP_PID" 2>/dev/null || true' INT TERM EXIT
    wait_for_health
    open_browser
    wait "$APP_PID"
    ;;
  *)
    echo "Unknown mode '$MODE'. Use: docker, java" >&2
    exit 1
    ;;
esac
