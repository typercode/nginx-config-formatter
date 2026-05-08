#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 /path/to/gradle-8.14.5-bin.zip [gradle tasks...]" >&2
  echo "Example: $0 /Volumes/typercode/tools/gradle-8.14.5-bin.zip buildPlugin" >&2
  exit 1
fi

GRADLE_ZIP="$1"
shift

if [[ ! -f "$GRADLE_ZIP" ]]; then
  echo "Gradle distribution zip not found: $GRADLE_ZIP" >&2
  exit 1
fi

GRADLE_VERSION="$(basename "$GRADLE_ZIP" | sed -E 's/^gradle-([0-9.]+)-bin\.zip$/\1/')"
if [[ "$GRADLE_VERSION" == "$(basename "$GRADLE_ZIP")" ]]; then
  echo "Expected a Gradle distribution named like gradle-8.14.5-bin.zip" >&2
  exit 1
fi

GRADLE_DIST_DIR="$ROOT_DIR/build/gradle-dist"
GRADLE_HOME="$GRADLE_DIST_DIR/gradle-$GRADLE_VERSION"
GRADLE_BIN="$GRADLE_HOME/bin/gradle"

mkdir -p "$GRADLE_DIST_DIR"
if [[ ! -x "$GRADLE_BIN" ]]; then
  unzip -q "$GRADLE_ZIP" -d "$GRADLE_DIST_DIR"
fi

if [[ $# -eq 0 ]]; then
  set -- buildPlugin
fi

USE_IDEA_JBR=false
if [[ -z "${JAVA_HOME:-}" ]]; then
  USE_IDEA_JBR=true
elif [[ -x "$JAVA_HOME/bin/java" ]]; then
  JAVA_MAJOR="$("$JAVA_HOME/bin/java" -version 2>&1 | awk -F'[\".]' '/version/ { if ($2 == "1") print $3; else print $2; exit }')"
  if [[ "$JAVA_MAJOR" =~ ^[0-9]+$ && "$JAVA_MAJOR" -lt 17 ]]; then
    USE_IDEA_JBR=true
  fi
fi

if [[ "$USE_IDEA_JBR" == true && -x "/Applications/IntelliJ IDEA.app/Contents/jbr/Contents/Home/bin/java" ]]; then
  export JAVA_HOME="/Applications/IntelliJ IDEA.app/Contents/jbr/Contents/Home"
fi

export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$ROOT_DIR/build/gradle-user-home}"
exec "$GRADLE_BIN" "$@"
