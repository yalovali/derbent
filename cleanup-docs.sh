#!/bin/bash

# Documentation Cleanup Script
# Removes old implementation summaries, fix reports, and task completion docs
# Keeps only essential coding guidelines and patterns for AI agents

echo "📚 Derbent Documentation Cleanup"
echo "=================================="
echo ""

# Create archive directory
ARCHIVE_DIR="docs/archive/deprecated-2026-01"
mkdir -p "$ARCHIVE_DIR"

echo "✅ Created archive directory: $ARCHIVE_DIR"
echo ""

# Move root-level implementation/fix summary documents
echo "📦 Archiving root-level implementation summaries..."
mv AVATAR_GENERATION_DEMO.md "$ARCHIVE_DIR/" 2>/dev/null
mv CALCULATED_FIELDS_IMPLEMENTATION_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv CIRCULAR_DEPENDENCY_FIX.md "$ARCHIVE_DIR/" 2>/dev/null
mv COMBOBOX_BACKGROUND_FIX.md "$ARCHIVE_DIR/" 2>/dev/null
mv COMBOBOX_FIX_DETAILS.md "$ARCHIVE_DIR/" 2>/dev/null
mv COMPONENT_NOTIFICATION_PROPAGATION_IMPLEMENTATION.md "$ARCHIVE_DIR/" 2>/dev/null
mv COMPONENT_REFACTORING_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv COMPONENT_TYPE_REFACTORING.md "$ARCHIVE_DIR/" 2>/dev/null
mv COMPREHENSIVE_CODE_REVIEW_2026-01.md "$ARCHIVE_DIR/" 2>/dev/null
mv COPILOT_INSTRUCTIONS_ENHANCEMENT_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv CSS_CODING_GUIDELINES_IMPLEMENTATION.md "$ARCHIVE_DIR/" 2>/dev/null
mv DEFAULT_ORDERING_IMPLEMENTATION.md "$ARCHIVE_DIR/" 2>/dev/null
mv DRAG_DROP_FIX_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv EPIC_FEATURE_STORY_ENHANCEMENT_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv FAIL_FAST_TEST_RULES.md "$ARCHIVE_DIR/" 2>/dev/null
mv FINAL_TEST_STATUS.md "$ARCHIVE_DIR/" 2>/dev/null
mv FONT_SIZE_SETTINGS_IMPLEMENTATION.md "$ARCHIVE_DIR/" 2>/dev/null
mv FONT_SIZE_SETTINGS_UX_GUIDE.md "$ARCHIVE_DIR/" 2>/dev/null
mv FONT_SIZE_SETTINGS_VISUAL_DEMO.md "$ARCHIVE_DIR/" 2>/dev/null
mv GRID_EXPANSION_FIX.md "$ARCHIVE_DIR/" 2>/dev/null
mv GRID_EXPANSION_VISUAL.md "$ARCHIVE_DIR/" 2>/dev/null
mv ICON_FIX_IMPLEMENTATION.md "$ARCHIVE_DIR/" 2>/dev/null
mv ICON_IMPLEMENTATION_COMPLETE.md "$ARCHIVE_DIR/" 2>/dev/null
mv ICON_UPDATE_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv IMPLEMENTATION_COMPLETE.md "$ARCHIVE_DIR/" 2>/dev/null
mv IMPLEMENTATION_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv IMPLEMENTATION_SUMMARY_OLD.md "$ARCHIVE_DIR/" 2>/dev/null
mv INDIVIDUAL_ENTITY_TESTING_PATTERN.md "$ARCHIVE_DIR/" 2>/dev/null
mv KANBAN_DRAG_DROP_FIX.md "$ARCHIVE_DIR/" 2>/dev/null
mv KANBAN_DRAG_DROP_VISUAL_UPDATE_FIX.md "$ARCHIVE_DIR/" 2>/dev/null
mv KANBAN_DROP_EVENT_FIX.md "$ARCHIVE_DIR/" 2>/dev/null
mv KANBAN_FILTER_FIX.md "$ARCHIVE_DIR/" 2>/dev/null
mv KANBAN_IMPROVEMENTS_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv LAZY_LOADING_FIX_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv PARENT_CHILD_HIERARCHY_IMPLEMENTATION_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv PERSISTENCE_SIMPLIFICATION_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv PLAYWRIGHT_TEST_CHANGES.md "$ARCHIVE_DIR/" 2>/dev/null
mv PLAYWRIGHT_TEST_REFACTORING.md "$ARCHIVE_DIR/" 2>/dev/null
mv QUICK_TEST_GUIDE.md "$ARCHIVE_DIR/" 2>/dev/null
mv REFACTORING_NOTES.md "$ARCHIVE_DIR/" 2>/dev/null
mv REFACTORING_RESPONSIBLE_TO_ASSIGNEDTO.md "$ARCHIVE_DIR/" 2>/dev/null
mv REFACTORING_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv SOLUTION_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv SPRINT_BACKLOG_IMPLEMENTATION.md "$ARCHIVE_DIR/" 2>/dev/null
mv SPRINT_IMPLEMENTATION_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv SPRINT_WIDGET_CHANGES.md "$ARCHIVE_DIR/" 2>/dev/null
mv SPRINT_WIDGET_ENHANCEMENT.md "$ARCHIVE_DIR/" 2>/dev/null
mv SPRINT_WIDGET_ENHANCEMENT_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv STATUS_INITIALIZATION_FIX_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv STATUS_INITIALIZATION_REFACTORING.md "$ARCHIVE_DIR/" 2>/dev/null
mv STORY_POINTS_IMPLEMENTATION.md "$ARCHIVE_DIR/" 2>/dev/null
mv STORY_POINTS_VISUAL_GUIDE.md "$ARCHIVE_DIR/" 2>/dev/null
mv SVG_ICON_KEY_TAKEAWAYS.md "$ARCHIVE_DIR/" 2>/dev/null
mv SVG_ICON_SOLUTION.md "$ARCHIVE_DIR/" 2>/dev/null
mv SVG_TESTING_GUIDE.md "$ARCHIVE_DIR/" 2>/dev/null
mv TASK_COMPLETION_GRID_EXPANSION.md "$ARCHIVE_DIR/" 2>/dev/null
mv TASK_COMPLETION_STORY_POINTS.md "$ARCHIVE_DIR/" 2>/dev/null
mv TASK_COMPLETION_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv TEAM_REFACTORING_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv TEST_MANAGEMENT_IMPLEMENTATION_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv TEST_NEW_ENTITIES.md "$ARCHIVE_DIR/" 2>/dev/null
mv USER_ICON_VISIBILITY_FIX.md "$ARCHIVE_DIR/" 2>/dev/null
mv VISUAL_COMPARISON.md "$ARCHIVE_DIR/" 2>/dev/null
mv WIDGET_STATE_PRESERVATION_IMPLEMENTATION.md "$ARCHIVE_DIR/" 2>/dev/null
mv WIDGET_STATE_PRESERVATION_PATTERN.md "$ARCHIVE_DIR/" 2>/dev/null

