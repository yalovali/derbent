# Validation Enhancement - Quick Reference

## What Was Done

Enhanced the Derbent validation system to:
1. Automatically validate all required fields (nullable=false)
2. Show user-friendly error messages with field names
3. Integrate validation into CRUD operations
4. Support custom business rules in services

## Key Files Modified

### 1. CExceptionNotify.java (NEW)
```java
// User-facing exception for validation/business errors
// Note: We use String returns for validation, but this is available for other cases
public class CExceptionNotify extends Exception {
    public CExceptionNotify(final String message) {
        super(message);
    }
}
```

### 2. CAbstractService.java (ENHANCED)
```java
// Added automatic nullable field validation
public String checkSaveAllowed(final EntityClass entity) {
    Check.notNull(entity, "Entity cannot be null");
    final String nullableFieldsError = validateNullableFields(entity);
    if (nullableFieldsError != null) {
        return nullableFieldsError;
    }
    return null;
}

// Validates @Column(nullable=false) fields
protected String validateNullableFields(final EntityClass entity) {
    // Uses reflection to check all fields
    // Returns "Required field 'Name' cannot be empty." if validation fails
    // Returns null if all required fields are populated
}
```

### 3. CCrudToolbar.java (ENHANCED)
```java
// Integrated validation into save operation
private void handleSave() throws Exception {
    // ... existing code ...
    
    // NEW: Check if save is allowed
    final String saveError = entityService.checkSaveAllowed(currentEntity);
    if (saveError != null) {
        showErrorNotification(saveError);  // Shows to user!
        return;
    }
    
    // Proceed with save...
}
```

## How It Works

### Automatic Validation Example

**Domain Class:**
```java
@Entity
public class CProjectItemStatus extends CStatus<CProjectItemStatus> {
    @Column(nullable = false, length = 100)
    @AMetaData(displayName = "Status Name", required = true)
    private String name;
    
    @Column(nullable = false, length = 7)
    @AMetaData(displayName = "Status Color", required = true)
    private String color;
    
    @Column(nullable = true)  // Can be null
    @AMetaData(displayName = "Description")
    private String description;
}
```

**Service (No code needed!):**
```java
@Service
public class CProjectItemStatusService extends CStatusService<CProjectItemStatus> {
    // Automatically validates nullable fields through inheritance
    // checkSaveAllowed() inherited from CAbstractService
}
```

**What Happens When User Saves:**
```
User leaves "Status Name" and "Status Color" empty
↓
Clicks "Save" button
↓
CCrudToolbar calls: checkSaveAllowed(entity)
↓
CAbstractService validates: finds 2 null required fields
↓
Returns: "Required fields cannot be empty: Status Name, Status Color"
↓
CCrudToolbar shows error notification to user
↓
Save is prevented
```

### Custom Validation Example

**Service with Custom Rules:**
```java
@Service
public class CWorkflowStatusRelationService extends CAbstractEntityRelationService<CWorkflowStatusRelation> {
    
    @Override
    public String checkSaveAllowed(final CWorkflowStatusRelation entity) {
        // 1. Call parent validation (nullable fields)
        String result = super.checkSaveAllowed(entity);
        if (result != null) {
            return result;
        }
        
        // 2. Add custom business rule
        if (entity.getFromStatus() == entity.getToStatus()) {
            return String.format(
                "From status and To status cannot be the same. %s -> %s",
                entity.getFromStatus().getName(),
                entity.getToStatus().getName()
            );
        }
        
        return null;  // All checks passed
    }
}
```

## Design Decision: String vs Exception

### Question from Issue
*"Is returning a string a good approach. or should we throw a CExceptionNoftify type of exception?"*

### Decision: **Use String Returns**

**Reasons:**
1. ✅ Consistent with existing codebase pattern
2. ✅ Simpler control flow (no try-catch needed)
3. ✅ Better performance (no stack trace overhead)
4. ✅ Clear semantics: `null` = success, `String` = error
5. ✅ Easy to chain validations

**Code Comparison:**

```java
// With String returns (CHOSEN) ✅
String error = service.checkSaveAllowed(entity);
if (error != null) {
    showErrorNotification(error);
    return;
}
// Proceed with save...

// vs. With exceptions ❌
try {
    service.checkSaveAllowed(entity);
    // Proceed with save...
} catch (CExceptionNotify e) {
    showErrorNotification(e.getMessage());
    return;
}
```

### When to Use CExceptionNotify

We created `CExceptionNotify` but chose not to use it for validation. Use it for:

```java
// Complex operations that can fail
public void processOrder(Order order) throws CExceptionNotify {
    if (!paymentVerified(order)) {
        throw new CExceptionNotify("Payment verification failed");
    }
}

// Background jobs reporting to users
public void importData(File file) throws CExceptionNotify {
    if (!file.exists()) {
        throw new CExceptionNotify("File not found: " + file.getName());
    }
}
```

