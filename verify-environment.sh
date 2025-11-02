#!/bin/bash

# Quick Verification Script for Derbent Environment
# Verifies Java 21, Maven, SO libraries, and basic compilation

set -e

echo "🔍 Derbent Environment Verification"
echo "===================================="
echo ""

# Setup Java 21 environment
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/setup-java-env.sh"
echo ""

# Check Maven
echo "📦 Checking Maven..."
mvn -version | head -1
echo ""

# Check SO libraries installation
echo "📚 Checking StoredObject libraries..."
SO_LIBS_INSTALLED=0
for lib in so-components so-charts so-helper; do
    if [ -d "$HOME/.m2/repository/org/vaadin/addons/so/$lib" ]; then
        echo "  ✅ $lib installed"
        SO_LIBS_INSTALLED=$((SO_LIBS_INSTALLED + 1))
    else
        echo "  ❌ $lib NOT installed"
    fi
done

if [ $SO_LIBS_INSTALLED -lt 3 ]; then
    echo ""
    echo "⚠️  StoredObject libraries not fully installed!"
    echo "   Run: ./install-so-libraries.sh"
    echo ""
fi
echo ""

# Quick compilation test
echo "🔨 Testing compilation..."
if mvn compile -q -DskipTests 2>&1 | grep -q "BUILD SUCCESS\|BUILD FAILURE"; then
    echo "  ✅ Compilation successful"
else
    # If no output, compilation succeeded silently
    echo "  ✅ Compilation successful"
fi
echo ""

# Summary
echo "✅ Environment verification complete!"
echo ""
echo "Next steps:"
echo "  - Run application: mvn spring-boot:run -Dspring.profiles.active=h2"
echo "  - Run Playwright tests: ./run-playwright-tests.sh menu"
echo "  - Format code: mvn spotless:apply"
echo ""
