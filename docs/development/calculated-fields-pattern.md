# Calculated Fields Pattern with JPA @PostLoad and Data Providers

## Overview

This document describes the pattern for implementing calculated/derived fields in entities using JPA `@PostLoad` lifecycle callbacks combined with data provider service methods. This pattern automatically populates computed values (such as totals, counts, aggregations) immediately after the entity is loaded from the database, following JPA best practices.

## When to Use This Pattern

Use this pattern for calculated fields when:

- **Field value is computed** from other entity data (e.g., sum of child items)
- **Value should be available immediately** after entity is loaded from database
- **No need to persist** the calculated value in the database
- **Computation is relatively fast** (avoid expensive calculations that slow down entity loading)
- **Business logic resides in service layer** (reusable, testable)

## Pattern Components

### 1. Entity Field Declaration

In your entity class, declare a `@Transient` field with `@AMetaData` annotation including `autoCalculate=true`:

```java
@Transient
@AMetaData(
    displayName = "Total Story Points",
    required = false,
    readOnly = true,
    description = "Sum of story points for all items in this sprint",
    hidden = false,
    dataProviderBean = "CSprintService",       // Service bean name
    dataProviderMethod = "getTotalStoryPoints", // Method name in service
    autoCalculate = true                        // Enable auto-calculation in @PostLoad
)
private Long totalStoryPoints;
```

