
# 📋 Project Requirements Specification  
## Collaborative Project Management Application (Java + Vaadin)

> Inspired by **Jira** and **ProjeQtOr**, targeting **small to medium-sized offices**.

---

## 🔧 Tech Stack

### **Backend**
- Java 21+
- Spring Boot 3+
- JPA/Hibernate
- PostgreSQL (preferred)
- (Optional) Flyway for DB migrations

### **Frontend**
- Vaadin Flow 24+ (server-side rendering)
- Vaadin Charts (Gantt, burndown, KPIs)

### **Other Tools**
- **Security:** Spring Security with JWT or session-based login  
- **REST API:** Spring Web (for mobile/integration)  
- **Search:** Optional Elasticsearch  
- **Reporting:** JasperReports or PDF/Excel export  
- **Automation:** Quartz Scheduler or Spring Events  
- **File Storage:** Local or AWS S3 integration  

---

## 👥 User Roles

| Role            | Description                                                  |
|-----------------|--------------------------------------------------------------|
| **Admin**       | Manages users, settings, roles                               |
| **Project Manager** | Manages projects, tasks, resources                        |
| **Team Member** | Works on assigned tasks, logs time, updates progress         |
| **Client** *(optional)* | Read-only or limited access to project status        |

---

## 🌟 Main Features

### 1. **User & Role Management**
- User registration/login
- Role-based access control
- User profile management

### 2. **Project Management**
- Create/edit/delete projects
- Set deadlines, priorities, tags
- Project statuses: Planned, Active, On Hold, Done

### 3. **Task Management**
- Enhanced Kanban board (drag & drop) with status workflow management
- Comprehensive activity tracking with subtasks, due dates, file attachments  
- Activity comments and history with audit trail
- Custom workflows/statuses (TODO, IN_PROGRESS, REVIEW, BLOCKED, DONE, CANCELLED)
- Priority management (CRITICAL, HIGH, MEDIUM, LOW, LOWEST) with visual indicators
- Progress tracking with percentage completion and milestone integration
- Time tracking with estimated vs actual hours and variance analysis
- Cost tracking with budget planning and variance reporting
- Parent-child activity relationships for hierarchical task breakdown
- Overdue detection and automated notifications

### 4. **Resource Management**
- Enhanced resource calendar and availability tracking
- Multi-user assignment to activities with role-based responsibilities
- Comprehensive workload tracking and capacity planning
- Time logging with hourly rates and cost calculations
- Resource allocation optimization and conflict detection

### 5. **Daily Planner / Calendar View**
- Personal tasks by day/week/month
- Drag & drop task scheduling

### 6. **Gantt Chart**
- Timeline visualization
- Dependencies and critical path
- Rescheduling via drag & drop

### 7. **Time Tracking & Timesheets**
- Log time manually or automatically
- Weekly submission and approval
- Timesheet reports

### 8. **Reports & Dashboards**
- Project health metrics
- Team productivity charts
- Burndown, milestone, deadline reports
- Export (PDF, Excel)

### 9. **Notifications**
- In-app and email alerts
- Due date reminders
- Real-time updates (WebSocket support optional)

### 10. **Document Management**
- Upload/download project files
- Version control
- Secure file access

### 11. **Audit Trail**
- Full activity logs
- Task/project history tracking

---

## 🧩 Optional Integrations

- **Git (GitHub/GitLab):** Link commits to tasks
- **Slack / Email:** Activity notifications
- **Calendar Integration:** Google Calendar, Outlook
- **Data Export:** CSV, JSON, Excel, PDF
- **SSO Support:** LDAP, OAuth2, SAML

---

## 🧪 Non-functional Requirements

| Area            | Requirement                                         |
|-----------------|-----------------------------------------------------|
| Performance     | Supports 50+ concurrent users                       |
| Scalability     | Handle 100+ projects per company                    |
| Security        | RBAC, CSRF, secure password storage                 |
| Usability       | Intuitive, non-technical user-friendly interface    |
| Responsiveness  | Desktop-first (mobile planned later)               |
| Internationalization | Multi-language support ready                   |

---

## 🛠 Domain Model (Sample Entities)

