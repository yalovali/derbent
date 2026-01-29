# Simplified Service-Based CopyTo Pattern - MANDATORY Coding Rule

## Date: 2026-01-29
## Status: ACTIVE - All entities must follow this pattern

## Overview

The CopyTo pattern has been **simplified** by removing `copyEntityTo()` from all entity subclasses and eliminating the `copyField` helper method. Now only `CEntityDB` has `copyEntityTo()`, and all field copying happens in services using direct setter/getter calls.

## Why Simplified?

### Previous Pattern Issues
- ❌ Every entity needed to override `copyEntityTo()` (even if just calling super)
- ❌ `copyField` helper added unnecessary abstraction layer
- ❌ `CEntityDB.copyField(source::getX, target::setX)` was longer than direct calls
- ❌ More code to maintain across 40+ entities

### Benefits of Simplified Pattern
- ✅ **Less Code**: Removed copyEntityTo from 21+ entity classes (~300 lines)
- ✅ **More Direct**: `target.setX(source.getX())` is clearer
- ✅ **No Helper Method**: No need for copyField abstraction
- ✅ **Self-Documenting**: Direct setter/getter calls are obvious
- ✅ **Single Location**: Only CEntityDB has copyEntityTo
- ✅ **Easier Maintenance**: One place to change delegation logic

## Simplified Architecture

```
┌──────────────────────────────────────────────────┐
│  Entity Classes                                   │
│  NO copyEntityTo override needed!                │
│  (Only CEntityDB has it)                         │
└──────────────────────┬───────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────┐
│  CEntityDB.copyEntityTo()                        │
│  1. Copy base field: target.setActive(...)       │
│  2. Copy interfaces: comments, attachments, etc. │
│  3. Delegate to service.copyEntityFieldsTo()     │
└──────────────────────┬───────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────┐
│  Service.copyEntityFieldsTo()                    │
│  Direct setter/getter calls:                     │
│  targetEntity.setField(sourceEntity.getField())  │
└──────────────────────────────────────────────────┘
```

## Implementation Guide

### Step 1: Entity Class - NO Override!

**Entity classes do NOT override copyEntityTo**. Just define your fields, getters, and setters.

```java
@Entity
@Table(name = "cactivity")
public class CActivity extends CProjectItem<CActivity> {
    
    @Column(nullable = false)
    private String notes;
    
    @Column
    private BigDecimal estimatedCost;
    
    // Getters and setters
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal cost) { this.estimatedCost = cost; }
    
    // NO copyEntityTo method needed!
}
```

### Step 2: Service Class - Direct Setter/Getter

**Service implements all field copying using direct setter/getter calls**.

```java
@Service
public class CActivityService extends CProjectItemService<CActivity> {
    
    @Override
    public void copyEntityFieldsTo(final CActivity source, 
                                   final CEntityDB<?> target,
                                   final CCloneOptions options) {
        // STEP 1: Call parent first
        super.copyEntityFieldsTo(source, target, options);
        
        // STEP 2: Type-check target
        if (!(target instanceof CActivity)) {
            return;
        }
        final CActivity targetActivity = (CActivity) target;
        
        // STEP 3: Copy fields using DIRECT setter/getter
        targetActivity.setNotes(source.getNotes());
        targetActivity.setEstimatedCost(source.getEstimatedCost());
        targetActivity.setAcceptanceCriteria(source.getAcceptanceCriteria());
        targetActivity.setResults(source.getResults());
        
        // STEP 4: Copy numeric fields
        targetActivity.setActualCost(source.getActualCost());
        targetActivity.setActualHours(source.getActualHours());
        targetActivity.setEstimatedHours(source.getEstimatedHours());
        
        // STEP 5: Copy references
        targetActivity.setPriority(source.getPriority());
        targetActivity.setEntityType(source.getEntityType());
        
        // STEP 6: Conditional - dates
        if (!options.isResetDates()) {
            targetActivity.setDueDate(source.getDueDate());
            targetActivity.setStartDate(source.getStartDate());
            targetActivity.setCompletionDate(source.getCompletionDate());
        }
        
        // STEP 7: Conditional - relations
        if (options.includesRelations()) {
            targetActivity.setRelatedEntity(source.getRelatedEntity());
        }
        
        LOGGER.debug("Copied activity '{}' with options: {}", source.getName(), options);
    }
}
```

