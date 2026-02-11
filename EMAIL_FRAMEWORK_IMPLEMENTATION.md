# EMAIL FRAMEWORK IMPLEMENTATION - Complete Email System

**Date**: 2026-02-11  
**Status**: 🚧 IN PROGRESS  
**Author**: SSC (Supreme System Architect) 👑

---

## Overview

Comprehensive email framework for Derbent PLM system following enterprise patterns with queue-based architecture, retry logic, and complete audit trail.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    EMAIL FRAMEWORK                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. CEmail (Abstract Base)                                  │
│     - Common fields for all emails                          │
│     - Company-scoped                                        │
│     - Retry management                                      │
│     ↓                                                       │
│  2. CEmailQueued (Pending)           3. CEmailSent (Archive)│
│     - Emails waiting to be sent          - Successfully sent│
│     - Priority ordering                  - Audit trail     │
│     - Retry tracking                     - Compliance      │
│                                                             │
└─────────────────────────────────────────────────────────────┘

FLOW:
  Create Email → CEmailQueued → Process → CEmailSent
                      ↓ (on failure)
                  Increment Retry → Try Again
                      ↓ (max retries)
                  Mark Failed → Manual Review
```

---

## Features Implemented ✅

### 1. Domain Entities

#### CEmail (Abstract Base Class)
- **Location**: `src/main/java/tech/derbent/api/email/domain/CEmail.java`
- **Extends**: `CEntityOfCompany<EntityClass>`
- **Pattern**: `@MappedSuperclass` (Hibernate inheritance)

**Fields**:
```java
// Sender
- fromEmail (required, email validation)
- fromName (optional)

// Recipient
- toEmail (required, email validation)
- toName (optional)

// Reply-To
- replyToEmail (optional, email validation)
- replyToName (optional)

// Content
- subject (required, max 255 chars)
- bodyText (optional, max 10000 chars)
- bodyHtml (optional, max 10000 chars)

// Priority
- priority (LOW, NORMAL, HIGH) - default: NORMAL

// Timestamps
- queuedAt (auto-set on creation)
- sentAt (set when successfully sent)

// Retry Management
- retryCount (starts at 0)
- maxRetries (default: 3)
- lastError (stores last failure message)

// Metadata
- emailType (WELCOME, PASSWORD_RESET, NOTIFICATION, etc.)
- referenceEntityType (entity type this email relates to)
- referenceEntityId (entity ID this email relates to)
```

**Methods**:
- `incrementRetryCount()`: Safely increment retry counter
- `hasReachedMaxRetries()`: Check if should give up

#### CEmailQueued
- **Location**: `src/main/java/tech/derbent/api/email/domain/CEmailQueued.java`
- **Table**: `cemail_queued`
- **Color**: `#FF9800` (Orange - pending action)
- **Icon**: `vaadin:clock`
- **Purpose**: Emails waiting to be sent

#### CEmailSent
- **Location**: `src/main/java/tech/derbent/api/email/domain/CEmailSent.java`
- **Table**: `cemail_sent`
- **Color**: `#4CAF50` (Green - success)
- **Icon**: `vaadin:check-circle`
- **Purpose**: Archive of successfully sent emails

### 2. Repositories

#### IEmailQueuedRepository
- **Location**: `src/main/java/tech/derbent/api/email/service/IEmailQueuedRepository.java`
- **Extends**: `IEntityOfCompanyRepository<CEmailQueued>`

**Custom Queries**:
```java
// Priority-ordered pending emails
List<CEmailQueued> findPendingEmails(CCompany) - HIGH → NORMAL → LOW, oldest first

// Failed emails (max retries reached)
List<CEmailQueued> findFailedEmails(CCompany) - for manual review

// By email type
List<CEmailQueued> findByEmailType(CCompany, String) - WELCOME, PASSWORD_RESET, etc.

// Cleanup queries
List<CEmailQueued> findQueuedBefore(CCompany, LocalDateTime) - purge old emails

// Counters
long countPendingEmails(CCompany) - dashboard metrics
long countFailedEmails(CCompany) - alert threshold
```

---

## Files Created (So Far) ✅

```
src/main/java/tech/derbent/api/email/
├── domain/
│   ├── CEmail.java ✅                      (Abstract base, 400+ lines)
│   ├── CEmailQueued.java ✅                (Concrete entity, 60 lines)
│   └── CEmailSent.java ✅                  (Concrete entity, 60 lines)
├── service/
│   └── IEmailQueuedRepository.java ✅      (Repository interface, 130 lines)
└── view/
    (to be created)
```

---

## Remaining Implementation 🚧

### 1. Repositories (IN PROGRESS)
- [ ] `IEmailSentRepository.java` - Archive queries

