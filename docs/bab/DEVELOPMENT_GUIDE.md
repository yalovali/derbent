# BAB Development Guide

**Version**: 1.0  
**Date**: 2026-01-29  
**Status**: ACTIVE - MANDATORY for BAB Development

---

## Table of Contents

1. [Development Environment Setup](#development-environment-setup)
2. [BAB-Specific Coding Standards](#bab-specific-coding-standards)
3. [Entity Development Patterns](#entity-development-patterns)
4. [Service Layer Guidelines](#service-layer-guidelines)
5. [Testing Standards](#testing-standards)
6. [Deployment Guidelines](#deployment-guidelines)
7. [Troubleshooting](#troubleshooting)

---

## Development Environment Setup

### Prerequisites

#### Required Software
```bash
# Java Development
java --version    # OpenJDK 17 or 21
mvn --version     # Maven 3.8+

# Database  
# Embedded: H2 (included)
# Production: PostgreSQL 13+

# Development Tools (Optional)
docker --version  # For PostgreSQL testing
```

#### IDE Configuration

**IntelliJ IDEA / Eclipse**:
```xml
<!-- Code formatter: eclipse-formatter.xml -->
<setting name="USE_TAB_CHARACTER" value="false" />
<setting name="TAB_SIZE" value="4" />
<setting name="INDENT_SIZE" value="4" />
<setting name="RIGHT_MARGIN" value="140" />
```

### Project Setup

#### Clone and Build
```bash
git clone <repository-url>
cd derbent

# Build with BAB profile
mvn clean compile -Pagents -DskipTests

# Run with BAB profile
mvn spring-boot:run -Dspring.profiles.active=bab,h2
```

#### Profile Configuration

**application-bab.yml** (for BAB development):
```yaml
spring:
  profiles:
    active: bab,h2
  datasource:
    url: jdbc:h2:mem:bab_dev;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driverClassName: org.h2.Driver
    username: sa
    password: 

# BAB-specific settings
bab:
  device:
    max-nodes-per-device: 10
    heartbeat-interval-seconds: 30
  protocols:
    can:
      default-bitrate: 500000
      default-interface: can0
    ethernet:
      default-port: 44818
    modbus:
      default-baud-rate: 115200
      default-slave-id: 1
    ros:
      default-version: ROS2
      default-namespace: /gateway

logging:
  level:
    tech.derbent.bab: DEBUG
    org.hibernate.SQL: DEBUG  # For query debugging
```

---

## BAB-Specific Coding Standards

### 1. Profile Isolation (MANDATORY)

**Every BAB component MUST be marked with `@Profile("bab")`**:

```java
// ✅ CORRECT - Service with BAB profile
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CBabDeviceService extends CEntityOfCompanyService<CBabDevice> {
    // Implementation
}

// ✅ CORRECT - Abstract service (no @Service, but profile annotation)
@Profile("bab")  
@PreAuthorize("isAuthenticated()")
public abstract class CBabNodeService<NodeType extends CBabNode<NodeType>> 
    extends CEntityOfCompanyService<NodeType> {
    // Abstract implementation
}

// ✅ CORRECT - Repository with profile
@Profile("bab")
public interface IBabDeviceRepository extends IEntityOfCompanyRepository<CBabDevice> {
    // Query methods
}

// ❌ WRONG - Missing profile annotation
@Service  // Will be active in ALL profiles!
public class CBabDeviceService {
    // This will interfere with PLM profile
}
```

### 2. Entity Naming Convention

**All BAB entities MUST use "Bab" prefix**:

```java
// ✅ CORRECT - Clear BAB identification
public class CBabDevice extends CEntityOfCompany<CBabDevice> { }
public class CBabNodeCAN extends CBabNode<CBabNodeCAN> { }
public class CProject_Bab extends CProject<CProject_Bab> { }

// ❌ WRONG - No BAB identification  
public class CDevice extends CEntityOfCompany<CDevice> { }  // Conflicts with PLM
public class CNode extends CEntityOfCompany<CNode> { }      // Too generic
```

### 3. No Workflow Implementation (CRITICAL)

**BAB entities MUST NOT implement `IHasStatusAndWorkflow`**:

```java
// ✅ CORRECT - BAB entities are simple
public class CBabDevice extends CEntityOfCompany<CBabDevice> {
    // Simple status field, not workflow
    private String deviceStatus; // ACTIVE, INACTIVE, ERROR
}

// ❌ WRONG - BAB entities don't use workflows  
public class CBabDevice extends CEntityOfCompany<CBabDevice> 
    implements IHasStatusAndWorkflow<CBabDevice> {
    // This adds unnecessary PLM complexity
}
```

### 4. Company-Scoped Design

**All BAB entities are company-scoped (not project-scoped)**:

```java
// ✅ CORRECT - Extend CEntityOfCompany
public class CBabDevice extends CEntityOfCompany<CBabDevice> {
    // Inherits company relationship
}

// ❌ WRONG - BAB doesn't use project scoping
public class CBabDevice extends CProjectItem<CBabDevice> {
    // Project scoping is PLM-specific
}
```

### 5. Abstract Entity Pattern

**Use `@MappedSuperclass` for abstract BAB entities**:

```java
// ✅ CORRECT - Abstract entity pattern
@MappedSuperclass  // NOT @Entity
public abstract class CBabNode<EntityClass> extends CEntityOfCompany<EntityClass> {
    
    // Common fields for all node types
    protected CBabNode() {
        super();
        // Abstract entities do NOT call initializeDefaults()
    }
    
    protected CBabNode(Class<EntityClass> clazz, String name, String nodeType) {
        super(clazz, name);
        this.nodeType = nodeType;
        // Abstract constructors do NOT call initializeDefaults()
    }
}

// ✅ CORRECT - Concrete entity pattern
@Entity
@Table(name = "cbab_node_can")
public class CBabNodeCAN extends CBabNode<CBabNodeCAN> {
    
    protected CBabNodeCAN() {
        super();
        // JPA constructor - NO initializeDefaults() call
    }
    
    public CBabNodeCAN(String name, String nodeType) {
        super(CBabNodeCAN.class, name, nodeType);
        initializeDefaults();  // Business constructors call this
    }
    
    private final void initializeDefaults() {
        // Set BAB-specific defaults
        enabled = false;
        bitrate = 500000;
        canInterface = "can0";
        extendedFrames = false;
        
        // Call service initialization
        CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
    }
}
```

---

## Entity Development Patterns

### 1. New BAB Entity Checklist

When creating a new BAB entity, ensure:

- [ ] **Naming**: Starts with "CBab" or ends with "_Bab"
- [ ] **Profile**: Marked with `@Profile("bab")`
- [ ] **Base Class**: Extends appropriate base (`CEntityOfCompany`, `CBabNode`)
- [ ] **Constants**: All required entity constants defined
- [ ] **Table Constraints**: Unique constraints for business rules
- [ ] **Initialization**: Proper constructor and `initializeDefaults()` pattern
- [ ] **Validation**: Entity-specific validation rules
- [ ] **Tests**: Unit tests for entity behavior

### 2. Entity Constants Template

```java
public class CBabNewEntity extends CEntityOfCompany<CBabNewEntity> {
    
    // MANDATORY - Entity constants
    public static final String DEFAULT_COLOR = "#6B5FA7";    // BAB purple theme
    public static final String DEFAULT_ICON = "vaadin:cogs";  // Appropriate icon
    public static final String ENTITY_TITLE_SINGULAR = "New Entity";
    public static final String ENTITY_TITLE_PLURAL = "New Entities";
    public static final String VIEW_NAME = "New Entity Management";
    
    // Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(CBabNewEntity.class);
}
```

### 3. Field Definition Patterns

#### Standard BAB Fields
```java
// Name field (inherited from CEntityNamed, but with BAB validation)
@Column(nullable = false, length = 255)
@Size(max = 255)
@NotBlank(message = "Name is required")
@AMetaData(
    displayName = "Name",
    required = true,
    readOnly = false,
    description = "Entity name",
    order = 10
)
private String name;

// Boolean configuration field
@Column(name = "enabled", nullable = false)
@AMetaData(
    displayName = "Enabled",
    required = true,
    readOnly = false,
    defaultValue = "false",
    description = "Whether this entity is enabled",
    order = 20
)
private Boolean enabled = false;

// IP Address field  
@Column(name = "ip_address", length = 45)
@Pattern(
    regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
             "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|" +
             "^([0-9a-fA-F]{0,4}:){7}[0-9a-fA-F]{0,4}$|^$",
    message = "Invalid IP address format (IPv4 or IPv6)"
)
@AMetaData(
    displayName = "IP Address",
    required = false,
    readOnly = false,
    description = "IPv4 or IPv6 address",
    maxLength = 45,
    order = 30
)
private String ipAddress = "";

// Device relationship (for node entities)
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "device_id", nullable = false)
@AMetaData(
    displayName = "Device",
    required = true,
    readOnly = true,
    description = "Device this entity belongs to",
    order = 40
)
private CBabDevice device;
```

### 4. Constructor Patterns

```java
public class CBabNewEntity extends CEntityOfCompany<CBabNewEntity> {
    
    /** Default constructor for JPA */
    protected CBabNewEntity() {
        super();
        // NO initializeDefaults() call in JPA constructor
    }
    
    /** Business constructor */
    public CBabNewEntity(String name, CCompany company) {
        super(CBabNewEntity.class, name, company);
        initializeDefaults();  // MANDATORY in business constructors
    }
    
    /** Initialize default values */
    private final void initializeDefaults() {
        // Set BAB-specific defaults
        enabled = false;
        deviceStatus = "INACTIVE";
        
        // Initialize collections (already done in field declarations)
        // attachments = new HashSet<>();  // Don't do this here!
        
        // MANDATORY: Call service initialization
        CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
    }
}
```

---

## Service Layer Guidelines

### 1. Service Class Structure

```java
@Service("CBabNewEntityService")  // Explicit bean name for clarity
@Profile("bab")                   // MANDATORY profile annotation
@PreAuthorize("isAuthenticated()") 
public class CBabNewEntityService extends CEntityOfCompanyService<CBabNewEntity> 
        implements IEntityRegistrable, IEntityWithView {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CBabNewEntityService.class);
    
    // Dependencies (constructor injection)
    private final CBabDeviceService deviceService;
    
    public CBabNewEntityService(
            final IBabNewEntityRepository repository,
            final Clock clock,
            final ISessionService sessionService,
            final CBabDeviceService deviceService) {
        super(repository, clock, sessionService);
        this.deviceService = deviceService;
    }
    
    // MANDATORY interface implementations
    @Override
    public Class<CBabNewEntity> getEntityClass() {
        return CBabNewEntity.class;
    }
    
    @Override
    public Class<?> getInitializerServiceClass() {
        return CBabNewEntityInitializerService.class;
    }
    
    @Override
    public Class<?> getPageServiceClass() {
        return CPageServiceBabNewEntity.class;
    }
    
    @Override
    public Class<?> getServiceClass() {
        return this.getClass();
    }
}
```

### 2. BAB Service Initialization Pattern

```java
@Override
public void initializeNewEntity(final Object entity) {
    super.initializeNewEntity(entity);  // ALWAYS call parent first
    
    LOGGER.debug("Initializing new BAB entity: {}", entity.getClass().getSimpleName());
    
    // Get company context (NOT project - BAB is company-scoped)
    final CCompany company = sessionService.getActiveCompany()
        .orElseThrow(() -> new CInitializationException("No active company"));
    
    // BAB entities do NOT use workflow initialization
    // DO NOT call: initializeNewEntity_IHasStatusAndWorkflow()
    
    // Set BAB-specific context
    if (entity instanceof CBabNewEntity) {
        final CBabNewEntity babEntity = (CBabNewEntity) entity;
        
        // Set device reference (for node entities)
        if (babEntity instanceof CBabNode) {
            final CBabDevice device = deviceService.getOrCreateCompanyDevice(company);
            ((CBabNode<?>) babEntity).setDevice(device);
        }
        
        // Set other context-dependent fields
        babEntity.setCreatedBy(sessionService.getActiveUser().orElse(null));
    }
    
    LOGGER.debug("BAB entity initialization complete");
}
```

### 3. Validation Patterns

```java
@Override
protected void validateEntity(final CBabNewEntity entity) {
    super.validateEntity(entity);
    
    // 1. Required Fields (BAB entities need name validation)
    Check.notBlank(entity.getName(), "Name is required");
    
    // 2. Length Checks
    if (entity.getName().length() > 255) {
        throw new IllegalArgumentException("Name cannot exceed 255 characters");
    }
    
    // 3. BAB-specific business rules
    if (entity.getDevice() != null) {
        // Ensure device belongs to same company
        if (!entity.getDevice().getCompany().equals(entity.getCompany())) {
            throw new IllegalArgumentException("Device must belong to same company");
        }
    }
    
    // 4. Unique constraints (mirror database constraints)
    if (entity instanceof CBabDevice) {
        // Only one device per company
        Optional<CBabDevice> existing = ((IBabDeviceRepository) repository)
            .findByCompany(entity.getCompany());
        if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
            throw new IllegalArgumentException("Company already has a device");
        }
    }
}
```

### 4. Abstract Service Pattern

```java
// Abstract service for node hierarchy
@Profile("bab")
@PreAuthorize("isAuthenticated()")
// NO @Service annotation on abstract classes!
public abstract class CBabNodeService<NodeType extends CBabNode<NodeType>> 
        extends CEntityOfCompanyService<NodeType> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeService.class);
    
    protected CBabNodeService(
            final IBabNodeRepository<NodeType> repository,
            final Clock clock,
            final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }
    
    // Common node operations
    public abstract List<NodeType> findByDevice(CBabDevice device);
    
    public List<NodeType> findEnabledByDevice(CBabDevice device) {
        return ((IBabNodeRepository<NodeType>) repository).findByDeviceAndEnabled(device, true);
    }
    
    @Transactional
    public void enableNode(NodeType node) {
        node.setEnabled(true);
        node.setNodeStatus("ACTIVE");
        save(node);
        LOGGER.info("Enabled node: {} on device: {}", node.getName(), node.getDevice().getName());
    }
    
    @Transactional
    public void disableNode(NodeType node) {
        node.setEnabled(false);
        node.setNodeStatus("INACTIVE");
        save(node);
        LOGGER.info("Disabled node: {} on device: {}", node.getName(), node.getDevice().getName());
    }
}

// Concrete service implementation
@Service("CBabNodeCANService")
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CBabNodeCANService extends CBabNodeService<CBabNodeCAN> 
        implements IEntityRegistrable, IEntityWithView {
    
    public CBabNodeCANService(
            final IBabNodeCANRepository repository,
            final Clock clock,
            final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }
    
    @Override
    public List<CBabNodeCAN> findByDevice(CBabDevice device) {
        return ((IBabNodeCANRepository) repository).findByDevice(device);
    }
    
    // CAN-specific methods
    public List<CBabNodeCAN> findByBitrate(Integer bitrate) {
        return ((IBabNodeCANRepository) repository).findByBitrate(bitrate);
    }
    
    @Override
    public Class<CBabNodeCAN> getEntityClass() {
        return CBabNodeCAN.class;
    }
    
    // Other interface methods...
}
```

---

## Testing Standards

### 1. Unit Testing Patterns

#### Entity Tests
```java
@ExtendWith(MockitoExtension.class)
@Profile("bab")
class CBabDeviceTest {
    
    private CBabDevice device;
    private CCompany company;
    
    @BeforeEach
    void setUp() {
        company = new CCompany("Test Company");
        device = new CBabDevice("Test Device", company);
    }
    
    @Test
    void testDeviceCreation() {
        assertThat(device.getName()).isEqualTo("Test Device");
        assertThat(device.getCompany()).isEqualTo(company);
        assertThat(device.getDeviceStatus()).isEqualTo("INACTIVE");
        assertThat(device.getEnabled()).isFalse();
    }
    
    @Test
    void testDeviceInitialization() {
        // Test that initializeDefaults sets proper values
        assertThat(device.getDeviceType()).isNotNull();
        assertThat(device.getLastHeartbeat()).isNull(); // Not set until first heartbeat
    }
    
    @Test
    void testUniqueCompanyConstraint() {
        CBabDevice secondDevice = new CBabDevice("Second Device", company);
        
        // Business logic should prevent multiple devices per company
        // This is enforced in service layer, not entity
        assertThat(device.getCompany()).isEqualTo(secondDevice.getCompany());
    }
}
```

#### Service Tests  
```java
@SpringBootTest
@ActiveProfiles({"bab", "test"})
@Transactional
class CBabDeviceServiceTest {
    
    @Autowired
    private CBabDeviceService deviceService;
    
    @Autowired
    private TestEntityManager entityManager;
    
    private CCompany company;
    
    @BeforeEach
    void setUp() {
        company = new CCompany("Test Company");
        entityManager.persistAndFlush(company);
    }
    
    @Test
    void testCreateDevice() {
        CBabDevice device = deviceService.newEntity();
        device.setName("Test Device");
        device.setDeviceType("Gateway");
        device.setCompany(company);
        
        CBabDevice saved = deviceService.save(device);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Device");
        assertThat(saved.getCompany()).isEqualTo(company);
    }
    
    @Test
    void testUniqueCompanyConstraint() {
        // First device
        CBabDevice device1 = deviceService.newEntity();
        device1.setName("Device 1");
        device1.setCompany(company);
        deviceService.save(device1);
        
        // Second device for same company should fail
        CBabDevice device2 = deviceService.newEntity();
        device2.setName("Device 2");
        device2.setCompany(company);
        
        assertThatThrownBy(() -> deviceService.save(device2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Company already has a device");
    }
}
```

### 2. Integration Testing

#### BAB Profile Test Configuration
```java
@SpringBootTest
@ActiveProfiles({"bab", "test"})
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:bab_test",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "logging.level.tech.derbent.bab=DEBUG"
})
class BabIntegrationTest {
    
    @Autowired
    private CBabDeviceService deviceService;
    
    @Autowired  
    private CBabNodeCANService canNodeService;
    
    @Test
    void testFullDeviceNodeWorkflow() {
        // 1. Create company and device
        CCompany company = new CCompany("Gateway Company");
        CBabDevice device = deviceService.newEntity();
        device.setName("Main Gateway");
        device.setCompany(company);
        device = deviceService.save(device);
        
        // 2. Create CAN node
        CBabNodeCAN canNode = canNodeService.newEntity();
        canNode.setName("CAN0 Interface");
        canNode.setDevice(device);
        canNode.setBitrate(500000);
        canNode = canNodeService.save(canNode);
        
        // 3. Verify relationships
        assertThat(canNode.getDevice()).isEqualTo(device);
        assertThat(canNode.getBitrate()).isEqualTo(500000);
        assertThat(canNode.getEnabled()).isFalse();
        
        // 4. Enable node
        canNodeService.enableNode(canNode);
        
        CBabNodeCAN updated = canNodeService.findById(canNode.getId()).orElseThrow();
        assertThat(updated.getEnabled()).isTrue();
        assertThat(updated.getNodeStatus()).isEqualTo("ACTIVE");
    }
}
```

### 3. BAB-Specific Test Utilities

```java
@TestComponent
@Profile("test")
public class BabTestUtils {
    
    @Autowired
    private CBabDeviceService deviceService;
    
    @Autowired
    private TestEntityManager entityManager;
    
    /**
     * Create a test company with a device and nodes for testing.
     */
    public BabTestContext createTestContext(String companyName) {
        // Create company
        CCompany company = new CCompany(companyName);
        entityManager.persistAndFlush(company);
        
        // Create device  
        CBabDevice device = deviceService.newEntity();
        device.setName("Test Device");
        device.setCompany(company);
        device = deviceService.save(device);
        
        return new BabTestContext(company, device);
    }
    
    public static class BabTestContext {
        private final CCompany company;
        private final CBabDevice device;
        
        public BabTestContext(CCompany company, CBabDevice device) {
            this.company = company;
            this.device = device;
        }
        
        // Getters
        public CCompany getCompany() { return company; }
        public CBabDevice getDevice() { return device; }
    }
}
```

---

## Deployment Guidelines

### 1. Profile Configuration

#### Development Environment
```yaml
# application-bab-dev.yml
spring:
  profiles:
    active: bab,h2,development
  datasource:
    url: jdbc:h2:file:./data/bab_dev;AUTO_SERVER=true
    username: sa
    password: 
  h2:
    console:
      enabled: true
      path: /h2-console

bab:
  development:
    mock-protocols: true    # Mock CAN/Modbus for development
    simulation-mode: true   # Generate simulated data
```

#### Production Environment  
```yaml
# application-bab-prod.yml
spring:
  profiles:
    active: bab,postgres,production
  datasource:
    url: jdbc:postgresql://localhost:5432/bab_gateway
    username: ${DB_USERNAME:bab_user}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate  # Never auto-create in production
    show-sql: false

bab:
  production:
    mock-protocols: false
    security:
      require-https: true
      session-timeout-minutes: 60
```

### 2. Docker Deployment

#### Dockerfile for BAB Gateway
```dockerfile
FROM openjdk:17-jre-slim

# System dependencies for protocols
RUN apt-get update && apt-get install -y \
    can-utils \
    socat \
    && rm -rf /var/lib/apt/lists/*

# Application setup
WORKDIR /app
COPY target/derbent-*.jar app.jar

# Protocol configuration
VOLUME ["/app/config", "/app/data"]

# Ports
EXPOSE 8080 44818 502

# Environment
ENV SPRING_PROFILES_ACTIVE=bab,postgres
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### Docker Compose for Complete Stack
```yaml
# docker-compose.bab.yml
version: '3.8'
services:
  bab-gateway:
    build: .
    ports:
      - "8080:8080"      # Web UI
      - "44818:44818"    # EtherNet/IP
      - "502:502"        # Modbus TCP
    environment:
      SPRING_PROFILES_ACTIVE: bab,postgres
      DB_HOST: postgres
      DB_NAME: bab_gateway
      DB_USERNAME: bab_user
      DB_PASSWORD: ${DB_PASSWORD}
    depends_on:
      - postgres
    volumes:
      - bab_data:/app/data
      - ./config:/app/config:ro
    networks:
      - bab_network
    devices:
      - "/dev/ttyUSB0:/dev/ttyUSB0"  # Modbus RTU
    cap_add:
      - NET_ADMIN  # For CAN interface management

  postgres:
    image: postgres:13-alpine
    environment:
      POSTGRES_DB: bab_gateway
      POSTGRES_USER: bab_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - bab_network

volumes:
  bab_data:
  postgres_data:

networks:
  bab_network:
    driver: bridge
```

### 3. System Configuration

#### CAN Interface Setup (Linux)
```bash
#!/bin/bash
# setup-can.sh - Configure CAN interfaces

# Load CAN modules
sudo modprobe can
sudo modprobe can_raw
sudo modprobe can_bcm

# Configure CAN0 interface
sudo ip link set can0 type can bitrate 500000
sudo ip link set up can0

# Configure virtual CAN for testing
sudo modprobe vcan
sudo ip link add dev vcan0 type vcan
sudo ip link set up vcan0

echo "CAN interfaces configured:"
ip link show type can
```

#### Systemd Service
```ini
# /etc/systemd/system/bab-gateway.service
[Unit]
Description=BAB IoT Gateway
After=network.target postgresql.service

[Service]
Type=simple
User=bab
WorkingDirectory=/opt/bab
ExecStart=/usr/bin/java -jar /opt/bab/derbent.jar
ExecStop=/bin/kill -TERM $MAINPID
Restart=always
RestartSec=10

# Environment
Environment=SPRING_PROFILES_ACTIVE=bab,postgres
Environment=JAVA_OPTS=-Xmx512m -server

# Security
PrivateTmp=yes
NoNewPrivileges=yes
ProtectHome=yes
ProtectSystem=strict
ReadWritePaths=/opt/bab/data

[Install]
WantedBy=multi-user.target
```

---

## Troubleshooting

### Common Issues

#### 1. Profile Activation Issues
```bash
# Check active profiles
curl http://localhost:8080/actuator/env | jq '.activeProfiles'

# Verify BAB beans are loaded
curl http://localhost:8080/actuator/beans | grep -i bab

# Check configuration
curl http://localhost:8080/actuator/configprops | grep bab
```

#### 2. Database Connection Issues
```bash
# Test H2 connection
curl http://localhost:8080/h2-console

# Check PostgreSQL connection
psql -h localhost -U bab_user -d bab_gateway -c "SELECT version();"

# Verify BAB tables exist
psql -h localhost -U bab_user -d bab_gateway -c "\dt cbab*"
```

#### 3. Protocol Configuration Issues
```bash
# Check CAN interfaces
ip link show type can

# Test CAN communication  
cansend can0 123#DEADBEEF
candump can0

# Check serial ports
ls -la /dev/ttyUSB* /dev/ttyS*

# Test Modbus connection
mbpoll -t 3 -r 1 -c 1 127.0.0.1
```

#### 4. Application Debugging
```yaml
# Enable debug logging
logging:
  level:
    tech.derbent.bab: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    org.springframework.security: DEBUG
```

### Performance Monitoring

#### Health Checks
```java
@Component
@Profile("bab")
public class BabHealthIndicator implements HealthIndicator {
    
    @Autowired
    private CBabDeviceService deviceService;
    
    @Override
    public Health health() {
        try {
            // Check device status
            List<CBabDevice> devices = deviceService.findAll();
            if (devices.isEmpty()) {
                return Health.down()
                    .withDetail("reason", "No devices configured")
                    .build();
            }
            
            // Check node connectivity  
            long activeNodes = devices.stream()
                .flatMap(device -> device.getNodes().stream())
                .filter(node -> "ACTIVE".equals(node.getNodeStatus()))
                .count();
            
            return Health.up()
                .withDetail("devices", devices.size())
                .withDetail("activeNodes", activeNodes)
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

#### Metrics Collection
```java
@Component  
@Profile("bab")
public class BabMetrics {
    
    private final MeterRegistry meterRegistry;
    private final CBabDeviceService deviceService;
    
    public BabMetrics(MeterRegistry meterRegistry, CBabDeviceService deviceService) {
        this.meterRegistry = meterRegistry;
        this.deviceService = deviceService;
        
        // Register custom metrics
        Gauge.builder("bab.devices.total")
            .register(meterRegistry, this, BabMetrics::getDeviceCount);
        
        Gauge.builder("bab.nodes.active")
            .register(meterRegistry, this, BabMetrics::getActiveNodeCount);
    }
    
    private double getDeviceCount(BabMetrics metrics) {
        try {
            return deviceService.findAll().size();
        } catch (Exception e) {
            return -1;
        }
    }
    
    private double getActiveNodeCount(BabMetrics metrics) {
        // Implementation to count active nodes
        return 0; // Placeholder
    }
}
```

---

**Document Control**:
- **Version**: 1.0
- **Created**: 2026-01-29
- **Next Review**: 2026-04-29
- **Classification**: Development Guide