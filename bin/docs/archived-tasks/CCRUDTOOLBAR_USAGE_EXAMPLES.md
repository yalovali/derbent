# CCrudToolbar Usage Examples

## Example 1: Simple Entity Page (Recommended Pattern)

```java
package tech.derbent.plm.activities.view;

import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.views.CAbstractPage;
import tech.derbent.api.views.components.CCrudToolbar;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.service.CActivityService;

public class CActivityPageNew extends CAbstractPage {
    
    private CCrudToolbar toolbar;
    private CEnhancedBinder<CActivity> binder;
    private CActivityService activityService;
    
    public CActivityPageNew(CActivityService activityService) {
        this.activityService = activityService;
        this.binder = new CEnhancedBinder<>(CActivity.class);
        
        // Create toolbar with minimal configuration
        toolbar = CCrudToolbar.create(binder);
        
        // Configure toolbar for CActivity
        toolbar.setEntityService(activityService);
        toolbar.setEntityClass(CActivity.class);
        toolbar.setNewEntitySupplier(() -> {
            CActivity activity = new CActivity();
            // Set defaults
            activity.setProject(sessionService.getActiveProject().orElse(null));
            return activity;
        });
        toolbar.setRefreshCallback(entity -> {
            // Reload from database
            CActivity reloaded = activityService.getById(entity.getId()).orElse(null);
            if (reloaded != null) {
                binder.setBean(reloaded);
            }
        });
        toolbar.setSaveCallback(entity -> {
            // Validate and save
            binder.writeBean(entity);
            CActivity saved = activityService.save(entity);
            binder.setBean(saved);
            toolbar.setCurrentEntity(saved);
        });
        toolbar.setNotificationService(notificationService);
        toolbar.setWorkflowStatusRelationService(workflowService);
        
        // Add toolbar to page
        add(toolbar);
        
        // Create form fields bound to binder
        createFormFields();
    }
    
    private void createFormFields() {
        // Form fields bound to binder
        // ...
    }
}
```

## Example 2: Dynamic Multi-Entity Page (Gantt-like)

```java
package tech.derbent.plm.gannt.view;

import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.views.CAbstractPage;
import tech.derbent.api.views.components.CCrudToolbar;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.meetings.domain.CMeeting;
import tech.derbent.plm.meetings.service.CMeetingService;

public class CGanttChartPage extends CAbstractPage {
    
    private CCrudToolbar toolbar;
    private CEnhancedBinder<?> binder;
    private Class<?> currentEntityType;
    
    private CActivityService activityService;
    private CMeetingService meetingService;
    
    public CGanttChartPage(CActivityService activityService, CMeetingService meetingService) {
        this.activityService = activityService;
        this.meetingService = meetingService;
        
        // Create toolbar with minimal configuration
        // No entity type set yet - will be determined when user selects item
        toolbar = CCrudToolbar.create(null);
        toolbar.setNotificationService(notificationService);
        
        add(toolbar);
        createGanttGrid();
    }
    
    private void createGanttGrid() {
        // Create grid with Activities and Meetings
        // ...
        
        grid.addSelectionListener(event -> {
            CEntityDB<?> selectedEntity = event.getSelectedItem();
            onEntitySelected(selectedEntity);
        });
    }
    
    private void onEntitySelected(CEntityDB<?> selectedEntity) {
        if (selectedEntity == null) {
            toolbar.setCurrentEntity(null);
            return;
        }
        
        Class<?> entityType = selectedEntity.getClass();
        
        // Check if we need to reconfigure for different entity type
        if (!entityType.equals(currentEntityType)) {
            reconfigureForEntityType(entityType);
        }
        
        // Set current entity
        toolbar.setCurrentEntity(selectedEntity);
        binder.setBean(selectedEntity);
    }
    
    private void reconfigureForEntityType(Class<?> entityType) {
        // Get appropriate service for entity type
        CAbstractService<?> service = getServiceForEntityType(entityType);
        
        // Reconfigure toolbar
        toolbar.reconfigureForEntityType(entityType, service);
        
        // Create new binder for entity type
        binder = new CEnhancedBinder<>(entityType);
        
        // Set new entity supplier based on type
        if (entityType == CActivity.class) {
            toolbar.setNewEntitySupplier(() -> {
                CActivity activity = new CActivity();
                activity.setProject(sessionService.getActiveProject().orElse(null));
                return activity;
            });
        } else if (entityType == CMeeting.class) {
            toolbar.setNewEntitySupplier(() -> {
                CMeeting meeting = new CMeeting();
                meeting.setProject(sessionService.getActiveProject().orElse(null));
                return meeting;
            });
        }
        
        // Set refresh callback
        toolbar.setRefreshCallback(entity -> {
            CEntityDB<?> reloaded = service.getById(((CEntityDB<?>) entity).getId()).orElse(null);
            if (reloaded != null) {
                binder.setBean(reloaded);
            }
        });
        
        // Set save callback
        toolbar.setSaveCallback(entity -> {
            binder.writeBean(entity);
            CEntityDB<?> saved = service.save((CEntityDB<?>) entity);
            binder.setBean(saved);
            toolbar.setCurrentEntity(saved);
        });
        
        currentEntityType = entityType;
        
        // Rebuild form for new entity type
        rebuildForm(entityType);
    }
    
    private CAbstractService<?> getServiceForEntityType(Class<?> entityType) {
        if (entityType == CActivity.class) {
            return activityService;
        } else if (entityType == CMeeting.class) {
            return meetingService;
        }
        throw new IllegalArgumentException("Unsupported entity type: " + entityType);
    }
    
    private void rebuildForm(Class<?> entityType) {
        // Rebuild form fields for new entity type
        // ...
    }
}
```

