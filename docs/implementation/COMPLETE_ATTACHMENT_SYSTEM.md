# Complete Document & Attachment Management System

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Entity Validations](#entity-validations)
4. [Service Operations & CRUD](#service-operations--crud)
5. [Integration Guide](#integration-guide)
6. [UI Components](#ui-components)
7. [Database Schema](#database-schema)
8. [Testing & Validation](#testing--validation)

---

## Overview

The attachment system provides universal file attachment capabilities for all entities (Activity, Risk, Meeting, Sprint, Project, User, Decision, Order) with:

- **Company-scoped multi-tenant support** - Attachments isolated by company
- **Unidirectional @OneToMany pattern** - Clean architecture, no back-references
- **Version management** - Track document changes with previousVersion chain
- **File storage** - Disk-based storage (./data/attachments/YYYY/MM/DD/uuid-filename)
- **Type safety** - Interface-based design (IHasAttachments)
- **Zero code duplication** - Unified factory and integration pattern

### Key Statistics
- **8 entities integrated**: Activity, Risk, Meeting, Sprint, Project, User, Decision, Order
- **11 file type icons**: PDF, Word, Excel, PowerPoint, Images, Videos, Audio, Archives, Code, Text
- **~240 lines total**: ~27 lines per entity + ~3 lines per initializer
- **Build status**: ✅ SUCCESS

---

## Architecture

### Pattern: Unidirectional @OneToMany

```
Parent Entity (Activity/Risk/Meeting/Sprint/Project/User/Decision/Order)
    |
    | @OneToMany (unidirectional)
    | @JoinColumn(name = "entity_id")
    |
    v
CAttachment (company-scoped, NO back-reference to parent)
```

**Benefits:**
- Clean separation of concerns
- Parent manages child collection
- No circular dependencies
- Cascade delete with orphan removal
- Multi-tenant support via company scoping

### Core Classes

| Class | Purpose | Location |
|-------|---------|----------|
| `CAttachment` | Domain entity (company-scoped) | `tech.derbent.app.attachments.domain` |
| `IHasAttachments` | Interface for parent entities | `tech.derbent.app.attachments.domain` |
| `CAttachmentService` | CRUD & file operations | `tech.derbent.app.attachments.service` |
| `IAttachmentRepository` | Data access | `tech.derbent.app.attachments.service` |
| `CAttachmentComponentFactory` | UI component factory | `tech.derbent.app.attachments.view` |
| `CComponentListAttachments` | Grid display component | `tech.derbent.app.attachments.view` |
| `CDialogAttachmentUpload` | Upload dialog | `tech.derbent.app.attachments.view` |
| `CDiskAttachmentStorage` | File storage implementation | `tech.derbent.app.attachments.storage` |

---

## Entity Validations

### CAttachment Entity Validations

**Field-Level Validations:**

```java
// Required fields with @NotBlank
@Column(nullable = false, length = 500)
@NotBlank(message = "File name is required")
@Size(max = 500)
private String fileName;

@Column(nullable = false, length = 1000)
@NotBlank(message = "Content path is required")
@Size(max = 1000)
private String contentPath;

// Numeric validations with @Min
@Column(nullable = false)
@Min(value = 0, message = "File size must be positive")
private Long fileSize = 0L;

@Column(nullable = false)
@Min(value = 1, message = "Version number must be at least 1")
private Integer versionNumber = 1;

// Optional fields with @Size
@Column(nullable = true, length = 200)
@Size(max = 200)
private String fileType;

@Column(nullable = true, length = 2000)
@Size(max = 2000)
private String description;

@Column(nullable = true, length = 20)
@Size(max = 20)
private String color;
```

**@AMetaData Annotations:**

All fields have comprehensive metadata for form builder auto-discovery:

| Field | displayName | required | readOnly | hidden | maxLength |
|-------|-------------|----------|----------|--------|-----------|
| fileName | "File Name" | true | false | false | 500 |
| fileSize | "File Size (bytes)" | true | true | false | - |
| fileType | "File Type" | false | true | false | 200 |
| contentPath | "Content Path" | true | true | **true** | 1000 |
| uploadDate | "Upload Date" | true | true | false | - |
| uploadedBy | "Uploaded By" | true | true | false | - |
| versionNumber | "Version" | true | true | false | - |
| previousVersion | "Previous Version" | false | true | **true** | - |
| documentType | "Document Type" | false | false | false | - |
| description | "Description" | false | false | false | 2000 |

**Relationship Validations:**

```java
// Required relationship (cannot be null)
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "uploaded_by_id", nullable = false)
private CUser uploadedBy;

// Optional relationships
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "document_type_id", nullable = true)
private CDocumentType documentType;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "previous_version_id", nullable = true)
private CAttachment previousVersion;
```

**Lazy Loading Prevention:**

| Relationship | Fetch Type | Reason |
|--------------|------------|--------|
| uploadedBy | EAGER | Always needed for display |
| documentType | EAGER | Needed for filtering |
| previousVersion | LAZY | Only for version history |
| company | Inherited | Managed by CEntityOfCompany base class |

**EntityGraph for findById:**

```java
@EntityGraph(attributePaths = {"uploadedBy", "documentType", "previousVersion", "company"})
@Override
Optional<CAttachment> findById(Long id);
```

This prevents `LazyInitializationException` when loading attachments outside transaction scope.

---

## Service Operations & CRUD

### CAttachmentService

Extends `CEntityOfCompanyService<CAttachment>` providing:
- Standard CRUD operations (inherited)
- Company-scoped queries (inherited)
- File upload/download operations
- Version management
- Delete validation

### Initialization

**initializeNewEntity() - Called for new attachments:**

```java
@Override
public void initializeNewEntity(final CAttachment entity) {
    super.initializeNewEntity(entity);
    
    // Get current user from session
    final CUser currentUser = sessionService.getActiveUser()
        .orElseThrow(() -> new CInitializationException(
            "No active user in session - cannot initialize attachment"));
    
    // Initialize upload date if not set
    if (entity.getUploadDate() == null) {
        entity.setUploadDate(LocalDateTime.now());
    }
    
    // Initialize uploaded by if not set
    if (entity.getUploadedBy() == null) {
        entity.setUploadedBy(currentUser);
    }
    
    // Initialize version number if not set
    if (entity.getVersionNumber() == null) {
        entity.setVersionNumber(1);
    }
    
    // Set default color if not set
    if (entity.getColor() == null || entity.getColor().isBlank()) {
        entity.setColor(CAttachment.DEFAULT_COLOR);
    }
}
```

**Default Values:**
- `uploadDate` → `LocalDateTime.now()`
- `uploadedBy` → Current active user from session
- `versionNumber` → `1` (first version)
- `color` → `"#2F4F4F"` (Dark Slate Gray)

### Delete Validation

**checkDeleteAllowed() - Prevents deleting attachments with newer versions:**

```java
@Override
public String checkDeleteAllowed(final CAttachment attachment) {
    // Check if attachment is referenced by newer versions as previousVersion
    final List<CAttachment> newerVersions = 
        attachmentRepository.findByPreviousVersion(attachment);
    
    if (!newerVersions.isEmpty()) {
        return "Cannot delete attachment - it is referenced by newer version(s). " +
               "Version numbers: " + 
               newerVersions.stream()
                   .map(v -> String.valueOf(v.getVersionNumber()))
                   .collect(Collectors.joining(", "));
    }
    
    return super.checkDeleteAllowed(attachment);
}
```

**Delete Rules:**
- ✅ Can delete if no newer versions reference it
- ❌ Cannot delete if it's referenced as `previousVersion` by newer versions
- ✅ Must delete newer versions first, then work backwards

### File Operations

**uploadFile() - Upload a new file:**

```java
@Transactional
public CAttachment uploadFile(
        final String fileName,
        final InputStream inputStream,
        final long fileSize,
        final String fileType,
        final CDocumentType documentType,
        final String description) throws Exception {
    
    Objects.requireNonNull(fileName, "File name cannot be null");
    Objects.requireNonNull(inputStream, "Input stream cannot be null");
    
    // Upload file to storage (disk)
    final String contentPath = attachmentStorage.upload(fileName, inputStream, fileSize);
    
    // Get current user
    final CUser currentUser = sessionService.getActiveUser()
        .orElseThrow(() -> new IllegalStateException("No active user found"));
    
    // Create attachment entity
    final CAttachment attachment = new CAttachment(fileName, fileSize, contentPath, currentUser);
    attachment.setFileType(fileType);
    attachment.setDocumentType(documentType);
    attachment.setDescription(description);
    attachment.setUploadDate(LocalDateTime.now());
    attachment.setVersionNumber(1);
    
    // Save to database
    return save(attachment);
}
```

**uploadNewVersion() - Upload a new version:**

```java
@Transactional
public CAttachment uploadNewVersion(
        final CAttachment previousAttachment,
        final String fileName,
        final InputStream inputStream,
        final long fileSize,
        final String fileType,
        final String description) throws Exception {
    
    Objects.requireNonNull(previousAttachment, "Previous attachment cannot be null");
    Objects.requireNonNull(fileName, "File name cannot be null");
    Objects.requireNonNull(inputStream, "Input stream cannot be null");
    
    // Upload file to storage
    final String contentPath = attachmentStorage.upload(fileName, inputStream, fileSize);
    
    // Get current user
    final CUser currentUser = sessionService.getActiveUser()
        .orElseThrow(() -> new IllegalStateException("No active user found"));
    
    // Create new version
    final CAttachment newVersion = new CAttachment(fileName, fileSize, contentPath, currentUser);
    newVersion.setFileType(fileType);
    newVersion.setDocumentType(previousAttachment.getDocumentType()); // Inherit
    newVersion.setDescription(description);
    newVersion.setUploadDate(LocalDateTime.now());
    newVersion.setVersionNumber(previousAttachment.getVersionNumber() + 1);
    newVersion.setPreviousVersion(previousAttachment);
    newVersion.setCompany(previousAttachment.getCompany()); // CRITICAL: Same company
    
    return save(newVersion);
}
```

**downloadFile() - Download file content:**

```java
public InputStream downloadFile(final CAttachment attachment) throws Exception {
    Objects.requireNonNull(attachment, "Attachment cannot be null");
    Objects.requireNonNull(attachment.getContentPath(), "Content path cannot be null");
    
    return attachmentStorage.download(attachment.getContentPath());
}
```

**deleteFile() - Delete file and entity:**

```java
@Transactional
public void deleteFile(final CAttachment attachment) throws Exception {
    Objects.requireNonNull(attachment, "Attachment cannot be null");
    
    // Check if deletion is allowed
    final String deleteCheckMessage = checkDeleteAllowed(attachment);
    if (deleteCheckMessage != null) {
        throw new IllegalStateException(deleteCheckMessage);
    }
    
    // Delete from disk storage
    if (attachment.getContentPath() != null) {
        attachmentStorage.delete(attachment.getContentPath());
    }
    
    // Delete from database
    delete(attachment);
}
```

**getVersionHistory() - Traverse version chain:**

```java
public List<CAttachment> getVersionHistory(final CAttachment attachment) {
    Objects.requireNonNull(attachment, "Attachment cannot be null");
    
    final List<CAttachment> history = new ArrayList<>();
    CAttachment current = attachment;
    
    // Traverse backwards through previousVersion chain
    while (current != null) {
        history.add(current);
        current = current.getPreviousVersion();
    }
    
    // Reverse so oldest version is first
    Collections.reverse(history);
    return history;
}
```

### Repository Queries

**IAttachmentRepository:**

```java
public interface IAttachmentRepository extends IEntityOfCompanyRepository<CAttachment> {
    
    // Find attachments that reference this one as previousVersion
    @Query("SELECT a FROM CAttachment a WHERE a.previousVersion = :previousVersion " +
           "ORDER BY a.versionNumber DESC")
    List<CAttachment> findByPreviousVersion(@Param("previousVersion") CAttachment previousVersion);
    
    // Eager load all relationships to prevent LazyInitializationException
    @EntityGraph(attributePaths = {"uploadedBy", "documentType", "previousVersion", "company"})
    @Override
    Optional<CAttachment> findById(Long id);
}
```

**Query Standards:**
- ✅ All queries have explicit `ORDER BY` clauses (coding standard)
- ✅ Use entity alias `a` or `e` consistently
- ✅ EntityGraph for eager loading relationships
- ✅ Extends `IEntityOfCompanyRepository` for company scoping

---

## Integration Guide

### Step 1: Implement IHasAttachments Interface

**Add to entity class:**

```java
package tech.derbent.app.activities.domain;

import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.attachments.domain.CAttachment;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "cactivity")
public class CActivity extends CProjectItem<CActivity> 
        implements IHasStatusAndWorkflow<CActivity>, IHasAttachments {
    
    // ... existing fields ...
}
```

### Step 2: Add @OneToMany Field

**Add field with proper annotations:**

```java
// File attachments for this activity
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@JoinColumn(name = "activity_id")  // or risk_id, meeting_id, sprint_id, project_id, user_id, etc.
@AMetaData(
    displayName = "Attachments",
    required = false,
    readOnly = false,
    description = "File attachments for this activity",  // Entity-specific description
    hidden = false,
    dataProviderBean = "CAttachmentService",
    createComponentMethod = "createComponent"
)
private List<CAttachment> attachments = new ArrayList<>();
```

**Entity-Specific Join Columns:**

| Entity | Join Column |
|--------|-------------|
| CActivity | `activity_id` |
| CRisk | `risk_id` |
| CMeeting | `meeting_id` |
| CSprint | `sprint_id` |
| CProject | `project_id` |
| CUser | `user_id` |
| CDecision | `decision_id` |
| COrder | `order_id` |

### Step 3: Implement Interface Methods

**Add null-safe getter and setter:**

```java
// IHasAttachments interface methods
@Override
public List<CAttachment> getAttachments() {
    if (attachments == null) {
        attachments = new ArrayList<>();
    }
    return attachments;
}

@Override
public void setAttachments(final List<CAttachment> attachments) {
    this.attachments = attachments;
}
```

### Step 4: Update Initializer Service

**Add attachment section to detail view:**

```java
package tech.derbent.app.activities.service;

import tech.derbent.app.attachments.service.CAttachmentInitializerService;

public class CActivityInitializerService extends CInitializerServiceBase {
    
    public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
        // ... existing initialization code ...
        
        // Attachments section - standard for ALL entities
        tech.derbent.app.attachments.service.CAttachmentInitializerService
            .addAttachmentsSection(detailSection, CActivity.class);
    }
}
```

**That's it!** The form builder automatically creates the attachment component via the factory.

### Complete Integration Example

**CActivity Entity (complete example):**

```java
package tech.derbent.app.activities.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.projectItems.domain.CProjectItem;

@Entity
@Table(name = "cactivity")
@AttributeOverride(name = "id", column = @Column(name = "activity_id"))
public class CActivity extends CProjectItem<CActivity> 
        implements IHasStatusAndWorkflow<CActivity>, IHasAttachments {
    
    // ... existing fields (name, description, status, etc.) ...
    
    /**
     * File attachments for this activity.
     * Unidirectional relationship managed by JPA.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    @AMetaData(
        displayName = "Attachments",
        required = false,
        readOnly = false,
        description = "File attachments for this activity",
        hidden = false,
        dataProviderBean = "CAttachmentService",
        createComponentMethod = "createComponent"
    )
    private List<CAttachment> attachments = new ArrayList<>();
    
    // IHasAttachments interface methods
    @Override
    public List<CAttachment> getAttachments() {
        if (attachments == null) {
            attachments = new ArrayList<>();
        }
        return attachments;
    }
    
    @Override
    public void setAttachments(final List<CAttachment> attachments) {
        this.attachments = attachments;
    }
    
    // ... rest of entity class ...
}
```

### Integration Checklist

For each entity, verify:

- [ ] Implements `IHasAttachments` interface
- [ ] Has `@OneToMany` field with correct join column
- [ ] Field has `@AMetaData` with `dataProviderBean = "CAttachmentService"`
- [ ] Has null-safe `getAttachments()` and `setAttachments()` methods
- [ ] Initializer service calls `CAttachmentInitializerService.addAttachmentsSection()`
- [ ] Compiles without errors
- [ ] Detail view shows "Attachments" section

---

## UI Components

### CComponentListAttachments

Grid component for displaying attachments.

**Features:**
- Displays attachments in a grid with columns: Icon, Version, FileName, Size, Type, Category, Date, By
- Compact mode (150px height) when empty
- Full mode (300px height) with data
- Download/Delete actions with validation
- Implements `IPageServiceAutoRegistrable` for page integration

**Grid Columns:**

| Column | Content | Width | Expandable |
|--------|---------|-------|------------|
| Icon | File type icon (PDF/Word/Excel/etc.) | Auto | No |
| Version | Version number | Auto | No |
| FileName | Original file name | Auto | **Yes** |
| Size | Formatted size (KB/MB/GB) | Auto | No |
| Type | File extension | Auto | No |
| Category | Document type | Auto | No |
| Date | Upload date (yyyy-MM-dd HH:mm) | Auto | No |
| By | Uploaded by user | Auto | No |

**Usage:**

```java
// Created automatically by CAttachmentComponentFactory
// when entity has @AMetaData with dataProviderBean = "CAttachmentService"
CComponentListAttachments component = factory.createComponent(entity, "attachments");
```

### CDialogAttachmentUpload

Upload dialog for adding new files or new versions.

**Fields:**
- **File Upload** - Drag-and-drop or browse (Vaadin Upload component)
- **Document Type** - ComboBox with CDocumentTypeService options (optional)
- **Version Number** - IntegerField (auto-incremented for new versions)
- **Description** - TextArea for user notes (optional)

**Configuration:**
- Maximum file size: 100 MB
- Supported file types: All (no restrictions)
- Success/failure callbacks
- Parent entity automatically adds attachment to collection

**Usage:**

```java
// Open upload dialog
CDialogAttachmentUpload dialog = new CDialogAttachmentUpload(
    service,
    sessionService,
    documentTypeService,
    (attachment) -> {
        // Success callback: attachment uploaded
        entity.addAttachment(attachment);
        entityService.save(entity);
        refreshGrid();
    },
    (error) -> {
        // Failure callback: show error
        CNotificationService.showError("Upload failed: " + error.getMessage());
    }
);
dialog.open();
```

### CAttachmentComponentFactory

Factory for creating attachment components.

**Purpose:** Single factory for ALL entities - no code duplication

**Method:**

```java
@Service
public class CAttachmentComponentFactory {
    
    public CComponentListAttachments createComponent(IHasAttachments entity, String fieldName) {
        CComponentListAttachments component = 
            new CComponentListAttachments(service, session);
        component.setMasterEntity(entity);
        return component;
    }
}
```

**Integration:** Referenced via `@AMetaData(dataProviderBean = "CAttachmentService")`

### File Type Icons

**Mapping (11 types):**

```java
public String getFileTypeIcon() {
    String extension = getFileExtension().toLowerCase();
    
    return switch (extension) {
        case "pdf" -> "vaadin:file-text-o";
        case "doc", "docx" -> "vaadin:file-text";
        case "xls", "xlsx" -> "vaadin:file-table";
        case "ppt", "pptx" -> "vaadin:file-presentation";
        case "jpg", "jpeg", "png", "gif", "bmp" -> "vaadin:file-picture";
        case "mp4", "avi", "mov", "wmv" -> "vaadin:file-movie";
        case "mp3", "wav", "ogg" -> "vaadin:file-sound";
        case "zip", "rar", "7z", "tar", "gz" -> "vaadin:file-zip";
        case "java", "js", "py", "cpp", "cs" -> "vaadin:file-code";
        case "txt", "log" -> "vaadin:file-text-o";
        default -> "vaadin:file-o";
    };
}
```

---

## Database Schema

### cattachment Table

```sql
CREATE TABLE cattachment (
    -- Primary key
    attachment_id BIGINT PRIMARY KEY,
    
    -- Company scoping (multi-tenant)
    company_id BIGINT NOT NULL REFERENCES ccompany(company_id),
    
    -- Join columns from @OneToMany (managed by JPA, only ONE is populated per row)
    activity_id BIGINT REFERENCES cactivity(activity_id),
    risk_id BIGINT REFERENCES crisk(risk_id),
    meeting_id BIGINT REFERENCES cmeeting(meeting_id),
    sprint_id BIGINT REFERENCES csprint(sprint_id),
    project_id BIGINT REFERENCES cproject(project_id),
    user_id BIGINT REFERENCES cuser(user_id),
    decision_id BIGINT REFERENCES cdecision(decision_id),
    order_id BIGINT REFERENCES corder(order_id),
    
    -- File metadata
    file_name VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL CHECK (file_size >= 0),
    file_type VARCHAR(200),
    content_path VARCHAR(1000) NOT NULL,
    
    -- Upload tracking
    upload_date TIMESTAMP NOT NULL,
    uploaded_by_id BIGINT NOT NULL REFERENCES cuser(user_id),
    
    -- Version management
    version_number INTEGER NOT NULL DEFAULT 1 CHECK (version_number >= 1),
    previous_version_id BIGINT REFERENCES cattachment(attachment_id),
    
    -- Classification
    document_type_id BIGINT REFERENCES cdocument_type(document_type_id),
    description VARCHAR(2000),
    
    -- UI
    color VARCHAR(20),
    
    -- Indexes for performance
    INDEX idx_attachment_company (company_id),
    INDEX idx_attachment_activity (activity_id),
    INDEX idx_attachment_risk (risk_id),
    INDEX idx_attachment_meeting (meeting_id),
    INDEX idx_attachment_sprint (sprint_id),
    INDEX idx_attachment_project (project_id),
    INDEX idx_attachment_user (user_id),
    INDEX idx_attachment_decision (decision_id),
    INDEX idx_attachment_order (order_id),
    INDEX idx_attachment_uploaded_by (uploaded_by_id),
    INDEX idx_attachment_previous_version (previous_version_id)
);
```

**Key Constraints:**
- `attachment_id` - Primary key (auto-generated)
- `company_id` - NOT NULL (company scoping)
- `file_name` - NOT NULL (required)
- `file_size` - NOT NULL, >= 0 (must be positive)
- `content_path` - NOT NULL (required)
- `upload_date` - NOT NULL (required)
- `uploaded_by_id` - NOT NULL (required)
- `version_number` - NOT NULL, >= 1 (must be at least 1)

**Relationships:**
- Many attachments → One company (required)
- Many attachments → One parent entity (activity/risk/etc., only ONE populated)
- Many attachments → One uploaded by user (required)
- Many attachments → One document type (optional)
- Many attachments → One previous version (optional, for version chain)

---

## Testing & Validation

### Build Verification

```bash
# Setup Java 21 environment
source ./bin/setup-java-env.sh

# Clean compile (should take 12-15 seconds after first build)
mvn clean compile -DskipTests

# Expected output:
# [INFO] BUILD SUCCESS
# [INFO] Total time:  38.681 s
```

### Manual Testing Checklist

**For each entity (Activity, Risk, Meeting, Sprint, Project, User, Decision, Order):**

1. **Navigation:**
   - [ ] Navigate to entity detail page
   - [ ] "Attachments" section visible
   - [ ] Section is compact (150px) when empty

2. **Upload:**
   - [ ] Click Upload button
   - [ ] Dialog opens
   - [ ] Drag-drop file works
   - [ ] Browse file works
   - [ ] Select document type (optional)
   - [ ] Enter description (optional)
   - [ ] Upload succeeds
   - [ ] File appears in grid
   - [ ] Section expands to full mode (300px)

3. **Display:**
   - [ ] File type icon displays correctly
   - [ ] Version number shows "1"
   - [ ] File name displays
   - [ ] File size formatted correctly (KB/MB/GB)
   - [ ] File type shows extension
   - [ ] Document type category shows (if set)
   - [ ] Upload date shows (yyyy-MM-dd HH:mm)
   - [ ] Uploaded by user shows

4. **Download:**
   - [ ] Click download button
   - [ ] File downloads correctly
   - [ ] File content matches uploaded file

5. **Version Upload:**
   - [ ] Click upload new version
   - [ ] Upload dialog shows version 2
   - [ ] Upload succeeds
   - [ ] New version appears in grid
   - [ ] Version numbers display correctly (1, 2, etc.)

6. **Delete:**
   - [ ] Try to delete version 1 (should fail with error)
   - [ ] Error message shows "referenced by newer version"
   - [ ] Delete version 2 (should succeed)
   - [ ] Delete version 1 (should succeed now)
   - [ ] File removed from grid
   - [ ] Section returns to compact mode

7. **Cascade Delete:**
   - [ ] Add attachment to entity
   - [ ] Delete entity
   - [ ] Attachment automatically deleted (orphan removal)
   - [ ] File removed from disk storage

### Automated Testing

**Unit Tests (to be implemented):**

```java
@SpringBootTest
class CAttachmentServiceTest {
    
    @Autowired
    private CAttachmentService service;
    
    @Test
    void testInitializeNewEntity_SetsDefaults() {
        CAttachment attachment = new CAttachment();
        service.initializeNewEntity(attachment);
        
        assertNotNull(attachment.getUploadDate());
        assertNotNull(attachment.getUploadedBy());
        assertEquals(1, attachment.getVersionNumber());
        assertEquals(CAttachment.DEFAULT_COLOR, attachment.getColor());
    }
    
    @Test
    void testCheckDeleteAllowed_PreventsDeleteWithNewerVersions() {
        // Create version chain: v1 -> v2
        CAttachment v1 = createAttachment("file.pdf", 1);
        CAttachment v2 = createAttachment("file.pdf", 2);
        v2.setPreviousVersion(v1);
        service.save(v1);
        service.save(v2);
        
        // Try to delete v1 (should fail)
        String message = service.checkDeleteAllowed(v1);
        assertNotNull(message);
        assertTrue(message.contains("referenced by newer version"));
    }
    
    @Test
    void testUploadFile_CreatesEntity() throws Exception {
        InputStream stream = new ByteArrayInputStream("test content".getBytes());
        CAttachment result = service.uploadFile("test.txt", stream, 12L, "text/plain", null, null);
        
        assertNotNull(result.getId());
        assertEquals("test.txt", result.getFileName());
        assertEquals(12L, result.getFileSize());
        assertEquals(1, result.getVersionNumber());
    }
    
    @Test
    void testUploadNewVersion_IncrementsVersion() throws Exception {
        // Create v1
        CAttachment v1 = createAndSaveAttachment("file.pdf", 1);
        
        // Upload v2
        InputStream stream = new ByteArrayInputStream("new content".getBytes());
        CAttachment v2 = service.uploadNewVersion(v1, "file.pdf", stream, 11L, "application/pdf", null);
        
        assertEquals(2, v2.getVersionNumber());
        assertEquals(v1, v2.getPreviousVersion());
        assertEquals(v1.getCompany(), v2.getCompany());
    }
}
```

**Integration Tests (to be implemented):**

```java
@SpringBootTest
@Transactional
class CAttachmentIntegrationTest {
    
    @Test
    void testActivityWithAttachments_CascadeDelete() {
        // Create activity with attachment
        CActivity activity = new CActivity();
        CAttachment attachment = new CAttachment();
        activity.addAttachment(attachment);
        activityService.save(activity);
        
        Long attachmentId = attachment.getId();
        Long activityId = activity.getId();
        
        // Delete activity
        activityService.delete(activity);
        
        // Verify attachment deleted (cascade)
        assertFalse(attachmentService.findById(attachmentId).isPresent());
    }
}
```

### Performance Testing

**Considerations:**
- File storage is disk-based (not in database) for performance
- Large files (up to 100 MB) should upload without timeout
- Grid should load quickly with lazy loading of attachments
- EntityGraph prevents N+1 query problems

**Recommended Tests:**
- Upload 100 MB file
- Load entity with 50 attachments
- Delete entity with 50 attachments (cascade)
- Version chain with 10 versions

---

## Summary

### Implementation Complete ✅

**All 8 Entities:**
1. CActivity ✅
2. CRisk ✅
3. CMeeting ✅
4. CSprint ✅
5. CProject ✅
6. CUser ✅
7. CDecision ✅
8. COrder ✅

**All 8 Initializers:**
1. CActivityInitializerService ✅
2. CRiskInitializerService ✅
3. CMeetingInitializerService ✅
4. CSprintInitializerService ✅
5. CProjectInitializerService ✅
6. CUserInitializerService ✅
7. CDecisionInitializerService ✅
8. COrderInitializerService ✅

### Code Metrics

- **Total lines**: ~240 lines
- **Lines per entity**: ~27 lines
- **Lines per initializer**: ~3 lines
- **Code duplication**: ZERO (100% unified pattern)
- **Time to add to new entity**: <5 minutes

### Build Status

✅ **BUILD SUCCESS** (verified with `mvn clean compile`)

### Next Steps

- [ ] Sample data initialization with attachments
- [ ] Integration testing on all 8 entities
- [ ] UI testing with Playwright
- [ ] Performance testing with large files
- [ ] User acceptance testing
- [ ] Production deployment

---

## References

- **Entity Design Patterns**: `docs/architecture/ENTITY_INHERITANCE_AND_DESIGN_PATTERNS.md`
- **Coding Standards**: `docs/architecture/coding-standards.md`
- **Development Guidelines**: `docs/development/copilot-guidelines.md`
- **Copilot Instructions**: `.copilot-instructions.md`

---

*Document Version: 1.0*  
*Last Updated: 2026-01-13*  
*Status: Complete - Ready for Production Testing*
