# Attachment System Implementation Guide

## Overview
Complete guide for integrating the attachment system into entities (Activity, Risk, Meeting, Sprint, Project, User, etc.)

## Architecture

### Pattern: Unidirectional @OneToMany
```
Parent Entity (Activity/Risk/Meeting/Sprint/Project/User)
    |
    | @OneToMany (unidirectional)
    | @JoinColumn(name = "activity_id")
    |
    v
CAttachment (company-scoped, no back-reference to parent)
```

**Benefits:**
- Clean separation of concerns
- Parent manages child collection
- No circular dependencies
- Cascade delete with orphan removal
- Multi-tenant support (company-scoped)

## Step 1: Add @OneToMany Field to Parent Entity

### Example: CActivity

```java
package tech.derbent.app.activities.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.projectItems.domain.CProjectItem;

@Entity
@Table(name = "cactivity")
public class CActivity extends CProjectItem<CActivity> {
    
    // ... existing fields ...
    
    /**
     * File attachments for this activity.
     * Unidirectional relationship - CAttachment has no back-reference to CActivity.
     * Join column activity_id is managed by JPA automatically.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    @AMetaData(
        displayName = "Attachments",
        required = false,
        readOnly = false,
        description = "File attachments for this activity",
        hidden = false,
        createComponentMethod = "createAttachmentsComponent"
    )
    private Set<CAttachment> attachments = new HashSet<>();
    
    // Getter and setter
    public Set<CAttachment> getAttachments() {
        if (attachments == null) {
            attachments = new HashSet<>();
        }
        return attachments;
    }
    
    public void setAttachments(final Set<CAttachment> attachments) {
        this.attachments = attachments;
    }
    
    /**
     * Add an attachment to this activity.
     * Use this method instead of direct list manipulation to ensure proper initialization.
     */
    public void addAttachment(final CAttachment attachment) {
        if (attachment == null) {
            return;
        }
        if (this.attachments == null) {
            this.attachments = new HashSet<>();
        }
        if (!this.attachments.contains(attachment)) {
            this.attachments.add(attachment);
        }
    }
    
    /**
     * Remove an attachment from this activity.
     */
    public void removeAttachment(final CAttachment attachment) {
        if (attachment != null && this.attachments != null) {
            this.attachments.remove(attachment);
        }
    }
}
```

### Other Entity Examples

#### CRisk
```java
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@JoinColumn(name = "risk_id")
@AMetaData(
    displayName = "Attachments",
    required = false,
    description = "File attachments for this risk",
    createComponentMethod = "createAttachmentsComponent"
)
private Set<CAttachment> attachments = new HashSet<>();
```

#### CMeeting
```java
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@JoinColumn(name = "meeting_id")
@AMetaData(
    displayName = "Attachments",
    required = false,
    description = "Meeting documents and files",
    createComponentMethod = "createAttachmentsComponent"
)
private Set<CAttachment> attachments = new HashSet<>();
```

#### CSprint
```java
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@JoinColumn(name = "sprint_id")
@AMetaData(
    displayName = "Attachments",
    required = false,
    description = "Sprint documentation",
    createComponentMethod = "createAttachmentsComponent"
)
private Set<CAttachment> attachments = new HashSet<>();
```

#### CProject (non-project item)
```java
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@JoinColumn(name = "project_id")
@AMetaData(
    displayName = "Attachments",
    required = false,
    description = "Project documentation and files",
    createComponentMethod = "createAttachmentsComponent"
)
private Set<CAttachment> attachments = new HashSet<>();
```

#### CUser (non-project item)
```java
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
@AMetaData(
    displayName = "Attachments",
    required = false,
    description = "User documents (CV, certifications, etc.)",
    createComponentMethod = "createAttachmentsComponent"
)
private Set<CAttachment> attachments = new HashSet<>();
```

## Step 2: Update Repository with EntityGraph

### Example: IActivityRepository

