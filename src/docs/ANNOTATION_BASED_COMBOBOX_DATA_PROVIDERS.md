# Annotation-Based ComboBox Data Provider System

## Overview

The enhanced `CEntityFormBuilder` now supports annotation-based data provider configuration for ComboBox fields, making form creation much simpler and more maintainable. Instead of writing complex `ComboBoxDataProvider` implementations that handle multiple entity types, you can now specify data providers directly in the `@MetaData` annotations on your entity fields.

## Problem Solved

**Before (Complex and Error-Prone):**
```java
// In your view class - complex data provider logic
final CEntityFormBuilder.ComboBoxDataProvider dataProvider = new CEntityFormBuilder.ComboBoxDataProvider() {
    @Override
    @SuppressWarnings("unchecked")
    public <T extends CEntityDB> List<T> getItems(final Class<T> entityType) {
        if (entityType == CActivityType.class) {
            return (List<T>) activityTypeService.list(Pageable.unpaged());
        } else if (entityType == CProject.class) {
            return (List<T>) projectService.findAllActive();
        } else if (entityType == CUser.class) {
            return (List<T>) userService.listByRole("ACTIVE");
        }
        // What if you add more ComboBox fields? More if-else blocks!
        // What if you forget to handle a type? Silent bugs!
        return Collections.emptyList();
    }
};
Div form = CEntityFormBuilder.buildForm(CActivity.class, binder, dataProvider);
```

**After (Simple and Maintainable):**
```java
// In your entity - clean annotations specify data sources
@MetaData(displayName = "Activity Type", dataProviderBean = "CActivityTypeService")
private CActivityType activityType;

@MetaData(displayName = "Project", dataProviderClass = CProjectService.class, dataProviderMethod = "findAllActive")
private CProject project;

@MetaData(displayName = "Assigned User", dataProviderBean = "userService", dataProviderMethod = "listByRole") 
private CUser assignedUser;

// In your view - super simple!
Div form = CEntityFormBuilder.buildForm(CActivity.class, binder); // No data provider needed!
```

## Configuration Options

### 1. Bean Name Approach
Specify the Spring bean name to use as the data provider:

```java
@MetaData(
    displayName = "Activity Type",
    dataProviderBean = "CActivityTypeService"  // Spring bean name
)
private CActivityType activityType;
```

### 2. Bean Class Approach  
Specify the Spring service class to use as the data provider:

```java
@MetaData(
    displayName = "Project",
    dataProviderClass = CProjectService.class  // Spring service class
)
private CProject project;
```

### 3. Custom Method Approach
Use a specific method on the data provider service:

```java
@MetaData(
    displayName = "Active Users",
    dataProviderBean = "userService",
    dataProviderMethod = "findAllActive"  // Custom method name
)
private CUser assignedUser;
```

### 4. Automatic Resolution
No explicit configuration needed - automatically finds service by naming convention:

```java
@MetaData(displayName = "Category")
// Automatically looks for: CategoryService, categoryService, or category.service
private CCategory category;
```

## Method Resolution Priority

The system tries to find data methods in the following order:

1. **Custom Method with Pageable:** `customMethod(Pageable pageable)`
2. **Custom Method:** `customMethod()`  
3. **Standard List with Pageable:** `list(Pageable pageable)`
4. **Standard List:** `list()`
5. **Standard FindAll:** `findAll()`

## Complete Example

### Entity Definition
```java
@Entity
public class CTask extends CEntityOfProject {
    
    @MetaData(
        displayName = "Task Name",
        required = true,
        order = 1,
        maxLength = 255
    )
    private String name;
    
    @MetaData(
        displayName = "Task Type",
        required = true,
        order = 2,
        dataProviderBean = "CTaskTypeService"
    )
    private CTaskType taskType;
    
    @MetaData(
        displayName = "Assigned User",
        required = false,
        order = 3,
        dataProviderClass = CUserService.class,
        dataProviderMethod = "findAllActive"
    )
    private CUser assignedUser;
    
    @MetaData(
        displayName = "Priority Level",
        required = true,
        order = 4
        // Automatic resolution - looks for PriorityLevelService
    )
    private CPriorityLevel priorityLevel;
    
    // Getters and setters...
}
```

### Service Implementations
```java
@Service
public class CTaskTypeService {
    
    @Autowired
    private CTaskTypeRepository repository;
    
    public List<CTaskType> list(Pageable pageable) {
        return repository.findAll(pageable).getContent();
    }
    
    public List<CTaskType> list() {
        return repository.findAll();
    }
}

@Service  
public class CUserService {
    
    @Autowired
    private CUserRepository repository;
    
    public List<CUser> findAllActive() {
        return repository.findByStatusAndActiveTrue("ACTIVE");
    }
    
    public List<CUser> list(Pageable pageable) {
        return repository.findAll(pageable).getContent();
    }
}
```

