#!/bin/bash
# Documentation Cleanup Script
# Use this to optionally archive fully-consolidated documentation

set -e

ARCHIVE_DIR="docs/archive/consolidated-2026-01"

echo "================================================================================
                    DOCUMENTATION CLEANUP UTILITY
================================================================================
"

echo "This script helps archive documentation that has been consolidated into AGENTS.md"
echo ""
echo "⚠️  WARNING: This will MOVE files to archive, not delete them"
echo ""
echo "Files that were fully integrated into AGENTS.md v2.0:"
echo "  - None yet - recommend keeping all specialized docs for deep dives"
echo ""
echo "Recommendation:"
echo "  1. Keep AGENTS.md as master playbook"
echo "  2. Keep specialized docs for detailed implementation notes"
echo "  3. Cross-reference between them"
echo ""
echo "Do you want to create the archive directory structure anyway? (y/n)"
read -r response

if [[ "$response" != "y" ]]; then
    echo "Aborted. No changes made."
    exit 0
fi

# Create archive structure
mkdir -p "$ARCHIVE_DIR"

echo "✅ Created archive directory: $ARCHIVE_DIR"
echo ""
echo "To archive a document later:"
echo "  git mv docs/architecture/old-doc.md $ARCHIVE_DIR/"
echo ""
echo "Current status:"
echo "  ✅ AGENTS.md v2.0: Master playbook (1680 lines)"
echo "  ✅ All specialized docs: Kept for detailed reference"
echo "  ✅ Cross-references: Maintained between documents"
echo ""
echo "================================================================================
                              ✅ COMPLETE
================================================================================
"
