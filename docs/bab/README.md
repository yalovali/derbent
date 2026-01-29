# BAB (Building Automation Bus) Profile Documentation

This directory contains comprehensive documentation for the BAB profile implementation in Derbent, including complete integration with the **Calimero CAN Gateway** project.

## Document Structure

### Core Documentation
- **[PROJECT_DEFINITION.md](PROJECT_DEFINITION.md)**: Complete project scope, requirements, and Calimero integration overview
- **[ARCHITECTURE.md](ARCHITECTURE.md)**: Technical architecture and design patterns  
- **[ENTITY_MODEL.md](ENTITY_MODEL.md)**: Entity relationships and database schema
- **[DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)**: Development environment and coding standards

### Implementation Planning  
- **[CALIMERO_INTEGRATION_PLAN.md](CALIMERO_INTEGRATION_PLAN.md)**: Detailed technical integration plan with Calimero C++ backend
- **[BAB_CALIMERO_AGILE_PLAN.md](BAB_CALIMERO_AGILE_PLAN.md)**: Complete 25-week agile implementation roadmap (180 story points)
- **[BAB_TODO_EPICS.md](BAB_TODO_EPICS.md)**: Detailed epic and story breakdown with task-level implementation guides
- **[COMPETITIVE_ANALYSIS.md](COMPETITIVE_ANALYSIS.md)**: Market analysis vs Vector CANoe, Kvaser, National Instruments, etc.

## BAB-Calimero System Overview

**BAB** provides the **Java/Vaadin web frontend** for managing the **Calimero C++ backend service**:

```
┌─────────────────────────────────────────────────────────────┐
│                   BAB-Calimero System                      │
├─────────────────────────────────────────────────────────────┤
│  BAB Frontend (Port 8080)      │  Calimero Backend         │
│  ├─ Device Management          │  ├─ CAN Gateway (50k fps) │
│  ├─ Node Configuration         │  ├─ Serial I/O (RS232)    │
│  ├─ User Authentication        │  ├─ Ethernet (UDP/TCP)    │
│  ├─ Project Organization       │  ├─ ROS2 Integration      │
│  ├─ Real-time Dashboard        │  ├─ Data Logging (CSV)    │
│  └─ Configuration Generation   │  └─ HTTP API (Port 8077)  │
└─────────────────────────────────────────────────────────────┘
```

## Key Features

### BAB Frontend (Java/Vaadin)
- **Web-based Management**: Complete gateway administration through browser interface
- **Multi-Protocol Configuration**: CAN, Serial (UART/RS232), Ethernet, ROS2 node management
- **Real-time Monitoring**: Live dashboard for 50k+ fps data streams with performance metrics
- **User Management**: Authentication, authorization, multi-company isolation
- **Configuration Generation**: Automatic generation of Calimero JSON config files

### Calimero Backend (C++ Service)
- **High-Performance I/O**: Real-time CAN, Serial, Ethernet processing at 50k+ fps
- **Event-driven Architecture**: <1% idle CPU, <10ms end-to-end latency
- **Protocol Routing**: Port-to-port forwarding, CAN ID filtering, protocol bridging
- **Data Logging**: High-performance CSV/binary logging with rotation
- **HTTP API**: Complete REST API for BAB integration (port 8077)

## Calimero Project Reference

**Location**: `/home/yasin/git/calimero/`  
**Type**: Complete C++17 project with extensive management documentation  

**Key Integration Points**:
- **HTTP API**: Communication via port 8077 (documented in `src/http/docs/`)
- **Configuration**: JSON config generation (templates in `config/`)
- **Project Management**: 8-document suite with 272 story points (`docs/management/`)

## Getting Started

### 1. Understanding the System
1. Review **[PROJECT_DEFINITION.md](PROJECT_DEFINITION.md)** for complete system scope
2. Read **[CALIMERO_INTEGRATION_PLAN.md](CALIMERO_INTEGRATION_PLAN.md)** for technical integration details
3. Study **[Calimero Documentation](../../../calimero/docs/)** for backend capabilities

### 2. Development Planning
1. Review **[BAB_CALIMERO_AGILE_PLAN.md](BAB_CALIMERO_AGILE_PLAN.md)** for 25-week implementation roadmap
2. Study **[BAB_TODO_EPICS.md](BAB_TODO_EPICS.md)** for detailed task breakdown
3. Follow **[DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)** for coding standards

### 3. Implementation
1. Start with Epic B1 (Service Integration) - 8 weeks, 60 story points
2. Follow sprint-by-sprint implementation in agile plan
3. Use detailed task breakdowns in TODO epics document

## Profile Configuration

BAB profile is activated with:
```properties
spring.profiles.active=bab
```

All BAB components are annotated with `@Profile("bab")` for isolation from other profiles.

## Market Positioning

BAB-Calimero creates a unique **open-source IoT gateway management platform** competing with:
- **Vector CANoe/CANalyzer** ($10k-50k) - Commercial CAN tooling
- **National Instruments** ($20k-100k) - Industrial measurement platforms  
- **Kvaser** ($5k-15k) - Hardware-software CAN solutions
- **Eclipse Kura/Node-RED** (Free) - Open-source but lower performance

**Competitive Advantages**:
- ✅ **Open Source** (vs $10k-50k licensing)
- ✅ **High Performance** (50k fps vs JavaScript limitations)
- ✅ **Linux Embedded** (vs Windows desktop requirements)
- ✅ **Web-based Management** (vs desktop-only tools)
- ✅ **Multi-protocol Industrial** (vs single-protocol focus)

See **[COMPETITIVE_ANALYSIS.md](COMPETITIVE_ANALYSIS.md)** for detailed market analysis.

## Project Timeline

**Total Duration**: 25 weeks (6 months)  
**Team Size**: 1-2 Java/Vaadin developers  
**Story Points**: 180 points across 5 epics  
**Sprint Velocity**: 15 points per 2-week sprint

**Epic Breakdown**:
- **Epic B1**: Calimero Service Integration (8 weeks, 60 points) - Critical
- **Epic B2**: Node Configuration Management (6 weeks, 45 points) - High  
- **Epic B3**: Real-time Dashboard & Monitoring (4 weeks, 30 points) - High
- **Epic B4**: Data Management Interface (4 weeks, 30 points) - Medium
- **Epic B5**: Production Operations (3 weeks, 15 points) - Low

---

**Document Control**:
- **Version**: 2.0
- **Updated**: 2026-01-29  
- **Calimero Integration**: Complete integration planning and documentation added
- **Implementation Ready**: Full agile plan with 180 story points across 25 weeks