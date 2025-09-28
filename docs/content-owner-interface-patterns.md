# IContentOwner and IHasContentOwner Interface Patterns

This document outlines the content owner interface patterns used in the Derbent project for context-aware data resolution and generic component implementation. These patterns enable forms and components to access context-specific data from their parent containers.

## 🎯 Core Interfaces

### IContentOwner Interface

```java
public interface IContentOwner {
    Object getCurrentEntity();
    void setCurrentEntity(Object entity);
}
```

**Purpose**: Provides context-specific data for content resource resolvers such as ComboBoxes in forms. This allows FormBuilder to access methods on the current page/context owner instead of just service beans, enabling context-aware data providers.

**Key Implementations**:
- `CUsersView` - Provides `getAvailableProjects()` for user context
- `CComponentDBEntity<T>` - Generic component with content owner capabilities  
- Panel classes extending `CPanelBase`

### IHasContentOwner Interface

```java
public interface IHasContentOwner {
    IContentOwner getContentOwner();
    void setContentOwner(IContentOwner parentContent);
}
```

**Purpose**: Enables hierarchical content owner relationships, allowing components to access parent context through the content owner chain.

## 🏗️ Implementation Patterns

### Generic Component Pattern

```java
public abstract class CComponentDBEntity<EntityClass extends CEntityDB<EntityClass>> 
    extends CVerticalLayout implements IContentOwner, IHasContentOwner {
    
    protected IContentOwner contentOwner = null;
    private EntityClass currentEntity;
    
    @Override
    public IContentOwner getContentOwner() { 
        return contentOwner; 
    }
    
    @Override
    public void setContentOwner(IContentOwner parentContent) { 
        this.contentOwner = parentContent; 
    }
    
    @Override
    public EntityClass getCurrentEntity() { 
        return currentEntity; 
    }
    
    @Override
    public void setCurrentEntity(Object entity) { 
        currentEntity = (EntityClass) entity; 
    }
}
```

### Dialog Pattern with Content Owner

```java
public class CUserProjectSettingsDialog extends CDBRelationDialog<CUserProjectSettings, CUser, CProject> {
    
    public CUserProjectSettingsDialog(IContentOwner parentContent, 
            final CUserService masterService, 
            final CProjectService detailService,
            final CUserProjectSettingsService userProjectSettingsService, 
            final CUserProjectSettings settings, 
            final CUser user,
            final Consumer<CUserProjectSettings> onSave) throws Exception {
        
        super(parentContent, settings != null ? settings : new CUserProjectSettings(), 
              user, masterService, detailService, userProjectSettingsService, onSave, settings == null);
        
        getEntity().setUser(user);
        setupDialog();
        populateForm();
    }
}
```

## 📋 Data Provider Integration

### Content Owner Method Pattern

Content owners can provide context-specific data through methods called by CDataProviderResolver:

```java
public class CUsersView extends CGridViewBaseNamed<CUser> implements IContentOwner {
    
    /** Content owner method to provide available projects for the current user context.
     * @return list of available projects */
    public List<CProject> getAvailableProjects() {
        LOGGER.debug("Getting available projects for user context");
        final CUser currentUser = getCurrentEntity();
        if (currentUser == null) {
            LOGGER.debug("No current user, returning all projects");
            return projectService.findAll();
        }
        // Get all projects - could be filtered based on user's company, role, etc.
        final List<CProject> allProjects = projectService.findAll();
        LOGGER.debug("Found {} available projects for user: {}", allProjects.size(),
                currentUser.getName() != null ? currentUser.getName() : "Unknown");
        return allProjects;
    }
}
```

### Entity Field Configuration

```java
@ManyToOne
@JoinColumn(name = "project_id")
@AMetaData(
    displayName = "Project", 
    required = false, 
    readOnly = false, 
    description = "User's project", 
    hidden = false, 
    order = 5,
    setBackgroundFromColor = true, 
    useIcon = true, 
    dataProviderOwner = "content",  // Use content owner instead of service
    dataProviderMethod = "getAvailableProjects"  // Method to call on content owner
)
private CProject project;
```

## 🔄 CDataProviderResolver Integration

The CDataProviderResolver handles content owner method calls with multiple fallback strategies:

1. **Parameter + Pageable**: `methodName(String param, Pageable pageable)`
2. **Parameter Only**: `methodName(String param)`  
3. **Pageable Only**: `methodName(Pageable pageable)`
4. **No Parameters**: `methodName()` - Used for getAvailableProjects()

### Null Content Owner Handling

The resolver gracefully handles null content owners:

