# BAB Profile Coding Rules

**Version**: 1.0  
**Date**: 2026-01-26  
**Status**: MANDATORY - All BAB profile development MUST follow these rules  
**Profile**: `@Profile("bab")` - Building Automation Bus IoT Gateway System

---

## Table of Contents

1. [BAB Profile Overview](#bab-profile-overview)
2. [Entity Design Patterns](#entity-design-patterns)
3. [Service Layer Patterns](#service-layer-patterns)
4. [Repository Patterns](#repository-patterns)
5. [Data Initialization](#data-initialization)
6. [Abstract Entity Hierarchy](#abstract-entity-hierarchy)
7. [Validation Rules](#validation-rules)
8. [UI Patterns](#ui-patterns)
9. [Testing Guidelines](#testing-guidelines)
10. [JSON Network Serialization Rules](#json-network-serialization-rules)
11. [Critical Rules Summary](#critical-rules-summary)

---

## BAB Profile Overview

### Purpose
BAB (Building Automation Bus) profile provides **minimal IoT gateway functionality** for:
- **Device Management**: Single gateway device per company
- **Communication Nodes**: CAN, Ethernet, Modbus, ROS protocol interfaces
- **Project Management**: BAB-specific project types with IP configuration
- **Lightweight Operation**: Minimal entities, no complex PLM features

### Architecture Principles
- ✅ **Minimal Entity Set**: Only essential entities for IoT gateway
- ✅ **Abstract Inheritance**: CBabNode hierarchy for communication protocols  
- ✅ **Company-Scoped**: All entities belong to a single company
- ✅ **No Status/Workflow**: BAB entities do NOT implement `IHasStatusAndWorkflow`
- ✅ **Profile Isolation**: BAB beans only active when `@Profile("bab")`
- ✅ **Constrained Value Fields Use ComboBox**: For limited value sets (including integer sets like baud rate/port), use `@AMetaData(dataProviderBean, dataProviderMethod)` with provider methods returning bounded `List<?>`; avoid free-text inputs for constrained domains.
- ✅ **DataProvider Method Names Are Standardized**: BAB `dataProviderMethod` names must follow canonical prefixes from `docs/development/combobox-data-provider-pattern.md`:
  `getComboValuesOf...`, `buildDataProviderComponent...`, `getCalculatedValueOf...`, `getDataProviderValuesOf...`.

---

## Entity Design Patterns

### 1. BAB Entity Categories

| Category | Entities | Purpose |
|----------|----------|---------|
| **Core** | `CBabDevice`, `CProject_Bab` | Primary business objects |
| **Communication** | `CBabNode*` (abstract hierarchy) | Protocol-specific interfaces |
| **Foundation** | `CCompany`, `CUser`, `CWorkflowEntity` | Required infrastructure |

### 2. Entity Hierarchy Rules

#### ✅ CORRECT - BAB Device (Simple Entity)
```java
@Entity
@Table(name = "cbab_device", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"company_id"})  // One device per company
})
@AttributeOverride(name = "id", column = @Column(name = "device_id"))
public class CBabDevice extends CEntityOfCompany<CBabDevice> {
    
    // MANDATORY constants
    public static final String DEFAULT_COLOR = "#6B5FA7";
    public static final String DEFAULT_ICON = "vaadin:server";
    public static final String ENTITY_TITLE_PLURAL = "Devices";
    public static final String ENTITY_TITLE_SINGULAR = "Device";
    public static final String VIEW_NAME = "Device Management";
    
    // BAB-specific fields with proper annotations
    @Column(name = "serial_number", nullable = true, length = 255, unique = true)
    @Size(max = 255)
    @AMetaData(
        displayName = "Serial Number", 
        required = false, 
        description = "Device serial number"
    )
    private String serialNumber;
    
    @Column(name = "firmware_version", nullable = true, length = 100)
    private String firmwareVersion;
    
    // Constructor patterns (MANDATORY)
    protected CBabDevice() {  // JPA constructor - NO initializeDefaults()
        super();
    }
    
    public CBabDevice(final String name, final CCompany company) {
        super(CBabDevice.class, name, company);
        initializeDefaults();  // Business constructor - MANDATORY call
    }
    
    private final void initializeDefaults() {
        // Initialize fields with empty defaults
        deviceStatus = "Offline";
        firmwareVersion = "";
        serialNumber = "";
        lastSeen = LocalDateTime.now();
        
        // MANDATORY: Call service initialization
        CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
    }
}
```

#### ✅ CORRECT - Abstract Node Hierarchy
```java
/** Abstract base for communication nodes */
@MappedSuperclass  // ✅ NOT @Entity - abstract entities use @MappedSuperclass
public abstract class CBabNode<EntityClass> extends CEntityOfCompany<EntityClass> {
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", nullable = false)
    private CBabDevice device;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;  // ✅ Initialize at declaration
    
    @Column(name = "node_type", nullable = false, length = 50)
    private String nodeType;
    
    // Constructor patterns for abstract entities
    protected CBabNode() {  // ✅ JPA constructor - NO initializeDefaults() call
        super();
    }
    
    protected CBabNode(final Class<EntityClass> clazz, final String name, 
                      final CBabDevice device, final String nodeType) {
        super(clazz, name, device.getCompany());
        this.device = device;
        this.nodeType = nodeType;
        this.enabled = true;
        // ✅ Abstract entities do NOT call initializeDefaults()
    }
}
```

#### ✅ CORRECT - Concrete Node Implementation
```java
@Entity  // ✅ Concrete entities are @Entity
@Table(name = "cbab_node_can")
public class CBabNodeCAN extends CBabNode<CBabNodeCAN> {
    
    // MANDATORY constants
    public static final String DEFAULT_COLOR = "#FF5722";
    public static final String DEFAULT_ICON = "vaadin:car";
    public static final String ENTITY_TITLE_PLURAL = "CAN Nodes";
    public static final String ENTITY_TITLE_SINGULAR = "CAN Node";
    public static final String VIEW_NAME = "CAN Node Configuration";
    
    // CAN-specific fields
    @Column(name = "bitrate", nullable = true)
    private Integer bitrate;
    
    @Column(name = "interface_name", nullable = true, length = 50)
    private String interfaceName;
    
    // Constructor patterns
    protected CBabNodeCAN() {  // ✅ JPA constructor - NO initializeDefaults()
        super();
    }
    
    public CBabNodeCAN(final String name, final CBabDevice device) {
        super(CBabNodeCAN.class, name, device, "CAN");
        initializeDefaults();  // ✅ MANDATORY for concrete entities
    }
    
    private final void initializeDefaults() {
        bitrate = 500000;
        interfaceName = "can0";
        CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
    }
    
    // MANDATORY - copyEntityTo implementation
    @Override
    protected void copyEntityTo(final CEntityDB<?> target, 
                               @SuppressWarnings("rawtypes") final CAbstractService serviceTarget,
                               final CCloneOptions options) {
        super.copyEntityTo(target, serviceTarget, options);
        
        if (target instanceof CBabNodeCAN) {
            final CBabNodeCAN targetNode = (CBabNodeCAN) target;
            copyField(this::getBitrate, targetNode::setBitrate);
            copyField(this::getInterfaceName, targetNode::setInterfaceName);
        }
    }
}
```

### 3. BAB Entity Rules

| Rule | Description | Example |
|------|-------------|---------|
| **No Status/Workflow** | BAB entities do NOT implement `IHasStatusAndWorkflow` | `CBabDevice`, `CBabNode*` |
| **Company Scoped** | All BAB entities extend `CEntityOfCompany` | Single company per BAB deployment |
| **Device Unique** | One device per company (database constraint) | `@UniqueConstraint(columnNames = {"company_id"})` |
| **Protocol Inheritance** | Communication nodes use abstract hierarchy | `CBabNode` → `CBabNodeCAN`, etc. |
| **Simple Workflow** | Projects use minimal workflow without complex status | BAB projects for configuration only |

---

## Service Layer Patterns

### 1. Abstract Service Hierarchy

#### ❌ WRONG - Abstract Service as Bean
```java
@Service  // ❌ WRONG - Abstract services are NOT @Service
@Profile("bab")
public abstract class CBabNodeService<NodeType> { }
```

#### ✅ CORRECT - Abstract Service Pattern
```java
@Profile("bab")
@PreAuthorize("isAuthenticated()")
// ✅ NO @Service - Abstract services are NOT Spring beans
public abstract class CBabNodeService<NodeType extends CBabNode<NodeType>> 
        extends CEntityOfCompanyService<NodeType> {
    
    // Common validation and business logic
    @Override
    protected void validateEntity(final NodeType entity) {
        super.validateEntity(entity);
        Check.notBlank(entity.getNodeType(), "Node Type is required");
        Check.notNull(entity.getDevice(), "Device is required");
    }
    
    // Abstract methods for concrete implementations
    public abstract List<NodeType> findByDevice(CBabDevice device);
}
```

#### ✅ CORRECT - Concrete Service Implementation
```java
@Service  // ✅ Concrete services are @Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CBabNodeCANService extends CBabNodeService<CBabNodeCAN> 
        implements IEntityRegistrable, IEntityWithView {
    
    public CBabNodeCANService(final IBabNodeCANRepository repository, 
                             final Clock clock, final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }
    
    @Override
    public Class<CBabNodeCAN> getEntityClass() {
        return CBabNodeCAN.class;
    }
    
    @Override
    protected void validateEntity(final CBabNodeCAN entity) {
        super.validateEntity(entity);
        // CAN-specific validation
        if (entity.getBitrate() != null && entity.getBitrate() <= 0) {
            throw new IllegalArgumentException("CAN bitrate must be positive");
        }
    }
    
    @Override
    public List<CBabNodeCAN> findByDevice(CBabDevice device) {
        return ((IBabNodeCANRepository) repository).findByDevice(device);
    }
}
```

---

## JSON Network Serialization Rules

For BAB network JSON export and profile-based exclusion logic, this document is mandatory:

- **[JSON_NETWORK_SERIALIZATION_CODING_RULES.md](JSON_NETWORK_SERIALIZATION_CODING_RULES.md)**

Key enforcement:
- Exclusion rules are defined per class (`createExcludedFieldMap_BabConfiguration/BabPolicy`).
- `getExcludedFieldMapForScenario(...)` must use helper methods from `IJsonNetworkSerializable`.
- Subclasses must merge super exclusions.
- `CJsonSerializer` keeps framework/global behavior; class-specific exclusions stay in the class.

### 2. Service Bean Resolution Rules

**CRITICAL**: When multiple concrete services exist, use specific services, not abstract base.

#### ❌ WRONG - Ambiguous Bean Resolution
```java
// This fails with "No qualifying bean" when multiple concrete services exist
final CBabNodeService nodeService = CSpringContext.getBean(CBabNodeService.class);
```

#### ✅ CORRECT - Specific Service Resolution
```java
// Use specific concrete services
final CBabNodeCANService canService = CSpringContext.getBean(CBabNodeCANService.class);
final CBabNodeEthernetService ethService = CSpringContext.getBean(CBabNodeEthernetService.class);

// Create nodes with appropriate services
final CBabNodeCAN canNode = new CBabNodeCAN("CAN Interface", device);
canService.save(canNode);

final CBabNodeEthernet ethNode = new CBabNodeEthernet("Ethernet Interface", device);
ethService.save(ethNode);
```

---

## Repository Patterns

### 1. Abstract Repository

#### ✅ CORRECT - Abstract Repository
```java
@Profile("bab")
@NoRepositoryBean  // ✅ MANDATORY - Abstract repositories are not beans
public interface IBabNodeRepository<NodeType extends CBabNode<NodeType>> 
        extends IEntityOfCompanyRepository<NodeType> {
    
    // Abstract method signatures
    List<NodeType> findByDevice(CBabDevice device);
    List<NodeType> findEnabledByDevice(CBabDevice device);
    Long countByDevice(CBabDevice device);
}
```

### 2. Concrete Repository

#### ✅ CORRECT - Concrete Repository with HQL
```java
@Profile("bab")
public interface IBabNodeCANRepository extends IBabNodeRepository<CBabNodeCAN> {
    
    @Override
    @Query("SELECT e FROM CBabNodeCAN e WHERE e.device = :device ORDER BY e.name ASC")
    List<CBabNodeCAN> findByDevice(@Param("device") CBabDevice device);
    
    @Override
    @Query("SELECT e FROM CBabNodeCAN e WHERE e.device = :device AND e.enabled = true ORDER BY e.name ASC")
    List<CBabNodeCAN> findEnabledByDevice(@Param("device") CBabDevice device);
    
    @Override
    @Query("SELECT COUNT(e) FROM CBabNodeCAN e WHERE e.device = :device")
    Long countByDevice(@Param("device") CBabDevice device);
}
```

---

## Data Initialization

### 1. BAB Data Initializer Pattern

**CRITICAL**: Follow exact `CDataInitializer` pattern with minimal BAB entities.

#### ✅ CORRECT - CBabDataInitializer Structure
```java
@Component
@Profile("bab")
public class CBabDataInitializer {
    
    // Constructor dependency injection (all required services)
    public CBabDataInitializer(
        final JdbcTemplate jdbcTemplate,
        final CGridEntityService gridEntityService,
        final CDetailSectionService detailSectionService,
        final CPageEntityService pageEntityService,
        final CBabDeviceService babDeviceService,
        final CProject_BabService projectService,
        final CUserService userService,
        final CCompanyService companyService,
        final ISessionService sessionService) {
        // Initialize dependencies
    }
    
    private void loadMinimalData(final boolean minimal) throws Exception {
        // ========== FOUNDATION ENTITIES (EXACT ORDER) ==========
        // 1. Create BAB company
        final CCompany company = CCompanyInitializerService.initializeSampleBab(minimal);
        
        // 2. Create roles and users
        final CUserCompanyRole adminRole = CUserCompanyRoleInitializerService.initializeSampleBab(company, minimal);
        CUserInitializerService.initializeSampleBab(company, adminRole, minimal);
        
        // 3. Set session context
        sessionService.setActiveCompany(company);
        final CUser user = userService.getRandomByCompany(company);
        sessionService.setActiveUser(user);
        
        // ========== REQUIRED FOR PROJECT WORKFLOW ==========
        // 4. Create status entities (REQUIRED for workflow relations)
        CProjectItemStatusInitializerService.initializeSample(company, minimal);
        
        // 5. Create user project roles (REQUIRED for workflow relations)
        CUserProjectRoleInitializerService.initializeSample(company, minimal);
        
        // 6. Create workflow WITH relations (CRITICAL: prevents "no status relations" error)
        final CWorkflowEntityService workflowService = CSpringContext.getBean(CWorkflowEntityService.class);
        final CWorkflowStatusRelationService workflowRelationService = CSpringContext.getBean(CWorkflowStatusRelationService.class);
        final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);
        final CUserProjectRoleService projectRoleService = CSpringContext.getBean(CUserProjectRoleService.class);
        
        CWorkflowEntityInitializerService.initializeSampleWorkflowEntities(
            company, minimal, statusService, projectRoleService, workflowService, workflowRelationService);
        
        // 7. Create project types
        CProjectTypeInitializerService.initializeSampleBab(company, minimal);
        
        // ========== BAB-SPECIFIC ENTITIES ==========
        // 8. Create BAB project
        final CProject_Bab project = CProject_BabInitializerService.initializeSampleBab(company, minimal);
        sessionService.setActiveProject(project);
        
        // 9. Initialize UI views
        initializeStandardViews(project);
        
        // 10. Create BAB devices and nodes
        CBabDeviceInitializerService.initializeSample(project, minimal);
        
        entityManager.flush();
    }
}
```

### 2. Initialization Order Rules

| Order | Phase | Entities | Why This Order |
|-------|-------|----------|----------------|
| **1** | Foundation | Company, Users, Roles | Basic authentication/authorization |
| **2** | Session | Set active company/user | Required for scoped operations |
| **3** | Status/Workflow | Status, Roles, Workflow with Relations | Projects need workflow to initialize |
| **4** | Project Setup | Project Types, Projects | Core business structure |
| **5** | UI | Views, Grids, Pages | User interface generation |
| **6** | BAB Entities | Devices, Nodes | Domain-specific functionality |

### 3. Critical Error Prevention

#### ❌ Common Error: Missing Workflow Relations
```
Workflow BAB Gateway Workflow has no status relations defined
```

**Root Cause**: Workflow created without status relations

#### ✅ Solution: Proper Workflow Initialization
```java
// WRONG - Only creates workflow entity
CWorkflowEntityInitializerService.initializeSampleBab(company, minimal);

// CORRECT - Creates workflow WITH status relations
CWorkflowEntityInitializerService.initializeSampleWorkflowEntities(
    company, minimal, statusService, projectRoleService, workflowService, workflowRelationService);
```

---

## Abstract Entity Hierarchy

### 1. Inheritance Patterns

| Pattern | Use Case | Annotation | Bean Status | Constructor Rules |
|---------|----------|------------|-------------|-------------------|
| **Abstract Entity** | `CBabNode<T>` | `@MappedSuperclass` | Not a bean | NO `initializeDefaults()` call |
| **Concrete Entity** | `CBabNodeCAN` | `@Entity` + `@Table` | Not a bean | MUST call `initializeDefaults()` |
| **Abstract Service** | `CBabNodeService<T>` | NO `@Service` | Not a bean | NOT injectable |
| **Concrete Service** | `CBabNodeCANService` | `@Service` + interfaces | Spring bean | Injectable |

### 2. Abstract Entity Constructor Rules

#### ✅ CORRECT - Abstract Entity Constructors
```java
@MappedSuperclass
public abstract class CBabNode<EntityClass> extends CEntityOfCompany<EntityClass> {
    
    protected CBabNode() {  // ✅ JPA constructor - NO initializeDefaults()
        super();
    }
    
    protected CBabNode(final Class<EntityClass> clazz, final String name, 
                      final CBabDevice device, final String nodeType) {
        super(clazz, name, device.getCompany());
        this.device = device;
        this.nodeType = nodeType;
        // ✅ NO initializeDefaults() call - concrete classes handle this
    }
}
```

#### ✅ CORRECT - Concrete Entity Constructors
```java
@Entity
public class CBabNodeCAN extends CBabNode<CBabNodeCAN> {
    
    protected CBabNodeCAN() {  // ✅ JPA constructor - NO initializeDefaults()
        super();
    }
    
    public CBabNodeCAN(final String name, final CBabDevice device) {
        super(CBabNodeCAN.class, name, device, "CAN");
        initializeDefaults();  // ✅ MANDATORY for concrete entities
    }
    
    private final void initializeDefaults() {
        bitrate = 500000;
        interfaceName = "can0";
        CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
    }
}
```

---

## Validation Rules

### 1. BAB-Specific Validation

#### ✅ Device Validation
```java
@Override
protected void validateEntity(final CBabDevice entity) throws CValidationException {
    super.validateEntity(entity);
    
    // Name validation (business entities must have names)
    Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
    
    // BAB-specific validations
    if (entity.getSerialNumber() != null && !entity.getSerialNumber().isBlank()) {
        if (entity.getSerialNumber().length() > 255) {
            throw new CValidationException("Serial number cannot exceed 255 characters");
        }
        
        // Check uniqueness
        Optional<CBabDevice> existing = repository.findBySerialNumber(entity.getSerialNumber());
        if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
            throw new CValidationException("Serial number already exists");
        }
    }
    
    // IP address validation (if provided)
    if (entity.getIpAddress() != null && !entity.getIpAddress().isBlank()) {
        if (!isValidIpAddress(entity.getIpAddress())) {
            throw new CValidationException("Invalid IP address format");
        }
    }
}
```

#### ✅ Node Validation
```java
@Override
protected void validateEntity(final CBabNodeCAN entity) throws CValidationException {
    super.validateEntity(entity);
    
    // Node-specific validation
    Check.notNull(entity.getDevice(), "Device is required");
    Check.notBlank(entity.getNodeType(), "Node type is required");
    
    // CAN-specific validation
    if (entity.getBitrate() != null && entity.getBitrate() <= 0) {
        throw new CValidationException("CAN bitrate must be positive");
    }
    
    if (entity.getSamplePoint() != null && 
        (entity.getSamplePoint() < 0.0 || entity.getSamplePoint() > 1.0)) {
        throw new CValidationException("Sample point must be between 0.0 and 1.0");
    }
}
```

---

## UI Patterns

### 1. BAB View Registration

#### ✅ CORRECT - View Initialization
```java
private void initializeStandardViews(final CProject_Bab project) throws Exception {
    // Core system views
    CSystemSettingsInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
    CCompanyInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
    CUserInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
    
    // BAB-specific views
    CProject_BabInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
    CBabDeviceInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
    
    // Administrative views
    CGridEntityInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
    CPageEntityInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
}
```

### 2. Profile-Specific UI Rules

- ✅ BAB views are simple CRUD interfaces
- ✅ No complex workflow UI (BAB entities don't use workflow)
- ✅ Device-centric navigation (device → nodes)
- ✅ Protocol-specific node forms (CAN bitrate, Ethernet IP, etc.)

---

## Testing Guidelines

### 1. BAB Profile Testing

#### ✅ Testing Commands
```bash
# Test BAB device page
mvn test -Dtest=CPageTestComprehensive -Dtest.routeKeyword=device -Dspring.profiles.active=h2,bab

# Test BAB project page
mvn test -Dtest=CPageTestComprehensive -Dtest.routeKeyword=bab -Dspring.profiles.active=h2,bab

# Comprehensive BAB test
mvn test -Dtest=CPageTestComprehensive -Dspring.profiles.active=h2,bab
```

### 2. BAB Test Verification

#### ✅ Successful BAB Initialization Logs
```
INFO  (CBabDataInitializer.java:201) loadMinimalData: Loading BAB minimal data (minimal=false)
INFO  (CBabDeviceInitializerService.java:140) initializeSample: Initializing BAB sample data for company: BAB Gateway  
INFO  (CBabDeviceInitializerService.java:155) initializeSample: Created sample device: IoT Gateway Device
INFO  (CBabDeviceInitializerService.java:88) createSampleNodes: Created CAN node: CAN Bus Interface
INFO  (CBabDeviceInitializerService.java:100) createSampleNodes: Created Ethernet node: Ethernet Interface
INFO  (CBabDataInitializer.java:255) loadMinimalData: BAB minimal data loaded successfully
```

#### ❌ Common Error Patterns
```
ERROR: Workflow BAB Gateway Workflow has no status relations defined
→ Fix: Ensure workflow relations are created in initialization

ERROR: No qualifying bean of type 'CBabNodeService' available: expected single matching bean but found 4
→ Fix: Use specific concrete services, not abstract base

ERROR: No such bean definition exception for initializer services  
→ Fix: Use static methods for initializer services, don't inject as beans
```

---

## Critical Rules Summary

### ✅ MUST DO

1. **Entity Rules**:
   - All BAB entities extend `CEntityOfCompany<T>`
   - Use `@Profile("bab")` on all BAB classes
   - Abstract entities use `@MappedSuperclass`, concrete use `@Entity`
   - Implement all entity constants (COLOR, ICON, TITLE, etc.)

2. **Constructor Rules**:
   - JPA constructors (protected, parameterless): NO `initializeDefaults()` call
   - Business constructors: MUST call `initializeDefaults()` as last statement
   - Abstract entity constructors: NO `initializeDefaults()` call

3. **Service Rules**:
   - Abstract services: NO `@Service` annotation
   - Concrete services: `@Service` + `@Profile("bab")` + interfaces
   - Use specific concrete services, not abstract base classes

4. **Initialization Rules**:
   - Follow exact order: Company → Users → Status → Workflow → Project → BAB entities
   - Create workflow WITH status relations (prevent "no relations" error)
   - Use static initializer methods, don't inject as beans

### ❌ NEVER DO

1. **Entity Anti-Patterns**:
   - BAB entities implementing `IHasStatusAndWorkflow` (not needed)
   - Using `@Entity` on abstract classes
   - Calling `initializeDefaults()` in JPA constructors

2. **Service Anti-Patterns**:
   - Adding `@Service` to abstract service classes
   - Injecting abstract `CBabNodeService` (causes bean ambiguity)
   - Creating workflow entities without status relations

3. **Initialization Anti-Patterns**:
   - Skipping status/workflow initialization (causes project creation failures)
   - Wrong initialization order (causes dependency errors)
   - Injecting initializer services as beans (they're utility classes)

### 🎯 Success Criteria

A BAB profile implementation is correct when:
- ✅ Data initialization completes without workflow relation errors
- ✅ All BAB pages load successfully in Playwright tests  
- ✅ Devices and nodes can be created via UI
- ✅ Projects can be created with BAB-specific features (IP address)
- ✅ No Spring bean resolution errors for services

---

**Last Updated**: 2026-01-26  
**Status**: Verified working with Playwright tests  
**Next Review**: When new BAB entities are added
