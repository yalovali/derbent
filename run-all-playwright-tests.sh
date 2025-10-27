#!/bin/bash

# Comprehensive Playwright Test Runner for Derbent Application
# This script runs ALL Playwright tests and generates screenshots for each test function

set -e

echo "🚀 Comprehensive Playwright Test Runner"
echo "========================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test tracking
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
SKIPPED_TESTS=0

# Create screenshots directory
mkdir -p target/screenshots
mkdir -p target/test-reports

# Set Playwright environment variables
export PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright"
export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true

# Function to run a single test
run_test() {
    local test_class=$1
    local test_name=$2
    local test_description=$3
    
    echo ""
    echo -e "${BLUE}▶️  Running: ${test_description}${NC}"
    echo "   Test Class: ${test_class}"
    echo "   Test Method: ${test_name}"
    echo "   ----------------------------------------"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Run the test
    if mvn test -Dtest="${test_class}#${test_name}" -Dspring.profiles.active=test -Dplaywright.headless=true > "target/test-reports/${test_class}_${test_name}.log" 2>&1; then
        echo -e "   ${GREEN}✅ PASSED${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        
        # Count screenshots generated
        screenshot_count=$(find target/screenshots -name "*.png" -newer "target/test-reports/${test_class}_${test_name}.log" 2>/dev/null | wc -l)
        if [[ $screenshot_count -gt 0 ]]; then
            echo "   📸 Generated ${screenshot_count} new screenshots"
        fi
    else
        echo -e "   ${RED}❌ FAILED${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        
        # Show last few lines of error log
        echo "   Error details (last 10 lines):"
        tail -10 "target/test-reports/${test_class}_${test_name}.log" | sed 's/^/   /'
    fi
}

# Function to run all tests in a class (when no specific method is needed)
run_test_class() {
    local test_class=$1
    local test_description=$2
    
    echo ""
    echo -e "${BLUE}▶️  Running: ${test_description}${NC}"
    echo "   Test Class: ${test_class}"
    echo "   ----------------------------------------"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Run the test
    if mvn test -Dtest="${test_class}" -Dspring.profiles.active=test -Dplaywright.headless=true > "target/test-reports/${test_class}.log" 2>&1; then
        echo -e "   ${GREEN}✅ PASSED${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        
        # Count screenshots generated
        screenshot_count=$(find target/screenshots -name "*.png" -newer "target/test-reports/${test_class}.log" 2>/dev/null | wc -l)
        if [[ $screenshot_count -gt 0 ]]; then
            echo "   📸 Generated ${screenshot_count} new screenshots"
        fi
    else
        echo -e "   ${RED}❌ FAILED${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        
        # Show last few lines of error log
        echo "   Error details (last 10 lines):"
        tail -10 "target/test-reports/${test_class}.log" | sed 's/^/   /'
    fi
}

# Create timestamp marker file for tracking new screenshots
touch target/test-reports/test-run-start.marker

echo ""
echo "==================================================================="
echo "               RUNNING ALL PLAYWRIGHT TESTS                        "
echo "==================================================================="

