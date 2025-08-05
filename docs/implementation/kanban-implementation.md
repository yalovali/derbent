# 📋 Kanban Board Implementation Guide
## Enhanced Drag-and-Drop Task Management

Following strict coding guidelines from `copilot-java-strict-coding-rules.md`

---

## 🎯 Overview

This guide provides detailed implementation steps for the Enhanced Kanban Board system, inspired by JIRA and ProjeQtOr project management platforms. The implementation follows the established architectural patterns and coding standards of the Derbent project.

---

## 📊 Architecture Overview

### Package Structure
```
tech.derbent.kanban/
├── domain/
│   ├── CKanbanColumn.java        # Column status representation
│   └── CActivityCard.java        # Card display entity
├── service/
│   ├── CKanbanService.java       # Business logic
│   └── CKanbanEventService.java  # Drag-drop event handling
├── view/
│   ├── CKanbanView.java          # Main kanban page
│   ├── CKanbanBoard.java         # Board container
│   ├── CKanbanColumn.java        # Status column component
│   └── CActivityCard.java        # Draggable task card
└── tests/
    ├── CKanbanBoardTest.java     # Unit tests
    └── CKanbanViewUITest.java    # UI automation tests
```

---

## 🏗️ Implementation Steps

### Step 1: Domain Entities

#### CKanbanColumn (Display Entity)
```java
/**
 * Display entity for kanban column representation.
 * Not a database entity - represents UI state for kanban columns.
 * 
 * LAZY LOADING ARCHITECTURE:
 * - This is a UI display entity, not a database entity
 * - Column data is derived from CActivityStatus entities
 * - Activities are loaded via service layer to avoid LazyInitializationException
 */
public class CKanbanColumn {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanColumn.class);
    
    private final CActivityStatus status;
    private final List<CActivity> activities;
    private final String columnTitle;
    private final String columnColor;
    private Integer itemCount;
    
    public CKanbanColumn(final CActivityStatus status) {
        LOGGER.info("CKanbanColumn constructor called with status: {}", status);
        
        if (status == null) {
            throw new IllegalArgumentException("Activity status cannot be null");
        }
        
        this.status = status;
        this.activities = new ArrayList<>();
        this.columnTitle = status.getName();
        this.columnColor = status.getColor() != null ? status.getColor() : "#6c757d";
        this.itemCount = 0;
    }
    
    public void addActivity(final CActivity activity) {
        LOGGER.info("addActivity called with activity: {}", activity);
        
        if (activity != null && !activities.contains(activity)) {
            activities.add(activity);
            itemCount = activities.size();
        }
    }
    
    public void removeActivity(final CActivity activity) {
        LOGGER.info("removeActivity called with activity: {}", activity);
        
        if (activity != null && activities.remove(activity)) {
            itemCount = activities.size();
        }
    }
    
    // Getters and validation methods
    public boolean canAcceptActivity(final CActivity activity) {
        return activity != null && activity.getProject() != null;
    }
}
```

### Step 2: Service Layer

