#!/bin/bash
# Comprehensive Page Testing Framework - Unified Test Runner
# Uses CPageTestComprehensive for ALL page testing

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/bin/setup-java-env.sh"

echo "🚀 Comprehensive Page Testing Framework"
echo "========================================"

# Configuration
HEADLESS="${PLAYWRIGHT_HEADLESS:-false}"
SLOWMO="${PLAYWRIGHT_SLOWMO:-0}"
TEST_CLASS="CPageTestComprehensive"

# Test execution function
run_test() {
    local extra_args="$1"
    mkdir -p target/screenshots test-results/playwright/coverage target/test-logs
    
    mvn test \
        -Dspring.profiles.active="${SPRING_PROFILES_ACTIVE:-test,derbent}" \
        -Dplaywright.headless=$HEADLESS \
        -Dplaywright.slowmo=$SLOWMO \
        $extra_args 2>&1 | tee target/test-logs/playwright-test-latest.log
    
    local result=${PIPESTATUS[0]}
    [[ $result -eq 0 ]] && echo "✅ Test PASSED" || echo "❌ Test FAILED"
    [[ -d test-results/playwright/coverage ]] && ls -lh test-results/playwright/coverage/*.{csv,md} 2>/dev/null || true
    return $result
}

# Command routing
case "${1:-menu}" in
    menu|"") run_test "-Dtest=automated_tests.tech.derbent.ui.automation.tests.common.CTestMenuNavigation_common" ;;
    bab) export PLAYWRIGHT_SCHEMA="BAB Gateway"; export SPRING_PROFILES_ACTIVE="test,bab"
         run_test "-Dtest=automated_tests.tech.derbent.ui.automation.tests.bab.CTestMenuNavigation_bab" ;;
    comprehensive) run_test "-Dtest=automated_tests.tech.derbent.ui.automation.tests.common.CTestPageComprehensive_common" ;;
    activity|user|storage|meeting|configure|device) run_test "-Dtest=automated_tests.tech.derbent.ui.automation.tests.common.CTestPageComprehensive_common -Dtest.routeKeyword=$1" ;;
    clean) rm -rf target/screenshots/*.png target/test-results test-results/playwright/coverage target/test-logs
           echo "✅ Cleaned" ;;
    install) mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install" ;;
    help|--help|-h) cat << 'EOFHELP'

Comprehensive Page Testing Framework
=====================================

USAGE: ./run-playwright-tests.sh [COMMAND]

COMMANDS:
    menu            Quick menu navigation test
    bab             BAB Gateway menu test
    comprehensive   Test ALL pages (full coverage)
    activity        Test activity pages
    user            Test user pages
    storage         Test storage pages  
    meeting         Test meeting pages
    configure       Test BAB configuration pages
    device          Test BAB device pages
    clean           Remove test artifacts
    install         Install Playwright browsers
    help            Show this help

ENVIRONMENT:
    PLAYWRIGHT_HEADLESS=false       Show browser (default)
    PLAYWRIGHT_HEADLESS=true        Run headless
    PLAYWRIGHT_SLOWMO=500           Slow motion (ms)
    
EXAMPLES:
    # Watch test in browser
    PLAYWRIGHT_HEADLESS=false ./run-playwright-tests.sh activity
    
    # Fast headless test
    PLAYWRIGHT_HEADLESS=true ./run-playwright-tests.sh user
    
    # Debug with slow motion
    PLAYWRIGHT_SLOWMO=1000 ./run-playwright-tests.sh meeting

DIRECT MAVEN:
    # Exact button text match (recommended)
    mvn test -Dtest=CPageTestComprehensive -Dtest.targetButtonText="BAB System Management"
    
    # Partial keyword filter
    mvn test -Dtest=CPageTestComprehensive -Dtest.routeKeyword=dashboard
    
    # Specific button ID (legacy)
    mvn test -Dtest=CPageTestComprehensive -Dtest.targetButtonId=test-aux-btn-devices-3
    
    # All pages
    mvn test -Dtest=CPageTestComprehensive

OUTPUT:
    Logs:     target/test-logs/playwright-test-latest.log
    Coverage: test-results/playwright/coverage/*.{csv,md}
    Screenshots: target/screenshots/*.png

DOCUMENTATION:
    docs/testing/COMPREHENSIVE_PAGE_TESTING.md

EOFHELP
        ;;
    *) echo "❌ Unknown command: $1"; echo "Run './run-playwright-tests.sh help' for usage"; exit 1 ;;
esac
