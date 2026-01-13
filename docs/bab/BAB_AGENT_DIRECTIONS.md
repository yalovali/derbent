# BAB Agent Directions - AI Assistant Guidelines

## Overview

This document provides AI assistant (GitHub Copilot) guidelines specific to the BAB IoT Gateway project. These directions **supplement** the core Derbent project guidelines and should be referenced when working with BAB-specific code.

---

## Profile Context

**When `bab` profile is active:**
- Use BAB-specific entity base classes (CBabItem instead of CProjectItem)
- Reference BAB documentation in `/docs/bab/` folder
- Follow BAB entity hierarchy patterns
- Consider device-centric architecture (single CBabDevice per database)

**Profile Detection:**
```java
@Profile("bab")  // BAB-specific beans and views
```

---

## Entity Design Guidelines

### Base Class Selection

**ALWAYS use this decision tree for BAB entities:**

```
Is this a BAB entity?
└─ YES → Extend CBabItem<T>
    │
    ├─ Is it the device itself?
    │  └─ YES → Extend CBabDeviceBase → CBabDevice
    │
    ├─ Is it a communication node?
    │  └─ YES → Extend CBabNode → Specific node type
    │      ├─ CBabNodeCAN
    │      ├─ CBabNodeModbus
    │      ├─ CBabNodeEthernet
    │      └─ CBabNodeROS
    │
    └─ Is it a configuration?
       └─ YES → Extend CBabConfiguration → Specific config type
           ├─ CBabNetworkConfig
           ├─ CBabSystemConfig
           └─ CBabProtocolConfig
```

### Entity Relationships

**Device is the root:**
```java
CBabDevice (unique, one per database)
  ├── @OneToMany CBabNode (multiple communication interfaces)
  ├── @OneToMany CBabConfiguration (system settings)
  ├── @ManyToOne CCompany (company ownership)
  └── @ManyToOne CUser (primary device admin)
```

---

## Coding Standards for BAB

### Entity Constants (MANDATORY)

Every BAB entity MUST define:

```java
public class CBabNodeCAN extends CBabNode {
    public static final String DEFAULT_COLOR = "#FF5722";
    public static final String DEFAULT_ICON = "vaadin:car";
    public static final String ENTITY_TITLE_PLURAL = "CAN Nodes";
    public static final String ENTITY_TITLE_SINGULAR = "CAN Node";
    private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeCAN.class);
    public static final String VIEW_NAME = "CAN Node Configuration";
}
```

### Service Layer Pattern

```java
@Service
@Profile("bab")
public class CBabNodeService extends CAbstractService<CBabNode> {
    
    private final IBabNodeRepository repository;
    private final ISessionService sessionService;
    
    public CBabNodeService(IBabNodeRepository repository, 
                          Clock clock, 
                          ISessionService sessionService) {
        super(repository, clock, sessionService);
        this.repository = repository;
        this.sessionService = sessionService;
    }
    
    @Transactional(readOnly = true)
    public List<CBabNode> findByDevice(CBabDevice device) {
        Objects.requireNonNull(device, "Device cannot be null");
        return repository.findByDevice(device);
    }
}
```

### Repository Pattern

```java
@Repository
@Profile("bab")
public interface IBabNodeRepository extends IAbstractRepository<CBabNode> {
    
    @Query("SELECT e FROM #{#entityName} e WHERE e.device = :device ORDER BY e.name ASC")
    List<CBabNode> findByDevice(@Param("device") CBabDevice device);
    
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.device = :device")
    Long countByDevice(@Param("device") CBabDevice device);
}
```

---

## View Implementation

### Dashboard View Structure

```java
@Route(value = "home", registerAtStartup = false)
@PageTitle("Dashboard")
@PermitAll
@Profile("bab")
public final class CBabDashboardView extends CAbstractPage {
    
    private static final long serialVersionUID = 1L;
    public static final String VIEW_NAME = "BAB Dashboard View";
    
    private CBabDevice device;
    
    @Override
    protected void initPage() {
        // Load unique device
        device = deviceService.getUniqueDevice()
            .orElse(null);
        
        if (device == null) {
            add(new CSpan("No device configured"));
            return;
        }
        
        // Build dashboard with device info
        add(create_deviceHeader());
        add(create_nodeStatusCards());
        add(create_configurationPanel());
    }
}
```

---

## Testing Requirements

### Playwright Tests for BAB

```bash
# Run BAB-specific tests
./run-playwright-tests.sh bab-dashboard
./run-playwright-tests.sh bab-nodes
./run-playwright-tests.sh bab-config
```

