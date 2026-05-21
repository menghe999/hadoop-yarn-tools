#!/usr/bin/env bash
set -euo pipefail

APP_HOME="$(cd "$(dirname "$0")/.." && pwd)"
APP_NAME="${APP_NAME:-hadoop-yarn-tools}"
MAIN_CLASS="${MAIN_CLASS:-com.example.YarnToolsApplication}"
PID_DIR="${PID_DIR:-${APP_HOME}/run}"
LOG_DIR="${LOG_DIR:-${APP_HOME}/logs}"
PID_FILE="${PID_FILE:-${PID_DIR}/${APP_NAME}.pid}"
LOG_FILE="${LOG_FILE:-${LOG_DIR}/${APP_NAME}.log}"

if [ -z "${APP_JAR:-}" ]; then
  APP_JAR="$(find "${APP_HOME}" -maxdepth 1 -name 'hadoop-yarn-tools-*.jar' | sort | tail -n 1)"
else
  APP_JAR="${APP_HOME}/${APP_JAR}"
fi

if [ ! -f "${APP_JAR}" ]; then
  echo "Application jar not found: ${APP_JAR}" >&2
  exit 1
fi

mkdir -p "${PID_DIR}" "${LOG_DIR}"

if [ -f "${PID_FILE}" ]; then
  OLD_PID="$(cat "${PID_FILE}")"
  if [ -n "${OLD_PID}" ] && kill -0 "${OLD_PID}" >/dev/null 2>&1; then
    echo "${APP_NAME} is already running, pid=${OLD_PID}"
    exit 0
  fi
fi

CLASSPATH="${APP_JAR}:${APP_HOME}/libs/*"
cd "${APP_HOME}"
nohup java ${JAVA_OPTS:-} -cp "${CLASSPATH}" "${MAIN_CLASS}" "$@" > "${LOG_FILE}" 2>&1 &
echo $! > "${PID_FILE}"
echo "${APP_NAME} started, pid=$(cat "${PID_FILE}"), log=${LOG_FILE}"
