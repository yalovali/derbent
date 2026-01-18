# CopyTo Pattern Audit and Implementation Plan

## Audit Summary

### Entities WITH Proper Implementation ✅
1. **CEntityDB** - Base implementation (active field + interfaces)
2. **CEntityNamed** - Name, description, dates
3. **CActivity** - Full implementation with all fields
4. **CMeeting** - Full implementation with collections
5. **CUser** - Full implementation with unique field handling

### Entities MISSING Implementation ❌

#### Project Items (Extend CProjectItem)
1. **CProvider** - 0 implementations found
2. **CSprint** - 0 implementations found  
3. **CProjectIncome** - 0 implementations found
4. **CProduct** - 0 implementations found
5. **CProductVersion** - 0 implementations found
6. **COrder** - 0 implementations found
7. **CInvoice** - 0 implementations found
8. **CRiskLevel** - 0 implementations found
9. **CValidationCase** - 0 implementations found
10. **CProjectExpense** - 0 implementations found
11. **CCustomer** - 0 implementations found
12. **CMilestone** - 0 implementations found
13. **CDeliverable** - 0 implementations found
14. **CProjectComponentVersion** - 0 implementations found
15. **CProjectComponent** - 0 implementations found
16. **CBudget** - 0 implementations found
17. **CAsset** - 0 implementations found
18. **CRisk** - 0 implementations found

#### Company Entities (Extend CEntityOfCompany)
19. **CAttachment** - 0 implementations found
20. **CComment** - 0 implementations found
21. **CTeam** - 0 implementations found

#### Project Entities (Extend CEntityOfProject)
22. **CValidationSuite** - 0 implementations found
23. **CValidationSession** - 0 implementations found
24. **CValidationExecution** - 0 implementations found

#### Other Entities
25. **CProjectItemStatus** - Status entity
26. **CWorkflowBase** - Workflow entity
27. **CWorkflowStatusRelation** - Relation entity
28. **CSprintItem** - Sprint relation entity

## Implementation Templates

### Template 1: Simple Entity (Basic Fields Only)

```java
/**
 * Example: CRiskLevel, CProjectItemStatus
 */
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof YourEntity) {
        final YourEntity targetEntity = (YourEntity) target;
        
        // Copy all basic fields
        copyField(this::getField1, targetEntity::setField1);
        copyField(this::getField2, targetEntity::setField2);
        copyField(this::getColor, targetEntity::setColor);
        copyField(this::getIcon, targetEntity::setIcon);
        
        LOGGER.debug("Successfully copied {} with options: {}", getName(), options);
    }
}
```

### Template 2: Financial Entity (With Amounts)

```java
/**
 * Example: CProjectIncome, CProjectExpense, CBudget, CInvoice
 */
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof YourFinancialEntity) {
        final YourFinancialEntity targetEntity = (YourFinancialEntity) target;
        
        // Copy basic fields
        copyField(this::getNotes, targetEntity::setNotes);
        copyField(this::getCategory, targetEntity::setCategory);
        copyField(this::getEntityType, targetEntity::setEntityType);
        
        // Copy amounts and currency
        copyField(this::getAmount, targetEntity::setAmount);
        copyField(this::getCurrency, targetEntity::setCurrency);
        copyField(this::getTaxRate, targetEntity::setTaxRate);
        copyField(this::getTaxAmount, targetEntity::setTaxAmount);
        copyField(this::getTotalAmount, targetEntity::setTotalAmount);
        
        // Conditional: dates
        if (!options.isResetDates()) {
            copyField(this::getTransactionDate, targetEntity::setTransactionDate);
            copyField(this::getDueDate, targetEntity::setDueDate);
            copyField(this::getPaidDate, targetEntity::setPaidDate);
        }
        
        // Conditional: relations
        if (options.includesRelations()) {
            copyField(this::getRelatedOrder, targetEntity::setRelatedOrder);
            copyField(this::getSupplier, targetEntity::setSupplier);
        }
        
        LOGGER.debug("Successfully copied {} with options: {}", getName(), options);
    }
}
```

### Template 3: Entity with Collections

