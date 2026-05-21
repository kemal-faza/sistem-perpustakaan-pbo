#!/bin/bash
# ============================================
# Test Script for Unix (Linux/macOS)
# Sistem Manajemen Perpustakaan - PBO
# ============================================

echo "Compiling tests..."
javac -encoding UTF-8 -cp "lib/*:out" -d out -sourcepath src:test test/unit/service/*.java test/unit/collection/*.java test/unit/model/*.java

if [ $? -ne 0 ]; then
    echo "Test compilation failed!"
    exit 1
fi

echo "Running tests..."
java -cp "lib/*:out" org.junit.platform.console.ConsoleLauncher --scan-class-path
