# Calculated Fields and `@PostLoad` Pattern

## Purpose

This document defines the enforced pattern for entity fields that must be initialized or recalculated immediately after JPA loads an entity.

It covers two related use cases:

1. `@AMetaData(autoCalculate = true)` fields (transient calculated values)
2. Manual `@PostLoad` initialization (back-references and relation wiring)

This is the canonical pattern used by `CSprint`.

## Current Architecture (Authoritative)

The shared auto-calculate implementation is in:

- `src/main/java/tech/derbent/api/entity/domain/CEntityDB.java`
- Method: `autoCalculateAnnotatedFieldsOnPostLoad()`

Concrete entities must keep their own `@PostLoad` callback and call the shared helper when needed.

## Pattern A: Auto-Calculated Transient Fields

### 1. Entity field declaration

```java
@Transient
@AMetaData(
    displayName = "Item Count",
    readOnly = true,
    dataProviderBean = "CSprintService",
    dataProviderMethod = "getItemCount",
    dataProviderParamMethod = "this",
    autoCalculate = true
)
private Integer itemCount;
```

Required metadata for auto-calc fields:

- `autoCalculate = true`
- `dataProviderBean = "..."`
- `dataProviderMethod = "..."`
- `dataProviderParamMethod = "this"` for entity-based callbacks

### 2. Entity `@PostLoad` callback

```java
@PostLoad
protected void postLoadEntity() throws Exception {
    autoCalculateAnnotatedFieldsOnPostLoad();

    // Entity-specific post-load logic can remain here
}
```

### 3. Data provider method in service

```java
public static Integer getItemCount(final CSprint sprint) {
    if (sprint == null) {
        return 0;
    }
    return sprint.getItemCount();
}
```

Provider method requirements:

- Must exist on the bean referenced by `dataProviderBean`
- Method name must match `dataProviderMethod`
- Return type must match the field type
- Should return non-null defaults (`0`, `0L`, empty list) where appropriate

## Pattern B: Manual `@PostLoad` Initialization

Use manual post-load logic for non-calculated wiring, for example:

- restoring parent/owner references in one-to-one composition relations
- setting runtime-only links that are not persisted directly

Example pattern:

```java
@PostLoad
protected void ensureRelations() throws Exception {
    autoCalculateAnnotatedFieldsOnPostLoad(); // optional, if auto-calc fields exist

    if (sprintItem != null) {
        sprintItem.setParentItem(this);
    }
    if (agileParentRelation != null) {
        agileParentRelation.setOwnerItem(this);
    }
}
```

## Canonical Example: `CSprint`

`CSprint` demonstrates the full pattern:

- transient fields with `autoCalculate=true` (`itemCount`, `totalStoryPoints`)
- provider callbacks in `CSprintService`
- entity `@PostLoad` calling shared helper
- additional manual post-load relation wiring (`sprintItems` parent item resolution)

See:

- `src/main/java/tech/derbent/plm/sprints/domain/CSprint.java`
- `src/main/java/tech/derbent/plm/sprints/service/CSprintService.java`

## Example: List Auto-Calculation (`CProject_Bab`)

For list fields, the same rules apply:

```java
@Transient
@AMetaData(
    autoCalculate = true,
    dataProviderBean = "CProject_BabService",
    dataProviderMethod = "updatePolicyRules",
    dataProviderParamMethod = "this"
)
private List<CBabPolicyRule> policyRules = new ArrayList<>();
```

Provider callback:

```java
public static List<CBabPolicyRule> updatePolicyRules(final CProject_Bab project) {
    if (project == null || project.getId() == null) {
        return Collections.emptyList();
    }
    project.setPolicyRules(CSpringContext.getBean(CBabPolicyRuleService.class).listByProject(project));
    return project.getPolicyRules();
}
```

And entity callback:

```java
@PostLoad
protected void postLoadEntity() throws Exception {
    autoCalculateAnnotatedFieldsOnPostLoad();
}
```

## Enforcement Checklist

When adding any `autoCalculate=true` field:

- Field is `@Transient`
- Field metadata defines provider bean + method
- `dataProviderParamMethod = "this"` is set for entity-based calculation
- Entity has `@PostLoad` and calls `autoCalculateAnnotatedFieldsOnPostLoad()`
- Provider method exists and returns matching type
- Provider returns safe defaults (avoid nulls)

When adding relation back-reference restoration:

- Keep it inside entity `@PostLoad`
- Do not move relation-specific logic to generic base class
- Keep `@PostLoad` idempotent and fast

## Anti-Patterns (Do Not Use)

- Re-implementing reflection scan logic in each entity
- Omitting provider metadata while setting `autoCalculate=true`
- Using `final` fields for values expected to be assigned during post-load auto-calc
- Doing heavy/expensive database work in `@PostLoad`

## Related Files

- `src/main/java/tech/derbent/api/annotations/AMetaData.java`
- `src/main/java/tech/derbent/api/annotations/CDataProviderResolver.java`
- `src/main/java/tech/derbent/api/entity/domain/CEntityDB.java`
- `src/main/java/tech/derbent/plm/sprints/domain/CSprint.java`
- `src/main/java/tech/derbent/plm/sprints/service/CSprintService.java`
- `src/main/java/tech/derbent/bab/project/domain/CProject_Bab.java`
- `src/main/java/tech/derbent/bab/project/service/CProject_BabService.java`

## Cross-Links

- `docs/architecture/coding-standards.md` (global coding standard and rule entry point)
