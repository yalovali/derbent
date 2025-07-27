# 📋 Enhanced Activity Management Requirements
## Comprehensive CActivity Enhancement Specification

> Inspired by **Jira** and **ProjeQtOr**, following strict coding guidelines from `copilot-java-strict-coding-rules.md`

---

## 🎯 Task Description

Enhance the existing `CActivity` class to support comprehensive project management capabilities including:
- **Resource Management**: User assignments, time tracking, workload management
- **Task Tracking**: Advanced status management, priority levels, dependencies
- **Project Management**: Milestone integration, progress tracking, deliverables
- **Budget Planning**: Cost estimation, actual vs planned tracking, hourly rates
- **UI/UX Enhancement**: Rich forms, validation, user-friendly interfaces

---

## 🌟 Main Features

### 1. **Resource Management**
- **User Assignment**: Assign activities to specific users with role-based permissions
- **Time Tracking**: Estimated hours, actual hours worked, remaining hours
- **Workload Management**: Track user capacity and availability
- **Resource Allocation**: Multiple users per activity with different roles

### 2. **Task Tracking & Workflow**
- **Status Management**: TODO, IN_PROGRESS, REVIEW, BLOCKED, DONE, CANCELLED
- **Priority Levels**: LOW, MEDIUM, HIGH, CRITICAL, URGENT
- **Progress Tracking**: Percentage completion, milestone checkpoints
- **Dependencies**: Task prerequisites and blocking relationships
- **Sub-activities**: Hierarchical task breakdown

### 3. **Project Management Integration**
- **Milestone Association**: Link activities to project milestones
- **Deadline Management**: Due dates, start dates, completion dates
- **Progress Reporting**: Visual progress indicators and reports
- **Deliverable Tracking**: Expected outputs and completion criteria

### 4. **Budget Planning & Tracking**
- **Cost Estimation**: Estimated cost based on hours and rates
- **Actual Cost Tracking**: Real-time cost accumulation
- **Hourly Rate Management**: User-specific or activity-specific rates
- **Budget Variance**: Planned vs actual analysis
- **Resource Cost Allocation**: Track costs per resource type

### 5. **Enhanced UI/UX**
- **Rich Form Builder**: Leverage `CEntityFormBuilder` for dynamic forms
- **Validation Framework**: Comprehensive input validation
- **User-friendly Dialogs**: Using `CWarningDialog`, `CInformationDialog`, `CExceptionDialog`
- **Responsive Design**: Desktop-first approach with mobile considerations
- **Accessibility**: ARIA-compliant and keyboard navigation support

---

## 👥 User Roles & Permissions

| Role | Permissions |
|------|-------------|
| **Project Manager** | Create, modify, delete activities; assign resources; set budgets |
| **Team Lead** | Assign team members; update progress; manage dependencies |
| **Team Member** | Update assigned activities; log time; report issues |
| **Stakeholder** | View activity progress; comment on activities |
| **Admin** | Full system access; manage activity types and workflows |

---

## 🔧 Technical Requirements

### **Domain Model Extensions**

#### **Enhanced CActivity Class**
```java
@Entity
@Table(name = "cactivity")
public class CActivity extends CEntityOfProject {
    // Existing fields
    private CActivityType activityType;
    
    // Resource Management
    private CUser assignedTo;
    private CUser createdBy;
    private Set<CActivityAssignment> assignments;
    
    // Time & Budget Tracking
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private BigDecimal remainingHours;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private BigDecimal hourlyRate;
    
    // Task Management
    private CActivityStatus status;
    private CActivityPriority priority;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate completionDate;
    private Integer progressPercentage;
    
    // Project Integration
    private CMilestone milestone;
    private CActivity parentActivity;
    private Set<CActivity> subActivities;
    private Set<CActivityDependency> dependencies;
    
    // Additional Info
    private String description;
    private String acceptanceCriteria;
    private String notes;
    private Set<CActivityComment> comments;
    private Set<CActivityAttachment> attachments;
}
```

#### **New Supporting Classes**
1. **CActivityStatus** - Enum entity for activity states
2. **CActivityPriority** - Enum entity for priority levels  
3. **CActivityAssignment** - Many-to-many relationship between activities and users
4. **CActivityDependency** - Task dependency relationships
5. **CActivityComment** - Activity comments and history
6. **CActivityAttachment** - File attachments for activities
7. **CMilestone** - Project milestone entity (if not exists)
8. **CResource** - Resource allocation entity (if not exists)

### **Service Layer Enhancements**
- **CActivityService**: Enhanced with resource management, progress calculation
- **CActivityAssignmentService**: Handle user assignments and workload
- **CActivityDependencyService**: Manage task dependencies and validation
- **CActivityTimeTrackingService**: Time logging and reporting
- **CActivityBudgetService**: Cost calculation and budget tracking

### **UI Components**
- **CActivityFormDialog**: Rich activity creation/editing form
- **CActivityGrid**: Enhanced grid with sorting, filtering, status indicators
- **CActivityKanbanBoard**: Drag-and-drop status management
- **CActivityGanttChart**: Timeline visualization
- **CActivityTimeTracker**: Time logging interface
- **CActivityDashboard**: Progress and budget overview

---

## 📊 Database Schema Changes

