#!/bin/bash

# UI Test Automation Runner for Derbent Application
# This script provides easy ways to run both Playwright and Selenium browser automation tests

set -e

echo "🚀 Derbent UI Test Automation Runner (Dual Framework)"
echo "====================================================="

# Function to install Playwright browsers
install_playwright_browsers() {
    echo "🔄 Installing Playwright browsers..."
    mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "✅ Playwright browsers installed successfully"
    else
        echo "⚠️ Playwright browser installation may have failed, but continuing..."
    fi
}

# Function to install Selenium WebDriver binaries
install_selenium_drivers() {
    echo "🔄 Selenium WebDriver binaries managed automatically by WebDriverManager..."
    echo "✅ Chrome driver will be downloaded automatically when needed"
}

# Function to start the application in background
start_application() {
    echo "🔄 Starting application..."
    mvn spring-boot:run > application.log 2>&1 &
    APP_PID=$!
    echo "📱 Application starting (PID: $APP_PID)..."
    
    # Wait for application to be ready
    echo "⏳ Waiting for application to be ready..."
    for i in {1..30}; do
        if curl -s http://localhost:8080 > /dev/null 2>&1; then
            echo "✅ Application is ready!"
            return 0
        fi
        sleep 2
        echo "   Waiting... ($i/30)"
    done
    
    echo "❌ Application failed to start within 60 seconds"
    kill $APP_PID 2>/dev/null || true
    exit 1
}

# Function to stop the application
stop_application() {
    if [[ -n "$APP_PID" ]]; then
        echo "🛑 Stopping application (PID: $APP_PID)..."
        kill $APP_PID 2>/dev/null || true
        wait $APP_PID 2>/dev/null || true
    fi
}

# Function to run tests
run_tests() {
    local test_class=$1
    local test_name=$2
    local framework=$3
    
    echo "🧪 Running $test_name ($framework)..."
    echo "=================================="
    
    # Create screenshots directory
    mkdir -p target/screenshots
    
    # Install browsers/drivers based on framework
    if [[ "$framework" == "Playwright" ]]; then
        install_playwright_browsers
    elif [[ "$framework" == "Selenium" ]]; then
        install_selenium_drivers
    fi
    
    # Run the tests
    if mvn test -Dtest="$test_class" -Dspring.profiles.active=test; then
        echo "✅ $test_name completed successfully!"
        
        # Show screenshot count
        screenshot_count=$(find target/screenshots -name "*.png" 2>/dev/null | wc -l)
        if [[ $screenshot_count -gt 0 ]]; then
            echo "📸 Generated $screenshot_count screenshots in target/screenshots/"
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

# Function to run both frameworks
run_both_frameworks() {
    echo "🎯 Running UI tests with both Playwright and Selenium frameworks..."
    
    local success=true
    
    # Run Playwright tests
    if ! run_tests "PlaywrightUIAutomationTest" "Playwright UI Automation Tests" "Playwright"; then
        success=false
    fi
    
    echo ""
    echo "---"
    echo ""
    
    # Run Selenium tests
    if ! run_tests "SeleniumUIAutomationTest" "Selenium UI Automation Tests" "Selenium"; then
        success=false
    fi
    
    if $success; then
        echo ""
        echo "🎉 All UI automation tests completed successfully!"
    else
        echo ""
        echo "⚠️ Some tests failed - check logs and screenshots for details"
        return 1
    fi
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [option]"
    echo ""
    echo "Framework Options:"
    echo "  all           Run all UI automation tests (both frameworks)"
    echo "  both          Run both Playwright and Selenium test suites"
    echo "  playwright    Run Playwright browser automation tests only"
    echo "  selenium      Run Selenium WebDriver automation tests only"
    echo ""
    echo "Test Category Options:"
    echo "  login         Run login/logout tests (both frameworks)"
    echo "  crud          Run CRUD operation tests (both frameworks)"
    echo "  grid          Run grid interaction tests (both frameworks)"
    echo "  navigation    Run navigation tests (both frameworks)"
    echo "  responsive    Run responsive design tests (both frameworks)"
    echo "  validation    Run form validation tests (both frameworks)"
    echo ""
    echo "Utility Options:"
    echo "  install       Install browsers and drivers for both frameworks"
    echo "  clean         Clean previous test results"
    echo "  compare       Run same tests in both frameworks for comparison"
    echo "  help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 all        # Run all tests with both frameworks"
    echo "  $0 playwright # Run only Playwright tests"
    echo "  $0 selenium   # Run only Selenium tests"
    echo "  $0 login      # Run login tests with both frameworks"
    echo "  $0 compare    # Run identical tests in both frameworks"
    echo "  $0 install    # Install all browsers and drivers"
    echo "  $0 clean      # Clean up test artifacts"
}

# Function to run comparative tests
run_comparative_tests() {
    echo "🔄 Running comparative tests with both frameworks..."
    
    local test_categories=("login" "navigation" "grid")
    local success=true
    
    for category in "${test_categories[@]}"; do
        echo ""
        echo "📊 Comparing $category tests..."
        echo "==============================="
        
        # Run with Playwright
        echo "🎭 Running $category with Playwright..."
        case $category in
            "login")
                if ! run_tests "PlaywrightUIAutomationTest#testLoginFunctionality" "Playwright Login Test" "Playwright"; then
                    success=false
                fi
                ;;
            "navigation")
                if ! run_tests "PlaywrightUIAutomationTest#testNavigationBetweenViews" "Playwright Navigation Test" "Playwright"; then
                    success=false
                fi
                ;;
            "grid")
                if ! run_tests "PlaywrightUIAutomationTest#testGridInteractions" "Playwright Grid Test" "Playwright"; then
                    success=false
                fi
                ;;
        esac
        
        echo ""
        
        # Run with Selenium
        echo "🌐 Running $category with Selenium..."
        case $category in
            "login")
                if ! run_tests "SeleniumUIAutomationTest#testApplicationLoadsAndLoginFunctionality" "Selenium Login Test" "Selenium"; then
                    success=false
                fi
                ;;
            "navigation")
                if ! run_tests "SeleniumUIAutomationTest#testNavigationBetweenViews" "Selenium Navigation Test" "Selenium"; then
                    success=false
                fi
                ;;
            "grid")
                if ! run_tests "SeleniumUIAutomationTest#testGridInteractions" "Selenium Grid Test" "Selenium"; then
                    success=false
                fi
                ;;
        esac
        
        echo "---"
    done
    
    if $success; then
        echo ""
        echo "🎉 Comparative testing completed successfully!"
        echo "📊 Check screenshots to compare framework behaviors"
    else
        echo ""
        echo "⚠️ Some comparative tests failed"
        return 1
    fi
}

