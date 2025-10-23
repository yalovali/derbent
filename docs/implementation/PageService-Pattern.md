# PageService Pattern Implementation

## Overview

The PageService pattern binds utility classes to dynamic page entities at runtime. When a page is loaded from the database, the system creates an instance of the corresponding PageService class and connects it to enable custom business logic and user code extensions.

## Architecture

### Core Classes

1. **CPageService** - Base abstract class for all page services
2. **CPageServiceDynamicPage** - Intermediate abstract class extending CPageService
3. **CPageServiceUtility** - Service that maps page service names to their implementation classes

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
import tech.derbent.app.page.view.CDynamicPageBase;
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

## Future Enhancements

The bind() method in each PageService can be extended to:
- Initialize custom components
- Set up event listeners
- Configure validation rules
- Load additional data
- Apply business logic
- Register custom handlers

This provides a clean extension point for adding entity-specific behavior without modifying the core framework.
