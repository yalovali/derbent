# PageService Pattern Implementation

## Overview

The PageService pattern binds utility classes to dynamic page entities at runtime. When a page is loaded from the database, the system creates an instance of the corresponding PageService class and connects it to enable custom business logic and user code extensions.

**Key Feature**: PageService provides complete CRUD (Create, Read, Update, Delete) operations that integrate seamlessly with the view layer, binder, and notification system.

## Architecture

### Core Classes

1. **CPageService** - Base abstract class for all page services with complete CRUD implementation
2. **CPageServiceDynamicPage** - Intermediate abstract class extending CPageService
3. **CPageServiceUtility** - Service that maps page service names to their implementation classes

### CRUD Operations Flow

The PageService pattern implements the following flow:

```
CCrudToolbar → ICrudToolbarOwnerPage (CDynamicPageViewWithSections) → CPageService → View Updates
```

1. **User clicks button** in CCrudToolbar (New, Save, Delete, Refresh)
2. **Toolbar calls** `pageBase.getPageService().actionXXX()`
3. **PageService executes** business logic (create entity, save, delete, refresh)
4. **PageService updates view** via `view.populateForm()` and `view.refreshGrid()`
5. **User sees results** with notifications and updated UI

### Implementation Classes

All PageService implementations are located in:
```
tech.derbent.api.services.pageservice.implementations
```

## Available PageService Implementations (30 total)

### Main Entities (10)
- `CPageServiceActivity` - Activity management
- `CPageServiceComment` - Comment management
- `CPageServiceCompany` - Company management
- `CPageServiceDecision` - Decision management
- `CPageServiceMeeting` - Meeting management
- `CPageServiceOrder` - Order management
- `CPageServiceProject` - Project management
- `CPageServiceRisk` - Risk management
- `CPageServiceUser` - User management
- `CPageServiceSystemSettings` - System settings management

### Type/Status Entities (18)
- `CPageServiceActivityPriority` - Activity priority types
- `CPageServiceProjectItemStatus` - Activity status types
- `CPageServiceActivityType` - Activity types
- `CPageServiceCommentPriority` - Comment priority types
- `CPageServiceDecisionStatus` - Decision status types
- `CPageServiceDecisionType` - Decision types
- `CPageServiceMeetingStatus` - Meeting status types
- `CPageServiceMeetingType` - Meeting types
- `CPageServiceOrderStatus` - Order status types
- `CPageServiceOrderType` - Order types
- `CPageServiceOrderApproval` - Order approval workflow
- `CPageServiceApprovalStatus` - Approval status types
- `CPageServiceCurrency` - Currency configuration
- `CPageServiceRiskStatus` - Risk status types
- `CPageServiceUserCompanyRole` - User-company role assignments
- `CPageServiceUserCompanySetting` - User-company settings
- `CPageServiceUserProjectRole` - User-project role assignments
- `CPageServiceUserProjectSettings` - User-project settings

### System Entities (2)
- `CPageServicePageEntity` - Dynamic page entity management
- `CPageServiceGridEntity` - Grid configuration management

## How It Works

### 1. Page Entity Creation

When a page entity is created during data initialization, the system automatically assigns the appropriate PageService:

```java
// In CInitializerServiceBase.createPageEntity()
String pageServiceName = getPageServiceNameForEntityClass(entityClass);
if (pageServiceName != null) {
    page.setPageService(pageServiceName);
}
```

### 2. Entity-to-PageService Mapping

The `CInitializerServiceBase.getPageServiceNameForEntityClass()` method maps entity classes to PageService names:

```java
switch (className) {
    case "CActivity":
        return "CPageServiceActivity";
    case "CComment":
        return "CPageServiceComment";
    // ... etc for all 30 entity types
}
```

### 3. PageService Instantiation

When a dynamic page is loaded, the system:

1. Gets the pageService name from the CPageEntity
2. Uses CPageServiceUtility to find the corresponding class
3. Creates an instance passing the view as parameter
4. Calls bind() to connect the service

