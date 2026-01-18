# Entity Type Filter Unification

## Overview

This document describes the unification of entity type filtering patterns across the Derbent application to ensure consistent, human-friendly display names throughout the user interface.

## Problem Statement

The application had **two different patterns** for entity type selection/filtering:

### Pattern 1: CEntityTypeFilter (Filter Toolbars)
- **Location**: `tech.derbent.api.ui.component.filter.CEntityTypeFilter`
- **Usage**: Filter toolbars (e.g., Kanban board filters)
- **Display Names**: ✅ Uses `CEntityRegistry.getEntityTitleSingular()` for human-friendly names
- **Example**: Shows "Activity", "Meeting" (not "CActivity", "CMeeting")
- **Features**: Auto-discovery of types, "All types" option, persistent selection

### Pattern 2: EntityTypeConfig (Entity Selection Dialogs)
- **Location**: `CComponentEntitySelection.EntityTypeConfig`
- **Usage**: Entity selection dialogs (backlog, sprint items, etc.)
- **Display Names**: ❌ Used hardcoded technical class names
- **Example**: Showed "CActivity", "CMeeting" (not user-friendly)
- **Features**: Includes service reference for data loading

## Solution: Unified Approach

### Factory Method for Registry-Based Names

Added `EntityTypeConfig.createWithRegistryName()` that automatically retrieves human-friendly names from the entity registry:

```java
/** Factory method that creates EntityTypeConfig using entity's registered display name.
 * 
 * Uses CEntityRegistry.getEntityTitleSingular() to get human-friendly names like
 * "Activity" instead of "CActivity", "Meeting" instead of "CMeeting".
 * 
 * If entity has ENTITY_TITLE_SINGULAR constant, it will be used.
 * Otherwise, falls back to simple class name without "C" prefix.
 */
public static <E extends CEntityDB<E>> EntityTypeConfig<E> createWithRegistryName(
    final Class<E> entityClass,
    final CAbstractService<E> service
) {
    // Implementation fetches from CEntityRegistry
}
```

### Before and After Comparison

#### BEFORE (Hardcoded Names):
```java
private static List<EntityTypeConfig<?>> createEntityTypes() {
    final List<EntityTypeConfig<?>> entityTypes = new ArrayList<>();
    final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
    final CMeetingService meetingService = CSpringContext.getBean(CMeetingService.class);
    
    // ❌ Hardcoded technical class names
    entityTypes.add(new EntityTypeConfig<>("CActivity", CActivity.class, activityService));
    entityTypes.add(new EntityTypeConfig<>("CMeeting", CMeeting.class, meetingService));
    
    return entityTypes;
}
```

#### AFTER (Registry-Based Names):
```java
private static List<EntityTypeConfig<?>> createEntityTypes() {
    final List<EntityTypeConfig<?>> entityTypes = new ArrayList<>();
    final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
    final CMeetingService meetingService = CSpringContext.getBean(CMeetingService.class);
    
    // ✅ Automatic human-friendly names from registry
    entityTypes.add(EntityTypeConfig.createWithRegistryName(CActivity.class, activityService));
    entityTypes.add(EntityTypeConfig.createWithRegistryName(CMeeting.class, meetingService));
    
    return entityTypes;
}
```

## Implementation Details

### 1. Entity Registry as Single Source of Truth

All entity classes define display names using constants:

```java
public class CActivity extends CProjectItem<CActivity> {
    public static final String ENTITY_TITLE_SINGULAR = "Activity";
    public static final String ENTITY_TITLE_PLURAL = "Activities";
    // ... rest of class
}

public class CMeeting extends CProjectItem<CMeeting> {
    public static final String ENTITY_TITLE_SINGULAR = "Meeting";
    public static final String ENTITY_TITLE_PLURAL = "Meetings";
    // ... rest of class
}
```

`CEntityRegistry` reads these constants during initialization and provides lookup methods:
- `getEntityTitleSingular(Class<?> entityClass)` → "Activity", "Meeting", etc.
- `getEntityTitlePlural(Class<?> entityClass)` → "Activities", "Meetings", etc.

### 2. Files Modified

#### CComponentEntitySelection.java
- Added `createWithRegistryName()` static factory method
- Added `CEntityRegistry` import
- Comprehensive JavaDoc with usage examples
- Old constructor remains for backwards compatibility

#### CComponentBacklog.java
- Updated `createEntityTypes()` method
- Now uses `createWithRegistryName()` factory method
- Displays "Activity" and "Meeting" instead of "CActivity" and "CMeeting"

#### CComponentListSprintItems.java
- Updated `getDialogEntityTypes()` method
- Uses `createWithRegistryName()` factory method
- Removed unused `ITEM_TYPE_ACTIVITY` and `ITEM_TYPE_MEETING` constants

#### CComponentListEntityBase.java
- Deprecated old `createEntityTypeConfig(String, Class, Service)` method
- Added deprecation notice pointing developers to new factory method

### 3. Benefits

| Benefit | Description |
|---------|-------------|
| **Consistency** | All entity type selectors use the same naming convention |
| **Maintainability** | Single source of truth (CEntityRegistry) for entity display names |
| **User-Friendly** | Users see "Activity", "Meeting" instead of technical names |
| **Configuration** | Works for all entities with ENTITY_TITLE_SINGULAR constant |
| **Backwards Compatible** | Old constructor still works, factory method is recommended |
| **Extensible** | New entity types automatically get proper names |

