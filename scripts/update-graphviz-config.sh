#!/bin/bash
# ==============================================================================
# Doxygen Configuration Editor Helper
# ==============================================================================
#
# Purpose:
#   Interactive helper script to update Doxygen configuration files.
#   Provides common configuration tasks and guidance.
#
# Usage:
#   ./scripts/update-graphviz-config.sh [--full | --gantt]
#
# ==============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Configuration files
DOXYFILE_FULL="$PROJECT_ROOT/Doxyfile"
DOXYFILE_GANTT="$PROJECT_ROOT/Doxyfile.gantt"

# Default target
TARGET="full"

print_header() {
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
}

print_info() {
    echo -e "${CYAN}ℹ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

show_menu() {
    print_header "Doxygen Configuration Editor"
    
    local doxyfile
    if [ "$TARGET" = "full" ]; then
        doxyfile="$DOXYFILE_FULL"
        print_info "Target: Full Project Documentation"
    else
        doxyfile="$DOXYFILE_GANTT"
        print_info "Target: Gantt Chart Documentation"
    fi
    
    print_info "File: $doxyfile"
    echo ""
    
    echo "What would you like to do?"
    echo ""
    echo "1) View current configuration"
    echo "2) Edit configuration in text editor"
    echo "3) Update specific setting"
    echo "4) Common configuration tasks"
    echo "5) Show configuration guide"
    echo "6) Validate configuration"
    echo "7) Switch target (full <-> gantt)"
    echo "8) Exit"
    echo ""
}

view_configuration() {
    print_header "Current Configuration"
    
    local doxyfile
    if [ "$TARGET" = "full" ]; then
        doxyfile="$DOXYFILE_FULL"
    else
        doxyfile="$DOXYFILE_GANTT"
    fi
    
    if [ -f "$doxyfile" ]; then
        echo "Project Name: $(grep "^PROJECT_NAME" "$doxyfile" | cut -d'=' -f2 | xargs)"
        echo "Output Directory: $(grep "^OUTPUT_DIRECTORY" "$doxyfile" | cut -d'=' -f2 | xargs)"
        echo "Generate HTML: $(grep "^GENERATE_HTML" "$doxyfile" | cut -d'=' -f2 | xargs)"
        echo "Generate LaTeX: $(grep "^GENERATE_LATEX" "$doxyfile" | cut -d'=' -f2 | xargs)"
        echo "Have DOT (Graphviz): $(grep "^HAVE_DOT" "$doxyfile" | cut -d'=' -f2 | xargs)"
        echo "Call Graph: $(grep "^CALL_GRAPH" "$doxyfile" | cut -d'=' -f2 | xargs)"
        echo "Collaboration Graph: $(grep "^COLLABORATION_GRAPH" "$doxyfile" | cut -d'=' -f2 | xargs)"
        echo "Class Graph: $(grep "^CLASS_GRAPH" "$doxyfile" | cut -d'=' -f2 | xargs)"
        echo "UML Look: $(grep "^UML_LOOK" "$doxyfile" | cut -d'=' -f2 | xargs)"
    else
        print_warning "Configuration file not found: $doxyfile"
    fi
    
    echo ""
}

edit_configuration() {
    local doxyfile
    if [ "$TARGET" = "full" ]; then
        doxyfile="$DOXYFILE_FULL"
    else
        doxyfile="$DOXYFILE_GANTT"
    fi
    
    if [ -f "$doxyfile" ]; then
        print_info "Opening $doxyfile in editor..."
        "${EDITOR:-nano}" "$doxyfile"
        print_success "Configuration edited"
    else
        print_warning "Configuration file not found: $doxyfile"
    fi
    
    echo ""
}

update_setting() {
    local doxyfile
    if [ "$TARGET" = "full" ]; then
        doxyfile="$DOXYFILE_FULL"
    else
        doxyfile="$DOXYFILE_GANTT"
    fi
    
    echo ""
    echo "Enter the setting name (e.g., PROJECT_NAME, CALL_GRAPH):"
    read -r setting_name
    
    if [ -z "$setting_name" ]; then
        print_warning "No setting name provided"
        return
    fi
    
    current_value=$(grep "^$setting_name" "$doxyfile" | cut -d'=' -f2- | xargs)
    
    if [ -n "$current_value" ]; then
        echo "Current value: $current_value"
    else
        print_warning "Setting not found in configuration"
    fi
    
    echo "Enter new value:"
    read -r new_value
    
    if [ -z "$new_value" ]; then
        print_warning "No value provided, keeping current setting"
        return
    fi
    
    # Backup original
    cp "$doxyfile" "$doxyfile.backup"
    
    # Update setting
    if grep -q "^$setting_name" "$doxyfile"; then
        sed -i "s|^$setting_name.*|$setting_name = $new_value|" "$doxyfile"
        print_success "Setting updated: $setting_name = $new_value"
        print_info "Backup saved: $doxyfile.backup"
    else
        print_warning "Setting not found: $setting_name"
    fi
    
    echo ""
}

common_tasks() {
    print_header "Common Configuration Tasks"
    
    echo "Select a task:"
    echo ""
    echo "1) Enable/Disable call graphs"
    echo "2) Enable/Disable collaboration graphs"
    echo "3) Change output format (HTML/LaTeX/PDF)"
    echo "4) Adjust graph complexity (max nodes)"
    echo "5) Enable/Disable UML style diagrams"
    echo "6) Add/Remove input directories"
    echo "7) Configure PlantUML integration"
    echo "8) Back to main menu"
    echo ""
    
    read -r choice
    
    local doxyfile
    if [ "$TARGET" = "full" ]; then
        doxyfile="$DOXYFILE_FULL"
    else
        doxyfile="$DOXYFILE_GANTT"
    fi
    
    case $choice in
        1)
            echo "Enable call graphs? (YES/NO):"
            read -r enable
            sed -i "s|^CALL_GRAPH.*|CALL_GRAPH = $enable|" "$doxyfile"
            sed -i "s|^CALLER_GRAPH.*|CALLER_GRAPH = $enable|" "$doxyfile"
            print_success "Call graphs: $enable"
            ;;
        2)
            echo "Enable collaboration graphs? (YES/NO):"
            read -r enable
            sed -i "s|^COLLABORATION_GRAPH.*|COLLABORATION_GRAPH = $enable|" "$doxyfile"
            print_success "Collaboration graphs: $enable"
            ;;
        3)
            echo "Generate HTML? (YES/NO):"
            read -r html
            echo "Generate LaTeX? (YES/NO):"
            read -r latex
            sed -i "s|^GENERATE_HTML.*|GENERATE_HTML = $html|" "$doxyfile"
            sed -i "s|^GENERATE_LATEX.*|GENERATE_LATEX = $latex|" "$doxyfile"
            print_success "Output formats updated"
            ;;
        4)
            echo "Enter maximum nodes in graph (default: 100, 0 for unlimited):"
            read -r max_nodes
            sed -i "s|^DOT_GRAPH_MAX_NODES.*|DOT_GRAPH_MAX_NODES = $max_nodes|" "$doxyfile"
            print_success "Max graph nodes: $max_nodes"
            ;;
        5)
            echo "Enable UML look? (YES/NO):"
            read -r uml
            sed -i "s|^UML_LOOK.*|UML_LOOK = $uml|" "$doxyfile"
            print_success "UML look: $uml"
            ;;
        *)
            print_info "Returning to main menu"
            ;;
    esac
    
    echo ""
}