echo "✅ Archived old implementation summaries"
echo ""

# Archive old architecture docs (now consolidated)
echo "📦 Archiving old architecture documents..."
cd docs/architecture
mv bean-access-patterns.md "$ARCHIVE_DIR/" 2>/dev/null
mv cgrid-configuration-patterns.md "$ARCHIVE_DIR/" 2>/dev/null
mv code-organization-cleanup-remaining.md "$ARCHIVE_DIR/" 2>/dev/null
mv coding-standards.md "$ARCHIVE_DIR/" 2>/dev/null
mv component-utility-reference.md "$ARCHIVE_DIR/" 2>/dev/null
mv database-constraints-audit.md "$ARCHIVE_DIR/" 2>/dev/null
mv drag-drop-component-pattern.md "$ARCHIVE_DIR/" 2>/dev/null
mv entity-dialog-coding-standards.md "$ARCHIVE_DIR/" 2>/dev/null
mv entity-inheritance-patterns.md "$ARCHIVE_DIR/" 2>/dev/null
mv entity-selection-component-design.md "$ARCHIVE_DIR/" 2>/dev/null
mv form-population-pattern.md "$ARCHIVE_DIR/" 2>/dev/null
mv interface-hierarchy-before-after.md "$ARCHIVE_DIR/" 2>/dev/null
mv interface-refactoring-2025-11.md "$ARCHIVE_DIR/" 2>/dev/null
mv kanban-and-refresh-patterns.md "$ARCHIVE_DIR/" 2>/dev/null
mv method-placement-guidelines.md "$ARCHIVE_DIR/" 2>/dev/null
mv multi-user-singleton-advisory.md "$ARCHIVE_DIR/" 2>/dev/null
mv service-layer-patterns.md "$ARCHIVE_DIR/" 2>/dev/null
mv ui-css-coding-standards.md "$ARCHIVE_DIR/" 2>/dev/null
mv value-persistence-pattern.md "$ARCHIVE_DIR/" 2>/dev/null
mv view-layer-patterns.md "$ARCHIVE_DIR/" 2>/dev/null
mv DATABASE_COMPOSITION_PATTERN.md "$ARCHIVE_DIR/" 2>/dev/null
mv STATUS_INITIALIZATION_PATTERN.md "$ARCHIVE_DIR/" 2>/dev/null
mv VALIDATION_PATTERN.md "$ARCHIVE_DIR/" 2>/dev/null

