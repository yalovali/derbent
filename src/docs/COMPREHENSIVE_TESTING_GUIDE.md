# 🧪 Comprehensive Testing Guide
## Testing Strategy for Derbent Project Management Application

Following strict coding guidelines from `copilot-java-strict-coding-rules.md`

---

## 📝 Overview

This guide provides comprehensive testing strategies and implementation details for all features in the Derbent project management application. Each testing approach follows the established testing architecture and ensures compliance with the project's quality standards.

---

## 🏗️ Testing Architecture

### Testing Structure Overview
```
src/test/java/
├── unit-tests/                    # Business logic and service tests (80%+ coverage)
│   ├── abstracts/tests/           # Generic test superclasses and utilities
│   ├── activities/tests/          # Activity management tests
│   ├── timetracking/tests/        # Time tracking system tests
│   ├── kanban/tests/              # Kanban board tests
│   ├── dashboard/tests/           # Dashboard and KPI tests
│   ├── search/tests/              # Search functionality tests
│   ├── templates/tests/           # Template system tests
│   └── notifications/tests/       # Notification system tests
├── ui-tests/                      # Vaadin UI component tests
│   ├── kanban/tests/              # Kanban board UI tests
│   ├── timetracking/tests/        # Time tracking UI tests
│   ├── dashboard/tests/           # Dashboard widget UI tests
│   └── search/tests/              # Search interface UI tests
└── automated-tests/               # Playwright automation tests
    ├── workflows/                 # End-to-end workflow tests
    ├── performance/               # Load and performance tests
    ├── accessibility/             # WCAG compliance tests
    └── cross-browser/             # Browser compatibility tests
```

---

## 🎯 Testing Standards per Priority Level

### 🔴 CRITICAL Priority Features

#### 1. Kanban Board Testing

