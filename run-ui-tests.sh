#!/bin/bash

# UI Test Automation Runner for Derbent Application
# This script provides easy ways to run the browser automation tests

set -e

echo "🚀 Derbent UI Test Automation Runner"
echo "=================================="

# Function to check if Chrome is installed
check_chrome() {
    if command -v google-chrome &> /dev/null || command -v chromium-browser &> /dev/null || command -v chrome &> /dev/null; then
        echo "✅ Chrome browser found"
    else
        echo "❌ Chrome browser not found. Please install Chrome to run UI tests."
        echo "   Ubuntu: sudo apt-get install google-chrome-stable"
        echo "   macOS: brew install --cask google-chrome"
        exit 1
    fi
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
    
    echo "🧪 Running $test_name..."
    echo "=================================="
    
    # Create screenshots directory
    mkdir -p target/screenshots
    
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

# Function to show usage
show_usage() {
    echo "Usage: $0 [option]"
    echo ""
    echo "Options:"
    echo "  all           Run all UI automation tests"
    echo "  testbench     Run Vaadin TestBench tests only"
    echo "  selenium      Run Selenium WebDriver tests only"
    echo "  unit          Run existing unit tests first"
    echo "  clean         Clean previous test results"
    echo "  help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 all        # Run all browser automation tests"
    echo "  $0 selenium   # Run only the free Selenium tests"
    echo "  $0 clean      # Clean up test artifacts"
}

# Main execution
main() {
    local command=${1:-help}
    
    case $command in
        "all")
            check_chrome
            echo "🎯 Running ALL UI automation tests..."
            run_tests "*UIAutomationTest" "All UI Automation Tests"
            ;;
            
        "testbench")
            check_chrome
            echo "🎯 Running Vaadin TestBench tests..."
            run_tests "ComprehensiveUIAutomationTest" "Vaadin TestBench Tests"
            ;;
            
        "selenium")
            check_chrome
            echo "🎯 Running Selenium WebDriver tests..."
            run_tests "SeleniumUIAutomationTest" "Selenium WebDriver Tests"
            ;;
            
        "unit")
            echo "🧪 Running existing unit tests first..."
            if mvn test -Dtest="CMeetingsViewUITest"; then
                echo "✅ Unit tests passed!"
            else
                echo "❌ Unit tests failed!"
                return 1
            fi
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