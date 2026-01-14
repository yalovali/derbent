#!/bin/bash

# Playwright UI Test Automation Runner for Derbent Application
# Comprehensive test suite with multiple test scenarios:
# - menu: Fast menu navigation test
# - comprehensive: Complete page testing with CRUD operations
# - all-views: Navigate through all application views
# - crud: Test CRUD operations on all pages with toolbars

set -e

# Setup Java 21 environment
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/bin/setup-java-env.sh"

echo "🚀 Derbent Playwright UI Test Suite"
echo "===================================="

# Default settings
HEADLESS_MODE="${PLAYWRIGHT_HEADLESS:-false}"
SHOW_CONSOLE="${PLAYWRIGHT_SHOW_CONSOLE:-true}"
SKIP_SCREENSHOTS="${PLAYWRIGHT_SKIP_SCREENSHOTS:-false}"
SLOWMO="${PLAYWRIGHT_SLOWMO:-0}"
VIEWPORT_WIDTH="${PLAYWRIGHT_VIEWPORT_WIDTH:-1920}"
VIEWPORT_HEIGHT="${PLAYWRIGHT_VIEWPORT_HEIGHT:-1080}"

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
    
    # Show options menu if INTERACTIVE mode is set
    if [ "${INTERACTIVE_MODE:-false}" = "true" ]; then
        show_options_menu
    fi
    
    run_test "automated_tests.tech.derbent.ui.automation.CMenuNavigationTest"
}

run_bab_menu_test() {
    echo "🧪 Running BAB Gateway Menu Navigation Test..."
    echo "=============================================="
    echo "This test will:"
    echo "  1. Select BAB Gateway schema on login"
    echo "  2. Reset the database with minimal BAB data"
    echo "  3. Login and browse available menu items"
    echo ""

    export PLAYWRIGHT_SCHEMA="BAB Gateway"
    export PLAYWRIGHT_FORCE_SAMPLE_RELOAD="true"
    export SPRING_PROFILES_ACTIVE="test,bab"

    run_test "automated_tests.tech.derbent.ui.automation.CBabMenuNavigationTest"
}

# Function to run comprehensive page test (all views)
run_comprehensive_test() {
    echo "🧪 Running Comprehensive Page Test..."
    echo "===================================="
    echo "This test will:"
    echo "  1. Login to the application"
    echo "  2. Navigate to all test auxiliary pages"
    echo "  3. Test each page for grids and CRUD toolbars"
    echo "  4. Run conditional tests based on page content"
    echo "  5. Capture detailed screenshots"
    
    # Show options menu if INTERACTIVE mode is set
    if [ "${INTERACTIVE_MODE:-false}" = "true" ]; then
        show_options_menu
    fi
    
    run_test "automated_tests.tech.derbent.ui.automation.CPageTestAuxillaryComprehensiveTest"
}

# Function to run all views navigation
run_all_views_test() {
    echo "🧪 Running All Views Navigation Test..."
    echo "======================================="
    echo "This test will:"
    echo "  1. Navigate through all application views"
    echo "  2. Capture screenshots of each view"
    echo "  3. Verify each page loads correctly"
    echo ""
    
    run_comprehensive_test
}

# Function to run CRUD operations test
run_crud_test() {
    echo "🧪 Running CRUD Operations Test..."
    echo "==================================="
    echo "This test will:"
    echo "  1. Navigate through all views with toolbars"
    echo "  2. Test CRUD operations (Create, Read, Update, Delete)"
    echo "  3. Test New, Edit, Delete, Save buttons"
    echo "  4. Verify form dialogs open and close"
    echo "  5. Capture screenshots at each step"
    echo ""
    
    run_comprehensive_test
}

# Function to run recent features CRUD test
run_recent_features_test() {
    echo "🧪 Running Recent Features CRUD Test..."
    echo "========================================"
    echo "This test will:"
    echo "  1. Test Issues & Bug Tracking CRUD operations"
    echo "  2. Test Teams Management CRUD operations"
    echo "  3. Test Attachments upload/download/delete"
    echo "  4. Test Comments add/edit/delete"
    echo "  5. Verify all recent commits (last 3 days) features"
    echo ""
    
    # Show options menu if INTERACTIVE mode is set
    if [ "${INTERACTIVE_MODE:-false}" = "true" ]; then
        show_options_menu
    fi
    
    run_test "automated_tests.tech.derbent.ui.automation.CRecentFeaturesCrudTest"
}