### Test Scenarios

**MUST verify:**
- ✅ Dashboard loads with device information
- ✅ Device uniqueness constraint (only one CBabDevice)
- ✅ Node CRUD operations
- ✅ Configuration validation
- ✅ Real-time status updates

---

## Database Considerations

### Unique Device Pattern

```java
@Entity
@Table(name = "cbab_device", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"company_id"})
})
public class CBabDevice extends CBabDeviceBase {
    // Ensures only one device per company
}
```

### Sample Data Initialization

```java
@Component
@Profile("bab")
public class CBabDataInitializer {
    
    public void initializeSampleData() {
        // Create unique device if not exists
        if (deviceService.count() == 0) {
            CBabDevice device = createSampleDevice();
            deviceService.save(device);
            
            // Add sample nodes
            createSampleNodes(device);
            
            // Add sample configurations
            createSampleConfigurations(device);
        }
    }
}
```

---

## Common Patterns

### Device Retrieval

```java
// Service method to get unique device
public Optional<CBabDevice> getUniqueDevice() {
    CCompany company = getCurrentCompany();
    return repository.findByCompany(company).stream().findFirst();
}

// Ensure device exists
public CBabDevice getOrCreateDevice() {
    return getUniqueDevice()
        .orElseGet(() -> {
            CBabDevice device = new CBabDevice("Default Gateway", getCurrentCompany());
            return save(device);
        });
}
```

### Node Management

```java
// Add node to device
public CBabNode addNode(CBabDevice device, String nodeType) {
    CBabNode node = createNodeByType(nodeType);
    node.setDevice(device);
    return nodeService.save(node);
}

// Factory for node types
private CBabNode createNodeByType(String type) {
    return switch (type) {
        case "CAN" -> new CBabNodeCAN();
        case "Modbus" -> new CBabNodeModbus();
        case "Ethernet" -> new CBabNodeEthernet();
        case "ROS" -> new CBabNodeROS();
        default -> throw new IllegalArgumentException("Unknown node type: " + type);
    };
}
```

---

## Validation Workflow

### Before Committing BAB Changes

```bash
# 1. Setup environment
source ./bin/setup-java-env.sh

# 2. Compile with BAB profile
mvn clean compile -Pbab

# 3. Run BAB-specific tests
./run-playwright-tests.sh bab-comprehensive

# 4. Verify dashboard loads
mvn spring-boot:run -Dspring.profiles.active=bab,h2 &
sleep 20
curl -s http://localhost:8080/home
pkill -f spring-boot:run

# 5. Commit with BAB prefix
git add [files]
git commit -m "feat(bab): descriptive message"
```

---

## Documentation References

### When Working on BAB Features

**ALWAYS reference in this order:**
1. This file (`BAB_AGENT_DIRECTIONS.md`) - BAB-specific patterns
2. `BAB_PROJECT_OVERVIEW.md` - Architecture and design
3. `../architecture/ENTITY_INHERITANCE_AND_DESIGN_PATTERNS.md` - Base patterns
4. `../architecture/coding-standards.md` - General standards
5. `../development/copilot-guidelines.md` - Development workflow

### BAB Documentation Structure

```
docs/bab/
├── BAB_PROJECT_OVERVIEW.md           # Architecture and overview
├── BAB_AGENT_DIRECTIONS.md           # This file (AI guidelines)
├── BAB_ENTITY_DESIGN.md              # Domain model details
├── BAB_API_REFERENCE.md              # Service layer docs
└── BAB_TESTING_GUIDE.md              # Testing strategies
```

---

## Key Differences from Derbent Core

| Aspect | Derbent Core | BAB Profile |
|--------|--------------|-------------|
| Base Entity | CProjectItem | CBabItem |
| Scope | Project-based | Device-based |
| Multi-Instance | Many projects | One device per DB |
| Authentication | User + Company + Project | User + Company |
| Dashboard | Project dashboard | Device dashboard |
| Main Navigation | Project selector | Device info header |

---

## Next Steps for AI Agents

When creating BAB features:
1. Check if entity extends CBabItem hierarchy
2. Add @Profile("bab") to all beans and views
3. Ensure device uniqueness in repositories
4. Add BAB-specific Playwright tests
5. Update BAB documentation folder
6. Commit with "feat(bab):" prefix

---

**Last Updated**: 2026-01-13  
**Applies to**: BAB Profile Only  
**Base Framework**: Derbent API / Spring Boot 3.5 / Vaadin 24.8
