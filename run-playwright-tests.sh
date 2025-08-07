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
    if mvn test -Dtest="$test_class" -Dspring.profiles.active=test -Dplaywright.headless=true; then
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

# Function to run mock tests that demonstrate screenshot functionality
run_mock_tests() {
    echo "🎭 Running mock Playwright tests with screenshot generation..."
    echo "============================================================="
    
    # Create screenshots directory
    mkdir -p target/screenshots
    
    # Run mock tests that generate screenshots
    if mvn test -Dtest="PlaywrightMockTest" -Dspring.profiles.active=test --batch-mode; then
        echo "✅ Mock tests completed successfully!"
        
        # Show screenshot count
        screenshot_count=$(find target/screenshots -name "*.png" 2>/dev/null | wc -l)
        if [[ $screenshot_count -gt 0 ]]; then
            echo "📸 Generated $screenshot_count mock screenshots in target/screenshots/"
            echo "Screenshots created:"
            ls -1 target/screenshots/*.png 2>/dev/null | head -10 || true
            if [[ $screenshot_count -gt 10 ]]; then
                echo "... and $((screenshot_count - 10)) more screenshots"
            fi
        fi
        
    else
        echo "❌ Mock tests failed!"
        return 1
    fi
}

# Function to run with Docker (if available)
run_with_docker() {
    echo "🐳 Running Playwright tests with Docker..."
    echo "=========================================="
    
    if command -v docker &> /dev/null; then
        echo "Building Playwright Docker image..."
        docker build -f Dockerfile.playwright -t derbent-playwright .
        
        echo "Running tests in Docker container..."
        docker run --rm -v $(pwd)/target:/app/target derbent-playwright
        
        # Show results
        screenshot_count=$(find target/screenshots -name "*.png" 2>/dev/null | wc -l)
        if [[ $screenshot_count -gt 0 ]]; then
            echo "📸 Generated $screenshot_count Docker screenshots in target/screenshots/"
        fi
    else
        echo "❌ Docker not available. Please install Docker to use this option."
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
    echo "  comprehensive Run comprehensive tests for ALL views (new)"
    echo "  status-types  Run status and type views tests only (new)"
    echo "  main-views    Run main business views tests only (new)"
    echo "  admin-views   Run administrative views tests only (new)"
    echo "  kanban-views  Run kanban board views tests only (new)"
    echo "  mock          Run mock tests that demonstrate screenshot functionality"
    echo "  docker        Run tests using Docker (recommended)"
    echo "  login         Run login/logout tests only"
    echo "  crud          Run CRUD operation tests only"
    echo "  grid          Run grid interaction tests only"
    echo "  search        Run search functionality tests only"
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
    echo "  $0 mock           # Run mock tests to demonstrate screenshot functionality"
    echo "  $0 comprehensive  # Run tests for ALL views (new comprehensive testing)"
    echo "  $0 status-types   # Run tests for all status and type views"
    echo "  $0 main-views     # Run tests for all main business views"
    echo "  $0 docker         # Run real Playwright tests using Docker (recommended)"
    echo "  $0 all            # Run all Playwright automation tests"
    echo "  $0 playwright     # Run complete Playwright test suite"
    echo "  $0 login          # Run only login/logout tests"
    echo "  $0 colors         # Run only user color and entry views tests"
    echo "  $0 install        # Install Playwright browsers"
    echo "  $0 clean          # Clean up test artifacts"
    echo ""
    echo "Note: Use 'mock' option to see screenshot functionality without browser issues."
    echo "      Use 'docker' option for full Playwright testing with real browsers."
}

# Main execution
main() {
    local command=${1:-help}
    
    case $command in
        "mock")
            echo "🎭 Running mock Playwright tests with screenshots..."
            run_mock_tests
            ;;
            
        "docker")
            echo "🐳 Running Playwright tests with Docker..."
            run_with_docker
            ;;
            
        "comprehensive")
            echo "🌟 Running comprehensive tests for ALL views..."
            run_playwright_tests "automated_tests.tech.derbent.ui.automation.ComprehensiveViewsPlaywrightTest" "Comprehensive All Views Tests"
            ;;
            
        "status-types")
            echo "⚙️ Running status and type views tests..."
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest#testAllStatusAndTypeViews" "Status and Type Views Tests"
            ;;
            
        "main-views")
            echo "🏢 Running main business views tests..."
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest#testAllMainBusinessViews" "Main Business Views Tests"
            ;;
            
        "admin-views")
            echo "🔧 Running administrative views tests..."
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest#testAllAdministrativeViews" "Administrative Views Tests"
            ;;
            
        "kanban-views")
            echo "📋 Running kanban board views tests..."
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest#testAllKanbanViews" "Kanban Board Views Tests"
            ;;
            
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
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest#testGridInteractions,tech.derbent.ui.automation.PlaywrightUIAutomationTest#testEntityRelationGrids" "Grid Interaction Tests"
            ;;
            
        "search")
            echo "🔍 Running search functionality tests..."
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest#testSearchFunctionality" "Search Functionality Tests"
            ;;
            
        "navigation")
            echo "🧭 Running navigation tests..."
            run_playwright_tests "tech.derbent.ui.automation.PlaywrightUIAutomationTest#testNavigationBetweenViews,tech.derbent.ui.automation.PlaywrightUIAutomationTest#testComprehensiveAllViewsNavigation" "Navigation Tests"
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