#### CKanbanService
```java
@Service
public class CKanbanService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanService.class);
    
    private final CActivityService activityService;
    private final CActivityStatusService activityStatusService;
    
    /**
     * Retrieves kanban board data for a specific project.
     * Uses eager loading to prevent LazyInitializationException.
     */
    public List<CKanbanColumn> getKanbanColumnsForProject(final Long projectId) {
        LOGGER.info("getKanbanColumnsForProject called with projectId: {}", projectId);
        
        if (projectId == null) {
            throw new ServiceException("Project ID cannot be null for kanban board");
        }
        
        // Get all activity statuses (these define the columns)
        final List<CActivityStatus> statuses = activityStatusService.findAllOrderedBySequence();
        
        // Get all activities for the project with eager loading
        final List<CActivity> activities = activityService.findByProjectIdWithFullData(projectId);
        
        // Build kanban columns
        final List<CKanbanColumn> columns = new ArrayList<>();
        
        for (final CActivityStatus status : statuses) {
            final CKanbanColumn column = new CKanbanColumn(status);
            
            // Add activities matching this status
            activities.stream()
                .filter(activity -> status.equals(activity.getActivityStatus()))
                .forEach(column::addActivity);
            
            columns.add(column);
        }
        
        return columns;
    }
    
    /**
     * Moves an activity from one status to another.
     * Handles the business logic and validation for status changes.
     */
    @Transactional
    public CActivity moveActivityToStatus(final Long activityId, final Long newStatusId) {
        LOGGER.info("moveActivityToStatus called with activityId: {}, newStatusId: {}", 
                   activityId, newStatusId);
        
        if (activityId == null || newStatusId == null) {
            throw new ServiceException("Activity ID and Status ID cannot be null");
        }
        
        // Get activity with eager loading
        final Optional<CActivity> activityOpt = activityService.get(activityId);
        if (activityOpt.isEmpty()) {
            throw new ServiceException("Activity not found with ID: " + activityId);
        }
        
        final CActivity activity = activityOpt.get();
        
        // Get new status
        final Optional<CActivityStatus> statusOpt = activityStatusService.get(newStatusId);
        if (statusOpt.isEmpty()) {
            throw new ServiceException("Activity status not found with ID: " + newStatusId);
        }
        
        final CActivityStatus newStatus = statusOpt.get();
        final CActivityStatus oldStatus = activity.getActivityStatus();
        
        // Validate status change
        validateStatusChange(activity, oldStatus, newStatus);
        
        // Update activity status
        activity.setActivityStatus(newStatus);
        activity.setLastModifiedDate(LocalDateTime.now());
        
        // Handle completion logic
        if (isCompletionStatus(newStatus)) {
            activity.setCompletionDate(LocalDate.now());
            activity.setProgressPercentage(100);
        } else if (isCompletionStatus(oldStatus) && !isCompletionStatus(newStatus)) {
            // Moving back from completed status
            activity.setCompletionDate(null);
            activity.setProgressPercentage(null);
        }
        
        final CActivity savedActivity = activityService.save(activity);
        
        // Publish status change event for notifications
        eventPublisher.publishEvent(new ActivityStatusChangeEvent(savedActivity, oldStatus, newStatus));
        
        return savedActivity;
    }
    
    private void validateStatusChange(final CActivity activity, 
                                    final CActivityStatus fromStatus, 
                                    final CActivityStatus toStatus) {
        // Business rules for status transitions
        if (fromStatus != null && fromStatus.getIsFinal() && !isAdminUser()) {
            throw new ServiceException("Cannot change status from final state: " + fromStatus.getName());
        }
        
        // Add more validation rules as needed
    }
    
    private boolean isCompletionStatus(final CActivityStatus status) {
        return status != null && ("DONE".equals(status.getName()) || "COMPLETED".equals(status.getName()));
    }
}
```

### Step 3: UI Components

