# Attachment System - Improved Pattern with Component Factory

## Overview

Instead of each page service implementing its own `createAttachmentsComponent()` method, we use a **centralized factory** referenced in the @AMetaData annotation.

## Benefits

1. ✅ **No code duplication** - Single factory for all entities
2. ✅ **Cleaner page services** - No attachment-specific methods needed
3. ✅ **Consistent behavior** - Same component creation logic everywhere
4. ✅ **Easy maintenance** - Update factory once, affects all entities
5. ✅ **Type safe** - Generic factory handles any entity type

## Pattern Comparison

### ❌ OLD Pattern (Duplicated Code)

Each page service had to implement the same method:

```java
// CPageServiceActivity.java
@Service
public class CPageServiceActivity extends CAbstractPageService<CActivity> {
    @Autowired
    private CAttachmentService attachmentService;
    @Autowired
    private ISessionService sessionService;
    private CComponentListAttachments<CActivity> componentAttachments;
    
    public CComponentListAttachments<CActivity> createAttachmentsComponent() {
        if (componentAttachments == null) {
            componentAttachments = new CComponentListAttachments<>(
                CActivity.class, attachmentService, sessionService);
            componentAttachments.registerWithPageService(this);
        }
        return componentAttachments;
    }
}

// CPageServiceRisk.java
@Service
public class CPageServiceRisk extends CAbstractPageService<CRisk> {
    @Autowired
    private CAttachmentService attachmentService;
    @Autowired
    private ISessionService sessionService;
    private CComponentListAttachments<CRisk> componentAttachments;
    
    // DUPLICATE METHOD - Same code, different type
    public CComponentListAttachments<CRisk> createAttachmentsComponent() {
        if (componentAttachments == null) {
            componentAttachments = new CComponentListAttachments<>(
                CRisk.class, attachmentService, sessionService);
            componentAttachments.registerWithPageService(this);
        }
        return componentAttachments;
    }
}

// ... repeated in CPageServiceMeeting, CPageServiceSprint, CPageServiceProject, CPageServiceUser, etc.
```

**Problems:**
- Code duplication in every page service
- Maintenance nightmare (update in 10+ places)
- Easy to forget entities
- Inconsistent implementations

### ✅ NEW Pattern (Centralized Factory)

**One factory handles all entities:**

```java
// CAttachmentComponentFactory.java
@Component("CAttachmentComponentFactory")
public class CAttachmentComponentFactory {
    @Autowired
    private CAttachmentService attachmentService;
    @Autowired
    private ISessionService sessionService;
    
    public <T extends CEntityDB<T>> CComponentListAttachments<T> createComponent(
            final T masterEntity, final String fieldName) {
        
        @SuppressWarnings("unchecked")
        final Class<T> entityClass = (Class<T>) masterEntity.getClass();
        
        final CComponentListAttachments<T> component = new CComponentListAttachments<>(
                entityClass, attachmentService, sessionService);
        
        component.setMasterEntity(masterEntity);
        return component;
    }
}
```

**Benefits:**
- ✅ Single implementation for ALL entities
- ✅ No code in page services
- ✅ Automatic support for new entities
- ✅ Consistent behavior guaranteed

## Implementation Guide

### Step 1: Add @OneToMany Field to Entity

Add the attachments field with proper @AMetaData annotation:

```java
package tech.derbent.app.activities.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.app.attachments.domain.CAttachment;

@Entity
public class CActivity extends CProjectItem<CActivity> {
    
    /**
     * File attachments for this activity.
     * 
     * Uses CAttachmentComponentFactory for component creation via @AMetaData.
     * Form builder will automatically invoke:
     *   CAttachmentComponentFactory.createComponent(activity, "attachments")
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    @AMetaData(
        displayName = "Attachments",
        required = false,
        readOnly = false,
        description = "File attachments for this activity",
        hidden = false,
        createComponentMethodBean = "CAttachmentComponentFactory",  // ← Factory bean name
        createComponentMethod = "createComponent"                    // ← Factory method name
    )
    private Set<CAttachment> attachments = new HashSet<>();
    
    // Standard getters/setters
    public Set<CAttachment> getAttachments() {
        if (attachments == null) {
            attachments = new HashSet<>();
        }
        return attachments;
    }
    
    public void setAttachments(final Set<CAttachment> attachments) {
        this.attachments = attachments;
    }
}
```

**Key Points:**
- `createComponentMethodBean = "CAttachmentComponentFactory"` - References the Spring bean
- `createComponentMethod = "createComponent"` - Method to invoke
- Form builder will call: `factory.createComponent(entity, "attachments")`

