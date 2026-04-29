#!/bin/bash
# Pattern Designer Helper Script
# Analyzes codebase for patterns and generates documentation

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

play_sound() {
    local kind="${1:-success}"
    if [ "${DERBENT_SOUND_ENABLED:-true}" != "true" ]; then return 0; fi
    if command -v paplay >/dev/null 2>&1; then
        case "$kind" in
            start)    paplay /usr/share/sounds/freedesktop/stereo/service-login.oga      >/dev/null 2>&1 || true; return 0 ;;
            all-done) paplay /usr/share/sounds/freedesktop/stereo/alarm-clock-elapsed.oga >/dev/null 2>&1 || true; return 0 ;;
            success)  paplay /usr/share/sounds/freedesktop/stereo/complete.oga            >/dev/null 2>&1 || true; return 0 ;;
            *)        paplay /usr/share/sounds/freedesktop/stereo/dialog-error.oga        >/dev/null 2>&1 || true; return 0 ;;
        esac
    fi
    case "$kind" in
        start)    printf '\a\a' ;;
        all-done) printf '\a\a\a\a\a' ;;
        success)  printf '\a\a' ;;
        *)        printf '\a\a\a' ;;
    esac
}

echo "🏗️  Pattern Designer Analysis Tool"
echo "=================================="
echo ""

# Function to analyze entity patterns
analyze_entities() {
    echo "📊 Analyzing Entity Patterns..."
    echo ""
    
    # Find all entity classes
    echo "Entity Classes Found:"
    grep -r "public class C.*extends.*<" src/main/java --include="*.java" | \
        grep -E "domain/C.*\.java" | \
        sed 's/.*\/\(C[^.]*\)\.java.*/  - \1/' | \
        sort | uniq
    
    echo ""
    
    # Check for abstract entities
    echo "Abstract Entities (@MappedSuperclass):"
    grep -rl "@MappedSuperclass" src/main/java --include="*.java" | \
        sed 's/.*\/\(C[^.]*\)\.java/  - \1/'
    
    echo ""
}

# Function to analyze service patterns
analyze_services() {
    echo "📊 Analyzing Service Patterns..."
    echo ""
    
    # Find service hierarchies
    echo "Service Hierarchies:"
    echo "  CEntityOfProjectService:"
    grep -r "extends CEntityOfProjectService<" src/main/java --include="*Service.java" | \
        sed 's/.*\/\([^/]*Service\)\.java.*/    - \1/' | \
        sort | uniq
    
    echo ""
    echo "  CEntityOfCompanyService:"
    grep -r "extends CEntityOfCompanyService<" src/main/java --include="*Service.java" | \
        sed 's/.*\/\([^/]*Service\)\.java.*/    - \1/' | \
        sort | uniq
    
    echo ""
}

# Function to analyze interface implementations
analyze_interfaces() {
    echo "📊 Analyzing Interface Implementations..."
    echo ""
    
    local interfaces=(
        "IHasAttachments"
        "IHasComments"
        "IHasLinks"
        "IHasStatusAndWorkflow"
        "IHasAgileParentRelation"
    )
    
    for iface in "${interfaces[@]}"; do
        echo "  $iface:"
        grep -r "implements.*$iface" src/main/java --include="*.java" | \
            grep -o "class C[^ ]*" | \
            sed 's/class /    - /' | \
            sort | uniq
        echo ""
    done
}

# Function to find pattern violations
find_violations() {
    echo "🔍 Checking for Pattern Violations..."
    echo ""
    
    violations_found=0
    
    # Check for raw types
    echo "Checking raw types..."
    raw_types=$(grep -r "extends C.*[^<>].*{" src/main/java --include="*.java" | grep -v "extends C.*<" | wc -l)
    if [ $raw_types -gt 0 ]; then
        echo "  ❌ Found $raw_types raw types"
        ((violations_found++))
    else
        echo "  ✅ No raw types found"
    fi
    
    # Check for field injection
    echo "Checking field injection..."
    field_injection=$(grep -r "@Autowired" src/main/java --include="*.java" | grep -v "Constructor" | wc -l)
    if [ $field_injection -gt 0 ]; then
        echo "  ❌ Found $field_injection @Autowired fields"
        ((violations_found++))
    else
        echo "  ✅ No field injection found"
    fi
    
    echo ""
    
    if [ $violations_found -eq 0 ]; then
        echo "✅ No violations found!"
    else
        echo "❌ Found $violations_found types of violations"
    fi
    
    echo ""
}

# Main menu
show_menu() {
    echo "Select analysis type:"
    echo "  1) Analyze Entities"
    echo "  2) Analyze Services"
    echo "  3) Analyze Interfaces"
    echo "  4) Find Violations"
    echo "  5) All of the above"
    echo "  q) Quit"
    echo ""
    read -p "Choice: " choice
    
    case $choice in
        1) analyze_entities ;;
        2) analyze_services ;;
        3) analyze_interfaces ;;
        4) find_violations ;;
        5)
            analyze_entities
            analyze_services
            analyze_interfaces
            find_violations
            ;;
        q) exit 0 ;;
        *) echo "Invalid choice" ;;
    esac
    
    echo ""
    read -p "Press Enter to continue..."
    show_menu
}

# Run
cd "$PROJECT_ROOT"
play_sound start
show_menu
play_sound all-done