#### CKanbanBoard (Main Board Component)
```java
/**
 * Main kanban board component that displays columns and handles drag-and-drop.
 * Follows the established UI component patterns in the Derbent project.
 */
public class CKanbanBoard extends HorizontalLayout implements HasComponents {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanBoard.class);
    
    private final CProject project;
    private final CKanbanService kanbanService;
    private final List<CKanbanColumnComponent> columnComponents;
    private final List<ComponentEventListener<ActivityStatusChangeEvent>> statusChangeListeners;
    
    public CKanbanBoard(final CProject project, final CKanbanService kanbanService) {
        LOGGER.info("CKanbanBoard constructor called with project: {}", project);
        
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null for kanban board");
        }
        
        this.project = project;
        this.kanbanService = kanbanService;
        this.columnComponents = new ArrayList<>();
        this.statusChangeListeners = new ArrayList<>();
        
        addClassName("c-kanban-board");
        setSpacing(true);
        setPadding(true);
        setWidthFull();
        setHeightFull();
        
        setupBoard();
    }
    
    private void setupBoard() {
        LOGGER.info("setupBoard called for project: {}", project.getName());
        
        try {
            final List<CKanbanColumn> columns = kanbanService.getKanbanColumnsForProject(project.getId());
            
            for (final CKanbanColumn column : columns) {
                final CKanbanColumnComponent columnComponent = new CKanbanColumnComponent(column);
                columnComponent.addActivityMoveListener(this::handleActivityMove);
                
                columnComponents.add(columnComponent);
                add(columnComponent);
            }
            
        } catch (final Exception e) {
            LOGGER.error("Error setting up kanban board for project: {}", project, e);
            showErrorMessage("Failed to load kanban board: " + e.getMessage());
        }
    }
    
    private void handleActivityMove(final ActivityMoveEvent event) {
        LOGGER.info("handleActivityMove called with event: {}", event);
        
        try {
            final CActivity activity = event.getActivity();
            final CActivityStatus newStatus = event.getNewStatus();
            
            // Move activity via service layer
            final CActivity updatedActivity = kanbanService.moveActivityToStatus(
                activity.getId(), newStatus.getId()
            );
            
            // Update UI
            refreshBoard();
            
            // Notify listeners
            fireStatusChangeEvent(new ActivityStatusChangeEvent(updatedActivity, 
                                                               event.getOldStatus(), 
                                                               newStatus));
            
            // Show success notification
            showSuccessMessage(String.format("Task '%s' moved to %s", 
                                            activity.getName(), newStatus.getName()));
            
        } catch (final Exception e) {
            LOGGER.error("Error moving activity: {}", event, e);
            showErrorMessage("Failed to move task: " + e.getMessage());
            refreshBoard(); // Revert UI changes
        }
    }
    
    public void refreshBoard() {
        LOGGER.info("refreshBoard called");
        
        removeAll();
        columnComponents.clear();
        setupBoard();
    }
    
    public void addStatusChangeListener(final ComponentEventListener<ActivityStatusChangeEvent> listener) {
        if (listener != null) {
            statusChangeListeners.add(listener);
        }
    }
    
    private void fireStatusChangeEvent(final ActivityStatusChangeEvent event) {
        statusChangeListeners.forEach(listener -> {
            try {
                listener.onComponentEvent(event);
            } catch (final Exception e) {
                LOGGER.error("Error in status change listener", e);
            }
        });
    }
    
    private void showSuccessMessage(final String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    private void showErrorMessage(final String message) {
        Notification.show(message, 5000, Notification.Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
```

