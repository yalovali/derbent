# BAB - IoT CAN Bus to Ethernet/ROS Gateway Project

## Project Overview

BAB is an IoT gateway device management system built on the Derbent API framework. It provides a web-based configuration interface for managing CAN Bus to Ethernet/ROS protocol gateway devices with support for various industrial communication protocols.

**Profile**: `bab` (Spring profile for BAB-specific features)

---

## Architecture

### Entity Hierarchy

```
CEntityDB<T>
└── CEntityNamed<T>
    └── CBabItem<T> (Base for all BAB entities - similar to CProjectItem)
        ├── CBabDeviceBase (Abstract device base)
        │   └── CBabDevice (Concrete device instance - unique per DB)
        │
        ├── CBabNode (Abstract node base)
        │   ├── CBabNodeCAN
        │   ├── CBabNodeModbus
        │   ├── CBabNodeEthernet
        │   └── CBabNodeROS
        │
        └── CBabConfiguration (Device configurations)
            ├── CBabNetworkConfig (IP, routing, DNS)
            ├── CBabSystemConfig (Linux settings, services)
            └── CBabProtocolConfig (Protocol-specific settings)
```

---

## Core Concepts

### 1. Device Management
- **CBabDevice**: Single unique device instance per database
- Connected via CUser and CCompany (standard BAB profile authentication)
- Represents the physical IoT gateway device
- Owns multiple nodes and configurations

### 2. Node System
Each node represents a communication interface:
- **CBabNodeCAN**: CAN bus interface configuration
- **CBabNodeModbus**: Modbus RTU/TCP interface
- **CBabNodeEthernet**: Ethernet interface settings
- **CBabNodeROS**: ROS (Robot Operating System) bridge

### 3. Configuration Management
Device configurations stored as entities:
- **Network**: IP addresses, routing tables, DNS
- **System**: Linux system settings, services, daemons
- **Protocol**: Protocol-specific parameters

---

## Database Schema

### User & Company Context
```
CUser (from base system)
  └── assigned to → CCompany
                      └── owns → CBabDevice (unique)
```

### Device Structure
```
CBabDevice (unique per database)
  ├── has many → CBabNode (CAN, Modbus, Ethernet, ROS)
  └── has many → CBabConfiguration (Network, System, Protocol)
```

---

## Implementation Status

### ✅ Completed
- [x] Basic profile structure (`bab` profile)
- [x] Dashboard view (CBabDashboardView)
- [x] Base entity classes (CBabDeviceBase, CBabDevice)
- [x] Data initializer (CBabDataInitializer)

### 🚧 In Progress
- [ ] Complete entity domain classes (CBabItem, CBabNode hierarchy)
- [ ] Service layer for device and node management
- [ ] Repository interfaces
- [ ] Initializer services for sample data

### 📋 Planned
- [ ] GUI for device configuration
- [ ] Node management views
- [ ] Real-time device status monitoring
- [ ] Protocol translation logic
- [ ] Device firmware management
- [ ] Log viewer and diagnostics

---

## File Structure

```
src/main/java/tech/derbent/bab/
├── config/
│   └── CBabDataInitializer.java       # Sample data initialization
├── device/
│   ├── domain/
│   │   ├── CBabItem.java              # Base class for BAB entities
│   │   ├── CBabDeviceBase.java        # Abstract device base
│   │   └── CBabDevice.java            # Concrete device entity
│   ├── service/
│   │   ├── CBabDeviceService.java     # Device business logic
│   │   └── CBabItemService.java       # Base service for BAB items
│   ├── repository/
│   │   ├── IBabDeviceRepository.java
│   │   └── IBabItemRepository.java
│   └── initializer/
│       └── CBabDeviceInitializerService.java
├── node/
│   ├── domain/
│   │   ├── CBabNode.java              # Abstract node base
│   │   ├── CBabNodeCAN.java
│   │   ├── CBabNodeModbus.java
│   │   ├── CBabNodeEthernet.java
│   │   └── CBabNodeROS.java
│   ├── service/
│   │   └── CBabNodeService.java
│   └── repository/
│       └── IBabNodeRepository.java
├── configuration/
│   ├── domain/
│   │   ├── CBabConfiguration.java
│   │   ├── CBabNetworkConfig.java
│   │   ├── CBabSystemConfig.java
│   │   └── CBabProtocolConfig.java
│   └── service/
│       └── CBabConfigurationService.java
└── ui/
    └── view/
        ├── CBabDashboardView.java     # Main dashboard
        ├── CBabDeviceView.java        # Device management
        ├── CBabNodeView.java          # Node configuration
        └── CBabConfigView.java        # System configuration
```

---

## Related Documentation

### Core Derbent API Documentation
- [Entity Inheritance Patterns](../architecture/ENTITY_INHERITANCE_AND_DESIGN_PATTERNS.md)
- [Coding Standards](../architecture/coding-standards.md)
- [Development Guidelines](../development/copilot-guidelines.md)

### BAB-Specific Documentation
- [BAB Agent Directions](./BAB_AGENT_DIRECTIONS.md) - AI agent guidelines
- [BAB Entity Design](./BAB_ENTITY_DESIGN.md) - Domain model details
- [BAB API Reference](./BAB_API_REFERENCE.md) - Service layer documentation

---

## Quick Start

### Running BAB Profile

```bash
# Setup Java environment
source ./bin/setup-java-env.sh

# Run with BAB profile
mvn spring-boot:run -Dspring.profiles.active=bab,h2

# Access dashboard
# Navigate to: http://localhost:8080/home
# Login with sample user credentials
```

### Development Workflow

1. **Entity Design**: Follow CEntityNamed → CBabItem inheritance
2. **Service Layer**: Extend CAbstractService for business logic
3. **Repository**: Extend IAbstractRepository for data access
4. **Views**: Extend CAbstractPage for UI components
5. **Testing**: Add Playwright tests for UI validation

---

## Technical Notes

### Entity Inheritance Rules
- **CBabItem**: Extends CEntityNamed, similar to CProjectItem pattern
- **All domain classes**: Must have C-prefix (CBabDevice, not BabDevice)
- **Services**: Follow CAbstractService pattern with dependency injection
- **Views**: Extend CAbstractPage with proper @Profile("bab") annotation

### Database Considerations
- **Device Uniqueness**: CBabDevice is unique per database (one device per installation)
- **Node Relationships**: Many nodes per device (one-to-many)
- **Configuration Storage**: Configurations as database entities (not flat files)
- **Sample Data**: Auto-initialized via CBabDataInitializer

### GUI Design Principles
- **Dashboard-First**: Main entry point is CBabDashboardView
- **Device-Centric**: All views relate to the single CBabDevice instance
- **Real-Time Updates**: Plan for WebSocket-based status updates
- **Configuration Validation**: GUI validates settings before applying to device

---

## Next Steps

1. Complete domain entity classes (CBabItem, CBabNode hierarchy)
2. Implement service layer with proper dependency injection
3. Create repository interfaces with standard query methods
4. Build GUI views for device and node configuration
5. Add Playwright tests for all views
6. Implement device communication layer
7. Add real-time monitoring dashboard components

---

**Last Updated**: 2026-01-13  
**Project Status**: Initial Development  
**Profile**: bab  
**Framework**: Derbent API / Spring Boot 3.5 / Vaadin 24.8
