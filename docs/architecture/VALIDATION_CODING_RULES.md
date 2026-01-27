# Validation Coding Rules (MANDATORY)

**Version**: 1.0  
**Date**: 2026-01-27  
**Status**: MANDATORY - All developers and AI agents MUST follow these rules  

---

## Table of Contents

1. [Overview](#overview)
2. [Rule 1: Use Base Class Validation Methods](#rule-1-use-base-class-validation-methods)
3. [Rule 2: Never Duplicate Validation Logic](#rule-2-never-duplicate-validation-logic)
4. [Rule 3: Validation Method Structure](#rule-3-validation-method-structure)
5. [Rule 4: Exception Handling](#rule-4-exception-handling)
6. [Rule 5: Validation Order](#rule-5-validation-order)
7. [Code Review Checklist](#code-review-checklist)
8. [Examples](#examples)

---

## Overview

This document defines **MANDATORY** coding rules for entity validation in the Derbent project. These rules ensure consistency, reduce code duplication, and improve maintainability.

**Golden Rule**: Never write custom duplicate validation logic when base class methods exist.

---

## Rule 1: Use Base Class Validation Methods

### 1.1 Company-Scoped Entities

**RULE**: For entities extending `CEntityOfCompany`, ALWAYS use `validateUniqueNameInCompany()`.

#### ✅ CORRECT
```java
@Override
protected void validateEntity(final CActivityType entity) {
    super.validateEntity(entity);
    
    // Use base class helper - MANDATORY
    validateUniqueNameInCompany(
        (IActivityTypeRepository) repository, 
        entity, 
        entity.getName(), 
        entity.getCompany()
    );
}
```

#### ❌ INCORRECT
```java
@Override
protected void validateEntity(final CActivityType entity) {
    super.validateEntity(entity);
    
    // WRONG - Duplicate validation logic
    final Optional<CActivityType> existing = 
        ((IActivityTypeRepository) repository).findByNameAndCompany(entity.getName(), entity.getCompany());
    if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
        throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
    }
}
```

### 1.2 Project-Scoped Entities

**RULE**: For entities extending `CEntityOfProject`, ALWAYS use `validateUniqueNameInProject()`.

#### ✅ CORRECT
```java
@Override
protected void validateEntity(final CActivity entity) {
    super.validateEntity(entity);
    
    Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
    Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
    
    // Use base class helper - MANDATORY
    validateUniqueNameInProject(
        (IActivityRepository) repository, 
        entity, 
        entity.getName(), 
        entity.getProject()
    );
    
    // Other entity-specific validations...
}
```

#### ❌ INCORRECT
```java
@Override
protected void validateEntity(final CActivity entity) {
    super.validateEntity(entity);
    
    // WRONG - Duplicate validation logic
    final Optional<CActivity> existing = 
        ((IActivityRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
    if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
        throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
    }
}
```

---

## Rule 2: Never Duplicate Validation Logic

### 2.1 Forbidden Patterns

**NEVER** write these patterns in service classes:

```java
// ❌ FORBIDDEN PATTERN 1 - Manual duplicate check
final Optional<EntityType> existing = repository.findByNameAndProject(...);
if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
    throw new IllegalArgumentException(...);
}

// ❌ FORBIDDEN PATTERN 2 - Manual duplicate check (company)
final Optional<EntityType> existing = repository.findByNameAndCompany(...);
if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
    throw new IllegalArgumentException(...);
}

// ❌ FORBIDDEN PATTERN 3 - Stream-based duplicate check
repository.findAll().stream()
    .filter(e -> e.getName().equals(entity.getName()))
    .filter(e -> !e.getId().equals(entity.getId()))
    .findFirst()
    .ifPresent(e -> { throw new IllegalArgumentException(...); });
```

### 2.2 Allowed Patterns

**ONLY** use base class validation methods:

```java
// ✅ CORRECT - Use base class helper
validateUniqueNameInProject(repository, entity, name, project);

// ✅ CORRECT - Use base class helper
validateUniqueNameInCompany(repository, entity, name, company);
```

---

## Rule 3: Validation Method Structure

### 3.1 Method Signature

```java
@Override
protected void validateEntity(final EntityType entity) {
    // MANDATORY structure
}
```

### 3.2 Validation Order (MANDATORY)

```java
@Override
protected void validateEntity(final EntityType entity) {
    // STEP 1: ALWAYS call super first
    super.validateEntity(entity);
    
    // STEP 2: Required fields (if not in base class)
    Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
    Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
    
    // STEP 3: Unique name validation - USE BASE CLASS HELPER
    validateUniqueNameInProject(
        (IEntityRepository) repository, 
        entity, 
        entity.getName(), 
        entity.getProject()
    );
    
    // STEP 4: Length validations
    if (entity.getDescription() != null && entity.getDescription().length() > 2000) {
        throw new IllegalArgumentException(
            ValidationMessages.formatMaxLength("Description cannot exceed %d characters", 2000)
        );
    }
    
    // STEP 5: Numeric validations
    validateNumericField(entity.getAmount(), "Amount", new BigDecimal("999999.99"));
    
    // STEP 6: Business logic validations
    if (entity.getEndDate() != null && entity.getStartDate() != null 
            && entity.getEndDate().isBefore(entity.getStartDate())) {
        throw new IllegalArgumentException("End date cannot be before start date");
    }
}
```

---

## Rule 4: Exception Handling

### 4.1 Exception Types

**RULE**: Use `IllegalArgumentException` for validation errors (base class methods already do this).

```java
// ✅ CORRECT - Base class throws IllegalArgumentException
validateUniqueNameInProject(repository, entity, name, project);

// ✅ CORRECT - Custom validation also uses IllegalArgumentException
if (entity.getAmount().compareTo(BigDecimal.ZERO) < 0) {
    throw new IllegalArgumentException("Amount cannot be negative");
}
```

### 4.2 Error Messages

**RULE**: Always use `ValidationMessages` constants for consistency.

```java
// ✅ CORRECT - Use constants
Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);

// ✅ CORRECT - Use formatters for dynamic messages
throw new IllegalArgumentException(
    ValidationMessages.formatMaxLength("Field cannot exceed %d characters", 255)
);

// ❌ INCORRECT - Hardcoded strings
throw new IllegalArgumentException("Name is required");
throw new IllegalArgumentException("Name must be unique");
```

---

## Rule 5: Validation Order

### 5.1 Validation Hierarchy

```
1. super.validateEntity()           ← Base class validations
2. Required field checks            ← Null/blank checks
3. Unique name validation           ← USE BASE CLASS HELPER
4. Length validations               ← String/text length
5. Numeric validations              ← Range/precision checks
6. Business logic validations       ← Domain-specific rules
```

### 5.2 Fail-Fast Principle

**RULE**: Validate in order of importance. Stop at first error.

```java
// ✅ CORRECT - Fail fast, check required first
Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
validateUniqueNameInProject(repository, entity, entity.getName(), entity.getProject());

// ❌ INCORRECT - Checking uniqueness before required
validateUniqueNameInProject(repository, entity, entity.getName(), entity.getProject());
Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);  // Too late!
```

---

## Code Review Checklist

### For Reviewers

When reviewing code that adds/modifies entity validation, check:

- [ ] Does `validateEntity()` call `super.validateEntity()` first?
- [ ] Does unique name validation use `validateUniqueNameInProject()` or `validateUniqueNameInCompany()`?
- [ ] Are there NO manual duplicate checks (`findByNameAndProject` + `if present`)?
- [ ] Are all error messages using `ValidationMessages` constants?
- [ ] Is validation order correct (super → required → unique → length → numeric → business)?
- [ ] Are exceptions of type `IllegalArgumentException`?
- [ ] Is the repository cast to the correct interface type?

### For Developers

Before committing validation code:

- [ ] I have used base class validation methods for unique name checks
- [ ] I have NOT written manual duplicate validation logic
- [ ] I have followed the validation order rules
- [ ] I have used `ValidationMessages` constants for all error messages
- [ ] I have tested that validation works for both create and update operations

---

## Examples

### Example 1: Type Entity (Company-Scoped)

```java
@Service
@PreAuthorize("isAuthenticated()")
public class CProductTypeService extends CTypeEntityService<CProductType> 
        implements IEntityRegistrable, IEntityWithView {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CProductTypeService.class);
    
    // Constructor, getters, etc...
    
    @Override
    protected void validateEntity(final CProductType entity) {
        super.validateEntity(entity);
        
        // Unique Name Check - use base class helper (MANDATORY)
        validateUniqueNameInCompany(
            (IProductTypeRepository) repository, 
            entity, 
            entity.getName(), 
            entity.getCompany()
        );
    }
}
```

### Example 2: Project Item (Project-Scoped)

```java
@Service
@PreAuthorize("isAuthenticated()")
public class CActivityService extends CProjectItemService<CActivity> 
        implements IEntityRegistrable, IEntityWithView {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityService.class);
    
    // Constructor, dependencies, etc...
    
    @Override
    protected void validateEntity(final CActivity entity) {
        super.validateEntity(entity);
        
        // 1. Required Fields
        Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
        Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
        
        // 2. Unique Checks - use base class helper (MANDATORY)
        validateUniqueNameInProject(
            (IActivityRepository) repository, 
            entity, 
            entity.getName(), 
            entity.getProject()
        );
        
        // 3. Numeric Checks
        validateNumericField(entity.getActualCost(), "Actual Cost", new BigDecimal("999999.99"));
        validateNumericField(entity.getEstimatedCost(), "Estimated Cost", new BigDecimal("999999.99"));
        
        // 4. Range Checks
        if (entity.getProgressPercentage() != null 
                && (entity.getProgressPercentage() < 0 || entity.getProgressPercentage() > 100)) {
            throw new IllegalArgumentException(
                ValidationMessages.formatRange(ValidationMessages.VALUE_RANGE, 0, 100)
                    .replace("Value", "Progress percentage")
            );
        }
    }
}
```

### Example 3: Abstract Service

```java
public abstract class CAgileEntityService<EntityClass extends CAgileEntity<EntityClass, ?>> 
        extends CProjectItemService<EntityClass> {
    
    protected abstract IProjectItemRespository<EntityClass> getTypedRepository();
    
    @Override
    protected void validateEntity(final EntityClass entity) {
        super.validateEntity(entity);
        
        Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
        Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
        
        // Unique name check - use base class helper (MANDATORY)
        validateUniqueNameInProject(
            getTypedRepository(), 
            entity, 
            entity.getName(), 
            entity.getProject()
        );
        
        // Numeric validations...
        validateNumericField(entity.getActualCost(), "Actual Cost", new BigDecimal("999999.99"));
    }
}
```

### Example 4: Entity with Additional Unique Fields

```java
@Override
protected void validateEntity(final CProduct entity) {
    super.validateEntity(entity);
    
    // 1. Required Fields
    Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
    Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
    Check.notNull(entity.getEntityType(), "Product type is required");
    
    // 2. Unique Name Check - use base class helper (MANDATORY)
    validateUniqueNameInProject(
        (IProductRepository) repository, 
        entity, 
        entity.getName(), 
        entity.getProject()
    );
    
    // 3. Additional Unique Field (Product Code)
    // NOTE: This is entity-specific, not a name check, so manual validation is OK
    if (entity.getProductCode() != null && !entity.getProductCode().isBlank()) {
        final boolean duplicateCode = repository.findAll().stream()
            .anyMatch(p -> p.getProject().equals(entity.getProject())
                && p.getProductCode() != null 
                && p.getProductCode().equalsIgnoreCase(entity.getProductCode()) 
                && !p.getId().equals(entity.getId()));
        
        if (duplicateCode) {
            throw new IllegalArgumentException("Product code must be unique within the project");
        }
    }
}
```

---

## Base Class Method Reference

### CEntityOfCompanyService.validateUniqueNameInCompany()

**Location**: `src/main/java/tech/derbent/api/entityOfCompany/service/CEntityOfCompanyService.java:162-173`

**Signature**:
```java
protected static <T extends CEntityOfCompany<T>> void validateUniqueNameInCompany(
    final IEntityOfCompanyRepository<T> repository,
    final T entity, 
    final String name, 
    final CCompany company)
```

**What it does**:
1. Validates repository, entity, name, and company are not null
2. Trims the name
3. Queries repository for existing entity with same name in company
4. Excludes current entity ID (for updates)
5. Throws `IllegalArgumentException` with `ValidationMessages.DUPLICATE_NAME_IN_COMPANY` if duplicate found

### CEntityOfProjectService.validateUniqueNameInProject()

**Location**: `src/main/java/tech/derbent/api/entityOfProject/service/CEntityOfProjectService.java:257-268`

**Signature**:
```java
protected static <T extends CEntityOfProject<T>> void validateUniqueNameInProject(
    final IEntityOfProjectRepository<T> repository,
    final T entity, 
    final String name, 
    final CProject<?> project)
```

**What it does**:
1. Validates repository, entity, name, and project are not null
2. Trims the name
3. Queries repository for existing entity with same name in project
4. Excludes current entity ID (for updates)
5. Throws `IllegalArgumentException` with `ValidationMessages.DUPLICATE_NAME_IN_PROJECT` if duplicate found

---

## Enforcement

### For AI Agents

**MANDATORY**: When generating or modifying entity validation code:

1. **ALWAYS** use `validateUniqueNameInProject()` or `validateUniqueNameInCompany()`
2. **NEVER** write manual duplicate validation logic
3. **ALWAYS** follow the validation order rules
4. **ALWAYS** use `ValidationMessages` constants

### For Developers

**Code Review**: Any PR containing manual duplicate validation logic MUST be rejected with reference to this document.

**Static Analysis**: Consider adding a custom lint rule to detect forbidden patterns:
- Pattern: `findByNameAndProject.*if.*isPresent.*getId.*equals`
- Pattern: `findByNameAndCompany.*if.*isPresent.*getId.*equals`

---

## Migration Guide

If you encounter old code with manual validation:

### Step 1: Identify the pattern
```java
// OLD CODE
final Optional<CEntity> existing = repository.findByNameAndProject(entity.getName(), entity.getProject());
if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
    throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
}
```

### Step 2: Replace with base class helper
```java
// NEW CODE
validateUniqueNameInProject(
    (IEntityRepository) repository, 
    entity, 
    entity.getName(), 
    entity.getProject()
);
```

### Step 3: Verify compilation
```bash
mvn clean compile -Pagents -DskipTests
```

### Step 4: Test
- Create new entity → Should validate uniqueness
- Update entity with same name → Should allow
- Update entity with existing name → Should reject

---

## Related Documents

- `docs/AGENTS.md` - Section 3.9: Validation Pattern (MANDATORY)
- `docs/BAB_CODING_RULES.md` - BAB-specific validation rules
- `src/main/java/tech/derbent/api/validation/ValidationMessages.java` - Error message constants

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-27 | Initial creation - Consolidated validation rules after refactoring 78 service classes |

---

**END OF DOCUMENT**
