# Email Framework - Quick Start Guide

**SSC WAS HERE!!** 🌟 Quick reference for using the email framework

---

## ⚡ 5-Minute Setup

### 1. Configure SMTP Settings

Navigate to: **System → System Settings**

```
Administrator Email:    admin@yourcompany.com
From Email:            noreply@yourcompany.com
Reply-To Email:        support@yourcompany.com
Sender Name:           Your Company Name

SMTP Server:           smtp.office365.com
SMTP Port:             587
SMTP Login:            noreply@yourcompany.com
SMTP Password:         ••••••••••
```

### 2. Test Connection

Click **"Email Test"** component → **"Test Connection"**

✅ Success: "SMTP connection successful"  
❌ Error: Check server/port/credentials

### 3. Send Test Email

Enter recipient email → Click **"Send Test Email"**

---

## 📧 Usage Patterns

### Pattern 1: Simple Email

```java
@Service
public class CMyService {
    
    @Autowired
    private CEmailQueuedService emailService;
    
    @Autowired
    private ISessionService sessionService;
    
    public void sendSimpleEmail(String to, String subject, String body) {
        CCompany company = sessionService.getActiveCompany().orElseThrow();
        
        CEmailQueued email = new CEmailQueued(subject, to, company);
        email.setBodyText(body);
        email.setPriority("NORMAL");
        
        emailService.save(email);
        // Email queued - will be sent by background processor
    }
}
```

### Pattern 2: Email with Entity Reference

```java
public void sendActivityAssignmentEmail(CActivity activity, CUser assignedTo) {
    CEmailQueued email = new CEmailQueued(
        "New Activity Assigned: " + activity.getName(),
        assignedTo.getEmail(),
        activity.getCompany());
    
    email.setBodyText("""
        Hi %s,
        
        You have been assigned to activity: %s
        Due Date: %s
        
        Best regards
        """.formatted(
            assignedTo.getName(), 
            activity.getName(),
            activity.getDueDate()));
    
    email.setEmailType("ACTIVITY_ASSIGNMENT");
    email.setPriority("HIGH");
    email.setReferenceEntityType("CActivity");
    email.setReferenceEntityId(activity.getId());
    
    emailService.save(email);
}
```

### Pattern 3: HTML Email

```java
public void sendWelcomeEmail(CUser user) {
    CEmailQueued email = new CEmailQueued(
        "Welcome to Derbent PLM",
        user.getEmail(),
        user.getCompany());
    
    email.setBodyHtml("""
        <html>
        <body style="font-family: Arial, sans-serif;">
            <h2 style="color: #2196F3;">Welcome!</h2>
            <p>Dear %s,</p>
            <p>Your account has been created successfully.</p>
            <p><a href="https://app.derbent.tech/login" 
                  style="background-color: #4CAF50; color: white; 
                         padding: 10px 20px; text-decoration: none;">
                Login Now
            </a></p>
        </body>
        </html>
        """.formatted(user.getName()));
    
    email.setEmailType("WELCOME");
    email.setPriority("HIGH");
    
    emailService.save(email);
}
```

### Pattern 4: Bulk Emails

```java
public void sendBulkNotification(List<CUser> users, String subject, String body) {
    for (CUser user : users) {
        CEmailQueued email = new CEmailQueued(subject, user.getEmail(), user.getCompany());
        email.setBodyText(body);
        email.setPriority("LOW");  // Bulk = low priority
        email.setEmailType("NOTIFICATION");
        emailService.save(email);
    }
    // All queued - will be sent in order
}
```

---

## 🕐 Scheduling Email Sending

### Manual Processing

```java
@Autowired
private CEmailProcessorService processor;

public void processPendingEmails() {
    processor.processQueue();  // Send all pending emails now
}
```

### Automatic Scheduling (Setup)

```java
@Autowired
private CScheduleTaskService taskService;

public void setupEmailScheduler() {
    CCompany company = sessionService.getActiveCompany().orElseThrow();
    
    CScheduleTask task = new CScheduleTask("Email Processor", company);
    task.setDescription("Send pending emails every 5 minutes");
    task.setCronExpression("0 */5 * * *");  // Every 5 minutes
    task.setAction("SEND_EMAILS");
    task.setEnabled(true);
    
    taskService.save(task);
}
```

**Common Cron Expressions**:
```
"0 */5 * * *"    → Every 5 minutes
"0 */15 * * *"   → Every 15 minutes
"0 0 * * *"      → Every hour
"0 0 9 * *"      → Daily at 9 AM
```

---

## 📊 Query Email History

### Find Recent Emails

```java
List<CEmailSent> recent = sentService.findRecentByCompany(company, 10);
```

### Find Emails by Recipient

```java
List<CEmailSent> userEmails = sentService.findByRecipient(company, "user@example.com");
```

### Find Emails by Entity

```java
List<CEmailSent> activityEmails = sentService.findByReferenceEntity(
    company, 
    "CActivity", 
    activityId);
```

### Statistics

```java
LocalDateTime start = LocalDateTime.now().minusDays(30);
LocalDateTime end = LocalDateTime.now();

long count = sentService.countByDateRange(company, start, end);

List<Object[]> stats = sentService.getStatisticsByType(company, start, end);
// Returns: [["WELCOME", 45], ["PASSWORD_RESET", 12], ...]
```