# Function to run workflow status and validation test
run_workflow_validation_test() {
    echo "🧪 Running Workflow Status and Name Validation Test..."
    echo "======================================================="
    echo "This test will:"
    echo "  1. Login to the application"
    echo "  2. Test status combobox appears for workflow entities"
    echo "  3. Verify status combobox shows valid transitions"
    echo "  4. Test save button disabled when name field is empty"
    echo "  5. Test save button enabled when name has content"
    echo "  6. Test name validation on multiple entity types"
    echo "  7. Capture detailed screenshots at each step"
    echo ""
    
    # Show options menu if INTERACTIVE mode is set
    if [ "${INTERACTIVE_MODE:-false}" = "true" ]; then
        show_options_menu
    fi
    
    run_test "automated_tests.tech.derbent.ui.automation.CWorkflowStatusAndValidationTest"
}

# Function to show interactive options menu
show_options_menu() {
    echo ""
    echo "🎛️  Test Configuration Options"
    echo "═══════════════════════════════════════════════════════════"
    echo ""
    echo "1. Browser Visibility"
    echo "   Current: $([ "$HEADLESS_MODE" = "true" ] && echo "HEADLESS (no browser window)" || echo "VISIBLE (show browser window)")"
    echo ""
    echo "2. Console Output"
    echo "   Current: $([ "$SHOW_CONSOLE" = "true" ] && echo "ENABLED (show test logs)" || echo "SUPPRESSED (quiet mode)")"
    echo ""
    echo "3. Screenshot Capture"
    echo "   Current: $([ "$SKIP_SCREENSHOTS" = "true" ] && echo "DISABLED (no screenshots)" || echo "ENABLED (capture screenshots)")"
    echo ""
    echo "4. Browser Slowdown (for debugging)"
    echo "   Current: ${SLOWMO}ms delay per action"
    echo ""
    echo "5. Viewport Size"
    echo "   Current: ${VIEWPORT_WIDTH}x${VIEWPORT_HEIGHT}"
    echo ""
    echo "═══════════════════════════════════════════════════════════"
    echo ""
    echo "Enter option number to toggle (1-5), or press Enter to start test:"
    read -r option
    
    case "$option" in
        1)
            if [ "$HEADLESS_MODE" = "true" ]; then
                HEADLESS_MODE="false"
                echo "✓ Browser visibility: VISIBLE"
            else
                HEADLESS_MODE="true"
                echo "✓ Browser visibility: HEADLESS"
            fi
            show_options_menu
            ;;
        2)
            if [ "$SHOW_CONSOLE" = "true" ]; then
                SHOW_CONSOLE="false"
                echo "✓ Console output: SUPPRESSED"
            else
                SHOW_CONSOLE="true"
                echo "✓ Console output: ENABLED"
            fi
            show_options_menu
            ;;
        3)
            if [ "$SKIP_SCREENSHOTS" = "true" ]; then
                SKIP_SCREENSHOTS="false"
                echo "✓ Screenshot capture: ENABLED"
            else
                SKIP_SCREENSHOTS="true"
                echo "✓ Screenshot capture: DISABLED"
            fi
            show_options_menu
            ;;
        4)
            echo "Enter slowdown delay in milliseconds (0-5000, default 0):"
            read -r slowmo_input
            if [[ "$slowmo_input" =~ ^[0-9]+$ ]] && [ "$slowmo_input" -ge 0 ] && [ "$slowmo_input" -le 5000 ]; then
                SLOWMO="$slowmo_input"
                echo "✓ Browser slowdown: ${SLOWMO}ms"
            else
                echo "⚠️ Invalid input, keeping current value: ${SLOWMO}ms"
            fi
            show_options_menu
            ;;
        5)
            echo "Enter viewport width (800-3840, default 1920):"
            read -r width_input
            echo "Enter viewport height (600-2160, default 1080):"
            read -r height_input
            if [[ "$width_input" =~ ^[0-9]+$ ]] && [ "$width_input" -ge 800 ] && [ "$width_input" -le 3840 ]; then
                VIEWPORT_WIDTH="$width_input"
            fi
            if [[ "$height_input" =~ ^[0-9]+$ ]] && [ "$height_input" -ge 600 ] && [ "$height_input" -le 2160 ]; then
                VIEWPORT_HEIGHT="$height_input"
            fi
            echo "✓ Viewport size: ${VIEWPORT_WIDTH}x${VIEWPORT_HEIGHT}"
            show_options_menu
            ;;
        "")
            echo "▶️ Starting test with current configuration..."
            ;;
        *)
            echo "⚠️ Invalid option"
            show_options_menu
            ;;
    esac
}

