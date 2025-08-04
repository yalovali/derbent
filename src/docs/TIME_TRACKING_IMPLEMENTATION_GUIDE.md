# ⏱️ Time Tracking Implementation Guide
## Advanced Activity Time Management System

Following strict coding guidelines from `copilot-java-strict-coding-rules.md`

---

## 🎯 Overview

This guide provides detailed implementation steps for the Advanced Time Tracking system, inspired by modern project management tools like JIRA and ProjeQtOr. The implementation includes real-time time tracking, timesheet management, and comprehensive time analytics.

---

## 📊 Architecture Overview

### Package Structure
```
tech.derbent.timetracking/
├── domain/
│   ├── CTimeLog.java             # Time entry entity
│   ├── CTimeCategory.java        # Time categorization
│   ├── CTimesheetEntry.java      # Timesheet line item
│   └── CTimeTrackingSession.java # Active tracking session
├── service/
│   ├── CTimeTrackingService.java # Core time tracking logic
│   ├── CTimesheetService.java    # Timesheet management
│   └── CTimeAnalyticsService.java # Time analytics and reporting
├── view/
│   ├── CTimeTrackingView.java    # Main time tracking interface
│   ├── CTimeTrackingPanel.java   # Time entry widget
│   ├── CTimesheetView.java       # Weekly timesheet view
│   └── CTimeLogDialog.java       # Quick time entry dialog
└── tests/
    ├── CTimeTrackingServiceTest.java # Unit tests
    └── CTimeTrackingViewUITest.java  # UI automation tests
```

---

## 🏗️ Implementation Steps

### Step 1: Domain Entities

#### CTimeLog (Primary Time Entry Entity)
```java
/**
 * Time log entity for tracking work time on activities.
 * Follows the established entity patterns in the Derbent project.
 * 
 * LAZY LOADING ARCHITECTURE:
 * - Uses @ManyToOne(fetch = FetchType.LAZY) for activity and category relationships
 * - Service layer implements eager loading via custom repository query: findByIdWithActivityAndCategory()
 * - UI components must access relationships through service layer to avoid LazyInitializationException
 */
@Entity
@Table(name = "ctime_log")
@AttributeOverride(name = "id", column = @Column(name = "time_log_id"))
public class CTimeLog extends CEntityBase {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CTimeLog.class);
    
    @MetaData(
        displayName = "Activity", required = true, readOnly = false,
        description = "Activity for which time is being tracked", hidden = false, order = 1,
        dataProviderBean = "CActivityService"
    )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private CActivity activity;
    
    @MetaData(
        displayName = "User", required = true, readOnly = false,
        description = "User who logged the time", hidden = false, order = 2,
        dataProviderBean = "CUserService"
    )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private CUser user;
    
    @MetaData(
        displayName = "Start Time", required = true, readOnly = false,
        description = "When the work started", hidden = false, order = 3
    )
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @MetaData(
        displayName = "End Time", required = false, readOnly = false,
        description = "When the work ended", hidden = false, order = 4
    )
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @MetaData(
        displayName = "Duration (Hours)", required = false, readOnly = true,
        description = "Calculated duration in hours", hidden = false, order = 5
    )
    @Column(name = "duration_hours", precision = 10, scale = 2)
    private BigDecimal durationHours;
    
    @MetaData(
        displayName = "Description", required = false, readOnly = false,
        description = "Description of work performed", hidden = false, order = 6,
        maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
    )
    @Column(name = "description", length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
    private String description;
    
    @MetaData(
        displayName = "Time Category", required = true, readOnly = false,
        description = "Category of work performed", hidden = false, order = 7,
        dataProviderBean = "CTimeCategoryService"
    )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CTimeCategory timeCategory;
    
    @MetaData(
        displayName = "Is Billable", required = true, readOnly = false,
        description = "Whether this time is billable", hidden = false, order = 8
    )
    @Column(name = "is_billable", nullable = false)
    private Boolean isBillable = true;
    
    @MetaData(
        displayName = "Hourly Rate", required = false, readOnly = false,
        description = "Hourly rate for this time entry", hidden = false, order = 9
    )
    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;
    
    @MetaData(
        displayName = "Total Cost", required = false, readOnly = true,
        description = "Calculated total cost (duration * rate)", hidden = false, order = 10
    )
    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;
    
    @MetaData(
        displayName = "Log Date", required = true, readOnly = false,
        description = "Date when work was performed", hidden = false, order = 11
    )
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;
    
    @MetaData(
        displayName = "Is Submitted", required = true, readOnly = false,
        description = "Whether timesheet entry is submitted", hidden = false, order = 12
    )
    @Column(name = "is_submitted", nullable = false)
    private Boolean isSubmitted = false;
    
    @MetaData(
        displayName = "Is Approved", required = true, readOnly = false,
        description = "Whether timesheet entry is approved", hidden = false, order = 13
    )
    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved = false;
    
    // Default constructor for JPA
    public CTimeLog() {
        super();
    }
    
    // Constructor for manual time entry
    public CTimeLog(final CActivity activity, final CUser user, final LocalDate logDate) {
        super();
        
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be null for time log");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null for time log");
        }
        if (logDate == null) {
            throw new IllegalArgumentException("Log date cannot be null for time log");
        }
        
        this.activity = activity;
        this.user = user;
        this.logDate = logDate;
        this.isBillable = true;
        this.isSubmitted = false;
        this.isApproved = false;
    }
    
    // Constructor for timer-based entry
    public CTimeLog(final CActivity activity, final CUser user, final LocalDateTime startTime) {
        this(activity, user, startTime.toLocalDate());
        this.startTime = startTime;
    }
    
    /**
     * Auto-calculate duration and cost when entity is persisted or updated.
     */
    @PrePersist
    @PreUpdate
    private void calculateDerivedFields() {
        calculateDuration();
        calculateTotalCost();
        
        if (logDate == null && startTime != null) {
            logDate = startTime.toLocalDate();
        }
    }
    
    /**
     * Calculate duration in hours based on start and end time.
     */
    private void calculateDuration() {
        if (startTime != null && endTime != null) {
            final long minutes = Duration.between(startTime, endTime).toMinutes();
            durationHours = BigDecimal.valueOf(minutes / 60.0)
                .setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    /**
     * Calculate total cost based on duration and hourly rate.
     */
    private void calculateTotalCost() {
        if (durationHours != null && hourlyRate != null) {
            totalCost = durationHours.multiply(hourlyRate)
                .setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    /**
     * Start time tracking session.
     */
    public void startTracking() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
            endTime = null;
            durationHours = null;
            totalCost = null;
        }
    }
    
    /**
     * Stop time tracking session.
     */
    public void stopTracking() {
        if (startTime != null && endTime == null) {
            endTime = LocalDateTime.now();
            calculateDuration();
            calculateTotalCost();
        }
    }
    
    /**
     * Check if this time log represents an active tracking session.
     */
    public Boolean isActiveTracking() {
        return startTime != null && endTime == null;
    }
    
    /**
     * Get tracking duration for active sessions.
     */
    public Duration getCurrentTrackingDuration() {
        if (isActiveTracking()) {
            return Duration.between(startTime, LocalDateTime.now());
        }
        return Duration.ZERO;
    }
    
    /**
     * Validate time log data.
     */
    public void validateTimeLog() {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalStateException("End time cannot be before start time");
        }
        
        if (durationHours != null && durationHours.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Duration cannot be negative");
        }
        
        if (logDate != null && logDate.isAfter(LocalDate.now())) {
            throw new IllegalStateException("Log date cannot be in the future");
        }
    }
    
    // Standard getters and setters with proper validation
    public void setDurationHours(final BigDecimal durationHours) {
        if (durationHours != null && durationHours.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Duration hours cannot be negative");
        }
        this.durationHours = durationHours;
        calculateTotalCost();
    }
    
    public void setHourlyRate(final BigDecimal hourlyRate) {
        if (hourlyRate != null && hourlyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Hourly rate cannot be negative");
        }
        this.hourlyRate = hourlyRate;
        calculateTotalCost();
    }
    
    @Override
    public String toString() {
        return String.format("CTimeLog{id=%d, activity='%s', user='%s', duration=%s, date=%s}",
                           getId(),
                           activity != null ? activity.getName() : "null",
                           user != null ? user.getName() : "null",
                           durationHours,
                           logDate);
    }
    
    // Additional getters and setters...
    // (Standard getters and setters for all fields)
}
```