#### CKanbanColumnComponent (Individual Column)
```java
/**
 * Individual kanban column component with drag-and-drop capabilities.
 * Represents a single status column in the kanban board.
 */
public class CKanbanColumnComponent extends VerticalLayout implements DropTarget<CKanbanColumnComponent> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanColumnComponent.class);
    
    private final CKanbanColumn column;
    private final List<CActivityCardComponent> activityCards;
    private final List<ComponentEventListener<ActivityMoveEvent>> moveListeners;
    
    private H3 columnHeader;
    private Badge itemCountBadge;
    private VerticalLayout cardContainer;
    
    public CKanbanColumnComponent(final CKanbanColumn column) {
        LOGGER.info("CKanbanColumnComponent constructor called with column: {}", column);
        
        if (column == null) {
            throw new IllegalArgumentException("Kanban column cannot be null");
        }
        
        this.column = column;
        this.activityCards = new ArrayList<>();
        this.moveListeners = new ArrayList<>();
        
        addClassName("c-kanban-column");
        setSpacing(true);
        setPadding(true);
        setWidth("300px");
        setHeightFull();
        
        setupColumn();
        setupDragAndDrop();
    }
    
    private void setupColumn() {
        // Column header with title and count
        final HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);
        
        columnHeader = new H3(column.getColumnTitle());
        columnHeader.addClassName("column-header");
        columnHeader.getStyle().set("color", getTextColorForBackground(column.getColumnColor()));
        
        itemCountBadge = new Badge(String.valueOf(column.getItemCount()));
        itemCountBadge.addThemeVariants(BadgeVariant.LUMO_SMALL);
        
        headerLayout.add(columnHeader, itemCountBadge);
        
        // Set column background color
        getStyle().set("background-color", column.getColumnColor() + "20"); // 20% opacity
        getStyle().set("border-left", "4px solid " + column.getColumnColor());
        
        // Card container
        cardContainer = new VerticalLayout();
        cardContainer.setSpacing(true);
        cardContainer.setPadding(false);
        cardContainer.setWidthFull();
        cardContainer.addClassName("card-container");
        
        add(headerLayout, cardContainer);
        
        // Add activity cards
        loadActivityCards();
    }
    
    private void loadActivityCards() {
        cardContainer.removeAll();
        activityCards.clear();
        
        for (final CActivity activity : column.getActivities()) {
            final CActivityCardComponent card = new CActivityCardComponent(activity);
            card.addDragStartListener(this::handleDragStart);
            card.addDragEndListener(this::handleDragEnd);
            
            activityCards.add(card);
            cardContainer.add(card);
        }
        
        updateItemCount();
    }
    
    private void setupDragAndDrop() {
        // Configure as drop target
        setDropEffect(DropEffect.MOVE);
        
        addDropListener(event -> {
            final Optional<Component> draggedComponent = event.getDragSourceComponent();
            
            if (draggedComponent.isPresent() && draggedComponent.get() instanceof CActivityCardComponent) {
                final CActivityCardComponent draggedCard = (CActivityCardComponent) draggedComponent.get();
                final CActivity activity = draggedCard.getActivity();
                
                handleActivityDrop(activity);
            }
        });
    }
    
    private void handleActivityDrop(final CActivity activity) {
        LOGGER.info("handleActivityDrop called with activity: {} to column: {}", 
                   activity, column.getColumnTitle());
        
        if (column.canAcceptActivity(activity)) {
            final CActivityStatus oldStatus = activity.getActivityStatus();
            final CActivityStatus newStatus = column.getStatus();
            
            if (!Objects.equals(oldStatus, newStatus)) {
                // Fire move event to parent board
                final ActivityMoveEvent moveEvent = new ActivityMoveEvent(
                    this, false, activity, oldStatus, newStatus
                );
                
                fireMoveEvent(moveEvent);
            }
        }
    }
    
    private void updateItemCount() {
        itemCountBadge.setText(String.valueOf(activityCards.size()));
    }
    
    public void addActivityMoveListener(final ComponentEventListener<ActivityMoveEvent> listener) {
        if (listener != null) {
            moveListeners.add(listener);
        }
    }
    
    private void fireMoveEvent(final ActivityMoveEvent event) {
        moveListeners.forEach(listener -> {
            try {
                listener.onComponentEvent(event);
            } catch (final Exception e) {
                LOGGER.error("Error in activity move listener", e);
            }
        });
    }
    
    private String getTextColorForBackground(final String backgroundColor) {
        // Simple contrast calculation - in practice, use the established CColorUtils
        return "#000000"; // Default to black for now
    }
}
```

### Step 4: Main Kanban View