**Unit Tests**
```java
@SpringBootTest
class CKanbanServiceTest {
    
    @Autowired
    private CKanbanService kanbanService;
    
    @MockBean
    private CActivityService activityService;
    
    @MockBean
    private CActivityStatusService activityStatusService;
    
    @MockBean
    private ApplicationEventPublisher eventPublisher;
    
    @Test
    @DisplayName("Should create kanban columns for project with correct activity distribution")
    void testGetKanbanColumnsForProject() {
        // Given
        final Long projectId = 1L;
        final List<CActivityStatus> statuses = createMockStatuses();
        final List<CActivity> activities = createMockActivitiesWithStatuses();
        
        when(activityStatusService.findAllOrderedBySequence()).thenReturn(statuses);
        when(activityService.findByProjectIdWithFullData(projectId)).thenReturn(activities);
        
        // When
        final List<CKanbanColumn> columns = kanbanService.getKanbanColumnsForProject(projectId);
        
        // Then
        assertNotNull(columns);
        assertEquals(4, columns.size(), "Should have 4 status columns");
        
        // Verify column data distribution
        final CKanbanColumn todoColumn = findColumnByStatus(columns, "TODO");
        assertNotNull(todoColumn, "TODO column should exist");
        assertEquals(2, todoColumn.getItemCount(), "TODO column should have 2 items");
        
        final CKanbanColumn progressColumn = findColumnByStatus(columns, "IN_PROGRESS");
        assertEquals(3, progressColumn.getItemCount(), "IN_PROGRESS column should have 3 items");
        
        final CKanbanColumn doneColumn = findColumnByStatus(columns, "DONE");
        assertEquals(5, doneColumn.getItemCount(), "DONE column should have 5 items");
    }
    
    @Test
    @DisplayName("Should move activity to new status and update timestamps")
    void testMoveActivityToStatus() {
        // Given
        final Long activityId = 1L;
        final Long newStatusId = 2L;
        
        final CActivity activity = createMockActivity("Test Activity", "TODO");
        final CActivityStatus newStatus = createMockStatus("IN_PROGRESS");
        final CActivityStatus oldStatus = activity.getActivityStatus();
        
        when(activityService.get(activityId)).thenReturn(Optional.of(activity));
        when(activityStatusService.get(newStatusId)).thenReturn(Optional.of(newStatus));
        when(activityService.save(any(CActivity.class))).thenReturn(activity);
        
        // When
        final CActivity result = kanbanService.moveActivityToStatus(activityId, newStatusId);
        
        // Then
        assertNotNull(result);
        assertEquals(newStatus, result.getActivityStatus());
        assertNotNull(result.getLastModifiedDate());
        
        // Verify event publishing
        verify(eventPublisher).publishEvent(any(ActivityStatusChangeEvent.class));
        verify(activityService).save(activity);
    }
    
    @Test
    @DisplayName("Should handle completion status change correctly")
    void testMoveActivityToCompletionStatus() {
        // Given
        final CActivity activity = createMockActivity("Test Activity", "IN_PROGRESS");
        final CActivityStatus completedStatus = createMockStatus("DONE");
        
        when(activityService.get(activity.getId())).thenReturn(Optional.of(activity));
        when(activityStatusService.get(completedStatus.getId())).thenReturn(Optional.of(completedStatus));
        when(activityService.save(any(CActivity.class))).thenReturn(activity);
        
        // When
        final CActivity result = kanbanService.moveActivityToStatus(activity.getId(), completedStatus.getId());
        
        // Then
        assertEquals(completedStatus, result.getActivityStatus());
        assertNotNull(result.getCompletionDate(), "Completion date should be set");
        assertEquals(Integer.valueOf(100), result.getProgressPercentage(), "Progress should be 100%");
    }
    
    @Test
    @DisplayName("Should validate status change permissions")
    void testStatusChangeValidation() {
        // Given
        final CActivity activity = createMockActivity("Test Activity", "DONE");
        activity.getActivityStatus().setIsFinal(true);
        final CActivityStatus newStatus = createMockStatus("TODO");
        
        when(activityService.get(activity.getId())).thenReturn(Optional.of(activity));
        when(activityStatusService.get(newStatus.getId())).thenReturn(Optional.of(newStatus));
        
        // When/Then
        assertThrows(ServiceException.class, () -> 
            kanbanService.moveActivityToStatus(activity.getId(), newStatus.getId()),
            "Should not allow changes from final status"
        );
    }
    
    @Test
    @DisplayName("Should handle null parameters gracefully")
    void testNullParameterHandling() {
        assertThrows(ServiceException.class, () -> 
            kanbanService.getKanbanColumnsForProject(null));
        
        assertThrows(ServiceException.class, () -> 
            kanbanService.moveActivityToStatus(null, 1L));
        
        assertThrows(ServiceException.class, () -> 
            kanbanService.moveActivityToStatus(1L, null));
    }
    
    // Helper methods for test data creation
    private List<CActivityStatus> createMockStatuses() {
        return Arrays.asList(
            createMockStatus("TODO"),
            createMockStatus("IN_PROGRESS"), 
            createMockStatus("REVIEW"),
            createMockStatus("DONE")
        );
    }
    
    private CActivityStatus createMockStatus(final String name) {
        final CActivityStatus status = new CActivityStatus();
        status.setId((long) name.hashCode());
        status.setName(name);
        status.setColor("#" + Integer.toHexString(name.hashCode()).substring(0, 6));
        status.setIsFinal("DONE".equals(name));
        return status;
    }
    
    private List<CActivity> createMockActivitiesWithStatuses() {
        final List<CActivity> activities = new ArrayList<>();
        
        // Create activities with different statuses
        activities.add(createActivityWithStatus("Activity 1", "TODO"));
        activities.add(createActivityWithStatus("Activity 2", "TODO"));
        activities.add(createActivityWithStatus("Activity 3", "IN_PROGRESS"));
        activities.add(createActivityWithStatus("Activity 4", "IN_PROGRESS"));
        activities.add(createActivityWithStatus("Activity 5", "IN_PROGRESS"));
        activities.add(createActivityWithStatus("Activity 6", "DONE"));
        activities.add(createActivityWithStatus("Activity 7", "DONE"));
        activities.add(createActivityWithStatus("Activity 8", "DONE"));
        activities.add(createActivityWithStatus("Activity 9", "DONE"));
        activities.add(createActivityWithStatus("Activity 10", "DONE"));
        
        return activities;
    }
    
    private CActivity createActivityWithStatus(final String name, final String statusName) {
        final CActivity activity = new CActivity();
        activity.setId((long) (name + statusName).hashCode());
        activity.setName(name);
        activity.setActivityStatus(createMockStatus(statusName));
        return activity;
    }
    
    private CKanbanColumn findColumnByStatus(final List<CKanbanColumn> columns, final String statusName) {
        return columns.stream()
            .filter(column -> statusName.equals(column.getColumnTitle()))
            .findFirst()
            .orElse(null);
    }
}
```