#### CTimeCategory (Work Category Entity)
```java
/**
 * Time category entity for categorizing different types of work.
 * Extends CTypeEntity following the established pattern for type entities.
 */
@Entity
@Table(name = "ctime_category")
@AttributeOverride(name = "id", column = @Column(name = "category_id"))
public class CTimeCategory extends CTypeEntity {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CTimeCategory.class);
    
    @MetaData(
        displayName = "Is Billable by Default", required = true, readOnly = false,
        description = "Whether this category is billable by default", hidden = false, order = 4
    )
    @Column(name = "is_billable_default", nullable = false)
    private Boolean isBillableDefault = true;
    
    @MetaData(
        displayName = "Default Hourly Rate", required = false, readOnly = false,
        description = "Default hourly rate for this category", hidden = false, order = 5
    )
    @Column(name = "default_hourly_rate", precision = 10, scale = 2)
    private BigDecimal defaultHourlyRate;
    
    @MetaData(
        displayName = "Category Icon", required = false, readOnly = false,
        description = "VaadinIcon name for this category", hidden = false, order = 6
    )
    @Column(name = "category_icon", length = 50)
    private String categoryIcon;
    
    // Default constructor
    public CTimeCategory() {
        super();
    }
    
    // Constructor with required fields
    public CTimeCategory(final String name, final String description) {
        super(name, description);
        this.isBillableDefault = true;
    }
    
    // Constructor with all key fields
    public CTimeCategory(final String name, final String description, 
                        final Boolean isBillableDefault, final BigDecimal defaultHourlyRate) {
        super(name, description);
        this.isBillableDefault = isBillableDefault != null ? isBillableDefault : true;
        this.defaultHourlyRate = defaultHourlyRate;
    }
    
    // Standard getters and setters with validation
    public void setDefaultHourlyRate(final BigDecimal defaultHourlyRate) {
        if (defaultHourlyRate != null && defaultHourlyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Default hourly rate cannot be negative");
        }
        this.defaultHourlyRate = defaultHourlyRate;
    }
    
    public Boolean getIsBillableDefault() {
        return isBillableDefault != null ? isBillableDefault : true;
    }
    
    public void setIsBillableDefault(final Boolean isBillableDefault) {
        this.isBillableDefault = isBillableDefault != null ? isBillableDefault : true;
    }
    
    public String getCategoryIcon() {
        return categoryIcon;
    }
    
    public void setCategoryIcon(final String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }
    
    public BigDecimal getDefaultHourlyRate() {
        return defaultHourlyRate;
    }
    
    @Override
    public String toString() {
        return String.format("CTimeCategory{id=%d, name='%s', billable=%s, rate=%s}",
                           getId(), getName(), isBillableDefault, defaultHourlyRate);
    }
}
```

