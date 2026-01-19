# Child Entity Management Patterns

## Overview

This document describes the patterns for implementing child entities that are managed exclusively through their parent entities, without standalone views or management pages.

## Pattern: Child Entities Managed by Parents

### Definition

Child entities are database entities that:
- Have NO standalone management views/pages
- Have NO dedicated service layer (or minimal service)
- Have NO menu entries or navigation
- Are ONLY accessed and managed through their parent entity's detail view
- Are displayed and edited via custom UI components embedded in parent forms

### Current Implementations

#### 1. CComment - Discussion and Notes

**Purpose**: User comments and discussion threads attached to parent entities.

**Domain Layer**:
```java
// Interface for parent entities
package tech.derbent.plm.comments.domain;

public interface IHasComments {
    Set<CComment> getComments();
    void setComments(Set<CComment> comments);
}

// Entity class
@Entity
@Table(name = "ccomment")
public class CComment extends CEntityOfCompany<CComment> {
    @Column(name = "comment_text", nullable = false, length = 4000)
    private String commentText;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = true)
    private CUser author;
    
    @Column(name = "is_important", nullable = false)
    private Boolean important = Boolean.FALSE;
    
    // No back-reference to parent - clean unidirectional pattern
}
```

**Parent Entity Implementation**:
```java
public class CActivity extends CProjectItem<CActivity> 
        implements IHasComments {
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    @AMetaData(
        displayName = "Comments",
        required = false,
        readOnly = false,
        description = "Comments for this activity",
        hidden = false,
        dataProviderBean = "CCommentService",
        createComponentMethod = "createComponent"
    )
    private Set<CComment> comments = new HashSet<>();
    
    @Override
    public Set<CComment> getComments() {
        if (comments == null) {
            comments = new HashSet<>();
        }
        return comments;
    }
    
    @Override
    public void setComments(Set<CComment> comments) {
        this.comments = comments;
    }
}
```

**Parent Entities with Comments**:
- ✅ CActivity
- ✅ CIssue
- ✅ CMeeting
- ✅ CDecision
- ✅ CRisk
- ✅ COrder
- ✅ CProject (newly added)
- ✅ CSprint (newly added)
- ✅ CTeam (newly added)
- ✅ CUser (newly added)

**Key Pattern Characteristics**:
- Comments have NO standalone service
- Comments have NO initializer service
- Comments have NO menu entry or dedicated view
- Comments are rendered via UI component factory method in parent detail view
- JoinColumn uses parent's FK field (e.g., "activity_id")

#### 2. CAttachment - File Attachments

**Purpose**: File attachments with metadata (filename, size, upload date, version).

**Domain Layer**:
```java
// Interface for parent entities
package tech.derbent.plm.attachments.domain;

public interface IHasAttachments {
    Set<CAttachment> getAttachments();
    void setAttachments(Set<CAttachment> attachments);
}

// Entity class
@Entity
@Table(name = "cattachment")
public class CAttachment extends CEntityOfCompany<CAttachment> {
    @Column(nullable = false, length = 500)
    private String fileName;
    
    @Column(nullable = false)
    private Long fileSize = 0L;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private CUser uploadedBy;
    
    // No back-reference to parent - clean unidirectional pattern
}
```

**Service Layer** (Minimal):
```java
@Service
public class CAttachmentService extends CAbstractService<CAttachment> {
    // Minimal service - primarily for component factory
    // No complex business logic
}
```

**Initializer Service Pattern**:
```java
public class CAttachmentInitializerService extends CInitializerServiceBase {
    
    /**
     * Add standard Attachments section to any entity detail view.
     * This is the ONLY method that creates attachment sections.
     * ALL entity initializers MUST call this method.
     */
    public static void addAttachmentsSection(
            final CDetailSection detailSection, 
            final Class<?> entityClass) throws Exception {
        
        // Section header - IDENTICAL for all entities
        detailSection.addScreenLine(
            CDetailLinesService.createSection("Attachments"));
        
        // Attachments field - IDENTICAL for all entities
        detailSection.addScreenLine(
            CDetailLinesService.createLineFromDefaults(
                entityClass, "attachments"));
    }
}
```