**UI Tests**
```java
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class CKanbanViewUITest extends CBaseUITest {
    
    @Test
    @Order(1)
    @DisplayName("Should navigate to kanban view and display board")
    void testKanbanViewNavigation() {
        // Navigate to kanban view
        navigateToView(CKanbanView.class);
        
        // Verify page title and basic elements
        assertPageTitle("Kanban Board");
        assertElementExists(".c-kanban-view");
        assertElementExists(".kanban-filters");
        
        // Verify project selection message if no project selected
        if (!hasCurrentProject()) {
            assertTextExists("Please select a project");
        } else {
            assertElementExists(".c-kanban-board");
        }
    }
    
    @Test
    @Order(2)
    @DisplayName("Should display kanban columns with correct headers")
    void testKanbanColumnsDisplay() {
        selectTestProject();
        navigateToView(CKanbanView.class);
        
        // Verify columns are displayed
        assertElementCount(".c-kanban-column", 4);
        
        // Verify column headers
        assertTextExists("TODO");
        assertTextExists("IN_PROGRESS");
        assertTextExists("REVIEW");
        assertTextExists("DONE");
        
        // Verify item counts are displayed
        assertElementExists(".item-count-badge");
        
        takeScreenshot("kanban-columns-display", false);
    }
    
    @Test
    @Order(3)
    @DisplayName("Should display activity cards with correct information")
    void testActivityCardDisplay() {
        selectTestProject();
        navigateToView(CKanbanView.class);
        
        // Wait for cards to load
        waitForElement(".c-activity-card");
        
        // Verify card content elements
        assertElementExists(".activity-title");
        assertElementExists(".activity-assignee");
        assertElementExists(".activity-due-date");
        assertElementExists(".activity-priority");
        
        // Verify card styling based on priority
        assertElementExists(".priority-high, .priority-medium, .priority-low");
        
        takeScreenshot("kanban-activity-cards", false);
    }
    
    @Test
    @Order(4)
    @DisplayName("Should perform drag and drop between columns")
    void testDragAndDropFunctionality() {
        selectTestProject();
        navigateToView(CKanbanView.class);
        
        // Wait for board to load
        waitForElement(".c-activity-card");
        
        // Find source card and target column
        final WebElement sourceCard = findElement(".c-activity-card");
        final String originalCardText = sourceCard.getText();
        
        final WebElement targetColumn = findElement(".c-kanban-column:nth-child(2) .card-container");
        
        // Record original positions
        final String sourceColumnClass = findParentColumn(sourceCard).getAttribute("class");
        
        // Perform drag and drop
        performDragAndDrop(sourceCard, targetColumn);
        
        // Wait for the operation to complete
        waitForNotification();
        
        // Verify success notification
        assertNotificationExists("Task moved to");
        
        // Verify card moved to new column
        final WebElement movedCard = findElementByText(originalCardText);
        final String newColumnClass = findParentColumn(movedCard).getAttribute("class");
        
        assertNotEquals(sourceColumnClass, newColumnClass, "Card should be in different column");
        
        takeScreenshot("kanban-drag-drop-success", false);
    }
    
    @Test
    @Order(5)
    @DisplayName("Should filter activities using search and filters")
    void testFilterFunctionality() {
        selectTestProject();
        navigateToView(CKanbanView.class);
        
        // Test search filter
        final WebElement searchField = findElement("vaadin-text-field[placeholder*='Search']");
        final int originalCardCount = countElements(".c-activity-card");
        
        // Enter search term
        searchField.clear();
        searchField.sendKeys("test");
        
        // Wait for filtering
        waitForCardCountChange(originalCardCount);
        
        // Verify filtered results
        final int filteredCardCount = countElements(".c-activity-card");
        assertTrue(filteredCardCount <= originalCardCount, "Should show fewer or same cards after filtering");
        
        // Test assignee filter
        clickElement("vaadin-combo-box[label='Assignee']");
        selectComboBoxItem("John Doe");
        
        // Wait for filter application
        waitForCardCountChange(filteredCardCount);
        
        // Clear filters
        clickElement("vaadin-button:contains('Clear Filters')");
        
        // Wait for all cards to return
        waitForCardCountChange(countElements(".c-activity-card"));
        
        // Verify all cards visible again
        assertEquals(originalCardCount, countElements(".c-activity-card"), 
                    "Should show all cards after clearing filters");
        
        takeScreenshot("kanban-filtering", false);
    }
    
    @Test
    @Order(6)
    @DisplayName("Should handle error scenarios gracefully")
    void testErrorHandling() {
        // Test with invalid project
        navigateToView(CKanbanView.class);
        
        // Simulate network error or invalid data
        // This would depend on how we want to simulate errors
        
        // Verify error notification or fallback UI
        // assertElementExists(".error-message");
        
        takeScreenshot("kanban-error-handling", false);
    }
    
    @Test
    @Order(7)
    @DisplayName("Should be responsive on different screen sizes")
    void testResponsiveDesign() {
        selectTestProject();
        navigateToView(CKanbanView.class);
        
        // Test desktop size
        resizeBrowserWindow(1920, 1080);
        waitForElement(".c-kanban-board");
        takeScreenshot("kanban-desktop", false);
        
        // Test tablet size
        resizeBrowserWindow(768, 1024);
        waitForElement(".c-kanban-board");
        takeScreenshot("kanban-tablet", false);
        
        // Test mobile size
        resizeBrowserWindow(375, 667);
        waitForElement(".c-kanban-board");
        takeScreenshot("kanban-mobile", false);
        
        // Restore original size
        resizeBrowserWindow(1280, 720);
    }
    
    // Helper methods
    private void selectTestProject() {
        // Implementation depends on project selection mechanism
        clickElement(".project-selector");
        selectComboBoxItem("Test Project");
    }
    
    private boolean hasCurrentProject() {
        return elementExists(".c-kanban-board");
    }
    
    private WebElement findParentColumn(final WebElement card) {
        return card.findElement(By.xpath("./ancestor::*[contains(@class, 'c-kanban-column')]"));
    }
    
    private void waitForCardCountChange(final int originalCount) {
        await().atMost(Duration.ofSeconds(5))
               .until(() -> countElements(".c-activity-card") != originalCount);
    }
    
    private void waitForNotification() {
        waitForElement("vaadin-notification");
    }
    
    private void performDragAndDrop(final WebElement source, final WebElement target) {
        new Actions(driver)
            .dragAndDrop(source, target)
            .perform();
    }
}
```

