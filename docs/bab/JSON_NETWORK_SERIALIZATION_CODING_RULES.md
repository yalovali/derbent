# BAB JSON Network Serialization Coding Rules

**Version**: 1.0  
**Date**: 2026-02-21  
**Status**: MANDATORY for BAB network JSON export (`JSONSENARIO_BABCONFIGURATION`, `JSONSENARIO_BABPOLICY`)

---

## Purpose

Define the enforced coding pattern for BAB JSON serialization using:

- `IJsonNetworkSerializable`
- `CJsonSerializer`

This ensures field exclusion rules are class-owned, profile-aware, and discoverable by agents.

---

## Mandatory Pattern

1. Entities participating in BAB network serialization must implement `IJsonNetworkSerializable` (directly or via inheritance).
2. Exclusion maps must be declared **per class** using:
   - `createExcludedFieldMap_BabConfiguration()`
   - `createExcludedFieldMap_BabPolicy()`
3. `getExcludedFieldMapForScenario(...)` must use interface helpers:
   - `getScenarioExcludedFieldMap(...)`
   - `mergeExcludedFieldMaps(...)`
4. Subclasses must merge `super.getExcludedFieldMapForScenario(scenario)` so base exclusions are preserved.
5. `CJsonSerializer` is responsible for reflection traversal and global framework filtering only; class-specific rules stay in classes.

---

## Required Helper Usage

```java
@Override
public Map<String, Set<String>> getExcludedFieldMapForScenario(final EJsonScenario scenario) {
    return mergeExcludedFieldMaps(
            super.getExcludedFieldMapForScenario(scenario),
            getScenarioExcludedFieldMap(scenario, EXCLUDED_FIELDS_BAB_CONFIGURATION, EXCLUDED_FIELDS_BAB_POLICY));
}
```

For policy-only classes:

```java
@Override
public Map<String, Set<String>> getExcludedFieldMapForScenario(final EJsonScenario scenario) {
    return mergeExcludedFieldMaps(
            super.getExcludedFieldMapForScenario(scenario),
            getScenarioExcludedFieldMap(scenario, Map.of(), EXCLUDED_FIELDS_BAB_POLICY));
}
```

---

## Current Base Rules

- `CEntityDB` excludes inherited `CEntity.LOGGER` for both scenarios.
- `CEntityNamed` excludes all `CEntityNamed` fields for both scenarios.
- `CEntityOfProject` excludes all `CEntityOfProject` fields for both scenarios.
- `CProject` defines project-level exclusions per scenario.
- `CProject_Bab` and CBabPolicy-related classes define their own scenario maps and merge with super.

---

## Files to Follow as Reference

- `src/main/java/tech/derbent/bab/policybase/domain/IJsonNetworkSerializable.java`
- `src/main/java/tech/derbent/bab/utils/CJsonSerializer.java`
- `src/main/java/tech/derbent/api/entity/domain/CEntityDB.java`
- `src/main/java/tech/derbent/api/entity/domain/CEntityNamed.java`
- `src/main/java/tech/derbent/api/entityOfProject/domain/CEntityOfProject.java`
- `src/main/java/tech/derbent/api/projects/domain/CProject.java`
- `src/main/java/tech/derbent/bab/project/domain/CProject_Bab.java`

---

## Agent Rule

When AI agents add or change BAB JSON-serializable entities, they must:

1. Add/adjust per-class exclusion maps in that class.
2. Use helper-based scenario/merge methods from `IJsonNetworkSerializable`.
3. Avoid adding entity-specific exclusions back into central serializer maps unless it is framework-wide behavior.
