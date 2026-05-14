@echo off
REM ============================================
REM Download JUnit for Windows
REM Sistem Manajemen Perpustakaan - PBO
REM ============================================

echo Downloading JUnit Platform Console Standalone...
curl -sLO "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.1/junit-platform-console-standalone-1.10.1.jar" -o lib\junit-platform-console-standalone-1.10.1.jar

echo Test dependencies downloaded.
echo.
echo Run tests with:
echo   scripts\test.bat