# 1. CSimpleLoginTest
echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}TEST SUITE 1: Simple Login Tests${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
run_test "automated_tests.tech.derbent.ui.automation.CSimpleLoginTest" \
         "loginWithDefaultCredentials" \
         "Simple Login - Default Credentials Test"

# 2. CSimpleLoginScreenshotTest
echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}TEST SUITE 2: Login Screenshot Tests${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
run_test "automated_tests.tech.derbent.ui.automation.CSimpleLoginScreenshotTest" \
         "loginAndCaptureScreenshots" \
         "Login Screenshot Capture Test"

# 3. CSampleDataMenuNavigationTest
echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}TEST SUITE 3: Sample Data & Menu Navigation Tests${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
run_test "automated_tests.tech.derbent.ui.automation.CSampleDataMenuNavigationTest" \
         "sampleDataLoginAndMenuScreenshots" \
         "Sample Data Menu Navigation Test"

# 4. CCompanyAwareLoginTest
echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}TEST SUITE 4: Company-Aware Login Tests${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
run_test "automated_tests.tech.derbent.ui.automation.CCompanyAwareLoginTest" \
         "testCompanyAwareLoginFlow" \
         "Company-Aware Login Flow Test"

run_test "automated_tests.tech.derbent.ui.automation.CCompanyAwareLoginTest" \
         "testMultipleCompanyLogin" \
         "Multiple Company Login Test"

run_test "automated_tests.tech.derbent.ui.automation.CCompanyAwareLoginTest" \
         "testUsernameFormatValidation" \
         "Username Format Validation Test"

# 5. CComprehensiveDynamicViewsTest
echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}TEST SUITE 5: Comprehensive Dynamic Views Tests${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
run_test "automated_tests.tech.derbent.ui.automation.CComprehensiveDynamicViewsTest" \
         "testCompleteNavigationAndDynamicViews" \
         "Complete Navigation & Dynamic Views Test"

run_test "automated_tests.tech.derbent.ui.automation.CComprehensiveDynamicViewsTest" \
         "testGridFunctionality" \
         "Grid Functionality Test"

run_test "automated_tests.tech.derbent.ui.automation.CComprehensiveDynamicViewsTest" \
         "testFormValidation" \
         "Form Validation Test"

# 6. CTypeStatusCrudTest
echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}TEST SUITE 6: Type & Status CRUD Tests${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
run_test "automated_tests.tech.derbent.ui.automation.CTypeStatusCrudTest" \
         "testActivityTypeCrudOperations" \
         "Activity Type CRUD Operations Test"

run_test "automated_tests.tech.derbent.ui.automation.CTypeStatusCrudTest" \
         "testActivityStatusCrudOperations" \
         "Activity Status CRUD Operations Test"

run_test "automated_tests.tech.derbent.ui.automation.CTypeStatusCrudTest" \
         "testMeetingTypeCrudOperations" \
         "Meeting Type CRUD Operations Test"

run_test "automated_tests.tech.derbent.ui.automation.CTypeStatusCrudTest" \
         "testDecisionTypeCrudOperations" \
         "Decision Type CRUD Operations Test"

run_test "automated_tests.tech.derbent.ui.automation.CTypeStatusCrudTest" \
         "testOrderTypeCrudOperations" \
         "Order Type CRUD Operations Test"

run_test "automated_tests.tech.derbent.ui.automation.CTypeStatusCrudTest" \
         "testApprovalStatusCrudOperations" \
         "Approval Status CRUD Operations Test"

# 7. CWorkflowStatusCrudTest
echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}TEST SUITE 7: Workflow Status CRUD Tests${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
run_test "automated_tests.tech.derbent.ui.automation.CWorkflowStatusCrudTest" \
         "testWorkflowStatusCrudOperations" \
         "Workflow Status CRUD Operations Test"

# 8. CDependencyCheckingTest
echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}TEST SUITE 8: Dependency Checking Tests${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
run_test "automated_tests.tech.derbent.ui.automation.CDependencyCheckingTest" \
         "testProjectItemStatusInUseCannotBeDeleted" \
         "Project Item Status Delete Prevention Test"

run_test "automated_tests.tech.derbent.ui.automation.CDependencyCheckingTest" \
         "testActivityTypeInUseCannotBeDeleted" \
         "Activity Type Delete Prevention Test"

run_test "automated_tests.tech.derbent.ui.automation.CDependencyCheckingTest" \
         "testLastUserCannotBeDeleted" \
         "Last User Delete Prevention Test"

# 9. CDialogRefreshTest
echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}TEST SUITE 9: Dialog Refresh Tests${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
run_test "automated_tests.tech.derbent.ui.automation.CDialogRefreshTest" \
         "testWorkflowStatusRelationDialogRefresh" \
         "Workflow Status Relation Dialog Refresh Test"

# 10. CButtonFunctionalityTest
echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}TEST SUITE 10: Button Functionality Tests${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
run_test "automated_tests.tech.derbent.ui.automation.CButtonFunctionalityTest" \
         "testButtonFunctionalityAcrossAllPages" \
         "Button Functionality Across All Pages Test"

run_test "automated_tests.tech.derbent.ui.automation.CButtonFunctionalityTest" \
         "testButtonResponsiveness" \
         "Button Responsiveness Test"

# Summary
echo ""
echo ""
echo "==================================================================="
echo "                    TEST EXECUTION SUMMARY                         "
echo "==================================================================="
echo ""
echo -e "Total Tests:   ${BLUE}${TOTAL_TESTS}${NC}"
echo -e "Passed:        ${GREEN}${PASSED_TESTS}${NC}"
echo -e "Failed:        ${RED}${FAILED_TESTS}${NC}"
echo -e "Skipped:       ${YELLOW}${SKIPPED_TESTS}${NC}"
echo ""

# Screenshot summary
screenshot_count=$(find target/screenshots -name "*.png" 2>/dev/null | wc -l)
echo -e "📸 Total Screenshots Generated: ${BLUE}${screenshot_count}${NC}"
echo ""

if [[ $screenshot_count -gt 0 ]]; then
    echo "Screenshots saved in: target/screenshots/"
    echo ""
    echo "Screenshot listing:"
    find target/screenshots -name "*.png" -type f -printf "  - %f\n" | sort
    echo ""
fi

# Test logs summary
echo "Test logs saved in: target/test-reports/"
echo ""

# Exit status
if [[ $FAILED_TESTS -eq 0 ]]; then
    echo -e "${GREEN}✅ All tests completed successfully!${NC}"
    exit 0
else
    echo -e "${RED}❌ ${FAILED_TESTS} test(s) failed${NC}"
    echo ""
    echo "Check test logs in target/test-reports/ for details"
    exit 1
fi
