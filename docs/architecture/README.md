# Architecture Documentation

## Overview

This directory contains architecture and design pattern documentation for the Derbent project. These documents define coding standards, design patterns, and best practices that ensure consistency and maintainability.

## Documentation Index

### Core Standards

1. **[Coding Standards](coding-standards.md)** - 📘 **START HERE**
   - General coding standards and naming conventions
   - Meta-guidelines for AI agents
   - Documentation hierarchy and structure
   - Cross-referencing rules
   - **Target**: All developers, AI agents

2. **[Validation Coding Rules](VALIDATION_CODING_RULES.md)** - ⚠️ **MANDATORY**
   - Unique name validation patterns
   - Base class validation method usage
   - Validation order and structure
   - Error message standards
   - **Target**: Service layer development, ALL developers/AI agents

3. **[UI, CSS, and Layout Coding Standards](ui-css-coding-standards.md)**
   - CSS patterns and best practices
   - Component styling and layout patterns
   - Color and icon standards
   - Responsive design patterns
   - **Target**: Frontend developers, UI work, AI agents

### Design Patterns

3. **[Entity Inheritance Patterns](entity-inheritance-patterns.md)**
   - Entity class hierarchies
   - Base class usage
   - Domain modeling patterns
   - **Target**: Domain model development

4. **[Service Layer Patterns](service-layer-patterns.md)**
   - Service class patterns
   - Business logic organization
   - Transaction management
   - **Target**: Business logic development

5. **[View Layer Patterns](view-layer-patterns.md)**
   - View/UI class patterns
   - Component structure
   - Event handling patterns
   - **Target**: UI/View development

### Specialized Patterns

6. **[Multi-User Singleton Advisory](multi-user-singleton-advisory.md)** - ⚠️ **CRITICAL**
   - Multi-user safety patterns
   - Service statelessness requirements
   - Session management
   - **Target**: Service development, concurrency

7. **[Bean Access Patterns](bean-access-patterns.md)**
   - Spring bean access patterns
   - Dependency injection
   - **Target**: Spring framework usage

8. **[CGrid Configuration Patterns](cgrid-configuration-patterns.md)**
   - Grid component configuration
   - Column setup patterns
   - **Target**: Grid/table development

9. **[Drag-Drop Component Pattern](drag-drop-component-pattern.md)**
   - Drag and drop implementation
   - Component interaction
   - **Target**: Interactive UI features

## Document Hierarchy for AI Agents

When generating code, AI agents should consult documentation in this order:

```
1. Specific Guidelines (for the task at hand)
   ├── UI/CSS work → ui-css-coding-standards.md
   ├── Component dev → ../development/component-coding-standards.md
   ├── Service layer → service-layer-patterns.md
   ├── View layer → view-layer-patterns.md
   └── Entity model → entity-inheritance-patterns.md

2. General Standards → coding-standards.md

3. Pattern-Specific Docs → (various pattern documents)

4. AI-Specific Guidelines → ../development/copilot-guidelines.md
```

## Quick Reference by Task

### Creating a New Entity
1. [Entity Inheritance Patterns](entity-inheritance-patterns.md)
2. [Coding Standards - Entity Class Structure](coding-standards.md#entity-class-structure)

### Creating a New Service
1. [Validation Coding Rules](VALIDATION_CODING_RULES.md) - **MANDATORY for all validation logic**
2. [Service Layer Patterns](service-layer-patterns.md)
3. [Multi-User Singleton Advisory](multi-user-singleton-advisory.md) - **CRITICAL**
4. [Coding Standards - Service Class Structure](coding-standards.md#service-class-structure)

### Creating a New View/UI Component
1. [View Layer Patterns](view-layer-patterns.md)
2. [UI, CSS, and Layout Coding Standards](ui-css-coding-standards.md)
3. [Component Coding Standards](../development/component-coding-standards.md)
4. [Coding Standards - View Class Structure](coding-standards.md#view-class-structure)

### Working with Grids
1. [CGrid Configuration Patterns](cgrid-configuration-patterns.md)
2. [UI, CSS, and Layout Coding Standards - Grid Standards](ui-css-coding-standards.md#grid-and-list-display-standards)

### Styling Components
1. [UI, CSS, and Layout Coding Standards](ui-css-coding-standards.md) - **PRIMARY**
2. [Component Coding Standards](../development/component-coding-standards.md)

### Using AI Assistants
1. [GitHub Copilot Guidelines](../development/copilot-guidelines.md)
2. [Coding Standards - Meta-Guidelines](coding-standards.md#meta-guidelines-using-this-documentation)

## Documentation Standards

### All Documents MUST Include

- **Clear Title**: Describes document scope
- **Target Audience**: Who should use this document
- **Overview Section**: Brief description of content
- **Related Documentation**: Links to related guidelines
- **Version and Date**: Last updated information

### Pattern Documentation Format

```markdown
### Pattern: [Pattern Name]

**When to Use**: [Describe scenario]

**✅ CORRECT**:
```java
// Example code
```

**❌ INCORRECT**:
```java
// Anti-pattern code
```

**Rule**: [Explicit rule statement]
```

### Cross-Referencing

- Use relative paths: `[Title](../category/document.md)`
- Link to specific sections: `[Title](document.md#section-name)`
- Avoid duplication: Reference instead of copying

## Contributing to Documentation

When updating guidelines:

1. **Follow the pattern format** defined in [Coding Standards - Meta-Guidelines](coding-standards.md#meta-guidelines-using-this-documentation)
2. **Add cross-references** to related documents
3. **Update version number** at bottom of document
4. **Update "Last Updated" date**
5. **Test with AI agents** to ensure patterns work
6. **Update this README** if adding new documents

## Related Documentation

### Development Guidelines
- [Component Coding Standards](../development/component-coding-standards.md)
- [GitHub Copilot Guidelines](../development/copilot-guidelines.md)
- [Multi-User Development Checklist](../development/multi-user-development-checklist.md)

### Implementation Patterns
- [Drag and Drop Pattern](../implementation/drag-and-drop-pattern.md)
- [Selection Event Pattern](../implementation/selection-event-pattern.md)
- [HasValue Pattern](../implementation/hasvalue-pattern.md)

### Testing
- [Playwright Testing Guidelines](../testing/)

---

**For Quick Start**: Begin with [Coding Standards](coding-standards.md) then consult specific pattern documents as needed.

**For AI Agents**: Start with the task-specific guideline (UI, Service, Entity, etc.) then fall back to general standards.

**Last Updated**: 2026-01-27
