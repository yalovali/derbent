# CopyTo Pattern Implementation Status

## Date: 2026-01-18
## Last Updated: 09:35 UTC

## Summary

**Total Entities**: 35  
**Implemented**: 6 (17%)  
**Remaining**: 29 (83%)  

## Implementation Status by Entity

### ✅ COMPLETED (6)

1. **CEntityDB** - Base class ✅
2. **CEntityNamed** - Base class ✅
3. **CActivity** - Full implementation ✅
4. **CMeeting** - Full implementation ✅
5. **CUser** - With unique field handling ✅
6. **CDecision** - JUST COMPLETED ✅

### 🔄 IN PROGRESS (0)

None currently

### ⏳ PENDING HIGH PRIORITY (10)

These are user-facing entities that need immediate implementation:

7. **CRisk** - Risk management
8. **CIssue** - Issue tracking
9. **CTicket** - Ticket system
10. **CMilestone** - Project tracking
11. **CSprint** - Sprint planning
12. **CProvider** - Business entity
13. **CCustomer** - Business entity
14. **CTeam** - Organizational
15. **CDeliverable** - Project outputs
16. **CAsset** - Resource management

### ⏳ PENDING MEDIUM PRIORITY (10)

Supporting entities:

17. **CBudget** - Financial planning
18. **CProjectExpense** - Financial tracking
19. **CProjectIncome** - Financial tracking
20. **CInvoice** - Financial transactions
21. **COrder** - Order management
22. **CProduct** - Product management
23. **CProductVersion** - Version management
24. **CProjectComponent** - Component management
25. **CProjectComponentVersion** - Version management
26. **CValidationCase** - Testing entity

### ⏳ PENDING LOW PRIORITY (9)

Configuration and utility entities:

27. **CValidationSuite** - Test suite
28. **CValidationSession** - Test execution
29. **CValidationExecution** - Test results
30. **CRiskLevel** - Configuration
31. **CAttachment** - Auto-handled via interface
32. **CComment** - Auto-handled via interface
33. **CGanntViewEntity** - View entity
34. **CKanbanLine** - Kanban management
35. **CKanbanColumn** - Kanban configuration

## Implementation Templates by Entity Type

### Template 1: Simple Work Item (CRisk, CIssue, CTicket)

```java
@Override
protected void copyEntityTo(final tech.derbent.api.entity.domain.CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof C{Entity}) {
        final C{Entity} targetEntity = (C{Entity}) target;
        
        // Copy basic fields
        copyField(this::getEntityType, targetEntity::setEntityType);
        copyField(this::getPriority, targetEntity::setPriority);
        copyField(this::getSeverity, targetEntity::setSeverity);
        copyField(this::getNotes, targetEntity::setNotes);
        
        // Conditional: dates
        if (!options.isResetDates()) {
            copyField(this::getDueDate, targetEntity::setDueDate);
            copyField(this::getStartDate, targetEntity::setStartDate);
            copyField(this::getClosedDate, targetEntity::setClosedDate);
        }
        
        // Conditional: relations
        if (options.includesRelations()) {
            copyField(this::getRelatedActivity, targetEntity::setRelatedActivity);
        }
        
        LOGGER.debug("Successfully copied {} '{}' with options: {}", 
            getClass().getSimpleName(), getName(), options);
    }
}
```

### Template 2: Financial Entity (CBudget, CProjectExpense, CProjectIncome, CInvoice)

```java
@Override
protected void copyEntityTo(final tech.derbent.api.entity.domain.CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof C{Entity}) {
        final C{Entity} targetEntity = (C{Entity}) target;
        
        // Copy basic fields
        copyField(this::getEntityType, targetEntity::setEntityType);
        copyField(this::getCategory, targetEntity::setCategory);
        copyField(this::getNotes, targetEntity::setNotes);
        
        // Copy financial fields
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
        
        LOGGER.debug("Successfully copied {} '{}' with options: {}", 
            getClass().getSimpleName(), getName(), options);
    }
}
```

### Template 3: Sprint/Project Item (CSprint, CMilestone, CDeliverable)

```java
@Override
protected void copyEntityTo(final tech.derbent.api.entity.domain.CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof C{Entity}) {
        final C{Entity} targetEntity = (C{Entity}) target;
        
        // Copy basic fields
        copyField(this::getEntityType, targetEntity::setEntityType);
        copyField(this::getGoal, targetEntity::setGoal);
        copyField(this::getNotes, targetEntity::setNotes);
        
        // Conditional: dates
        if (!options.isResetDates()) {
            copyField(this::getStartDate, targetEntity::setStartDate);
            copyField(this::getEndDate, targetEntity::setEndDate);
            copyField(this::getCompletionDate, targetEntity::setCompletionDate);
        }
        
        // Conditional: collections (e.g., sprint items, team members)
        if (options.includesRelations()) {
            // NOTE: Sprint items should NOT be copied - they reference other entities
            // Collections are handled by parent class via interfaces
        }
        
        LOGGER.debug("Successfully copied {} '{}' with options: {}", 
            getClass().getSimpleName(), getName(), options);
    }
}
```

