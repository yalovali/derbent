# Coder Agent Configuration

## Agent Settings
- **Mode**: Implementation
- **Primary Tools**: edit, create, view, grep, glob, bash
- **Output Format**: Code with documentation
- **Verification**: Build + compile checks

## Code Generation Rules

### Entity Generation Checklist
- [ ] C-prefix on class name
- [ ] Generic type parameter: `CEntity<CEntity>`
- [ ] All 5 constants defined
- [ ] Collections at field declaration
- [ ] JPA constructor: NO initializeDefaults() call
- [ ] Business constructor: WITH initializeDefaults() call
- [ ] initializeDefaults() signature: `private final void`
- [ ] @AMetaData on UI fields
- [ ] copyEntityTo() implemented
- [ ] BAB JSON entities: follow `docs/bab/JSON_NETWORK_SERIALIZATION_CODING_RULES.md` (`IJsonNetworkSerializable`, per-class scenario exclusion maps, helper-based merge with super)

### Service Generation Checklist
- [ ] C-prefix on class name
- [ ] @Service annotation
- [ ] @PreAuthorize("isAuthenticated()")
- [ ] Constructor injection (NO @Autowired fields)
- [ ] getEntityClass() returns correct type
- [ ] initializeNewEntity() calls super first
- [ ] validateEntity() with Check.notBlank() for name
- [ ] validateEntity() checks length constraints
- [ ] validateEntity() mirrors DB unique constraints

### Repository Generation Checklist
- [ ] I-prefix on interface name
- [ ] Extends correct base repository
- [ ] HQL queries use concrete entity names
- [ ] PageView queries use LEFT JOIN FETCH
- [ ] @Param annotations on parameters

## Code Templates

### Entity Template
```java
@Entity
@Table(name = "c{entity}_table")
@AttributeOverride(name = "id", column = @Column(name = "{entity}_id"))
public class C{Entity} extends C{Parent}<C{Entity}> {
    
    // Constants
    public static final String DEFAULT_COLOR = "#{COLOR}";
    public static final String DEFAULT_ICON = "vaadin:{icon}";
    public static final String ENTITY_TITLE_SINGULAR = "{Entity}";
    public static final String ENTITY_TITLE_PLURAL = "{Entities}";
    private static final Logger LOGGER = LoggerFactory.getLogger(C{Entity}.class);
    public static final String VIEW_NAME = "{Entities} View";
    
    // Fields
    private Set<CAttachment> attachments = new HashSet<>();
    
    @Column(nullable = false, length = 255)
    @AMetaData(displayName = "Name", required = true)
    private String name;
    
    // Constructors
    protected C{Entity}() { super(); }
    
    public C{Entity}(String name, CProject project) {
        super(C{Entity}.class, name, project);
        initializeDefaults();
    }
    
    private final void initializeDefaults() {
        CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
    }
    
    // Getters/setters
    // copyEntityTo
}
```

### Service Template
```java
@Service
@PreAuthorize("isAuthenticated()")
public class C{Entity}Service extends C{Parent}Service<C{Entity}>
        implements IEntityRegistrable, IEntityWithView {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(C{Entity}Service.class);
    
    public C{Entity}Service(
        final I{Entity}Repository repository,
        final Clock clock,
        final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }
    
    @Override
    protected Class<C{Entity}> getEntityClass() {
        return C{Entity}.class;
    }
    
    @Override
    public void initializeNewEntity(final Object entity) {
        super.initializeNewEntity(entity);
        // Context initialization
    }
    
    @Override
    protected void validateEntity(final C{Entity} entity) throws CValidationException {
        super.validateEntity(entity);
        
        Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
        
        if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
            throw new CValidationException(
                ValidationMessages.formatMaxLength(
                    ValidationMessages.NAME_MAX_LENGTH, 
                    CEntityConstants.MAX_LENGTH_NAME));
        }
    }
}
```

## Build Verification Commands

```bash
# Format code
mvn spotless:apply

# Compile (Java 17 for agents)
mvn clean compile -Pagents -DskipTests

# Full build (if requested)
mvn clean verify -Pagents
```

## Commit Message Format

```
{type}: {short description}

- {detail 1}
- {detail 2}

Patterns followed:
- {pattern 1}
- {pattern 2}
```

Types: feat, fix, refactor, docs, test, chore