```java
// In CDynamicPageBase.getPageService()
Class<?> clazz = CPageServiceUtility.getPageServiceClassByName(pageEntity.getPageService());
var constructor = clazz.getDeclaredConstructor(CDynamicPageBase.class);
CPageService page = (CPageService) constructor.newInstance(this);
```

### 4. Binding Phase

The bind() method is called during page navigation:

```java
@Override
public void beforeEnter(final BeforeEnterEvent event) {
    // Security and validation checks...
    pageService.bind();
}
```

## Creating a New PageService

To add a PageService for a new entity type:

### 1. Create the Implementation Class

```java
package tech.derbent.api.services.pageservice.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.page.view.CDynamicPageBase;
import tech.derbent.yourmodule.domain.CYourEntity;

public class CPageServiceYourEntity extends CPageServiceDynamicPage<CYourEntity> {

    Logger LOGGER = LoggerFactory.getLogger(CPageServiceYourEntity.class);
    Long serialVersionUID = 1L;

    public CPageServiceYourEntity(CDynamicPageBase view) {
        super(view);
    }

    @Override
    public void bind() {
        try {
            LOGGER.debug("Binding {} to dynamic page for entity {}.", 
                this.getClass().getSimpleName(), 
                CYourEntity.class.getSimpleName());
            Check.notNull(view, "View must not be null to bind page service.");
            super.bind();
            
            // Add your custom logic here
            
        } catch (Exception e) {
            LOGGER.error("Error binding {} to dynamic page for entity {}: {}", 
                this.getClass().getSimpleName(), 
                CYourEntity.class.getSimpleName(),
                e.getMessage());
            throw e;
        }
    }
}
```

### 2. Register in CPageServiceUtility

Add to the availablePageServices list:
```java
private static final List<String> availablePageServices = List.of(
    // ... existing services
    "CPageServiceYourEntity"
);
```

Add to getPageServiceClassByName():
```java
case "CPageServiceYourEntity":
    return CPageServiceYourEntity.class;
```

Add the import:
```java
import tech.derbent.api.services.pageservice.implementations.CPageServiceYourEntity;
```

### 3. Map in CInitializerServiceBase

Add to getPageServiceNameForEntityClass():
```java
case "CYourEntity":
    return "CPageServiceYourEntity";
```

## Pattern Benefits

1. **Separation of Concerns** - Business logic is separated from view code
2. **Runtime Binding** - PageServices are bound dynamically when pages load
3. **Extensibility** - Easy to add custom logic without modifying core view classes
4. **Type Safety** - Generic typing ensures correct entity-service matching
5. **Consistency** - All entities follow the same pattern
6. **Complete CRUD** - Full create, read, update, delete operations with proper error handling
7. **Binder Integration** - Seamlessly integrates with Vaadin binders for form data binding

## CRUD Operations

### actionCreate()

Creates a new entity instance and prepares the form for data entry:

```java
public void actionCreate() {
    // 1. Create new entity using reflection
    final EntityClass newEntity = getEntityClass().getDeclaredConstructor().newInstance();
    
    // 2. Set project context if applicable
    if (newEntity instanceof CEntityOfProject) {
        ((CEntityOfProject<?>) newEntity).setProject(activeProject);
    }
    
    // 3. Set current entity
    setCurrentEntity(newEntity);
    
    // 4. Populate form to display the new entity
    view.populateForm();
    
    // 5. Show success notification
    getNotificationService().showSuccess("New entity created...");
}
```

