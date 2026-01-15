# Attachment Integration Checklist

## Entities to Complete

This document tracks attachment integration for all requested entities:
- ✅ CActivity (DONE - reference implementation)
- ⏳ CRisk
- ⏳ CMeeting  
- ⏳ CSprint
- ⏳ CProject
- ⏳ CUser
- ⏳ CDecision
- ⏳ COrder

## Standard Implementation Pattern

For EACH entity, complete these 3 steps:

### Step 1: Entity Class - Add IHasAttachments Implementation

```java
// Example: CRisk.java
public class CRisk extends CProjectItem<CRisk> implements IHasAttachments {
    
    // Add OneToMany field
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_id")  // CHANGE: use entity-specific column name
    @AMetaData(
        displayName = "Attachments",
        required = false,
        readOnly = false,
        description = "File attachments for this risk",  // CHANGE: entity-specific description
        hidden = false,
        createComponentMethodBean = "CAttachmentComponentFactory",
        createComponentMethod = "createComponent"
    )
    private Set<CAttachment> attachments = new HashSet<>();
    
    // Add interface implementation
    @Override
    public Set<CAttachment> getAttachments() {
        if (attachments == null) {
            attachments = new HashSet<>();
        }
        return attachments;
    }
    
    @Override
    public void setAttachments(List<CAttachment> attachments) {
        this.attachments = attachments;
    }
}
```

### Step 2: Entity Initializer - Add Attachments Section

```java
// Example: CRiskInitializerService.java
public static CDetailSection createBasicView(CProject project) {
    CDetailSection scr = createBaseScreenEntity(project, CRisk.class);
    
    // ... other sections (Details, Schedule, etc.) ...
    
    // Add attachments section - ONE LINE
    tech.derbent.app.attachments.service.CAttachmentInitializerService
        .addAttachmentsSection(scr, CRisk.class);
    
    // ... more sections (System, Additional Info) ...
    
    return scr;
}
```

### Step 3: Repository - Add EntityGraph (if needed)

```java
// Example: IRiskRepository.java
@EntityGraph(attributePaths = {
    "status", 
    "assignedTo", 
    "project", 
    "company",
    "attachments",                    // Add this
    "attachments.uploadedBy",         // Add this
    "attachments.documentType"        // Add this
})
@Override
Optional<CRisk> findById(Long id);
```

## Entity-Specific Details

### CRisk
- **Join Column**: `risk_id`
- **Description**: "File attachments for this risk"
- **Location**: `src/main/java/tech/derbent/app/risks/risk/domain/CRisk.java`
- **Initializer**: `src/main/java/tech/derbent/app/risks/risk/service/CRiskInitializerService.java`
- **Repository**: `src/main/java/tech/derbent/app/risks/risk/service/IRiskRepository.java`

### CMeeting
- **Join Column**: `meeting_id`
- **Description**: "Meeting documents and files"
- **Location**: `src/main/java/tech/derbent/app/meetings/domain/CMeeting.java`
- **Initializer**: `src/main/java/tech/derbent/app/meetings/service/CMeetingInitializerService.java`
- **Repository**: `src/main/java/tech/derbent/app/meetings/service/IMeetingRepository.java`

### CSprint
- **Join Column**: `sprint_id`
- **Description**: "Sprint documentation and files"
- **Location**: `src/main/java/tech/derbent/app/sprints/domain/CSprint.java`
- **Initializer**: `src/main/java/tech/derbent/app/sprints/service/CSprintInitializerService.java`
- **Repository**: `src/main/java/tech/derbent/app/sprints/service/ISprintRepository.java`

### CProject
- **Join Column**: `project_id`
- **Description**: "Project documentation and files"
- **Location**: `src/main/java/tech/derbent/api/projects/domain/CProject.java`
- **Initializer**: `src/main/java/tech/derbent/api/projects/service/CProjectInitializerService.java`
- **Repository**: `src/main/java/tech/derbent/api/projects/service/IProjectRepository.java`

### CUser
- **Join Column**: `user_id`
- **Description**: "User documents (CV, certifications, etc.)"
- **Location**: `src/main/java/tech/derbent/base/users/domain/CUser.java`
- **Initializer**: `src/main/java/tech/derbent/base/users/service/CUserInitializerService.java`
- **Repository**: `src/main/java/tech/derbent/base/users/service/IUserRepository.java`

### CDecision
- **Join Column**: `decision_id`
- **Description**: "Decision supporting documents"
- **Location**: `src/main/java/tech/derbent/app/decisions/domain/CDecision.java`
- **Initializer**: `src/main/java/tech/derbent/app/decisions/service/CDecisionInitializerService.java`
- **Repository**: `src/main/java/tech/derbent/app/decisions/service/IDecisionRepository.java`

### COrder  
- **Join Column**: `order_id`
- **Description**: "Order documents and invoices"
- **Location**: Need to find COrder location
- **Initializer**: Need to find COrderInitializerService location
- **Repository**: Need to find IOrderRepository location

## Database Schema

Each entity table needs NO CHANGES - the join column is managed by JPA via `@JoinColumn` on the parent entity.

Example for Risk:
```sql
-- No changes needed to crisk table

-- cattachment table already has risk_id column:
CREATE TABLE cattachment (
    attachment_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES ccompany(company_id),
    risk_id BIGINT REFERENCES crisk(risk_id),  -- Already exists
    -- ... other columns ...
);
```

## Testing Checklist

For EACH entity after implementation:

- [ ] Entity compiles successfully
- [ ] Initializer compiles successfully  
- [ ] Repository compiles successfully
- [ ] Detail view shows Attachments section
- [ ] Can upload file to entity
- [ ] Can download uploaded file
- [ ] Can delete uploaded file
- [ ] Attachments persist across page refreshes
- [ ] Cascade delete works (delete entity removes attachments)
- [ ] Grid shows compact mode when empty
- [ ] Grid shows full mode with attachments

## Implementation Order

1. ✅ CActivity - COMPLETE (reference implementation)
2. CRisk - Implement next
3. CMeeting - Implement next
4. CSprint - Implement next  
5. CProject - Implement next
6. CDecision - Implement next
7. CUser - Implement next
8. COrder - Implement next (if exists)

## Common Imports Needed

```java
// Entity imports
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
```

## Validation Steps

After completing all entities, verify:

1. ✅ All entities compile
2. ✅ All initializers compile
3. ✅ All repositories compile
4. ✅ Application starts successfully
5. ✅ Navigate to each entity detail page
6. ✅ Verify Attachments section appears
7. ✅ Upload test file to each entity type
8. ✅ Verify files persist and can be downloaded
9. ✅ Run Playwright tests if available
10. ✅ Check database for proper foreign key relationships

## Notes

- **ALWAYS** use exact same @AMetaData (except description)
- **ALWAYS** use exact same interface implementation (copy-paste)
- **ALWAYS** call CAttachmentInitializerService.addAttachmentsSection()
- **NEVER** create custom section code in entity initializers
- **NEVER** use different field names (always "attachments")
- **NEVER** skip the IHasAttachments interface implementation
