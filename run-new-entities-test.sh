#!/bin/bash
# Run tests ONLY for newly added entities (Financial, Test Management, Team/Issue)

set -e

source ./bin/setup-java-env.sh

echo "🎯 ========================================"
echo "🎯 TESTING NEW ENTITIES ONLY"
echo "🎯 ========================================"
echo ""
echo "📋 Test Scope:"
echo "   🏦 Financial Entities (7): Budgets, Invoices, Payments, Orders, etc."
echo "   🧪 Test Management (5): Test Cases, Scenarios, Runs, Steps, Results"
echo "   👥 Team/Issue (3): Issues, Issue Types, Teams"
echo ""
echo "⚙️  Configuration:"
echo "   Browser: ${PLAYWRIGHT_HEADLESS:-false} (visible)"
echo "   Screenshots: ${PLAYWRIGHT_SKIP_SCREENSHOTS:-false}"
echo "   Timeout: 15 minutes"
echo ""

# Set environment variables
export PLAYWRIGHT_HEADLESS="${PLAYWRIGHT_HEADLESS:-false}"
export PLAYWRIGHT_SHOW_CONSOLE="${PLAYWRIGHT_SHOW_CONSOLE:-true}"
export PLAYWRIGHT_SKIP_SCREENSHOTS="${PLAYWRIGHT_SKIP_SCREENSHOTS:-false}"

# Create log directory
mkdir -p target/test-logs target/screenshots

LOG_FILE="target/test-logs/new-entities-test-$(date +%Y%m%d-%H%M%S).log"

echo "📝 Logs will be saved to: $LOG_FILE"
echo ""
echo "🚀 Starting tests..."
echo ""

# Run ONLY the new entities test class
mvn test \
  -Dtest=CPageTestNewEntities \
  -Dspring.profiles.active=h2 \
  -Dspring.devtools.restart.enabled=false \
  2>&1 | tee "$LOG_FILE"

TEST_RESULT=${PIPESTATUS[0]}

echo ""
echo "========================================"
if [ $TEST_RESULT -eq 0 ]; then
  echo "✅ NEW ENTITIES TEST COMPLETED SUCCESSFULLY"
else
  echo "❌ NEW ENTITIES TEST FAILED"
fi
echo "========================================"
echo ""
echo "📋 Log file: $LOG_FILE"
echo "📸 Screenshots: target/screenshots/"
echo ""

exit $TEST_RESULT
