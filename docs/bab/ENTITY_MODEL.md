# BAB Entity Model

**Version**: 1.0  
**Date**: 2026-01-29  
**Status**: ACTIVE

---

## Table of Contents

1. [Entity Overview](#entity-overview)
2. [Core Entities](#core-entities)  
3. [Communication Node Hierarchy](#communication-node-hierarchy)
4. [Entity Relationships](#entity-relationships)
5. [Field Specifications](#field-specifications)
6. [Validation Rules](#validation-rules)
7. [Database Schema](#database-schema)

---

## Entity Overview

### BAB Entity Categories

| Category | Entities | Purpose |
|----------|----------|---------|
| **Core Business** | `CBabDevice`, `CProject_Bab` | Primary business objects |
| **Communication** | `CBabNode*` hierarchy | Protocol interfaces |
| **Configuration** | `CSystemSettings_Bab` | Gateway settings |
| **Monitoring** | `CDashboardProject_Bab` | Operational dashboards |

### Key Design Principles

1. **Minimalist Design**: Only essential entities for IoT gateway functionality
2. **Company-Scoped**: All entities belong to a single company per gateway
3. **No Workflow**: BAB entities do NOT implement `IHasStatusAndWorkflow`
4. **Abstract Inheritance**: `CBabNode` hierarchy for protocol specialization
5. **Profile Isolation**: All components marked with `@Profile("bab")`

---

## Core Entities

### CBabDevice

**Purpose**: Represents the physical gateway device  
**Scope**: One device per company (unique constraint)  
**Base Class**: `CEntityOfCompany<CBabDevice>`

```java
@Entity
@Table(name = "cbab_device", uniqueConstraints = {
    @UniqueConstraint(columnNames = "company_id")  // One device per company
})
public class CBabDevice extends CEntityOfCompany<CBabDevice> {
    
    // Entity constants
    public static final String DEFAULT_COLOR = "#6B5FA7";
    public static final String DEFAULT_ICON = "vaadin:server";
    public static final String ENTITY_TITLE_SINGULAR = "Device";
    public static final String ENTITY_TITLE_PLURAL = "Devices";
    public static final String VIEW_NAME = "Device Management";
}
```

#### Core Fields

| Field | Type | Purpose | Constraints |
|-------|------|---------|-------------|
| `deviceSerialNumber` | `String(100)` | Hardware identifier | Unique, alphanumeric |
| `deviceType` | `String(50)` | Gateway model/type | Required |
| `lastHeartbeat` | `LocalDateTime` | Last activity timestamp | Auto-updated |
| `deviceStatus` | `String(20)` | Operational status | ACTIVE, INACTIVE, ERROR |
| `ipAddress` | `String(45)` | Network address | IPv4/IPv6 format |
| `createdBy` | `CUser` | User who registered device | Optional |

#### Business Rules

1. **Singleton Pattern**: Only one device per company
2. **Heartbeat Monitoring**: Updated every 30 seconds during operation
3. **Status Management**: Automatic status based on heartbeat and node health
4. **Network Configuration**: IP address required for remote management

### CProject_Bab

**Purpose**: BAB-specific project with network configuration  
**Base Class**: `CProject<CProject_Bab>`  
**Discriminator**: `@DiscriminatorValue("BAB")`

```java
@Entity
@DiscriminatorValue("BAB")
public class CProject_Bab extends CProject<CProject_Bab> {
    
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                     "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|" +
                     "^([0-9a-fA-F]{0,4}:){7}[0-9a-fA-F]{0,4}$|^$")
    private String ipAddress;  // Gateway IP configuration
}
```

#### Extended Fields

| Field | Type | Purpose | Constraints |
|-------|------|---------|-------------|
| `ipAddress` | `String(45)` | Gateway IP address | IPv4/IPv6 pattern |

### CSystemSettings_Bab

**Purpose**: Gateway-specific system configuration  
**Base Class**: `CSystemSettings<CSystemSettings_Bab>`  
**Discriminator**: `@DiscriminatorValue("BAB")`

#### BAB-Specific Configuration Fields

| Field | Type | Default | Purpose |
|-------|------|---------|---------|
| `gatewayIpAddress` | `String(45)` | `""` | Primary gateway IP |
| `gatewayPort` | `Integer` | `8080` | Communication port |
| `deviceScanIntervalSeconds` | `Integer` | `30` | Device discovery interval |
| `maxConcurrentConnections` | `Integer` | `50` | Connection limit |
| `enableDeviceAutoDiscovery` | `Boolean` | `true` | Auto-discover devices |

---

## Communication Node Hierarchy

### Abstract Base: CBabNode

**Purpose**: Base class for all communication protocol interfaces  
**Pattern**: `@MappedSuperclass` for inheritance  
**Base Class**: `CEntityOfCompany<EntityClass>`

```java
@MappedSuperclass
public abstract class CBabNode<EntityClass> extends CEntityOfCompany<EntityClass> {
    
    // Common fields for all node types
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", nullable = false)
    private CBabDevice device;          // Parent device
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;    // Enable/disable interface
    
    @Column(name = "node_status", length = 50)
    private String nodeStatus;          // ACTIVE, INACTIVE, ERROR, CONNECTING
    
    @Column(name = "node_type", length = 50)
    private String nodeType;            // CAN, ETHERNET, MODBUS, ROS
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_id")
    private CUser createdBy;            // User who configured node
}
```

### Concrete Node Types

#### 1. CBabNodeCAN - CAN-bus Interface

**Purpose**: CAN (Controller Area Network) communication interface  
**Use Cases**: Automotive, industrial automation, embedded systems

```java
@Entity
@Table(name = "cbab_node_can")
public class CBabNodeCAN extends CBabNode<CBabNodeCAN> {
    
    public static final String DEFAULT_COLOR = "#FF5722";
    public static final String DEFAULT_ICON = "vaadin:car";
    public static final String ENTITY_TITLE_SINGULAR = "CAN Node";
    public static final String ENTITY_TITLE_PLURAL = "CAN Nodes";
}
```

| Field | Type | Default | Purpose | Constraints |
|-------|------|---------|---------|-------------|
| `bitrate` | `Integer` | `500000` | CAN bitrate (bps) | 125k, 250k, 500k, 1M |
| `canInterface` | `String(10)` | `"can0"` | Linux interface name | can0, can1, vcan0 |
| `extendedFrames` | `Boolean` | `false` | 29-bit vs 11-bit IDs | Standard/Extended |

#### 2. CBabNodeEthernet - Ethernet/IP Interface

**Purpose**: Ethernet/IP industrial communication protocol  
**Use Cases**: Industrial networking, PLC communication

```java
@Entity  
@Table(name = "cbab_node_ethernet")
public class CBabNodeEthernet extends CBabNode<CBabNodeEthernet> {
    
    public static final String DEFAULT_COLOR = "#2196F3";
    public static final String DEFAULT_ICON = "vaadin:connect";
    public static final String ENTITY_TITLE_SINGULAR = "Ethernet Node";
    public static final String ENTITY_TITLE_PLURAL = "Ethernet Nodes";
}
```

| Field | Type | Default | Purpose | Constraints |
|-------|------|---------|---------|-------------|
| `ipAddress` | `String(45)` | `null` | Interface IP address | IPv4/IPv6 format |
| `port` | `Integer` | `44818` | EtherNet/IP port | 1024-65535 |
| `subnetMask` | `String(45)` | `null` | Network mask | CIDR or dotted decimal |
| `macAddress` | `String(17)` | `null` | Physical address | MAC format |

#### 3. CBabNodeModbus - Modbus RTU/TCP Interface

**Purpose**: Modbus communication protocol (RTU over serial, TCP over Ethernet)  
**Use Cases**: SCADA systems, industrial sensors, meters

```java
@Entity
@Table(name = "cbab_node_modbus")  
public class CBabNodeModbus extends CBabNode<CBabNodeModbus> {
    
    public static final String DEFAULT_COLOR = "#FF9800";
    public static final String DEFAULT_ICON = "vaadin:automation";
    public static final String ENTITY_TITLE_SINGULAR = "Modbus Node";
    public static final String ENTITY_TITLE_PLURAL = "Modbus Nodes";
}
```

| Field | Type | Default | Purpose | Constraints |
|-------|------|---------|---------|-------------|
| `serialPort` | `String(20)` | `"/dev/ttyUSB0"` | Serial port path | Linux/Windows paths |
| `baudRate` | `Integer` | `115200` | Serial baud rate | 9600, 19200, 38400, 115200 |
| `parity` | `String(10)` | `"NONE"` | Serial parity | NONE, EVEN, ODD |
| `dataBits` | `Integer` | `8` | Serial data bits | 7, 8 |
| `stopBits` | `Integer` | `1` | Serial stop bits | 1, 2 |
| `slaveId` | `Integer` | `1` | Modbus device ID | 1-247 |
| `ipAddress` | `String(45)` | `null` | TCP IP address | For Modbus TCP |
| `port` | `Integer` | `502` | TCP port | Standard Modbus port |

#### 4. CBabNodeROS - Robot Operating System Interface

**Purpose**: ROS (Robot Operating System) communication interface  
**Use Cases**: Robotics, autonomous vehicles, research platforms

```java
@Entity
@Table(name = "cbab_node_ros")
public class CBabNodeROS extends CBabNode<CBabNodeROS> {
    
    public static final String DEFAULT_COLOR = "#9C27B0";
    public static final String DEFAULT_ICON = "vaadin:robot";
    public static final String ENTITY_TITLE_SINGULAR = "ROS Node";
    public static final String ENTITY_TITLE_PLURAL = "ROS Nodes";
}
```

| Field | Type | Default | Purpose | Constraints |
|-------|------|---------|---------|-------------|
| `rosVersion` | `String(10)` | `"ROS2"` | ROS version | ROS1, ROS2 |
| `masterUri` | `String(255)` | `"http://localhost:11311"` | ROS master URI | ROS1 only |
| `nodeNamespace` | `String(100)` | `"/gateway"` | ROS namespace | Valid ROS namespace |
| `qosProfile` | `Integer` | `0` | Quality of Service | ROS2 QoS profiles |

---

## Entity Relationships

### Relationship Diagram

```
CCompany
│
├── CBabDevice (1:1)
│   └── CBabNode* (1:*)
│       ├── CBabNodeCAN
│       ├── CBabNodeEthernet  
│       ├── CBabNodeModbus
│       └── CBabNodeROS
│
├── CProject_Bab (1:*)
│   
└── CSystemSettings_Bab (1:1)
```

### Relationship Specifications

#### Company ↔ Device (1:1)
```java
// CBabDevice constraint
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "company_id"))

// Business rule: One device per company
public class CBabDeviceService {
    public CBabDevice createDevice(String name, CCompany company) {
        if (repository.findByCompany(company).isPresent()) {
            throw new IllegalStateException("Company already has a device");
        }
        return new CBabDevice(name, company);
    }
}
```

#### Device ↔ Nodes (1:*)
```java
// CBabNode relationship
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "device_id", nullable = false)
private CBabDevice device;

// Query methods
public interface IBabNodeRepository<NodeType extends CBabNode<NodeType>> {
    List<NodeType> findByDevice(CBabDevice device);
    List<NodeType> findByDeviceAndEnabled(CBabDevice device, Boolean enabled);
}
```

#### Company ↔ Projects (1:*)
```java
// Standard CProject relationship with BAB extensions
@Entity
@DiscriminatorValue("BAB")
public class CProject_Bab extends CProject<CProject_Bab> {
    // Inherits company relationship from base CProject
}
```

---

## Field Specifications

### Common Field Patterns

#### Name Fields
```java
@Column(nullable = false, length = 255)
@Size(max = 255)
@NotBlank(message = "Name is required")  
@AMetaData(displayName = "Name", required = true, order = 10)
private String name;
```

#### IP Address Fields
```java
@Column(length = 45) // Supports IPv6
@Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                 "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|" +
                 "^([0-9a-fA-F]{0,4}:){7}[0-9a-fA-F]{0,4}$|^$")
@AMetaData(displayName = "IP Address", description = "IPv4 or IPv6 address")
private String ipAddress;
```

#### Boolean Configuration Fields  
```java
@Column(nullable = false)
@AMetaData(displayName = "Enabled", required = true, defaultValue = "false")
private Boolean enabled = false;
```

#### Timestamp Fields
```java
@Column(nullable = true)
@AMetaData(displayName = "Last Heartbeat", readOnly = true)
private LocalDateTime lastHeartbeat;
```

### Protocol-Specific Validations

#### CAN Bitrate Validation
```java
@Min(value = 125000, message = "CAN bitrate must be at least 125 kbps")
@Max(value = 1000000, message = "CAN bitrate cannot exceed 1 Mbps")
private Integer bitrate;

// Valid values: 125000, 250000, 500000, 1000000
```

#### Serial Port Validation
```java
@Pattern(regexp = "^(/dev/tty|COM).*|^$", message = "Invalid serial port format")
private String serialPort;

// Examples: /dev/ttyUSB0, /dev/ttyS0, COM1, COM2
```

#### Port Range Validation  
```java
@Min(value = 1024, message = "Port must be at least 1024")
@Max(value = 65535, message = "Port cannot exceed 65535")
private Integer port;
```

---

## Validation Rules

### Entity-Level Validation

#### CBabDevice Validation
```java
@Override
protected void validateEntity(final CBabDevice entity) {
    super.validateEntity(entity);
    
    // Required fields
    Check.notBlank(entity.getName(), "Device name is required");
    Check.notBlank(entity.getDeviceType(), "Device type is required");
    
    // Unique device per company
    Optional<CBabDevice> existing = repository.findByCompany(entity.getCompany());
    if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
        throw new ValidationException("Company already has a device");
    }
    
    // Serial number format
    if (entity.getDeviceSerialNumber() != null) {
        if (!entity.getDeviceSerialNumber().matches("^[A-Z0-9-]+$")) {
            throw new ValidationException("Invalid serial number format");
        }
    }
}
```

#### CBabNode Validation
```java
@Override  
protected void validateEntity(final CBabNode entity) {
    super.validateEntity(entity);
    
    // Required relationships
    Check.notNull(entity.getDevice(), "Device is required");
    Check.notBlank(entity.getNodeType(), "Node type is required");
    
    // Node type consistency
    String expectedType = getNodeTypeForClass(entity.getClass());
    if (!expectedType.equals(entity.getNodeType())) {
        throw new ValidationException("Node type mismatch");
    }
}
```

#### Protocol-Specific Validations

**CAN Node Validation**:
```java
// Bitrate validation
if (entity.getBitrate() != null) {
    Set<Integer> validBitrates = Set.of(125000, 250000, 500000, 1000000);
    if (!validBitrates.contains(entity.getBitrate())) {
        throw new ValidationException("Invalid CAN bitrate");
    }
}

// Interface name validation  
if (entity.getCanInterface() != null) {
    if (!entity.getCanInterface().matches("^(can|vcan)\\d+$")) {
        throw new ValidationException("Invalid CAN interface name");
    }
}
```

**Modbus Node Validation**:
```java
// Slave ID range
if (entity.getSlaveId() != null) {
    if (entity.getSlaveId() < 1 || entity.getSlaveId() > 247) {
        throw new ValidationException("Modbus slave ID must be 1-247");
    }
}

// Baud rate validation
Set<Integer> validBaudRates = Set.of(9600, 19200, 38400, 57600, 115200);
if (!validBaudRates.contains(entity.getBaudRate())) {
    throw new ValidationException("Invalid baud rate");
}
```

### Cross-Entity Validation

#### Device-Node Consistency
```java
public class CBabNodeService {
    
    @Override
    protected void validateEntity(final CBabNode entity) {
        super.validateEntity(entity);
        
        // Ensure device belongs to same company as user
        CUser currentUser = sessionService.getActiveUser().orElseThrow();
        if (!entity.getDevice().getCompany().equals(currentUser.getCompany())) {
            throw new SecurityException("Node device must belong to user's company");
        }
        
        // Check device capacity (max nodes per device)
        long nodeCount = repository.countByDevice(entity.getDevice());
        if (nodeCount >= MAX_NODES_PER_DEVICE) {
            throw new ValidationException("Device has reached maximum node capacity");
        }
    }
}
```

---

## Database Schema

### Primary Tables

#### cbab_device
```sql
CREATE TABLE cbab_device (
    device_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL UNIQUE,  -- One device per company constraint
    name VARCHAR(255) NOT NULL,
    device_serial_number VARCHAR(100),
    device_type VARCHAR(50) NOT NULL,
    last_heartbeat TIMESTAMP,
    device_status VARCHAR(20) DEFAULT 'INACTIVE',
    ip_address VARCHAR(45),
    created_by_id BIGINT,
    active BOOLEAN DEFAULT true,
    created_date TIMESTAMP DEFAULT NOW(),
    last_modified_date TIMESTAMP DEFAULT NOW(),
    
    CONSTRAINT fk_device_company FOREIGN KEY (company_id) REFERENCES ccompany(company_id),
    CONSTRAINT fk_device_created_by FOREIGN KEY (created_by_id) REFERENCES cuser(user_id),
    CONSTRAINT uk_device_company UNIQUE (company_id),
    CONSTRAINT chk_device_status CHECK (device_status IN ('ACTIVE', 'INACTIVE', 'ERROR'))
);
```

#### cbab_node (Base table for all node types)
```sql  
CREATE TABLE cbab_node (
    node_id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    node_type VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT false,
    node_status VARCHAR(50),
    created_by_id BIGINT,
    active BOOLEAN DEFAULT true,
    created_date TIMESTAMP DEFAULT NOW(),
    last_modified_date TIMESTAMP DEFAULT NOW(),
    
    CONSTRAINT fk_node_device FOREIGN KEY (device_id) REFERENCES cbab_device(device_id),
    CONSTRAINT fk_node_created_by FOREIGN KEY (created_by_id) REFERENCES cuser(user_id),
    CONSTRAINT chk_node_type CHECK (node_type IN ('CAN', 'ETHERNET', 'MODBUS', 'ROS')),
    CONSTRAINT uk_node_device_name UNIQUE (device_id, name)
);
```

#### cbab_node_can (CAN-specific fields)
```sql
CREATE TABLE cbab_node_can (
    node_id BIGINT PRIMARY KEY,
    bitrate INTEGER DEFAULT 500000,
    can_interface VARCHAR(10) DEFAULT 'can0', 
    extended_frames BOOLEAN DEFAULT false,
    
    CONSTRAINT fk_can_node FOREIGN KEY (node_id) REFERENCES cbab_node(node_id),
    CONSTRAINT chk_can_bitrate CHECK (bitrate IN (125000, 250000, 500000, 1000000)),
    CONSTRAINT chk_can_interface CHECK (can_interface ~ '^(can|vcan)\d+$')
);
```

#### cbab_node_ethernet (Ethernet-specific fields)
```sql
CREATE TABLE cbab_node_ethernet (
    node_id BIGINT PRIMARY KEY,
    ip_address VARCHAR(45),
    port INTEGER DEFAULT 44818,
    subnet_mask VARCHAR(45),
    mac_address VARCHAR(17),
    
    CONSTRAINT fk_ethernet_node FOREIGN KEY (node_id) REFERENCES cbab_node(node_id),
    CONSTRAINT chk_ethernet_port CHECK (port BETWEEN 1024 AND 65535),
    CONSTRAINT chk_ethernet_mac CHECK (mac_address ~ '^([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}$|^$')
);
```

#### cbab_node_modbus (Modbus-specific fields)
```sql  
CREATE TABLE cbab_node_modbus (
    node_id BIGINT PRIMARY KEY,
    serial_port VARCHAR(20) DEFAULT '/dev/ttyUSB0',
    baud_rate INTEGER DEFAULT 115200,
    parity VARCHAR(10) DEFAULT 'NONE',
    data_bits INTEGER DEFAULT 8,
    stop_bits INTEGER DEFAULT 1,
    slave_id INTEGER DEFAULT 1,
    ip_address VARCHAR(45),        -- For Modbus TCP
    port INTEGER DEFAULT 502,      -- For Modbus TCP
    
    CONSTRAINT fk_modbus_node FOREIGN KEY (node_id) REFERENCES cbab_node(node_id),
    CONSTRAINT chk_modbus_baud CHECK (baud_rate IN (9600, 19200, 38400, 57600, 115200)),
    CONSTRAINT chk_modbus_parity CHECK (parity IN ('NONE', 'EVEN', 'ODD')),
    CONSTRAINT chk_modbus_data_bits CHECK (data_bits IN (7, 8)),
    CONSTRAINT chk_modbus_stop_bits CHECK (stop_bits IN (1, 2)),
    CONSTRAINT chk_modbus_slave_id CHECK (slave_id BETWEEN 1 AND 247)
);
```

#### cbab_node_ros (ROS-specific fields)  
```sql
CREATE TABLE cbab_node_ros (
    node_id BIGINT PRIMARY KEY,
    ros_version VARCHAR(10) DEFAULT 'ROS2',
    master_uri VARCHAR(255) DEFAULT 'http://localhost:11311',
    node_namespace VARCHAR(100) DEFAULT '/gateway',
    qos_profile INTEGER DEFAULT 0,
    
    CONSTRAINT fk_ros_node FOREIGN KEY (node_id) REFERENCES cbab_node(node_id),
    CONSTRAINT chk_ros_version CHECK (ros_version IN ('ROS1', 'ROS2')),
    CONSTRAINT chk_ros_qos CHECK (qos_profile >= 0)
);
```

### Indexes for Performance

```sql
-- Device queries
CREATE INDEX idx_device_company ON cbab_device(company_id);
CREATE INDEX idx_device_status ON cbab_device(device_status);
CREATE INDEX idx_device_heartbeat ON cbab_device(last_heartbeat);

-- Node queries  
CREATE INDEX idx_node_device ON cbab_node(device_id);
CREATE INDEX idx_node_type ON cbab_node(node_type);
CREATE INDEX idx_node_enabled ON cbab_node(enabled);
CREATE INDEX idx_node_device_type ON cbab_node(device_id, node_type);
```

---

**Document Control**:
- **Version**: 1.0
- **Created**: 2026-01-29
- **Next Review**: 2026-04-29  
- **Classification**: Technical Reference