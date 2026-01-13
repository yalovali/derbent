# BAB Quick Reference - Derbent Pattern Compliance

**For Developers**: Use this as a checklist when creating new BAB entities or features.

---

## 📋 Entity Creation Checklist

### 1. Choose Correct Base Class
```java
// ✅ For BAB entities (company-scoped, no project)
public class CMyEntity extends CEntityOfCompany<CMyEntity>

// ❌ Don't create custom base classes
public class CMyEntity extends CBabItem  // WRONG
```

### 2. Entity Annotations
```java
@Entity
@Table(name = "cbab_my_entity")
@AttributeOverride(name = "id", column = @Column(name = "my_entity_id"))
public class CMyEntity extends CEntityOfCompany<CMyEntity> {
```

### 3. Required Constants
```java
public static final String DEFAULT_COLOR = "#HEXCODE";
public static final String DEFAULT_ICON = "vaadin:icon-name";
public static final String ENTITY_TITLE_PLURAL = "My Entities";
public static final String ENTITY_TITLE_SINGULAR = "My Entity";
private static final Logger LOGGER = LoggerFactory.getLogger(CMyEntity.class);
private static final long serialVersionUID = 1L;
public static final String VIEW_NAME = "My Entity Management";
```

### 4. Field Pattern
```java
@Column(name = "field_name", nullable = true, length = 255)
@Size(max = 255)
@AMetaData(
    displayName = "Field Name", 
    required = false, 
    readOnly = false, 
    description = "Description of field", 
    hidden = false, 
    maxLength = 255
)
private String fieldName;
```

### 5. Constructors
```java
/** Default constructor for JPA. */
public CMyEntity() {
    super();
}

public CMyEntity(final String name, final CCompany company) {
    super(CMyEntity.class, name, company);
}
```

### 6. Getters/Setters
```java
public String getFieldName() { return fieldName; }

public void setFieldName(final String fieldName) { 
    this.fieldName = fieldName; 
    updateLastModified();
}
```

### 7. Initialize Defaults
```java
@Override
protected void initializeDefaults() {
    super.initializeDefaults();
    if (fieldName == null) {
        fieldName = "default value";
    }
}
```

---

## 📋 Repository Creation Checklist

### Location
```
✅ device/service/IMyEntityRepository.java
❌ device/repository/IMyEntityRepository.java  // WRONG
```

### Template
```java
@Profile("bab")
public interface IMyEntityRepository extends IAbstractRepository<CMyEntity> {
    
    @Query("SELECT e FROM #{#entityName} e WHERE e.company = :company ORDER BY e.name ASC")
    List<CMyEntity> findByCompany(@Param("company") CCompany company);
    
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.company = :company")
    Long countByCompany(@Param("company") CCompany company);
}
```

**Rules:**
- ✅ Always use `#{#entityName}` in queries
- ✅ Always include `ORDER BY` clause
- ✅ Use `@Param` for parameters
- ✅ Add `@Profile("bab")` annotation

---

## 📋 Service Creation Checklist

### Template
```java
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CMyEntityService extends CAbstractService<CMyEntity> 
        implements IEntityRegistrable, IEntityWithView {

    private static final Logger LOGGER = LoggerFactory.getLogger(CMyEntityService.class);
    private final IMyEntityRepository repository;
    private final ISessionService sessionService;

    public CMyEntityService(final IMyEntityRepository repository, 
                           final Clock clock, 
                           final ISessionService sessionService) {
        super(repository, clock, sessionService);
        this.repository = repository;
        this.sessionService = sessionService;
    }

    @Override
    public Class<CMyEntity> getEntityClass() {
        return CMyEntity.class;
    }

    @Override
    public IAbstractRepository<CMyEntity> getRepository() {
        return repository;
    }

    @Override
    public Class<?> getInitializerServiceClass() {
        return CMyEntityInitializerService.class;
    }

    @Override
    public Class<?> getPageServiceClass() {
        return CPageServiceMyEntity.class;
    }

    @Override
    public Class<?> getServiceClass() {
        return this.getClass();
    }
}
```

**Required Methods:**
- ✅ `getEntityClass()` - returns entity class
- ✅ `getRepository()` - returns repository instance
- ✅ `getInitializerServiceClass()` - returns initializer class
- ✅ `getPageServiceClass()` - returns page service class
- ✅ `getServiceClass()` - returns service class

---