### View Implementation
```java
@Route("tasks/:task_id?/:action?(edit)")
public class CTaskView extends CProjectAwareMDPage<CTask> {
    
    @Override
    protected void createDetailsLayout() {
        LOGGER.info("Creating details layout for CTaskView");
        final Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");
        
        // NEW APPROACH: No complex data provider logic needed!
        // All ComboBox fields get their data automatically based on annotations
        editorLayoutDiv.add(CEntityFormBuilder.buildForm(CTask.class, getBinder()));
        
        getBaseDetailsLayout().add(editorLayoutDiv);
    }
}
```

## Migration Guide

### Step 1: Update Your Entities
Add data provider information to your `@MetaData` annotations:

```java
// Before
@MetaData(displayName = "Activity Type")
private CActivityType activityType;

// After  
@MetaData(displayName = "Activity Type", dataProviderBean = "CActivityTypeService")
private CActivityType activityType;
```

### Step 2: Simplify Your Views
Remove the complex `ComboBoxDataProvider` implementations:

```java
// Before - complex provider logic
final CEntityFormBuilder.ComboBoxDataProvider dataProvider = new CEntityFormBuilder.ComboBoxDataProvider() {
    // ... complex implementation
};
editorLayoutDiv.add(CEntityFormBuilder.buildForm(MyEntity.class, getBinder(), dataProvider));

// After - simple call
editorLayoutDiv.add(CEntityFormBuilder.buildForm(MyEntity.class, getBinder()));
```

### Step 3: Test Incrementally
The system is backward compatible, so you can migrate one entity at a time. Existing `ComboBoxDataProvider` implementations continue to work.

## Benefits

1. **Cleaner Code:** Data provider configuration is co-located with field definitions
2. **Better Maintainability:** Adding new ComboBox fields doesn't require view changes
3. **Type Safety:** Configuration is checked at annotation level
4. **Automatic Resolution:** Common patterns work without explicit configuration
5. **Better Separation of Concerns:** View logic focuses on UI, not data retrieval
6. **Self-Documenting:** You can see the data source right at the field definition
7. **Backward Compatible:** Existing code continues to work

## Advanced Features

### Caching
The `CDataProviderResolver` caches bean lookups and method resolutions for performance, but always fetches fresh data.

### Error Handling
The system gracefully handles missing services, invalid configurations, and network issues by:
- Logging detailed error information
- Falling back to empty ComboBox lists
- Continuing form generation for other fields

### Multiple Data Sources
Different ComboBox fields can use completely different data sources:

```java
@MetaData(displayName = "Internal Type", dataProviderBean = "internalTypeService")
private CInternalType internalType;

@MetaData(displayName = "External Type", dataProviderClass = ExternalTypeService.class)
private CExternalType externalType;
```

## Best Practices

1. **Use Bean Names for Standard Services:** `dataProviderBean = "CEntityTypeService"`
2. **Use Bean Classes for Type Safety:** `dataProviderClass = CServiceClass.class`
3. **Use Custom Methods for Filtered Data:** `dataProviderMethod = "findAllActive"`
4. **Follow Naming Conventions:** `CEntityTypeService` for automatic resolution
5. **Provide Fallback Methods:** Implement both `list()` and `list(Pageable)` in services
6. **Handle Null Values:** Services should return empty lists, not null
7. **Log Appropriately:** Use the built-in logging for troubleshooting

## Troubleshooting

### ComboBox is Empty
1. Check if the service bean exists in Spring context
2. Verify the service method signature matches expectations
3. Check application logs for resolution errors
4. Ensure the service method returns a non-null list

### Method Not Found Errors
1. Verify the method name in `dataProviderMethod`
2. Check if the service implements the expected method signatures
3. Review the method resolution priority order

### Performance Issues
1. Use pagination in service methods when possible
2. Consider caching at the service level for reference data
3. Monitor the cache statistics using `getCacheStats()`

## Related Documentation

- [COPILOT_AGENT_GUIDELINE.md](COPILOT_AGENT_GUIDELINE.md) - General coding standards
- [MetaData Javadoc](../main/java/tech/derbent/abstracts/annotations/MetaData.java) - Complete annotation reference
- [CEntityFormBuilder Javadoc](../main/java/tech/derbent/abstracts/annotations/CEntityFormBuilder.java) - Form builder API
- [CDataProviderResolver Javadoc](../main/java/tech/derbent/abstracts/annotations/CDataProviderResolver.java) - Data resolution details