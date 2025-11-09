#!/bin/bash

# Selenium UI Test Automation Runner for Derbent Application
# This script runs Selenium-based UI tests with support for headless and visible browser modes

set -e

echo "🚀 Derbent Selenium UI Test Automation Runner"
echo "=============================================="

# Default values
HEADLESS="true"
TEST_CLASS=""
PROFILE="test"

# Function to show usage
show_usage() {
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -h, --headless    Run in headless mode (default: true)"
    echo "  -v, --visible     Run with visible browser"
    echo "  -t, --test CLASS  Run specific test class (default: all Selenium tests)"
    echo "  -p, --profile     Spring profile to use (default: test)"
    echo "  --help            Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Run all Selenium tests in headless mode"
    echo "  $0 --visible                          # Run all Selenium tests with visible browser"
    echo "  $0 -t CSeleniumProjectCrudDemoTest    # Run specific test in headless mode"
    echo "  $0 -v -t CSeleniumProjectCrudDemoTest # Run specific test with visible browser"
    echo ""
    exit 0
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--headless)
            HEADLESS="true"
            shift
            ;;
        -v|--visible)
            HEADLESS="false"
            shift
            ;;
        -t|--test)
            TEST_CLASS="$2"
            shift 2
            ;;
        -p|--profile)
            PROFILE="$2"
            shift 2
            ;;
        --help)
            show_usage
            ;;
        *)
            echo "Unknown option: $1"
            show_usage
            ;;
    esac
done

# Create screenshots directory
mkdir -p target/screenshots

echo ""
echo "📋 Test Configuration:"
echo "  Browser Mode: $([ "$HEADLESS" = "true" ] && echo "Headless" || echo "Visible")"
echo "  Test Class: $([ -z "$TEST_CLASS" ] && echo "All Selenium tests" || echo "$TEST_CLASS")"
echo "  Spring Profile: $PROFILE"
echo ""

# Build the Maven command
MVN_CMD="mvn test -Dspring.profiles.active=$PROFILE -Dselenium.headless=$HEADLESS"

if [ -n "$TEST_CLASS" ]; then
    # Run specific test class
    MVN_CMD="$MVN_CMD -Dtest=automated_tests.tech.derbent.ui.selenium.$TEST_CLASS"
else
    # Run all Selenium tests
    MVN_CMD="$MVN_CMD -Dtest=automated_tests.tech.derbent.ui.selenium.*"
fi

echo "🧪 Running Selenium tests..."
echo "Command: $MVN_CMD"
echo ""

# Run the tests
if $MVN_CMD; then
    echo ""
    echo "✅ Selenium tests completed successfully!"
    
    # Show screenshot count
    screenshot_count=$(find target/screenshots -name "selenium-*.png" 2>/dev/null | wc -l)
    if [[ $screenshot_count -gt 0 ]]; then
        echo "📸 Generated $screenshot_count Selenium screenshots in target/screenshots/"
        echo ""
        echo "Screenshots include:"
        find target/screenshots -name "selenium-*.png" -type f -printf "  - %f\n" | sort
    else
        echo "ℹ️ No Selenium screenshots were generated"
    fi
    
    echo ""
    echo "📁 Full test results available in:"
    echo "  - Screenshots: target/screenshots/"
    echo "  - Test reports: target/surefire-reports/"
    echo ""
    exit 0
else
    echo ""
    echo "❌ Selenium tests failed!"
    echo "Check the test output above for details."
    echo ""
    
    # Try to show recent screenshots even on failure
    screenshot_count=$(find target/screenshots -name "selenium-*.png" 2>/dev/null | wc -l)
    if [[ $screenshot_count -gt 0 ]]; then
        echo "📸 $screenshot_count screenshots captured (including failure screenshots)"
        echo "View them in: target/screenshots/"
    fi
    
    exit 1
fi
