#!/bin/bash
# ==============================================================================
# Derbent Project Documentation Generator
# ==============================================================================
# 
# Purpose:
#   Generates comprehensive code documentation using Doxygen and Graphviz.
#   Creates class diagrams, call graphs, collaboration diagrams, and HTML docs.
#
# Prerequisites:
#   - doxygen (apt-get install doxygen)
#   - graphviz (apt-get install graphviz)
#   - plantuml (optional, for enhanced diagrams)
#
# Usage:
#   ./scripts/generate-graphviz-docs.sh [options]
#
# Options:
#   --full       Generate full documentation (default)
#   --gantt      Generate only Gantt chart documentation
#   --clean      Clean generated documentation before building
#   --open       Open documentation in browser after generation
#   --help       Show this help message
#
# Output:
#   - Full project docs: docs/graphviz-output/html/index.html
#   - Gantt docs: docs/doxygen-output/html/index.html
#
# ==============================================================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Configuration
DOXYFILE_FULL="$PROJECT_ROOT/Doxyfile"
DOXYFILE_GANTT="$PROJECT_ROOT/Doxyfile.gantt"
OUTPUT_DIR_FULL="$PROJECT_ROOT/docs/graphviz-output"
OUTPUT_DIR_GANTT="$PROJECT_ROOT/docs/doxygen-output"

# Default options
MODE="full"
CLEAN=false
OPEN_BROWSER=false

# ==============================================================================
# Helper Functions
# ==============================================================================