# Generic function to run a test
run_test() {
    local test_class="$1"
    local schema_arg=()
    local reload_arg=()
    local spring_profiles="${SPRING_PROFILES_ACTIVE:-test}"
    if [ -n "$PLAYWRIGHT_SCHEMA" ]; then
        schema_arg=("-Dplaywright.schema=$PLAYWRIGHT_SCHEMA")
    fi
    if [ -n "$PLAYWRIGHT_FORCE_SAMPLE_RELOAD" ]; then
        reload_arg=("-Dplaywright.forceSampleReload=$PLAYWRIGHT_FORCE_SAMPLE_RELOAD")
    fi
    
    # Create screenshots directory
    mkdir -p target/screenshots
    
    # Install Playwright browsers if needed
    install_playwright_browsers
    
    # Set Playwright environment variables to use cached browser
    export PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright"
    export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true
    export PLAYWRIGHT_SKIP_SCREENSHOTS="$SKIP_SCREENSHOTS"
    export PLAYWRIGHT_SLOWMO="$SLOWMO"
    export PLAYWRIGHT_VIEWPORT_WIDTH="$VIEWPORT_WIDTH"
    export PLAYWRIGHT_VIEWPORT_HEIGHT="$VIEWPORT_HEIGHT"
    
    # Run the test with Playwright-specific profile
    echo ""
    echo "🎯 Test Configuration:"
    echo "   Browser mode: $([ "$HEADLESS_MODE" = "true" ] && echo "HEADLESS" || echo "VISIBLE")"
    echo "   Console output: $([ "$SHOW_CONSOLE" = "true" ] && echo "ENABLED" || echo "SUPPRESSED")"
    echo "   Screenshots: $([ "$SKIP_SCREENSHOTS" = "true" ] && echo "DISABLED" || echo "ENABLED")"
    echo "   Slowdown: ${SLOWMO}ms"
    echo "   Viewport: ${VIEWPORT_WIDTH}x${VIEWPORT_HEIGHT}"
    echo ""
    
    local test_result=0
    if [ "$SHOW_CONSOLE" = "true" ]; then
        mvn test -Dtest="$test_class" \
            -Dspring.profiles.active="$spring_profiles" \
            "${schema_arg[@]}" \
            "${reload_arg[@]}" \
            -Dplaywright.headless=$HEADLESS_MODE \
            -Dplaywright.slowmo=$SLOWMO \
            -Dplaywright.viewport.width=$VIEWPORT_WIDTH \
            -Dplaywright.viewport.height=$VIEWPORT_HEIGHT || test_result=$?
    else
        mvn test -Dtest="$test_class" \
            -Dspring.profiles.active="$spring_profiles" \
            "${schema_arg[@]}" \
            "${reload_arg[@]}" \
            -Dplaywright.headless=$HEADLESS_MODE \
            -Dplaywright.slowmo=$SLOWMO \
            -Dplaywright.viewport.width=$VIEWPORT_WIDTH \
            -Dplaywright.viewport.height=$VIEWPORT_HEIGHT > /dev/null 2>&1 || test_result=$?
    fi
    
    if [ $test_result -eq 0 ]; then
        echo "✅ Test completed successfully!"
        
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
        echo "❌ Test failed!"
        
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

Run Playwright UI automation tests for the Derbent application.

OPTIONS:
    (no args)       Run the menu navigation test (default)
    menu            Run the menu navigation test
    bab             Run the BAB Gateway menu navigation test
    comprehensive   Run comprehensive page tests (all views + CRUD operations)
    all-views       Navigate through all application views and capture screenshots
    crud            Test CRUD operations on all pages with toolbars
    recent-features Test recent features (Issues, Teams, Attachments, Comments)
    workflow-validation  Test workflow status combobox and name field validation
    clean           Clean test artifacts (screenshots, reports)
    install         Install Playwright browsers
    help            Show this help message

ENVIRONMENT VARIABLES:
    PLAYWRIGHT_HEADLESS          Set to 'true' for headless mode, 'false' for visible browser (default: false)
    PLAYWRIGHT_SHOW_CONSOLE      Set to 'true' to show console output, 'false' to suppress (default: true)
    PLAYWRIGHT_SKIP_SCREENSHOTS  Set to 'true' to disable screenshot capture (default: false)
    PLAYWRIGHT_SLOWMO            Delay in milliseconds between actions for debugging (default: 0)
    PLAYWRIGHT_VIEWPORT_WIDTH    Browser viewport width in pixels (default: 1920)
    PLAYWRIGHT_VIEWPORT_HEIGHT   Browser viewport height in pixels (default: 1080)
    PLAYWRIGHT_SCHEMA            Set schema selection at login (default: Derbent)
    PLAYWRIGHT_FORCE_SAMPLE_RELOAD  Set to 'true' to force DB reset on login (default: false)
    SPRING_PROFILES_ACTIVE       Spring profiles to activate for tests (default: test)
    INTERACTIVE_MODE             Set to 'true' to show configuration menu before test (default: false)

EXAMPLES:
    # Run with interactive configuration menu
    INTERACTIVE_MODE=true ./run-playwright-tests.sh menu
    
    # Run quick menu navigation test (default, ~37 seconds)
    ./run-playwright-tests.sh

    # Run BAB Gateway menu navigation test
    ./run-playwright-tests.sh bab
    
    # Run comprehensive test covering all views and CRUD operations (~2-5 minutes)
    ./run-playwright-tests.sh comprehensive
    
    # Run in headless mode without screenshots (fast)
    PLAYWRIGHT_HEADLESS=true PLAYWRIGHT_SKIP_SCREENSHOTS=true ./run-playwright-tests.sh menu
    
    # Run with visible browser and slow motion for debugging
    PLAYWRIGHT_SLOWMO=500 ./run-playwright-tests.sh menu
    
    # Run with custom viewport size
    PLAYWRIGHT_VIEWPORT_WIDTH=1280 PLAYWRIGHT_VIEWPORT_HEIGHT=720 ./run-playwright-tests.sh menu
    
    # Run quiet mode (headless, no console, no screenshots)
    PLAYWRIGHT_HEADLESS=true PLAYWRIGHT_SHOW_CONSOLE=false PLAYWRIGHT_SKIP_SCREENSHOTS=true ./run-playwright-tests.sh comprehensive
    
    # Run comprehensive test with interactive options
    INTERACTIVE_MODE=true ./run-playwright-tests.sh comprehensive

TEST DESCRIPTIONS:
    menu            Fast menu navigation test (under 1 minute)
                    - Logs into the application
                    - Browses all hierarchical menu items
                    - Captures screenshots for each menu item (if enabled)

    bab             BAB Gateway menu navigation test (under 1 minute)
                    - Selects BAB Gateway schema
                    - Resets database with minimal BAB data
                    - Logs into the application
                    - Browses available menu items
    
    comprehensive   Complete page testing with CRUD operations (2-5 minutes)
                    - Tests all pages accessible via test auxiliary buttons
                    - Detects grids and runs grid tests (sorting, selection)
                    - Detects CRUD toolbars and runs CRUD tests
                    - Tests New, Edit, Delete button functionality
                    - Captures detailed screenshots at each step (if enabled)
    
    all-views       Navigate all application views
                    - Same as comprehensive but focuses on navigation
                    - Verifies all pages load correctly
                    - Captures screenshots of each view (if enabled)
    
    crud            CRUD operation testing
                    - Tests Create, Read, Update, Delete operations
                    - Tests toolbar buttons on pages with CRUD functionality
                    - Verifies form dialogs open and close correctly
                    - Captures screenshots of each operation (if enabled)
    
    recent-features Test recent features from last 3 days (2-3 minutes)
                    - Tests Issues & Bug Tracking CRUD operations
                    - Tests Teams Management CRUD operations
                    - Tests Attachments upload/download/delete
                    - Tests Comments add/edit/delete functionality
                    - Verifies all recent commits features working
                    - Captures detailed screenshots at each step (if enabled)
    
    workflow-validation  Workflow status and name validation testing (1-2 minutes)
                    - Tests status combobox appears for workflow entities
                    - Verifies status combobox shows valid workflow transitions
                    - Tests save button disabled when name field is empty
                    - Tests save button enabled when name has content
                    - Tests name validation on multiple entity types
                    - Captures screenshots showing validation behavior (if enabled)

INTERACTIVE MODE:
    Set INTERACTIVE_MODE=true to configure test options before running:
    - Toggle browser visibility (headless vs visible)
    - Toggle console output
    - Enable/disable screenshot capture
    - Set slowdown delay for debugging (slows down browser actions)
    - Configure viewport size

SCREENSHOTS:
    Screenshots save to: target/screenshots/ (when enabled)
    Screenshot filenames include sequence numbers and descriptive names
    Use PLAYWRIGHT_SKIP_SCREENSHOTS=true to disable for faster execution
    
OUTPUT:
    Tests provide detailed logging including:
    - Pages visited
    - Grids found and tested
    - CRUD toolbars detected and tested
    - Screenshot counts and locations
    - Test execution summary

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
    bab)
        run_bab_menu_test
        ;;
    comprehensive)
        run_comprehensive_test
        ;;
    all-views)
        run_all_views_test
        ;;
    crud)
        run_crud_test
        ;;
    recent-features)
        run_recent_features_test
        ;;
    workflow-validation)
        run_workflow_validation_test
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