```java
/**
 * Example: CSprint, CTeam, CValidationSuite
 */
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof YourEntity) {
        final YourEntity targetEntity = (YourEntity) target;
        
        // Copy basic fields
        copyField(this::getGoal, targetEntity::setGoal);
        copyField(this::getNotes, targetEntity::setNotes);
        
        // Conditional: dates
        if (!options.isResetDates()) {
            copyField(this::getStartDate, targetEntity::setStartDate);
            copyField(this::getEndDate, targetEntity::setEndDate);
        }
        
        // Conditional: collections
        if (options.includesRelations()) {
            copyCollection(this::getMembers, 
                (col) -> targetEntity.members = (Set<CUser>) col, 
                true);
            copyCollection(this::getItems, 
                (col) -> targetEntity.items = (Set<Item>) col, 
                true);
        }
        
        LOGGER.debug("Successfully copied {} with options: {}", getName(), options);
    }
}
```

### Template 4: Product/Version Entity

```java
/**
 * Example: CProduct, CProductVersion, CProjectComponent, CProjectComponentVersion
 */
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof YourProductEntity) {
        final YourProductEntity targetEntity = (YourProductEntity) target;
        
        // Copy basic fields
        copyField(this::getSpecifications, targetEntity::setSpecifications);
        copyField(this::getFeatures, targetEntity::setFeatures);
        copyField(this::getEntityType, targetEntity::setEntityType);
        
        // Copy version info
        copyField(this::getVersionNumber, targetEntity::setVersionNumber);
        copyField(this::getVersionNotes, targetEntity::setVersionNotes);
        
        // Conditional: dates
        if (!options.isResetDates()) {
            copyField(this::getReleaseDate, targetEntity::setReleaseDate);
            copyField(this::getEndOfLifeDate, targetEntity::setEndOfLifeDate);
        }
        
        // Conditional: relations
        if (options.includesRelations()) {
            copyField(this::getParentProduct, targetEntity::setParentProduct);
        }
        
        LOGGER.debug("Successfully copied {} with options: {}", getName(), options);
    }
}
```

### Template 5: Validation/Testing Entity

```java
/**
 * Example: CValidationCase, CValidationSuite, CValidationSession, CValidationExecution
 */
@Override
protected void copyEntityTo(final CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof YourValidationEntity) {
        final YourValidationEntity targetEntity = (YourValidationEntity) target;
        
        // Copy test fields
        copyField(this::getTestSteps, targetEntity::setTestSteps);
        copyField(this::getExpectedResult, targetEntity::setExpectedResult);
        copyField(this::getPreconditions, targetEntity::setPreconditions);
        copyField(this::getTestData, targetEntity::setTestData);
        copyField(this::getEntityType, targetEntity::setEntityType);
        
        // Don't copy execution results - these are run-specific
        // Don't copy pass/fail status - these are test outcomes
        
        // Conditional: relations
        if (options.includesRelations()) {
            copyField(this::getTestSuite, targetEntity::setTestSuite);
            copyCollection(this::getTestCases, 
                (col) -> targetEntity.testCases = (Set<CValidationCase>) col, 
                true);
        }
        
        LOGGER.debug("Successfully copied {} with options: {}", getName(), options);
    }
}
```

## Priority Implementation Order

### High Priority (User-Facing Entities)
1. **CSprint** - Frequently copied for sprint planning
2. **CRisk** - Risk management
3. **CMilestone** - Project tracking  
4. **CCustomer** - Business entities
5. **CProvider** - Business entities
6. **CTeam** - Organizational

### Medium Priority (Supporting Entities)
7. **CProduct** / **CProductVersion** - Product management
8. **CBudget** / **CProjectExpense** / **CProjectIncome** - Financial
9. **CInvoice** / **COrder** - Financial transactions
10. **CValidationCase** / **CValidationSuite** - Testing
11. **CProjectComponent** / **CProjectComponentVersion** - Components
12. **CDeliverable** - Project outputs

### Low Priority (Utility Entities)
13. **CAttachment** - Usually auto-copied via interface
14. **CComment** - Usually auto-copied via interface
15. **CRiskLevel** - Configuration entity
16. **CAsset** - Resource management
17. **CValidationSession** / **CValidationExecution** - Test execution

### Skip/Special Handling
18. **CSprintItem** - Relation table, special handling needed
19. **CWorkflowStatusRelation** - Relation table
20. **CProjectItemStatus** - Configuration entity
21. **CWorkflowBase** - Configuration entity