### Template 4: Business Entity (CProvider, CCustomer, CTeam)

```java
@Override
protected void copyEntityTo(final tech.derbent.api.entity.domain.CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof C{Entity}) {
        final C{Entity} targetEntity = (C{Entity}) target;
        
        // Copy basic fields
        copyField(this::getEntityType, targetEntity::setEntityType);
        copyField(this::getNotes, targetEntity::setNotes);
        
        // Copy contact fields (make unique if necessary)
        if (this.getEmail() != null && !this.getEmail().isBlank()) {
            targetEntity.setEmail(this.getEmail().replace("@", "+copy@"));
        }
        copyField(this::getPhone, targetEntity::setPhone);
        copyField(this::getAddress, targetEntity::setAddress);
        copyField(this::getCity, targetEntity::setCity);
        copyField(this::getCountry, targetEntity::setCountry);
        
        // Conditional: collections (team members, contacts)
        if (options.includesRelations()) {
            copyCollection(this::getMembers, 
                (col) -> targetEntity.members = (Set<CUser>) col, 
                true);
        }
        
        LOGGER.debug("Successfully copied {} '{}' with options: {}", 
            getClass().getSimpleName(), getName(), options);
    }
}
```

### Template 5: Product/Version Entity (CProduct, CProductVersion, CProjectComponent)

```java
@Override
protected void copyEntityTo(final tech.derbent.api.entity.domain.CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof C{Entity}) {
        final C{Entity} targetEntity = (C{Entity}) target;
        
        // Copy basic fields
        copyField(this::getEntityType, targetEntity::setEntityType);
        copyField(this::getSpecifications, targetEntity::setSpecifications);
        copyField(this::getFeatures, targetEntity::setFeatures);
        copyField(this::getNotes, targetEntity::setNotes);
        
        // Copy version info (if applicable)
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
        
        LOGGER.debug("Successfully copied {} '{}' with options: {}", 
            getClass().getSimpleName(), getName(), options);
    }
}
```

### Template 6: Test/Validation Entity (CValidationCase, CValidationSuite, CValidationSession)

```java
@Override
protected void copyEntityTo(final tech.derbent.api.entity.domain.CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof C{Entity}) {
        final C{Entity} targetEntity = (C{Entity}) target;
        
        // Copy test definition fields
        copyField(this::getEntityType, targetEntity::setEntityType);
        copyField(this::getTestSteps, targetEntity::setTestSteps);
        copyField(this::getExpectedResult, targetEntity::setExpectedResult);
        copyField(this::getPreconditions, targetEntity::setPreconditions);
        copyField(this::getTestData, targetEntity::setTestData);
        
        // DON'T copy execution results - these are run-specific
        // DON'T copy pass/fail status - these are test outcomes
        
        // Conditional: relations
        if (options.includesRelations()) {
            copyField(this::getTestSuite, targetEntity::setTestSuite);
            copyCollection(this::getTestCases, 
                (col) -> targetEntity.testCases = (Set<CValidationCase>) col, 
                true);
        }
        
        LOGGER.debug("Successfully copied {} '{}' with options: {}", 
            getClass().getSimpleName(), getName(), options);
    }
}
```

### Template 7: Simple Configuration Entity (CRiskLevel, CAttachment, CComment)

```java
@Override
protected void copyEntityTo(final tech.derbent.api.entity.domain.CEntityDB<?> target, final CCloneOptions options) {
    super.copyEntityTo(target, options);
    
    if (target instanceof C{Entity}) {
        final C{Entity} targetEntity = (C{Entity}) target;
        
        // Copy all basic fields
        copyField(this::getField1, targetEntity::setField1);
        copyField(this::getField2, targetEntity::setField2);
        copyField(this::getColor, targetEntity::setColor);
        copyField(this::getIcon, targetEntity::setIcon);
        
        LOGGER.debug("Successfully copied {} '{}' with options: {}", 
            getClass().getSimpleName(), getName(), options);
    }
}
```

## Implementation Progress Tracking