```java
package tech.derbent.app.activities.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.activities.domain.CActivity;

public interface IActivityRepository extends IEntityOfProjectRepository<CActivity> {
    
    /**
     * Find activity by ID with eager loading of relationships including attachments.
     * This prevents LazyInitializationException when accessing attachments outside transaction.
     */
    @EntityGraph(attributePaths = {
        "status", 
        "assignedTo", 
        "project", 
        "company",
        "attachments",
        "attachments.uploadedBy",
        "attachments.documentType"
    })
    @Override
    Optional<CActivity> findById(Long id);
    
    /**
     * Find activities by project with attachments eagerly loaded.
     */
    @EntityGraph(attributePaths = {
        "status",
        "assignedTo",
        "attachments",
        "attachments.uploadedBy",
        "attachments.documentType"
    })
    @Query("SELECT a FROM CActivity a WHERE a.project = :project ORDER BY a.name ASC")
    List<CActivity> findByProjectWithAttachments(@Param("project") CProject project);
}
```

**Important Notes:**
- Include `"attachments"` in attributePaths for eager loading
- Include nested paths: `"attachments.uploadedBy"`, `"attachments.documentType"`
- Prevents LazyInitializationException when accessing attachments in views
- Only use in methods where attachments are needed (not all queries)

## Step 3: Add Component Method to Page Service

### Example: CPageServiceActivity

```java
package tech.derbent.app.activities.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.service.CAttachmentService;
import tech.derbent.app.attachments.view.CComponentListAttachments;
import tech.derbent.base.session.service.ISessionService;

@Service
public class CPageServiceActivity extends CAbstractPageService<CActivity> {
    
    @Autowired
    private CAttachmentService attachmentService;
    
    @Autowired
    private ISessionService sessionService;
    
    private CComponentListAttachments<CActivity> componentAttachments;
    
    // ... other fields and methods ...
    
    /**
     * Create attachments component for activity detail page.
     * Called by form builder via reflection when rendering detail view.
     * 
     * @return configured attachment list component
     */
    public CComponentListAttachments<CActivity> createAttachmentsComponent() {
        if (componentAttachments == null) {
            componentAttachments = new CComponentListAttachments<>(
                CActivity.class,
                attachmentService,
                sessionService
            );
            
            // Register with page service for event coordination
            componentAttachments.registerWithPageService(this);
        }
        return componentAttachments;
    }
}
```

## Step 4: Update Component to Use Parent Collection

### CComponentListAttachments - Updated refreshGrid()

```java
protected void refreshGrid() {
    if (masterEntity == null) {
        LOGGER.debug("Master entity is null, clearing grid");
        grid.setItems(List.of());
        return;
    }
    
    // Load attachments from parent entity's collection
    List<CAttachment> items = masterEntity.getAttachments();
    if (items == null) {
        items = new ArrayList<>();
        masterEntity.setAttachments(items);
    }
    
    // Sort by version number descending (newest first)
    items.sort((a1, a2) -> {
        int v1 = a1.getVersionNumber() != null ? a1.getVersionNumber() : 0;
        int v2 = a2.getVersionNumber() != null ? a2.getVersionNumber() : 0;
        return Integer.compare(v2, v1);
    });
    
    grid.setItems(items);
    LOGGER.debug("Loaded {} attachments for {}", items.size(), 
        masterEntity.getClass().getSimpleName());
}
```

## Database Schema