show_guide() {
    print_header "Doxygen Configuration Guide"
    
    cat << 'EOF'
Common Doxygen Configuration Options:

PROJECT_NAME
    The name of your project
    Example: "Derbent Project Management"

OUTPUT_DIRECTORY
    Where to generate documentation
    Example: docs/graphviz-output

HAVE_DOT
    Enable/disable Graphviz diagrams
    Values: YES | NO

CALL_GRAPH
    Generate call dependency graphs
    Values: YES | NO
    Note: Shows which functions call which

CALLER_GRAPH
    Generate reverse call graphs
    Values: YES | NO
    Note: Shows which functions are called by which

CLASS_GRAPH
    Generate class inheritance diagrams
    Values: YES | NO

COLLABORATION_GRAPH
    Generate collaboration diagrams
    Values: YES | NO
    Note: Shows class relationships and dependencies

UML_LOOK
    Use UML notation in diagrams
    Values: YES | NO

DOT_GRAPH_MAX_NODES
    Maximum nodes in a graph
    Values: Number (0 = unlimited)
    Note: Large graphs can be slow to generate

INTERACTIVE_SVG
    Generate interactive SVG diagrams
    Values: YES | NO
    Note: Allows clickable diagrams in browsers

INPUT
    Source directories to document
    Example: src/main/java/tech/derbent

FILE_PATTERNS
    File types to include
    Example: *.java *.md

RECURSIVE
    Process subdirectories
    Values: YES | NO

EXCLUDE_PATTERNS
    Directories/files to exclude
    Example: */test/* */bin/*

To edit configuration:
    nano Doxyfile              # Full project
    nano Doxyfile.gantt        # Gantt charts only

To generate documentation:
    ./scripts/generate-graphviz-docs.sh

More information:
    https://www.doxygen.nl/manual/config.html

EOF
    
    echo ""
}

validate_configuration() {
    print_header "Validating Configuration"
    
    local doxyfile
    if [ "$TARGET" = "full" ]; then
        doxyfile="$DOXYFILE_FULL"
    else
        doxyfile="$DOXYFILE_GANTT"
    fi
    
    if [ ! -f "$doxyfile" ]; then
        print_warning "Configuration file not found: $doxyfile"
        return
    fi
    
    print_info "Checking $doxyfile..."
    echo ""
    
    # Check critical settings
    local errors=0
    
    if ! grep -q "^PROJECT_NAME" "$doxyfile"; then
        print_warning "PROJECT_NAME not set"
        ((errors++))
    else
        print_success "PROJECT_NAME is set"
    fi
    
    if ! grep -q "^OUTPUT_DIRECTORY" "$doxyfile"; then
        print_warning "OUTPUT_DIRECTORY not set"
        ((errors++))
    else
        print_success "OUTPUT_DIRECTORY is set"
    fi
    
    if ! grep -q "^INPUT" "$doxyfile"; then
        print_warning "INPUT directories not set"
        ((errors++))
    else
        print_success "INPUT is set"
    fi
    
    # Check if dot is available when HAVE_DOT is YES
    if grep -q "^HAVE_DOT.*YES" "$doxyfile"; then
        if command -v dot &> /dev/null; then
            print_success "HAVE_DOT = YES and Graphviz is installed"
        else
            print_warning "HAVE_DOT = YES but Graphviz (dot) is not installed"
            print_info "Install with: sudo apt-get install graphviz"
            ((errors++))
        fi
    fi
    
    echo ""
    if [ $errors -eq 0 ]; then
        print_success "Configuration is valid!"
    else
        print_warning "Found $errors potential issue(s)"
    fi
    
    echo ""
}

switch_target() {
    if [ "$TARGET" = "full" ]; then
        TARGET="gantt"
        print_success "Switched to Gantt chart configuration"
    else
        TARGET="full"
        print_success "Switched to full project configuration"
    fi
    echo ""
}

# Parse arguments
if [ "$1" = "--gantt" ]; then
    TARGET="gantt"
elif [ "$1" = "--full" ]; then
    TARGET="full"
fi

# Main loop
while true; do
    show_menu
    read -r choice
    
    case $choice in
        1)
            view_configuration
            ;;
        2)
            edit_configuration
            ;;
        3)
            update_setting
            ;;
        4)
            common_tasks
            ;;
        5)
            show_guide
            ;;
        6)
            validate_configuration
            ;;
        7)
            switch_target
            ;;
        8)
            print_success "Goodbye!"
            exit 0
            ;;
        *)
            print_warning "Invalid choice"
            ;;
    esac
    
    echo "Press Enter to continue..."
    read -r
done