## 📋 Initializer Service Checklist

### Template
```java
@Component
@Profile("bab")
public class CMyEntityInitializerService extends CInitializerServiceBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CMyEntityInitializerService.class);
    private static final Class<?> clazz = CMyEntity.class;

    public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
        LOGGER.info("Initializing sample data for company: {}", company.getName());

        final CMyEntityService service = (CMyEntityService) CSpringContext.getBean(
                CEntityRegistry.getServiceClassForEntity(clazz));

        // Create sample data
        final CMyEntity entity = new CMyEntity("Sample Entity", company);
        entity.setDescription("Sample description");
        service.save(entity);
    }
}
```

**Method Signature:**
```java
public static void initializeSample(final CCompany company, final boolean minimal) throws Exception
```

---

## 📋 Page Service Checklist

### Placeholder Template
```java
@Service
@Profile("bab")
public class CPageServiceMyEntity {
    // Page service methods will be added when views are implemented
}
```

---

## 🔍 Common Mistakes to Avoid

### ❌ Don't Do This:
```java
// Custom base classes
public class CBabItem extends CEntityNamed  // WRONG

// Repository in wrong package
device/repository/IMyRepository.java  // WRONG

// Missing interfaces
public class CMyService extends CAbstractService<CMyEntity>  // INCOMPLETE

// Missing @PreAuthorize
@Service
public class CMyService  // MISSING ANNOTATION

// Incomplete method signature
public static void initializeSample(CCompany company)  // MISSING boolean minimal

// Missing ORDER BY
@Query("SELECT e FROM #{#entityName} e WHERE ...")  // MISSING ORDER BY

// Direct class name in JPQL
@Query("SELECT e FROM CMyEntity e WHERE ...")  // USE #{#entityName}
```

### ✅ Do This Instead:
```java
// Standard Derbent base
public class CMyEntity extends CEntityOfCompany<CMyEntity>

// Repository in service package
device/service/IMyRepository.java

// Complete interfaces
public class CMyService extends CAbstractService<CMyEntity> 
        implements IEntityRegistrable, IEntityWithView

// Required annotation
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CMyService

// Complete method signature
public static void initializeSample(final CCompany company, final boolean minimal) throws Exception

// Always include ORDER BY
@Query("SELECT e FROM #{#entityName} e WHERE ... ORDER BY e.name ASC")

// Use entity name placeholder
@Query("SELECT e FROM #{#entityName} e WHERE ...")
```

---

## 📂 File Organization

```
bab/
├── device/
│   ├── domain/              # Entity classes only
│   │   └── CMyEntity.java
│   ├── service/             # All service-related files
│   │   ├── IMyEntityRepository.java      # Repository interface
│   │   ├── CMyEntityService.java         # Service class
│   │   ├── CMyEntityInitializerService.java  # Sample data
│   │   └── CPageServiceMyEntity.java     # Page service
│   └── view/                # View classes (when implemented)
│       └── CMyEntityView.java
```

---

## 🎯 Verification Commands

```bash
# Check compilation
mvn clean compile -DskipTests

# Verify file locations
find src/main/java/tech/derbent/bab -name "*.java" -type f

# Check for repositories in wrong location
find src/main/java/tech/derbent/bab -path "*/repository/*.java"
# Should return nothing

# Check for missing @Profile annotations
grep -r "class.*Service" src/main/java/tech/derbent/bab/*/service/*.java | grep -v "@Profile"
# Should return nothing
```

---

## 📚 Reference Examples

**Study these files for patterns:**
- Entity: `src/main/java/tech/derbent/app/activities/domain/CActivity.java`
- Service: `src/main/java/tech/derbent/app/activities/service/CActivityService.java`
- Repository: `src/main/java/tech/derbent/app/activities/service/IActivityRepository.java`
- Initializer: `src/main/java/tech/derbent/app/activities/service/CActivityInitializerService.java`

**BAB Examples:**
- Entity: `src/main/java/tech/derbent/bab/device/domain/CBabDevice.java`
- Service: `src/main/java/tech/derbent/bab/device/service/CBabDeviceService.java`
- Repository: `src/main/java/tech/derbent/bab/device/service/IBabDeviceRepository.java`
- Initializer: `src/main/java/tech/derbent/bab/device/service/CBabDeviceInitializerService.java`

---

**Last Updated**: 2026-01-13  
**Status**: ✅ Complete and Verified