#### CTimeTrackingSession (Active Session Entity)
```java
/**
 * Entity for managing active time tracking sessions.
 * Represents ongoing time tracking that hasn't been stopped yet.
 */
@Entity
@Table(name = "ctime_tracking_session")
@AttributeOverride(name = "id", column = @Column(name = "session_id"))
public class CTimeTrackingSession extends CEntityBase {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CTimeTrackingSession.class);
    
    @MetaData(
        displayName = "Activity", required = true, readOnly = false,
        description = "Activity being tracked", hidden = false, order = 1,
        dataProviderBean = "CActivityService"
    )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private CActivity activity;
    
    @MetaData(
        displayName = "User", required = true, readOnly = false,
        description = "User tracking time", hidden = false, order = 2,
        dataProviderBean = "CUserService"
    )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private CUser user;
    
    @MetaData(
        displayName = "Start Time", required = true, readOnly = false,
        description = "When tracking started", hidden = false, order = 3
    )
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @MetaData(
        displayName = "Last Update", required = false, readOnly = true,
        description = "Last update timestamp", hidden = false, order = 4
    )
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
    
    @MetaData(
        displayName = "Description", required = false, readOnly = false,
        description = "Current work description", hidden = false, order = 5,
        maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
    )
    @Column(name = "description", length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
    private String description;
    
    @MetaData(
        displayName = "Time Category", required = true, readOnly = false,
        description = "Category of work being performed", hidden = false, order = 6,
        dataProviderBean = "CTimeCategoryService"
    )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CTimeCategory timeCategory;
    
    @MetaData(
        displayName = "Is Paused", required = true, readOnly = false,
        description = "Whether tracking is currently paused", hidden = false, order = 7
    )
    @Column(name = "is_paused", nullable = false)
    private Boolean isPaused = false;
    
    @MetaData(
        displayName = "Total Pause Duration", required = false, readOnly = true,
        description = "Total time paused in minutes", hidden = false, order = 8
    )
    @Column(name = "total_pause_duration")
    private Integer totalPauseDurationMinutes = 0;
    
    // Default constructor
    public CTimeTrackingSession() {
        super();
    }
    
    // Constructor for new session
    public CTimeTrackingSession(final CActivity activity, final CUser user, final CTimeCategory category) {
        super();
        
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be null for tracking session");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null for tracking session");
        }
        if (category == null) {
            throw new IllegalArgumentException("Time category cannot be null for tracking session");
        }
        
        this.activity = activity;
        this.user = user;
        this.timeCategory = category;
        this.startTime = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
        this.isPaused = false;
        this.totalPauseDurationMinutes = 0;
    }
    
    /**
     * Get current tracking duration excluding paused time.
     */
    public Duration getCurrentDuration() {
        if (startTime == null) {
            return Duration.ZERO;
        }
        
        final Duration totalDuration = Duration.between(startTime, LocalDateTime.now());
        final Duration pauseDuration = Duration.ofMinutes(totalPauseDurationMinutes != null ? totalPauseDurationMinutes : 0);
        
        return totalDuration.minus(pauseDuration);
    }
    
    /**
     * Get current duration in hours as BigDecimal.
     */
    public BigDecimal getCurrentDurationHours() {
        final Duration duration = getCurrentDuration();
        return BigDecimal.valueOf(duration.toMinutes() / 60.0)
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Pause the tracking session.
     */
    public void pause() {
        if (!isPaused) {
            isPaused = true;
            lastUpdate = LocalDateTime.now();
        }
    }
    
    /**
     * Resume the tracking session.
     */
    public void resume() {
        if (isPaused && lastUpdate != null) {
            final Duration pauseDuration = Duration.between(lastUpdate, LocalDateTime.now());
            totalPauseDurationMinutes += (int) pauseDuration.toMinutes();
            isPaused = false;
            lastUpdate = LocalDateTime.now();
        }
    }
    
    /**
     * Convert this session to a completed time log.
     */
    public CTimeLog toTimeLog() {
        final CTimeLog timeLog = new CTimeLog(activity, user, startTime);
        timeLog.setEndTime(LocalDateTime.now());
        timeLog.setDescription(description);
        timeLog.setTimeCategory(timeCategory);
        
        // Apply category defaults
        if (timeCategory != null) {
            timeLog.setIsBillable(timeCategory.getIsBillableDefault());
            timeLog.setHourlyRate(timeCategory.getDefaultHourlyRate());
        }
        
        return timeLog;
    }
    
    @PreUpdate
    private void updateLastUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }
    
    // Standard getters and setters...
    public CActivity getActivity() { return activity; }
    public void setActivity(final CActivity activity) { this.activity = activity; }
    
    public CUser getUser() { return user; }
    public void setUser(final CUser user) { this.user = user; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(final LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(final LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
    
    public String getDescription() { return description; }
    public void setDescription(final String description) { this.description = description; }
    
    public CTimeCategory getTimeCategory() { return timeCategory; }
    public void setTimeCategory(final CTimeCategory timeCategory) { this.timeCategory = timeCategory; }
    
    public Boolean getIsPaused() { return isPaused != null ? isPaused : false; }
    public void setIsPaused(final Boolean isPaused) { this.isPaused = isPaused != null ? isPaused : false; }
    
    public Integer getTotalPauseDurationMinutes() { return totalPauseDurationMinutes != null ? totalPauseDurationMinutes : 0; }
    public void setTotalPauseDurationMinutes(final Integer totalPauseDurationMinutes) { 
        this.totalPauseDurationMinutes = totalPauseDurationMinutes != null ? totalPauseDurationMinutes : 0; 
    }
    
    @Override
    public String toString() {
        return String.format("CTimeTrackingSession{id=%d, activity='%s', user='%s', duration=%s, paused=%s}",
                           getId(),
                           activity != null ? activity.getName() : "null",
                           user != null ? user.getName() : "null",
                           getCurrentDurationHours(),
                           isPaused);
    }
}
```

### Step 2: Repository Layer