### **New Tables**
```sql
-- Activity status lookup
CREATE TABLE cactivitystatus (
    cactivitystatus_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    color VARCHAR(7), -- Hex color code
    is_final BOOLEAN DEFAULT FALSE
);

-- Activity priority lookup  
CREATE TABLE cactivitypriority (
    cactivitypriority_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    priority_level INTEGER NOT NULL,
    color VARCHAR(7)
);

-- Activity assignments (many-to-many)
CREATE TABLE cactivityassignment (
    cactivityassignment_id BIGSERIAL PRIMARY KEY,
    activity_id BIGINT NOT NULL REFERENCES cactivity(activity_id),
    user_id BIGINT NOT NULL REFERENCES cuser(user_id),
    role VARCHAR(100), -- e.g., 'ASSIGNEE', 'REVIEWER', 'APPROVER'
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estimated_hours DECIMAL(10,2),
    actual_hours DECIMAL(10,2)
);

-- Activity dependencies
CREATE TABLE cactivitydependency (
    cactivitydependency_id BIGSERIAL PRIMARY KEY,
    predecessor_id BIGINT NOT NULL REFERENCES cactivity(activity_id),
    successor_id BIGINT NOT NULL REFERENCES cactivity(activity_id),
    dependency_type VARCHAR(50) DEFAULT 'FINISH_TO_START',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### **Enhanced cactivity Table**
```sql
ALTER TABLE cactivity ADD COLUMN assigned_to_id BIGINT REFERENCES cuser(user_id);
ALTER TABLE cactivity ADD COLUMN created_by_id BIGINT REFERENCES cuser(user_id);
ALTER TABLE cactivity ADD COLUMN cactivitystatus_id BIGINT REFERENCES cactivitystatus(cactivitystatus_id);
ALTER TABLE cactivity ADD COLUMN cactivitypriority_id BIGINT REFERENCES cactivitypriority(cactivitypriority_id);
ALTER TABLE cactivity ADD COLUMN milestone_id BIGINT; -- References milestone table
ALTER TABLE cactivity ADD COLUMN parent_activity_id BIGINT REFERENCES cactivity(activity_id);

-- Time & Budget fields
ALTER TABLE cactivity ADD COLUMN estimated_hours DECIMAL(10,2);
ALTER TABLE cactivity ADD COLUMN actual_hours DECIMAL(10,2) DEFAULT 0;
ALTER TABLE cactivity ADD COLUMN remaining_hours DECIMAL(10,2);
ALTER TABLE cactivity ADD COLUMN estimated_cost DECIMAL(12,2);
ALTER TABLE cactivity ADD COLUMN actual_cost DECIMAL(12,2) DEFAULT 0;
ALTER TABLE cactivity ADD COLUMN hourly_rate DECIMAL(10,2);

-- Dates & Progress
ALTER TABLE cactivity ADD COLUMN start_date DATE;
ALTER TABLE cactivity ADD COLUMN due_date DATE;
ALTER TABLE cactivity ADD COLUMN completion_date DATE;
ALTER TABLE cactivity ADD COLUMN progress_percentage INTEGER DEFAULT 0;

-- Additional Info
ALTER TABLE cactivity ADD COLUMN description TEXT;
ALTER TABLE cactivity ADD COLUMN acceptance_criteria TEXT;
ALTER TABLE cactivity ADD COLUMN notes TEXT;
```

---

## 🚀 Implementation Phases

### **Phase 1: Core Domain Model** ✅
- Create enum entities (CActivityStatus, CActivityPriority)
- Update CActivity with basic enhancement fields
- Add proper validation and metadata annotations
- Update repositories and basic services

### **Phase 2: Resource Management**
- Implement CActivityAssignment entity and service
- Add user assignment functionality
- Create time tracking capabilities
- Enhance UI for resource allocation

### **Phase 3: Advanced Features**
- Implement activity dependencies
- Add milestone integration
- Create budget tracking features
- Enhance reporting capabilities

### **Phase 4: UI/UX Enhancement**
- Create rich form dialogs
- Implement Kanban board view
- Add Gantt chart visualization
- Enhance dashboard and reporting

---

## 🧪 Testing Strategy

### **Unit Tests**
- Domain model validation tests
- Service layer business logic tests
- Repository data access tests
- UI component behavior tests

### **Integration Tests**
- End-to-end activity lifecycle tests
- User role and permission tests
- Database constraint validation tests
- API endpoint tests

### **Manual Testing**
- UI usability testing
- Performance testing with large datasets
- Cross-browser compatibility testing
- Accessibility testing

---

## 📝 Coding Standards Compliance

Following `copilot-java-strict-coding-rules.md`:

- ✅ **Class Naming**: All classes start with "C" prefix
- ✅ **PostgreSQL Only**: Database schema optimized for PostgreSQL
- ✅ **Null Safety**: Comprehensive null checking in all methods
- ✅ **Logging**: Logger statements at function start with parameters
- ✅ **Base Classes**: Extend existing abstracts for consistency
- ✅ **UI Components**: Use CButton, CGrid, and other base components
- ✅ **Exception Handling**: Use CWarningDialog, CInformationDialog, CExceptionDialog
- ✅ **Validation**: Comprehensive input validation and user feedback
- ✅ **MVC Architecture**: Clear separation of concerns
- ✅ **Documentation**: Comprehensive JavaDoc and inline comments

---

## 🎯 Success Criteria

1. **Functional Requirements Met**: All specified features implemented and working
2. **Code Quality**: Passes all static analysis and follows coding standards
3. **Performance**: Supports 50+ concurrent users with responsive UI
4. **Usability**: Intuitive interface matching Jira/ProjeQtOr usability standards
5. **Test Coverage**: >90% code coverage with comprehensive test suite
6. **Documentation**: Complete technical and user documentation
7. **Scalability**: Architecture supports future enhancements and growth

---

## 📚 References

- **Atlassian Jira**: Task management and workflow inspiration
- **ProjeQtOr**: Project management feature reference (https://projeqtor.org/en)
- **Project Design Description**: `/src/docs/project_design_description.md`
- **Coding Guidelines**: `/src/docs/copilot-java-strict-coding-rules.md`
- **Vaadin Documentation**: UI component best practices
- **Spring Boot Best Practices**: Service and repository layer patterns