#!/bin/sh

APP_HOME=$( cd "$( dirname "$0" )" && pwd )
APP_NAME=$( basename "$0" )
APP_BASE_NAME=$( basename "$0" .sh )

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

warn () { echo "$*"; }
die () { echo; echo "$*"; echo; exit 1; }

cygwin=false; msys=false; darwin=false; nonstop=false
case "`uname`" in
  CYGWIN* ) cygwin=true ;;
  Darwin* ) darwin=true ;;
  MINGW* ) msys=true ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ] ; then
  if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
    JAVACMD="$JAVA_HOME/jre/sh/java"
  else
    JAVACMD="$JAVA_HOME/bin/java"
  fi
  [ ! -x "$JAVACMD" ] && die "ERROR: JAVA_HOME invalid: $JAVA_HOME"
else
  JAVACMD="java"
  which java >/dev/null 2>&1 || die "ERROR: no java found"
fi

if ! command -v xargs >/dev/null 2>&1; then
  die "xargs is not available"
fi

if "$cygwin"; then
  APP_HOME=$( cygpath --path --mixed "$APP_HOME" )
  CLASSPATH=$( cygpath --path --mixed "$CLASSPATH" )
  JAVACMD=$( cygpath --unix "$JAVACMD" )
elif "$msys"; then
  APP_HOME=$( cd "$APP_HOME"; pwd -W 2>/dev/null || echo "$APP_HOME" )
fi

exec "$JAVACMD" "$DEFAULT_JVM_OPTS" "$JAVA_OPTS" "$GRADLE_OPTS" \
  "-Dorg.gradle.appname=$APP_BASE_NAME" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain "$@"