#### CTimeLogRepository
```java
@Repository
public interface CTimeLogRepository extends CEntityRepository<CTimeLog> {
    
    /**
     * Find time logs by activity ID with eager loading.
     * Prevents LazyInitializationException by loading related entities.
     */
    @Query("SELECT tl FROM CTimeLog tl " +
           "LEFT JOIN FETCH tl.activity a " +
           "LEFT JOIN FETCH tl.user u " +
           "LEFT JOIN FETCH tl.timeCategory tc " +
           "WHERE a.id = :activityId " +
           "ORDER BY tl.logDate DESC, tl.startTime DESC")
    List<CTimeLog> findByActivityIdWithFullData(@Param("activityId") Long activityId);
    
    /**
     * Find time logs by user and date range with eager loading.
     */
    @Query("SELECT tl FROM CTimeLog tl " +
           "LEFT JOIN FETCH tl.activity a " +
           "LEFT JOIN FETCH tl.timeCategory tc " +
           "WHERE tl.user.id = :userId " +
           "AND tl.logDate BETWEEN :startDate AND :endDate " +
           "ORDER BY tl.logDate DESC, tl.startTime DESC")
    List<CTimeLog> findByUserAndDateRangeWithFullData(@Param("userId") Long userId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
    
    /**
     * Find time logs by project ID.
     */
    @Query("SELECT tl FROM CTimeLog tl " +
           "LEFT JOIN FETCH tl.activity a " +
           "LEFT JOIN FETCH tl.user u " +
           "LEFT JOIN FETCH tl.timeCategory tc " +
           "WHERE a.project.id = :projectId " +
           "ORDER BY tl.logDate DESC, tl.startTime DESC")
    List<CTimeLog> findByProjectIdWithFullData(@Param("projectId") Long projectId);
    
    /**
     * Calculate total hours for activity.
     */
    @Query("SELECT COALESCE(SUM(tl.durationHours), 0) FROM CTimeLog tl WHERE tl.activity.id = :activityId")
    BigDecimal calculateTotalHoursForActivity(@Param("activityId") Long activityId);
    
    /**
     * Calculate total hours for user in date range.
     */
    @Query("SELECT COALESCE(SUM(tl.durationHours), 0) FROM CTimeLog tl " +
           "WHERE tl.user.id = :userId " +
           "AND tl.logDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalHoursForUserInPeriod(@Param("userId") Long userId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
    
    /**
     * Calculate total cost for project.
     */
    @Query("SELECT COALESCE(SUM(tl.totalCost), 0) FROM CTimeLog tl " +
           "WHERE tl.activity.project.id = :projectId " +
           "AND tl.isBillable = true")
    BigDecimal calculateTotalCostForProject(@Param("projectId") Long projectId);
    
    /**
     * Find unsubmitted time logs for user.
     */
    @Query("SELECT tl FROM CTimeLog tl " +
           "LEFT JOIN FETCH tl.activity a " +
           "LEFT JOIN FETCH tl.timeCategory tc " +
           "WHERE tl.user.id = :userId " +
           "AND tl.isSubmitted = false " +
           "ORDER BY tl.logDate DESC")
    List<CTimeLog> findUnsubmittedTimeLogsForUser(@Param("userId") Long userId);
    
    /**
     * Find time logs needing approval.
     */
    @Query("SELECT tl FROM CTimeLog tl " +
           "LEFT JOIN FETCH tl.activity a " +
           "LEFT JOIN FETCH tl.user u " +
           "LEFT JOIN FETCH tl.timeCategory tc " +
           "WHERE tl.isSubmitted = true " +
           "AND tl.isApproved = false " +
           "ORDER BY tl.logDate DESC")
    List<CTimeLog> findTimeLogsNeedingApproval();
}
```

#### CTimeTrackingSessionRepository
```java
@Repository
public interface CTimeTrackingSessionRepository extends CEntityRepository<CTimeTrackingSession> {
    
    /**
     * Find active session for user.
     */
    @Query("SELECT ts FROM CTimeTrackingSession ts " +
           "LEFT JOIN FETCH ts.activity a " +
           "LEFT JOIN FETCH ts.timeCategory tc " +
           "WHERE ts.user.id = :userId")
    Optional<CTimeTrackingSession> findActiveSessionForUser(@Param("userId") Long userId);
    
    /**
     * Find all active sessions.
     */
    @Query("SELECT ts FROM CTimeTrackingSession ts " +
           "LEFT JOIN FETCH ts.activity a " +
           "LEFT JOIN FETCH ts.user u " +
           "LEFT JOIN FETCH ts.timeCategory tc " +
           "ORDER BY ts.startTime DESC")
    List<CTimeTrackingSession> findAllActiveSessionsWithFullData();
    
    /**
     * Find sessions older than specified hours (for cleanup).
     */
    @Query("SELECT ts FROM CTimeTrackingSession ts " +
           "WHERE ts.startTime < :cutoffTime")
    List<CTimeTrackingSession> findSessionsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
}
```

### Step 3: Service Layer