#### 2. Time Tracking Testing

**Unit Tests**
```java
@SpringBootTest
class CTimeTrackingServiceTest {
    
    @Autowired
    private CTimeTrackingService timeTrackingService;
    
    @MockBean
    private CTimeLogRepository timeLogRepository;
    
    @MockBean
    private CTimeTrackingSessionRepository sessionRepository;
    
    @MockBean
    private CActivityService activityService;
    
    @MockBean
    private CUserService userService;
    
    @MockBean
    private CTimeCategoryService timeCategoryService;
    
    @Test
    @DisplayName("Should start time tracking session successfully")
    void testStartTimeTracking() {
        // Given
        final Long activityId = 1L;
        final Long userId = 1L;
        final Long categoryId = 1L;
        final String description = "Working on feature";
        
        final CActivity activity = createMockActivity();
        final CUser user = createMockUser();
        final CTimeCategory category = createMockTimeCategory();
        
        when(sessionRepository.findActiveSessionForUser(userId)).thenReturn(Optional.empty());
        when(activityService.get(activityId)).thenReturn(Optional.of(activity));
        when(userService.get(userId)).thenReturn(Optional.of(user));
        when(timeCategoryService.get(categoryId)).thenReturn(Optional.of(category));
        when(sessionRepository.save(any(CTimeTrackingSession.class))).thenAnswer(i -> i.getArgument(0));
        
        // When
        final CTimeTrackingSession session = timeTrackingService.startTimeTracking(
            activityId, userId, categoryId, description
        );
        
        // Then
        assertNotNull(session);
        assertEquals(activity, session.getActivity());
        assertEquals(user, session.getUser());
        assertEquals(category, session.getTimeCategory());
        assertEquals(description, session.getDescription());
        assertNotNull(session.getStartTime());
        assertFalse(session.getIsPaused());
        
        verify(sessionRepository).save(any(CTimeTrackingSession.class));
        verify(eventPublisher).publishEvent(any(TimeTrackingStartedEvent.class));
    }
    
    @Test
    @DisplayName("Should prevent multiple active sessions for same user")
    void testPreventMultipleActiveSessions() {
        // Given
        final Long userId = 1L;
        final CTimeTrackingSession existingSession = createMockSession();
        
        when(sessionRepository.findActiveSessionForUser(userId)).thenReturn(Optional.of(existingSession));
        
        // When/Then
        assertThrows(ServiceException.class, () -> 
            timeTrackingService.startTimeTracking(1L, userId, 1L, "description"),
            "Should not allow multiple active sessions"
        );
    }
    
    @Test
    @DisplayName("Should stop time tracking and create time log")
    void testStopTimeTracking() {
        // Given
        final Long userId = 1L;
        final CTimeTrackingSession session = createMockSession();
        session.setStartTime(LocalDateTime.now().minusHours(2));
        
        when(sessionRepository.findActiveSessionForUser(userId)).thenReturn(Optional.of(session));
        when(timeLogRepository.save(any(CTimeLog.class))).thenAnswer(i -> i.getArgument(0));
        
        // When
        final CTimeLog timeLog = timeTrackingService.stopTimeTracking(userId);
        
        // Then
        assertNotNull(timeLog);
        assertEquals(session.getActivity(), timeLog.getActivity());
        assertEquals(session.getUser(), timeLog.getUser());
        assertEquals(session.getTimeCategory(), timeLog.getTimeCategory());
        assertNotNull(timeLog.getStartTime());
        assertNotNull(timeLog.getEndTime());
        assertNotNull(timeLog.getDurationHours());
        assertTrue(timeLog.getDurationHours().compareTo(BigDecimal.ZERO) > 0);
        
        verify(sessionRepository).delete(session);
        verify(timeLogRepository).save(any(CTimeLog.class));
        verify(eventPublisher).publishEvent(any(TimeTrackingStoppedEvent.class));
        verify(eventPublisher).publishEvent(any(TimeLogCreatedEvent.class));
    }
    
    @Test
    @DisplayName("Should pause and resume time tracking")
    void testPauseAndResumeTimeTracking() {
        // Given
        final Long userId = 1L;
        final CTimeTrackingSession session = createMockSession();
        session.setIsPaused(false);
        
        when(sessionRepository.findActiveSessionForUser(userId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(CTimeTrackingSession.class))).thenAnswer(i -> i.getArgument(0));
        
        // When - Pause
        final CTimeTrackingSession pausedSession = timeTrackingService.pauseTimeTracking(userId);
        
        // Then - Verify paused
        assertTrue(pausedSession.getIsPaused());
        verify(eventPublisher).publishEvent(any(TimeTrackingPausedEvent.class));
        
        // When - Resume
        pausedSession.setIsPaused(true); // Simulate paused state
        final CTimeTrackingSession resumedSession = timeTrackingService.resumeTimeTracking(userId);
        
        // Then - Verify resumed
        assertFalse(resumedSession.getIsPaused());
        verify(eventPublisher).publishEvent(any(TimeTrackingResumedEvent.class));
    }
    
    @Test
    @DisplayName("Should create manual time log with validation")
    void testCreateManualTimeLog() {
        // Given
        final Long activityId = 1L;
        final Long userId = 1L;
        final LocalDate logDate = LocalDate.now().minusDays(1);
        final BigDecimal duration = new BigDecimal("2.5");
        final String description = "Manual work entry";
        final Long categoryId = 1L;
        
        final CActivity activity = createMockActivity();
        final CUser user = createMockUser();
        final CTimeCategory category = createMockTimeCategory();
        
        when(activityService.get(activityId)).thenReturn(Optional.of(activity));
        when(userService.get(userId)).thenReturn(Optional.of(user));
        when(timeCategoryService.get(categoryId)).thenReturn(Optional.of(category));
        when(timeLogRepository.save(any(CTimeLog.class))).thenAnswer(i -> i.getArgument(0));
        
        // When
        final CTimeLog timeLog = timeTrackingService.createManualTimeLog(
            activityId, userId, logDate, duration, description, categoryId
        );
        
        // Then
        assertNotNull(timeLog);
        assertEquals(activity, timeLog.getActivity());
        assertEquals(user, timeLog.getUser());
        assertEquals(logDate, timeLog.getLogDate());
        assertEquals(duration, timeLog.getDurationHours());
        assertEquals(description, timeLog.getDescription());
        assertEquals(category, timeLog.getTimeCategory());
        
        verify(timeLogRepository).save(any(CTimeLog.class));
        verify(eventPublisher).publishEvent(any(TimeLogCreatedEvent.class));
    }
    
    @Test
    @DisplayName("Should validate manual time log parameters")
    void testManualTimeLogValidation() {
        // Test null activity ID
        assertThrows(ServiceException.class, () -> 
            timeTrackingService.createManualTimeLog(null, 1L, LocalDate.now(), 
                                                   BigDecimal.ONE, "desc", 1L));
        
        // Test zero duration
        assertThrows(ServiceException.class, () -> 
            timeTrackingService.createManualTimeLog(1L, 1L, LocalDate.now(), 
                                                   BigDecimal.ZERO, "desc", 1L));
        
        // Test negative duration
        assertThrows(ServiceException.class, () -> 
            timeTrackingService.createManualTimeLog(1L, 1L, LocalDate.now(), 
                                                   new BigDecimal("-1"), "desc", 1L));
        
        // Test future date
        assertThrows(ServiceException.class, () -> 
            timeTrackingService.createManualTimeLog(1L, 1L, LocalDate.now().plusDays(1), 
                                                   BigDecimal.ONE, "desc", 1L));
    }
    
    @Test
    @DisplayName("Should calculate total hours for activity correctly")
    void testCalculateTotalHoursForActivity() {
        // Given
        final Long activityId = 1L;
        final BigDecimal expectedTotal = new BigDecimal("15.50");
        
        when(timeLogRepository.calculateTotalHoursForActivity(activityId)).thenReturn(expectedTotal);
        
        // When
        final BigDecimal totalHours = timeTrackingService.calculateTotalHoursForActivity(activityId);
        
        // Then
        assertEquals(expectedTotal, totalHours);
        verify(timeLogRepository).calculateTotalHoursForActivity(activityId);
    }
    
    @Test
    @DisplayName("Should handle cleanup of old sessions")
    void testCleanupOldSessions() {
        // Given
        final LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        final List<CTimeTrackingSession> oldSessions = Arrays.asList(
            createMockSession(),
            createMockSession()
        );
        
        when(sessionRepository.findSessionsOlderThan(any(LocalDateTime.class))).thenReturn(oldSessions);
        when(timeLogRepository.save(any(CTimeLog.class))).thenAnswer(i -> i.getArgument(0));
        
        // When
        timeTrackingService.cleanupOldSessions();
        
        // Then
        verify(sessionRepository).findSessionsOlderThan(any(LocalDateTime.class));
        verify(timeLogRepository, times(2)).save(any(CTimeLog.class));
        verify(sessionRepository, times(2)).delete(any(CTimeTrackingSession.class));
    }
    
    // Helper methods for test data creation
    private CActivity createMockActivity() {
        final CActivity activity = new CActivity();
        activity.setId(1L);
        activity.setName("Test Activity");
        return activity;
    }
    
    private CUser createMockUser() {
        final CUser user = new CUser();
        user.setId(1L);
        user.setName("Test User");
        return user;
    }
    
    private CTimeCategory createMockTimeCategory() {
        final CTimeCategory category = new CTimeCategory();
        category.setId(1L);
        category.setName("Development");
        category.setIsBillableDefault(true);
        category.setDefaultHourlyRate(new BigDecimal("50.00"));
        return category;
    }
    
    private CTimeTrackingSession createMockSession() {
        final CTimeTrackingSession session = new CTimeTrackingSession();
        session.setId(1L);
        session.setActivity(createMockActivity());
        session.setUser(createMockUser());
        session.setTimeCategory(createMockTimeCategory());
        session.setStartTime(LocalDateTime.now().minusHours(1));
        session.setIsPaused(false);
        return session;
    }
}
```

