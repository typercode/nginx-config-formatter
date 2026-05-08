#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
IDEA_HOME="${IDEA_HOME:-/Applications/IntelliJ IDEA.app/Contents}"
JBR_HOME="${JBR_HOME:-$IDEA_HOME/jbr/Contents/Home}"
JAVAC="$JBR_HOME/bin/javac"
BUILD_DIR="$ROOT_DIR/build/offline-plugin"
CLASSES_DIR="$BUILD_DIR/classes"
PLUGIN_DIR="$BUILD_DIR/NginxConfigFormatter"
DIST_DIR="$ROOT_DIR/dist"
VERSION="$(grep '^pluginVersion=' "$ROOT_DIR/gradle.properties" | cut -d= -f2)"
PLATFORM_TYPE="$(grep '^platformType=' "$ROOT_DIR/gradle.properties" | cut -d= -f2)"
PLATFORM_VERSION="$(grep '^platformVersion=' "$ROOT_DIR/gradle.properties" | cut -d= -f2)"
ZIP_PATH="$DIST_DIR/nginx-config-formatter-$VERSION-$PLATFORM_TYPE-$PLATFORM_VERSION.zip"

if [[ ! -x "$JAVAC" ]]; then
  echo "Missing javac at $JAVAC. Set IDEA_HOME or JBR_HOME to a Java 21+ runtime." >&2
  exit 1
fi

rm -rf "$BUILD_DIR"
mkdir -p "$CLASSES_DIR" "$PLUGIN_DIR/lib" "$PLUGIN_DIR/META-INF" "$DIST_DIR"

SOURCE_LIST="$BUILD_DIR/sources.txt"
(
  cd "$ROOT_DIR"
  find src/main/java -name '*.java' | sort > "$SOURCE_LIST"
)

CLASSPATH="$(find "$IDEA_HOME/lib" -maxdepth 1 -name '*.jar' | sort | paste -sd ':' -)"

(
  cd "$ROOT_DIR"
  "$JAVAC" \
    --release 21 \
    -encoding UTF-8 \
    -cp "$CLASSPATH" \
    -d "$CLASSES_DIR" \
    @"$SOURCE_LIST"
)

cp "$ROOT_DIR/src/main/resources/META-INF/plugin.xml" "$PLUGIN_DIR/META-INF/plugin.xml"
mkdir -p "$CLASSES_DIR/META-INF"
cp "$ROOT_DIR/src/main/resources/META-INF/plugin.xml" "$CLASSES_DIR/META-INF/plugin.xml"
(
  cd "$CLASSES_DIR"
  /usr/bin/zip -qr "$PLUGIN_DIR/lib/nginx-config-formatter.jar" .
)

rm -f "$ZIP_PATH"
(
  cd "$BUILD_DIR"
  /usr/bin/zip -qr "$ZIP_PATH" "NginxConfigFormatter"
)

echo "$ZIP_PATH"