#### CKanbanView
```java
@Route("kanban")
@PageTitle("Kanban Board")
@Menu(order = 150, icon = VaadinIcon.KANBAN)
@PermitAll
public class CKanbanView extends CProjectAwareMDPage<CActivity> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanView.class);
    
    private final CKanbanService kanbanService;
    
    private CKanbanBoard kanbanBoard;
    private HorizontalLayout filterLayout;
    private ComboBox<CUser> assigneeFilter;
    private ComboBox<CActivityPriority> priorityFilter;
    private TextField searchField;
    
    public CKanbanView(final CKanbanService kanbanService) {
        super("Kanban Board", CActivity.class);
        
        LOGGER.info("CKanbanView constructor called");
        
        this.kanbanService = kanbanService;
    }
    
    @Override
    protected void setupView() {
        LOGGER.info("setupView called");
        
        super.setupView();
        
        addClassName("c-kanban-view");
        setSizeFull();
        
        setupFilters();
        setupKanbanBoard();
    }
    
    private void setupFilters() {
        filterLayout = new HorizontalLayout();
        filterLayout.setWidthFull();
        filterLayout.setSpacing(true);
        filterLayout.setPadding(true);
        filterLayout.addClassName("kanban-filters");
        
        // Search field
        searchField = new TextField();
        searchField.setPlaceholder("Search tasks...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.addValueChangeListener(event -> applyFilters());
        
        // Assignee filter
        assigneeFilter = new ComboBox<>("Assignee");
        assigneeFilter.setItemLabelGenerator(CUser::getName);
        assigneeFilter.addValueChangeListener(event -> applyFilters());
        
        // Priority filter
        priorityFilter = new ComboBox<>("Priority");
        priorityFilter.setItemLabelGenerator(CActivityPriority::getName);
        priorityFilter.addValueChangeListener(event -> applyFilters());
        
        // Clear filters button
        final CButton clearFiltersButton = CButton.createSecondary("Clear Filters", VaadinIcon.CLOSE);
        clearFiltersButton.addClickListener(event -> clearFilters());
        
        filterLayout.add(searchField, assigneeFilter, priorityFilter, clearFiltersButton);
        
        add(filterLayout);
    }
    
    private void setupKanbanBoard() {
        final CProject currentProject = getCurrentProject();
        
        if (currentProject == null) {
            showProjectSelectionMessage();
            return;
        }
        
        try {
            kanbanBoard = new CKanbanBoard(currentProject, kanbanService);
            kanbanBoard.addStatusChangeListener(this::handleStatusChange);
            
            add(kanbanBoard);
            
        } catch (final Exception e) {
            LOGGER.error("Error setting up kanban board", e);
            showErrorNotification("Failed to load kanban board: " + e.getMessage());
        }
    }
    
    private void handleStatusChange(final ActivityStatusChangeEvent event) {
        LOGGER.info("handleStatusChange called with event: {}", event);
        
        // Refresh filters if needed
        loadFilterData();
        
        // Show success notification (already handled by kanban board)
    }
    
    @Override
    protected void onProjectChange(final ProjectChangeEvent event) {
        LOGGER.info("onProjectChange called with event: {}", event);
        
        super.onProjectChange(event);
        
        if (kanbanBoard != null) {
            remove(kanbanBoard);
        }
        
        setupKanbanBoard();
        loadFilterData();
    }
    
    private void loadFilterData() {
        final CProject currentProject = getCurrentProject();
        
        if (currentProject != null) {
            // Load assignees for the current project
            final List<CUser> projectUsers = activityService.findAssignedUsersForProject(currentProject.getId());
            assigneeFilter.setItems(projectUsers);
            
            // Load priorities
            final List<CActivityPriority> priorities = activityPriorityService.findAll();
            priorityFilter.setItems(priorities);
        }
    }
    
    private void applyFilters() {
        // Implementation for filtering kanban board based on selected criteria
        // This would update the kanban board to show only matching activities
        LOGGER.info("applyFilters called");
        
        if (kanbanBoard != null) {
            // Apply filters and refresh board
            // Implementation details would depend on how filtering is implemented
            kanbanBoard.refreshBoard();
        }
    }
    
    private void clearFilters() {
        searchField.clear();
        assigneeFilter.clear();
        priorityFilter.clear();
        applyFilters();
    }
    
    private void showProjectSelectionMessage() {
        final Div messageDiv = new Div();
        messageDiv.addClassName("project-selection-message");
        messageDiv.add(new H3("Please select a project to view the kanban board"));
        
        add(messageDiv);
    }
}
```

---

## 🎨 CSS Styling