---

## 🟠 HIGH Priority Feature Testing

#### 3. Dashboard Testing

**Unit Tests**
```java
@SpringBootTest
class CDashboardWidgetTest {
    
    @Test
    @DisplayName("Should calculate project health metrics correctly")
    void testProjectHealthMetricsCalculation() {
        // Given
        final CProject project = createMockProject();
        final List<CActivity> activities = createMockActivitiesForHealthTest();
        
        when(activityService.findByProjectId(project.getId())).thenReturn(activities);
        
        // When
        final ProjectHealthMetrics metrics = dashboardService.calculateHealthMetrics(project);
        
        // Then
        assertNotNull(metrics);
        assertEquals(10, metrics.getTotalActivities());
        assertEquals(4, metrics.getCompletedActivities());
        assertEquals(2, metrics.getOverdueActivities());
        assertEquals(1, metrics.getAtRiskActivities());
        assertEquals(40.0, metrics.getCompletionPercentage(), 0.1);
    }
    
    @Test
    @DisplayName("Should calculate team workload correctly")
    void testTeamWorkloadCalculation() {
        // Given
        final CProject project = createMockProject();
        final List<CUser> teamMembers = createMockTeamMembers();
        final Map<CUser, List<CActivity>> userActivities = createUserActivityMap();
        
        // When
        final Map<CUser, WorkloadMetrics> workload = 
            dashboardService.calculateTeamWorkload(project, teamMembers);
        
        // Then
        assertNotNull(workload);
        assertEquals(3, workload.size());
        
        final WorkloadMetrics johnMetrics = workload.get(findUserByName(teamMembers, "John"));
        assertEquals(85.0, johnMetrics.getWorkloadPercentage(), 0.1);
        assertEquals(142.0, johnMetrics.getTotalHours(), 0.1);
        assertEquals(18, johnMetrics.getTaskCount());
    }
}
```

