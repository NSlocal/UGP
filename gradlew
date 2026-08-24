#!/bin/sh
# Gradle start up script for POSIX
APP_HOME=$( cd "$(dirname "$0")" && pwd )
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
if [ ! -f "$CLASSPATH" ]; then
  echo "ERROR: $CLASSPATH not found!"; exit 1
fi
exec java -Xmx64m -Xms64m -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