## Pattern Comparison

### Before (Old Pattern)
```java
// Entity class - needed override
@Override
protected void copyEntityTo(final CEntityDB<?> target, 
                           final CAbstractService serviceTarget,
                           final CCloneOptions options) {
    super.copyEntityTo(target, serviceTarget, options);
    // NOTE: Delegated to service
}

// Service class - used copyField helper
CEntityDB.copyField(source::getNotes, targetEntity::setNotes);
CEntityDB.copyField(source::getEstimatedCost, targetEntity::setEstimatedCost);
```

### After (Simplified Pattern)
```java
// Entity class - NO override needed!
// Just your fields, getters, setters

// Service class - direct calls
targetEntity.setNotes(source.getNotes());
targetEntity.setEstimatedCost(source.getEstimatedCost());
```

## Field Handling Patterns

### Basic Fields
```java
// Simple fields - direct setter/getter
targetEntity.setName(source.getName());
targetEntity.setDescription(source.getDescription());
targetEntity.setActive(source.getActive());
```

### Conditional Fields
```java
// Dates - only if not resetting
if (!options.isResetDates()) {
    targetEntity.setDueDate(source.getDueDate());
    targetEntity.setStartDate(source.getStartDate());
}

// Relations - only if including
if (options.includesRelations()) {
    targetEntity.setParentEntity(source.getParentEntity());
}

// Status - only if cloning
if (options.isCloneStatus()) {
    targetEntity.setStatus(source.getStatus());
}
```

### Collections
```java
// Collections - create new instance
if (source.getAttendees() != null) {
    targetEntity.setAttendees(new HashSet<>(source.getAttendees()));
}

if (source.getTags() != null) {
    targetEntity.setTags(new ArrayList<>(source.getTags()));
}
```

### Unique Fields
```java
// Make unique to avoid constraint violations
if (source.getEmail() != null) {
    targetEntity.setEmail(source.getEmail().replace("@", "+copy@"));
}

if (source.getLogin() != null) {
    targetEntity.setLogin(source.getLogin() + "_copy");
}
```

## Copy Rules

### ✅ ALWAYS Copy
- Basic data fields (name, description, notes)
- Numeric fields (amounts, quantities)
- Boolean flags (active, enabled)
- Enum values (type, category, priority)
- Entity references (type, priority)

### ⚠️ CONDITIONAL Copy
- **Dates**: Only if `!options.isResetDates()`
- **Relations**: Only if `options.includesRelations()`
- **Status**: Only if `options.isCloneStatus()`
- **Workflow**: Only if `options.isCloneWorkflow()`

### ❌ NEVER Copy
- **ID fields** (auto-generated by database)
- **Passwords** (security sensitive)
- **Tokens** (security sensitive)
- **Session data** (temporary state)
- **Audit fields** (createdBy, lastModifiedBy, createdDate, lastModifiedDate)

### 🔧 MAKE UNIQUE
- **Email addresses** (replace @ with +copy@)
- **Usernames** (add _copy suffix)
- **SKUs** (add _copy suffix)
- **Unique codes** (add suffix/prefix)

## Base Service Hierarchy

### CAbstractService
```java
public void copyEntityFieldsTo(EntityClass source, CEntityDB<?> target, CCloneOptions options) {
    // Default: do nothing - concrete services override
}
```

### CEntityNamedService
```java
public void copyEntityFieldsTo(EntityClass source, CEntityDB<?> target, CCloneOptions options) {
    super.copyEntityFieldsTo(source, target, options);
    
    if (!(target instanceof CEntityNamed)) return;
    CEntityNamed<?> targetNamed = (CEntityNamed<?>) target;
    
    targetNamed.setName(source.getName());
    targetNamed.setDescription(source.getDescription());
    
    if (!options.isResetDates()) {
        targetNamed.setCreatedDate(source.getCreatedDate());
        targetNamed.setLastModifiedDate(source.getLastModifiedDate());
    }
}
```