**Performance Tests**
```java
@SpringBootTest
class DashboardPerformanceTest {
    
    @Test
    @DisplayName("Should load dashboard widgets within acceptable time")
    void testDashboardLoadTime() {
        // Given
        final CProject largeProject = createProjectWithManyActivities(1000);
        
        // When
        final long startTime = System.currentTimeMillis();
        final List<CDashboardWidget> widgets = dashboardService.loadDashboardWidgets(largeProject);
        final long endTime = System.currentTimeMillis();
        
        // Then
        final long loadTime = endTime - startTime;
        assertTrue(loadTime < 2000, "Dashboard should load within 2 seconds"); // 2 second SLA
        assertNotNull(widgets);
        assertFalse(widgets.isEmpty());
    }
    
    @Test
    @DisplayName("Should handle concurrent dashboard requests efficiently")
    void testConcurrentDashboardAccess() throws InterruptedException {
        // Given
        final int numberOfUsers = 10;
        final CountDownLatch latch = new CountDownLatch(numberOfUsers);
        final List<Long> loadTimes = Collections.synchronizedList(new ArrayList<>());
        
        // When
        for (int i = 0; i < numberOfUsers; i++) {
            new Thread(() -> {
                try {
                    final long startTime = System.currentTimeMillis();
                    dashboardService.loadDashboardWidgets(createMockProject());
                    final long endTime = System.currentTimeMillis();
                    loadTimes.add(endTime - startTime);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        latch.await(30, TimeUnit.SECONDS);
        
        // Then
        assertEquals(numberOfUsers, loadTimes.size());
        final double averageLoadTime = loadTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        assertTrue(averageLoadTime < 3000, "Average load time should be under 3 seconds");
    }
}
```

---

## 🧪 Automated End-to-End Testing

