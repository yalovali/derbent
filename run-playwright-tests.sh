#!/bin/bash

# Playwright UI Test Automation Runner for Derbent Application
# Fast menu navigation test with configurable browser visibility and console output

set -e

# Setup Java 21 environment
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/setup-java-env.sh"

echo "🚀 Derbent Playwright Menu Navigation Test"
echo "=========================================="

# Default settings
HEADLESS_MODE="${PLAYWRIGHT_HEADLESS:-false}"
SHOW_CONSOLE="${PLAYWRIGHT_SHOW_CONSOLE:-true}"

# Function to install Playwright browsers
install_playwright_browsers() {
    echo "🔄 Installing Playwright browsers..."
    mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install" > /dev/null 2>&1 || true
    echo "⚠️ Browser installation completed"
}

# Function to run the menu navigation test
run_menu_test() {
    echo "🧪 Running Menu Navigation Test..."
    echo "=================================="
    echo "This test will:"
    echo "  1. Login to the application"
    echo "  2. Browse all menu items"
    echo "  3. Capture screenshots for each menu item"
    echo "  4. Complete in under 1 minute"
    echo ""
    
    # Create screenshots directory
    mkdir -p target/screenshots
    
    # Install Playwright browsers if needed
    install_playwright_browsers
    
    # Set Playwright environment variables to use cached browser
    export PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright"
    export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true
    
    # Run the test with Playwright-specific profile
    echo "🎭 Browser mode: $([ "$HEADLESS_MODE" = "true" ] && echo "HEADLESS" || echo "VISIBLE")"
    echo "📋 Console output: $([ "$SHOW_CONSOLE" = "true" ] && echo "ENABLED" || echo "SUPPRESSED")"
    echo ""
    
    local test_result=0
    if [ "$SHOW_CONSOLE" = "true" ]; then
        mvn test -Dtest="automated_tests.tech.derbent.ui.automation.CMenuNavigationTest" \
            -Dspring.profiles.active=test \
            -Dplaywright.headless=$HEADLESS_MODE || test_result=$?
    else
        mvn test -Dtest="automated_tests.tech.derbent.ui.automation.CMenuNavigationTest" \
            -Dspring.profiles.active=test \
            -Dplaywright.headless=$HEADLESS_MODE > /dev/null 2>&1 || test_result=$?
    fi
    
    if [ $test_result -eq 0 ]; then
        echo "✅ Menu navigation test completed successfully!"
        
        # Show screenshot count
        screenshot_count=$(find target/screenshots -name "*.png" 2>/dev/null | wc -l)
        if [[ $screenshot_count -gt 0 ]]; then
            echo "📸 Generated $screenshot_count screenshots in target/screenshots/"
            echo ""
            echo "Screenshots:"
            find target/screenshots -name "*.png" -type f -printf "  - %f\n" | sort
        fi
        return 0
    else
        echo "❌ Menu navigation test failed!"
        
        # Show any screenshots that were taken
        screenshot_count=$(find target/screenshots -name "*.png" 2>/dev/null | wc -l)
        if [[ $screenshot_count -gt 0 ]]; then
            echo "📸 Debug screenshots available in target/screenshots/ ($screenshot_count files)"
        fi
        return 1
    fi
}

# Show usage information
show_usage() {
    cat << EOF

Usage: ./run-playwright-tests.sh [OPTION]

Run Playwright menu navigation test for the Derbent application.

OPTIONS:
    (no args)       Run the menu navigation test (default)
    menu            Run the menu navigation test
    clean           Clean test artifacts (screenshots, reports)
    install         Install Playwright browsers
    help            Show this help message

ENVIRONMENT VARIABLES:
    PLAYWRIGHT_HEADLESS      Set to 'true' for headless mode, 'false' for visible browser (default: false)
    PLAYWRIGHT_SHOW_CONSOLE  Set to 'true' to show console output, 'false' to suppress (default: true)

EXAMPLES:
    # Run with visible browser and console output (default)
    ./run-playwright-tests.sh
    
    # Run with visible browser without console output
    PLAYWRIGHT_SHOW_CONSOLE=false ./run-playwright-tests.sh
    
    # Run in headless mode with console output
    PLAYWRIGHT_HEADLESS=true ./run-playwright-tests.sh
    
    # Run in headless mode without console output (quiet)
    PLAYWRIGHT_HEADLESS=true PLAYWRIGHT_SHOW_CONSOLE=false ./run-playwright-tests.sh

DESCRIPTION:
    Fast menu navigation test that:
    - Logs into the application
    - Browses all dynamically generated menu items
    - Captures screenshots for each menu item
    - Completes in under 1 minute
    
    Screenshots are saved to: target/screenshots/
    
    For complete testing guidelines and patterns, see:
    - docs/development/copilot-guidelines.md
    - .github/copilot-instructions.md

EOF
}

# Main script logic
case "${1:-menu}" in
    menu|"")
        run_menu_test
        ;;
    clean)
        echo "🧹 Cleaning test artifacts..."
        rm -rf target/screenshots/*.png
        rm -rf target/test-results/
        echo "✅ Test artifacts cleaned"
        ;;
    install)
        install_playwright_browsers
        ;;
    help|--help|-h)
        show_usage
        ;;
    *)
        echo "❌ Unknown option: $1"
        echo ""
        show_usage
        exit 1
        ;;
esac
