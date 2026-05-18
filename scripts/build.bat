@echo off
REM ============================================
REM Build Script for Windows (CMD)
REM Sistem Manajemen Perpustakaan - PBO
REM ============================================

echo Compiling main source...
javac -encoding UTF-8 -cp "lib/*" -d out -sourcepath src src\Main.java

IF %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    exit /b 1
)

echo Compilation successful.
echo.
echo To run: java -cp "out;lib/*" Main
