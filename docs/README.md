# Derbent Documentation

## Overview

This directory contains comprehensive documentation for the Derbent project. The documentation is organized by topic and purpose to support developers, AI agents, and code reviewers.

## Directory Structure

```
docs/
├── architecture/          # 📐 Architecture and design patterns
├── development/          # 🛠️ Development workflows and guidelines
├── implementation/       # 🔧 Specific implementation patterns
├── testing/              # 🧪 Testing guidelines and patterns
├── features/             # ✨ Feature-specific documentation
├── components/           # 🎨 Component library documentation
├── configuration/        # ⚙️ Configuration guides
├── fixes/                # 🐛 Bug fix documentation
└── references/           # 📚 Reference materials
```

## Getting Started

### For Developers

**New to the project?**
1. Start with [Architecture README](architecture/README.md)
2. Read [Coding Standards](architecture/coding-standards.md)
3. Check [GitHub Copilot Guidelines](development/copilot-guidelines.md) if using AI assistants

**Working on specific tasks?**
- UI/Styling → [UI, CSS, and Layout Coding Standards](architecture/ui-css-coding-standards.md)
- Components → [Component Coding Standards](development/component-coding-standards.md)
- Services → [Service Layer Patterns](architecture/service-layer-patterns.md)
- Entities → [Entity Inheritance Patterns](architecture/entity-inheritance-patterns.md)
- Testing → [Testing Guidelines](testing/)

### For AI Agents (GitHub Copilot, Codex)

**Documentation Hierarchy** (consult in this order):

1. **Task-Specific Guidelines**
   - UI work → [ui-css-coding-standards.md](architecture/ui-css-coding-standards.md)
   - Component → [component-coding-standards.md](development/component-coding-standards.md)
   - Service → [service-layer-patterns.md](architecture/service-layer-patterns.md)

2. **General Standards**
   - [coding-standards.md](architecture/coding-standards.md)

3. **AI-Specific Guidelines**
   - [copilot-guidelines.md](development/copilot-guidelines.md)

