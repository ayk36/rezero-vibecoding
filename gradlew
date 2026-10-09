#!/bin/sh
# Minimal Gradle wrapper bootstrap script.

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Missing $WRAPPER_JAR. Download the Gradle wrapper JAR before running this script." >&2
  exit 1
fi

exec java ${JAVA_OPTS:-} -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
