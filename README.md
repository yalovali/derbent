# Derbent - Collaborative Project Management System

A comprehensive Java-based project management application built with Vaadin Flow, designed for small to medium-sized development teams. Inspired by Jira and ProjeQtOr, Derbent provides enterprise-grade project management capabilities with a modern, responsive interface.

## 🎯 Project Overview

Derbent is a full-stack project management solution featuring:
- **Multi-tenant Architecture** - Support for multiple companies and projects
- **Role-based Access Control** - Granular permissions for different user types
- **Rich UI Components** - Advanced grids, forms, and kanban boards
- **Entity-driven Development** - Code generation and metadata-driven forms
- **Comprehensive Testing** - Playwright-based UI automation

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (recommended) or H2 for development

### Getting Started
```bash
# Clone the repository
git clone https://github.com/yalovali/derbent.git

# Navigate to project directory
cd derbent

# Run the application
mvn spring-boot:run
```

### First Login
- Default URL: `http://localhost:8080`
- Admin credentials will be created on first startup
- Follow the setup wizard to configure your first company and project

## 📚 Documentation

For comprehensive development guidelines and architectural patterns, see:

- **[GitHub Copilot Guidelines](docs/copilot-guidelines.md)** - Essential coding patterns and AI assistance guidelines
- **[Entity Class Patterns](docs/entity-inheritance.md)** - Entity layer inheritance and design patterns
- **[Service Class Patterns](docs/service-patterns.md)** - Business logic and repository patterns
- **[View Class Patterns](docs/view-patterns.md)** - UI component and page patterns
- **[Icon and Auxiliary Patterns](docs/utility-patterns.md)** - Utility classes and common patterns

## 🏗️ Architecture

### Technology Stack
- **Backend**: Java 17, Spring Boot 3.5, Hibernate/JPA
- **Frontend**: Vaadin Flow 24.8 (Server-side rendering)
- **Database**: PostgreSQL with H2 for testing
- **Testing**: Playwright for UI automation, JUnit for unit tests
- **Build**: Maven with custom profiles

### Project Structure
```
src/main/java/tech/derbent/
├── abstracts/              # Base classes and annotations
│   ├── domains/            # Core entity base classes
│   ├── views/              # UI base classes
│   └── annotations/        # Custom annotations
├── api/                    # Core API and utilities
│   ├── domains/            # CEntity, CEntityDB base classes
│   ├── services/           # Service base classes
│   ├── views/              # View base classes
│   └── utils/              # CAuxillaries and utilities
├── [module]/               # Feature modules (activities, users, etc.)
│   ├── domain/            # Entity classes
│   ├── service/           # Business logic and repositories
│   └── view/              # UI components and views
└── session/               # Session and security management
```

### Core Principles
- **C-Prefix Convention**: All custom classes start with 'C' (CActivity, CUser, etc.)
- **Inheritance Hierarchies**: Consistent base class patterns for entities, services, and views
- **Metadata-Driven**: Extensive use of annotations for automatic UI generation
- **Project-Aware**: Multi-tenant support with project context throughout

## 🛠️ Development Workflow

### Using GitHub Copilot
This project is optimized for GitHub Copilot assistance. See [Copilot Guidelines](docs/copilot-guidelines.md) for:
- Naming conventions and class patterns
- Code generation templates
- Common coding patterns and anti-patterns
- AI-friendly documentation standards

### Adding New Features
1. Create entity classes following inheritance patterns
2. Implement service layer with repository patterns
3. Build UI using view inheritance hierarchy
4. Add comprehensive Playwright tests
5. Update documentation and Copilot guidelines

## 🔧 Key Features

### Entity Management
- **Hierarchical Entities**: Projects → Activities → Comments
- **Type Entities**: Configurable types for activities, meetings, risks
- **Status Tracking**: Color-coded status entities with workflow support
- **Audit Trail**: Automatic tracking of entity changes

### UI Components
- **Enhanced Grids**: Sortable, filterable data tables with export
- **Smart Forms**: Metadata-driven form generation with validation
- **Kanban Boards**: Drag-and-drop task management
- **Color-Aware Components**: Consistent theming and status visualization

### Project Management
- **Multi-Project Support**: Organize work across multiple projects
- **Activity Tracking**: Detailed task management with time tracking
- **Meeting Management**: Schedule and track meeting outcomes
- **Risk Management**: Identify and monitor project risks

## 📈 Performance & Scalability

- **Lazy Loading**: Optimized entity loading patterns
- **Efficient Queries**: JOIN FETCH strategies for related entities
- **Connection Pooling**: Configurable database connection management
- **Caching**: Strategic caching for frequently accessed data

## 🧪 Testing Strategy

### Automated Testing
- **Playwright Tests**: Comprehensive UI automation for all views
- **Unit Tests**: Service layer and utility class coverage
- **Integration Tests**: End-to-end workflow validation

### Test Guidelines
- All new features require Playwright test coverage
- Tests follow the Page Object Model pattern
- Headless and headed test execution support

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Read the [Copilot Guidelines](docs/copilot-guidelines.md)
2. Follow the established inheritance patterns
3. Add comprehensive tests for new features
4. Update documentation for significant changes

## 🆘 Support

For questions and support:
- Review the documentation in the `docs/` directory
- Check existing issues and patterns in the codebase
- Follow the established coding conventions