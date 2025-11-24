# Component Development Coding Standards

## Overview
This document defines coding standards for developing UI components in the Derbent application, based on the CComponentListEntityBase pattern.

## 1. Exception Handling

### Rule: User-Facing Functions Must NOT Throw Exceptions
All user-facing functions (button clicks, event handlers, etc.) must catch exceptions and display them to users using `CNotificationService.showException()`.

**❌ INCORRECT:**
```java
private void onButtonClick() {
    entity.doSomething(); // Could throw exception
}
```

**✅ CORRECT:**
```java
private void onButtonClick() {
    try {
        entity.doSomething();
    } catch (final Exception ex) {
        LOGGER.error("Error performing operation", ex);
        CNotificationService.showException("Error performing operation", ex);
    }
}
```

### Rule: Always Use CNotificationService.showException for User Errors
Never use `showError()` when you have an exception. Always use `showException()` to provide full error details.

**❌ INCORRECT:**
```java
} catch (Exception e) {
    CNotificationService.showError("Error: " + e.getMessage());
}
```

**✅ CORRECT:**
```java
} catch (Exception e) {
    LOGGER.error("Error description", e);
    CNotificationService.showException("Error description", e);
}
```

## 2. Null Checking

### Rule: Replace Redundant Null Checks with Check.notNull
Remove `if (x != null) { ... }` patterns with no else clause. Use `Check.notNull()` instead to throw exceptions immediately.

**❌ INCORRECT:**
```java
public void process(Entity entity) {
    if (entity != null) {
        entity.doSomething();
    }
    // Continues without handling null case
}
```

**✅ CORRECT:**
```java
public void process(Entity entity) {
    Check.notNull(entity, "Entity cannot be null");
    entity.doSomething();
}
```

### Note: Conditional Logic with Else is Allowed
If your code has proper else handling or returns alternative values, keep the if statement:

**✅ ACCEPTABLE:**
```java
// Has else clause - this is fine
if (item != null) {
    return item.getName();
} else {
    return "Unknown";
}

// Returns alternative - this is fine
if (item != null && item.getStatus() != null) {
    return item.getStatus().getName();
}
return ""; // Alternative return
```

## 3. Validation Annotations

### Rule: All Entity Fields Must Have Validation Annotations
All non-nullable entity fields must have appropriate Jakarta Validation annotations with clear messages.

**✅ REQUIRED Annotations:**
- `@NotNull(message = "Clear description")` - For required fields
- `@Size(max = N, message = "Clear description")` - For string length constraints
- `@Min/@Max` - For numeric constraints
- `@NotBlank` - For strings that cannot be empty

**Example:**
```java
@Column(name = "item_type", nullable = false, length = 50)
@NotNull(message = "Item type is required")
@Size(max = 50, message = "Item type must not exceed 50 characters")
@AMetaData(
    displayName = "Item Type", required = true, readOnly = false,
    description = "Type of the project item", hidden = false, order = 4,
    maxLength = 50
)
private String itemType;
```

## 4. Component Structure

### Rule: Follow the CComponentListEntityBase Pattern
When creating list-based CRUD components:

1. **Extend CComponentListEntityBase** for generic functionality
2. **Implement all abstract methods** required by the base class
3. **Use proper service injection** in constructor
4. **Validate constructor parameters** with Check.notNull
5. **Configure grid columns** in configureGrid method
6. **Handle exceptions** in all user-facing methods

**Example Structure:**
```java
public class CComponentListMyEntity extends CComponentListEntityBase<CMyEntity, CParentEntity> {
    
    private final CMyEntityService myEntityService;
    private CParentEntity currentParent;
    
    public CComponentListMyEntity(final CMyEntityService myEntityService) {
        super("My Entities", CMyEntity.class, myEntityService);
        Check.notNull(myEntityService, "MyEntityService cannot be null");
        this.myEntityService = myEntityService;
    }
    
    @Override
    protected void configureGrid(final CGrid<CMyEntity> grid) {
        Check.notNull(grid, "Grid cannot be null");
        // Add columns...
    }
    
    @Override
    protected CMyEntity createNewEntity() {
        Check.notNull(currentParent, "Parent cannot be null when creating entity");
        // Create and return entity...
    }
    
    // ... implement other abstract methods
}
```

