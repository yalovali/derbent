#!/bin/bash
# Verifier Agent Helper Script
# Runs all verification checks

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

cd "$PROJECT_ROOT"

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

# Known non-C-prefix exceptions (do not rename; referenced in docs/pom)
C_PREFIX_EXCLUDES=(
    "src/main/java/tech/derbent/Application.java"
    "src/main/java/tech/derbent/DbResetApplication.java"
    "src/main/java/tech/derbent/SimpleDbResetApplication.java"
    "src/main/java/tech/derbent/api/config/AppConfig.java"
    "src/main/java/tech/derbent/api/config/VaadinConfig.java"
    "src/main/java/tech/derbent/api/projects/events/ProjectListChangeEvent.java"
    "src/main/java/tech/derbent/api/ui/config/LayoutServiceInjector.java"
    "src/main/java/tech/derbent/base/session/config/SessionConfiguration.java"
    "src/main/java/tech/derbent/base/users/config/UserServiceConfiguration.java"
)

# Check function
check() {
    local name="$1"
    local result=$2
    
    if [ $result -eq 0 ]; then
        echo -e "  ${GREEN}✅ PASS${NC}: $name"
        passed=$((passed + 1))
    else
        echo -e "  ${RED}❌ FAIL${NC}: $name"
        failed=$((failed + 1))
    fi
    return 0
}

echo "🔍 Running Static Analysis Checks..."
echo ""

# Check 1: C-Prefix Convention
echo "1. C-Prefix Convention"
prefix_matches=$(grep -r "^public class [A-Z]" src/main/java --include="*.java" 2>/dev/null | grep -v "public class C" || true)
if [ -n "$prefix_matches" ]; then
    prefix_matches=$(echo "$prefix_matches" | grep -v -F -f <(printf "%s\n" "${C_PREFIX_EXCLUDES[@]}") || true)
fi
if [ -n "$prefix_matches" ]; then
    violations=$(echo "$prefix_matches" | wc -l)
else
    violations=0
fi
check "C-Prefix on all classes" $([[ $violations -eq 0 ]] && echo 0 || echo 1)
if [ $violations -ne 0 ]; then
    echo "$prefix_matches"
fi

# Check 2: Raw Types (generic bases only)
echo "2. Generic Types"
declare -A generic_base_map
while read -r base; do
    if [ -n "$base" ]; then
        generic_base_map["$base"]=1
    fi
done < <(rg -N "^[[:space:]]*(public|protected|private)?[[:space:]]*(abstract|final)?[[:space:]]*(class|interface|record)[[:space:]]+(C[A-Za-z0-9_]+)[[:space:]]*<" \
    src/main/java --type-add "java:*.java" -t java \
    | sed -E 's/.*(class|interface|record)[[:space:]]+(C[A-Za-z0-9_]+)[[:space:]]*<.*/\2/' \
    | sort -u || true)

raw_type_matches=""
while IFS=: read -r file line_number line; do
    if [ -z "$file" ]; then
        continue
    fi
    if echo "$line" | rg -q "extends C[A-Za-z0-9_]+[[:space:]]*<"; then
        continue
    fi
    base=$(echo "$line" | sed -E 's/.*extends (C[A-Za-z0-9_]+).*/\1/')
    if [ -n "${generic_base_map[$base]}" ]; then
        raw_type_matches+="${file}:${line_number}:${line}"$'\n'
    fi
done < <(rg -n "^[[:space:]]*(public|protected|private)?[[:space:]]*(abstract|final)?[[:space:]]*class[[:space:]]+[A-Za-z0-9_]+[[:space:]]+extends[[:space:]]+C[A-Za-z0-9_]+" \
    src/main/java --type-add "java:*.java" -t java || true)

violations=0
if [ -n "$raw_type_matches" ]; then
    violations=$(echo "$raw_type_matches" | wc -l | tr -d ' ')
fi
check "No raw types" $([[ $violations -eq 0 ]] && echo 0 || echo 1)
if [ $violations -ne 0 ]; then
    echo "$raw_type_matches"
fi

# Check 3: Field Injection
echo "3. Constructor Injection"
field_injection_matches=$(rg -n -U "@Autowired\\s*\\n\\s*(private|protected)" src/main/java --type-add "java:*.java" -t java || true)
violations=0
if [ -n "$field_injection_matches" ]; then
    violations=$(echo "$field_injection_matches" | wc -l | tr -d ' ')
fi
check "No field injection" $([[ $violations -eq 0 ]] && echo 0 || echo 1)
if [ $violations -ne 0 ]; then
    echo "$field_injection_matches"
fi

# Check 4: Entity Constants (sample check)
echo "4. Entity Constants"
missing=0
for file in $(rg -l "^[[:space:]]*@Entity\\b" src/main/java --type-add "java:*.java" -t java 2>/dev/null); do
    if rg -q "^[[:space:]]*(public|protected|private)?[[:space:]]*abstract[[:space:]]+class" "$file"; then
        continue
    fi
    if ! grep -q "DEFAULT_COLOR" "$file" 2>/dev/null; then
        echo "Missing DEFAULT_COLOR: $file"
        missing=$((missing + 1))
    fi
    if ! grep -q "DEFAULT_ICON" "$file" 2>/dev/null; then
        echo "Missing DEFAULT_ICON: $file"
        missing=$((missing + 1))
    fi
    if ! grep -q "ENTITY_TITLE_SINGULAR" "$file" 2>/dev/null; then
        echo "Missing ENTITY_TITLE_SINGULAR: $file"
        missing=$((missing + 1))
    fi
    if ! grep -q "ENTITY_TITLE_PLURAL" "$file" 2>/dev/null; then
        echo "Missing ENTITY_TITLE_PLURAL: $file"
        missing=$((missing + 1))
    fi
    if ! grep -q "VIEW_NAME" "$file" 2>/dev/null; then
        echo "Missing VIEW_NAME: $file"
        missing=$((missing + 1))
    fi
done
check "Entity constants present" $([[ $missing -eq 0 ]] && echo 0 || echo 1)

# Check 5: Imports vs Fully-Qualified
echo "5. Import Statements"
import_matches=$(rg -n "tech\\.derbent\\.[a-z].*\\.[A-Z]" src/main/java --type-add "java:*.java" -t java || true)
if [ -n "$import_matches" ]; then
    import_matches=$(echo "$import_matches" \
        | rg -v ":import " \
        | rg -v ":\\s*\\* " \
        | rg -v ":\\s*//" \
        | rg -v ":\\s*\\* @param" \
        | rg -v ":\\s*\\* @return" \
        | rg -v "\".*tech\\.derbent.*\"" || true)
fi
violations=0
if [ -n "$import_matches" ]; then
    violations=$(echo "$import_matches" | wc -l | tr -d ' ')
fi
check "Uses imports (not fully-qualified)" $([[ $violations -eq 0 ]] && echo 0 || echo 1)
if [ $violations -ne 0 ]; then
    echo "$import_matches"
fi

echo ""
echo "🏗️  Running Build Checks..."
echo ""

# Check 6: Compilation
echo "6. Compilation"
if mvn clean compile -Pagents -DskipTests -q > /tmp/build.log 2>&1; then
    check "Maven compile" 0
else
    check "Maven compile" 1
fi

# Check 7: Spotless
echo "7. Code Formatting"
if mvn spotless:check -q > /tmp/spotless.log 2>&1; then
    check "Spotless formatting" 0
else
    check "Spotless formatting" 1
    echo -e "  ${YELLOW}⚠️  Run 'mvn spotless:apply' to fix${NC}"
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
