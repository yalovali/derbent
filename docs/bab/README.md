# BAB IoT Gateway Project Documentation

This folder contains documentation for the **BAB (IoT CAN Bus to Ethernet/ROS Gateway)** project built on the Derbent API framework.

---

## 📚 Documentation Structure

### Core Documentation Files

1. **[BAB_PROJECT_OVERVIEW.md](./BAB_PROJECT_OVERVIEW.md)** - **START HERE**
   - Project architecture and entity hierarchy
   - Core concepts and database schema
   - File structure and implementation status
   - Quick start guide and technical notes

2. **[BAB_AGENT_DIRECTIONS.md](./BAB_AGENT_DIRECTIONS.md)** - **AI Assistant Guidelines**
   - AI assistant (GitHub Copilot) specific guidelines
   - Entity design decision trees
   - Coding standards for BAB profile
   - Testing requirements and validation workflows

---

## 🎯 Quick Reference

### When to Use BAB Profile

Use the `bab` profile when:
- Working with IoT gateway device management
- Managing CAN, Modbus, Ethernet, or ROS communication nodes
- Configuring device network settings and protocols
- Building device-centric (not project-centric) applications

### Key Concepts

- **CBabDevice**: Single unique device per company/database
- **CBabNode**: Communication interface (CAN, Modbus, Ethernet, ROS)
- **CBabItem**: Base class for all BAB entities (similar to CProjectItem)
- **Company Scoped**: All entities belong to a CCompany (no CProject required)

---

## 🚀 Quick Start

### Run BAB Application

```bash
# Setup Java environment
source ./bin/setup-java-env.sh

# Run with BAB profile
mvn spring-boot:run -Dspring.profiles.active=bab,h2

# Access dashboard
# Navigate to: http://localhost:8080/home
```

### Entity Hierarchy Quick Reference

```
CEntityDB → CEntityNamed → CBabItem
                              ├── CBabDeviceBase → CBabDevice
                              ├── CBabNode → (CAN, Modbus, Ethernet, ROS)
                              └── CBabConfiguration → (Network, System, Protocol)
```

---

## 📂 Related Documentation

### Derbent Core Documentation (Apply to BAB)

- **[Entity Inheritance Patterns](../architecture/ENTITY_INHERITANCE_AND_DESIGN_PATTERNS.md)**
  - Entity design rules and patterns
  - When to use inheritance vs interfaces
  - Lazy loading best practices

- **[Coding Standards](../architecture/coding-standards.md)**
  - C-prefix convention (MANDATORY)
  - UI component naming standards
  - Notification standards
  - Code formatting rules

- **[Development Guidelines](../development/copilot-guidelines.md)**
  - AI assistant workflow preferences
  - Testing infrastructure
  - Validation scenarios
  - Git commit preferences

---

## 🏗️ Implementation Checklist

### ✅ Completed

- [x] Base entity classes (CBabItem, CBabDeviceBase, CBabDevice)
- [x] Node domain hierarchy (CBabNode, CAN, Modbus, Ethernet, ROS)
- [x] Repository interfaces (IBabDeviceRepository, IBabNodeRepository)
- [x] Service layer (CBabDeviceService, CBabNodeService)
- [x] Sample data initializer (CBabDeviceInitializerService)
- [x] Dashboard view structure (CBabDashboardView)
- [x] Documentation (this folder)

### 🚧 Next Steps

- [ ] Configuration entity classes (Network, System, Protocol)
- [ ] Complete dashboard UI with device status cards
- [ ] Node management views (CRUD operations)
- [ ] Configuration management views
- [ ] Real-time device status monitoring
- [ ] Playwright tests for all views
- [ ] Device communication layer

---

## 🧪 Testing

### Run BAB Tests

```bash
# Run BAB-specific Playwright tests (when implemented)
./run-playwright-tests.sh bab-dashboard
./run-playwright-tests.sh bab-nodes
./run-playwright-tests.sh bab-config
```

### Test Coverage Requirements

- ✅ Device uniqueness (one per company)
- ✅ Node CRUD operations
- ✅ Dashboard loads correctly
- ✅ Configuration validation
- ✅ Real-time status updates (future)

---

## 🔧 Development Workflow

### Adding New BAB Features

1. **Check Documentation**: Review BAB_AGENT_DIRECTIONS.md
2. **Design Entities**: Follow CBabItem hierarchy pattern
3. **Add @Profile("bab")**: All beans and views must have BAB profile
4. **Create Tests**: Add Playwright tests for UI validation
5. **Update Docs**: Document changes in this folder
6. **Commit**: Use "feat(bab):" prefix for commits

### Code Standards

- **All classes**: Must have C-prefix (CBabDevice, not BabDevice)
- **Entity constants**: Must define ENTITY_TITLE_SINGULAR/PLURAL
- **Services**: Extend CAbstractService with dependency injection
- **Views**: Extend CAbstractPage with @Profile("bab")
- **Notifications**: Use CNotificationService (never direct Notification.show)

---

## 📝 Documentation Conventions

### When Adding New Documentation

- Place in `/docs/bab/` folder
- Follow existing naming pattern: `BAB_*.md`
- Update this README.md index
- Reference core Derbent docs where applicable
- Include code examples and diagrams

### Documentation Maintenance

- Update after significant features are added
- Keep implementation checklist current
- Document breaking changes prominently
- Add migration guides when needed

---

## 🤝 Contributing

### For AI Assistants (GitHub Copilot)

1. **ALWAYS** read BAB_AGENT_DIRECTIONS.md first
2. Follow Derbent coding standards (C-prefix, naming conventions)
3. Add @Profile("bab") to all BAB-specific beans
4. Test with `bab,h2` profile before committing
5. Update documentation when adding features

### For Human Developers

1. Review BAB_PROJECT_OVERVIEW.md for architecture
2. Follow entity inheritance patterns strictly
3. Use provided service/repository patterns
4. Add Playwright tests for all UI features
5. Keep documentation synchronized with code

---

## 📧 Contact & Support

For questions or issues:
- Review existing documentation in this folder
- Check core Derbent documentation in `/docs/architecture/`
- Refer to code examples in existing BAB classes

---

**Last Updated**: 2026-01-13  
**BAB Version**: 1.0.0 (Initial Implementation)  
**Derbent Framework**: Spring Boot 3.5 / Vaadin 24.8  
**Profile**: `bab`
