#!/bin/bash

# Script to monitor test execution and fail-fast on exceptions
# Usage: ./scripts/check-test-exceptions.sh <test-command>

set -e

TEMP_LOG="/tmp/derbent-test-exceptions.log"
EXCEPTION_PATTERNS="ERROR.*Exception|CRITICAL|FATAL|BindingException|RuntimeException"

echo "🚨 Starting exception monitoring for fail-fast testing..."
echo "📝 Monitoring patterns: $EXCEPTION_PATTERNS"
echo "📋 Test log will be saved to: $TEMP_LOG"
echo ""

# Run the test command and capture output
echo "🚀 Starting test execution..."
if [ $# -eq 0 ]; then
    echo "Usage: $0 <test-command>"
    echo "Example: $0 './run-playwright-tests.sh menu'"
    exit 1
fi

# Execute the test command and pipe output to both console and log file
"$@" 2>&1 | tee "$TEMP_LOG" &
TEST_PID=$!

# Monitor the log file for exceptions in real-time
tail -F "$TEMP_LOG" 2>/dev/null | while IFS= read -r line; do
    if echo "$line" | grep -qE "$EXCEPTION_PATTERNS"; then
        echo ""
        echo "🚨 ========================================"
        echo "❌ FAIL-FAST: EXCEPTION DETECTED!"
        echo "🚨 ========================================"
        echo "📍 Exception found: $line"
        echo ""
        echo "🔍 Extracting all exceptions from log..."
        echo ""
        
        # Show all exceptions found so far
        grep -E "$EXCEPTION_PATTERNS" "$TEMP_LOG" | head -10 | while IFS= read -r exception_line; do
            echo "  ❌ $exception_line"
        done
        
        echo ""
        echo "🛑 STOPPING TEST EXECUTION (FAIL-FAST)"
        
        # Kill the test process
        kill $TEST_PID 2>/dev/null || true
        pkill -P $TEST_PID 2>/dev/null || true
        
        # Wait a moment for cleanup
        sleep 2
        
        echo ""
        echo "📋 Full exception report saved to: $TEMP_LOG"
        echo "🔧 Fix the exception above and re-run the test."
        echo ""
        
        exit 1
    fi
done &

MONITOR_PID=$!

# Wait for test to complete or be killed
wait $TEST_PID 2>/dev/null
TEST_EXIT_CODE=$?

# Kill the monitor process
kill $MONITOR_PID 2>/dev/null || true

if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo ""
    echo "✅ Test completed successfully without exceptions!"
    echo "📋 Full log available at: $TEMP_LOG"
else
    echo ""
    echo "❌ Test failed with exit code: $TEST_EXIT_CODE"
    echo "📋 Check log for details: $TEMP_LOG"
fi

exit $TEST_EXIT_CODE