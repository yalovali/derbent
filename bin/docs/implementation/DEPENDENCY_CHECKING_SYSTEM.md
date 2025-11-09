# Dependency Checking System

## Overview

The Dependency Checking System provides automatic validation before entity deletion, preventing data integrity violations by checking if entities are referenced by other entities in the system.

## Architecture

### Core Components

1. **IDependencyChecker Interface** (`tech.derbent.api.interfaces`)
   - Defines the contract for dependency checking
   - Single method: `String checkDependencies(EntityClass entity)`
   - Returns `null` if entity can be deleted, error message otherwise

2. **CAbstractService Implementation**
   - All services inherit default dependency checking behavior
   - Default implementation allows deletion (returns `null`)
   - Services override `checkDependencies()` to implement specific rules

3. **CCrudToolbar Integration**
   - Automatically configures dependency checker from service
   - Checks dependencies before showing delete confirmation
   - Displays user-friendly error messages

## Implementation Pattern

### Service Implementation Example

```java
@Override
public String checkDependencies(final EntityClass entity) {
    Check.notNull(entity, "Entity cannot be null");
    Check.notNull(entity.getId(), "Entity ID cannot be null");
    
    try {
        // Check if entity is marked as non-deletable
        if (entity.getAttributeNonDeletable()) {
            return "This entity is marked as non-deletable.";
        }
        
        // Check if entity is being used by other entities
        final long usageCount = repository.countByEntity(entity);
        if (usageCount > 0) {
            return String.format(
                "Cannot delete. It is being used by %d item%s.", 
                usageCount, 
                usageCount == 1 ? "" : "s"
            );
        }
        
        return null; // Entity can be deleted
    } catch (final Exception e) {
        LOGGER.error("Error checking dependencies: {}", entity.getName(), e);
        return "Error checking dependencies: " + e.getMessage();
    }
}
```

## Implemented Deletion Rules

### User Deletion Rules (CUserService)

1. **Last User Protection**
   - Cannot delete the last user in a company
   - Message: "Cannot delete the last user in the company. At least one user must remain."

2. **Self-Deletion Prevention**
   - Users cannot delete their own account while logged in
   - Message: "You cannot delete your own user account while logged in."

### Company Deletion Rules (CCompanyService)

1. **Own Company Protection**
   - Users cannot delete the company they're currently using
   - Message: "You cannot delete your own company. Please switch to another company first."

2. **User Association Check**
   - Cannot delete company with associated users
   - Message: "Cannot delete company. It is associated with X user(s). Please remove all users first."

### Type/Status Entity Rules

Applies to: ActivityType, ProjectItemStatus, UserType, and similar entities.

1. **Non-Deletable Flag**
   - System-configured entities cannot be deleted
   - Message: "This [type] is marked as non-deletable and cannot be removed from the system."

2. **Usage Count Check**
   - Cannot delete if referenced by other entities
   - Message: "Cannot delete [type]. It is being used by X [entities]."

## Adding Dependency Checking to New Services

### Step 1: Add Count Method to Repository

```java
@Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.fieldName = :entity")
long countByEntity(@Param("entity") EntityType entity);
```

### Step 2: Inject Repository in Service

```java
@Autowired
private IRelatedRepository relatedRepository;

public MyService(IMyRepository repository, Clock clock, 
                 ISessionService sessionService,
                 IRelatedRepository relatedRepository) {
    super(repository, clock, sessionService);
    this.relatedRepository = relatedRepository;
}
```

### Step 3: Override checkDependencies Method

```java
@Override
public String checkDependencies(final MyEntity entity) {
    Check.notNull(entity, "Entity cannot be null");
    Check.notNull(entity.getId(), "Entity ID cannot be null");
    
    try {
        // Implement your specific rules
        final long usageCount = relatedRepository.countByMyEntity(entity);
        if (usageCount > 0) {
            return String.format(
                "Cannot delete. It is being used by %d related item%s.",
                usageCount,
                usageCount == 1 ? "" : "s"
            );
        }
        return null;
    } catch (final Exception e) {
        LOGGER.error("Error checking dependencies for {}: {}", 
                     entity.getName(), e);
        return "Error checking dependencies: " + e.getMessage();
    }
}
```

## User Experience

### Delete Flow

1. User clicks Delete button in CCrudToolbar
2. System automatically calls `service.checkDependencies(entity)`
3. If dependencies exist:
   - Error notification is shown
   - Delete operation is cancelled
4. If no dependencies:
   - Confirmation dialog is shown
   - User can proceed with deletion

### Error Messages

Error messages follow this pattern:
- Clear explanation of why deletion failed
- Count of dependent entities (when applicable)
- Actionable guidance (e.g., "Please remove all users first")

## Testing

### Manual Testing Checklist

For each entity type with dependency checking:

1. ✓ Try to delete entity with dependencies → Should show error
2. ✓ Try to delete entity without dependencies → Should succeed
3. ✓ Try to delete non-deletable entity → Should show error
4. ✓ Verify error message is clear and informative
5. ✓ Verify entity count in error message is accurate

### Automated Testing

See `docs/testing/DEPENDENCY_CHECKING_TESTS.md` for Playwright test examples.

## Best Practices

### 1. Message Formatting
- Use proper pluralization (1 item vs 2 items)
- Include entity count when checking relationships
- Provide actionable guidance

### 2. Performance
- Use COUNT queries instead of fetching full entity lists
- Consider caching for frequently checked relationships
- Use appropriate transaction boundaries

### 3. Error Handling
- Always catch and log exceptions
- Return user-friendly error messages
- Never expose technical details to users

### 4. Consistency
- Follow the established pattern for all services
- Use consistent message formatting
- Maintain similar logging patterns

## Future Enhancements

Potential improvements for consideration:

1. **Batch Dependency Checking**
   - Check multiple entities at once
   - Useful for bulk delete operations

2. **Cascading Delete Options**
   - Allow deletion with automatic cleanup
   - Configurable per entity type

3. **Soft Delete Integration**
   - Automatic soft delete for entities with dependencies
   - Preserve referential integrity

4. **Dependency Visualization**
   - Show dependency graph to users
   - Help users understand relationships

## Related Documentation

- `CCrudToolbar.java` - UI component implementing delete flow
- `CAbstractService.java` - Base service with default implementation
- `IDependencyChecker.java` - Core interface definition
