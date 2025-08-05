# Derbent - Collaborative Project Management Application

A Java-based project management application built with Vaadin, inspired by Jira and ProjeQtOr, targeting small to medium-sized offices.

## 📋 Development Standards

**Important:** All contributors must follow our strict coding guidelines:
- **[Coding Standards](./docs/architecture/coding-standards.md)** - Comprehensive coding standards and GitHub Copilot usage guidelines  
- **[Project Design](./docs/architecture/project-design.md)** - Complete project requirements and architecture overview

## 📚 Documentation

For complete documentation, see [`docs/`](./docs/):

### Quick Start Guides
- **[Architecture & Design](./docs/architecture/)** - Project design and coding standards
- **[Implementation Guides](./docs/implementation/)** - Feature implementation patterns
- **[Development Guides](./docs/guides/)** - UI components and best practices
- **[Testing Documentation](./docs/testing/)** - Testing strategies and automation

### Key Documents
- [Enhanced Binder Guide](./docs/guides/enhanced-binder-guide.md) - Form binding best practices
- [Activity Management](./docs/implementation/activity-management.md) - Core business logic patterns  
- [CSS Guidelines](./docs/guides/css-guidelines.md) - Styling standards and Vaadin theming
- [Testing Guide](./docs/testing/comprehensive-testing-guide.md) - Complete testing strategy

## Getting Started

The [Getting Started](https://vaadin.com/docs/latest/getting-started) guide will quickly familiarize you with your new
Derbent implementation. You'll learn how to set up your development environment, understand the project 
structure, and find resources to help you add muscles to your skeleton — transforming it into a fully-featured 
application.

For detailed development guidance, start with:
1. **[Project Design](./docs/architecture/project-design.md)** - Understanding the project architecture
2. **[Coding Standards](./docs/architecture/coding-standards.md)** - Development guidelines and best practices  
3. **[Implementation Guides](./docs/implementation/)** - Feature-specific implementation patterns

## 🏗️ Project Structure

```
src/
├── main/java/tech/derbent/
│   ├── abstracts/           # Base classes and annotations
│   ├── activities/          # Activity management
│   ├── administration/      # Company settings
│   ├── companies/           # Company management
│   ├── meetings/            # Meeting management
│   ├── projects/            # Project management
│   ├── setup/              # System settings
│   └── users/              # User management
└── test/                   # Unit and integration tests
docs/                       # Complete documentation
```

## 🚀 Technology Stack

- **Java 17** - Programming language
- **Spring Boot 3.5** - Application framework
- **Vaadin Flow 24.8** - UI framework
- **Hibernate/JPA** - Data persistence
- **Playwright** - UI test automation
- **Maven** - Build tool

## 🔧 Development Tools

- **Enhanced Binder** - Type-safe form binding
- **Annotation-based ComboBox** - Data provider system
- **Hierarchical Navigation** - Multi-level menu system
- **Grid Components** - Advanced data display
- **Color-aware Components** - Theme-consistent UI