#### CTimeTrackingService
```java
@Service
public class CTimeTrackingService extends CEntityService<CTimeLog> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CTimeTrackingService.class);
    
    private final CTimeLogRepository timeLogRepository;
    private final CTimeTrackingSessionRepository sessionRepository;
    private final CTimeCategoryService timeCategoryService;
    private final CActivityService activityService;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    protected CEntityRepository<CTimeLog> getRepository() {
        return timeLogRepository;
    }
    
    /**
     * Start time tracking for an activity.
     */
    @Transactional
    public CTimeTrackingSession startTimeTracking(final Long activityId, final Long userId, 
                                                  final Long categoryId, final String description) {
        LOGGER.info("startTimeTracking called with activityId: {}, userId: {}, categoryId: {}", 
                   activityId, userId, categoryId);
        
        if (activityId == null || userId == null || categoryId == null) {
            throw new ServiceException("Activity ID, User ID, and Category ID cannot be null");
        }
        
        // Check if user already has an active session
        final Optional<CTimeTrackingSession> existingSession = 
            sessionRepository.findActiveSessionForUser(userId);
        
        if (existingSession.isPresent()) {
            throw new ServiceException("User already has an active time tracking session. Please stop the current session first.");
        }
        
        // Get entities with eager loading
        final CActivity activity = activityService.get(activityId)
            .orElseThrow(() -> new ServiceException("Activity not found with ID: " + activityId));
        
        final CUser user = userService.get(userId)
            .orElseThrow(() -> new ServiceException("User not found with ID: " + userId));
        
        final CTimeCategory category = timeCategoryService.get(categoryId)
            .orElseThrow(() -> new ServiceException("Time category not found with ID: " + categoryId));
        
        // Create and save session
        final CTimeTrackingSession session = new CTimeTrackingSession(activity, user, category);
        session.setDescription(description);
        
        final CTimeTrackingSession savedSession = sessionRepository.save(session);
        
        // Publish event
        eventPublisher.publishEvent(new TimeTrackingStartedEvent(savedSession));
        
        return savedSession;
    }
    
    /**
     * Stop time tracking and create time log.
     */
    @Transactional
    public CTimeLog stopTimeTracking(final Long userId) {
        LOGGER.info("stopTimeTracking called for userId: {}", userId);
        
        if (userId == null) {
            throw new ServiceException("User ID cannot be null");
        }
        
        // Find active session
        final CTimeTrackingSession session = sessionRepository.findActiveSessionForUser(userId)
            .orElseThrow(() -> new ServiceException("No active time tracking session found for user"));
        
        // Create time log from session
        final CTimeLog timeLog = session.toTimeLog();
        final CTimeLog savedTimeLog = save(timeLog);
        
        // Delete the session
        sessionRepository.delete(session);
        
        // Publish events
        eventPublisher.publishEvent(new TimeTrackingStoppedEvent(savedTimeLog));
        eventPublisher.publishEvent(new TimeLogCreatedEvent(savedTimeLog));
        
        return savedTimeLog;
    }
    
    /**
     * Pause active time tracking.
     */
    @Transactional
    public CTimeTrackingSession pauseTimeTracking(final Long userId) {
        LOGGER.info("pauseTimeTracking called for userId: {}", userId);
        
        final CTimeTrackingSession session = sessionRepository.findActiveSessionForUser(userId)
            .orElseThrow(() -> new ServiceException("No active time tracking session found for user"));
        
        session.pause();
        final CTimeTrackingSession savedSession = sessionRepository.save(session);
        
        eventPublisher.publishEvent(new TimeTrackingPausedEvent(savedSession));
        
        return savedSession;
    }
    
    /**
     * Resume paused time tracking.
     */
    @Transactional
    public CTimeTrackingSession resumeTimeTracking(final Long userId) {
        LOGGER.info("resumeTimeTracking called for userId: {}", userId);
        
        final CTimeTrackingSession session = sessionRepository.findActiveSessionForUser(userId)
            .orElseThrow(() -> new ServiceException("No active time tracking session found for user"));
        
        if (!session.getIsPaused()) {
            throw new ServiceException("Time tracking session is not paused");
        }
        
        session.resume();
        final CTimeTrackingSession savedSession = sessionRepository.save(session);
        
        eventPublisher.publishEvent(new TimeTrackingResumedEvent(savedSession));
        
        return savedSession;
    }
    
    /**
     * Get active session for user.
     */
    @Transactional(readOnly = true)
    public Optional<CTimeTrackingSession> getActiveSessionForUser(final Long userId) {
        LOGGER.info("getActiveSessionForUser called with userId: {}", userId);
        
        if (userId == null) {
            return Optional.empty();
        }
        
        return sessionRepository.findActiveSessionForUser(userId);
    }
    
    /**
     * Create manual time log entry.
     */
    @Transactional
    public CTimeLog createManualTimeLog(final Long activityId, final Long userId, 
                                       final LocalDate logDate, final BigDecimal durationHours,
                                       final String description, final Long categoryId) {
        LOGGER.info("createManualTimeLog called with activityId: {}, userId: {}, durationHours: {}", 
                   activityId, userId, durationHours);
        
        if (activityId == null || userId == null || logDate == null || durationHours == null) {
            throw new ServiceException("Activity ID, User ID, log date, and duration cannot be null");
        }
        
        if (durationHours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("Duration must be greater than zero");
        }
        
        if (logDate.isAfter(LocalDate.now())) {
            throw new ServiceException("Log date cannot be in the future");
        }
        
        // Get entities with eager loading
        final CActivity activity = activityService.get(activityId)
            .orElseThrow(() -> new ServiceException("Activity not found with ID: " + activityId));
        
        final CUser user = userService.get(userId)
            .orElseThrow(() -> new ServiceException("User not found with ID: " + userId));
        
        final CTimeCategory category = categoryId != null 
            ? timeCategoryService.get(categoryId).orElse(null)
            : timeCategoryService.findDefaultCategory();
        
        // Create time log
        final CTimeLog timeLog = new CTimeLog(activity, user, logDate);
        timeLog.setDurationHours(durationHours);
        timeLog.setDescription(description);
        timeLog.setTimeCategory(category);
        
        // Apply category defaults if category is specified
        if (category != null) {
            timeLog.setIsBillable(category.getIsBillableDefault());
            timeLog.setHourlyRate(category.getDefaultHourlyRate());
        }
        
        final CTimeLog savedTimeLog = save(timeLog);
        
        // Publish event
        eventPublisher.publishEvent(new TimeLogCreatedEvent(savedTimeLog));
        
        return savedTimeLog;
    }
    
    /**
     * Get time logs for activity with full data.
     */
    @Transactional(readOnly = true)
    public List<CTimeLog> getTimeLogsForActivity(final Long activityId) {
        LOGGER.info("getTimeLogsForActivity called with activityId: {}", activityId);
        
        if (activityId == null) {
            return Collections.emptyList();
        }
        
        return timeLogRepository.findByActivityIdWithFullData(activityId);
    }
    
    /**
     * Get time logs for user in date range.
     */
    @Transactional(readOnly = true)
    public List<CTimeLog> getTimeLogsForUserInPeriod(final Long userId, 
                                                     final LocalDate startDate, 
                                                     final LocalDate endDate) {
        LOGGER.info("getTimeLogsForUserInPeriod called with userId: {}, startDate: {}, endDate: {}", 
                   userId, startDate, endDate);
        
        if (userId == null || startDate == null || endDate == null) {
            return Collections.emptyList();
        }
        
        if (endDate.isBefore(startDate)) {
            throw new ServiceException("End date cannot be before start date");
        }
        
        return timeLogRepository.findByUserAndDateRangeWithFullData(userId, startDate, endDate);
    }
    
    /**
     * Calculate total hours for activity.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalHoursForActivity(final Long activityId) {
        LOGGER.info("calculateTotalHoursForActivity called with activityId: {}", activityId);
        
        if (activityId == null) {
            return BigDecimal.ZERO;
        }
        
        return timeLogRepository.calculateTotalHoursForActivity(activityId);
    }
    
    /**
     * Update activity actual hours when time log is saved.
     */
    @Override
    @Transactional
    public CTimeLog save(final CTimeLog timeLog) {
        LOGGER.info("save called with timeLog: {}", timeLog);
        
        if (timeLog == null) {
            throw new ServiceException("Time log cannot be null");
        }
        
        // Validate time log
        timeLog.validateTimeLog();
        
        final CTimeLog savedTimeLog = super.save(timeLog);
        
        // Update activity actual hours
        if (savedTimeLog.getActivity() != null) {
            updateActivityActualHours(savedTimeLog.getActivity().getId());
        }
        
        return savedTimeLog;
    }
    
    /**
     * Update activity actual hours based on time logs.
     */
    private void updateActivityActualHours(final Long activityId) {
        final BigDecimal totalHours = calculateTotalHoursForActivity(activityId);
        
        final Optional<CActivity> activityOpt = activityService.get(activityId);
        if (activityOpt.isPresent()) {
            final CActivity activity = activityOpt.get();
            activity.setActualHours(totalHours);
            activityService.save(activity);
        }
    }
    
    /**
     * Clean up old tracking sessions (for scheduled cleanup).
     */
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void cleanupOldSessions() {
        LOGGER.info("cleanupOldSessions called");
        
        final LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        final List<CTimeTrackingSession> oldSessions = 
            sessionRepository.findSessionsOlderThan(cutoffTime);
        
        for (final CTimeTrackingSession session : oldSessions) {
            try {
                // Convert to time log before deleting
                final CTimeLog timeLog = session.toTimeLog();
                save(timeLog);
                sessionRepository.delete(session);
                
                LOGGER.info("Converted abandoned session to time log: {}", session);
            } catch (final Exception e) {
                LOGGER.error("Error cleaning up session: {}", session, e);
            }
        }
    }
}
```