**Key Features:**
- Automatic project context setting
- Special handling for CUser entities
- **Calls `view.populateForm()`** to refresh form fields (fixes issue where form wasn't updated)
- Shows user-friendly notifications

### actionSave()

Saves the current entity with validation and error handling:

```java
public void actionSave() {
    // 1. Get current entity
    final EntityClass entity = getCurrentEntity();
    
    // 2. Write form data to entity using binder
    if (view.getBinder() != null) {
        view.getBinder().writeBean(entity);
    }
    
    // 3. Save entity via service
    final EntityClass savedEntity = getEntityService().save(entity);
    
    // 4. Update current entity with saved version
    setCurrentEntity(savedEntity);
    
    // 5. Refresh grid and form
    view.refreshGrid();
    view.populateForm();
    
    // 6. Show success notification
    getNotificationService().showSaveSuccess();
}
```

**Key Features:**
- Uses `view.getBinder().writeBean()` to write form data to entity
- Handles optimistic locking exceptions
- Handles validation exceptions with user-friendly messages
- Refreshes both grid and form after save
- Updates entity with generated ID for new entities

### actionDelete()

Deletes the current entity with confirmation:

```java
public void actionDelete() {
    // 1. Get current entity
    final EntityClass entity = getCurrentEntity();
    
    // 2. Show confirmation dialog
    getNotificationService().showConfirmationDialog("Delete selected item?", () -> {
        // 3. Delete entity
        getEntityService().delete(entity.getId());
        
        // 4. Clear current entity
        setCurrentEntity(null);
        
        // 5. Refresh grid and form
        view.refreshGrid();
        view.populateForm();
        
        // 6. Show success notification
        getNotificationService().showDeleteSuccess();
    });
}
```

**Key Features:**
- Shows confirmation dialog before deleting
- Clears current selection after delete
- Refreshes grid to remove deleted item
- Proper error handling with user notifications

### actionRefresh()

Reloads the current entity from the database:

```java
public void actionRefresh() {
    // 1. Get current entity
    final EntityClass entity = getCurrentEntity();
    
    // 2. Reload from database
    final CEntityDB<?> reloaded = getEntityService().getById(entity.getId()).orElse(null);
    
    // 3. Update view with reloaded entity
    if (reloaded != null) {
        view.onEntityRefreshed(reloaded);
        getNotificationService().showInfo("Entity refreshed successfully");
    }
}
```

**Key Features:**
- Discards unsaved form changes
- Reloads fresh data from database
- Updates form with refreshed data via `onEntityRefreshed()`
- Warns if entity was deleted

## Pattern Structure

```
CPageService (abstract base)
    ↓
CPageServiceDynamicPage<T> (generic intermediate)
    ↓
CPageServiceActivity, CPageServiceComment, etc. (concrete implementations)
```

Each implementation:
- Extends CPageServiceDynamicPage<EntityClass>
- Takes CDynamicPageBase view in constructor
- Implements bind() method with custom logic
- Has access to view, entity service, and entity class via inherited methods
- **Inherits all CRUD operations from base class** - no need to override unless custom behavior needed

## View Integration

The PageService accesses the view through these key methods:

```java
// Get current entity
view.getCurrentEntity()

// Set current entity
view.setCurrentEntity(entity)

// Refresh form fields
view.populateForm()

// Refresh grid data
view.refreshGrid()

// Handle entity refresh
view.onEntityRefreshed(entity)

// Get the binder for form data
view.getBinder()

// Get services
view.getEntityService()
view.getSessionService()
view.getNotificationService()
```

## Simple Pattern Guidelines

1. **View Layer** (CDynamicPageViewWithSections, CDynamicPageBase)
   - Manages UI components (grids, forms, layouts)
   - Delegates CRUD actions to PageService
   - Updates UI when notified by PageService

2. **PageService Layer** (CPageService, implementations)
   - Handles CRUD business logic
   - Accesses view through simple getter methods
   - Uses binder to read/write form data
   - Calls view methods to update UI

3. **Service Layer** (CAbstractService, entity services)
   - Pure data manipulation
   - No UI dependencies
   - Database operations only

## Binder Integration

The PageService uses the view's binder to read and write form data:

```java
// Write form data to entity (in actionSave)
if (view.getBinder() != null) {
    view.getBinder().writeBean(entity);
}

// Read entity data to form (handled by view.populateForm)
view.populateForm(); // This calls binder.setBean(entity) internally
```

The binder ensures:
- Form validation before save
- Automatic field-to-property mapping
- Type-safe data binding
- Validation error messages

## Future Enhancements

The bind() method in each PageService can be extended to:
- Initialize custom components
- Set up event listeners
- Configure validation rules
- Load additional data
- Apply business logic
- Register custom handlers

This provides a clean extension point for adding entity-specific behavior without modifying the core framework.