## Example 3: Legacy Pattern (Backward Compatible)

```java
package tech.derbent.plm.projects.view;

import tech.derbent.api.views.CAbstractEntityDBPage;
import tech.derbent.plm.projects.domain.CProject;
import tech.derbent.plm.projects.service.CProjectService;

// No changes needed - uses deprecated constructor internally
public class CProjectPage extends CAbstractEntityDBPage<CProject> {
    
    public CProjectPage(CProjectService projectService, ISessionService sessionService) {
        super(CProject.class, projectService, sessionService);
        // Toolbar is automatically created and configured in parent constructor
        // Uses deprecated CCrudToolbar(IContentOwner, binder) constructor
    }
    
    // Custom save validation if needed
    @Override
    protected boolean onBeforeSaveEvent() {
        // Custom validation
        return super.onBeforeSaveEvent();
    }
}
```

## Example 4: Custom Configuration

```java
package tech.derbent.plm.custom.view;

import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.views.CAbstractPage;
import tech.derbent.api.views.components.CCrudToolbar;

public class CCustomPage extends CAbstractPage {
    
    private CCrudToolbar toolbar;
    
    public CCustomPage() {
        CEnhancedBinder<CMyEntity> binder = new CEnhancedBinder<>(CMyEntity.class);
        
        // Create toolbar
        toolbar = CCrudToolbar.create(binder);
        
        // Full configuration
        toolbar.setEntityService(myEntityService);
        toolbar.setEntityClass(CMyEntity.class);
        toolbar.setNewEntitySupplier(() -> new CMyEntity());
        toolbar.setNotificationService(notificationService);
        toolbar.setWorkflowStatusRelationService(workflowService);
        
        // Custom refresh - reload from database and update UI
        toolbar.setRefreshCallback(entity -> {
            CMyEntity reloaded = myEntityService.getById(entity.getId()).orElse(null);
            if (reloaded != null) {
                binder.setBean(reloaded);
                updateCustomUI(reloaded);
            }
        });
        
        // Custom save - with extra validation
        toolbar.setSaveCallback(entity -> {
            // Custom pre-save logic
            if (!validateCustomRules(entity)) {
                notificationService.showError("Custom validation failed");
                return;
            }
            
            // Save
            binder.writeBean(entity);
            CMyEntity saved = myEntityService.save(entity);
            binder.setBean(saved);
            toolbar.setCurrentEntity(saved);
            
            // Custom post-save logic
            updateRelatedEntities(saved);
            notificationService.showSuccess("Saved successfully");
        });
        
        // Custom dependency checker
        toolbar.setDependencyChecker(entity -> {
            // Custom dependency check
            if (hasCustomDependencies(entity)) {
                return "Cannot delete: custom dependencies exist";
            }
            return null; // OK to delete
        });
        
        // Hide specific buttons if needed
        toolbar.configureButtonVisibility(true, true, false, true); // No delete button
        
        add(toolbar);
    }
    
    private boolean validateCustomRules(CMyEntity entity) {
        // Custom validation logic
        return true;
    }
    
    private void updateCustomUI(CMyEntity entity) {
        // Update custom UI components
    }
    
    private void updateRelatedEntities(CMyEntity entity) {
        // Update related entities
    }
    
    private boolean hasCustomDependencies(CMyEntity entity) {
        // Check custom dependencies
        return false;
    }
}
```

## Example 5: Incremental Configuration

```java
package tech.derbent.plm.incremental.view;

import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.views.CAbstractPage;
import tech.derbent.api.views.components.CCrudToolbar;

public class CIncrementalPage extends CAbstractPage {
    
    private CCrudToolbar toolbar;
    
    public CIncrementalPage() {
        CEnhancedBinder<CMyEntity> binder = new CEnhancedBinder<>(CMyEntity.class);
        
        // Create toolbar - visible but buttons disabled
        toolbar = CCrudToolbar.create(binder);
        add(toolbar);
        
        // Buttons are now visible but disabled
        
        // Configure incrementally as services become available
        configureServices();
    }
    
    private void configureServices() {
        // Set entity service - Save/Delete buttons enable
        toolbar.setEntityService(myEntityService);
        
        // Set new entity supplier - Create button enables
        toolbar.setNewEntitySupplier(() -> new CMyEntity());
        
        // Set refresh callback - Refresh button enables
        toolbar.setRefreshCallback(entity -> {
            CMyEntity reloaded = myEntityService.getById(entity.getId()).orElse(null);
            if (reloaded != null) {
                binder.setBean(reloaded);
            }
        });
        
        // Set notification service - Better error messages
        toolbar.setNotificationService(notificationService);
    }
}
```

## Key Differences from Old Pattern

### Old Pattern (Required all parameters upfront)
```java
// Had to provide everything at construction
CCrudToolbar toolbar = new CCrudToolbar(
    entityService,
    entityClass,
    binder,
    newEntitySupplier,
    entityRefreshedCallback,
    notificationService,
    workflowService,
    updateListener
);
```

### New Pattern (Flexible configuration)
```java
// Create with minimal requirements
CCrudToolbar toolbar = CCrudToolbar.create(binder);

// Configure as needed
toolbar.setEntityService(entityService);
toolbar.setNewEntitySupplier(() -> new MyEntity());
// ... other configuration

// Can reconfigure for different entity types
toolbar.reconfigureForEntityType(newClass, newService);
```

## Benefits

1. **Flexibility**: Configure only what you need
2. **Incremental**: Add configuration as services become available
3. **Dynamic**: Change entity type at runtime
4. **Clear**: Each setter has clear purpose
5. **Visible**: Toolbar visible even without full configuration
6. **Backward Compatible**: Old code still works with deprecated constructor