### kanban-styles.css
```css
/* Kanban Board Styles */
.c-kanban-board {
    background: var(--lumo-base-color);
    overflow-x: auto;
    min-height: 100%;
}

.c-kanban-column {
    background: var(--lumo-contrast-5pct);
    border-radius: var(--lumo-border-radius-m);
    box-shadow: var(--lumo-box-shadow-xs);
    transition: box-shadow 0.2s ease;
}

.c-kanban-column:hover {
    box-shadow: var(--lumo-box-shadow-s);
}

.c-kanban-column.drag-over {
    background: var(--lumo-primary-color-10pct);
    border: 2px dashed var(--lumo-primary-color);
}

.column-header {
    margin: 0;
    font-size: var(--lumo-font-size-l);
    font-weight: 600;
}

.card-container {
    min-height: 200px;
    transition: background-color 0.2s ease;
}

/* Activity Card Styles */
.c-activity-card {
    background: white;
    border-radius: var(--lumo-border-radius-m);
    box-shadow: var(--lumo-box-shadow-xs);
    padding: var(--lumo-space-m);
    margin-bottom: var(--lumo-space-s);
    cursor: grab;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.c-activity-card:hover {
    transform: translateY(-2px);
    box-shadow: var(--lumo-box-shadow-m);
}

.c-activity-card.dragging {
    opacity: 0.8;
    transform: rotate(5deg);
    cursor: grabbing;
}

.activity-title {
    font-weight: 600;
    color: var(--lumo-header-text-color);
    margin-bottom: var(--lumo-space-xs);
}

.activity-assignee {
    display: flex;
    align-items: center;
    gap: var(--lumo-space-xs);
    color: var(--lumo-secondary-text-color);
    font-size: var(--lumo-font-size-s);
}

.activity-due-date {
    color: var(--lumo-secondary-text-color);
    font-size: var(--lumo-font-size-s);
}

.activity-due-date.overdue {
    color: var(--lumo-error-text-color);
    font-weight: 600;
}

.activity-priority {
    padding: 2px 8px;
    border-radius: var(--lumo-border-radius-s);
    font-size: var(--lumo-font-size-xs);
    font-weight: 600;
    text-transform: uppercase;
}

.priority-critical { background: #ff4757; color: white; }
.priority-high { background: #ff6b35; color: white; }
.priority-medium { background: #ffa726; color: white; }
.priority-low { background: #66bb6a; color: white; }
.priority-lowest { background: #95a5a6; color: white; }

/* Filter Layout */
.kanban-filters {
    background: var(--lumo-contrast-5pct);
    border-bottom: 1px solid var(--lumo-contrast-10pct);
}

/* Project Selection Message */
.project-selection-message {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 300px;
    color: var(--lumo-secondary-text-color);
}
```

---

## 🧪 Testing Implementation

### Unit Tests
```java
@SpringBootTest
class CKanbanServiceTest {
    
    @Autowired
    private CKanbanService kanbanService;
    
    @MockBean
    private CActivityService activityService;
    
    @MockBean
    private CActivityStatusService activityStatusService;
    
    @Test
    void testGetKanbanColumnsForProject() {
        // Given
        final Long projectId = 1L;
        final List<CActivityStatus> statuses = createMockStatuses();
        final List<CActivity> activities = createMockActivities();
        
        when(activityStatusService.findAllOrderedBySequence()).thenReturn(statuses);
        when(activityService.findByProjectIdWithFullData(projectId)).thenReturn(activities);
        
        // When
        final List<CKanbanColumn> columns = kanbanService.getKanbanColumnsForProject(projectId);
        
        // Then
        assertNotNull(columns);
        assertEquals(4, columns.size()); // TODO, IN_PROGRESS, REVIEW, DONE
        
        // Verify column data
        final CKanbanColumn todoColumn = columns.get(0);
        assertEquals("TODO", todoColumn.getColumnTitle());
        assertEquals(2, todoColumn.getItemCount());
    }
    
    @Test
    void testMoveActivityToStatus() {
        // Given
        final Long activityId = 1L;
        final Long newStatusId = 2L;
        
        final CActivity activity = createMockActivity();
        final CActivityStatus newStatus = createMockStatus("IN_PROGRESS");
        
        when(activityService.get(activityId)).thenReturn(Optional.of(activity));
        when(activityStatusService.get(newStatusId)).thenReturn(Optional.of(newStatus));
        when(activityService.save(any(CActivity.class))).thenReturn(activity);
        
        // When
        final CActivity result = kanbanService.moveActivityToStatus(activityId, newStatusId);
        
        // Then
        assertNotNull(result);
        assertEquals(newStatus, result.getActivityStatus());
        verify(activityService).save(activity);
    }
    
    @Test
    void testMoveActivityToStatusWithNullIds() {
        // When/Then
        assertThrows(ServiceException.class, () -> 
            kanbanService.moveActivityToStatus(null, 1L));
        
        assertThrows(ServiceException.class, () -> 
            kanbanService.moveActivityToStatus(1L, null));
    }
}
```

