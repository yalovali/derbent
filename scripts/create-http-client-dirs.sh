#!/bin/bash
# BAB HTTP Client Implementation Script
# Creates all necessary directories and files for HTTP client system

set -e  # Exit on error

PROJECT_ROOT="/home/yasin/git/derbent"
BAB_HTTP_BASE="$PROJECT_ROOT/src/main/java/tech/derbent/bab/http"

echo "🚀 BAB HTTP Client Implementation Script"
echo "========================================"
echo ""

# Create directory structure
echo "📁 Creating directory structure..."
mkdir -p "$BAB_HTTP_BASE/domain"
mkdir -p "$BAB_HTTP_BASE/service"
mkdir -p "$BAB_HTTP_BASE/clientproject/domain"
mkdir -p "$BAB_HTTP_BASE/clientproject/service"

echo "✅ Directories created:"
echo "   - $BAB_HTTP_BASE/domain"
echo "   - $BAB_HTTP_BASE/service"
echo "   - $BAB_HTTP_BASE/clientproject/domain"
echo "   - $BAB_HTTP_BASE/clientproject/service"
echo ""

# List created structure
echo "📂 Directory structure:"
ls -R "$BAB_HTTP_BASE/"
echo ""

echo "✅ Directory setup complete!"
echo ""
echo "Next steps:"
echo "1. Review HTTP_CLIENT_ARCHITECTURE.md for design details"
echo "2. Run implementation script to create all classes"
echo "3. Build project: mvn clean compile -Pagents -DskipTests"
echo "4. Test connection with Calimero server"
echo ""
echo "Calimero Server: ~/git/calimero"
echo "Calimero Test: ~/git/calimeroTest"