**Key attributes:**
- `@Transient` - JPA will not persist this field
- `dataProviderBean` - Name of the Spring service bean (usually the entity's service class)
- `dataProviderMethod` - Name of the method to call for computing the value
- `autoCalculate = true` - **NEW**: Enables automatic calculation in `@PostLoad` callback
- `readOnly = true` - Calculated fields should typically be read-only

**Note:** Fields without `autoCalculate=true` will NOT be populated by `@PostLoad`, allowing selective calculation.

### 2. Entity @PostLoad Method

Add a `@PostLoad` method that discovers and calls service methods for fields marked with `autoCalculate=true`:

```java
import jakarta.persistence.PostLoad;

/** JPA lifecycle callback: Populates transient calculated fields after entity is loaded from database.
 * Automatically discovers fields with @AMetaData(autoCalculate=true) and invokes service methods. */
@PostLoad
protected void calculateTransientFields() {
    try {
        final java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
        for (final java.lang.reflect.Field field : fields) {
            final AMetaData metadata = field.getAnnotation(AMetaData.class);
            // Only process fields marked with autoCalculate=true
            if (metadata != null && metadata.autoCalculate() && 
                !metadata.dataProviderBean().isEmpty() && !metadata.dataProviderMethod().isEmpty()) {
                try {
                    // Resolve service bean using CDataProviderResolver
                    final Object serviceBean = CDataProviderResolver.resolveBean(
                        metadata.dataProviderBean(), null);
                    
                    if (serviceBean != null) {
                        // Invoke method using CAuxillaries (same pattern as form builder)
                        final Object value = CAuxillaries.invokeMethod(
                            serviceBean, metadata.dataProviderMethod(), this);
                        
                        // Set the field value
                        field.setAccessible(true);
                        field.set(this, value);
                    }
                } catch (final Exception e) {
                    LOGGER.warn("Error calculating field {}: {}", field.getName(), e.getMessage());
                }
            }
        }
    } catch (final Exception e) {
        LOGGER.error("Error in @PostLoad calculateTransientFields: {}", e.getMessage(), e);
    }
}
```

### 3. Entity Getter/Setter

Provide standard getter/setter methods:

```java
/** Get the total story points for all items in this sprint.
 * @return total story points, or 0 if no items have story points */
public Long getTotalStoryPoints() {
    if (sprintItems == null || sprintItems.isEmpty()) {
        return 0L;
    }
    long total = 0L;
    for (final CSprintItem sprintItem : sprintItems) {
        if (sprintItem.getItem() instanceof ISprintableItem) {
            final Long itemStoryPoint = ((ISprintableItem) sprintItem.getItem()).getStoryPoint();
            if (itemStoryPoint != null) {
                total += itemStoryPoint;
            }
        }
    }
    return total;
}

/** Sets the total story points. Populated automatically via @PostLoad after entity is loaded.
 * @param totalStoryPoints the total story points value */
public void setTotalStoryPoints(final Long totalStoryPoints) {
    this.totalStoryPoints = totalStoryPoints;
}
```

### 4. Service Callback Method

In your service class (e.g., `CSprintService`), implement the callback method:

```java
/** Data provider callback: Calculates the total story points for all items in a sprint.
 * Called automatically by @PostLoad after entity is loaded from database.
 * 
 * @param sprint the sprint entity to calculate story points for
 * @return sum of story points for all sprint items */
public Long getTotalStoryPoints(final CSprint sprint) {
    if (sprint == null) {
        LOGGER.warn("getTotalStoryPoints called with null sprint");
        return 0L;
    }
    return sprint.getTotalStoryPoints(); // Delegates to entity method
}
```

**Important points:**
- Method signature: `public ReturnType methodName(EntityType entity)`
- Method is called automatically by `@PostLoad` after entity is loaded from database
- Should handle `null` entity gracefully
- Typically delegates to entity's getter method
- Can include additional business logic if needed

## Complete Example: Sprint Story Points

### Entity: CSprint.java

```java
@Entity
@Table(name = "csprint")
public class CSprint extends CProjectItem<CSprint> {
    
    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL)
    @OrderBy("itemOrder ASC")
    private List<CSprintItem> sprintItems = new ArrayList<>();
    
    // Calculated field with data provider
    @Transient
    @AMetaData(
        displayName = "Total Story Points",
        required = false,
        readOnly = true,
        description = "Sum of story points for all items in this sprint",
        hidden = false,
        dataProviderBean = "CSprintService",
        dataProviderMethod = "getTotalStoryPoints"
    )
    private Long totalStoryPoints;
    
    // Getter computes value from child items
    public Long getTotalStoryPoints() {
        if (sprintItems == null || sprintItems.isEmpty()) {
            return 0L;
        }
        long total = 0L;
        for (final CSprintItem item : sprintItems) {
            if (item.getItem() instanceof ISprintableItem) {
                final Long points = ((ISprintableItem) item.getItem()).getStoryPoint();
                if (points != null) {
                    total += points;
                }
            }
        }
        return total;
    }
    
    // Setter for framework use
    public void setTotalStoryPoints(final Long totalStoryPoints) {
        this.totalStoryPoints = totalStoryPoints;
    }
}
```

### Service: CSprintService.java

```java
@Service
@PreAuthorize("isAuthenticated()")
public class CSprintService extends CProjectItemService<CSprint> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CSprintService.class);
    
    // ... other service methods ...
    
    /** Data provider callback for totalStoryPoints field */
    public Long getTotalStoryPoints(final CSprint sprint) {
        if (sprint == null) {
            LOGGER.warn("getTotalStoryPoints called with null sprint");
            return 0L;
        }
        return sprint.getTotalStoryPoints();
    }
    
    /** Data provider callback for itemCount field */
    public Integer getItemCount(final CSprint sprint) {
        if (sprint == null) {
            LOGGER.warn("getItemCount called with null sprint");
            return 0;
        }
        return sprint.getItemCount();
    }
}
```

## How It Works

1. **Entity Load**: JPA loads entity from database
2. **@PostLoad Trigger**: JPA automatically invokes the `calculateTransientFields()` method
3. **Field Discovery**: Method uses reflection to find fields with `@AMetaData(autoCalculate=true)`
4. **Bean Resolution**: Uses `CDataProviderResolver.resolveBean()` to get the service bean
5. **Method Invocation**: Calls service method using `CAuxillaries.invokeMethod()` with entity as parameter
6. **Field Population**: Sets the calculated value in the transient field
7. **Entity Ready**: Entity is now fully populated with calculated fields

**Comparison with Form Builder Approach:**
- **@PostLoad (NEW)**: Automatic calculation when entity loaded from DB (JPA lifecycle)
- **Form Builder (OLD)**: Manual calculation when form/grid is built (UI layer)
- **Benefits**: Earlier calculation, follows JPA patterns, works in all contexts (not just UI)

## Selective Calculation with autoCalculate

The `autoCalculate` attribute allows you to control which fields are calculated in `@PostLoad`:

```java
// This field WILL be calculated in @PostLoad
@Transient
@AMetaData(
    displayName = "Total Story Points",
    dataProviderBean = "CSprintService",
    dataProviderMethod = "getTotalStoryPoints",
    autoCalculate = true  // ✅ Auto-calculated
)
private Long totalStoryPoints;

// This field will NOT be calculated in @PostLoad (only when form builder processes it)
@Transient
@AMetaData(
    displayName = "Component Widget",
    dataProviderBean = "pageservice",
    dataProviderMethod = "getComponentWidget"
    // autoCalculate not set (defaults to false) ❌ Not auto-calculated
)
private CComponentWidgetEntity<CSprint> componentWidget;
```

**When to use `autoCalculate=true`:**
- Simple calculated values (counts, totals, aggregations)
- Values needed immediately after entity load
- Fast computations that don't slow down entity loading

**When to omit `autoCalculate` (defaults to false):**
- UI components that should only be created when needed
- Expensive calculations that would slow down entity loading
- Values that require UI context (like current user, view state)

## Alternative: Page Service Data Providers

For view-specific calculated fields, you can also use page services:

```java
// In entity
@Transient
@AMetaData(
    displayName = "Available Statuses",
    dataProviderBean = "pageservice",  // Special keyword for page service
    dataProviderMethod = "getAvailableStatusesForProjectItem"
)
private List<CProjectItemStatus> availableStatuses;

// In page service (e.g., CPageServiceSprint)
public List<CProjectItemStatus> getAvailableStatusesForProjectItem() {
    final CSprint entity = getView().getCurrentEntity();
    if (entity == null) {
        return List.of();
    }
    return projectItemStatusService.getValidNextStatuses(entity);
}
```

## Benefits

1. **JPA Lifecycle Integration**: Uses standard JPA `@PostLoad` callback (follows "the book")
2. **Automatic Calculation**: Fields populated immediately after entity load from database
3. **No Database Storage**: Calculated values computed on-demand, no DB column needed
4. **Service-Layer Logic**: Business logic centralized in service layer (testable, reusable)
5. **Annotation-Driven**: Declarative configuration via `@AMetaData(autoCalculate=true)`
6. **Selective Calculation**: Only fields marked with `autoCalculate=true` are processed
7. **Reuses Existing Infrastructure**: Leverages `CDataProviderResolver` and `CAuxillaries` patterns
8. **Type-Safe**: Strongly typed method signatures, compile-time checking
9. **Works Everywhere**: Not limited to UI context - works in services, repositories, tests

## Best Practices

### DO:
- ✅ Use `autoCalculate=true` for simple aggregations (sum, count, average)
- ✅ Handle `null` entity parameter gracefully in service methods
- ✅ Log warnings for unexpected states
- ✅ Delegate to entity getter methods when possible
- ✅ Mark field as `readOnly = true` in metadata
- ✅ Use descriptive method names matching field names
- ✅ Keep calculations fast to avoid slowing down entity loading

### DON'T:
- ❌ Perform expensive database queries in `@PostLoad` callbacks
- ❌ Modify entity state in data provider methods
- ❌ Use for fields that need to be searchable/filterable in database
- ❌ Return `null` - use sensible defaults (0, empty list, etc.)
- ❌ Throw exceptions - handle errors gracefully
- ❌ Set `autoCalculate=true` for expensive or UI-dependent calculations

## Performance Considerations

- **@PostLoad Timing**: Runs immediately after entity load, before transaction completes
- **Lazy Loading**: Ensure related collections are loaded if needed for calculation
- **Caching**: Consider caching computed values if calculation is expensive
- **N+1 Queries**: Be aware of potential N+1 query issues when computing for multiple entities
- **Grid Display**: For grids with many rows, ensure calculations are efficient

## Implementation Classes

The `@PostLoad` pattern leverages existing infrastructure classes:

### CDataProviderResolver
- **Location**: `tech.derbent.api.annotations.CDataProviderResolver`
- **Purpose**: Resolves service beans by name, handles special keywords like "pageservice", "session", "context"
- **Key Method**: `resolveBean(String beanName, IContentOwner contentOwner)`
- **Usage in @PostLoad**: Finds the service bean specified in `dataProviderBean` attribute

### CAuxillaries
- **Location**: `tech.derbent.api.utils.CAuxillaries`
- **Purpose**: Reflection utilities for invoking methods dynamically
- **Key Method**: `invokeMethod(Object target, String methodName, Object... args)`
- **Usage in @PostLoad**: Invokes the service method specified in `dataProviderMethod` attribute

### Flow Diagram
```
Entity Load (JPA)
    ↓
@PostLoad Callback
    ↓
For each field with @AMetaData(autoCalculate=true):
    ↓
CDataProviderResolver.resolveBean(dataProviderBean)
    ↓
CAuxillaries.invokeMethod(serviceBean, dataProviderMethod, entity)
    ↓
Field.set(entity, calculatedValue)
```

## Related Patterns

- **Component Methods**: `createComponentMethod` for custom UI components
- **Filter Methods**: `filterMethod` for dynamic filtering
- **Validation Methods**: Custom validation logic in service layer

## See Also

- `tech.derbent.api.annotations.AMetaData` - Metadata annotation reference (includes `autoCalculate` attribute)
- `tech.derbent.api.annotations.CDataProviderResolver` - Service bean resolution utility
- `tech.derbent.api.utils.CAuxillaries` - Reflection and method invocation utilities
- `tech.derbent.api.annotations.CFormBuilder` - Form building with data providers
- `tech.derbent.api.screens.view.CComponentGridEntity` - Grid building with data providers
- `tech.derbent.app.sprints.domain.CSprint` - Complete example implementation with `@PostLoad`
- `tech.derbent.app.sprints.service.CSprintService` - Service callback examples
- `jakarta.persistence.PostLoad` - JPA lifecycle callback documentation