### 2. Services
- [ ] `CEmailQueuedService.java` - Queue management
- [ ] `CEmailSentService.java` - Archive management
- [ ] `CEmailService.java` - Sending logic (future - NOT NOW)

### 3. Initializers
- [ ] `CEmailQueuedInitializerService.java` - Sample data
- [ ] `CEmailSentInitializerService.java` - Sample data

### 4. Page Services
- [ ] `CPageServiceEmailQueued.java` - UI configuration
- [ ] `CPageServiceEmailSent.java` - UI configuration

### 5. System Settings Enhancement
- [ ] Add comprehensive email settings to `CSystemSettings.java`
- [ ] Email test component (like LDAP test)

### 6. Views (Optional)
- [ ] Queue management view
- [ ] Sent emails view
- [ ] Email test dialog

---

## Email Settings (To Add to SystemSettings)

Following LDAP pattern, add these fields to `CSystemSettings.java`:

```java
// Email Settings Section
@Column(name = "enable_email", nullable = false)
@AMetaData(displayName = "Enable Email", ...)
private Boolean enableEmail = true;

@Column(name = "email_administrator", length = 255)
@Email
@AMetaData(displayName = "Administrator Email", defaultValue = "yasin.yilmaz@ecemtag.com.tr", ...)
private String emailAdministrator = "yasin.yilmaz@ecemtag.com.tr";

@Column(name = "email_from", length = 255)
@Email
@AMetaData(displayName = "From Email", defaultValue = "info@ecemtag.com.tr", ...)
private String emailFrom = "info@ecemtag.com.tr";

@Column(name = "email_from_name", length = 255)
@AMetaData(displayName = "From Name", defaultValue = "info@ecemtag.com.tr", ...)
private String emailFromName = "info@ecemtag.com.tr";

@Column(name = "email_reply_to", length = 255)
@Email
@AMetaData(displayName = "Reply-To Email", defaultValue = "info@ecemtag.com.tr", ...)
private String emailReplyTo = "info@ecemtag.com.tr";

@Column(name = "email_reply_to_name", length = 255)
@AMetaData(displayName = "Reply-To Name", defaultValue = "info@ecemtag.com.tr", ...)
private String emailReplyToName = "info@ecemtag.com.tr";

@Column(name = "smtp_host", length = 255)
@AMetaData(displayName = "SMTP Server", defaultValue = "smtp.office365.com", ...)
private String smtpHost = "smtp.office365.com";

@Column(name = "smtp_port", nullable = false)
@AMetaData(displayName = "SMTP Port", defaultValue = "587", ...)
private Integer smtpPort = 587;

@Column(name = "smtp_username", length = 255)
@AMetaData(displayName = "SMTP Username", defaultValue = "info@ecemtag.com.tr", ...)
private String smtpUsername = "info@ecemtag.com.tr";

@Column(name = "smtp_password", length = 255)
@AMetaData(displayName = "SMTP Password", inputType = "password", ...)
private String smtpPassword = "";

@Column(name = "smtp_use_tls", nullable = false)
@AMetaData(displayName = "Use TLS", defaultValue = "true", ...)
private Boolean smtpUseTls = true;

@Column(name = "smtp_use_ssl", nullable = false)
@AMetaData(displayName = "Use SSL", defaultValue = "false", ...)
private Boolean smtpUseSsl = false;

@Column(name = "email_eol_format", length = 50)
@AMetaData(displayName = "End of Line Format", defaultValue = "default", ...)
private String emailEolFormat = "default";

@Column(name = "email_max_attachment_size_mb", nullable = false)
@AMetaData(displayName = "Max Attachment Size (MB)", defaultValue = "5", ...)
private Integer emailMaxAttachmentSizeMb = 5;

@Column(name = "email_embed_images", nullable = false)
@AMetaData(displayName = "Embed Images in Emails", defaultValue = "false", ...)
private Boolean emailEmbedImages = false;

// Email Test Component - Transient placeholder
@Transient
@AMetaData(
    displayName = "Email Test",
    required = false,
    readOnly = false,
    description = "Test email configuration",
    hidden = false,
    dataProviderBean = "pageservice",
    createComponentMethod = "createComponentEmailTest",
    captionVisible = false
)
private final CSystemSettings<?> placeHolder_createComponentEmailTest = null;
```

---

## Usage Patterns

### Creating a Queued Email