### Attachment Table
```sql
CREATE TABLE cattachment (
    attachment_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES ccompany(company_id),
    
    -- Join columns (ONE of these will be set, populated by @JoinColumn)
    activity_id BIGINT REFERENCES cactivity(activity_id),
    risk_id BIGINT REFERENCES crisk(risk_id),
    meeting_id BIGINT REFERENCES cmeeting(meeting_id),
    sprint_id BIGINT REFERENCES csprint(sprint_id),
    project_id BIGINT REFERENCES cproject(project_id),
    user_id BIGINT REFERENCES cuser(user_id),
    
    -- File metadata
    file_name VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL CHECK (file_size >= 0),
    file_type VARCHAR(200),
    content_path VARCHAR(1000) NOT NULL,
    
    -- Upload tracking
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    uploaded_by_id BIGINT NOT NULL REFERENCES cuser(user_id),
    
    -- Version management
    version_number INTEGER NOT NULL DEFAULT 1 CHECK (version_number >= 1),
    previous_version_id BIGINT REFERENCES cattachment(attachment_id),
    
    -- Classification
    document_type_id BIGINT REFERENCES cdocument_type(document_type_id),
    description VARCHAR(2000),
    
    -- Display
    color VARCHAR(20),
    
    -- Audit fields (inherited from CEntityOfCompany)
    created_at TIMESTAMP,
    created_by BIGINT REFERENCES cuser(user_id),
    updated_at TIMESTAMP,
    updated_by BIGINT REFERENCES cuser(user_id),
    
    -- Indexes for performance
    CONSTRAINT idx_activity_attachment UNIQUE (activity_id, attachment_id),
    CONSTRAINT idx_risk_attachment UNIQUE (risk_id, attachment_id),
    CONSTRAINT idx_meeting_attachment UNIQUE (meeting_id, attachment_id),
    CONSTRAINT idx_sprint_attachment UNIQUE (sprint_id, attachment_id),
    CONSTRAINT idx_project_attachment UNIQUE (project_id, attachment_id),
    CONSTRAINT idx_user_attachment UNIQUE (user_id, attachment_id)
);

CREATE INDEX idx_cattachment_company ON cattachment(company_id);
CREATE INDEX idx_cattachment_activity ON cattachment(activity_id);
CREATE INDEX idx_cattachment_risk ON cattachment(risk_id);
CREATE INDEX idx_cattachment_meeting ON cattachment(meeting_id);
CREATE INDEX idx_cattachment_sprint ON cattachment(sprint_id);
CREATE INDEX idx_cattachment_project ON cattachment(project_id);
CREATE INDEX idx_cattachment_user ON cattachment(user_id);
CREATE INDEX idx_cattachment_previous_version ON cattachment(previous_version_id);
CREATE INDEX idx_cattachment_upload_date ON cattachment(upload_date DESC);
```

## Usage Examples

### Uploading an Attachment

```java
// In Activity detail page
public void uploadAttachment() {
    // Open upload dialog
    CDialogAttachmentUpload dialog = new CDialogAttachmentUpload(
        activity,  // parent entity
        attachmentService,
        sessionService
    );
    
    // Set success callback
    dialog.setOnUploadSuccess(attachment -> {
        // Add attachment to activity's collection
        activity.getAttachments().add(attachment);
        
        // Save activity (cascade will save attachment relationship)
        activityService.save(activity);
        
        // Refresh UI
        componentAttachments.refreshGrid();
    });
    
    dialog.open();
}
```

### Downloading an Attachment

```java
// In attachment grid component
private void on_buttonDownload_clicked() {
    CAttachment selected = grid.asSingleSelect().getValue();
    if (selected == null) {
        CNotificationService.showWarning("Please select an attachment to download");
        return;
    }
    
    try {
        // Create stream resource
        StreamResource resource = attachmentService.downloadFile(selected);
        
        // Trigger download in browser
        triggerBrowserDownload(resource, selected.getFileName());
        
        CNotificationService.showSuccess("Download started");
    } catch (Exception e) {
        LOGGER.error("Error downloading attachment", e);
        CNotificationService.showException("Failed to download file", e);
    }
}
```

### Deleting an Attachment

