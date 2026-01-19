# Entity Copy Pattern - Coding Standard

## Pattern: copyEntityTo (NOT createClone)

All entities extending CEntityDB use the `copyEntityTo` pattern for copying.

### Implementation Rules:

1. **Override copyEntityTo in your entity class**
2. **Always call super.copyEntityTo() first**
3. **Use copyField() for simple fields**
4. **Use interface methods for collections (IHasLinks.copyLinksTo, etc.)**
5. **Respect CCloneOptions (resetDates, cloneWorkflow, etc.)**

### Example Implementation:

```java
@Override
protected void copyEntityTo(final CEntityDB<?> target, 
        @SuppressWarnings("rawtypes") CAbstractService serviceTarget,
        final CCloneOptions options) {
    // ALWAYS call parent first
    super.copyEntityTo(target, serviceTarget, options);
    
    // Type-check target
    if (target instanceof final CMyEntity targetEntity) {
        // Copy simple fields
        copyField(this::getMyField, targetEntity::setMyField);
        
        // Copy dates conditionally
        if (!options.isResetDates()) {
            copyField(this::getDueDate, targetEntity::setDueDate);
        }
        
        // Copy collections using interface methods
        IHasLinks.copyLinksTo(this, target, options);
        IHasAttachments.copyAttachmentsTo(this, target, options);
        
        LOGGER.debug("Copied entity '{}'", getName());
    }
}
```

### What Gets Copied Automatically (by base classes):

- **CEntityDB**: id (new), version, timestamps (new)
- **CEntityNamed**: name, description, color, icon
- **CEntityOfCompany**: company reference
- **CEntityOfProject**: project reference, assignedTo, createdBy
- **CProjectItem**: status, workflow (if cloneWorkflow=true)

### What Should NOT Be Copied:

- Sprint relationships (sprintItem) - clone starts outside sprint
- Widget entities - created separately if needed  
- Parent-child relationships - must be established explicitly
- Auto-generated fields (sequence numbers, calculated values)

### Deprecated Pattern (DO NOT USE):

```java
// ❌ OLD PATTERN - DO NOT USE
EntityClass createClone(CCloneOptions options) { ... }
```

The `createClone` method and `ICloneable` interface are deprecated.
Use `copyEntityTo` instead.

## Usage from UI:

The `copyTo()` method in CEntityDB is the entry point:

```java
// Copy to same type
CActivity copy = activity.copyTo(CActivity.class, options);

// Copy to different type (if compatible)
CMeeting meeting = activity.copyTo(CMeeting.class, options);
```

## Implementation Status:

### ✅ Implemented:
- CActivity
- CMeeting  
- CLink
- CUser
- CEntityDB (base)
- CEntityNamed (base)
- CEntityOfCompany (base)

### 🔄 Needs Implementation:
Check each CProjectItem/CEntityOfProject subclass and add copyEntityTo if they have custom fields beyond the base class.