cd ../..
echo "✅ Archived old architecture documents"
echo ""

# Remove entire archived-tasks directory (already archived)
echo "🗑️  Removing archived-tasks directory..."
rm -rf docs/archived-tasks
echo "✅ Removed archived-tasks"
echo ""

# Remove BAB-specific docs (specific project)
echo "🗑️  Removing BAB project docs..."
rm -rf docs/bab
echo "✅ Removed BAB project docs"
echo ""

# Archive old testing reports
echo "📦 Archiving old testing reports..."
cd docs/testing
mv BAB_ALL_TESTS_FINAL_REPORT.md "$ARCHIVE_DIR/" 2>/dev/null
mv BAB_GATEWAY_TEST_REPORT.md "$ARCHIVE_DIR/" 2>/dev/null
mv BAB_MENU_CRUD_FINAL_REPORT.md "$ARCHIVE_DIR/" 2>/dev/null
mv BUG_FIXES_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv CRITICAL_BUGS_DISCOVERED.md "$ARCHIVE_DIR/" 2>/dev/null
mv DEPENDENCY_CHECKING_TESTS.md "$ARCHIVE_DIR/" 2>/dev/null
mv GENERIC_CRUD_TEST_ANALYSIS.md "$ARCHIVE_DIR/" 2>/dev/null
mv GENERIC_TESTS_BAB_CONFIRMED_WORKING.md "$ARCHIVE_DIR/" 2>/dev/null
mv GENERIC_TESTS_PROFILE_AUTO_DETECTION_SUCCESS.md "$ARCHIVE_DIR/" 2>/dev/null
mv PLAYWRIGHT_CRUD_TEST_IMPLEMENTATION_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv PLAYWRIGHT_TEST_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv crud-operations-validation-report.md "$ARCHIVE_DIR/" 2>/dev/null

cd ../..
echo "✅ Archived old testing reports"
echo ""

