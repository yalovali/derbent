#!/bin/bash

# Playwright UI Test Automation Runner for Derbent Application
# This script runs the sample data menu navigation test: login screen, login, navigate all generated menu items

set -e

# Setup Java 21 environment
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/setup-java-env.sh"

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
    
    # Set Playwright environment variables to use cached browser
    export PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright"
    export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true
    
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

# Function to run the company login test
run_company_login_test() {
    echo "🔐 Running Company-Aware Login Test..."
    echo "=================================="
    echo "This test will:"
    echo "  1. Validate company selection dropdown"
    echo "  2. Test username@companyId pattern"
    echo "  3. Verify multi-tenant isolation"
    echo "  4. Test login with multiple companies"
    echo ""
    
    mkdir -p target/screenshots
    install_playwright_browsers
    
    # Set Playwright environment variables to use cached browser
    export PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright"
    export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true
    
    if mvn test -Dtest="automated_tests.tech.derbent.ui.automation.CCompanyAwareLoginTest" -Dspring.profiles.active=test -Dplaywright.headless=true; then
        echo "✅ Company login test completed successfully!"
        show_screenshots
    else
        echo "❌ Company login test failed!"
        show_screenshots
        return 1
    fi
}

# Function to run comprehensive dynamic views test
run_comprehensive_test() {
    echo "🚀 Running Comprehensive Dynamic Views Test..."
    echo "=================================="
    echo "This test will:"
    echo "  1. Test complete navigation coverage"
    echo "  2. Validate dynamic page loading"
    echo "  3. Test CRUD operations on entities"
    echo "  4. Test grid functionality"
    echo "  5. Test form validation"
    echo ""
    
    mkdir -p target/screenshots
    install_playwright_browsers
    
    # Set Playwright environment variables to use cached browser
    export PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright"
    export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true
    
    if mvn test -Dtest="automated_tests.tech.derbent.ui.automation.CComprehensiveDynamicViewsTest" -Dspring.profiles.active=test -Dplaywright.headless=true; then
        echo "✅ Comprehensive test completed successfully!"
        show_screenshots
    else
        echo "❌ Comprehensive test failed!"
        show_screenshots
        return 1
    fi
}

# Function to run type and status CRUD tests
run_type_status_test() {
    echo "🔧 Running Type and Status CRUD Test..."
    echo "=================================="
    echo "This test will:"
    echo "  1. Test complete CRUD operations on Type entities"
    echo "  2. Test complete CRUD operations on Status entities"
    echo "  3. Validate toolbar button operations"
    echo "  4. Verify responses to updates (notifications, grid refresh)"
    echo "  5. Test validation and error handling"
    echo ""
    
    mkdir -p target/screenshots
    install_playwright_browsers
    
    # Set Playwright environment variables to use cached browser
    export PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright"
    export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true
    
    if mvn test -Dtest="automated_tests.tech.derbent.ui.automation.CTypeStatusCrudTest" -Dspring.profiles.active=test -Dplaywright.headless=true; then
        echo "✅ Type and Status CRUD test completed successfully!"
        show_screenshots
    else
        echo "❌ Type and Status CRUD test failed!"
        show_screenshots
        return 1
    fi
}

# Function to run button functionality test
run_button_functionality_test() {
    echo "🔘 Running Button Functionality Test..."
    echo "=================================="
    echo "This test will:"
    echo "  1. Navigate to all pages systematically"
    echo "  2. Test New button presence and responsiveness"
    echo "  3. Test Save button functionality"
    echo "  4. Test Delete button functionality"
    echo "  5. Verify all buttons are working correctly"
    echo ""
    
    mkdir -p target/screenshots
    install_playwright_browsers
    
    # Set Playwright environment variables to use cached browser
    export PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright"
    export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true
    
    if mvn test -Dtest="automated_tests.tech.derbent.ui.automation.CButtonFunctionalityTest" -Dspring.profiles.active=test -Dplaywright.headless=true; then
        echo "✅ Button functionality test completed successfully!"
        show_screenshots
    else
        echo "❌ Button functionality test failed!"
        show_screenshots
        return 1
    fi
}