```java
private <T extends CEntityDB<T>> List<T> resolveDataFromContentOwner(IContentOwner contentOwner, 
        final Class<T> entityType, final String methodName, final String paramMethodName) throws Exception {
    
    Check.notNull(entityType, "Entity type cannot be null");
    LOGGER.debug("Resolving data from content owner using method '{}' for entity type: {}", 
                 methodName, entityType.getSimpleName());
    
    // Handle null content owner gracefully by returning empty list
    if (contentOwner == null) {
        LOGGER.debug("Content owner is null, returning empty list for entity type: {}", 
                     entityType.getSimpleName());
        return Collections.emptyList();
    }
    
    try {
        return callDataMethod(contentOwner, methodName, entityType, paramMethodName);
    } catch (final Exception e) {
        LOGGER.error("Error resolving data from content owner: {}", e.getMessage(), e);
        throw e;
    }
}
```

## 🧪 Testing Patterns

### Mock Content Owner Implementation

```java
private static class MockContentOwnerWithProjects implements IContentOwner {
    
    private CUser currentUser;
    
    @Override
    public Object getCurrentEntity() { 
        return currentUser; 
    }
    
    @Override
    public void setCurrentEntity(Object entity) {
        this.currentUser = (CUser) entity;
    }
    
    /** Implementation of getAvailableProjects method that should be called by the data provider resolver. */
    public List<CProject> getAvailableProjects() {
        // Create test projects
        CProject project1 = new CProject();
        project1.setName("Test Project 1");
        project1.setDescription("Description for Test Project 1");
        
        CProject project2 = new CProject();
        project2.setName("Test Project 2");
        project2.setDescription("Description for Test Project 2");
        
        return List.of(project1, project2);
    }
}
```

## 🎨 Usage Examples

### Creating a Context-Aware Component

```java
public class CMyCustomComponent extends CComponentDBEntity<CMyEntity> {
    
    public CMyCustomComponent(String title, IContentOwner parentContent, 
            CEnhancedBinder<CMyEntity> binder, CMyEntityService service) {
        super(title, parentContent, binder, CMyEntity.class, service);
        // Component automatically inherits content owner capabilities
    }
    
    @Override
    protected void updatePanelEntityFields() {
        setEntityFields(List.of("name", "description", "project")); // project field will use content owner
    }
    
    // Optional: provide custom context methods
    public List<CCustomData> getCustomContextData() {
        // Access parent context through getContentOwner() if needed
        return customService.findByCurrentContext();
    }
}
```

### Dialog with Content Owner Integration

```java
public class CMyRelationDialog extends CDBRelationDialog<CMyRelation, CMaster, CDetail> {
    
    public CMyRelationDialog(IContentOwner parentContent, CMyRelation relation, CMaster master,
            CMasterService masterService, CDetailService detailService, 
            CMyRelationService relationService, Consumer<CMyRelation> onSave, boolean isNew) {
        
        super(parentContent, relation, master, masterService, detailService, relationService, onSave, isNew);
        setupDialog();
        populateForm();
    }
    
    @Override
    protected List<String> getFormFields() {
        return List.of("detail", "role", "permission"); // detail field will use content owner for data
    }
}
```

## ✅ Best Practices

1. **Always pass content owner**: When creating components or dialogs, always pass the content owner (usually `this`)
2. **Implement both interfaces**: Generic components should implement both IContentOwner and IHasContentOwner
3. **Handle null gracefully**: Content owner methods should handle null current entities gracefully
4. **Use descriptive method names**: Content owner methods should have clear, descriptive names like `getAvailableProjects()`
5. **Log context operations**: Use DEBUG logging for content owner method calls to aid troubleshooting
6. **Return appropriate collections**: Always return List<T>, never null from content owner methods

## 🚨 Common Pitfalls

1. **Forgetting content owner parameter**: Components without content owner lose context-aware capabilities
2. **Not handling null content owner**: Can cause NullPointerException in data resolution
3. **Wrong method signature**: Content owner methods must match expected signatures for CDataProviderResolver
4. **Missing dataProviderOwner**: Set `dataProviderOwner = "content"` in @AMetaData annotations
5. **Not casting current entity**: Remember to cast getCurrentEntity() to the expected type in content owner methods

## 🔍 Debugging Tips

1. **Enable DEBUG logging**: Set `logging.level.tech.derbent.api.annotations.CDataProviderResolver=DEBUG`
2. **Check method cache**: CDataProviderResolver caches method lookups - use `clearCaches()` in tests
3. **Verify method signatures**: Use reflection to check if content owner methods have expected signatures
4. **Test with mock content owners**: Create mock implementations to isolate content owner behavior
5. **Check content owner chain**: Verify that content owner is properly set through the component hierarchy

## 📚 Related Documentation

- [View Patterns](view-patterns.md) - View layer architecture and UI component patterns
- [CDataProviderResolver Parameter Pattern](CDataProviderResolver-parameter-pattern-example.md) - Parameter resolution examples
- [Service Patterns](service-patterns.md) - Service layer patterns and interfaces