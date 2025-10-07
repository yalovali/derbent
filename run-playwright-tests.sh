#!/bin/bash

# Playwright UI Test Automation Runner for Derbent Application
# This script runs the sample data menu navigation test: login screen, login, navigate all generated menu items

set -e

echo "🚀 Derbent Playwright UI Test Automation Runner"
echo "==============================================="

# Function to install Playwright browsers
install_playwright_browsers() {
    echo "🔄 Installing Playwright browsers..."
    mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install" > /dev/null 2>&1 || true
    echo "⚠️ Browser installation completed (tests will run in headless mode if needed)"
}

# Function to run the menu navigation test
run_menu_navigation_test() {
    echo "🧪 Running Sample Data Menu Navigation Test..."
    echo "=================================="
    echo "This test will:"
    echo "  1. Load sample data"
    echo "  2. Display login screen"
    echo "  3. Login to the application"
    echo "  4. Navigate all generated menu items"
    echo "  5. Generate screenshots for each step"
    echo ""
    
    # Create screenshots directory
    mkdir -p target/screenshots
    
    # Install Playwright browsers if needed
    install_playwright_browsers
    
    # Run the test with Playwright-specific profile
    if mvn test -Dtest="automated_tests.tech.derbent.ui.automation.CSampleDataMenuNavigationTest" -Dspring.profiles.active=test -Dplaywright.headless=true; then
        echo "✅ Menu navigation test completed successfully!"
        
        # Show screenshot count
        screenshot_count=$(find target/screenshots -name "*.png" 2>/dev/null | wc -l)
        if [[ $screenshot_count -gt 0 ]]; then
            echo "📸 Generated $screenshot_count Playwright screenshots in target/screenshots/"
            echo ""
            echo "Screenshots include:"
            find target/screenshots -name "*.png" -type f -printf "  - %f\n" | sort
        fi
        
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

Run Playwright UI tests for the Derbent application.

OPTIONS:
    (no args)       Run the sample data menu navigation test (default)
    menu            Run the sample data menu navigation test
    clean           Clean test artifacts (screenshots, reports)
    install         Install Playwright browsers
    help            Show this help message

DESCRIPTION:
    The menu navigation test performs the following:
    1. Initialize sample data in the database
    2. Display the login screen
    3. Login with test credentials
    4. Navigate through all dynamically generated menu items
    5. Capture screenshots at each step

    Screenshots are saved to: target/screenshots/

EXAMPLES:
    ./run-playwright-tests.sh              # Run menu navigation test
    ./run-playwright-tests.sh menu         # Run menu navigation test (explicit)
    ./run-playwright-tests.sh clean        # Clean up test artifacts
    ./run-playwright-tests.sh install      # Install Playwright browsers

