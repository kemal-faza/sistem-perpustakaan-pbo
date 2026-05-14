#!/bin/bash
# Download JUnit 5 and Mockito for running tests
cd "$(dirname "$0")/../lib"
curl -sLO "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.1/junit-platform-console-standalone-1.10.1.jar"
echo "Test dependencies downloaded. Run tests with:"
echo "  javac -cp \"lib/*:out\" -d out -sourcepath src:test test/unit/**/*.java"
echo "  java -jar lib/junit-platform-console-standalone-1.10.1.jar -cp \"out:lib/*\" --scan-class-path"