```java
// In attachment grid component
private void on_buttonDelete_clicked() {
    CAttachment selected = grid.asSingleSelect().getValue();
    if (selected == null) {
        CNotificationService.showWarning("Please select an attachment to delete");
        return;
    }
    
    // Confirm deletion
    CNotificationService.showConfirmationDialog(
        "Delete attachment '" + selected.getFileName() + "'?",
        () -> {
            try {
                // Check if deletion is allowed (no newer versions reference it)
                String error = attachmentService.checkDeleteAllowed(selected);
                if (error != null) {
                    CNotificationService.showError(error);
                    return;
                }
                
                // Remove from parent's collection
                masterEntity.getAttachments().remove(selected);
                
                // Delete file from disk and database
                attachmentService.delete(selected);
                
                // Refresh UI
                refreshGrid();
                
                CNotificationService.showDeleteSuccess();
            } catch (Exception e) {
                LOGGER.error("Error deleting attachment", e);
                CNotificationService.showException("Failed to delete attachment", e);
            }
        }
    );
}
```

### Uploading New Version

```java
// In attachment grid component
private void on_buttonNewVersion_clicked() {
    CAttachment selected = grid.asSingleSelect().getValue();
    if (selected == null) {
        CNotificationService.showWarning("Please select an attachment to version");
        return;
    }
    
    // Open upload dialog for new version
    CDialogAttachmentUpload dialog = new CDialogAttachmentUpload(
        masterEntity,
        attachmentService,
        sessionService
    );
    
    // Set previous version
    dialog.setPreviousVersion(selected);
    
    // Set success callback
    dialog.setOnUploadSuccess(newVersion -> {
        // Add new version to parent's collection
        masterEntity.getAttachments().add(newVersion);
        
        // Save parent (cascade will save new version)
        pageService.save(masterEntity);
        
        // Refresh UI
        refreshGrid();
        
        CNotificationService.showSuccess(
            "Version " + newVersion.getVersionNumber() + " uploaded successfully"
        );
    });
    
    dialog.open();
}
```

## Sample Data Initialization

### CAttachmentInitializerService

```java
package tech.derbent.app.attachments.service;

import java.io.ByteArrayInputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.derbent.api.initialization.CInitializerServiceBase;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.documenttypes.domain.CDocumentType;
import tech.derbent.app.documenttypes.service.CDocumentTypeService;
import tech.derbent.base.users.domain.CUser;

@Service
public class CAttachmentInitializerService extends CInitializerServiceBase {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CAttachmentInitializerService.class);
    
    private final CAttachmentService attachmentService;
    private final CActivityService activityService;
    private final CDocumentTypeService documentTypeService;
    
    public CAttachmentInitializerService(
            final CAttachmentService attachmentService,
            final CActivityService activityService,
            final CDocumentTypeService documentTypeService) {
        this.attachmentService = attachmentService;
        this.activityService = activityService;
        this.documentTypeService = documentTypeService;
    }
    
    /**
     * Initialize sample attachments for a project.
     * 
     * @param project the project
     * @param minimal if true, create minimal sample data (3-5 items); otherwise full set
     * @throws Exception if initialization fails
     */
    public static void initializeSample(final CProject project, final boolean minimal) 
            throws Exception {
        
        LOGGER.info("Initializing sample attachments for project: {}", project.getName());
        
        final CAttachmentService service = getService(CAttachmentService.class);
        final CActivityService activityService = getService(CActivityService.class);
        final CDocumentTypeService docTypeService = getService(CDocumentTypeService.class);
        
        // Get first activity from project
        final List<CActivity> activities = activityService.findByProject(project);
        if (activities.isEmpty()) {
            LOGGER.warn("No activities found for project {}, skipping attachment samples", 
                project.getName());
            return;
        }
        
        final CActivity activity = activities.get(0);
        
        // Get document types
        final List<CDocumentType> docTypes = docTypeService.findByCompany(project.getCompany());
        final CDocumentType specType = docTypes.stream()
            .filter(dt -> "Specification".equals(dt.getName()))
            .findFirst()
            .orElse(docTypes.isEmpty() ? null : docTypes.get(0));
        
        // Sample attachments data
        final String[][] samples = minimal ? 
            new String[][] {
                {"Requirements_v1.pdf", "application/pdf", "Initial requirements document"},
                {"Design_Mockup.png", "image/png", "UI mockup for feature"},
                {"Meeting_Notes.txt", "text/plain", "Notes from planning meeting"}
            } :
            new String[][] {
                {"Requirements_v1.pdf", "application/pdf", "Initial requirements document"},
                {"Requirements_v2.pdf", "application/pdf", "Updated requirements after review"},
                {"Design_Mockup.png", "image/png", "UI mockup for feature"},
                {"Technical_Spec.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Technical specification"},
                {"Meeting_Notes.txt", "text/plain", "Notes from planning meeting"},
                {"Test_Plan.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "QA test plan"},
                {"Architecture_Diagram.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "System architecture"},
                {"Code_Review.pdf", "application/pdf", "Code review checklist"}
            };
        
        // Create sample attachments
        int created = 0;
        for (String[] data : samples) {
            final String fileName = data[0];
            final String mimeType = data[1];
            final String description = data[2];
            
            // Create dummy file content
            final String content = "Sample content for " + fileName;
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(
                content.getBytes());
            
            // Upload attachment
            final CAttachment attachment = service.uploadFile(
                fileName,
                inputStream,
                content.length(),
                mimeType,
                specType,
                description
            );
            
            // Add to activity's collection
            activity.getAttachments().add(attachment);
            created++;
            
            LOGGER.debug("Created sample attachment: {}", fileName);
        }
        
        // Save activity (cascade will persist attachment relationships)
        activityService.save(activity);
        
        LOGGER.info("Created {} sample attachments for project {}", created, project.getName());
    }
}
```