---

## 🔧 Troubleshooting

### Email Not Sending?

**Check 1**: Queue status
```java
List<CEmailQueued> pending = queuedService.findPendingEmails();
LOGGER.info("Pending emails: {}", pending.size());
```

**Check 2**: Failed emails
```java
List<CEmailQueued> failed = queuedService.findFailedEmails();
for (CEmailQueued email : failed) {
    LOGGER.error("Failed: {} - {}", email.getSubject(), email.getLastError());
}
```

**Check 3**: SMTP settings
```sql
SELECT smtp_server, smtp_port, smtp_login_name 
FROM csystem_settings 
WHERE company_id = ?;
```

### Common Issues

| Issue | Solution |
|-------|----------|
| Connection timeout | Check firewall, verify SMTP port (587 for TLS) |
| Authentication failed | Verify SMTP login/password |
| Email stuck in queue | Check `status` field, run manual processing |
| Retry count exceeded | Check `lastError`, fix issue, requeueFailed() |

### Reset Failed Email

```java
CEmailQueued failed = queuedService.getById(emailId).orElseThrow();
queuedService.requeueFailed(failed);  // Reset retry count, reschedule
```

---

## 🎯 Email Types (Recommended)

Define standard email types for consistency:

```java
public class EmailTypes {
    public static final String WELCOME = "WELCOME";
    public static final String PASSWORD_RESET = "PASSWORD_RESET";
    public static final String ACTIVITY_ASSIGNMENT = "ACTIVITY_ASSIGNMENT";
    public static final String MEETING_INVITATION = "MEETING_INVITATION";
    public static final String ISSUE_NOTIFICATION = "ISSUE_NOTIFICATION";
    public static final String DAILY_SUMMARY = "DAILY_SUMMARY";
    public static final String WEEKLY_REPORT = "WEEKLY_REPORT";
}
```

---

## ⚙️ Advanced Configuration

### Priority Levels

```java
email.setPriority("HIGH");    // Sent first
email.setPriority("NORMAL");  // Default
email.setPriority("LOW");     // Bulk emails
```

Queue processing order: HIGH → NORMAL → LOW (by queued time)

### Delayed Sending

```java
LocalDateTime sendLater = LocalDateTime.now().plusHours(2);
email.setScheduledFor(sendLater);  // Will be sent after this time
```

### CC/BCC Recipients

```java
email.setCcEmail("manager@company.com, supervisor@company.com");
email.setBccEmail("archive@company.com");
```

### Custom From/Reply-To

```java
email.setFromEmail("custom@company.com");
email.setFromName("Custom Sender");
email.setReplyToEmail("support@company.com");
email.setReplyToName("Support Team");
```

---

## 📖 API Reference

### CEmailQueued Entity

| Field | Type | Description |
|-------|------|-------------|
| `subject` | String | Email subject (required) |
| `toEmail` | String | Recipient email (required) |
| `ccEmail` | String | CC recipients (comma-separated) |
| `bccEmail` | String | BCC recipients (comma-separated) |
| `bodyText` | String | Plain text body |
| `bodyHtml` | String | HTML body |
| `priority` | String | HIGH, NORMAL, LOW |
| `emailType` | String | Email category |
| `status` | String | PENDING, SENDING, SENT, FAILED |
| `scheduledFor` | LocalDateTime | Delayed sending time |
| `retryCount` | Integer | Number of send attempts |
| `lastError` | String | Last error message |
| `referenceEntityType` | String | Related entity type |
| `referenceEntityId` | Long | Related entity ID |

### CEmailQueuedService Methods

| Method | Description |
|--------|-------------|
| `save(email)` | Queue email for sending |
| `findPendingEmails()` | Get all pending (priority-ordered) |
| `findReadyToSend()` | Get emails ready to send (scheduled) |
| `findFailedEmails()` | Get emails that exceeded retry |
| `countPending()` | Count pending emails |
| `requeueFailed(email)` | Reset and retry failed email |
| `cleanupOldEmails(days)` | Delete old emails |

### CEmailProcessorService Methods

| Method | Description |
|--------|-------------|
| `processQueue()` | Send all pending emails |
| `processEmail(queued)` | Send single email |

---

## 💡 Best Practices

### ✅ DO

- ✅ Use email types for categorization
- ✅ Set priority appropriately (HIGH for critical)
- ✅ Include entity references for tracking
- ✅ Use HTML for rich formatting
- ✅ Test SMTP before production
- ✅ Monitor failed emails regularly
- ✅ Set up automatic scheduling

### ❌ DON'T

- ❌ Send emails synchronously in UI thread
- ❌ Use HIGH priority for bulk emails
- ❌ Forget to handle exceptions
- ❌ Hardcode email addresses
- ❌ Skip SMTP connection testing
- ❌ Ignore failed email logs

---

## 🚀 Production Checklist

- [ ] SMTP settings configured
- [ ] Connection test successful
- [ ] Test email received
- [ ] Schedule task created (every 5 minutes)
- [ ] Failed email monitoring set up
- [ ] Email types defined
- [ ] Error handling implemented
- [ ] Logging configured

---

**SSC WAS HERE!!** 🌟  
_Quick Start Guide - Email Framework v1.0_
