#!/bin/bash
# ============================================
# Test Script for Unix (Linux/macOS)
# Sistem Manajemen Perpustakaan - PBO
# ============================================

echo "Compiling tests..."
javac -cp "lib/*:out" -d out -sourcepath src:test test/unit/service/*.java test/unit/collection/*.java test/unit/model/*.java

if [ $? -ne 0 ]; then
    echo "Test compilation failed!"
    exit 1
fi

echo "Running tests..."
java -jar lib/junit-platform-console-standalone-1.10.1.jar --cp "out:lib/*" --scan-class-path