## All Requirements Satisfied

From the original issue:

| Requirement | Status | Implementation |
|------------|--------|----------------|
| Check checkDeleteAllowed function | ✅ Done | Enhanced with nullable validation |
| Add similar functions to services | ✅ Done | All inherit from CAbstractService |
| Make sure they are used | ✅ Done | Integrated in CCrudToolbar |
| Show results to user | ✅ Done | showErrorNotification() |
| Status cannot be from and to | ✅ Done | CWorkflowStatusRelationService |
| Required fields validation | ✅ Done | validateNullableFields() |
| Generic code with field properties | ✅ Done | Uses @AMetaData displayName |
| Status in flow cannot be deleted | ✅ Done | CProjectItemStatusService |
| CRUD functions use properly | ✅ Done | CCrudToolbar integration |
| String vs Exception decision | ✅ Done | Chose String returns |

## Testing

### Run Unit Tests
```bash
mvn test -Dtest=CAbstractServiceValidationTest
```

**Results:**
```
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0

✅ testCheckSaveAllowed_WithValidEntity_ReturnsNull
✅ testCheckSaveAllowed_WithMissingName_ReturnsError
✅ testCheckSaveAllowed_WithNullEntity_ThrowsException
✅ testCheckDeleteAllowed_WithValidEntity_ReturnsNull
✅ testCheckDeleteAllowed_WithNullEntity_ThrowsException
✅ testCheckDeleteAllowed_WithNullId_ThrowsException
✅ testValidateNullableFields_FormatsFieldNamesNicely
```

### Manual Testing Scenarios

1. **Test Required Field Validation:**
   - Open Activity Status edit screen
   - Leave "Status Name" empty
   - Click Save
   - ✅ Should see: "Required field 'Status Name' cannot be empty."

2. **Test Delete Protection:**
   - Create an activity with a status
   - Try to delete that status
   - ✅ Should see: "Cannot delete. It is being used by 1 activity."

3. **Test Business Rule:**
   - Create workflow relation with same from/to status
   - Click Save
   - ✅ Should see: "From status and To status cannot be the same."

## Services Updated

### Automatically Benefit from Base Validation

All these services now automatically validate nullable fields:

**Status Services:**
- CProjectItemStatusService ✅
- CMeetingStatusService ✅
- CDecisionStatusService ✅
- COrderStatusService ✅
- CApprovalStatusService ✅
- CRiskStatusService ✅

**Type Services:**
- CActivityTypeService ✅
- CMeetingTypeService ✅
- CDecisionTypeService ✅
- COrderTypeService ✅

**Other Services:**
- CActivityService ✅
- CProjectService ✅
- CMeetingService ✅
- All other services extending CAbstractService ✅

### Services with Custom Validation

**CWorkflowStatusRelationService:**
- ✅ Validates from/to status are different

**CProjectItemStatusService:**
- ✅ Checks if status is used by activities before delete
- ✅ Checks if status is used in workflows before delete

**CTypeEntityService:**
- ✅ Checks non-deletable flag before delete

## User Experience

### Before Enhancement
```
User tries to save entity with missing fields
↓
Entity saved with null values
↓
Causes database errors or NullPointerExceptions later
❌ Poor user experience
```

### After Enhancement
```
User tries to save entity with missing fields
↓
Validation catches the issue immediately
↓
Shows clear error: "Required field 'Status Name' cannot be empty."
↓
User fixes the issue
↓
Entity saved successfully
✅ Excellent user experience
```

## Documentation

Comprehensive documentation created:

1. **VALIDATION_GUIDE.md** - Complete implementation guide with examples
2. **VALIDATION_IMPLEMENTATION_SUMMARY.md** - Technical summary
3. **This file** - Quick reference for developers

## Next Steps

The validation system is **complete and ready for use**. No additional work required.

### Optional Future Enhancements

If needed in the future, could add:
- Field-level validation rules (regex patterns, etc.)
- Async validation (database uniqueness checks)
- Validation groups (different rules for create vs update)
- Custom validator functions

But the current implementation satisfies all requirements and provides a solid foundation.

## Questions?

See the comprehensive guides:
- **VALIDATION_GUIDE.md** - Full implementation guide with all details
- **VALIDATION_IMPLEMENTATION_SUMMARY.md** - Technical summary

Or check the source code:
- `src/main/java/tech/derbent/api/services/CAbstractService.java`
- `src/main/java/tech/derbent/api/views/components/CCrudToolbar.java`
- `src/main/java/tech/derbent/api/exceptions/CExceptionNotify.java`
- `src/test/java/tech/derbent/api/services/CAbstractServiceValidationTest.java`