## 5. Logging

### Rule: Log at Appropriate Levels
- `LOGGER.debug()` - For flow tracking and variable values
- `LOGGER.error()` - For exceptions and error conditions
- `LOGGER.warn()` - For unexpected but handled conditions

**Example:**
```java
try {
    LOGGER.debug("Processing entity: {}", entity.getId());
    entityService.save(entity);
} catch (final Exception e) {
    LOGGER.error("Error saving entity", e);
    CNotificationService.showException("Error saving entity", e);
}
```

## 6. Icons and UI Elements

### Rule: Use VaadinIcon for All Icons
Always use VaadinIcon constants instead of string literals.

**✅ CORRECT:**
```java
Button addButton = new Button("Add", VaadinIcon.PLUS.create());
Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
Button moveUpButton = new Button("Move Up", VaadinIcon.ARROW_UP.create());
Button moveDownButton = new Button("Move Down", VaadinIcon.ARROW_DOWN.create());
```

## 7. Button Click Handlers

### Rule: Always Wrap Button Click Handlers with Try-Catch
Button click listeners must never propagate exceptions to the UI framework.

**✅ CORRECT:**
```java
addButton.addClickListener(e -> {
    try {
        handleAdd();
    } catch (final Exception ex) {
        LOGGER.error("Error handling add operation", ex);
        CNotificationService.showException("Error adding item", ex);
    }
});
```

Or delegate to a method that has try-catch:

**✅ ALSO CORRECT:**
```java
addButton.addClickListener(e -> handleAdd());

// In handleAdd method:
protected void handleAdd() {
    try {
        // Logic here
    } catch (final Exception ex) {
        LOGGER.error("Error handling add operation", ex);
        CNotificationService.showException("Error adding item", ex);
    }
}
```

## 8. Service Methods

### Rule: Service Methods Should Validate Parameters
All service methods should validate parameters with Check utilities at the beginning.

**✅ CORRECT:**
```java
public void processEntity(Entity entity, Parent parent) {
    Check.notNull(entity, "Entity cannot be null");
    Check.notNull(parent, "Parent cannot be null");
    Check.notNull(parent.getId(), "Parent must be saved before processing");
    
    // Process entity...
}
```

## 9. Constants

### Rule: Define Constants for Magic Strings and Numbers
Never use magic strings or numbers in code. Define them as constants.

**❌ INCORRECT:**
```java
if ("CActivity".equals(type)) { ... }
```

**✅ CORRECT:**
```java
private static final String ITEM_TYPE_ACTIVITY = "CActivity";
private static final String ITEM_TYPE_MEETING = "CMeeting";

if (ITEM_TYPE_ACTIVITY.equals(type)) { ... }
```

## 10. Notification Methods

### Standard Notification Methods
Use these CNotificationService methods appropriately:

- `showException(String message, Exception ex)` - For exceptions with full details
- `showSaveSuccess()` - After successful save
- `showDeleteSuccess()` - After successful delete
- `showWarning(String message)` - For warnings
- `showInfo(String message)` - For informational messages

## Summary Checklist

For each new component or feature:

- [ ] All button click handlers wrapped in try-catch
- [ ] All exceptions shown to user with `showException()`
- [ ] Redundant `if (x != null)` replaced with `Check.notNull()`
- [ ] Entity fields have `@NotNull` and other validation annotations
- [ ] Constructor parameters validated with Check utilities
- [ ] All service methods validate their parameters
- [ ] VaadinIcon used for all icons
- [ ] Magic strings/numbers defined as constants
- [ ] Appropriate logging at DEBUG and ERROR levels
- [ ] Follows CComponentListEntityBase pattern (if list component)

## Enforcement

These standards should be enforced through:
1. Code reviews
2. Static analysis tools
3. Team coding guidelines
4. Automated checks where possible

**Last Updated:** 2025-11-24
**Version:** 1.0
