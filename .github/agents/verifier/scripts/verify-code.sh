#!/bin/bash
# Verifier Agent Helper Script
# Runs all verification checks

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

echo "✅ Derbent Code Verifier"
echo "========================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

passed=0
failed=0

# Check function
check() {
    local name="$1"
    local result=$2
    
    if [ $result -eq 0 ]; then
        echo -e "  ${GREEN}✅ PASS${NC}: $name"
        ((passed++))
    else
        echo -e "  ${RED}❌ FAIL${NC}: $name"
        ((failed++))
    fi
}

echo "🔍 Running Static Analysis Checks..."
echo ""

# Check 1: C-Prefix Convention
echo "1. C-Prefix Convention"
violations=$(grep -r "^public class [A-Z]" src/main/java --include="*.java" 2>/dev/null | grep -v "^public class C" | wc -l)
check "C-Prefix on all classes" $([[ $violations -eq 0 ]] && echo 0 || echo 1)

# Check 2: Raw Types
echo "2. Generic Types"
violations=$(grep -r "extends C.*[^<>].*{" src/main/java --include="*.java" 2>/dev/null | grep -v "extends C.*<" | wc -l)
check "No raw types" $([[ $violations -eq 0 ]] && echo 0 || echo 1)

# Check 3: Field Injection
echo "3. Constructor Injection"
violations=$(grep -r "@Autowired" src/main/java --include="*.java" 2>/dev/null | grep -v "Constructor" | wc -l)
check "No field injection" $([[ $violations -eq 0 ]] && echo 0 || echo 1)

# Check 4: Entity Constants (sample check)
echo "4. Entity Constants"
missing=0
for file in $(find src/main/java -name "C*.java" -path "*/domain/*" | head -5); do
    if grep -q "extends C.*<" "$file" 2>/dev/null; then
        if ! grep -q "DEFAULT_COLOR\|DEFAULT_ICON\|ENTITY_TITLE" "$file" 2>/dev/null; then
            ((missing++))
        fi
    fi
done
check "Entity constants present (sample)" $([[ $missing -eq 0 ]] && echo 0 || echo 1)

# Check 5: Imports vs Fully-Qualified
echo "5. Import Statements"
violations=$(grep -r "tech\.derbent\.[a-z].*\.[A-Z]" src/main/java --include="*.java" 2>/dev/null | \
    grep -v "^import" | grep -v "* @param" | grep -v "* @return" | wc -l)
check "Uses imports (not fully-qualified)" $([[ $violations -eq 0 ]] && echo 0 || echo 1)

echo ""
echo "🏗️  Running Build Checks..."
echo ""

# Check 6: Compilation
echo "6. Compilation"
mvn clean compile -Pagents -DskipTests -q 2>&1 > /tmp/build.log
build_result=$?
check "Maven compile" $build_result

# Check 7: Spotless
echo "7. Code Formatting"
mvn spotless:check -q 2>&1 > /tmp/spotless.log
spotless_result=$?
if [ $spotless_result -ne 0 ]; then
    check "Spotless formatting" 1
    echo -e "  ${YELLOW}⚠️  Run 'mvn spotless:apply' to fix${NC}"
else
    check "Spotless formatting" 0
fi

echo ""
echo "📊 Summary"
echo "=========="
total=$((passed + failed))
echo "  Total Checks: $total"
echo -e "  ${GREEN}Passed: $passed${NC}"
echo -e "  ${RED}Failed: $failed${NC}"
echo ""

if [ $failed -eq 0 ]; then
    echo -e "${GREEN}✅ ALL CHECKS PASSED!${NC}"
    exit 0
else
    echo -e "${RED}❌ SOME CHECKS FAILED${NC}"
    echo ""
    echo "Review logs:"
    echo "  - Build: /tmp/build.log"
    echo "  - Spotless: /tmp/spotless.log"
    exit 1
fi