### Step 4: UI Components

#### CTimeTrackingPanel (Main Widget)
```java
/**
 * Time tracking panel component for activity time management.
 * Displays current time tracking status and provides controls for time management.
 */
public class CTimeTrackingPanel extends VerticalLayout {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CTimeTrackingPanel.class);
    
    private final CTimeTrackingService timeTrackingService;
    private final CTimeCategoryService timeCategoryService;
    private final CActivity activity;
    private final CUser currentUser;
    
    private CTimeTrackingSession activeSession;
    private Timer updateTimer;
    
    // UI Components
    private HorizontalLayout progressLayout;
    private ProgressBar progressBar;
    private Span progressText;
    private Span estimatedHoursSpan;
    private Span loggedHoursSpan;
    private Span remainingHoursSpan;
    
    private HorizontalLayout timerLayout;
    private CButton startButton;
    private CButton pauseButton;
    private CButton stopButton;
    private Span timerDisplay;
    
    private TextArea descriptionField;
    private ComboBox<CTimeCategory> categoryComboBox;
    private CButton saveLogButton;
    
    private Grid<CTimeLog> timeLogGrid;
    
    public CTimeTrackingPanel(final CActivity activity, final CUser currentUser,
                             final CTimeTrackingService timeTrackingService,
                             final CTimeCategoryService timeCategoryService) {
        LOGGER.info("CTimeTrackingPanel constructor called with activity: {}, user: {}", 
                   activity, currentUser);
        
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be null for time tracking panel");
        }
        if (currentUser == null) {
            throw new IllegalArgumentException("Current user cannot be null for time tracking panel");
        }
        
        this.activity = activity;
        this.currentUser = currentUser;
        this.timeTrackingService = timeTrackingService;
        this.timeCategoryService = timeCategoryService;
        
        addClassName("c-time-tracking-panel");
        setSpacing(true);
        setPadding(true);
        setWidthFull();
        
        initializeComponents();
        setupLayout();
        loadActiveSession();
        loadTimeLogHistory();
    }
    
    private void initializeComponents() {
        // Progress section
        progressLayout = new HorizontalLayout();
        progressLayout.setWidthFull();
        progressLayout.setAlignItems(Alignment.CENTER);
        
        progressBar = new ProgressBar();
        progressBar.setWidth("200px");
        progressText = new Span();
        progressText.addClassName("progress-text");
        
        estimatedHoursSpan = new Span();
        loggedHoursSpan = new Span();
        remainingHoursSpan = new Span();
        
        // Timer section
        timerLayout = new HorizontalLayout();
        timerLayout.setAlignItems(Alignment.CENTER);
        timerLayout.setSpacing(true);
        
        startButton = CButton.createSuccess("Start", VaadinIcon.PLAY);
        pauseButton = CButton.createSecondary("Pause", VaadinIcon.PAUSE);
        stopButton = CButton.createError("Stop", VaadinIcon.STOP);
        timerDisplay = new Span("00:00:00");
        timerDisplay.addClassName("timer-display");
        
        // Configure button actions
        startButton.addClickListener(event -> startTimeTracking());
        pauseButton.addClickListener(event -> pauseTimeTracking());
        stopButton.addClickListener(event -> stopTimeTracking());
        
        // Description and category
        descriptionField = new TextArea("Description");
        descriptionField.setPlaceholder("What are you working on?");
        descriptionField.setWidthFull();
        descriptionField.setMaxLength(500);
        
        categoryComboBox = new ComboBox<>("Category");
        categoryComboBox.setItemLabelGenerator(CTimeCategory::getName);
        categoryComboBox.setWidthFull();
        
        saveLogButton = CButton.createPrimary("Save Log", VaadinIcon.FLOPPY_O);
        saveLogButton.addClickListener(event -> openManualLogDialog());
        
        // Time log history grid
        timeLogGrid = new Grid<>(CTimeLog.class, false);
        setupTimeLogGrid();
    }
    
    private void setupLayout() {
        // Title
        final H3 title = new H3("Time Tracking - " + activity.getName());
        title.addClassName("panel-title");
        
        // Progress section
        updateProgressDisplay();
        
        final HorizontalLayout progressDetailsLayout = new HorizontalLayout(
            estimatedHoursSpan, loggedHoursSpan, remainingHoursSpan
        );
        progressDetailsLayout.setWidthFull();
        progressDetailsLayout.setJustifyContentMode(JustifyContentMode.AROUND);
        
        progressLayout.add(progressBar, progressText);
        
        // Timer section
        timerLayout.add(startButton, pauseButton, stopButton, timerDisplay);
        
        final Div timerSection = new Div();
        timerSection.addClassName("timer-section");
        timerSection.add(new H4("Today's Time Log"), timerLayout);
        
        // Description and category section
        final HorizontalLayout inputLayout = new HorizontalLayout(descriptionField, categoryComboBox);
        inputLayout.setWidthFull();
        inputLayout.setFlexGrow(2, descriptionField);
        inputLayout.setFlexGrow(1, categoryComboBox);
        
        final HorizontalLayout actionLayout = new HorizontalLayout(saveLogButton);
        actionLayout.setJustifyContentMode(JustifyContentMode.END);
        
        // Time log history section
        final H4 historyTitle = new H4("Time Log History");
        
        // Add all sections
        add(title, progressLayout, progressDetailsLayout, timerSection, inputLayout, actionLayout, historyTitle, timeLogGrid);
        
        // Load category data
        loadCategoryData();
        updateTimerControls();
    }
    
    private void updateProgressDisplay() {
        final BigDecimal estimatedHours = activity.getEstimatedHours() != null ? activity.getEstimatedHours() : BigDecimal.ZERO;
        final BigDecimal actualHours = activity.getActualHours() != null ? activity.getActualHours() : BigDecimal.ZERO;
        
        // Calculate progress percentage
        double progressValue = 0.0;
        if (estimatedHours.compareTo(BigDecimal.ZERO) > 0) {
            progressValue = actualHours.doubleValue() / estimatedHours.doubleValue();
            progressValue = Math.min(progressValue, 1.0); // Cap at 100%
        }
        
        progressBar.setValue(progressValue);
        progressText.setText(String.format("%.0f%% Complete", progressValue * 100));
        
        // Update hour displays
        estimatedHoursSpan.setText("⏰ Estimated: " + formatHours(estimatedHours));
        loggedHoursSpan.setText("📝 Logged: " + formatHours(actualHours));
        
        final BigDecimal remainingHours = estimatedHours.subtract(actualHours);
        remainingHoursSpan.setText("⏳ Remaining: " + formatHours(remainingHours));
        
        // Apply styling based on progress
        if (progressValue >= 1.0) {
            progressBar.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
        } else if (progressValue >= 0.8) {
            progressBar.addThemeVariants(ProgressBarVariant.LUMO_CONTRAST);
        }
    }
    
    private String formatHours(final BigDecimal hours) {
        if (hours == null || hours.compareTo(BigDecimal.ZERO) == 0) {
            return "0h";
        }
        return hours.setScale(1, RoundingMode.HALF_UP) + "h";
    }
    
    private void loadActiveSession() {
        try {
            final Optional<CTimeTrackingSession> sessionOpt = 
                timeTrackingService.getActiveSessionForUser(currentUser.getId());
            
            if (sessionOpt.isPresent()) {
                activeSession = sessionOpt.get();
                
                // Check if session is for current activity
                if (Objects.equals(activeSession.getActivity().getId(), activity.getId())) {
                    startUpdateTimer();
                    updateDescriptionFromSession();
                } else {
                    // User has a session for different activity
                    showActiveSessionWarning();
                }
            }
            
            updateTimerControls();
            
        } catch (final Exception e) {
            LOGGER.error("Error loading active session", e);
            showErrorNotification("Failed to load active session: " + e.getMessage());
        }
    }
    
    private void startTimeTracking() {
        LOGGER.info("startTimeTracking called");
        
        try {
            // Validate inputs
            final String description = descriptionField.getValue();
            final CTimeCategory category = categoryComboBox.getValue();
            
            if (category == null) {
                showValidationError("Please select a time category");
                return;
            }
            
            // Start tracking
            activeSession = timeTrackingService.startTimeTracking(
                activity.getId(), currentUser.getId(), category.getId(), description
            );
            
            startUpdateTimer();
            updateTimerControls();
            
            showSuccessNotification("Time tracking started");
            
        } catch (final Exception e) {
            LOGGER.error("Error starting time tracking", e);
            showErrorNotification("Failed to start time tracking: " + e.getMessage());
        }
    }
    
    private void pauseTimeTracking() {
        LOGGER.info("pauseTimeTracking called");
        
        try {
            if (activeSession != null) {
                activeSession = timeTrackingService.pauseTimeTracking(currentUser.getId());
                updateTimerControls();
                showSuccessNotification("Time tracking paused");
            }
        } catch (final Exception e) {
            LOGGER.error("Error pausing time tracking", e);
            showErrorNotification("Failed to pause time tracking: " + e.getMessage());
        }
    }
    
    private void resumeTimeTracking() {
        LOGGER.info("resumeTimeTracking called");
        
        try {
            if (activeSession != null) {
                activeSession = timeTrackingService.resumeTimeTracking(currentUser.getId());
                updateTimerControls();
                showSuccessNotification("Time tracking resumed");
            }
        } catch (final Exception e) {
            LOGGER.error("Error resuming time tracking", e);
            showErrorNotification("Failed to resume time tracking: " + e.getMessage());
        }
    }
    
    private void stopTimeTracking() {
        LOGGER.info("stopTimeTracking called");
        
        try {
            if (activeSession != null) {
                // Update session description before stopping
                activeSession.setDescription(descriptionField.getValue());
                
                final CTimeLog timeLog = timeTrackingService.stopTimeTracking(currentUser.getId());
                
                activeSession = null;
                stopUpdateTimer();
                updateTimerControls();
                
                // Refresh displays
                updateProgressDisplay();
                loadTimeLogHistory();
                
                showSuccessNotification("Time tracking stopped. " + 
                                      formatHours(timeLog.getDurationHours()) + " logged.");
                
                // Clear description
                descriptionField.clear();
            }
        } catch (final Exception e) {
            LOGGER.error("Error stopping time tracking", e);
            showErrorNotification("Failed to stop time tracking: " + e.getMessage());
        }
    }
    
    private void updateTimerControls() {
        final boolean hasActiveSession = activeSession != null && 
                                       Objects.equals(activeSession.getActivity().getId(), activity.getId());
        final boolean isPaused = hasActiveSession && activeSession.getIsPaused();
        
        startButton.setEnabled(!hasActiveSession);
        pauseButton.setEnabled(hasActiveSession && !isPaused);
        stopButton.setEnabled(hasActiveSession);
        
        // Update pause button to resume if paused
        if (isPaused) {
            pauseButton.setText("Resume");
            pauseButton.setIcon(new Icon(VaadinIcon.PLAY));
            pauseButton.removeClickListeners();
            pauseButton.addClickListener(event -> resumeTimeTracking());
        } else {
            pauseButton.setText("Pause");
            pauseButton.setIcon(new Icon(VaadinIcon.PAUSE));
            pauseButton.removeClickListeners();
            pauseButton.addClickListener(event -> pauseTimeTracking());
        }
    }
    
    private void startUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
        
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getUI().isPresent()) {
                    getUI().get().access(() -> updateTimerDisplay());
                }
            }
        }, 0, 1000); // Update every second
    }
    
    private void stopUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
        timerDisplay.setText("00:00:00");
    }
    
    private void updateTimerDisplay() {
        if (activeSession != null && Objects.equals(activeSession.getActivity().getId(), activity.getId())) {
            final Duration duration = activeSession.getCurrentDuration();
            timerDisplay.setText(formatDuration(duration));
            
            if (activeSession.getIsPaused()) {
                timerDisplay.addClassName("timer-paused");
            } else {
                timerDisplay.removeClassName("timer-paused");
            }
        }
    }
    
    private String formatDuration(final Duration duration) {
        final long hours = duration.toHours();
        final long minutes = duration.toMinutesPart();
        final long seconds = duration.toSecondsPart();
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    private void setupTimeLogGrid() {
        timeLogGrid.addColumn(timeLog -> timeLog.getLogDate().toString())
            .setHeader("Date")
            .setWidth("120px")
            .setSortable(true);
        
        timeLogGrid.addColumn(timeLog -> formatHours(timeLog.getDurationHours()))
            .setHeader("Duration")
            .setWidth("100px")
            .setSortable(true);
        
        timeLogGrid.addColumn(timeLog -> timeLog.getTimeCategory() != null ? 
                             timeLog.getTimeCategory().getName() : "")
            .setHeader("Category")
            .setWidth("150px");
        
        timeLogGrid.addColumn(timeLog -> timeLog.getDescription() != null ? 
                             timeLog.getDescription() : "")
            .setHeader("Description")
            .setFlexGrow(1);
        
        timeLogGrid.setHeightByRows(true);
        timeLogGrid.setMaxHeight("300px");
    }
    
    private void loadTimeLogHistory() {
        try {
            final List<CTimeLog> timeLogs = timeTrackingService.getTimeLogsForActivity(activity.getId());
            timeLogGrid.setItems(timeLogs);
        } catch (final Exception e) {
            LOGGER.error("Error loading time log history", e);
            showErrorNotification("Failed to load time log history");
        }
    }
    
    private void loadCategoryData() {
        try {
            final List<CTimeCategory> categories = timeCategoryService.findAllActive();
            categoryComboBox.setItems(categories);
            
            // Set default category if available
            final CTimeCategory defaultCategory = timeCategoryService.findDefaultCategory();
            if (defaultCategory != null) {
                categoryComboBox.setValue(defaultCategory);
            }
            
        } catch (final Exception e) {
            LOGGER.error("Error loading category data", e);
            showErrorNotification("Failed to load time categories");
        }
    }
    
    private void updateDescriptionFromSession() {
        if (activeSession != null && activeSession.getDescription() != null) {
            descriptionField.setValue(activeSession.getDescription());
        }
    }
    
    private void openManualLogDialog() {
        final CTimeLogDialog dialog = new CTimeLogDialog(activity, currentUser, 
                                                        timeTrackingService, timeCategoryService);
        dialog.addSaveListener(event -> {
            updateProgressDisplay();
            loadTimeLogHistory();
        });
        dialog.open();
    }
    
    private void showActiveSessionWarning() {
        final CWarningDialog warning = new CWarningDialog(
            "Active Session Detected",
            String.format("You have an active time tracking session for activity '%s'. " +
                         "Please stop that session before starting a new one.",
                         activeSession.getActivity().getName())
        );
        warning.open();
    }
    
    private void showSuccessNotification(final String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    private void showErrorNotification(final String message) {
        Notification.show(message, 5000, Notification.Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
    
    private void showValidationError(final String message) {
        Notification.show(message, 4000, Notification.Position.TOP_CENTER)
            .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
    }
    
    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        stopUpdateTimer();
    }
}
```

---

This implementation guide provides a comprehensive foundation for building the Advanced Time Tracking system following the Derbent project's coding standards and architectural patterns. The implementation includes real-time tracking, manual time entry, timesheet management, and comprehensive time analytics capabilities.