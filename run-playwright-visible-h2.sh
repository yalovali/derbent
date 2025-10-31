#!/bin/bash

# Playwright UI Test Runner with visible browser against in-memory H2 profile.
# Keeps the PostgreSQL-based runner untouched while offering a headful option
# that works inside the sandboxed CLI environment.

set -e

echo "🚀 Playwright Test Runner (H2 + Visible Browser)"
echo "================================================"

# Ensure screenshots directory is present for test artifacts
mkdir -p target/screenshots

# Install Playwright browsers if they are missing. Runs silently to avoid noise.
echo "🔄 Checking Playwright browsers..."
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install" >/dev/null 2>&1 || true

# Reuse cached browsers when possible
export PLAYWRIGHT_BROWSERS_PATH="${HOME}/.cache/ms-playwright"
export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true

echo "🧪 Running test with visible browser using H2 profile..."
echo ""

mvn test \
    -Dtest=automated_tests.tech.derbent.ui.automation.CSampleDataMenuNavigationTest \
    -Dspring.profiles.active=test \
    -Dplaywright.headless=false

echo ""
echo "✅ Test completed successfully!"

# Report screenshots created during the run
SCREENSHOT_COUNT=$(find target/screenshots -name "*.png" 2>/dev/null | wc -l)
if [ "${SCREENSHOT_COUNT}" -gt 0 ]; then
    echo "📸 Generated ${SCREENSHOT_COUNT} screenshots in target/screenshots/"
fi
