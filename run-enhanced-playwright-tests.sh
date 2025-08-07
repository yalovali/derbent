#!/bin/bash

# Enhanced Playwright Test Runner with Virtual Display Support
# This script runs Playwright tests with proper display setup for headless environments

set -e

echo "🚀 Enhanced Playwright Test Runner with Virtual Display"
echo "======================================================"

# Function to setup virtual display
setup_virtual_display() {
    echo "🖥️ Setting up virtual display..."
    
    # Start virtual display if not already running
    if ! pgrep -x "Xvfb" > /dev/null; then
        echo "Starting Xvfb on display :99..."
        Xvfb :99 -screen 0 1920x1080x24 -nolisten tcp -dpi 96 &
        XVFB_PID=$!
        export DISPLAY=:99
        sleep 2  # Give Xvfb time to start
        echo "✅ Virtual display started (PID: $XVFB_PID)"
    else
        echo "✅ Xvfb already running"
        export DISPLAY=:99
    fi
}

# Function to run tests with virtual display
run_tests_with_display() {
    local test_class=$1
    local test_name=$2
    
    echo "🧪 Running $test_name with virtual display..."
    echo "=============================================="
    
    # Create screenshots directory
    mkdir -p target/screenshots
    
    # Set up environment for headless browser testing
    export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=0
    export PLAYWRIGHT_BROWSERS_PATH=""
    
    # Use chromium-browser if available
    if [ -f "/usr/bin/chromium-browser" ]; then
        echo "📋 Using system Chromium browser: /usr/bin/chromium-browser"
    fi
    
    # Run tests with Maven
    if xvfb-run -a --server-args="-screen 0 1920x1080x24 -nolisten tcp -dpi 96" \
       mvn test -Dtest="$test_class" \
       -Dspring.profiles.active=test \
       -Dheadless=false \
       -Djava.awt.headless=false; then
        
        echo "✅ $test_name completed successfully!"
        
        # Show screenshot count
        screenshot_count=$(find target/screenshots -name "*.png" 2>/dev/null | wc -l)
        if [[ $screenshot_count -gt 0 ]]; then
            echo "📸 Generated $screenshot_count screenshots in target/screenshots/"
            echo "Screenshots:"
            ls -la target/screenshots/*.png 2>/dev/null || true
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
    echo "  simple        Run simple Playwright test with screenshots"
    echo "  all           Run all Playwright tests"
    echo "  accessibility Run accessibility tests"
    echo "  workflow      Run complete workflow tests"
    echo "  clean         Clean previous test results"
    echo "  setup         Set up virtual display only"
    echo "  help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 simple        # Run simple screenshot test"
    echo "  $0 all           # Run all Playwright tests with screenshots"
    echo "  $0 setup         # Set up virtual display for manual testing"
}

# Main execution
main() {
    local command=${1:-help}
    
    case $command in
        "simple")
            echo "🎯 Running simple Playwright screenshot test..."
            setup_virtual_display
            run_tests_with_display "PlaywrightSimpleTest" "Simple Screenshot Test"
            ;;
            
        "accessibility")
            echo "♿ Running accessibility tests with screenshots..."
            setup_virtual_display
            run_tests_with_display "PlaywrightUIAutomationTest#testAccessibilityBasics" "Accessibility Tests"
            ;;
            
        "workflow")
            echo "🔄 Running workflow tests with screenshots..."
            setup_virtual_display
            run_tests_with_display "PlaywrightUIAutomationTest#testCompleteApplicationFlow" "Workflow Tests"
            ;;
            
        "all")
            echo "🎯 Running all Playwright tests with screenshots..."
            setup_virtual_display
            run_tests_with_display "PlaywrightUIAutomationTest" "All Playwright Tests"
            ;;
            
        "setup")
            echo "🔧 Setting up virtual display..."
            setup_virtual_display
            echo "✅ Virtual display setup completed. DISPLAY=$DISPLAY"
            ;;
            
        "clean")
            echo "🧹 Cleaning test artifacts..."
            rm -rf target/screenshots target/surefire-reports target/test-classes
            pkill -f "Xvfb" || true
            echo "✅ Cleanup completed!"
            ;;
            
        "help"|*)
            show_usage
            ;;
    esac
}

# Run the main function
main "$@"