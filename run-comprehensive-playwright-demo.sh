#!/bin/bash

# Comprehensive Playwright Test Runner - Demonstrates ALL view testing capabilities
# This script showcases the enhanced testing that covers all views, status, and type views

set -e

echo "🌟 Comprehensive Playwright Test Demonstration"
echo "=============================================="
echo "This script demonstrates the enhanced Playwright testing capabilities"
echo "that now cover ALL views, including status and type views."
echo ""

# Function to show test overview
show_test_overview() {
    echo "📊 Test Coverage Overview:"
    echo "========================="
    echo "• Main Business Views: 8 views (Projects, Activities, Meetings, etc.)"
    echo "• Status & Type Views: 9 views (ActivityStatus, DecisionType, etc.)"
    echo "• Administrative Views: 3 views (CompanySettings, SystemSettings, Dashboard)"
    echo "• Kanban Board Views: 3 views (Activity, Meeting, Generic)"
    echo "• Example/Demo Views: 4 views (Hierarchical Menu, Search Demo, etc.)"
    echo "• Detail Views: 1 view (ProjectDetails)"
    echo "• TOTAL: 28 comprehensive views tested"
    echo ""
}

# Function to run demonstration tests
run_demo_tests() {
    echo "🧪 Running comprehensive view testing demonstrations..."
    echo ""
    
    echo "1️⃣ Running mock tests to demonstrate comprehensive view coverage..."
    ./run-playwright-tests.sh mock
    
    echo ""
    echo "2️⃣ Checking generated screenshots..."
    if [ -d "target/screenshots" ]; then
        screenshot_count=$(find target/screenshots -name "*.png" | wc -l)
        accessibility_count=$(find target/screenshots -name "accessibility-*view*" | wc -l)
        mock_count=$(find target/screenshots -name "mock-*view*" | wc -l)
        workflow_count=$(find target/screenshots -name "workflow-*" | wc -l)
        
        echo "📸 Screenshot Summary:"
        echo "   Total screenshots: $screenshot_count"
        echo "   Accessibility views: $accessibility_count/28 views"
        echo "   Mock view tests: $mock_count/28 views" 
        echo "   Workflow screenshots: $workflow_count"
        echo ""
        
        if [ $accessibility_count -eq 28 ] && [ $mock_count -eq 28 ]; then
            echo "✅ SUCCESS: All 28 views have been tested and documented!"
        else
            echo "⚠️ Warning: Some views may not have been fully covered"
        fi
    fi
    
    echo ""
    echo "3️⃣ Available test categories:"
    echo "   • comprehensive  - Test ALL views at once"
    echo "   • status-types   - Test status and type configuration views"
    echo "   • main-views     - Test main business entity views"
    echo "   • admin-views    - Test administrative and system views"
    echo "   • kanban-views   - Test Kanban board views"
    echo ""
    
    echo "📝 Example commands:"
    echo "   ./run-playwright-tests.sh comprehensive  # Test all 28 views"
    echo "   ./run-playwright-tests.sh status-types   # Test 9 status/type views"
    echo "   ./run-playwright-tests.sh main-views     # Test 8 main business views"
    echo ""
}

# Function to show view details
show_view_details() {
    echo "📋 Detailed View Coverage:"
    echo "=========================="
    echo ""
    echo "🏢 Main Business Views (8):"
    echo "   • CProjectsView - Project management"
    echo "   • CActivitiesView - Activity tracking"
    echo "   • CMeetingsView - Meeting management"
    echo "   • CDecisionsView - Decision tracking"
    echo "   • CUsersView - User management"
    echo "   • COrdersView - Order management"
    echo "   • CRiskView - Risk management"
    echo "   • CCompanyView - Company information"
    echo ""
    echo "⚙️ Status & Type Configuration Views (9):"
    echo "   • CActivityStatusView - Activity status config"
    echo "   • CActivityTypeView - Activity type config"
    echo "   • CDecisionStatusView - Decision status config"
    echo "   • CDecisionTypeView - Decision type config"
    echo "   • CMeetingStatusView - Meeting status config"
    echo "   • CMeetingTypeView - Meeting type config"
    echo "   • CUserTypeView - User type config"
    echo "   • CRiskStatusView - Risk status config"
    echo "   • CCommentPriorityView - Comment priority config"
    echo ""
    echo "🔧 Administrative Views (3):"
    echo "   • CCompanySettingsView - Company settings"
    echo "   • CSystemSettingsView - System configuration"
    echo "   • CDashboardView - Main dashboard"
    echo ""
    echo "📋 Kanban Board Views (3):"
    echo "   • CActivityKanbanBoardView - Activity kanban"
    echo "   • CMeetingKanbanBoardView - Meeting kanban"
    echo "   • CGenericActivityKanbanBoardView - Generic kanban"
    echo ""
    echo "🎯 Example & Demo Views (4):"
    echo "   • CExampleHierarchicalMenuView - Menu demo"
    echo "   • CExampleSettingsView - Settings demo"
    echo "   • CSearchDemoView - Search functionality demo"
    echo "   • CSearchShowcaseView - Search showcase"
    echo ""
    echo "📄 Detail Views (1):"
    echo "   • CProjectDetailsView - Project detail view"
}

# Main execution
main() {
    local command=${1:-demo}
    
    case $command in
        "demo")
            show_test_overview
            run_demo_tests
            ;;
            
        "details")
            show_view_details
            ;;
            
        "overview")
            show_test_overview
            ;;
            
        "help"|*)
            echo "Usage: $0 [option]"
            echo ""
            echo "Options:"
            echo "  demo        Run comprehensive testing demonstration (default)"
            echo "  details     Show detailed view breakdown"
            echo "  overview    Show test coverage overview"
            echo "  help        Show this help message"
            echo ""
            echo "This script demonstrates the comprehensive Playwright testing"
            echo "implementation that covers all views, status views, and type views."
            ;;
    esac
}

# Run the main function
main "$@"