### Step 2: That's It! No Page Service Changes

The page service **DOES NOT** need any attachment-specific code:

```java
package tech.derbent.app.activities.service;

import org.springframework.stereotype.Service;
import tech.derbent.app.activities.domain.CActivity;

@Service
public class CPageServiceActivity extends CAbstractPageService<CActivity> {
    
    // NO createAttachmentsComponent() method needed!
    // Factory handles it automatically via @AMetaData
    
    // ... other methods ...
}
```

### Step 3: Same Pattern for All Entities

**Apply to Activity:**
```java
@AMetaData(
    displayName = "Attachments",
    createComponentMethodBean = "CAttachmentComponentFactory",
    createComponentMethod = "createComponent"
)
private Set<CAttachment> attachments = new HashSet<>();
```

**Apply to Risk:**
```java
@AMetaData(
    displayName = "Attachments",
    createComponentMethodBean = "CAttachmentComponentFactory",
    createComponentMethod = "createComponent"
)
private Set<CAttachment> attachments = new HashSet<>();
```

**Apply to Meeting:**
```java
@AMetaData(
    displayName = "Attachments",
    createComponentMethodBean = "CAttachmentComponentFactory",
    createComponentMethod = "createComponent"
)
private Set<CAttachment> attachments = new HashSet<>();
```

**Apply to Sprint:**
```java
@AMetaData(
    displayName = "Attachments",
    createComponentMethodBean = "CAttachmentComponentFactory",
    createComponentMethod = "createComponent"
)
private Set<CAttachment> attachments = new HashSet<>();
```

**Apply to Project:**
```java
@AMetaData(
    displayName = "Attachments",
    createComponentMethodBean = "CAttachmentComponentFactory",
    createComponentMethod = "createComponent"
)
private Set<CAttachment> attachments = new HashSet<>();
```

**Apply to User:**
```java
@AMetaData(
    displayName = "Attachments",
    createComponentMethodBean = "CAttachmentComponentFactory",
    createComponentMethod = "createComponent"
)
private Set<CAttachment> attachments = new HashSet<>();
```

**Apply to ANY entity:**
Just copy the same @AMetaData annotation!

## How It Works

### Form Builder Integration

1. **Entity Rendering:**
   ```
   Form Builder detects @AMetaData with createComponentMethodBean
   ```

2. **Bean Lookup:**
   ```
   Spring context retrieves bean: CAttachmentComponentFactory
   ```

3. **Method Invocation:**
   ```java
   factory.createComponent(activity, "attachments")
   ```

4. **Component Creation:**
   ```java
   new CComponentListAttachments<>(CActivity.class, service, session)
   ```

5. **Component Display:**
   ```
   Form Builder embeds component in detail view
   ```

### Sequence Diagram

```
User Opens Activity Detail
        |
        v
Form Builder Renders Activity
        |
        v
Detects attachments field
        |
        v
Reads @AMetaData annotation
        |
        v
createComponentMethodBean = "CAttachmentComponentFactory"
createComponentMethod = "createComponent"
        |
        v
Spring Context
        |
        v
CAttachmentComponentFactory bean
        |
        v
factory.createComponent(activity, "attachments")
        |
        v
new CComponentListAttachments<CActivity>(...)
        |
        v
component.setMasterEntity(activity)
        |
        v
component.refreshGrid()
        |
        v
Display attachment grid in form
```

## Factory Implementation Details

### Generic Type Handling

```java
public <T extends CEntityDB<T>> CComponentListAttachments<T> createComponent(
        final T masterEntity, final String fieldName) {
    
    // Extract entity class at runtime
    @SuppressWarnings("unchecked")
    final Class<T> entityClass = (Class<T>) masterEntity.getClass();
    
    // Create type-safe component
    return new CComponentListAttachments<>(entityClass, service, session);
}
```

### Method Signatures

The factory supports two method signatures for flexibility:

```java
// Full signature (preferred)
createComponent(T masterEntity, String fieldName)

// Simplified signature (fallback)
createComponent(T masterEntity)
```

Form builders can call either method.

## Advantages Over Old Pattern

| Aspect | Old Pattern | New Pattern |
|--------|-------------|-------------|
| **Code Lines** | ~15 lines per entity | 0 lines per entity |
| **Maintenance** | Update 10+ places | Update 1 place |
| **Consistency** | Can drift | Always consistent |
| **New Entities** | Must implement method | Automatic support |
| **Testing** | Test each service | Test factory once |
| **Dependencies** | Service needs factory deps | No service changes |
| **Type Safety** | Manual type parameters | Automatic inference |

