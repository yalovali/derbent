#!/bin/bash

# Playwright UI Test Automation Runner for Derbent Application
# Provides smoke and journey flows that can run in headless environments without an X server.

set -e

PLAYWRIGHT_HEADLESS="${PLAYWRIGHT_HEADLESS:-true}"
DEFAULT_TEST="login"

echo "🚀 Derbent Playwright UI Test Automation Runner"
echo "==============================================="
echo "🎯 Default test: ${DEFAULT_TEST}"
echo "🎭 Headless mode: ${PLAYWRIGHT_HEADLESS}"

install_playwright_browsers() {
    echo "🔄 Ensuring Playwright browsers are installed..."
    mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install" >/dev/null 2>&1 || true
    echo "✅ Browser installation step completed"
}

clean_artifacts() {
    echo "🧹 Cleaning Playwright artifacts..."
    rm -rf target/screenshots test-results/playwright >/dev/null 2>&1 || true
    echo "✅ Cleanup complete"
}

summarize_screenshots() {
    local count
    count=$(find target/screenshots -name "*.png" 2>/dev/null | wc -l | tr -d '[:space:]')
    if [[ -n "${count}" && "${count}" != "0" ]]; then
        echo "📸 Generated ${count} screenshots in target/screenshots/"
        find target/screenshots -name "*.png" -type f -printf "  - %f\n" | sort
    else
        echo "ℹ️ No screenshots were generated for this run"
    fi
}

prepare_run() {
    mkdir -p target/screenshots
    install_playwright_browsers
}

run_login_smoke_test() {
    echo ""
    echo "🧪 Running Login Smoke Test..."
    echo "------------------------------"
    echo "This test will:"
    echo "  1. Start the application with in-memory H2 database"
    echo "  2. Load the login page"
    echo "  3. Authenticate with default credentials"
    echo "  4. Verify the main application shell appears"
    echo ""

    prepare_run

    if mvn test -Dtest="automated_tests.tech.derbent.ui.automation.CSimpleLoginTest" \
        -Dspring.profiles.active=test \
        -Dplaywright.headless="${PLAYWRIGHT_HEADLESS}"; then
        echo "✅ Login smoke test completed successfully!"
        summarize_screenshots
    else
        echo "❌ Login smoke test failed"
        summarize_screenshots
        return 1
    fi
}

run_menu_navigation_test() {
    echo ""
    echo "🧪 Running Sample Data Menu Navigation Test..."
    echo "---------------------------------------------"
    echo "This test will:"
    echo "  1. Load sample data"
    echo "  2. Authenticate with default credentials"
    echo "  3. Navigate across all generated menu items"
    echo "  4. Capture screenshots for each entity view"
    echo ""

    prepare_run

    if mvn test -Dtest="automated_tests.tech.derbent.ui.automation.CSampleDataMenuNavigationTest" \
        -Dspring.profiles.active=test \
        -Dplaywright.headless="${PLAYWRIGHT_HEADLESS}"; then
        echo "✅ Menu navigation test completed successfully!"
        summarize_screenshots
    else
        echo "❌ Menu navigation test failed"
        summarize_screenshots
        return 1
    fi
}

show_usage() {
    cat <<EOF

Usage: ./run-playwright-tests.sh [COMMAND]

Run Playwright UI tests for the Derbent application. The default command is 'login'.

COMMANDS:
    login           Run the headless login smoke test (default)
    menu            Run the sample data menu navigation journey
    clean           Remove Playwright screenshots and cached reports
    install         Install or update Playwright browsers
    help            Show this help message

ENVIRONMENT VARIABLES:
    PLAYWRIGHT_HEADLESS    Set to "false" to attempt a visible browser (requires X server)

EXAMPLES:
    ./run-playwright-tests.sh
    ./run-playwright-tests.sh menu
    PLAYWRIGHT_HEADLESS=false ./run-playwright-tests.sh login

EOF
}

COMMAND="${1:-${DEFAULT_TEST}}"
case "${COMMAND}" in
    ""|login)
        run_login_smoke_test
        ;;
    menu)
        run_menu_navigation_test
        ;;
    clean)
        clean_artifacts
        ;;
    install)
        install_playwright_browsers
        ;;
    help|--help|-h)
        show_usage
        ;;
    *)
        echo "❌ Unknown command: ${COMMAND}"
        show_usage
        exit 1
        ;;
esac