### UI Tests
```java
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class CKanbanViewUITest extends CBaseUITest {
    
    @Test
    @Order(1)
    void testKanbanViewNavigation() {
        // Navigate to kanban view
        navigateToView(CKanbanView.class);
        
        // Verify page title
        assertPageTitle("Kanban Board");
        
        // Verify kanban board is displayed
        assertElementExists(".c-kanban-board");
    }
    
    @Test
    @Order(2)
    void testKanbanColumnsDisplay() {
        navigateToView(CKanbanView.class);
        
        // Verify columns are displayed
        assertElementExists(".c-kanban-column");
        
        // Verify column headers
        assertTextExists("TODO");
        assertTextExists("IN_PROGRESS");
        assertTextExists("REVIEW");
        assertTextExists("DONE");
    }
    
    @Test
    @Order(3)
    void testActivityCardDisplay() {
        navigateToView(CKanbanView.class);
        
        // Verify activity cards are displayed
        assertElementExists(".c-activity-card");
        
        // Verify card content
        assertElementExists(".activity-title");
        assertElementExists(".activity-assignee");
        assertElementExists(".activity-due-date");
    }
    
    @Test
    @Order(4)
    void testDragAndDropFunctionality() {
        navigateToView(CKanbanView.class);
        
        // Find first activity card
        final WebElement sourceCard = findElement(".c-activity-card");
        final WebElement targetColumn = findElement(".c-kanban-column:nth-child(2)");
        
        // Perform drag and drop
        performDragAndDrop(sourceCard, targetColumn);
        
        // Verify success notification
        assertNotificationExists("Task moved to");
        
        // Take screenshot for verification
        takeScreenshot("kanban-drag-drop-success", false);
    }
    
    @Test
    @Order(5)
    void testFilterFunctionality() {
        navigateToView(CKanbanView.class);
        
        // Test search filter
        final WebElement searchField = findElement("vaadin-text-field[placeholder='Search tasks...']");
        searchField.sendKeys("test");
        
        // Verify filtered results
        waitForElement(".c-activity-card");
        
        // Clear filters
        clickElement("vaadin-button:contains('Clear Filters')");
        
        // Verify all cards visible again
        assertElementCount(".c-activity-card", greaterThan(0));
    }
}
```

---

## 📚 Additional Implementation Notes

### Performance Considerations
1. **Lazy Loading**: Use eager loading for kanban data to prevent LazyInitializationException
2. **Caching**: Consider caching kanban column data for frequently accessed projects
3. **Pagination**: For projects with many activities, implement virtual scrolling or pagination
4. **Real-time Updates**: Consider WebSocket integration for real-time board updates

### Security Considerations
1. **Access Control**: Verify user has access to project before displaying kanban board
2. **Status Change Permissions**: Validate user permissions for status changes
3. **Input Validation**: Validate all drag-and-drop operations

### Accessibility
1. **Keyboard Navigation**: Implement keyboard shortcuts for moving cards
2. **Screen Reader Support**: Add appropriate ARIA labels and descriptions
3. **Color Contrast**: Ensure sufficient contrast for all status colors

### Mobile Responsiveness
1. **Touch Support**: Implement touch-based drag and drop for mobile devices
2. **Responsive Layout**: Adapt column layout for smaller screens
3. **Gesture Support**: Add swipe gestures for mobile interaction

---

This implementation guide provides a comprehensive foundation for building the Enhanced Kanban Board following the Derbent project's coding standards and architectural patterns.