# Archive old implementation docs (specific features)
echo "📦 Archiving old implementation docs..."
cd docs/implementation
mv AGILE_SPRINT_DRAG_DROP_PATTERN.md "$ARCHIVE_DIR/" 2>/dev/null
mv AUTHENTICATION_CALL_HIERARCHY.md "$ARCHIVE_DIR/" 2>/dev/null
mv CIRCULAR_DEPENDENCY_RESOLUTION.md "$ARCHIVE_DIR/" 2>/dev/null
mv COMPANY_LOGIN_PATTERN.md "$ARCHIVE_DIR/" 2>/dev/null
mv DEPENDENCY_CHECKING_SYSTEM.md "$ARCHIVE_DIR/" 2>/dev/null
mv ENTITY_TYPE_FILTER_UNIFICATION.md "$ARCHIVE_DIR/" 2>/dev/null
mv GANTT-TIMELINE-COMPLETE-SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv GANTT_DESIGN_PATTERN.md "$ARCHIVE_DIR/" 2>/dev/null
mv GANTT_INDEX.md "$ARCHIVE_DIR/" 2>/dev/null
mv HIERARCHICAL_MENU_ORDER.md "$ARCHIVE_DIR/" 2>/dev/null
mv KANBAN_POSTIT_EVENT_FLOW.md "$ARCHIVE_DIR/" 2>/dev/null
mv LOGIN_AUTHENTICATION_MECHANISM.md "$ARCHIVE_DIR/" 2>/dev/null
mv OWNER_NOTIFICATION_INTERFACES.md "$ARCHIVE_DIR/" 2>/dev/null
mv PATTERN_VALIDATION_REPORT.md "$ARCHIVE_DIR/" 2>/dev/null
mv centralized-component-map.md "$ARCHIVE_DIR/" 2>/dev/null
mv gantt-timeline-header.md "$ARCHIVE_DIR/" 2>/dev/null
mv gantt-timeline-visual-guide.md "$ARCHIVE_DIR/" 2>/dev/null
mv ihasdragcontrol-refactoring-summary.md "$ARCHIVE_DIR/" 2>/dev/null
mv ihasdragcontrol-unified-api.md "$ARCHIVE_DIR/" 2>/dev/null
mv status-change-behavior-fix.md "$ARCHIVE_DIR/" 2>/dev/null
mv workflow-status-relation-roles-field.md "$ARCHIVE_DIR/" 2>/dev/null

cd ../..
echo "✅ Archived old implementation docs"
echo ""

# Archive old development docs (already in master guide)
echo "📦 Archiving old development docs..."
cd docs/development
mv COPILOT_CLI_CONFIGURATION_GUIDE.md "$ARCHIVE_DIR/" 2>/dev/null
mv DATABASE_SCHEMA_FIX.md "$ARCHIVE_DIR/" 2>/dev/null
mv MULTI_USER_QUICK_REFERENCE.md "$ARCHIVE_DIR/" 2>/dev/null
mv ORGANIZATION_IMPROVEMENTS_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv ai-tools-guide.md "$ARCHIVE_DIR/" 2>/dev/null
mv component-compliance-report.md "$ARCHIVE_DIR/" 2>/dev/null
mv component-entity-selection-usage.md "$ARCHIVE_DIR/" 2>/dev/null
mv documentation-guide.md "$ARCHIVE_DIR/" 2>/dev/null
mv drag-drop-event-binding.md "$ARCHIVE_DIR/" 2>/dev/null
mv drag-drop-notification-propagation.md "$ARCHIVE_DIR/" 2>/dev/null
mv multi-user-development-checklist.md "$ARCHIVE_DIR/" 2>/dev/null
mv sprint-item-lifecycle.md "$ARCHIVE_DIR/" 2>/dev/null
mv universal-filter-toolbar-framework.md "$ARCHIVE_DIR/" 2>/dev/null
mv value-persistence-code-examples.md "$ARCHIVE_DIR/" 2>/dev/null
mv value-persistence-implementation.md "$ARCHIVE_DIR/" 2>/dev/null
mv value-persistence-simplified.md "$ARCHIVE_DIR/" 2>/dev/null

cd ../..
echo "✅ Archived old development docs"
echo ""