print_header() {
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

show_help() {
    cat << EOF
Derbent Documentation Generator

Usage: $(basename "$0") [options]

Options:
    --full       Generate full project documentation (default)
    --gantt      Generate only Gantt chart documentation
    --clean      Clean generated documentation before building
    --open       Open documentation in browser after generation
    --help       Show this help message

Examples:
    # Generate full documentation
    ./scripts/generate-graphviz-docs.sh

    # Clean and regenerate Gantt documentation
    ./scripts/generate-graphviz-docs.sh --gantt --clean

    # Generate and open in browser
    ./scripts/generate-graphviz-docs.sh --open

Prerequisites:
    sudo apt-get install doxygen graphviz
    
    Optional:
    sudo apt-get install plantuml

Output Locations:
    Full project:  docs/graphviz-output/html/index.html
    Gantt charts:  docs/doxygen-output/html/index.html

EOF
    exit 0
}

check_dependencies() {
    print_header "Checking Dependencies"
    
    local missing_deps=()
    
    # Check doxygen
    if command -v doxygen &> /dev/null; then
        print_success "doxygen is installed ($(doxygen --version))"
    else
        print_error "doxygen is not installed"
        missing_deps+=("doxygen")
    fi
    
    # Check dot (graphviz)
    if command -v dot &> /dev/null; then
        print_success "graphviz is installed ($(dot -V 2>&1 | head -1))"
    else
        print_error "graphviz is not installed"
        missing_deps+=("graphviz")
    fi
    
    # Check plantuml (optional)
    if [ -f "/usr/share/plantuml/plantuml.jar" ] || command -v plantuml &> /dev/null; then
        print_success "plantuml is installed (optional)"
    else
        print_warning "plantuml is not installed (optional - enhanced diagrams)"
    fi
    
    if [ ${#missing_deps[@]} -gt 0 ]; then
        echo ""
        print_error "Missing required dependencies: ${missing_deps[*]}"
        echo ""
        print_info "Install with: sudo apt-get install ${missing_deps[*]}"
        echo ""
        exit 1
    fi
    
    echo ""
}

clean_output() {
    print_header "Cleaning Previous Documentation"
    
    if [ "$MODE" = "full" ]; then
        if [ -d "$OUTPUT_DIR_FULL" ]; then
            print_info "Removing $OUTPUT_DIR_FULL"
            rm -rf "$OUTPUT_DIR_FULL"
            print_success "Cleaned full documentation output"
        else
            print_info "No previous full documentation to clean"
        fi
    elif [ "$MODE" = "gantt" ]; then
        if [ -d "$OUTPUT_DIR_GANTT" ]; then
            print_info "Removing $OUTPUT_DIR_GANTT"
            rm -rf "$OUTPUT_DIR_GANTT"
            print_success "Cleaned Gantt documentation output"
        else
            print_info "No previous Gantt documentation to clean"
        fi
    fi
    
    echo ""
}

generate_documentation() {
    print_header "Generating Documentation"
    
    local doxyfile
    local output_dir
    
    if [ "$MODE" = "full" ]; then
        doxyfile="$DOXYFILE_FULL"
        output_dir="$OUTPUT_DIR_FULL"
        print_info "Mode: Full project documentation"
    elif [ "$MODE" = "gantt" ]; then
        doxyfile="$DOXYFILE_GANTT"
        output_dir="$OUTPUT_DIR_GANTT"
        print_info "Mode: Gantt chart documentation"
    fi
    
    if [ ! -f "$doxyfile" ]; then
        print_error "Doxyfile not found: $doxyfile"
        exit 1
    fi
    
    print_info "Using configuration: $doxyfile"
    print_info "Output directory: $output_dir"
    echo ""
    
    cd "$PROJECT_ROOT"
    
    print_info "Running Doxygen..."
    if doxygen "$doxyfile"; then
        echo ""
        print_success "Documentation generated successfully!"
    else
        echo ""
        print_error "Documentation generation failed!"
        exit 1
    fi
    
    echo ""
}

show_statistics() {
    print_header "Documentation Statistics"
    
    local output_dir
    if [ "$MODE" = "full" ]; then
        output_dir="$OUTPUT_DIR_FULL"
    else
        output_dir="$OUTPUT_DIR_GANTT"
    fi
    
    if [ -d "$output_dir/html" ]; then
        local html_files=$(find "$output_dir/html" -name "*.html" | wc -l)
        local png_files=$(find "$output_dir/html" -name "*.png" | wc -l)
        local svg_files=$(find "$output_dir/html" -name "*.svg" | wc -l)
        local total_size=$(du -sh "$output_dir" | cut -f1)
        
        print_info "HTML files generated: $html_files"
        print_info "PNG diagrams generated: $png_files"
        print_info "SVG diagrams generated: $svg_files"
        print_info "Total documentation size: $total_size"
        echo ""
        print_success "Documentation location: $output_dir/html/index.html"
    else
        print_warning "Documentation directory not found"
    fi
    
    echo ""
}

open_documentation() {
    local output_dir
    if [ "$MODE" = "full" ]; then
        output_dir="$OUTPUT_DIR_FULL"
    else
        output_dir="$OUTPUT_DIR_GANTT"
    fi
    
    local index_file="$output_dir/html/index.html"
    
    if [ -f "$index_file" ]; then
        print_header "Opening Documentation"
        
        if command -v xdg-open &> /dev/null; then
            xdg-open "$index_file" &
            print_success "Opening documentation in default browser"
        elif command -v open &> /dev/null; then
            open "$index_file" &
            print_success "Opening documentation in default browser"
        else
            print_warning "Could not auto-open browser. Open manually:"
            print_info "file://$index_file"
        fi
    else
        print_error "Documentation index not found: $index_file"
    fi
    
    echo ""
}

# ==============================================================================
# Parse Arguments
# ==============================================================================

while [[ $# -gt 0 ]]; do
    case $1 in
        --full)
            MODE="full"
            shift
            ;;
        --gantt)
            MODE="gantt"
            shift
            ;;
        --clean)
            CLEAN=true
            shift
            ;;
        --open)
            OPEN_BROWSER=true
            shift
            ;;
        --help|-h)
            show_help
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# ==============================================================================
# Main Execution
# ==============================================================================

print_header "Derbent Documentation Generator"
print_info "Project: $(basename "$PROJECT_ROOT")"
print_info "Mode: $MODE"
echo ""

# Check if required tools are installed
check_dependencies

# Clean if requested
if [ "$CLEAN" = true ]; then
    clean_output
fi

# Generate documentation
generate_documentation

# Show statistics
show_statistics

# Open in browser if requested
if [ "$OPEN_BROWSER" = true ]; then
    open_documentation
fi

print_header "Documentation Generation Complete"
print_success "All tasks completed successfully!"

if [ "$MODE" = "full" ]; then
    print_info "View documentation: docs/graphviz-output/html/index.html"
else
    print_info "View documentation: docs/doxygen-output/html/index.html"
fi

echo ""