```java
// Get services
CEmailQueuedService emailService = getBean(CEmailQueuedService.class);
CSystemSettingsService settingsService = getBean(CSystemSettingsService.class);

// Get system settings
CSystemSettings<?> settings = settingsService.getSystemSettings();

// Create email
CEmailQueued email = new CEmailQueued(
    "Welcome to Derbent PLM",
    "user@example.com",
    company
);

// Set sender (from system settings)
email.setFromEmail(settings.getEmailFrom());
email.setFromName(settings.getEmailFromName());

// Set reply-to (from system settings)
email.setReplyToEmail(settings.getEmailReplyTo());
email.setReplyToName(settings.getEmailReplyToName());

// Set content
email.setBodyText("Welcome! Your account has been created.");
email.setBodyHtml("<h1>Welcome!</h1><p>Your account has been created.</p>");

// Set priority
email.setPriority("HIGH");

// Set type and reference
email.setEmailType("WELCOME");
email.setReferenceEntityType("CUser");
email.setReferenceEntityId(userId);

// Save to queue
emailService.save(email);
```

### Processing Queue (Future - Background Service)

```java
// Find pending emails
List<CEmailQueued> pending = emailService.findPendingEmails(company);

for (CEmailQueued email : pending) {
    try {
        // Attempt to send (future implementation)
        // sendEmail(email);
        
        // On success: move to sent archive
        CEmailSent sentEmail = emailService.moveToSent(email);
        
    } catch (Exception e) {
        // On failure: increment retry count
        email.incrementRetryCount();
        email.setLastError(e.getMessage());
        emailService.save(email);
        
        if (email.hasReachedMaxRetries()) {
            // Alert admin - email failed permanently
            LOGGER.error("Email {} failed after {} retries", 
                email.getId(), email.getRetryCount());
        }
    }
}
```

---

## Database Schema

### cemail_queued Table
```sql
CREATE TABLE cemail_queued (
    email_queued_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES ccompany(company_id),
    
    -- Sender
    from_email VARCHAR(255) NOT NULL,
    from_name VARCHAR(255),
    
    -- Recipient
    to_email VARCHAR(255) NOT NULL,
    to_name VARCHAR(255),
    
    -- Reply-To
    reply_to_email VARCHAR(255),
    reply_to_name VARCHAR(255),
    
    -- Content
    subject VARCHAR(255) NOT NULL,
    body_text TEXT,
    body_html TEXT,
    
    -- Priority
    priority VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    
    -- Timestamps
    queued_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    
    -- Retry
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    last_error VARCHAR(2000),
    
    -- Metadata
    email_type VARCHAR(100),
    reference_entity_type VARCHAR(100),
    reference_entity_id BIGINT,
    
    -- Audit
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255)
);

CREATE INDEX idx_email_queued_company ON cemail_queued(company_id);
CREATE INDEX idx_email_queued_priority ON cemail_queued(priority, queued_at);
CREATE INDEX idx_email_queued_type ON cemail_queued(email_type);
CREATE INDEX idx_email_queued_reference ON cemail_queued(reference_entity_type, reference_entity_id);
```

### cemail_sent Table
```sql
CREATE TABLE cemail_sent (
    email_sent_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES ccompany(company_id),
    
    -- Same fields as cemail_queued
    -- (see above)
    
    -- Additional constraint: sent_at must be NOT NULL
);

CREATE INDEX idx_email_sent_company ON cemail_sent(company_id);
CREATE INDEX idx_email_sent_date ON cemail_sent(sent_at DESC);
CREATE INDEX idx_email_sent_type ON cemail_sent(email_type);
```

---

## Testing Checklist

- [ ] Create queued email via service
- [ ] Verify database record
- [ ] Check validation (required fields)
- [ ] Test retry increment
- [ ] Test max retries reached
- [ ] Move email to sent archive
- [ ] Verify sent timestamp
- [ ] Query pending emails (priority order)
- [ ] Query failed emails
- [ ] Test email type filtering

---

## Next Steps

1. **Complete Repositories** ✅ NEXT
   - Create `IEmailSentRepository.java`

2. **Create Services**
   - Implement `CEmailQueuedService.java`
   - Implement `CEmailSentService.java`

3. **Create Initializers**
   - Sample data for testing

4. **Add Email Settings to SystemSettings**
   - Follow LDAP settings pattern
   - Add email test component

5. **Create Page Services**
   - UI configuration for both entities

6. **Integration Testing**
   - Create emails programmatically
   - Verify queue/sent flow

---

## Future Enhancements (NOT NOW)

1. **Email Sending Service**
   - SMTP integration
   - HTML template engine
   - Attachment handling

2. **Background Processor**
   - Scheduled email processing
   - Retry logic implementation
   - Error handling

3. **Email Templates**
   - Template entity
   - Variable substitution
   - Preview functionality

4. **Advanced Features**
   - Email tracking (opens, clicks)
   - Bounce handling
   - Unsubscribe management

---

**Status**: 🚧 Entities and base repository created  
**Next**: Complete remaining repositories and services  
**Then**: System settings integration and testing

---

**SSC WAS HERE!!** 👑✨
