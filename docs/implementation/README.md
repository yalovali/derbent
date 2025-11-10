# Implementation Documentation

This directory contains detailed implementation guides and design patterns for the Derbent project.

## 📂 Documentation Categories

### 🎨 Gantt Chart Implementation
**[GANTT_INDEX.md](GANTT_INDEX.md)** - Complete index for all Gantt chart documentation

The Gantt chart implementation includes:
- Design pattern documentation
- PlantUML architecture diagrams  
- Technical implementation guides
- Visual design guides
- Call hierarchy documentation

**Start here**: [GANTT_DESIGN_PATTERN.md](GANTT_DESIGN_PATTERN.md) for complete Gantt design patterns.

### 🔐 Authentication & Authorization
- [AUTHENTICATION_CALL_HIERARCHY.md](AUTHENTICATION_CALL_HIERARCHY.md) - Authentication call flow
- [COMPANY_LOGIN_PATTERN.md](COMPANY_LOGIN_PATTERN.md) - Company login implementation
- [LOGIN_AUTHENTICATION_MECHANISM.md](LOGIN_AUTHENTICATION_MECHANISM.md) - Authentication mechanism details

### 🔄 Workflow & Status Management
- [WORKFLOW_ENTITY_PATTERN.md](WORKFLOW_ENTITY_PATTERN.md) - Workflow entity pattern
- [WORKFLOW_STATUS_RELATION_PATTERN.md](WORKFLOW_STATUS_RELATION_PATTERN.md) - Status relation pattern
- [WORKFLOW_QUICK_REFERENCE.md](WORKFLOW_QUICK_REFERENCE.md) - Quick reference guide
- [workflow-status-relation-roles-field.md](workflow-status-relation-roles-field.md) - Roles field implementation

### 🗑️ Data Management
- [CHECK_DELETE_ALLOWED_PATTERN.md](CHECK_DELETE_ALLOWED_PATTERN.md) - Delete validation pattern
- [DEPENDENCY_CHECKING_SYSTEM.md](DEPENDENCY_CHECKING_SYSTEM.md) - Dependency checking
- [CRUD-Operations-Guide.md](CRUD-Operations-Guide.md) - CRUD operations guide

### 🎯 UI Patterns
- [selection-event-pattern.md](selection-event-pattern.md) - Selection event handling
- [Grid-Selection-After-Save-Pattern.md](Grid-Selection-After-Save-Pattern.md) - Grid selection after save
- [PageService-Pattern.md](PageService-Pattern.md) - Page service pattern
- [HIERARCHICAL_MENU_ORDER.md](HIERARCHICAL_MENU_ORDER.md) - Menu hierarchy

### 🧪 Testing
- [PLAYWRIGHT_BEST_PRACTICES.md](PLAYWRIGHT_BEST_PRACTICES.md) - Playwright testing best practices
- [PLAYWRIGHT_TEST_GUIDE.md](PLAYWRIGHT_TEST_GUIDE.md) - Complete test guide
- [PLAYWRIGHT_TEST_GUIDELINES.md](PLAYWRIGHT_TEST_GUIDELINES.md) - Testing guidelines

### 🔧 Architecture & Patterns
- [CIRCULAR_DEPENDENCY_RESOLUTION.md](CIRCULAR_DEPENDENCY_RESOLUTION.md) - Resolving circular dependencies

## 🎯 Quick Start Guides

### For New Developers
1. Start with [GANTT_INDEX.md](GANTT_INDEX.md) for Gantt features
2. Review [WORKFLOW_QUICK_REFERENCE.md](WORKFLOW_QUICK_REFERENCE.md) for workflow concepts
3. Check [CRUD-Operations-Guide.md](CRUD-Operations-Guide.md) for data operations

### For Frontend Developers
1. [GANTT_INDEX.md](GANTT_INDEX.md) - Gantt UI implementation
2. [selection-event-pattern.md](selection-event-pattern.md) - UI event handling
3. [Grid-Selection-After-Save-Pattern.md](Grid-Selection-After-Save-Pattern.md) - Grid patterns

### For Backend Developers
1. [WORKFLOW_ENTITY_PATTERN.md](WORKFLOW_ENTITY_PATTERN.md) - Entity patterns
2. [CHECK_DELETE_ALLOWED_PATTERN.md](CHECK_DELETE_ALLOWED_PATTERN.md) - Data validation
3. [DEPENDENCY_CHECKING_SYSTEM.md](DEPENDENCY_CHECKING_SYSTEM.md) - Dependencies

### For Testers
1. [PLAYWRIGHT_TEST_GUIDE.md](PLAYWRIGHT_TEST_GUIDE.md) - Complete testing guide
2. [PLAYWRIGHT_BEST_PRACTICES.md](PLAYWRIGHT_BEST_PRACTICES.md) - Best practices
3. [PLAYWRIGHT_TEST_GUIDELINES.md](PLAYWRIGHT_TEST_GUIDELINES.md) - Guidelines

## 📊 Architecture Documentation

See [../architecture/](../architecture/) for:
- [coding-standards.md](../architecture/coding-standards.md) - Project coding standards
- [service-layer-patterns.md](../architecture/service-layer-patterns.md) - Service layer patterns
- [view-layer-patterns.md](../architecture/view-layer-patterns.md) - View layer patterns
- [entity-inheritance-patterns.md](../architecture/entity-inheritance-patterns.md) - Entity patterns

## 🖼️ Diagrams

Architecture diagrams are available in [../diagrams/](../diagrams/):
- Gantt call hierarchy diagrams
- Component structure diagrams
- Data flow diagrams
- UI region diagrams

See [../diagrams/README.md](../diagrams/README.md) for details.

## 📝 Document Conventions

All implementation documents follow these conventions:
- **Pattern Documents**: Describe reusable design patterns with examples
- **Guide Documents**: Step-by-step instructions for specific tasks
- **Reference Documents**: Quick lookup information and checklists
- **Call Hierarchy Documents**: Sequence diagrams and flow descriptions

## 🔄 Contributing Documentation

When adding new implementation patterns:
1. Create a new `.md` file in this directory
2. Follow existing document structure
3. Add to this README under appropriate category
4. Create PlantUML diagrams if helpful (in `../diagrams/`)
5. Link to related architecture documentation

## 📞 Related Resources

- [Main Project README](../../README.md) - Project overview
- [Architecture Documentation](../architecture/) - Design principles
- [Testing Documentation](../testing/) - Test strategies
- [Features Documentation](../features/) - Feature descriptions

---

**Need help finding documentation?** Check the category that matches your task above, or search this directory for keywords related to your work.