### CEntityOfCompanyService
```java
public void copyEntityFieldsTo(EntityClass source, CEntityDB<?> target, CCloneOptions options) {
    super.copyEntityFieldsTo(source, target, options);
    
    if (!(target instanceof CEntityOfCompany)) return;
    CEntityOfCompany<?> targetCompanyEntity = (CEntityOfCompany<?>) target;
    
    targetCompanyEntity.setCompany(source.getCompany());
}
```

### CProjectItemService
```java
public void copyEntityFieldsTo(EntityClass source, CEntityDB<?> target, CCloneOptions options) {
    super.copyEntityFieldsTo(source, target, options);
    
    if (!(target instanceof CProjectItem)) return;
    CProjectItem<?> targetProjectItem = (CProjectItem<?>) target;
    
    targetProjectItem.setProject(source.getProject());
    targetProjectItem.setCreatedBy(source.getCreatedBy());
    
    if (options.includesRelations()) {
        targetProjectItem.setParentId(source.getParentId());
        targetProjectItem.setParentType(source.getParentType());
    }
}
```

## Migration Checklist

### Migrating Existing Entity

If your entity currently has `copyEntityTo()`:

1. ✅ **Remove copyEntityTo method** from entity class
2. ✅ **Add copyEntityFieldsTo** to service class
3. ✅ **Replace all CEntityDB.copyField** with direct setter/getter
4. ✅ **Replace all CEntityDB.copyCollection** with new HashSet/ArrayList
5. ✅ **Test compilation**
6. ✅ **Test copy functionality** via UI or unit tests

### Creating New Entity

For new entities:

1. ✅ **DO NOT add copyEntityTo** to entity class
2. ✅ **Add copyEntityFieldsTo** to service class
3. ✅ **Use direct setter/getter** for all fields
4. ✅ **Follow conditional patterns** for dates, relations, etc.

## Testing

### Unit Test Example
```java
@Test
void testCopyEntity() {
    // Given
    CActivity source = new CActivity("Test Activity", project);
    source.setNotes("Original notes");
    source.setEstimatedCost(BigDecimal.valueOf(1000));
    activityService.save(source);
    
    // When
    CActivity target = new CActivity("Copy", project);
    CCloneOptions options = new CCloneOptions.Builder().build();
    activityService.copyEntityFieldsTo(source, target, options);
    
    // Then
    assertEquals(source.getNotes(), target.getNotes());
    assertEquals(source.getEstimatedCost(), target.getEstimatedCost());
    assertNotEquals(source.getId(), target.getId());
}
```

## Benefits Summary

| Aspect | Before | After |
|--------|--------|-------|
| **Entity Class** | Must override copyEntityTo | No override needed |
| **Lines of Code** | ~15 lines per entity | 0 lines per entity |
| **Service Class** | Uses copyField helper | Direct setter/getter |
| **Readability** | `copyField(source::getX, target::setX)` | `target.setX(source.getX())` |
| **Total Lines** | ~600 lines across all entities | ~300 lines (50% reduction) |
| **Maintenance** | 40+ places to update | Single CEntityDB.copyEntityTo |

## See Also

- `.github/copilot-instructions.md` - Section 4.3 for quick reference
- `docs/implementation/SERVICE_BASED_COPY_IMPLEMENTATION_STATUS.md` - Implementation status
- `src/main/java/tech/derbent/api/entity/domain/CEntityDB.java` - Base copyEntityTo implementation
- `src/main/java/tech/derbent/plm/activities/service/CActivityService.java` - Complete example

---

**Last Updated**: 2026-01-29  
**Pattern Status**: ✅ Active and Simplified  
**Entities Using Pattern**: 8/40 (base classes + CActivity, CMeeting, CUser, CDecision)
