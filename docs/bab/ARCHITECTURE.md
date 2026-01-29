# BAB Architecture Overview

**Version**: 1.0  
**Date**: 2026-01-29  
**Status**: ACTIVE

---

## Table of Contents

1. [System Architecture](#system-architecture)
2. [Component Architecture](#component-architecture)
3. [Data Architecture](#data-architecture)
4. [Communication Architecture](#communication-architecture)
5. [Deployment Architecture](#deployment-architecture)
6. [Security Architecture](#security-architecture)

---

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────┐
│                   BAB Gateway System                │
├─────────────────────────────────────────────────────┤
│ Web UI (Vaadin)           │ REST API               │
├─────────────────────────────────────────────────────┤
│ Spring Boot Application Layer                       │
│ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐    │
│ │   Device    │ │    Node     │ │   Project   │    │
│ │ Management  │ │ Management  │ │ Management  │    │
│ └─────────────┘ └─────────────┘ └─────────────┘    │
├─────────────────────────────────────────────────────┤
│ Communication Protocol Layer                        │
│ ┌─────┐ ┌─────────┐ ┌─────────┐ ┌─────────────┐    │
│ │ CAN │ │Ethernet │ │ Modbus  │ │     ROS     │    │
│ └─────┘ └─────────┘ └─────────┘ └─────────────┘    │
├─────────────────────────────────────────────────────┤
│ Database Layer (PostgreSQL/H2)                     │
└─────────────────────────────────────────────────────┘
```

### Architecture Principles

1. **Layered Architecture**: Clear separation of concerns
2. **Profile-based Activation**: Components only active with `@Profile("bab")`
3. **Minimal Dependencies**: Reduced complexity for embedded deployment
4. **Protocol Abstraction**: Unified interface for different communication protocols
5. **Single Responsibility**: Each component has a focused purpose

---

## Component Architecture

### Core Components

#### 1. Device Management Layer

**CBabDevice**
- **Purpose**: Represents the physical gateway device
- **Scope**: One device per company
- **Responsibilities**:
  - Device identification and metadata
  - Status monitoring
  - Configuration management
  - User assignment

**Key Patterns**:
```java
@Entity
@Profile("bab")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "company_id"))
public class CBabDevice extends CEntityOfCompany<CBabDevice> {
    // One device per company constraint
    private String deviceSerialNumber;
    private String deviceType;
    private LocalDateTime lastHeartbeat;
    private CBabDeviceStatus status;
}
```

#### 2. Communication Node Layer

**Abstract Node Hierarchy**:
```
CBabNode (Abstract)
├── CBabNodeCAN
├── CBabNodeEthernet  
├── CBabNodeModbus
└── CBabNodeROS
```

**CBabNode (Abstract Base)**
- **Pattern**: `@MappedSuperclass` for inheritance
- **Common Fields**: enabled, nodeStatus, nodeType, device reference
- **Responsibilities**: Base functionality for all protocol nodes

**Concrete Node Types**:

```java
// CAN-bus Node
@Entity
public class CBabNodeCAN extends CBabNode<CBabNodeCAN> {
    private Integer bitrate;        // CAN bitrate (125k, 250k, 500k, 1M)
    private String canInterface;    // can0, can1, etc.
    private Boolean extendedFrames; // 29-bit vs 11-bit addressing
}

// Ethernet Node  
@Entity
public class CBabNodeEthernet extends CBabNode<CBabNodeEthernet> {
    private String ipAddress;       // Interface IP address
    private Integer port;           // Communication port
    private String subnetMask;      // Network configuration
    private String macAddress;      // Physical address
}

// Modbus Node
@Entity
public class CBabNodeModbus extends CBabNode<CBabNodeModbus> {
    private String serialPort;     // /dev/ttyUSB0, COM1, etc.
    private Integer baudRate;      // 9600, 19200, 38400, 115200
    private String parity;         // NONE, EVEN, ODD
    private Integer dataBits;      // 7, 8
    private Integer stopBits;      // 1, 2
    private Integer slaveId;       // Modbus slave ID
}

// ROS Node
@Entity  
public class CBabNodeROS extends CBabNode<CBabNodeROS> {
    private String rosVersion;     // ROS1, ROS2
    private String masterUri;      // ROS master URI
    private String nodeNamespace;  // ROS namespace
    private Integer qosProfile;    // Quality of Service profile
}
```

#### 3. Project Management Layer

**CProject_Bab**
- **Purpose**: BAB-specific project configuration
- **Extensions**: Adds IP address configuration to base project
- **Scope**: Company-scoped projects for network configuration

```java
@Entity
@DiscriminatorValue("BAB")  
public class CProject_Bab extends CProject<CProject_Bab> {
    @Pattern(regexp = "IPv4|IPv6_REGEX")
    private String ipAddress;  // Gateway IP configuration
}
```

### Service Architecture

#### Abstract Service Pattern
```java
// Abstract service - NO @Service annotation
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public abstract class CBabNodeService<NodeType extends CBabNode<NodeType>> 
    extends CEntityOfCompanyService<NodeType> {
    
    // Common node operations
    public abstract List<NodeType> findByDevice(CBabDevice device);
    public abstract void enableNode(NodeType node);
    public abstract void disableNode(NodeType node);
}

// Concrete service - HAS @Service annotation  
@Service
@Profile("bab")
public class CBabNodeCANService extends CBabNodeService<CBabNodeCAN> 
    implements IEntityRegistrable, IEntityWithView {
    
    // CAN-specific operations
    public void setBitrate(CBabNodeCAN node, Integer bitrate) { }
    public List<CBabNodeCAN> findByBitrate(Integer bitrate) { }
}
```

---

## Data Architecture

### Entity Relationship Diagram

```
┌─────────────┐     ┌─────────────┐
│  CCompany   │────▶│ CBabDevice  │
│             │ 1:1 │             │
└─────────────┘     └─────────────┘
                           │
                           │ 1:*
                           ▼
                    ┌─────────────┐
                    │  CBabNode   │
                    │ (Abstract)  │
                    └─────────────┘
                           │
          ┌─────────────────┼─────────────────┐
          │                 │                 │
          ▼                 ▼                 ▼
   ┌────────────┐   ┌──────────────┐   ┌─────────────┐
   │CBabNodeCAN │   │CBabNodeEthernet│   │CBabNodeModbus│
   └────────────┘   └──────────────┘   └─────────────┘
                           │
                           ▼
                   ┌─────────────┐
                   │ CBabNodeROS │
                   └─────────────┘

┌─────────────┐     ┌─────────────────┐
│  CCompany   │────▶│  CProject_Bab   │
│             │ 1:* │                 │
└─────────────┘     └─────────────────┘
```

### Database Schema

#### Table Naming Convention
- **BAB Entities**: `cbab_*` prefix for BAB-specific tables
- **Shared Entities**: Standard Derbent naming for inherited entities

#### Key Tables
```sql
-- Device table (one per company)
CREATE TABLE cbab_device (
    device_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL UNIQUE, -- One device per company
    name VARCHAR(255) NOT NULL,
    device_serial_number VARCHAR(100),
    device_type VARCHAR(50),
    last_heartbeat TIMESTAMP,
    status VARCHAR(20),
    created_by_id BIGINT,
    FOREIGN KEY (company_id) REFERENCES ccompany(company_id),
    FOREIGN KEY (created_by_id) REFERENCES cuser(user_id)
);

-- Abstract node table  
CREATE TABLE cbab_node (
    node_id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    node_type VARCHAR(50) NOT NULL, -- CAN, ETHERNET, MODBUS, ROS
    enabled BOOLEAN DEFAULT false,
    node_status VARCHAR(50),
    created_by_id BIGINT,
    FOREIGN KEY (device_id) REFERENCES cbab_device(device_id),
    FOREIGN KEY (created_by_id) REFERENCES cuser(user_id)
);

-- CAN node specific table
CREATE TABLE cbab_node_can (
    node_id BIGINT PRIMARY KEY,
    bitrate INTEGER DEFAULT 500000, -- 125k, 250k, 500k, 1M
    can_interface VARCHAR(10) DEFAULT 'can0',
    extended_frames BOOLEAN DEFAULT false,
    FOREIGN KEY (node_id) REFERENCES cbab_node(node_id)
);

-- Ethernet node specific table  
CREATE TABLE cbab_node_ethernet (
    node_id BIGINT PRIMARY KEY,
    ip_address VARCHAR(45), -- IPv4/IPv6
    port INTEGER DEFAULT 502,
    subnet_mask VARCHAR(45),
    mac_address VARCHAR(17),
    FOREIGN KEY (node_id) REFERENCES cbab_node(node_id)
);
```

---

## Communication Architecture

### Protocol Support

#### 1. CAN-bus Protocol
```java
public class CBabNodeCAN {
    // Bitrate options: 125k, 250k, 500k, 1M bps
    private Integer bitrate = 500000;
    
    // Interface mapping: can0, can1, vcan0 (virtual)
    private String canInterface = "can0";
    
    // Addressing: 11-bit (standard) vs 29-bit (extended)
    private Boolean extendedFrames = false;
}
```

**CAN Frame Structure**:
- **Standard**: 11-bit identifier + up to 8 bytes data
- **Extended**: 29-bit identifier + up to 8 bytes data
- **Real-time**: Low latency, deterministic communication

#### 2. Ethernet/IP Protocol
```java
public class CBabNodeEthernet {
    private String ipAddress;   // Static or DHCP assigned
    private Integer port;       // Default 44818 for EtherNet/IP
    private String subnetMask;  // Network configuration
}
```

#### 3. Modbus Protocol
```java
public class CBabNodeModbus {
    // RTU over RS-485/RS-232
    private String serialPort = "/dev/ttyUSB0";
    private Integer baudRate = 115200;
    private String parity = "NONE";
    
    // TCP over Ethernet
    private String ipAddress;
    private Integer port = 502; // Standard Modbus port
    
    private Integer slaveId = 1; // Modbus device ID
}
```

#### 4. ROS Protocol
```java
public class CBabNodeROS {
    private String rosVersion = "ROS2";           // ROS1 or ROS2
    private String masterUri = "http://localhost:11311"; // ROS1 master
    private String nodeNamespace = "/gateway";    // ROS namespace
    private Integer qosProfile = 0;               // ROS2 QoS profile
}
```

### Communication Flow

```
Web UI ←→ Spring Boot ←→ Protocol Layer ←→ Physical Interface
   │           │              │                   │
   └─ HTTP ────┴── Service ───┴─── Driver ────────┴── Hardware
```

---

## Deployment Architecture

### Deployment Models

#### 1. Embedded Gateway
```
┌─────────────────────────────┐
│     Embedded Linux Device   │
│  ┌─────────────────────────┐ │
│  │   BAB Application       │ │
│  │   (Spring Boot)         │ │
│  └─────────────────────────┘ │
│  ┌─────────────────────────┐ │
│  │   H2 Database           │ │
│  └─────────────────────────┘ │
│  ┌─────────────────────────┐ │
│  │  Protocol Drivers       │ │
│  │  CAN | ETH | MB | ROS   │ │
│  └─────────────────────────┘ │
└─────────────────────────────┘
```

#### 2. Industrial PC
```
┌─────────────────────────────┐
│    Industrial PC/Server     │
│  ┌─────────────────────────┐ │
│  │   BAB Application       │ │
│  │   (Spring Boot)         │ │
│  └─────────────────────────┘ │
│  ┌─────────────────────────┐ │
│  │  PostgreSQL Database    │ │
│  └─────────────────────────┘ │
│  ┌─────────────────────────┐ │
│  │  Protocol Hardware      │ │
│  │  CAN Cards | RS-485     │ │
│  └─────────────────────────┘ │
└─────────────────────────────┘
```

### Resource Requirements

| Component | Embedded | Industrial PC |
|-----------|----------|---------------|
| **RAM** | 256MB | 2GB |
| **Storage** | 1GB | 10GB |
| **CPU** | ARM Cortex-A9 | x86_64 |
| **Network** | Ethernet | Ethernet + WiFi |
| **Protocols** | CAN + 1 other | All protocols |

### Configuration Management

#### Environment-Specific Configuration
```yaml
# application-bab-embedded.yml
spring:
  profiles:
    active: bab,h2,embedded
  datasource:
    url: "jdbc:h2:file:/opt/bab/data/gateway"
    
bab:
  device:
    memory-limit: 256MB
    protocols: [CAN]
    
# application-bab-industrial.yml  
spring:
  profiles:
    active: bab,postgres,industrial
  datasource:
    url: "jdbc:postgresql://localhost/bab_gateway"
    
bab:
  device:
    memory-limit: 2GB
    protocols: [CAN, ETHERNET, MODBUS, ROS]
```

---

## Security Architecture

### Authentication & Authorization

#### Simplified Security Model
```java
@Configuration
@Profile("bab")
public class BabSecurityConfig {
    
    // Minimal security for embedded deployment
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/device/status").permitAll()  // Health check
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults())
            .build();
    }
}
```

#### Default Roles
- **OPERATOR**: Basic device monitoring and configuration
- **ADMIN**: Full device and node management
- **VIEWER**: Read-only access to device status

### Network Security

#### Industrial Network Considerations
```java
@Component
@Profile("bab") 
public class BabNetworkSecurity {
    
    // IP whitelist for CAN bridge access
    private Set<String> allowedNetworks = Set.of(
        "192.168.1.0/24",    // Local management network
        "10.0.0.0/8",        // Industrial network
        "172.16.0.0/12"      // Private networks
    );
    
    // Protocol-specific security
    public boolean isCanAccessAllowed(String sourceIp) {
        return allowedNetworks.stream()
            .anyMatch(network -> isInNetwork(sourceIp, network));
    }
}
```

### Data Protection

#### Minimal Data Exposure
- **Device Data**: Local storage only, no cloud transmission
- **Communication Logs**: Rotate after 24 hours
- **Configuration**: Encrypted storage for sensitive parameters
- **Audit Trail**: Basic logging without sensitive protocol data

---

## Performance Considerations

### Memory Management
```java
@Component
@Profile("bab")
public class BabPerformanceConfig {
    
    // JVM tuning for embedded deployment
    @Value("${bab.memory.heap-size:128m}")
    private String heapSize;
    
    // Connection pooling
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(5);        // Minimal connections
        config.setConnectionTimeout(30000);   // 30 second timeout
        return new HikariDataSource(config);
    }
}
```

### Protocol Optimization
- **CAN**: Direct kernel socket access for minimal latency
- **Ethernet**: Async I/O with Netty for high throughput  
- **Modbus**: Connection pooling for RTU/TCP efficiency
- **ROS**: Native message serialization without XML overhead

---

**Document Control**:
- **Version**: 1.0
- **Created**: 2026-01-29  
- **Next Review**: 2026-04-29
- **Classification**: Technical Reference