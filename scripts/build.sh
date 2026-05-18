#!/bin/bash
# ============================================
# Build Script for Unix (Linux/macOS)
# Sistem Manajemen Perpustakaan - PBO
# ============================================

echo "Compiling main source..."
javac -encoding UTF-8 -cp "lib/*" -d out -sourcepath src src/Main.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo "Compilation successful."
echo ""
echo "To run: java -cp \"out:lib/*\" Main"