# Archive old feature docs
echo "📦 Archiving old feature docs..."
cd docs/features
mv ADVANCED_CALLBACK_PATTERNS.md "$ARCHIVE_DIR/" 2>/dev/null
mv CALLBACK_PATTERN_FLOW.md "$ARCHIVE_DIR/" 2>/dev/null
mv IMPLEMENTATION_SUMMARY.md "$ARCHIVE_DIR/" 2>/dev/null
mv LOGIN_SCREEN_PROFILE_DISPLAY.md "$ARCHIVE_DIR/" 2>/dev/null
mv PARENT_CHILD_HIERARCHY.md "$ARCHIVE_DIR/" 2>/dev/null
mv component-callback-pattern.md "$ARCHIVE_DIR/" 2>/dev/null
mv message-with-details-dialog-visual.md "$ARCHIVE_DIR/" 2>/dev/null
mv message-with-details-dialog.md "$ARCHIVE_DIR/" 2>/dev/null

cd ../..
echo "✅ Archived old feature docs"
echo ""

# Archive old fix docs
echo "📦 Archiving old fix docs..."
cd docs/fixes
mv ccrudtoolbar-combobox-fix.md "$ARCHIVE_DIR/" 2>/dev/null
mv drag-drop-propagation-fix.md "$ARCHIVE_DIR/" 2>/dev/null
mv storedobject-charts-troubleshooting.md "$ARCHIVE_DIR/" 2>/dev/null
mv vaadin-jar-resources-windows-fix.md "$ARCHIVE_DIR/" 2>/dev/null

cd ../..
echo "✅ Archived old fix docs"
echo ""

# Archive old component docs
echo "📦 Archiving old component docs..."
cd docs/components
mv CComponentItemDetails.md "$ARCHIVE_DIR/" 2>/dev/null
mv CComponentListSelection-usage.md "$ARCHIVE_DIR/" 2>/dev/null
mv UNIVERSAL_ENTITY_TYPE_FILTER.md "$ARCHIVE_DIR/" 2>/dev/null

cd ../..
echo "✅ Archived old component docs"
echo ""

echo "=================================="
echo "✅ Cleanup Complete!"
echo ""
echo "📊 Summary:"
echo "- Moved ~120 old implementation/fix summaries to archive"
echo "- Removed archived-tasks directory"
echo "- Removed BAB project-specific docs"
echo "- Archived old architecture docs (now in master guide)"
echo ""
echo "📁 Archived to: $ARCHIVE_DIR"
echo ""
echo "✅ Keeping essential docs:"
echo "  - docs/DERBENT_CODING_MASTER_GUIDE.md (PRIMARY)"
echo "  - docs/architecture/ENTITY_INHERITANCE_AND_DESIGN_PATTERNS.md"
echo "  - docs/architecture/LAZY_LOADING_BEST_PRACTICES.md"
echo "  - docs/architecture/CHILD_ENTITY_PATTERNS.md"
echo "  - docs/architecture/NEW_ENTITY_COMPLETE_CHECKLIST.md"
echo "  - docs/development/copilot-guidelines.md"
echo "  - docs/development/component-coding-standards.md"
echo "  - docs/implementation/WORKFLOW_ENTITY_PATTERN.md"
echo "  - docs/implementation/drag-drop-unified-pattern.md"
echo "  - docs/testing/PLAYWRIGHT_USAGE.md"
echo "  - docs/testing/RECENT_FEATURES_CRUD_TEST_PATTERNS.md"
echo "  - Root: README.md, CONTRIBUTING.md, CODE_OF_CONDUCT.md, SECURITY.md"
echo "  - Root: TEST_MANAGEMENT_IMPLEMENTATION_COMPLETE.md"
echo "  - Root: TEST_MODULE_AUDIT_COMPLETE.md"
echo "  - Root: TESTING_RULES.md, PLAYWRIGHT_TESTING_GUIDE.md"
echo "  - Root: AGENTS.md"
echo ""