## Implementation Checklist per Entity

For each entity to be implemented:

### Analysis Phase
- [ ] Identify all fields (use entity class)
- [ ] Identify unique constraints
- [ ] Identify required fields (@NotNull, @NotBlank)
- [ ] Identify date fields
- [ ] Identify relations (ManyToOne, OneToMany, ManyToMany)
- [ ] Identify collections
- [ ] Identify sensitive fields (passwords, etc.)
- [ ] Check parent class for inherited copyEntityTo

### Implementation Phase
- [ ] Add @Override copyEntityTo method
- [ ] Call super.copyEntityTo(target, options) first
- [ ] Add type check: if (target instanceof YourEntity)
- [ ] Copy all basic fields using copyField()
- [ ] Handle unique fields (add suffix/prefix)
- [ ] Handle conditional dates with !options.isResetDates()
- [ ] Handle conditional relations with options.includesRelations()
- [ ] Handle collections with copyCollection(getter, setter, true)
- [ ] Add debug logging
- [ ] Add JavaDoc comment

### Testing Phase
- [ ] Test copy to same type
- [ ] Test copy to different type (if applicable)
- [ ] Test unique field handling
- [ ] Test with all copy options
- [ ] Test in CopyTo dialog UI
- [ ] Verify no validation errors
- [ ] Verify no constraint violations

## Quick Implementation Guide

### Step 1: Find Your Entity Class
```bash
cd src/main/java/tech/derbent/app
grep -r "class YourEntity" .
```

### Step 2: Check Current Implementation
```bash
grep -A 20 "protected void copyEntityTo" your_entity_file.java
```

### Step 3: List All Fields
```bash
grep "@Column\|@ManyToOne\|@OneToMany\|private.*;" your_entity_file.java | head -30
```

### Step 4: Pick Template
- Simple entity → Template 1
- Financial entity → Template 2  
- Entity with collections → Template 3
- Product/version → Template 4
- Testing entity → Template 5

### Step 5: Implement
Copy template, adjust field names, add to entity class after constructor section.

### Step 6: Test
```java
final YourEntity source = createTestEntity();
final CCloneOptions options = new CCloneOptions.Builder().build();
final YourEntity copy = source.copyTo(YourEntity.class, options);
// Verify all fields
```

### Step 7: Compile
```bash
./mvnw compile -DskipTests
```

## Next Steps

1. **Add to coding standards** ✅ - Created COPY_TO_PATTERN_CODING_RULE.md
2. **Update AGENTS.md** - Add copyEntityTo requirement
3. **Update NEW_ENTITY_COMPLETE_CHECKLIST.md** - Add copyEntityTo step
4. **Implement high-priority entities** - CSprint, CRisk, CMilestone, etc.
5. **Create test suite** - Automated tests for all copyEntityTo implementations
6. **Document in Copilot guidelines** - Add to entity scaffolding template

## Common Field Patterns

### Financial Fields
```java
copyField(this::getAmount, targetEntity::setAmount);
copyField(this::getCurrency, targetEntity::setCurrency);
copyField(this::getTaxRate, targetEntity::setTaxRate);
```

### Contact Fields
```java
copyField(this::getEmail, targetEntity::setEmail);
copyField(this::getPhone, targetEntity::setPhone);
copyField(this::getAddress, targetEntity::setAddress);
```

### Location Fields
```java
copyField(this::getAddress, targetEntity::setAddress);
copyField(this::getCity, targetEntity::setCity);
copyField(this::getCountry, targetEntity::setCountry);
```

### Measurement Fields
```java
copyField(this::getWidth, targetEntity::setWidth);
copyField(this::getHeight, targetEntity::setHeight);
copyField(this::getWeight, targetEntity::setWeight);
```

### Priority/Status Fields
```java
copyField(this::getPriority, targetEntity::setPriority);
copyField(this::getSeverity, targetEntity::setSeverity);
copyField(this::getImportance, targetEntity::setImportance);
```

## Conclusion

All entities must implement copyEntityTo() to:
- Enable cross-type copying
- Support the CopyTo dialog
- Maintain data integrity
- Follow architectural standards

This is now a **mandatory coding rule** documented in:
- `docs/architecture/COPY_TO_PATTERN_CODING_RULE.md`

All new entities MUST include this implementation from day one.
