# Calculated Fields Implementation Summary

## Problem Statement
Implement a pattern for calculated fields (such as sprint items total story points) where values are calculated after JPA loads the entity from the database, using data provider service methods. The implementation should follow JPA best practices ("the book") and reuse existing infrastructure.

## Solution Implemented

### 1. Added autoCalculate Attribute to @AMetaData
**File**: `src/main/java/tech/derbent/api/annotations/AMetaData.java`

```java
/** When true, the field value is automatically calculated and populated after entity is loaded from database
 * using JPA @PostLoad lifecycle callback. */
boolean autoCalculate() default false;
```

**Purpose**: Allows selective field calculation - only fields marked with `autoCalculate=true` are processed by `@PostLoad`.

### 2. Entity-Level Changes (CSprint.java)
**File**: `src/main/java/tech/derbent/app/sprints/domain/CSprint.java`

**Added Import**:
```java
import jakarta.persistence.PostLoad;
```

**Updated Field Annotations**:
```java
@Transient
@AMetaData(
    displayName = "Item Count",
    dataProviderBean = "CSprintService",
    dataProviderMethod = "getItemCount",
    autoCalculate = true  // NEW: Enable auto-calculation
)
private Integer itemCount;

@Transient
@AMetaData(
    displayName = "Total Story Points",
    dataProviderBean = "CSprintService",
    dataProviderMethod = "getTotalStoryPoints",
    autoCalculate = true  // NEW: Enable auto-calculation
)
private Long totalStoryPoints;
```

**Added @PostLoad Method**:
```java
@PostLoad
protected void calculateTransientFields() {
    // Discovers fields with autoCalculate=true
    // Uses CDataProviderResolver.resolveBean() for service lookup
    // Uses CAuxillaries.invokeMethod() for method invocation
    // Populates field values after entity load
}
```

### 3. Service-Level Changes (CSprintService.java)
**File**: `src/main/java/tech/derbent/app/sprints/service/CSprintService.java`

**Updated Comments**:
- Clarified that methods are called by `@PostLoad` (not form builder)
- Explained the JPA lifecycle callback pattern
- Service methods themselves unchanged (still reusable)

```java
/** Data provider callback: Calculates the total story points for all items in a sprint.
 * Called automatically by @PostLoad after entity is loaded from database.
 * @param sprint the sprint entity to calculate story points for
 * @return sum of story points for all sprint items */
public Long getTotalStoryPoints(final CSprint sprint) {
    if (sprint == null) {
        LOGGER.warn("getTotalStoryPoints called with null sprint");
        return 0L;
    }
    return sprint.getTotalStoryPoints();
}
```

### 4. Documentation Updates
**File**: `docs/development/calculated-fields-pattern.md`

- Complete rewrite focusing on JPA `@PostLoad` pattern
- Added `autoCalculate` attribute documentation
- Added implementation class references (CDataProviderResolver, CAuxillaries)
- Added flow diagram showing the calculation process
- Updated all examples to use new pattern

## How It Works

```
1. JPA loads entity from database
        ↓
2. JPA triggers @PostLoad callback
        ↓
3. calculateTransientFields() discovers fields with autoCalculate=true
        ↓
4. CDataProviderResolver.resolveBean("CSprintService") → service bean
        ↓
5. CAuxillaries.invokeMethod(service, "getTotalStoryPoints", entity) → calculated value
        ↓
6. field.set(entity, calculatedValue)
        ↓
7. Entity ready with populated calculated fields
```

## Key Benefits

1. **JPA Lifecycle Integration**: Uses standard `@PostLoad` callback (follows JPA specification)
2. **Automatic Calculation**: Fields populated immediately when entity loads from database
3. **Reuses Infrastructure**: Leverages existing `CDataProviderResolver` and `CAuxillaries` patterns
4. **Selective Calculation**: Only fields with `autoCalculate=true` are processed
5. **Service Logic Preserved**: Business logic stays in service layer (testable, reusable)
6. **Works Everywhere**: Not limited to UI context - works in services, repositories, tests
7. **Annotation-Driven**: Declarative configuration via metadata

