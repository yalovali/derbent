#!/bin/bash

# Simplified Playwright UI Test Runner with PostgreSQL
# Runs tests with visible browser using existing PostgreSQL database

set -e

# Setup Java 21 environment
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/setup-java-env.sh"

echo "🚀 Playwright Test Runner (PostgreSQL + Visible Browser)"
echo "========================================================"

# PostgreSQL connection settings (modify if needed)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-derbent}"
DB_USER="${DB_USER:-postgres}"
DB_PASS="${DB_PASS:-derbent}"

# Check PostgreSQL connectivity
echo "🔍 Checking PostgreSQL at $DB_HOST:$DB_PORT/$DB_NAME..."
export PGPASSWORD="$DB_PASS"
if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1;" >/dev/null 2>&1; then
    echo "❌ Cannot connect to PostgreSQL"
    echo "   Make sure PostgreSQL is running and database '$DB_NAME' exists"
    echo "   Using: host=$DB_HOST port=$DB_PORT user=$DB_USER database=$DB_NAME"
    exit 1
fi
echo "✅ PostgreSQL connected"
unset PGPASSWORD

# Create screenshots directory
mkdir -p target/screenshots

# Install Playwright browsers (silently, only if needed)
echo "🔄 Checking Playwright browsers..."
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install" >/dev/null 2>&1 || true

# Run the test
echo "🧪 Running test with visible browser..."
echo ""

mvn test \
    -Dtest="automated_tests.tech.derbent.ui.automation.CSampleDataMenuNavigationTest" \
    -Dspring.profiles.active=default \
    -Dspring.datasource.url="jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME" \
    -Dspring.datasource.username="$DB_USER" \
    -Dspring.datasource.password="$DB_PASS" \
    -Dspring.jpa.hibernate.ddl-auto=none \
    -Dspring.sql.init.mode=never \
    -Dplaywright.headless=false

# Report results
echo ""
echo "✅ Test completed successfully!"

# Show screenshots if any
SCREENSHOT_COUNT=$(find target/screenshots -name "*.png" 2>/dev/null | wc -l)
if [ "$SCREENSHOT_COUNT" -gt 0 ]; then
    echo "📸 Generated $SCREENSHOT_COUNT screenshots in target/screenshots/"
fi