# Function to run all tests
run_all_tests() {
    echo "🧪 Running All Playwright Tests..."
    echo "=================================="
    echo ""
    
    mkdir -p target/screenshots
    install_playwright_browsers
    
    local failed=0
    
    echo "▶️ Test 1/5: Menu Navigation Test"
    if ! run_menu_navigation_test; then
        failed=$((failed + 1))
    fi
    echo ""
    
    echo "▶️ Test 2/5: Company Login Test"
    if ! run_company_login_test; then
        failed=$((failed + 1))
    fi
    echo ""
    
    echo "▶️ Test 3/5: Comprehensive Dynamic Views Test"
    if ! run_comprehensive_test; then
        failed=$((failed + 1))
    fi
    echo ""
    
    echo "▶️ Test 4/5: Type and Status CRUD Test"
    if ! run_type_status_test; then
        failed=$((failed + 1))
    fi
    echo ""
    
    echo "▶️ Test 5/5: Button Functionality Test"
    if ! run_button_functionality_test; then
        failed=$((failed + 1))
    fi
    echo ""

    if [[ $failed -eq 0 ]]; then
        echo "✅ All tests completed successfully!"
        return 0
    else
        echo "❌ $failed test(s) failed"
        return 1
    fi
}

# Function to show screenshots
show_screenshots() {
    screenshot_count=$(find target/screenshots -name "*.png" 2>/dev/null | wc -l)
    if [[ $screenshot_count -gt 0 ]]; then
        echo "📸 Generated $screenshot_count screenshots in target/screenshots/"
        if [[ $screenshot_count -le 20 ]]; then
            echo ""
            echo "Screenshots:"
            find target/screenshots -name "*.png" -type f -printf "  - %f\n" | sort
        fi
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
    login           Run the company-aware login pattern test
    comprehensive   Run comprehensive dynamic views test
    status-types    Run Type and Status CRUD operations test
    buttons         Run button functionality test across all pages
    all             Run all Playwright tests
    clean           Clean test artifacts (screenshots, reports)
    install         Install Playwright browsers
    help            Show this help message

DESCRIPTION:
    Available test suites:
    
    1. Menu Navigation Test (default)
       - Initialize sample data in the database
       - Display the login screen
       - Login with test credentials
       - Navigate through all dynamically generated menu items
       - Capture screenshots at each step
    
    2. Company Login Test
       - Validate company selection dropdown
       - Test username@companyId authentication pattern
       - Verify multi-tenant login isolation
       - Test login with multiple companies
    
    3. Comprehensive Dynamic Views Test
       - Complete navigation coverage of all views
       - Dynamic page loading validation
       - CRUD operations testing on key entities
       - Grid functionality across views
       - Form validation testing
    
    4. Type and Status CRUD Test
       - Test complete CRUD operations on Type entities
       - Test complete CRUD operations on Status entities
       - Validate toolbar button operations (New, Save, Delete, Refresh)
       - Verify responses to updates (notifications, grid refresh)
       - Test validation and error handling
    
    5. Button Functionality Test
       - Navigate to all pages systematically
       - Test New button presence and responsiveness on each page
       - Test Save button functionality after form fills
       - Test Delete button functionality with data selection
       - Verify all buttons are working correctly across the application
    
    Screenshots are saved to: target/screenshots/

EXAMPLES:
    ./run-playwright-tests.sh              # Run menu navigation test
    ./run-playwright-tests.sh menu         # Run menu navigation test (explicit)
    ./run-playwright-tests.sh login        # Run company login test
    ./run-playwright-tests.sh comprehensive # Run comprehensive test
    ./run-playwright-tests.sh status-types # Run Type and Status CRUD test
    ./run-playwright-tests.sh buttons      # Run button functionality test
    ./run-playwright-tests.sh all          # Run all tests
    ./run-playwright-tests.sh clean        # Clean up test artifacts
    ./run-playwright-tests.sh install      # Install Playwright browsers

EOF
}

# Main script logic
case "${1:-menu}" in
    menu)
        run_menu_navigation_test
        ;;
    login)
        run_company_login_test
        ;;
    comprehensive)
        run_comprehensive_test
        ;;
    status-types)
        run_type_status_test
        ;;
    buttons)
        run_button_functionality_test
        ;;
    all)
        run_all_tests
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
