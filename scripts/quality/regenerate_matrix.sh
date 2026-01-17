#!/bin/bash
# Regenerate Code Quality Matrix
# This script regenerates the code quality matrix from current codebase

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/../.." && pwd )"
TEMP_DIR="/tmp/quality_matrix"

echo "=================================="
echo "Code Quality Matrix Regeneration"
echo "=================================="
echo ""

# Create temp directory
mkdir -p "$TEMP_DIR"

# Step 1: Generate class list
echo "Step 1: Generating class list..."
cd "$PROJECT_ROOT"
find src/main/java/tech/derbent -name "C*.java" -type f | \
  sed 's|src/main/java/||' | sed 's|/|.|g' | sed 's|.java||' | \
  sort > "$TEMP_DIR/all_classes.txt"

CLASS_COUNT=$(wc -l < "$TEMP_DIR/all_classes.txt")
echo "Found $CLASS_COUNT classes"
echo ""

# Step 2: Check Python dependencies
echo "Step 2: Checking Python dependencies..."
if ! python3 -c "import openpyxl" 2>/dev/null; then
    echo "Installing openpyxl..."
    pip3 install --user openpyxl
fi
echo "Dependencies OK"
echo ""

# Step 3: Run generator
echo "Step 3: Running quality matrix generator..."
python3 "$SCRIPT_DIR/generate_quality_matrix.py"
echo ""

# Step 4: Verify output
if [ -f "$PROJECT_ROOT/docs/CODE_QUALITY_MATRIX.xlsx" ]; then
    SIZE=$(du -h "$PROJECT_ROOT/docs/CODE_QUALITY_MATRIX.xlsx" | cut -f1)
    echo "=================================="
    echo "✓ Success!"
    echo "=================================="
    echo "Matrix saved to: docs/CODE_QUALITY_MATRIX.xlsx"
    echo "File size: $SIZE"
    echo "Classes analyzed: $CLASS_COUNT"
    echo "Quality dimensions: 51"
    echo ""
    echo "Open with: libreoffice docs/CODE_QUALITY_MATRIX.xlsx"
    echo "Or: open docs/CODE_QUALITY_MATRIX.xlsx"
else
    echo "=================================="
    echo "✗ Error!"
    echo "=================================="
    echo "Matrix file was not created. Check errors above."
    exit 1
fi