## Testing Checklist

### Unit Tests
- [ ] Entity validation (NotBlank, Size, Min constraints)
- [ ] Service methods (uploadFile, uploadNewVersion, checkDeleteAllowed)
- [ ] Version chain traversal (getVersionHistory)
- [ ] File type icon mapping (getFileTypeIcon)

### Integration Tests
- [ ] Upload file and verify database record
- [ ] Upload new version and verify previousVersion link
- [ ] Delete attachment and verify file removed from disk
- [ ] Cascade delete when parent entity deleted
- [ ] Lazy loading with EntityGraph
- [ ] Multi-tenant isolation (company scoping)

### UI Tests (Playwright)
- [ ] Upload dialog opens and accepts files
- [ ] Grid displays attachments with correct columns
- [ ] Download button triggers file download
- [ ] Delete with confirmation works
- [ ] Version history display
- [ ] File type icons display correctly

## Troubleshooting

### LazyInitializationException
**Problem:** Exception when accessing attachments outside transaction

**Solution:** 
1. Add `@EntityGraph` to repository findById() method
2. Include `"attachments"` in attributePaths
3. Include nested: `"attachments.uploadedBy"`, `"attachments.documentType"`

### Attachments Not Saved
**Problem:** Attachments don't persist when saving parent

**Solution:**
1. Verify `cascade = CascadeType.ALL` on @OneToMany
2. Ensure attachment is added to parent's collection: `parent.getAttachments().add(attachment)`
3. Save parent entity after adding attachment

### Wrong Join Column
**Problem:** JPA tries to create wrong foreign key

**Solution:**
Ensure @JoinColumn name matches database column:
```java
@JoinColumn(name = "activity_id")  // Must match DB column name
```

### File Not Found on Download
**Problem:** File not found on disk when downloading

**Solution:**
1. Check `derbent.attachments.storage.path` configuration
2. Verify file path in contentPath field
3. Check file permissions on storage directory

## Summary

The attachment system uses a clean unidirectional @OneToMany pattern:
- ✅ Parent entities manage attachment collections
- ✅ CAttachment has NO back-references (no getActivity/getRisk/etc.)
- ✅ Company-scoped for multi-tenant support
- ✅ Cascade operations (save/delete)
- ✅ Version management with previousVersion chain
- ✅ File type icons for visual clarity
- ✅ Proper lazy loading with EntityGraph
- ✅ Complete validations and metadata annotations

Follow this guide to integrate attachments into any entity in the system.