### Week 1 (Jan 18-24)
- [x] CDecision ✅ (COMPLETED 2026-01-18)
- [ ] CRisk
- [ ] CIssue
- [ ] CTicket
- [ ] CMilestone
- [ ] CSprint

### Week 2 (Jan 25-31)
- [ ] CProvider
- [ ] CCustomer
- [ ] CTeam
- [ ] CDeliverable
- [ ] CAsset

### Week 3 (Feb 1-7)
- [ ] CBudget
- [ ] CProjectExpense
- [ ] CProjectIncome
- [ ] CInvoice
- [ ] COrder

### Week 4 (Feb 8-14)
- [ ] CProduct
- [ ] CProductVersion
- [ ] CProjectComponent
- [ ] CProjectComponentVersion
- [ ] CValidationCase

### Week 5 (Feb 15-21)
- [ ] CValidationSuite
- [ ] CValidationSession
- [ ] CValidationExecution
- [ ] CRiskLevel
- [ ] Remaining entities

## Testing Checklist per Entity

After implementing copyEntityTo for each entity:

1. **Compilation**
   - [ ] `./mvnw compile -DskipTests` succeeds

2. **Same-Type Copy**
   - [ ] Copy Activity → Activity
   - [ ] Verify all fields copied
   - [ ] Verify no validation errors

3. **Cross-Type Copy**
   - [ ] Copy Activity → Meeting
   - [ ] Verify common fields copied
   - [ ] Verify no validation errors

4. **Unique Fields**
   - [ ] Email fields made unique
   - [ ] Login fields made unique
   - [ ] No constraint violations

5. **Copy Options**
   - [ ] Test with all options enabled
   - [ ] Test with all options disabled
   - [ ] Test with resetDates=true
   - [ ] Test with includesRelations=true

6. **UI Testing**
   - [ ] CopyTo dialog shows entity
   - [ ] Copy operation succeeds
   - [ ] Navigation to copied entity works
   - [ ] Copied entity displays correctly

## Common Patterns to Follow

### 1. Always Call Super First
```java
super.copyEntityTo(target, options);
```

### 2. Always Type-Check
```java
if (target instanceof YourEntity) {
    final YourEntity targetEntity = (YourEntity) target;
    // ...
}
```

### 3. Use copyField for Simple Fields
```java
copyField(this::getField, targetEntity::setField);
```

### 4. Check Options for Conditional Fields
```java
if (!options.isResetDates()) {
    copyField(this::getDueDate, targetEntity::setDueDate);
}
```

### 5. Use copyCollection for Collections
```java
if (options.includesRelations()) {
    copyCollection(this::getChildren, 
        (col) -> targetEntity.children = (Set<Child>) col, 
        true);
}
```

### 6. Make Unique Fields Unique
```java
if (this.getEmail() != null) {
    targetEntity.setEmail(this.getEmail().replace("@", "+copy@"));
}
```

### 7. Always Add Debug Logging
```java
LOGGER.debug("Successfully copied {} '{}' with options: {}", 
    getClass().getSimpleName(), getName(), options);
```

## Files Reference

- **Pattern Documentation**: `docs/architecture/COPY_TO_PATTERN_CODING_RULE.md`
- **Audit Report**: `docs/implementation/COPY_TO_PATTERN_AUDIT_AND_FIXES.md`
- **Entity Checklist**: `docs/architecture/NEW_ENTITY_COMPLETE_CHECKLIST.md`
- **Coding Standards**: `AGENTS.md`

## Notes

1. **CAttachment and CComment** are auto-handled via their interfaces (`IHasAttachments`, `IHasComments`), but should still implement copyEntityTo for direct copying scenarios.

2. **Sprint Items** should NOT copy their child items (CSprintItem) - these are references to other entities and should be handled separately.

3. **Unique constraints** (email, login, username) MUST be made unique to avoid database constraint violations.

4. **Passwords and sensitive data** must NEVER be copied.

5. **Auto-generated fields** (IDs, audit fields) are handled by base classes and should NOT be copied explicitly.

## Compilation Status

- **Current**: ✅ All implemented entities compile successfully
- **CDecision**: ✅ Compiles successfully (2026-01-18)
- **Overall Project**: ✅ Compiles successfully

## Next Steps

1. Implement copyEntityTo for CRisk (high priority)
2. Implement copyEntityTo for CIssue (high priority)
3. Implement copyEntityTo for CTicket (high priority)
4. Continue with remaining high-priority entities
5. Test each implementation thoroughly
6. Update this document as entities are completed

---

**Last Updated**: 2026-01-18 09:35 UTC  
**Status**: 6/35 entities completed (17%)  
**Next Target**: CRisk, CIssue, CTicket
