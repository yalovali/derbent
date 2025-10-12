# Dependency Checking Implementation - Summary and Usage Guide

## What Was Implemented

The Dependency Checking System provides automatic validation before entity deletion to prevent data integrity violations. This implementation adds intelligent dependency checking to the CCrudToolbar component, ensuring users cannot delete entities that are referenced by other entities.

## Architecture Overview

```
┌──────────────────┐
│  CCrudToolbar    │ ← Automatically configures dependency checker
└────────┬─────────┘
         │ uses
         ▼
┌──────────────────┐
│ CAbstractService │ ← Implements IDependencyChecker interface
└────────┬─────────┘
         │ overrides
         ▼
┌──────────────────┐
│ Specific Service │ ← Custom dependency logic per entity type
│ (e.g., CUserService)
└──────────────────┘
```

## Key Components

### 1. IDependencyChecker Interface
```java
public interface IDependencyChecker<EntityClass> {
    String checkDependencies(EntityClass entity);
}
```

- Returns `null` if entity can be deleted
- Returns error message if deletion should be blocked

### 2. CAbstractService Enhancement
- All services now implement `IDependencyChecker`
- Default implementation allows deletion (returns `null`)
- Services override to add specific rules

### 3. CCrudToolbar Auto-Configuration
- Automatically uses service's `checkDependencies()` method
- No manual configuration required
- Checks dependencies before showing confirmation dialog

## Implemented Rules

### User Deletion Protection

**CUserService**:
1. ✅ Cannot delete the last user in a company
2. ✅ Cannot delete your own user account while logged in

```java
// Example error messages:
"Cannot delete the last user in the company. At least one user must remain."
"You cannot delete your own user account while logged in."
```

### Company Deletion Protection

**CCompanyService**:
1. ✅ Cannot delete your current company
2. ✅ Cannot delete company with associated users

```java
// Example error messages:
"You cannot delete your own company. Please switch to another company first."
"Cannot delete company. It is associated with 5 user(s). Please remove all users first."
```

### Type/Status Entity Protection

**CActivityTypeService, CActivityStatusService, CUserTypeService**:
1. ✅ Cannot delete non-deletable system types
2. ✅ Cannot delete types/statuses in use by other entities
3. ✅ Shows count of dependent entities

```java
// Example error messages:
"This activity type is marked as non-deletable and cannot be removed from the system."
"Cannot delete activity type. It is being used by 12 activities."
"Cannot delete user type. It is being used by 3 users."
```

## Usage Examples

### For Developers: Adding Dependency Checking to New Services

**Step 1: Add Count Query to Repository**

```java
@Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.myType = :type")
long countByMyType(@Param("type") MyType type);
```

**Step 2: Inject Repository in Service Constructor**

```java
@Autowired
private IMyRelatedRepository myRelatedRepository;

public MyTypeService(IMyTypeRepository repository, Clock clock,
                     ISessionService sessionService,
                     IMyRelatedRepository myRelatedRepository) {
    super(repository, clock, sessionService);
    this.myRelatedRepository = myRelatedRepository;
}
```

**Step 3: Override checkDependencies Method**

```java
@Override
public String checkDependencies(final MyType type) {
    Check.notNull(type, "Type cannot be null");
    Check.notNull(type.getId(), "Type ID cannot be null");
    
    try {
        // Check non-deletable flag
        if (type.getAttributeNonDeletable()) {
            return "This type is marked as non-deletable.";
        }
        
        // Check usage
        final long count = myRelatedRepository.countByMyType(type);
        if (count > 0) {
            return String.format(
                "Cannot delete. It is being used by %d item%s.",
                count, count == 1 ? "" : "s"
            );
        }
        
        return null; // Can be deleted
    } catch (Exception e) {
        LOGGER.error("Error checking dependencies: {}", type.getName(), e);
        return "Error checking dependencies: " + e.getMessage();
    }
}
```

### For Users: What to Expect

When trying to delete an entity:

