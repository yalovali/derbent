#!/bin/bash

# Playwright UI Test Automation Runner for Derbent Application
# This script provides easy ways to run the Playwright browser automation tests

set -e

echo "🚀 Derbent Playwright UI Test Automation Runner"
echo "==============================================="

# Function to install Playwright browsers
install_playwright_browsers() {
    echo "🔄 Installing Playwright browsers..."
    mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install" > /dev/null 2>&1 || true
    echo "⚠️ Browser installation completed (tests will run in headless mode if needed)"
}

# Function to run Playwright tests
run_playwright_tests() {
    local test_class=$1
    local test_name=$2
    
    echo "🧪 Running $test_name..."
    echo "=================================="
    
    # Create screenshots directory
    mkdir -p target/screenshots
    
    # Install Playwright browsers if needed
    install_playwright_browsers
    
    # Run the tests with Playwright-specific profile
    if mvn test -Dtest="$test_class" -Dspring.profiles.active=test -Dplaywirght.headless=true; then
        echo "✅ $test_name completed successfully!"
        
        # Show screenshot count
        screenshot_count=$(find target/screenshots -name "*.png" 2>/dev/null | wc -l)
        if [[ $screenshot_count -gt 0 ]]; then
            echo "📸 Generated $screenshot_count Playwright screenshots in target/screenshots/"
        fi
        
    else
        echo "❌ $test_name failed!"
        
        # Show any screenshots that were taken
        screenshot_count=$(find target/screenshots -name "*.png" 2>/dev/null | wc -l)
        if [[ $screenshot_count -gt 0 ]]; then
            echo "📸 Debug screenshots available in target/screenshots/ ($screenshot_count files)"
        fi
        
        return 1
    fi
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [option]"
    echo ""
    echo "Options:"
    echo "  all           Run all Playwright UI automation tests"
    echo "  playwright    Run Playwright browser automation tests"
    echo "  login         Run login/logout tests only"
    echo "  crud          Run CRUD operation tests only"
    echo "  grid          Run grid interaction tests only"
    echo "  navigation    Run navigation tests only"
    echo "  responsive    Run responsive design tests only"
    echo "  accessibility Run accessibility tests only"
    echo "  validation    Run form validation tests only"
    echo "  workflow      Run complete workflow tests"
    echo "  colors        Run user color and entry views tests only"
    echo "  clean         Clean previous test results"
    echo "  install       Install Playwright browsers"
    echo "  help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 all        # Run all Playwright automation tests"
    echo "  $0 playwright # Run complete Playwright test suite"
    echo "  $0 login      # Run only login/logout tests"
    echo "  $0 colors     # Run only user color and entry views tests"
    echo "  $0 install    # Install Playwright browsers"
    echo "  $0 clean      # Clean up test artifacts"
}

# Main execution
main() {
    local command=${1:-help}
    
    case $command in
        "colors")
            echo "🎨 Running user color and entry views tests..."
            run_playwright_tests "tech.derbent.ui.automation.UserColorAndEntryViewsPlaywrightTest" "User Color and Entry Views Tests"
            ;;
            
        "all"|"playwright")
            echo "🎯 Running complete Playwright UI automation test suite..."
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest" "Playwright UI Automation Tests"
            ;;
            
        "login")
            echo "🔐 Running login/logout tests..."
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest#testLoginFunctionality,tech.derbent.ui.automation.PlaywrightUIAutomationTest#testLogoutFunctionality,tech.derbent.ui.automation.PlaywrightUIAutomationTest#testInvalidLoginHandling" "Login/Logout Tests"
            ;;
            
        "crud")
            echo "📝 Running CRUD operation tests..."
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest#testCRUDOperationsInProjects,tech.derbent.ui.automation.PlaywrightUIAutomationTest#testCRUDOperationsInMeetings" "CRUD Operation Tests"
            ;;
            
        "grid")
            echo "📊 Running grid interaction tests..."
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest#testGridInteractions" "Grid Interaction Tests"
            ;;
            
        "navigation")
            echo "🧭 Running navigation tests..."
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest#testNavigationBetweenViews" "Navigation Tests"
            ;;
            
        "responsive")
            echo "📱 Running responsive design tests..."
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest#testResponsiveDesign" "Responsive Design Tests"
            ;;
            
        "accessibility")
            echo "♿ Running accessibility tests..."
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest#testAccessibilityBasics" "Accessibility Tests"
            ;;
            
        "validation")
            echo "✅ Running form validation tests..."
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest#testFormValidationAndErrorHandling" "Form Validation Tests"
            ;;
            
        "workflow")
            echo "🔄 Running complete workflow tests..."
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest#testCompleteApplicationFlow" "Complete Workflow Tests"
            ;;
            
        "install")
            echo "🔧 Installing Playwright browsers..."
            install_playwright_browsers
            ;;
            
        "clean")
            echo "🧹 Cleaning test artifacts..."
            rm -rf target/screenshots target/surefire-reports target/test-classes
            echo "✅ Cleanup completed!"
            ;;
            
        "help"|*)
            show_usage
            ;;
    esac
}

# Run the main function
main "$@"