# BAB Project Documentation

**BAB (Building Automation Bus)** is an IoT Gateway System for CAN-bus gateway between device interfaces.

## Project Scope

BAB provides a web interface for IoT systems, specifically designed for:
- **CAN-bus Gateway Management**: Primary communication protocol for automotive/industrial systems
- **Multi-Protocol Support**: Ethernet, Modbus, ROS integration
- **Device Interface Management**: Web-based configuration and monitoring
- **Lightweight Operation**: Minimal overhead for embedded/gateway environments

## Documentation Structure

| Document | Purpose |
|----------|---------|
| [Project Definition](PROJECT_DEFINITION.md) | Core project scope, goals, and architectural decisions |
| [Architecture Overview](ARCHITECTURE.md) | Technical architecture and design patterns |
| [Entity Model](ENTITY_MODEL.md) | BAB-specific entities and relationships |
| [Development Guide](DEVELOPMENT_GUIDE.md) | Development environment and workflow |
| [Coding Rules](CODING_RULES.md) | BAB-specific coding standards and patterns |
| [Deployment Guide](DEPLOYMENT_GUIDE.md) | Installation and configuration |
| [API Reference](API_REFERENCE.md) | REST API documentation |

## Quick Start

1. **Profile Activation**: BAB functionality is activated with `@Profile("bab")`
2. **Core Entities**: 
   - `CBabDevice`: Gateway device management
   - `CBabNode*`: Communication protocol interfaces (CAN, Ethernet, Modbus, ROS)
   - `CProject_Bab`: BAB-specific projects with IP configuration
3. **Key Features**:
   - Single device per company model
   - Protocol-specific node configuration
   - Real-time device monitoring
   - Minimal resource footprint

## Related Documentation

- [Main AGENTS.md](../AGENTS.md): Overall project coding standards
- [BAB Coding Rules](../BAB_CODING_RULES.md): BAB-specific coding patterns
- [Architecture Documentation](../architecture/): General architectural patterns

## Contributing

All BAB development must follow the project-specific patterns defined in this documentation folder. When making changes:

1. Update relevant documentation files
2. Follow BAB entity patterns (minimal, company-scoped, no workflow)
3. Test with `@Profile("bab")` activation
4. Ensure compliance with BAB coding rules

---

**Version**: 1.0  
**Last Updated**: 2026-01-29  
**Maintainer**: BAB Development Team