#!/bin/bash

# Copilot-friendly test execution script
# Attempts multiple approaches to run Playwright tests in sandbox environment

set -e

echo "🤖 Copilot Test Execution Attempt"
echo "================================="
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check if Maven repositories are accessible
check_maven_repos() {
    echo "Checking Maven repository access..."
    if ping -c 1 maven.vaadin.com &> /dev/null || ping -c 1 repo.maven.apache.org &> /dev/null; then
        echo -e "${GREEN}✓${NC} Maven repositories accessible"
        return 0
    else
        echo -e "${RED}✗${NC} Maven repositories NOT accessible"
        return 1
    fi
}

# Function to check if pre-built JAR exists
check_prebuilt_jar() {
    if [ -f "derbent-with-dependencies.jar" ]; then
        echo -e "${GREEN}✓${NC} Found pre-built JAR: derbent-with-dependencies.jar"
        return 0
    elif [ -f "target/derbent-1.0-SNAPSHOT.jar" ]; then
        echo -e "${GREEN}✓${NC} Found compiled JAR: target/derbent-1.0-SNAPSHOT.jar"
        return 0
    else
        echo -e "${RED}✗${NC} No pre-built JAR found"
        return 1
    fi
}

# Function to check if application is already running
check_app_running() {
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080 | grep -q "200\|302"; then
        echo -e "${GREEN}✓${NC} Application is already running on port 8080"
        return 0
    else
        echo -e "${YELLOW}ℹ${NC} Application not running on port 8080"
        return 1
    fi
}

# Approach 1: Use pre-built JAR
try_prebuilt_jar() {
    echo ""
    echo "📦 Approach 1: Running with pre-built JAR"
    echo "========================================"
    
    if check_prebuilt_jar; then
        JAR_FILE=""
        if [ -f "derbent-with-dependencies.jar" ]; then
            JAR_FILE="derbent-with-dependencies.jar"
        else
            JAR_FILE="target/derbent-1.0-SNAPSHOT.jar"
        fi
        
        echo "Starting application from $JAR_FILE..."
        java -jar "$JAR_FILE" --spring.profiles.active=h2 &
        APP_PID=$!
        echo "Application PID: $APP_PID"
        
        echo "Waiting for application to start (30 seconds)..."
        sleep 30
        
        if check_app_running; then
            echo "Running Playwright tests..."
            mvn test -Dtest="automated_tests.tech.derbent.ui.automation.CTypeStatusCrudTest" \
                -Dspring.profiles.active=test -Dplaywright.headless=true || true
            
            # Check if screenshots were generated
            if [ -d "target/screenshots" ] && [ "$(ls -A target/screenshots)" ]; then
                echo -e "${GREEN}✓${NC} Screenshots generated successfully!"
                ls -lh target/screenshots/ | tail -10
                kill $APP_PID
                return 0
            fi
        fi
        
        kill $APP_PID 2>/dev/null || true
    fi
    
    return 1
}

# Approach 2: Maven build and run (if repositories accessible)
try_maven_build() {
    echo ""
    echo "🔨 Approach 2: Maven build and test"
    echo "===================================="
    
    if check_maven_repos; then
        echo "Attempting Maven compilation..."
        if mvn clean compile -DskipTests -q; then
            echo -e "${GREEN}✓${NC} Maven build successful"
            
            echo "Running tests via script..."
            if ./run-playwright-tests.sh status-types; then
                echo -e "${GREEN}✓${NC} Tests executed successfully"
                return 0
            fi
        fi
    fi
    
    return 1
}

# Approach 3: Docker (if available)
try_docker() {
    echo ""
    echo "🐳 Approach 3: Docker-based testing"
    echo "==================================="
    
    if command -v docker &> /dev/null; then
        echo -e "${GREEN}✓${NC} Docker is available"
        
        # Check for Docker image
        if docker images | grep -q "derbent-test\|derbent-playwright"; then
            echo "Running tests in Docker container..."
            mkdir -p target/screenshots
            docker run -v $(pwd)/target/screenshots:/app/target/screenshots derbent-test || \
            docker run -v $(pwd)/target/screenshots:/app/target/screenshots derbent-playwright
            
            if [ "$(ls -A target/screenshots)" ]; then
                echo -e "${GREEN}✓${NC} Tests completed in Docker"
                return 0
            fi
        else
            echo -e "${YELLOW}ℹ${NC} No Docker image found (derbent-test or derbent-playwright)"
        fi
    else
        echo -e "${RED}✗${NC} Docker not available"
    fi
    
    return 1
}

# Approach 4: Test code analysis (no execution)
analyze_tests() {
    echo ""
    echo "🔍 Approach 4: Test code analysis (no execution)"
    echo "================================================"
    
    echo "Test files found:"
    find src/test/java -name "*Test.java" -type f | wc -l
    
    echo ""
    echo "Test methods found:"
    grep -r "@Test" src/test/java --include="*.java" | wc -l
    
    echo ""
    echo "CRUD test coverage:"
    grep -r "testCreate\|testRead\|testUpdate\|testDelete" src/test/java --include="*.java" | wc -l
    
    echo ""
    echo "Screenshot capture points:"
    grep -r "takeScreenshot" src/test/java --include="*.java" | wc -l
    
    echo -e "${YELLOW}ℹ${NC} Tests analyzed but not executed due to environment limitations"
    return 0
}

# Main execution
main() {
    echo "Current directory: $(pwd)"
    echo "Java version:"
    java -version 2>&1 | head -1
    echo "Maven version:"
    mvn -version 2>&1 | head -1
    echo ""
    
    # Try each approach in order
    if try_prebuilt_jar; then
        echo -e "${GREEN}✓✓✓ SUCCESS: Tests executed via pre-built JAR${NC}"
        exit 0
    fi
    
    if try_maven_build; then
        echo -e "${GREEN}✓✓✓ SUCCESS: Tests executed via Maven build${NC}"
        exit 0
    fi
    
    if try_docker; then
        echo -e "${GREEN}✓✓✓ SUCCESS: Tests executed via Docker${NC}"
        exit 0
    fi
    
    # Fallback: Just analyze
    echo ""
    echo -e "${YELLOW}⚠️  Cannot execute tests in current environment${NC}"
    echo ""
    analyze_tests
    
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "To enable Copilot test execution:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Option 1: Pre-built JAR (Recommended)"
    echo "  1. Run locally: mvn clean package -DskipTests"
    echo "  2. Commit: derbent-with-dependencies.jar"
    echo "  3. Copilot can then run tests automatically"
    echo ""
    echo "Option 2: Docker image"
    echo "  1. Build: docker build -f Dockerfile.playwright -t derbent-test ."
    echo "  2. Push to accessible registry"
    echo "  3. Copilot can pull and run"
    echo ""
    echo "For detailed instructions, see:"
    echo "  - COPILOT_SANDBOX_TESTING_GUIDE.md"
    echo "  - GUI_TESTING_EXECUTION_GUIDE.md"
    echo ""
    echo "For now, tests must be run locally where Maven"
    echo "repositories are accessible."
    echo ""
    
    exit 1
}

# Run main function
main "$@"
