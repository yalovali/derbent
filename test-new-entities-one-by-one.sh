#!/bin/bash
# Test each new entity individually and fix issues

set -e

source ./bin/setup-java-env.sh

# New entities to test
NEW_ENTITIES=(
    "budgets"
    "budget-types"
    "invoices"
    "invoice-items"
    "orders"
    "currencies"
    "test-cases"
    "test-scenarios"
    "test-runs"
    "test-steps"
    "test-case-results"
    "issues"
    "issue-types"
    "teams"
)

mkdir -p target/test-logs target/screenshots

echo "🎯 ========================================"
echo "🎯 TESTING NEW ENTITIES ONE BY ONE"
echo "🎯 ========================================"
echo ""
echo "📋 Entities to test: ${#NEW_ENTITIES[@]}"
for entity in "${NEW_ENTITIES[@]}"; do
    echo "   - $entity"
done
echo ""

FAILED_ENTITIES=()
PASSED_ENTITIES=()

for entity in "${NEW_ENTITIES[@]}"; do
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "🧪 Testing: $entity"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    LOG_FILE="target/test-logs/entity-test-${entity}-$(date +%Y%m%d-%H%M%S).log"
    
    # Run focused test on this entity
    if PLAYWRIGHT_HEADLESS=false \
       mvn test \
       -Dtest=CPageTestNewEntities#testSingleEntity \
       -Dentity.name="$entity" \
       -Dspring.profiles.active=h2 \
       -Dplaywright.headless=false \
       -Dplaywright.showConsole=true \
       > "$LOG_FILE" 2>&1; then
        echo "✅ $entity PASSED"
        PASSED_ENTITIES+=("$entity")
    else
        echo "❌ $entity FAILED - Check log: $LOG_FILE"
        FAILED_ENTITIES+=("$entity")
        
        echo ""
        echo "📋 Last 30 lines of log:"
        tail -30 "$LOG_FILE"
        echo ""
        echo "⏸️  Pausing for 5 seconds before next entity..."
        sleep 5
    fi
    echo ""
done

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 FINAL RESULTS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "✅ Passed: ${#PASSED_ENTITIES[@]}/${#NEW_ENTITIES[@]}"
for entity in "${PASSED_ENTITIES[@]}"; do
    echo "   ✅ $entity"
done
echo ""
if [ ${#FAILED_ENTITIES[@]} -gt 0 ]; then
    echo "❌ Failed: ${#FAILED_ENTITIES[@]}/${#NEW_ENTITIES[@]}"
    for entity in "${FAILED_ENTITIES[@]}"; do
        echo "   ❌ $entity"
    done
    echo ""
    exit 1
else
    echo "🎉 ALL ENTITIES PASSED!"
    exit 0
fi