1. **If dependencies exist**:
   - Error notification appears immediately
   - Delete confirmation dialog does NOT appear
   - Entity is not deleted
   - Error message explains why

2. **If no dependencies**:
   - Confirmation dialog appears as normal
   - User can proceed with deletion
   - Success notification shows after deletion

## Testing

### Manual Testing Steps

1. **Test Last User Protection**:
   - Create company with one user
   - Try to delete that user
   - Should see error about last user

2. **Test Type in Use**:
   - Create activity type
   - Create activity using that type
   - Try to delete the type
   - Should see error with count

3. **Test Successful Deletion**:
   - Create type without any usage
   - Try to delete
   - Should see confirmation dialog
   - Delete should succeed

### Automated Tests

Run Playwright tests:
```bash
./run-playwright-tests.sh mock
```

Tests are in: `CDependencyCheckingTest.java`
- testActivityTypeInUseCannotBeDeleted()
- testUserTypeInUseCannotBeDeleted()
- testActivityStatusInUseCannotBeDeleted()
- testLastUserCannotBeDeleted()

## Error Message Guidelines

All error messages follow these patterns:

1. **Clear explanation**: Tell the user exactly why deletion failed
2. **Entity counts**: Include the number of dependent entities
3. **Action guidance**: Suggest what the user should do
4. **Proper grammar**: Use correct singular/plural forms

Good examples:
✅ "Cannot delete. It is being used by 1 activity."
✅ "Cannot delete. It is being used by 5 activities."
✅ "Cannot delete company. It is associated with 3 user(s). Please remove all users first."

Bad examples:
❌ "Cannot delete" (no explanation)
❌ "Error" (too vague)
❌ "It is being used by activities" (no count)

## Benefits

1. **Data Integrity**: Prevents orphaned references and broken relationships
2. **User Experience**: Clear error messages explain why deletion failed
3. **Automatic**: Works without manual configuration
4. **Extensible**: Easy to add rules for new entity types
5. **Maintainable**: Centralized pattern across all services

## Files Modified/Created

### New Files:
- `src/main/java/tech/derbent/api/interfaces/IDependencyChecker.java`
- `docs/implementation/DEPENDENCY_CHECKING_SYSTEM.md`
- `docs/testing/DEPENDENCY_CHECKING_TESTS.md`
- `src/test/java/automated_tests/tech/derbent/ui/automation/CDependencyCheckingTest.java`

### Modified Files:
- `src/main/java/tech/derbent/api/services/CAbstractService.java`
- `src/main/java/tech/derbent/api/views/components/CCrudToolbar.java`
- `src/main/java/tech/derbent/users/service/CUserService.java`
- `src/main/java/tech/derbent/users/service/IUserRepository.java`
- `src/main/java/tech/derbent/companies/service/CCompanyService.java`
- `src/main/java/tech/derbent/activities/service/CActivityTypeService.java`
- `src/main/java/tech/derbent/activities/service/CActivityStatusService.java`
- `src/main/java/tech/derbent/activities/service/IActivityRepository.java`
- `src/main/java/tech/derbent/users/service/CUserTypeService.java`

## Next Steps

To extend this implementation to other entity types:

1. **Meeting Types/Statuses**: Add count queries and implement checks
2. **Order Types/Statuses**: Add count queries and implement checks
3. **Decision Types/Statuses**: Add count queries and implement checks
4. **Risk Statuses**: Add count queries and implement checks
5. **Currency**: Check if used in orders before deletion

The pattern is the same for all - just follow the steps in the "Adding Dependency Checking" section above.

## Support and Documentation

- Full architecture: `docs/implementation/DEPENDENCY_CHECKING_SYSTEM.md`
- Test strategy: `docs/testing/DEPENDENCY_CHECKING_TESTS.md`
- Code examples in this document

## Summary

✅ Core infrastructure implemented
✅ 5 entity types with dependency checking
✅ Automatic configuration in CCrudToolbar
✅ Comprehensive documentation
✅ Playwright tests created
✅ All code compiles successfully
✅ Ready for manual testing and deployment