**Important**: Read the [Meta-Guidelines](architecture/coding-standards.md#meta-guidelines-using-this-documentation) section in coding-standards.md for AI agent usage rules.

## Core Documentation

### Essential Reading

| Document | Purpose | Priority |
|----------|---------|----------|
| [Coding Standards](architecture/coding-standards.md) | General coding standards and conventions | 🔴 **HIGH** |
| [UI, CSS, and Layout Standards](architecture/ui-css-coding-standards.md) | UI component styling and layout patterns | 🔴 **HIGH** |
| [Component Coding Standards](development/component-coding-standards.md) | Component development rules | 🟡 **MEDIUM** |
| [GitHub Copilot Guidelines](development/copilot-guidelines.md) | AI assistant best practices | 🟡 **MEDIUM** |
| [Multi-User Singleton Advisory](architecture/multi-user-singleton-advisory.md) | Service statelessness (CRITICAL) | 🔴 **HIGH** |

### Architecture Patterns

| Document | Description |
|----------|-------------|
| [Entity Inheritance Patterns](architecture/entity-inheritance-patterns.md) | Domain model hierarchies |
| [Service Layer Patterns](architecture/service-layer-patterns.md) | Business logic organization |
| [View Layer Patterns](architecture/view-layer-patterns.md) | UI/View class patterns |
| [CGrid Configuration Patterns](architecture/cgrid-configuration-patterns.md) | Grid component setup |
| [Drag-Drop Component Pattern](architecture/drag-drop-component-pattern.md) | Interactive UI patterns |

### Development Workflows

| Document | Description |
|----------|-------------|
| [Multi-User Development Checklist](development/multi-user-development-checklist.md) | Multi-user safety checks |
| [Workflow Status Change Pattern](development/workflow-status-change-pattern.md) | Status management |
| [Calculated Fields Pattern](development/calculated-fields-pattern.md) | Computed field patterns |

## Quick Reference Guides

### By Task Type

**Creating New Components**
1. Choose component type (Entity, Service, View)
2. Consult relevant pattern document
3. Follow checklist in document
4. Reference [Coding Standards](architecture/coding-standards.md) for naming

**Styling UI Components**
1. Check [UI, CSS, and Layout Standards](architecture/ui-css-coding-standards.md)
2. Use Lumo variables for spacing/colors
3. Use C-prefixed components only
4. Apply factory methods for buttons

**Working with Data**
1. Entity design → [Entity Inheritance Patterns](architecture/entity-inheritance-patterns.md)
2. Business logic → [Service Layer Patterns](architecture/service-layer-patterns.md)
3. Multi-user safety → [Multi-User Singleton Advisory](architecture/multi-user-singleton-advisory.md)

**Testing**
1. Unit tests → Standard JUnit patterns
2. UI tests → [Playwright Testing Guidelines](testing/)
3. Integration tests → [Testing Documentation](testing/)

## Documentation Standards

### Every Document MUST Include

- ✅ Clear title describing scope
- ✅ Target audience section
- ✅ Overview/introduction
- ✅ Related documentation links
- ✅ Version and last updated date

### Pattern Documentation Format

Use this format for documenting code patterns:

```markdown
### Pattern: [Name]

**When to Use**: [Scenario]

**✅ CORRECT**:
```java
// Good example
```

**❌ INCORRECT**:
```java
// Anti-pattern
```

**Rule**: [Clear rule statement]
```

## Finding Documentation

### By Feature

- **Kanban Board**: [Kanban Implementation](features/)
- **Gantt Chart**: [Gantt Implementation](features/)
- **Sprint Management**: [Sprint Documentation](features/)
- **User Management**: [User System](features/)

### By Component

- **Buttons**: [UI Standards - Button Patterns](architecture/ui-css-coding-standards.md#button-creation-patterns)
- **Grids**: [CGrid Configuration](architecture/cgrid-configuration-patterns.md)
- **Forms**: [Component Standards](development/component-coding-standards.md)
- **Dialogs**: [Component Standards](development/component-coding-standards.md)

### By Problem

- **Multi-user issues**: [Multi-User Singleton Advisory](architecture/multi-user-singleton-advisory.md)
- **Styling issues**: [UI, CSS, and Layout Standards](architecture/ui-css-coding-standards.md)
- **Component errors**: [Component Coding Standards](development/component-coding-standards.md)
- **Service errors**: [Service Layer Patterns](architecture/service-layer-patterns.md)

## Contributing to Documentation

### Adding New Documentation

1. Choose appropriate directory (`architecture/`, `development/`, etc.)
2. Follow the documentation standards format
3. Add cross-references to related documents
4. Update relevant README files
5. Test with AI agents if applicable

### Updating Existing Documentation

1. Increment version number
2. Update "Last Updated" date
3. Verify cross-references still valid
4. Update related documents if needed
5. Test changes with AI agents

### Documentation Review Checklist

- [ ] Clear title and audience
- [ ] Overview section present
- [ ] Examples use ✅/❌ format
- [ ] Cross-references to related docs
- [ ] Version and date updated
- [ ] Follows naming conventions
- [ ] AI-agent friendly format

## Tools and References

### External Documentation

- [Vaadin Documentation](https://vaadin.com/docs)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [JPA Documentation](https://docs.oracle.com/javaee/7/tutorial/persistence-intro.htm)

### Internal Tools

- [Database Query Debugging](DATABASE_QUERY_DEBUGGING.md)
- [GraphViz Documentation Guide](GRAPHVIZ_DOCUMENTATION_GUIDE.md)
- [Doxygen Usage](DOXYGEN_USAGE.md)

## Need Help?

1. **Can't find what you're looking for?**
   - Check [Architecture README](architecture/README.md) for detailed index
   - Search for keywords in relevant category

2. **Documentation unclear?**
   - Raise an issue with specific questions
   - Suggest improvements

3. **Using AI assistants?**
   - Read [GitHub Copilot Guidelines](development/copilot-guidelines.md)
   - Check [Meta-Guidelines](architecture/coding-standards.md#meta-guidelines-using-this-documentation)

---

**Quick Links**:
- [Architecture Documentation](architecture/README.md)
- [Development Guidelines](development/)
- [Testing Documentation](testing/)
- [Feature Documentation](features/)

**Last Updated**: 2026-01-01
