#!/bin/bash
# Comprehensive Playwright Test with Real-time Logging
# This script runs ALL entities with browser VISIBLE and logs to shared file

set -e

source ./bin/setup-java-env.sh

SHARED_LOG_FILE="target/test-logs/comprehensive-live-$(date +%Y%m%d-%H%M%S).log"
mkdir -p target/test-logs target/screenshots

echo "🎯 ========================================" | tee "$SHARED_LOG_FILE"
echo "🎯 COMPREHENSIVE PLAYWRIGHT TEST" | tee -a "$SHARED_LOG_FILE"
echo "🎯 Browser: VISIBLE (always)" | tee -a "$SHARED_LOG_FILE"
echo "🎯 Fail-Fast: ON (stop on exceptions)" | tee -a "$SHARED_LOG_FILE"
echo "🎯 ========================================" | tee -a "$SHARED_LOG_FILE"
echo "" | tee -a "$SHARED_LOG_FILE"
echo "📋 Test Coverage:" | tee -a "$SHARED_LOG_FILE"
echo "   ✅ All 65+ entities (walk through all pages)" | tee -a "$SHARED_LOG_FILE"
echo "   ✅ CRUD operations (Create, Read, Update, Delete)" | tee -a "$SHARED_LOG_FILE"
echo "   ✅ Attachments sections (upload, download, delete)" | tee -a "$SHARED_LOG_FILE"
echo "   ✅ Comments sections (add, edit, delete)" | tee -a "$SHARED_LOG_FILE"
echo "   ✅ Status workflows (transitions)" | tee -a "$SHARED_LOG_FILE"
echo "   ⚠️  Special focus on NEW entities:" | tee -a "$SHARED_LOG_FILE"
echo "      - Budget, Issue, Team, Finance, Test Execution" | tee -a "$SHARED_LOG_FILE"
echo "" | tee -a "$SHARED_LOG_FILE"
echo "📝 Live log file: $SHARED_LOG_FILE" | tee -a "$SHARED_LOG_FILE"
echo "📺 Monitor in another terminal:" | tee -a "$SHARED_LOG_FILE"
echo "   tail -f $SHARED_LOG_FILE" | tee -a "$SHARED_LOG_FILE"
echo "" | tee -a "$SHARED_LOG_FILE"
echo "🚀 Starting comprehensive test..." | tee -a "$SHARED_LOG_FILE"
echo "" | tee -a "$SHARED_LOG_FILE"

# Run with visible browser and full console output
PLAYWRIGHT_HEADLESS=false \
PLAYWRIGHT_SHOW_CONSOLE=true \
./run-playwright-tests.sh comprehensive 2>&1 | tee -a "$SHARED_LOG_FILE"

TEST_RESULT=${PIPESTATUS[0]}

echo "" | tee -a "$SHARED_LOG_FILE"
echo "========================================" | tee -a "$SHARED_LOG_FILE"
if [ $TEST_RESULT -eq 0 ]; then
  echo "✅ COMPREHENSIVE TEST COMPLETED" | tee -a "$SHARED_LOG_FILE"
else
  echo "❌ TEST FAILED - Check logs above" | tee -a "$SHARED_LOG_FILE"
fi
echo "========================================" | tee -a "$SHARED_LOG_FILE"
echo "" | tee -a "$SHARED_LOG_FILE"
echo "📋 Full log: $SHARED_LOG_FILE" | tee -a "$SHARED_LOG_FILE"
echo "📸 Screenshots: target/screenshots/" | tee -a "$SHARED_LOG_FILE"
echo "" | tee -a "$SHARED_LOG_FILE"

exit $TEST_RESULT