## Migration Guide

### From Old to New Pattern

**Step 1: Add Factory Class**
```bash
# Already created at:
src/main/java/tech/derbent/app/attachments/view/CAttachmentComponentFactory.java
```

**Step 2: Update Entity @AMetaData**

Change from:
```java
@AMetaData(
    displayName = "Attachments",
    createComponentMethod = "createAttachmentsComponent"  // Old: references page service
)
```

To:
```java
@AMetaData(
    displayName = "Attachments",
    createComponentMethodBean = "CAttachmentComponentFactory",  // New: references factory
    createComponentMethod = "createComponent"
)
```

**Step 3: Remove Page Service Method**

Delete this from page services:
```java
// DELETE THIS METHOD - No longer needed
public CComponentListAttachments<CActivity> createAttachmentsComponent() {
    // ... old code ...
}
```

**Step 4: Remove Autowired Dependencies**

Remove from page services:
```java
// DELETE THESE - No longer needed
@Autowired
private CAttachmentService attachmentService;

@Autowired
private ISessionService sessionService;

private CComponentListAttachments<CActivity> componentAttachments;
```

**Step 5: Test**
- Open entity detail page
- Verify attachments tab displays
- Upload, download, delete attachments
- Confirm everything works

## Best Practices

### 1. Consistent @AMetaData

Use exact same annotation for all entities:

```java
@AMetaData(
    displayName = "Attachments",
    required = false,
    readOnly = false,
    description = "File attachments for this {entity}",  // Customize description
    hidden = false,
    createComponentMethodBean = "CAttachmentComponentFactory",
    createComponentMethod = "createComponent"
)
private Set<CAttachment> attachments = new HashSet<>();
```

### 2. Field Name Convention

Always use `attachments` as the field name for consistency:
- ✅ `private List<CAttachment> attachments`
- ❌ `private List<CAttachment> files`
- ❌ `private List<CAttachment> documents`

### 3. Initialization in Constructor

Initialize the list in entity constructor:

```java
public CActivity() {
    super();
    this.attachments = new HashSet<>();
}
```

### 4. Null-Safe Getter

Ensure getter returns non-null list:

```java
public Set<CAttachment> getAttachments() {
    if (attachments == null) {
        attachments = new HashSet<>();
    }
    return attachments;
}
```

## Testing the Factory

### Unit Test

```java
@SpringBootTest
class CAttachmentComponentFactoryTest {
    
    @Autowired
    private CAttachmentComponentFactory factory;
    
    @Test
    void testCreateComponent_Activity() {
        // Given
        CActivity activity = new CActivity();
        activity.setId(1L);
        
        // When
        CComponentListAttachments<CActivity> component = 
            factory.createComponent(activity, "attachments");
        
        // Then
        assertNotNull(component);
        assertEquals(activity, component.getMasterEntity());
    }
    
    @Test
    void testCreateComponent_Risk() {
        // Given
        CRisk risk = new CRisk();
        risk.setId(2L);
        
        // When
        CComponentListAttachments<CRisk> component = 
            factory.createComponent(risk, "attachments");
        
        // Then
        assertNotNull(component);
        assertEquals(risk, component.getMasterEntity());
    }
    
    @Test
    void testCreateComponent_Generic() {
        // Test that factory works with any CEntityDB subclass
        testEntityType(new CActivity());
        testEntityType(new CRisk());
        testEntityType(new CMeeting());
        testEntityType(new CSprint());
        testEntityType(new CProject());
    }
    
    private <T extends CEntityDB<T>> void testEntityType(T entity) {
        CComponentListAttachments<T> component = 
            factory.createComponent(entity, "attachments");
        assertNotNull(component);
        assertEquals(entity, component.getMasterEntity());
    }
}
```

## Summary

The **centralized factory pattern** eliminates code duplication and simplifies attachment integration:

1. ✅ **One factory class** handles ALL entities
2. ✅ **@AMetaData annotation** references factory bean
3. ✅ **Zero page service code** required
4. ✅ **Automatic support** for new entities
5. ✅ **Type-safe** generic implementation
6. ✅ **Easy maintenance** - update once, affects all

**Migration Steps:**
1. Add CAttachmentComponentFactory (already done)
2. Update @AMetaData in entities (add createComponentMethodBean)
3. Remove createAttachmentsComponent() from page services
4. Test and verify

This pattern can be extended to other shared components (comments, tags, etc.) following the same factory approach.
