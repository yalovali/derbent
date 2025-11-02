# CCrudToolbar Refactoring Guide

## Overview
The CCrudToolbar has been refactored to support dynamic entity type changes and provide a more flexible configuration approach. This is essential for pages like Gantt charts where the entity type can change based on user interaction.

## Key Changes

### 1. Minimal Constructor
The toolbar can now be created with just a binder:

```java
// Create toolbar with minimal configuration
CCrudToolbar toolbar = CCrudToolbar.create(binder);
```

or

```java
CCrudToolbar toolbar = new CCrudToolbar(binder);
```

### 2. Post-Construction Configuration
All configuration can be set after construction:

```java
// Create toolbar
CCrudToolbar toolbar = CCrudToolbar.create(binder);

// Configure as needed
toolbar.setEntityService(entityService);
toolbar.setEntityClass(MyEntity.class);
toolbar.setNewEntitySupplier(() -> new MyEntity());
toolbar.setRefreshCallback(entity -> refreshForm(entity));
toolbar.setSaveCallback(entity -> saveEntity(entity));
toolbar.setNotificationService(notificationService);
toolbar.setWorkflowStatusRelationService(workflowService);
```

### 3. Dynamic Entity Type Changes
The toolbar can now be reconfigured for different entity types:

```java
// Initial configuration for Activity
toolbar.reconfigureForEntityType(CActivity.class, activityService);

// Later, when user selects a different entity type in Gantt
toolbar.reconfigureForEntityType(CMeeting.class, meetingService);
```

### 4. Visible with Disabled Buttons
When not fully configured, the toolbar is visible but with disabled buttons. This provides better UX feedback.

## Usage Patterns

### Pattern 1: Simple Entity Page (Recommended for new code)

```java
public class MyEntityPage extends CAbstractPage {
    private CCrudToolbar toolbar;
    private CEnhancedBinder<MyEntity> binder;
    
    public MyEntityPage() {
        binder = new CEnhancedBinder<>(MyEntity.class);
        
        // Create toolbar with minimal configuration
        toolbar = CCrudToolbar.create(binder);
        
        // Configure for MyEntity
        toolbar.setEntityService(myEntityService);
        toolbar.setEntityClass(MyEntity.class);
        toolbar.setNewEntitySupplier(() -> new MyEntity());
        toolbar.setNotificationService(notificationService);
        
        // Add toolbar to layout
        add(toolbar);
    }
}
```

### Pattern 2: Legacy Pattern (Backward Compatible)

```java
public class MyEntityPage extends CAbstractEntityDBPage<MyEntity> 
        implements IContentOwner {
    
    public MyEntityPage() {
        super(MyEntity.class, entityService, sessionService);
        // Toolbar is automatically created and configured in parent constructor
    }
}
```

The deprecated constructor automatically configures the toolbar from the IContentOwner interface.

### Pattern 3: Dynamic Entity Type Changes (Gantt Charts)

```java
public class GanttChartPage extends CAbstractPage {
    private CCrudToolbar toolbar;
    private CEnhancedBinder<?> binder;
    private Class<?> currentEntityType;
    
    public GanttChartPage() {
        // Create toolbar with minimal configuration
        toolbar = CCrudToolbar.create(null); // Will set binder later
        toolbar.setNotificationService(notificationService);
        
        add(toolbar);
    }
    
    private void onEntitySelected(CEntityDB<?> selectedEntity) {
        if (selectedEntity == null) {
            return;
        }
        
        // Determine entity type
        Class<?> entityType = selectedEntity.getClass();
        
        // Check if we need to reconfigure
        if (!entityType.equals(currentEntityType)) {
            // Get appropriate service for entity type
            CAbstractService<?> service = getServiceForEntityType(entityType);
            
            // Reconfigure toolbar
            toolbar.reconfigureForEntityType(entityType, service);
            
            // Create new binder for entity type
            binder = new CEnhancedBinder<>(entityType);
            
            // Update supplier for entity type
            toolbar.setNewEntitySupplier(() -> createEntityOfType(entityType));
            
            currentEntityType = entityType;
        }
        
        // Set current entity
        toolbar.setCurrentEntity(selectedEntity);
        binder.setBean(selectedEntity);
    }
}
```

## Migration Guide

### For Simple Entity Pages
If your page extends `CAbstractEntityDBPage`, no changes are required. The deprecated constructor is used for backward compatibility.

### For Dynamic Pages
If your page needs to support different entity types:

1. Create toolbar with minimal constructor
2. Use `reconfigureForEntityType()` when entity type changes
3. Update new entity supplier when entity type changes
4. Ensure status combobox is recreated for workflow-enabled entities

### For Custom Pages
If you're creating a custom page:

1. Use `CCrudToolbar.create(binder)` to create toolbar
2. Configure via setter methods
3. Call `setCurrentEntity()` when entity changes
4. Call `reconfigureForEntityType()` if entity type can change

## Benefits

1. **Flexibility**: Toolbar can be configured incrementally
2. **Dynamic**: Supports changing entity types at runtime
3. **Clear UX**: Disabled buttons show what's not configured
4. **Backward Compatible**: Existing code continues to work
5. **Type Safe**: Proper handling of generic types
6. **Better Separation**: Configuration is explicit and clear

## Testing Requirements

After implementing changes:

1. Test simple entity pages (Activities, Projects, Users)
2. Test dynamic pages (CDynamicPageViewWithSections)
3. Test Gantt chart pages with entity type changes
4. Verify status combobox updates correctly
5. Verify buttons are properly enabled/disabled
6. Test create, save, delete, refresh operations
7. Verify notifications work correctly
8. Check workflow transitions work correctly

## Status Combobox Behavior

The status combobox is now dynamically created based on entity type:

- Created only for entities implementing `IHasStatusAndWorkflow`
- Recreated when `reconfigureForEntityType()` is called
- Properly populated with valid workflow transitions
- Hidden when entity type doesn't support workflow

## Common Pitfalls

1. **Not calling updateButtonStates()**: If you manually set fields, button states won't update. Use setter methods instead.
2. **Forgetting to reconfigure**: When entity type changes, call `reconfigureForEntityType()`
3. **Null entity service**: Ensure entity service is set before operations
4. **Missing new entity supplier**: Create button will be disabled without it

## Future Enhancements

Potential improvements for future versions:

1. Add method to batch-configure multiple settings
2. Add validation to warn about incomplete configuration
3. Add event system for configuration changes
4. Add builder pattern alternative
5. Add configuration presets for common scenarios
