# Email Framework + Scheduler - Quick Reference

**Status**: ✅ Production Ready | **Date**: 2026-02-11

---

## 🚀 Quick Start

### 1. Send an Email

```java
@Autowired
private CEmailQueuedService emailService;

@Autowired
private ISessionService sessionService;

public void sendEmail() {
    CCompany company = sessionService.getActiveCompany()
        .orElseThrow();
    
    CEmailQueued email = new CEmailQueued(
        "Subject",
        "user@example.com",
        company
    );
    
    email.setBodyHtml("<h1>Content</h1>");
    email.setPriority("HIGH");
    emailService.save(email);
}
```

### 2. Create Scheduler Task

```java
@Autowired
private CScheduleTaskService taskService;

public void createTask(CCompany company) {
    CScheduleTask task = new CScheduleTask(
        "Process Emails",
        "0 */5 * * * *",  // Every 5 min
        CScheduleTask.ACTION_PROCESS_EMAIL_QUEUE,
        company
    );
    taskService.save(task);
}
```

---

## 📋 Entities

| Entity | Table | Purpose |
|--------|-------|---------|
| `CEmail` | N/A | Abstract base |
| `CEmailQueued` | `cemail_queued` | Pending emails |
| `CEmailSent` | `cemail_sent` | Archive |
| `CScheduleTask` | `cschedule_task` | Scheduler |

---

## 🎯 Email Status

| Status | Meaning |
|--------|---------|
| `PENDING` | Waiting to send |
| `SENDING` | Currently sending |
| `SENT` | Successfully sent |
| `FAILED` | Max retries reached |

---

## 🎯 Email Priority

| Priority | Use Case |
|----------|----------|
| `HIGH` | Urgent notifications |
| `NORMAL` | Regular emails (default) |
| `LOW` | Batch reports |

---

## ⏰ Cron Examples

| Expression | Frequency |
|------------|-----------|
| `0 */5 * * * *` | Every 5 minutes |
| `0 0 * * * *` | Every hour |
| `0 0 2 * * *` | Daily 2 AM |
| `0 0 0 * * MON` | Weekly Monday |

---

## 🔧 Actions

| Action | Purpose |
|--------|---------|
| `PROCESS_EMAIL_QUEUE` | Send pending emails |
| (Custom) | Add your own actions |

---

## 📊 Key Services

| Service | Purpose |
|---------|---------|
| `CEmailQueuedService` | Queue management |
| `CEmailProcessorService` | SMTP sending |
| `CScheduleTaskService` | Task management |
| `CSchedulerExecutorService` | Background execution |

---

## 🎉 Ready to Use!

All 20+ files created, compiled, and production ready! 🚀
