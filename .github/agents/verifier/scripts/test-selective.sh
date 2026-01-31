#!/bin/bash
# Selective Test Runner
# Runs Playwright tests by keyword or button ID

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

echo "🧪 Derbent Selective Test Runner"
echo "================================="
echo ""

# Function to run test by keyword
run_by_keyword() {
    local keyword="$1"
    
    echo "Running tests for keyword: $keyword"
    echo ""
    
    cd "$PROJECT_ROOT"
    
    mvn test -Dtest=CPageTestComprehensive \
        -Dtest.routeKeyword="$keyword" \
        2>&1 | tee "/tmp/playwright-$keyword.log"
    
    result=$?
    
    echo ""
    if [ $result -eq 0 ]; then
        echo "✅ Tests PASSED for keyword: $keyword"
    else
        echo "❌ Tests FAILED for keyword: $keyword"
        echo "   Log: /tmp/playwright-$keyword.log"
    fi
    
    return $result
}

# Function to run test by button ID
run_by_button() {
    local button_id="$1"
    
    echo "Running test for button ID: $button_id"
    echo ""
    
    cd "$PROJECT_ROOT"
    
    mvn test -Dtest=CPageTestComprehensive \
        -Dtest.targetButtonId="$button_id" \
        2>&1 | tee "/tmp/playwright-button.log"
    
    result=$?
    
    echo ""
    if [ $result -eq 0 ]; then
        echo "✅ Test PASSED for button: $button_id"
    else
        echo "❌ Test FAILED for button: $button_id"
        echo "   Log: /tmp/playwright-button.log"
    fi
    
    return $result
}

# Show available keywords
show_keywords() {
    echo "Available test keywords:"
    echo ""
    echo "  activity    - Activities, Activity Types, Activity Priorities"
    echo "  storage     - Storages, Storage Types, Storage Items"
    echo "  meeting     - Meetings, Meeting Types"
    echo "  user        - Users, User Roles, User Project Roles"
    echo "  issue       - Issues, Issue Types, Issue Priorities"
    echo "  product     - Products, Product Types, Product Categories"
    echo "  customer    - Customers, Customer Types"
    echo "  provider    - Providers, Provider Types"
    echo ""
}

# Main
if [ $# -eq 0 ]; then
    show_keywords
    read -p "Enter keyword or button ID: " input
else
    input="$1"
fi

# Detect if input is button ID or keyword
if [[ "$input" == test-aux-btn-* ]]; then
    run_by_button "$input"
else
    run_by_keyword "$input"
fi