### Workflow Tests
```java
@SpringBootTest
class ProjectManagementWorkflowTest extends CBaseAutomatedTest {
    
    @Test
    @DisplayName("Complete project management workflow")
    void testCompleteProjectWorkflow() {
        // Step 1: Login and setup
        applicationLogin("project.manager@test.com", "test123");
        
        // Step 2: Create new project
        navigateToView(CProjectsView.class);
        clickNew();
        fillProjectForm("Test Project", "2024-01-01", "2024-06-01", "50000");
        clickSave();
        assertNotificationExists("Project created successfully");
        
        // Step 3: Create activities using template
        navigateToView(CActivitiesView.class);
        clickElement("button:contains('Create from Template')");
        selectTemplate("Web Development");
        clickNext();
        customizeTemplateSettings();
        clickCreateActivities();
        assertNotificationExists("Activities created from template");
        
        // Step 4: Setup kanban board
        navigateToView(CKanbanView.class);
        assertElementCount(".c-activity-card", greaterThan(0));
        
        // Step 5: Start time tracking
        clickFirstActivityCard();
        clickElement("button:contains('Start Tracking')");
        selectTimeCategory("Development");
        enterDescription("Working on user interface");
        clickStart();
        assertNotificationExists("Time tracking started");
        
        // Step 6: Move activity through workflow
        dragActivityToColumn("IN_PROGRESS");
        assertNotificationExists("Task moved to IN_PROGRESS");
        
        // Step 7: Add comments and updates
        clickActivityCard();
        addComment("Good progress on this feature");
        updateProgress(75);
        clickSave();
        
        // Step 8: Complete activity
        stopTimeTracking();
        dragActivityToColumn("DONE");
        assertNotificationExists("Task moved to DONE");
        
        // Step 9: Verify dashboard updates
        navigateToView(CDashboardView.class);
        assertElementExists(".project-progress:contains('25%')"); // 1 of 4 activities done
        
        // Step 10: Generate report
        navigateToView(CReportsView.class);
        selectReportType("Project Summary");
        generateReport();
        assertElementExists(".report-content");
        
        takeScreenshot("complete-workflow-success", false);
    }
    
    @Test
    @DisplayName("Time tracking workflow with multiple sessions")
    void testTimeTrackingWorkflow() {
        applicationLogin("developer@test.com", "test123");
        selectTestProject();
        
        // Start tracking
        navigateToView(CActivitiesView.class);
        selectActivity("Implement user authentication");
        clickTimeTrackingTab();
        startTimeTracking("Development", "Working on login form");
        
        // Pause and resume
        waitSeconds(5);
        pauseTimeTracking();
        assertTimerPaused();
        
        waitSeconds(3);
        resumeTimeTracking();
        assertTimerRunning();
        
        // Stop and verify log creation
        waitSeconds(10);
        stopTimeTracking();
        assertTimeLogCreated();
        assertActivityHoursUpdated();
        
        // Create manual time entry
        addManualTimeEntry("2024-01-15", "3.5", "Testing", "Code review and testing");
        assertTimeLogAdded();
        
        // Verify timesheet
        navigateToView(CTimesheetView.class);
        selectWeek("2024-01-15");
        assertTimesheetEntries(2);
        submitTimesheet();
        assertNotificationExists("Timesheet submitted");
        
        takeScreenshot("time-tracking-workflow", false);
    }
}
```

### Cross-Browser Testing
```java
@ParameterizedTest
@ValueSource(strings = {"chrome", "firefox", "edge"})
@DisplayName("Should work correctly across different browsers")
void testCrossBrowserCompatibility(final String browserName) {
    // Setup browser-specific configuration
    setupBrowser(browserName);
    
    // Execute core functionality tests
    testBasicNavigation();
    testKanbanDragAndDrop();
    testTimeTrackingInterface();
    testDashboardDisplay();
    
    // Take browser-specific screenshots
    takeScreenshot("cross-browser-" + browserName, false);
}
```

---

## 📊 Performance Testing

### Load Testing
```java
@LoadTest
class ApplicationLoadTest {
    
    @Test
    @DisplayName("Should handle 100 concurrent users")
    void testConcurrentUserLoad() {
        final int numberOfUsers = 100;
        final ExecutorService executor = Executors.newFixedThreadPool(numberOfUsers);
        final CountDownLatch latch = new CountDownLatch(numberOfUsers);
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger errorCount = new AtomicInteger(0);
        
        for (int i = 0; i < numberOfUsers; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    simulateUserSession(userId);
                    successCount.incrementAndGet();
                } catch (final Exception e) {
                    errorCount.incrementAndGet();
                    LOGGER.error("User session {} failed", userId, e);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all users to complete
        assertTrue(latch.await(60, TimeUnit.SECONDS), "All user sessions should complete within 60 seconds");
        
        // Verify success rate
        final double successRate = (double) successCount.get() / numberOfUsers;
        assertTrue(successRate >= 0.95, "Success rate should be at least 95%");
        
        LOGGER.info("Load test completed: {} successes, {} errors, {}% success rate", 
                   successCount.get(), errorCount.get(), successRate * 100);
    }
    
    private void simulateUserSession(final int userId) {
        // Simulate typical user workflow
        final String userEmail = "user" + userId + "@test.com";
        
        // Login
        applicationLogin(userEmail, "test123");
        
        // Navigate between views
        navigateToView(CDashboardView.class);
        waitForDashboardLoad();
        
        navigateToView(CActivitiesView.class);
        waitForActivitiesLoad();
        
        navigateToView(CKanbanView.class);
        waitForKanbanLoad();
        
        // Perform some actions
        if (userId % 10 == 0) { // 10% of users create activities
            createNewActivity();
        }
        
        if (userId % 5 == 0) { // 20% of users start time tracking
            startTimeTracking();
            waitSeconds(5);
            stopTimeTracking();
        }
        
        // Logout
        logout();
    }
}
```