## Usage Guidelines

### For New Entity Type Selectors

When creating a new entity selection dialog or filter:

```java
// ✅ RECOMMENDED: Use factory method
List<EntityTypeConfig<?>> entityTypes = List.of(
    EntityTypeConfig.createWithRegistryName(CActivity.class, activityService),
    EntityTypeConfig.createWithRegistryName(CMeeting.class, meetingService),
    EntityTypeConfig.createWithRegistryName(CRisk.class, riskService)
);

// ❌ AVOID: Hardcoded display names
List<EntityTypeConfig<?>> entityTypes = List.of(
    new EntityTypeConfig<>("CActivity", CActivity.class, activityService),  // Wrong!
    new EntityTypeConfig<>("CMeeting", CMeeting.class, meetingService)      // Wrong!
);
```

### For New Entity Classes

Ensure your entity class defines the title constants:

```java
@Entity
@Table(name = "c_my_entity")
public class CMyEntity extends CEntityDB<CMyEntity> {
    // REQUIRED: Define singular title constant
    public static final String ENTITY_TITLE_SINGULAR = "My Entity";
    
    // RECOMMENDED: Define plural title constant
    public static final String ENTITY_TITLE_PLURAL = "My Entities";
    
    // ... rest of entity class
}
```

If these constants are not defined, the factory method falls back to:
- Remove "C" prefix from class name: `CMyEntity` → `MyEntity`

## Testing Verification

### Manual Testing Steps

1. **Backlog View**
   - Navigate to a project's Backlog view
   - Click "Add from existing" button
   - Verify entity type selector shows "Activity" and "Meeting"
   - NOT "CActivity" and "CMeeting"

2. **Sprint Items View**
   - Open Sprint detail view
   - Navigate to Sprint Items section
   - Click "Add from existing" button
   - Verify entity type selector shows "Activity" and "Meeting"

3. **Kanban Board Filter**
   - Open Kanban board view
   - Check the "Type" filter dropdown
   - Verify it shows "All types", "Activity", "Meeting"
   - Selection should persist across page refreshes

### Automated Testing

The Playwright test suite includes tests for entity type filters:
```bash
./run-playwright-tests.sh comprehensive
```

## Patterns Summary

### When to Use Each Pattern

| Pattern | Use Case | Features |
|---------|----------|----------|
| **CEntityTypeFilter** | Filter toolbars, Kanban boards | Auto-discovery, "All types" option, filtering |
| **EntityTypeConfig** | Entity selection dialogs | Service reference, data loading, selection |

Both patterns now use CEntityRegistry for consistent naming.

### CEntityTypeFilter (Already Consistent)
```java
// Used in filter toolbars - automatically uses registry names
CEntityTypeFilter filter = new CEntityTypeFilter();
filter.setAvailableEntityTypes(sprintItems);  // Auto-discovers types
toolbar.addFilterComponent(filter);
```

### EntityTypeConfig (Now Consistent)
```java
// Used in selection dialogs - now uses registry names via factory method
List<EntityTypeConfig<?>> types = List.of(
    EntityTypeConfig.createWithRegistryName(CActivity.class, activityService),
    EntityTypeConfig.createWithRegistryName(CMeeting.class, meetingService)
);
CDialogEntitySelection dialog = new CDialogEntitySelection("Select Item", types, itemsProvider, false);
```

## Future Enhancements

### Recommended Next Steps

1. **Additional Entity Types**: Add support for other sprintable items:
   - CRisk, CAction, CValidationCase, etc.
   - Each needs ENTITY_TITLE_SINGULAR constant
   - Use createWithRegistryName() in dialogs

2. **Icon Support**: Consider adding icon resolution:
   - Similar to title resolution from entity registry
   - Would unify icon display across entity type selectors

3. **Color Support**: Consider adding color resolution:
   - Entity type filters could show colored indicators
   - Would match entity-specific color schemes

### Migration Guide for Existing Code

If you find code using hardcoded entity type names:

```java
// BEFORE
new EntityTypeConfig<>("CActivity", CActivity.class, activityService)

// AFTER
EntityTypeConfig.createWithRegistryName(CActivity.class, activityService)
```

## Related Files

### Core Implementation
- `tech.derbent.api.ui.component.enhanced.CComponentEntitySelection` - EntityTypeConfig class
- `tech.derbent.api.ui.component.filter.CEntityTypeFilter` - Filter component
- `tech.derbent.api.registry.CEntityRegistry` - Entity metadata registry

### Updated Components
- `tech.derbent.api.ui.component.enhanced.CComponentBacklog` - Backlog entity selection
- `tech.derbent.api.ui.component.enhanced.CComponentListSprintItems` - Sprint item selection
- `tech.derbent.api.ui.component.enhanced.CComponentListEntityBase` - Base class utilities

### Documentation
- `/docs/implementation/ENTITY_TYPE_FILTER_UNIFICATION.md` - This document
- `/docs/development/universal-filter-toolbar-framework.md` - Filter framework guide
- `/docs/architecture/coding-standards.md` - General coding standards

## Conclusion

The entity type filter unification ensures consistent, user-friendly naming across all entity type selectors in the Derbent application. By using `CEntityRegistry` as the single source of truth and providing a convenient factory method, we've eliminated duplicate patterns and hardcoded display names while maintaining backwards compatibility.

All future development should use the `EntityTypeConfig.createWithRegistryName()` factory method when creating entity type configurations for selection dialogs.