**Usage in Parent Initializer**:
```java
public class CActivityInitializerService extends CInitializerServiceBase {
    
    public static CDetailSection createBasicView(CProject project) throws Exception {
        CDetailSection scr = createBaseScreenEntity(project, CActivity.class);
        
        // ... other sections ...
        
        // Attachments section - standard for ALL entities
        CAttachmentInitializerService.addAttachmentsSection(scr, CActivity.class);
        
        // ... more sections ...
        
        return scr;
    }
}
```

**Parent Entities with Attachments**:
- ✅ CActivity
- ✅ CIssue
- ✅ CMeeting
- ✅ CDecision
- ✅ CRisk
- ✅ COrder
- ✅ CProject
- ✅ CSprint
- ✅ CTeam
- ✅ CUser

**Key Pattern Characteristics**:
- Attachments have minimal service (for component factory only)
- Attachments have initializer service with `addAttachmentsSection()` helper
- Attachments have NO standalone management page (though one CAN be created for admin)
- UI component renders file list with upload/download/delete actions
- Storage handled by CDiskAttachmentStorage service

#### 3. CDetailLines - Section Definition Lines

**Purpose**: Define which fields appear in entity detail views and their layout.

**Domain Layer**:
```java
@Entity
@Table(name = "cdetail_lines")
public class CDetailLines extends CEntityDB<CDetailLines> {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detailsection_id", nullable = false)
    private CDetailSection detailSection;  // Back-reference for bidirectional
    
    @Column(name = "entity_property", nullable = false, length = 255)
    private String entityProperty;
    
    @Column(name = "item_order", nullable = false)
    private Integer itemOrder;
    
    @Column(name = "line_type", nullable = false, length = 50)
    private String lineType;  // "FIELD", "SECTION", "SEPARATOR"
}
```

**Parent Entity**:
```java
@Entity
@Table(name = "cdetailsection")
public class CDetailSection extends CEntityOfProject<CDetailSection> {
    
    @OneToMany(mappedBy = "detailSection", 
               cascade = CascadeType.ALL, 
               fetch = FetchType.LAZY, 
               orphanRemoval = true)
    @OrderBy("itemOrder ASC")
    private List<CDetailLines> detailLines = new ArrayList<>();
    
    public void addScreenLine(CDetailLines detailLine) {
        if (detailLine.getItemOrder() == 0) {
            detailLine.setItemOrder(detailLines.size() + 1);
        }
        detailLines.add(detailLine);
        detailLine.setDetailSection(this);  // Maintain bidirectional relationship
    }
}
```

**Key Pattern Characteristics**:
- **Bidirectional** relationship (unlike Comments/Attachments)
- Managed through parent's `addScreenLine()` method
- No standalone service or views
- Used programmatically by initializer services

#### 4. CSprintItem - Sprint Progress Tracking

**Purpose**: Track activities/meetings within sprints with progress and story points.

**Domain Layer**:
```java
@Entity
@Table(name = "csprintitem")
public class CSprintItem extends CEntityDB<CSprintItem> {
    
    @ManyToOne
    @JoinColumn(name = "sprint_id")
    private CSprint sprint;
    
    @OneToOne
    @JoinColumn(name = "activity_id")
    private CActivity activity;
    
    @Column(name = "item_order")
    private Integer itemOrder;
    
    @Column(name = "story_point")
    private Long storyPoint;
    
    // No service, no views - managed via Sprint and Activity
}
```

**Parent Activity Implementation**:
```java
public class CActivity extends CProjectItem<CActivity> 
        implements ISprintableItem {
    
    @OneToOne(fetch = FetchType.EAGER, 
              cascade = CascadeType.ALL, 
              orphanRemoval = true)
    @JoinColumn(name = "sprintitem_id", nullable = true)
    private CSprintItem sprintItem;
    
    @Override
    public CSprintItem getSprintItem() {
        return sprintItem;
    }
    
    @Override
    public void setSprintItem(CSprintItem sprintItem) {
        this.sprintItem = sprintItem;
    }
}
```

**Key Pattern Characteristics**:
- Join table entity (Activity ↔ Sprint)
- Owned by Activity (OneToOne from Activity)
- Referenced by Sprint (OneToMany from Sprint)
- No standalone management
- Managed via Kanban board UI

## Implementation Checklist

When adding a new child entity managed by parents:

