#!/bin/sh

#
# Gradle wrapper startup script
#

# Resolve APP_HOME
app_path=$0
while [ -h "$app_path" ]; do
    ls=`ls -ld "$app_path"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        app_path="$link"
    else
        app_path=`dirname "$app_path"`"/$link"
    fi
done

APP_HOME=`dirname "$app_path"`
APP_HOME=`cd "$APP_HOME" && pwd`
APP_BASE_NAME=`basename "$0"`

# Check if wrapper jar exists
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$CLASSPATH" ]; then
    # Fallback to system gradle if wrapper jar is not present
    if command -v gradle >/dev/null 2>&1; then
        exec gradle "$@"
    else
        echo "ERROR: Gradle wrapper JAR not found at $CLASSPATH and 'gradle' command not found in PATH." >&2
        exit 1
    fi
fi

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
else
    JAVACMD="java"
fi

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

eval set -- "$DEFAULT_JVM_OPTS" "$JAVA_OPTS" "$GRADLE_OPTS" "\"-Dorg.gradle.appname=$APP_BASE_NAME\"" -classpath "\"$CLASSPATH\"" org.gradle.wrapper.GradleWrapperMain "$@"

exec "$JAVACMD" "$@"