# Main execution
main() {
    local command=${1:-help}
    
    case $command in
        "all"|"both")
            run_both_frameworks
            ;;
            
        "playwright")
            echo "🎭 Running Playwright UI automation tests..."
            run_tests "PlaywrightUIAutomationTest" "Playwright UI Automation Tests" "Playwright"
            ;;
            
        "selenium")
            echo "🌐 Running Selenium UI automation tests..."
            run_tests "SeleniumUIAutomationTest" "Selenium UI Automation Tests" "Selenium"
            ;;
            
        "login")
            echo "🔐 Running login/logout tests with both frameworks..."
            run_tests "PlaywrightUIAutomationTest#testLoginFunctionality,PlaywrightUIAutomationTest#testLogoutFunctionality" "Playwright Login Tests" "Playwright"
            echo ""
            run_tests "SeleniumUIAutomationTest#testApplicationLoadsAndLoginFunctionality,SeleniumUIAutomationTest#testLogoutFunctionality" "Selenium Login Tests" "Selenium"
            ;;
            
        "crud")
            echo "📝 Running CRUD operation tests with both frameworks..."
            run_tests "PlaywrightUIAutomationTest#testCRUDOperationsInProjects" "Playwright CRUD Tests" "Playwright"
            echo ""
            run_tests "SeleniumUIAutomationTest#testCRUDOperations" "Selenium CRUD Tests" "Selenium"
            ;;
            
        "grid")
            echo "📊 Running grid interaction tests with both frameworks..."
            run_tests "PlaywrightUIAutomationTest#testGridInteractions" "Playwright Grid Tests" "Playwright"
            echo ""
            run_tests "SeleniumUIAutomationTest#testGridInteractions" "Selenium Grid Tests" "Selenium"
            ;;
            
        "navigation")
            echo "🧭 Running navigation tests with both frameworks..."
            run_tests "PlaywrightUIAutomationTest#testNavigationBetweenViews" "Playwright Navigation Tests" "Playwright"
            echo ""
            run_tests "SeleniumUIAutomationTest#testNavigationBetweenViews" "Selenium Navigation Tests" "Selenium"
            ;;
            
        "responsive")
            echo "📱 Running responsive design tests with both frameworks..."
            run_tests "PlaywrightUIAutomationTest#testResponsiveDesign" "Playwright Responsive Tests" "Playwright"
            echo ""
            run_tests "SeleniumUIAutomationTest#testResponsiveDesignAndMobileView" "Selenium Responsive Tests" "Selenium"
            ;;
            
        "validation")
            echo "✅ Running form validation tests with both frameworks..."
            run_tests "PlaywrightUIAutomationTest#testFormValidationAndErrorHandling" "Playwright Validation Tests" "Playwright"
            echo ""
            run_tests "SeleniumUIAutomationTest#testFormValidationAndErrorHandling" "Selenium Validation Tests" "Selenium"
            ;;
            
        "compare")
            run_comparative_tests
            ;;
            
        "install")
            echo "🔧 Installing browsers and drivers for both frameworks..."
            install_playwright_browsers
            install_selenium_drivers
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

# Set up trap to cleanup on exit
trap stop_application EXIT

# Run the main function
main "$@"