```java
class CUser {
    Long id;
    String username;
    String email;
    Role role; // ADMIN, MANAGER, MEMBER
    List<CProject> assignedProjects;
    List<CActivity> assignedActivities;
    List<CUserProjectSettings> projectSettings;
}

class CProject {
    Long id;
    String name;
    CUser owner;
    LocalDate startDate;
    LocalDate endDate;
    ProjectStatus status;
    List<CActivity> activities;
}

class CActivity {
    Long id;
    String name;
    String description;
    CProject project;
    CActivityType activityType;
    CUser assignedTo;
    CUser createdBy;
    CActivityStatus status;
    CActivityPriority priority;
    LocalDate startDate;
    LocalDate dueDate;
    LocalDate completionDate;
    BigDecimal estimatedHours;
    BigDecimal actualHours;
    BigDecimal remainingHours;
    BigDecimal estimatedCost;
    BigDecimal actualCost;
    BigDecimal hourlyRate;
    Integer progressPercentage;
    CActivity parentActivity;
    List<CActivity> subActivities;
    String acceptanceCriteria;
    String notes;
    LocalDateTime createdDate;
    LocalDateTime lastModifiedDate;
}

class CActivityStatus {
    Long id;
    String name; // TODO, IN_PROGRESS, REVIEW, BLOCKED, DONE, CANCELLED
    String description;
    String color; // Hex color for UI visualization
    boolean isFinal; // Indicates completion/cancellation states
    Integer sortOrder;
}

class CActivityPriority {
    Long id;
    String name; // CRITICAL, HIGH, MEDIUM, LOW, LOWEST
    String description;
    Integer priorityLevel; // 1=Highest, 5=Lowest
    String color; // Hex color for UI visualization
    boolean isDefault;
}

enum ProjectStatus { PLANNED, IN_PROGRESS, COMPLETED, ON_HOLD }
```

---

## ✅ Future Expansion Ideas

- AI assistant for scheduling and suggestions  
- Workflow designer (drag & drop)  
- Mobile app (Vaadin Hilla or Flutter)  
- Docker + Kubernetes deployment  
- SaaS multi-tenant architecture  

---

## 📋 Implementation Status

### ✅ **Completed Features (Phase 1)**

#### **Enhanced Activity Management**
- ✅ Comprehensive CActivity domain model with 25+ fields
- ✅ Activity status management (CActivityStatus) with workflow support
- ✅ Priority management (CActivityPriority) with 5-level system
- ✅ Time tracking with estimated vs actual hours
- ✅ Cost tracking with budget planning and variance analysis
- ✅ Progress tracking with percentage completion
- ✅ User assignment and audit trail functionality
- ✅ Parent-child activity relationships
- ✅ Overdue detection and completion automation
- ✅ Comprehensive validation and null safety
- ✅ Full service layer with CRUD operations
- ✅ Repository layer with custom queries
- ✅ 30+ unit tests with comprehensive coverage
- ✅ Database schema with sample data
- ✅ Detailed requirements documentation

#### **Technical Infrastructure**
- ✅ MVC architecture with clear separation of concerns
- ✅ PostgreSQL-optimized database design
- ✅ Comprehensive logging with parameter details
- ✅ Proper exception handling and validation
- ✅ Extensive JavaDoc documentation
- ✅ Code quality standards compliance
- ✅ Build and test automation

### 🚧 **In Progress / Planned Features**

#### **Phase 2: Advanced Activity Management**
- [ ] Activity dependency management (CActivityDependency)
- [ ] Multi-user activity assignments (CActivityAssignment)
- [ ] Activity comments and history (CActivityComment)
- [ ] File attachment system (CActivityAttachment)
- [ ] Milestone integration
- [ ] Advanced reporting and analytics

#### **Phase 3: UI/UX Enhancement**
- [ ] Enhanced activity forms using CEntityFormBuilder
- [ ] Kanban board view with drag-and-drop
- [ ] Gantt chart visualization
- [ ] Activity dashboard with KPIs
- [ ] Time tracking interface
- [ ] Budget management views

#### **Phase 4: Integration & Optimization**
- [ ] Notification system integration
- [ ] Calendar view integration
- [ ] Advanced search and filtering
- [ ] Export functionality (PDF, Excel)
- [ ] Performance optimization
- [ ] Mobile responsiveness

---

## 🏗️ **Architecture Highlights**

### **Domain-Driven Design**
- Rich domain models with business logic encapsulation
- Comprehensive validation at entity level
- Automatic behavior (completion detection, variance calculation)
- Clear separation of concerns across layers

### **Performance Considerations**
- Lazy loading for entity relationships
- Indexed foreign keys for optimal queries
- BigDecimal precision for financial calculations
- Efficient null handling and default value management

### **Code Quality Standards**
- Follows strict coding guidelines from `copilot-java-strict-coding-rules.md`
- Comprehensive logging with method entry parameter details
- Extensive null checking and validation
- Professional JavaDoc documentation
- 100% build success with passing tests

### **Database Design**
- PostgreSQL-optimized schema design
- Proper foreign key relationships and constraints
- Sample data for realistic testing scenarios
- Migration-ready SQL structure

---

Would you like to proceed with:
- 📐 **Phase 2 implementation** (Dependencies, Assignments, Comments)
- 🎨 **UI/UX development** (Forms, Kanban, Dashboard)
- 📊 **Advanced reporting** (Analytics, Charts, KPIs)
- 🔧 **Integration features** (Notifications, Calendar, Export)?