# Calculated Fields Implementation Summary

## Problem Statement
Implement a pattern for calculated fields (such as sprint items total story points) using data providers with callback functions in service classes.

## Solution Implemented

### 1. Entity-Level Changes (CSprint.java)
Added `@AMetaData` annotations with data provider configuration to transient calculated fields:

```java
// Item Count field
@Transient
@AMetaData(
    displayName = "Item Count",
    dataProviderBean = "CSprintService",
    dataProviderMethod = "getItemCount"
)
private Integer itemCount;

// Total Story Points field
@Transient
@AMetaData(
    displayName = "Total Story Points",
    dataProviderBean = "CSprintService",
    dataProviderMethod = "getTotalStoryPoints"
)
private Long totalStoryPoints;
```

### 2. Service-Level Changes (CSprintService.java)
Implemented data provider callback methods:

```java
/** Data provider callback: Calculates the total number of items in a sprint */
public Integer getItemCount(final CSprint sprint) {
    if (sprint == null) {
        LOGGER.warn("getItemCount called with null sprint");
        return 0;
    }
    return sprint.getItemCount();
}

/** Data provider callback: Calculates the total story points for all items */
public Long getTotalStoryPoints(final CSprint sprint) {
    if (sprint == null) {
        LOGGER.warn("getTotalStoryPoints called with null sprint");
        return 0L;
    }
    return sprint.getTotalStoryPoints();
}
```

### 3. Documentation
Created comprehensive documentation file: `docs/development/calculated-fields-pattern.md`

**Contents:**
- Pattern overview and use cases
- Complete implementation examples
- Step-by-step guide for entity, service, and page service patterns
- Best practices and performance considerations
- DO's and DON'Ts
- Related patterns and references

## How It Works

1. **Field Declaration**: Entity declares `@Transient` field with `@AMetaData` specifying service bean and method
2. **Metadata Discovery**: Form builder/grid builder detects the annotation
3. **Bean Lookup**: Framework uses Spring context to find the service bean
4. **Method Invocation**: Framework calls the specified method passing entity instance
5. **Value Display**: Returned value is used for display in UI

## Benefits

- ✅ **No Database Storage**: Values computed on-demand, no DB columns needed
- ✅ **Always Current**: Recalculated each time displayed
- ✅ **Centralized Logic**: Computation in service layer, not UI
- ✅ **Reusable**: Same callback for forms, grids, reports
- ✅ **Type-Safe**: Compile-time checking
- ✅ **Testable**: Service methods unit-testable

## Example Usage

### Sprint Total Story Points
When a sprint form or grid displays the "Total Story Points" field:
1. Form builder detects `dataProviderBean="CSprintService"`
2. Calls `CSprintService.getTotalStoryPoints(sprint)`
3. Service delegates to `sprint.getTotalStoryPoints()`
4. Entity method sums story points from all sprint items
5. Computed value displayed in UI

### Sprint Item Count
Same pattern for item count:
1. Field annotation points to `CSprintService.getItemCount()`
2. Service method returns `sprint.getItemCount()`
3. Entity method returns `sprintItems.size()`
4. Count displayed in UI

## Files Changed

1. **src/main/java/tech/derbent/app/sprints/domain/CSprint.java**
   - Added data provider annotations to itemCount field
   - Added totalStoryPoints field with annotations
   - Added setter for totalStoryPoints

2. **src/main/java/tech/derbent/app/sprints/service/CSprintService.java**
   - Added getItemCount() callback method
   - Added getTotalStoryPoints() callback method
   - Added comprehensive documentation comments

3. **docs/development/calculated-fields-pattern.md** (NEW)
   - Complete pattern documentation
   - Implementation guide
   - Examples and best practices

## Testing

- ✅ Code compiles successfully with `mvn clean compile`
- ✅ All annotations properly configured
- ✅ Service methods follow naming conventions
- ✅ Null safety implemented in callbacks
- ✅ Documentation complete with examples

## Future Applications

This pattern can now be used for any calculated field in any entity:

**Examples:**
- Project completion percentage
- Task time estimates vs actuals
- Resource utilization percentages
- Financial totals and summaries
- Status distribution counts
- Average ratings or scores

**Template:**
```java
// In Entity
@Transient
@AMetaData(
    displayName = "Display Name",
    dataProviderBean = "YourEntityService",
    dataProviderMethod = "calculateFieldName"
)
private ReturnType fieldName;

// In Service
public ReturnType calculateFieldName(YourEntity entity) {
    if (entity == null) {
        return defaultValue;
    }
    return entity.computeValue();
}
```

## References

- Sprint domain: `tech.derbent.app.sprints.domain.CSprint`
- Sprint service: `tech.derbent.app.sprints.service.CSprintService`
- Metadata annotation: `tech.derbent.api.annotations.AMetaData`
- Form builder: `tech.derbent.api.annotations.CFormBuilder`
- Documentation: `docs/development/calculated-fields-pattern.md`
