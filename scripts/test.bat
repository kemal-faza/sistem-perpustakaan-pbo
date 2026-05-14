@echo off
REM ============================================
REM Test Script for Windows (CMD)
REM Sistem Manajemen Perpustakaan - PBO
REM ============================================

echo Compiling tests...
dir /s /B test\*.java > test_sources.txt
javac -cp "lib/*;out" -d out -sourcepath "src;test" @test_sources.txt
del test_sources.txt

IF %ERRORLEVEL% NEQ 0 (
    echo Test compilation failed!
    exit /b 1
)

echo Running tests...
java -jar lib\junit-platform-console-standalone-1.10.1.jar --cp "out;lib/*" --scan-class-path