### 1. Domain Layer
- [ ] Create entity class extending appropriate base (CEntityDB, CEntityOfCompany, etc.)
- [ ] Add required fields with @AMetaData annotations
- [ ] **DO NOT** add back-reference to parent (keep unidirectional for simplicity)
- [ ] Define entity constants (DEFAULT_COLOR, ENTITY_TITLE_SINGULAR, etc.)

### 2. Interface Layer (Optional)
- [ ] Create `IHas[EntityName]` interface if multiple parents need this child
- [ ] Define getter/setter methods in interface

### 3. Parent Entity
- [ ] Implement interface (if created)
- [ ] Add `@OneToMany` field with proper cascade and orphanRemoval
- [ ] Use `@JoinColumn(name = "parent_id")` for FK in child table
- [ ] Add `@AMetaData` with dataProviderBean and createComponentMethod
- [ ] Implement getter (initialize Set if null)
- [ ] Implement setter

### 4. Service Layer (Minimal)
- [ ] **ONLY** create service if needed for component factory
- [ ] Extend CAbstractService
- [ ] Keep business logic minimal - children managed by parent

### 5. Initializer Pattern
- [ ] **DO NOT** create standalone initializer for child
- [ ] Add section to parent entity initializer
- [ ] Use pattern: `detailSection.addScreenLine(createSection("[Name]"))`
- [ ] Add field line: `detailSection.addScreenLine(createLineFromDefaults(parentClass, "fieldName"))`

### 6. UI Components
- [ ] Create component class if custom rendering needed
- [ ] Component should handle CRUD operations on child collection
- [ ] Register factory method in appropriate service
- [ ] Component receives parent entity and field name

## Anti-Patterns (DO NOT DO)

❌ **Creating standalone service with full CRUD operations**
```java
// WRONG - Too much infrastructure for child entity
@Service
public class CCommentService extends CAbstractService<CComment> {
    public List<CComment> findAll() { ... }
    public CComment save(CComment comment) { ... }
    // etc.
}
```

✅ **Correct - Minimal or no service**
```java
// RIGHT - No service needed, managed by parent
// OR minimal service only for component factory
```

❌ **Creating initializer with standalone view**
```java
// WRONG - Child entities don't get own pages
public class CCommentInitializerService {
    public static void initialize(CProject project, ...) {
        // Creates menu entry and standalone page
    }
}
```

✅ **Correct - Helper method for parent sections**
```java
// RIGHT - Helper to add section to parent views
public class CAttachmentInitializerService {
    public static void addAttachmentsSection(
            CDetailSection detailSection, 
            Class<?> entityClass) {
        // Adds section to PARENT's detail view
    }
}
```

❌ **Adding menu entry**
```java
// WRONG - Child entities don't appear in menu
private static final String menuTitle = "Comments";
private static final boolean showInQuickToolbar = true;
```

✅ **Correct - No menu entries**
```java
// RIGHT - Accessed only through parent
// No menu configuration needed
```

## Sample Data Pattern

Child entities get sample data through parent entity initialization:

```java
public class CIssueInitializerService {
    
    public static void initializeSample(CProject project, boolean minimal) {
        // Create sample issues
        initializeProjectEntity(nameAndDescriptions, 
            issueService, project, minimal,
            (item, index) -> {
                CIssue issue = (CIssue) item;
                
                // Add sample comments
                CComment comment1 = new CComment(
                    "This is a critical issue that needs immediate attention.",
                    getCurrentUser()
                );
                issue.getComments().add(comment1);
                
                CComment comment2 = new CComment(
                    "I've started working on this. Will update progress.",
                    getCurrentUser()
                );
                issue.getComments().add(comment2);
            });
    }
}
```

## Summary

Child entities managed by parents follow these principles:

1. **No Standalone Infrastructure**: No dedicated views, minimal/no service layer
2. **Unidirectional Relationships**: Parent owns child, child has no back-reference (cleaner)
3. **Cascade Operations**: Parent manages lifecycle (cascade ALL, orphanRemoval true)
4. **Component-Based UI**: Custom components handle child entity CRUD in parent forms
5. **Section Integration**: Added to parent detail views via helper methods
6. **Sample Data**: Initialized through parent entity sample data methods

This pattern keeps the codebase clean, reduces complexity, and ensures child entities are only accessible through their proper parent context.
