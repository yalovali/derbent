#!/bin/bash
# Selective Test Runner
# Runs Playwright tests by keyword or button ID

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

echo "🧪 Derbent Selective Test Runner"
echo "================================="
echo ""

play_sound() {
    local kind="${1:-success}"
    if [ "${DERBENT_SOUND_ENABLED:-true}" != "true" ]; then
        return 0
    fi

    if command -v paplay >/dev/null 2>&1; then
        if [ "$kind" = "start" ] && [ -f /usr/share/sounds/freedesktop/stereo/service-login.oga ]; then
            paplay /usr/share/sounds/freedesktop/stereo/service-login.oga >/dev/null 2>&1 || true
            return 0
        fi
        if [ "$kind" = "all-done" ] && [ -f /usr/share/sounds/freedesktop/stereo/alarm-clock-elapsed.oga ]; then
            paplay /usr/share/sounds/freedesktop/stereo/alarm-clock-elapsed.oga >/dev/null 2>&1 || true
            return 0
        fi
        if [ "$kind" = "success" ] && [ -f /usr/share/sounds/freedesktop/stereo/complete.oga ]; then
            paplay /usr/share/sounds/freedesktop/stereo/complete.oga >/dev/null 2>&1 || true
            return 0
        fi
        if [ "$kind" != "success" ] && [ -f /usr/share/sounds/freedesktop/stereo/dialog-error.oga ]; then
            paplay /usr/share/sounds/freedesktop/stereo/dialog-error.oga >/dev/null 2>&1 || true
            return 0
        fi
    fi

    if [ "$kind" = "start" ]; then
        printf '\a'
    elif [ "$kind" = "success" ]; then
        printf '\a\a'
    elif [ "$kind" = "all-done" ]; then
        printf '\a\a\a\a\a'
    else
        printf '\a\a\a'
    fi
}


# Function to run test by keyword
run_by_keyword() {
    local keyword="$1"
    
    echo "Running tests for keyword: $keyword"
    echo ""

    play_sound start

    cd "$PROJECT_ROOT"
    
    ./mvnw -Pagents test -Dtest=CPageComprehensiveTest \
        -Dtest.routeKeyword="$keyword" \
        2>&1 | tee "/tmp/playwright-$keyword.log"
    
    result=$?
    
    echo ""
    if [ $result -eq 0 ]; then
        echo "✅ Tests PASSED for keyword: $keyword"
        play_sound all-done
    else
        echo "❌ Tests FAILED for keyword: $keyword"
        echo "   Log: /tmp/playwright-$keyword.log"
        play_sound error
    fi
    
    return $result
}

# Function to run test by button ID
run_by_button() {
    local button_id="$1"
    
    echo "Running test for button ID: $button_id"
    echo ""

    play_sound start

    cd "$PROJECT_ROOT"
    
    ./mvnw -Pagents test -Dtest=CPageComprehensiveTest \
        -Dtest.targetButtonId="$button_id" \
        2>&1 | tee "/tmp/playwright-button.log"
    
    result=$?
    
    echo ""
    if [ $result -eq 0 ]; then
        echo "✅ Test PASSED for button: $button_id"
        play_sound all-done
    else
        echo "❌ Test FAILED for button: $button_id"
        echo "   Log: /tmp/playwright-button.log"
        play_sound error
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
