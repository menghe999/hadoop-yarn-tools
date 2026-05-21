#!/usr/bin/env bash
set -euo pipefail

APP_HOME="$(cd "$(dirname "$0")/.." && pwd)"
APP_NAME="${APP_NAME:-hadoop-yarn-tools}"
PID_DIR="${PID_DIR:-${APP_HOME}/run}"
PID_FILE="${PID_FILE:-${PID_DIR}/${APP_NAME}.pid}"
STOP_TIMEOUT="${STOP_TIMEOUT:-30}"

if [ ! -f "${PID_FILE}" ]; then
  echo "${APP_NAME} is not running: pid file not found"
  exit 0
fi

PID="$(cat "${PID_FILE}")"
if [ -z "${PID}" ] || ! kill -0 "${PID}" >/dev/null 2>&1; then
  rm -f "${PID_FILE}"
  echo "${APP_NAME} is not running"
  exit 0
fi

kill "${PID}"

COUNT=0
while kill -0 "${PID}" >/dev/null 2>&1; do
  if [ "${COUNT}" -ge "${STOP_TIMEOUT}" ]; then
    kill -9 "${PID}" >/dev/null 2>&1 || true
    break
  fi
  COUNT=$((COUNT + 1))
  sleep 1
done

rm -f "${PID_FILE}"
echo "${APP_NAME} stopped"
