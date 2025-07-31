#!/bin/bash

# Selenium UI Test Automation Runner for Derbent Application
# This script provides easy ways to run the Selenium WebDriver automation tests

set -e

echo "🚀 Derbent Selenium UI Test Automation Runner"
echo "=============================================="

# Function to install WebDriver binaries
install_webdriver_binaries() {
    echo "🔄 WebDriver binaries will be managed automatically by WebDriverManager..."
    echo "✅ WebDriverManager will download Chrome driver as needed"
}

# Function to run Selenium tests
run_selenium_tests() {
    local test_class=$1
    local test_name=$2
    
    echo "🧪 Running $test_name..."
    echo "=================================="
    
    # Create screenshots directory
    mkdir -p target/screenshots
    
    # Set system properties for headless mode
    export SELENIUM_HEADLESS=true
    
    # Run the tests with Selenium-specific profile
    if mvn test -Dtest="$test_class" -Dspring.profiles.active=test -Dselenium.headless=true; then
        echo "✅ $test_name completed successfully!"
        
        # Show screenshot count
        screenshot_count=$(find target/screenshots -name "selenium-*.png" 2>/dev/null | wc -l)
        if [[ $screenshot_count -gt 0 ]]; then
            echo "📸 Generated $screenshot_count Selenium screenshots in target/screenshots/"
        fi
        
    else
        echo "❌ $test_name failed!"
        
        # Show any screenshots that were taken
        screenshot_count=$(find target/screenshots -name "selenium-*.png" 2>/dev/null | wc -l)
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
    echo "  all           Run all Selenium UI automation tests"
    echo "  selenium      Run Selenium WebDriver automation tests"
    echo "  login         Run login/logout tests only"
    echo "  crud          Run CRUD operation tests only"
    echo "  grid          Run grid interaction tests only"
    echo "  responsive    Run responsive design tests only"
    echo "  validation    Run form validation tests only"
    echo "  clean         Clean previous test results"
    echo "  install       Check WebDriver setup"
    echo "  help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 all        # Run all Selenium automation tests"
    echo "  $0 selenium   # Run complete Selenium test suite"
    echo "  $0 login      # Run only login/logout tests"
    echo "  $0 clean      # Clean up test artifacts"
}

# Main execution
main() {
    local command=${1:-help}
    
    case $command in
        "all"|"selenium")
            echo "🎯 Running complete Selenium UI automation test suite..."
            run_selenium_tests "SeleniumUIAutomationTest" "Selenium UI Automation Tests"
            ;;
            
        "login")
            echo "🔐 Running login/logout tests..."
            run_selenium_tests "SeleniumUIAutomationTest#testApplicationLoadsAndLoginFunctionality,SeleniumUIAutomationTest#testLogoutFunctionality" "Login/Logout Tests"
            ;;
            
        "crud")
            echo "📝 Running CRUD operation tests..."
            run_selenium_tests "SeleniumUIAutomationTest#testCRUDOperations" "CRUD Operation Tests"
            ;;
            
        "grid")
            echo "📊 Running grid interaction tests..."
            run_selenium_tests "SeleniumUIAutomationTest#testGridInteractions" "Grid Interaction Tests"
            ;;
            
        "responsive")
            echo "📱 Running responsive design tests..."
            run_selenium_tests "SeleniumUIAutomationTest#testResponsiveDesignAndMobileView" "Responsive Design Tests"
            ;;
            
        "validation")
            echo "✅ Running form validation tests..."
            run_selenium_tests "SeleniumUIAutomationTest#testFormValidationAndErrorHandling" "Form Validation Tests"
            ;;
            
        "install")
            echo "🔧 Checking WebDriver setup..."
            install_webdriver_binaries
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