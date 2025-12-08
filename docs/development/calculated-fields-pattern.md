# Calculated Fields Pattern with Data Providers

## Overview

This document describes the pattern for implementing calculated/derived fields in entities using data provider callbacks. This pattern allows computed values (such as totals, counts, aggregations) to be displayed in the UI without being stored in the database.

## When to Use This Pattern

Use data provider callbacks for calculated fields when:

- **Field value is computed** from other entity data (e.g., sum of child items)
- **Value changes frequently** based on related data changes
- **No need to persist** the calculated value in the database
- **Computation is relatively fast** (avoid expensive calculations)

## Pattern Components

### 1. Entity Field Declaration

In your entity class, declare a `@Transient` field with `@AMetaData` annotation:

```java
@Transient
@AMetaData(
    displayName = "Total Story Points",
    required = false,
    readOnly = true,
    description = "Sum of story points for all items in this sprint",
    hidden = false,
    dataProviderBean = "CSprintService",      // Service bean name
    dataProviderMethod = "getTotalStoryPoints" // Method name in service
)
private Long totalStoryPoints;
```

**Key attributes:**
- `@Transient` - JPA will not persist this field
- `dataProviderBean` - Name of the Spring service bean (usually the entity's service class)
- `dataProviderMethod` - Name of the method to call for computing the value
- `readOnly = true` - Calculated fields should typically be read-only

### 2. Entity Getter/Setter

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

/** Sets the total story points. Typically set by service data provider.
 * @param totalStoryPoints the total story points value */
public void setTotalStoryPoints(final Long totalStoryPoints) {
    this.totalStoryPoints = totalStoryPoints;
}
```

### 3. Service Callback Method

In your service class (e.g., `CSprintService`), implement the callback method:

```java
/** Data provider callback: Calculates the total story points for all items in a sprint.
 * This method is invoked automatically by the form builder when displaying
 * the totalStoryPoints field.
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
- Method is called by form builder/grid builder automatically
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

1. **Form/Grid Building**: When CFormBuilder or CComponentGridEntity processes entity metadata
2. **Metadata Discovery**: Detects `@AMetaData` annotation with `dataProviderBean` and `dataProviderMethod`
3. **Bean Lookup**: Uses Spring context to find the service bean (e.g., `CSpringContext.getBean("CSprintService")`)
4. **Method Invocation**: Calls the specified method with the entity instance as parameter
5. **Value Display**: Uses returned value for display in form field or grid column

## Alternative: Page Service Data Providers

For view-specific calculated fields, you can also use page services:

```java
// In entity
@Transient
@AMetaData(
    displayName = "Available Statuses",
    dataProviderBean = "view",  // Special keyword for page service
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

1. **No Database Storage**: Calculated values are computed on-demand, no DB column needed
2. **Always Current**: Values are recalculated each time they're displayed
3. **Centralized Logic**: Computation logic lives in service layer, not UI layer
4. **Reusable**: Same callback can be used in forms, grids, reports
5. **Type-Safe**: Strongly typed method signatures, compile-time checking
6. **Testable**: Service methods can be unit tested independently

## Best Practices

### DO:
- ✅ Use for simple aggregations (sum, count, average)
- ✅ Handle `null` entity parameter gracefully
- ✅ Log warnings for unexpected states
- ✅ Delegate to entity getter methods when possible
- ✅ Mark field as `readOnly = true` in metadata
- ✅ Use descriptive method names matching field names

### DON'T:
- ❌ Perform expensive database queries in callbacks
- ❌ Modify entity state in data provider methods
- ❌ Use for fields that need to be searchable/filterable in database
- ❌ Return `null` - use sensible defaults (0, empty list, etc.)
- ❌ Throw exceptions - handle errors gracefully

## Performance Considerations

- **Lazy Loading**: Ensure related collections are loaded if needed for calculation
- **Caching**: Consider caching computed values if calculation is expensive
- **N+1 Queries**: Be aware of potential N+1 query issues when computing for multiple entities
- **Grid Display**: For grids with many rows, ensure calculations are efficient

## Related Patterns

- **Component Methods**: `createComponentMethod` for custom UI components
- **Filter Methods**: `filterMethod` for dynamic filtering
- **Validation Methods**: Custom validation logic in service layer

## See Also

- `tech.derbent.api.annotations.AMetaData` - Metadata annotation reference
- `tech.derbent.api.annotations.CFormBuilder` - Form building with data providers
- `tech.derbent.api.screens.view.CComponentGridEntity` - Grid building with data providers
- `tech.derbent.app.sprints.domain.CSprint` - Complete example implementation
- `tech.derbent.app.sprints.service.CSprintService` - Service callback examples