### Memory Testing
```java
@Test
@DisplayName("Should not have memory leaks during long sessions")
void testMemoryUsage() {
    final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    final long initialMemory = memoryBean.getHeapMemoryUsage().getUsed();
    
    // Simulate long user session
    applicationLogin("test@test.com", "test123");
    
    for (int i = 0; i < 100; i++) {
        // Perform memory-intensive operations
        navigateToView(CDashboardView.class);
        loadLargeDashboard();
        
        navigateToView(CKanbanView.class);
        loadKanbanWithManyCards();
        
        navigateToView(CReportsView.class);
        generateLargeReport();
        
        // Force garbage collection periodically
        if (i % 10 == 0) {
            System.gc();
            waitSeconds(2);
        }
    }
    
    // Check final memory usage
    System.gc();
    waitSeconds(5);
    
    final long finalMemory = memoryBean.getHeapMemoryUsage().getUsed();
    final long memoryIncrease = finalMemory - initialMemory;
    final double increasePercentage = (double) memoryIncrease / initialMemory * 100;
    
    // Memory increase should be reasonable (less than 50% increase)
    assertTrue(increasePercentage < 50.0, 
              String.format("Memory increase should be less than 50%%, was %.2f%%", increasePercentage));
    
    LOGGER.info("Memory usage: Initial: {} MB, Final: {} MB, Increase: {:.2f}%", 
               initialMemory / 1024 / 1024, finalMemory / 1024 / 1024, increasePercentage);
}
```

---

## ♿ Accessibility Testing

```java
@Test
@DisplayName("Should meet WCAG 2.1 AA accessibility standards")
void testAccessibilityCompliance() {
    navigateToView(CKanbanView.class);
    
    // Test keyboard navigation
    testKeyboardNavigation();
    
    // Test screen reader compatibility
    testScreenReaderLabels();
    
    // Test color contrast
    testColorContrast();
    
    // Test focus management
    testFocusManagement();
    
    // Generate accessibility report
    generateAccessibilityReport();
}

private void testKeyboardNavigation() {
    // Test tab navigation through kanban board
    pressKey("Tab");
    assertElementHasFocus(".c-activity-card");
    
    // Test arrow key navigation
    pressKey("ArrowRight");
    assertElementHasFocus(".c-kanban-column:nth-child(2)");
    
    // Test Enter key activation
    pressKey("Enter");
    assertDialogOpened();
}

private void testScreenReaderLabels() {
    // Verify ARIA labels are present
    assertElementHasAttribute(".c-activity-card", "aria-label");
    assertElementHasAttribute(".c-kanban-column", "aria-labelledby");
    
    // Verify role attributes
    assertElementHasAttribute(".c-kanban-board", "role", "application");
    assertElementHasAttribute(".c-activity-card", "role", "button");
}
```

---

## 📋 Testing Execution Commands

### Maven Test Commands
```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest="**/unit-tests/**/*Test"

# Run only UI tests  
mvn test -Dtest="**/ui-tests/**/*Test"

# Run only automated tests
mvn test -Dtest="**/automated-tests/**/*Test"

# Run specific test class
mvn test -Dtest="CKanbanServiceTest"

# Run tests with coverage report
mvn test jacoco:report

# Run performance tests
mvn test -Dtest="**/*PerformanceTest" -Dspring.profiles.active=performance

# Run accessibility tests
mvn test -Dtest="**/*AccessibilityTest" -Dspring.profiles.active=accessibility
```

### Playwright Test Commands
```bash
# Run all Playwright tests
./run-playwright-tests.sh all

# Run specific test suite
./run-playwright-tests.sh kanban

# Run tests with specific browser
./run-playwright-tests.sh all --browser=firefox

# Run tests in headless mode
./run-playwright-tests.sh all --headless

# Run tests with video recording
./run-playwright-tests.sh all --video
```

---

## 📈 Test Coverage Goals

### Coverage Targets
- **Unit Tests**: 80%+ coverage for service and domain layers
- **Integration Tests**: 70%+ coverage for repository and controller layers  
- **UI Tests**: 60%+ coverage for view components
- **End-to-End Tests**: 100% coverage of critical user workflows

### Quality Gates
- All tests must pass before code merge
- No decrease in test coverage allowed
- Performance tests must meet SLA requirements
- Accessibility tests must pass WCAG 2.1 AA standards

---

This comprehensive testing guide provides the foundation for ensuring quality across all features in the Derbent project management application. Each testing approach follows established patterns and maintains consistency with the project's architectural standards.