## Pattern Comparison

### Before (Form Builder Approach)
- Calculated when form/grid is built
- Only works in UI layer
- Manual invocation required

### After (JPA @PostLoad Approach)
- ✅ Calculated when entity loads from DB
- ✅ Works in all contexts (UI, services, tests)
- ✅ Automatic via JPA lifecycle
- ✅ Follows JPA best practices
- ✅ Reuses existing infrastructure

## Example Usage

### In Entity
```java
@Transient
@AMetaData(
    displayName = "Total Story Points",
    dataProviderBean = "CSprintService",
    dataProviderMethod = "getTotalStoryPoints",
    autoCalculate = true  // Enable @PostLoad calculation
)
private Long totalStoryPoints;
```

### In Service
```java
public Long getTotalStoryPoints(final CSprint sprint) {
    if (sprint == null) return 0L;
    return sprint.getTotalStoryPoints();
}
```

### Result
When any code loads a CSprint entity from the database:
```java
CSprint sprint = sprintRepository.findById(id);
// @PostLoad automatically runs
// sprint.getTotalStoryPoints() is already populated!
```

## Files Changed

1. **src/main/java/tech/derbent/api/annotations/AMetaData.java**
   - Added `autoCalculate` boolean attribute

2. **src/main/java/tech/derbent/app/sprints/domain/CSprint.java**
   - Added `@PostLoad` import
   - Added `autoCalculate=true` to calculated fields
   - Implemented `calculateTransientFields()` method

3. **src/main/java/tech/derbent/app/sprints/service/CSprintService.java**
   - Updated documentation comments
   - Service methods unchanged

4. **docs/development/calculated-fields-pattern.md**
   - Complete documentation rewrite
   - JPA @PostLoad focus
   - Implementation class references

## Infrastructure Classes Used

### CDataProviderResolver
- **Location**: `tech.derbent.api.annotations.CDataProviderResolver`
- **Method**: `resolveBean(String beanName, IContentOwner contentOwner)`
- **Purpose**: Resolves service beans by name, handles special keywords

### CAuxillaries
- **Location**: `tech.derbent.api.utils.CAuxillaries`
- **Method**: `invokeMethod(Object target, String methodName, Object... args)`
- **Purpose**: Reflection utilities for invoking methods dynamically

## Future Applications

This pattern can now be used for any calculated field in any entity:

**Template:**
```java
// In Entity
@Transient
@AMetaData(
    displayName = "Display Name",
    dataProviderBean = "YourEntityService",
    dataProviderMethod = "calculateFieldName",
    autoCalculate = true  // Enable @PostLoad
)
private ReturnType fieldName;

@PostLoad
protected void calculateTransientFields() {
    // Standard @PostLoad implementation (reusable across entities)
}

// In Service
public ReturnType calculateFieldName(YourEntity entity) {
    if (entity == null) return defaultValue;
    return entity.computeValue();
}
```

## Testing

- ✅ Code compiles successfully with `mvn clean compile`
- ✅ All annotations properly configured
- ✅ @PostLoad method follows JPA lifecycle patterns
- ✅ Uses existing CDataProviderResolver and CAuxillaries infrastructure
- ✅ Documentation complete with examples

## References

- JPA Specification: `@PostLoad` lifecycle callback
- Sprint domain: `tech.derbent.app.sprints.domain.CSprint`
- Sprint service: `tech.derbent.app.sprints.service.CSprintService`
- Data provider resolver: `tech.derbent.api.annotations.CDataProviderResolver`
- Utilities: `tech.derbent.api.utils.CAuxillaries`
- Metadata annotation: `tech.derbent.api.annotations.AMetaData`
- Documentation: `docs/development/calculated-fields-pattern.md`
