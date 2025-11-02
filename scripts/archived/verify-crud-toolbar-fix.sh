#!/bin/bash
# Quick verification script for CrudToolbar fix
# Run this script once Maven network connectivity is restored

set -e  # Exit on any error

echo "=================================="
echo "CrudToolbar Fix Verification Script"
echo "=================================="
echo ""

# Step 1: Clean and compile
echo "Step 1: Compiling the code..."
mvn clean compile -DskipTests
if [ $? -eq 0 ]; then
    echo "✅ Compilation successful"
else
    echo "❌ Compilation failed"
    exit 1
fi
echo ""

# Step 2: Apply code formatting
echo "Step 2: Applying code formatting..."
mvn spotless:apply
if [ $? -eq 0 ]; then
    echo "✅ Code formatting applied"
else
    echo "❌ Code formatting failed"
    exit 1
fi
echo ""

# Step 3: Check if there are any formatting violations
echo "Step 3: Checking code formatting..."
mvn spotless:check
if [ $? -eq 0 ]; then
    echo "✅ Code formatting is correct"
else
    echo "❌ Code formatting violations found"
    exit 1
fi
echo ""

# Step 4: Start the application in background
echo "Step 4: Starting the application with H2 database..."
mvn spring-boot:run -Dspring.profiles.active=h2 &
APP_PID=$!
echo "Application PID: $APP_PID"
echo "Waiting for application to start (20 seconds)..."
sleep 20
echo ""

# Step 5: Check if application is running
echo "Step 5: Checking if application is accessible..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/)
if [ "$HTTP_CODE" == "302" ] || [ "$HTTP_CODE" == "200" ]; then
    echo "✅ Application is running (HTTP $HTTP_CODE)"
else
    echo "❌ Application is not accessible (HTTP $HTTP_CODE)"
    kill $APP_PID 2>/dev/null || true
    exit 1
fi
echo ""

echo "=================================="
echo "✅ All automated checks passed!"
echo "=================================="
echo ""
echo "Next steps for manual testing:"
echo "1. Open http://localhost:8080 in a browser"
echo "2. Login with admin/test123"
echo "3. Test CRUD operations on these pages:"
echo "   - Activities Management (/cdynamicpagerouter/page:3)"
echo "   - Meetings Management (/cdynamicpagerouter/page:4)"
echo "   - Projects Management (/cdynamicpagerouter/page:1)"
echo "   - Users Management (/cdynamicpagerouter/page:12)"
echo "4. For each page, test:"
echo "   - New button (creates new entity)"
echo "   - Save button (saves changes)"
echo "   - Delete button (shows dependency errors if applicable)"
echo "   - Refresh button (reloads data)"
echo "   - Status combobox (for entities with workflows)"
echo "5. Take screenshots of each page showing CRUD toolbar"
echo "6. Run Playwright tests: ./run-playwright-tests.sh mock"
echo ""
echo "Application is still running (PID: $APP_PID)"
echo "To stop: kill $APP_PID"
echo ""
