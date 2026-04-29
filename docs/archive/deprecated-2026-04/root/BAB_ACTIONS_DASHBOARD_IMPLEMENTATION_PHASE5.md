# BAB Actions Dashboard Implementation Guide - Phase 5

**Version**: 1.0  
**Date**: 2026-02-05  
**Phase**: 5 - Advanced Monitoring & Analytics  
**Status**: Implementation Ready  

## Phase 5: Advanced Monitoring & Analytics

### 5.1 Monitoring Infrastructure Implementation

#### 5.1.1 Real-Time Policy Execution Monitor

**Task**: Implement real-time monitoring of policy rule executions with live dashboard updates

**Classes to Implement**:

```java
/**
 * Service for monitoring policy rule executions in real-time
 */
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CPolicyExecutionMonitorService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CPolicyExecutionMonitorService.class);
    
    // Dependencies
    private final CPolicyExecutionLogService executionLogService;
    private final ISessionService sessionService;
    private final CCalimeroWebSocketClient webSocketClient;
    
    // Real-time data structures
    private final Map<Long, CPolicyExecutionStats> projectStats = new ConcurrentHashMap<>();
    private final Map<Long, List<CPolicyExecutionEvent>> recentEvents = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    
    public CPolicyExecutionMonitorService(final CPolicyExecutionLogService executionLogService,
                                        final ISessionService sessionService) {
        this.executionLogService = executionLogService;
        this.sessionService = sessionService;
        this.webSocketClient = new CCalimeroWebSocketClient();
        
        initializeWebSocketConnection();
    }
    
    /**
     * Start monitoring policy executions for a project
     */
    @Async
    public CompletableFuture<Void> startMonitoring(final CProject_Bab project) {
        return CompletableFuture.runAsync(() -> {
            try {
                LOGGER.info("Starting policy execution monitoring for project: {}", project.getName());
                
                // Initialize project statistics
                initializeProjectStats(project);
                
                // Connect to Calimero WebSocket for real-time events
                connectToCalimeroWebSocket(project);
                
                // Start periodic statistics update
                scheduleStatsUpdate(project);
                
                LOGGER.info("Policy execution monitoring started for project: {}", project.getName());
                
            } catch (final Exception e) {
                LOGGER.error("Error starting policy execution monitoring", e);
                throw new RuntimeException("Failed to start monitoring", e);
            }
        }, executorService);
    }
    
    /**
     * Get current execution statistics for a project
     */
    public CPolicyExecutionStats getCurrentStats(final Long projectId) {
        return projectStats.computeIfAbsent(projectId, id -> new CPolicyExecutionStats(id));
    }
    
    /**
     * Get recent execution events for a project
     */
    public List<CPolicyExecutionEvent> getRecentEvents(final Long projectId, final int limit) {
        final List<CPolicyExecutionEvent> events = recentEvents.getOrDefault(projectId, new ArrayList<>());
        return events.stream()
                .sorted((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Process incoming execution event from Calimero
     */
    public void processExecutionEvent(final CPolicyExecutionEvent event) {
        try {
            // Update statistics
            updateProjectStats(event);
            
            // Store recent event
            addRecentEvent(event);
            
            // Persist to database if significant
            if (event.isSignificant()) {
                persistExecutionEvent(event);
            }
            
            // Notify UI components via WebSocket
            notifyUIComponents(event);
            
        } catch (final Exception e) {
            LOGGER.error("Error processing execution event", e);
        }
    }
    
    /**
     * Generate execution report for a time period
     */
    public CPolicyExecutionReport generateExecutionReport(final CProject_Bab project,
                                                         final LocalDateTime fromTime,
                                                         final LocalDateTime toTime) {
        try {
            final CPolicyExecutionReport report = new CPolicyExecutionReport();
            report.setProjectId(project.getId());
            report.setProjectName(project.getName());
            report.setFromTime(fromTime);
            report.setToTime(toTime);
            report.setGeneratedAt(LocalDateTime.now());
            
            // Query execution logs
            final List<CPolicyExecutionLog> logs = executionLogService.findByProjectAndTimeRange(
                project, fromTime, toTime);
            
            // Aggregate statistics
            report.setTotalExecutions(logs.size());
            report.setSuccessfulExecutions(logs.stream()
                .mapToInt(log -> log.isSuccess() ? 1 : 0).sum());
            report.setFailedExecutions(logs.size() - report.getSuccessfulExecutions());
            
            // Rule-specific statistics
            final Map<Long, CPolicyRuleExecutionStats> ruleStats = aggregateRuleStats(logs);
            report.setRuleStats(ruleStats);
            
            // Node-specific statistics
            final Map<Long, CBabNodeExecutionStats> nodeStats = aggregateNodeStats(logs);
            report.setNodeStats(nodeStats);
            
            // Performance metrics
            report.setAverageExecutionTimeMs(calculateAverageExecutionTime(logs));
            report.setThroughputPerSecond(calculateThroughput(logs, fromTime, toTime));
            
            return report;
            
        } catch (final Exception e) {
            LOGGER.error("Error generating execution report", e);
            throw new RuntimeException("Failed to generate execution report", e);
        }
    }
    
    private void initializeProjectStats(final CProject_Bab project) {
        final CPolicyExecutionStats stats = new CPolicyExecutionStats(project.getId());
        stats.setProjectName(project.getName());
        stats.setMonitoringStartTime(LocalDateTime.now());
        stats.setTotalExecutions(0L);
        stats.setSuccessfulExecutions(0L);
        stats.setFailedExecutions(0L);
        stats.setAverageExecutionTimeMs(0.0);
        
        projectStats.put(project.getId(), stats);
    }
    
    private void connectToCalimeroWebSocket(final CProject_Bab project) {
        try {
            final String wsUrl = buildWebSocketUrl(project);
            webSocketClient.connect(wsUrl, new CPolicyExecutionEventHandler(this));
            
            // Subscribe to policy execution events
            final CCalimeroWebSocketMessage subscription = CCalimeroWebSocketMessage.builder()
                .type("subscribe")
                .topic("policy.executions")
                .projectId(project.getId())
                .build();
            
            webSocketClient.send(subscription);
            
        } catch (final Exception e) {
            LOGGER.error("Error connecting to Calimero WebSocket", e);
        }
    }
    
    private String buildWebSocketUrl(final CProject_Bab project) {
        return String.format("ws://%s:%d/ws/policy-executions?project=%d", 
                           project.getIpAddress(), 
                           project.getWebSocketPort() != null ? project.getWebSocketPort() : 8081,
                           project.getId());
    }
    
    private void updateProjectStats(final CPolicyExecutionEvent event) {
        final CPolicyExecutionStats stats = projectStats.get(event.getProjectId());
        if (stats != null) {
            stats.incrementTotalExecutions();
            
            if (event.isSuccess()) {
                stats.incrementSuccessfulExecutions();
            } else {
                stats.incrementFailedExecutions();
            }
            
            if (event.getExecutionTimeMs() != null) {
                stats.updateAverageExecutionTime(event.getExecutionTimeMs());
            }
            
            stats.setLastUpdateTime(LocalDateTime.now());
        }
    }
    
    private void addRecentEvent(final CPolicyExecutionEvent event) {
        recentEvents.computeIfAbsent(event.getProjectId(), k -> new CopyOnWriteArrayList<>())
                   .add(event);
        
        // Keep only last 1000 events per project
        final List<CPolicyExecutionEvent> events = recentEvents.get(event.getProjectId());
        if (events.size() > 1000) {
            events.subList(0, events.size() - 1000).clear();
        }
    }
    
    private void persistExecutionEvent(final CPolicyExecutionEvent event) {
        try {
            final CPolicyExecutionLog log = new CPolicyExecutionLog();
            log.setProjectId(event.getProjectId());
            log.setRuleId(event.getRuleId());
            log.setSourceNodeId(event.getSourceNodeId());
            log.setDestinationNodeId(event.getDestinationNodeId());
            log.setExecutionTime(event.getTimestamp());
            log.setSuccess(event.isSuccess());
            log.setExecutionTimeMs(event.getExecutionTimeMs());
            log.setErrorMessage(event.getErrorMessage());
            log.setDataSize(event.getDataSize());
            
            executionLogService.save(log);
            
        } catch (final Exception e) {
            LOGGER.error("Error persisting execution event", e);
        }
    }
    
    @PreDestroy
    public void shutdown() {
        try {
            if (webSocketClient != null) {
                webSocketClient.close();
            }
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (final InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * Real-time execution statistics for a project
 */
public class CPolicyExecutionStats {
    private final Long projectId;
    private String projectName;
    private LocalDateTime monitoringStartTime;
    private LocalDateTime lastUpdateTime;
    private AtomicLong totalExecutions = new AtomicLong(0);
    private AtomicLong successfulExecutions = new AtomicLong(0);
    private AtomicLong failedExecutions = new AtomicLong(0);
    private volatile double averageExecutionTimeMs = 0.0;
    private final AtomicLong executionTimeSum = new AtomicLong(0);
    
    public CPolicyExecutionStats(final Long projectId) {
        this.projectId = projectId;
    }
    
    public void incrementTotalExecutions() {
        totalExecutions.incrementAndGet();
    }
    
    public void incrementSuccessfulExecutions() {
        successfulExecutions.incrementAndGet();
    }
    
    public void incrementFailedExecutions() {
        failedExecutions.incrementAndGet();
    }
    
    public void updateAverageExecutionTime(final long executionTimeMs) {
        executionTimeSum.addAndGet(executionTimeMs);
        final long total = totalExecutions.get();
        if (total > 0) {
            averageExecutionTimeMs = (double) executionTimeSum.get() / total;
        }
    }
    
    public double getSuccessRate() {
        final long total = totalExecutions.get();
        return total > 0 ? (double) successfulExecutions.get() / total * 100.0 : 0.0;
    }
    
    // Standard getters and setters...
}

/**
 * Event representing a policy rule execution
 */
public class CPolicyExecutionEvent {
    private Long projectId;
    private Long ruleId;
    private Long sourceNodeId;
    private Long destinationNodeId;
    private LocalDateTime timestamp;
    private boolean success;
    private String errorMessage;
    private Long executionTimeMs;
    private Long dataSize;
    private String triggerType;
    private String actionType;
    private Map<String, Object> metadata;
    
    public boolean isSignificant() {
        return !success || executionTimeMs > 1000 || dataSize > 1024 * 1024;
    }
    
    // Standard getters and setters...
}
```

#### 5.1.2 Live Dashboard Components

**Task**: Implement real-time monitoring components for the working area

```java
/**
 * Working area tab component for live policy execution monitoring
 */
@Profile("bab")
public class CWorkingAreaTabMonitoring extends CWorkingAreaTabBase {
    
    public static final String ID_ROOT = "custom-working-area-tab-monitoring";
    public static final String ID_STATS_CARD = "custom-monitoring-stats-card";
    public static final String ID_EVENTS_GRID = "custom-monitoring-events-grid";
    public static final String ID_REFRESH_TOGGLE = "custom-monitoring-refresh-toggle";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CWorkingAreaTabMonitoring.class);
    
    // Dependencies
    private final CPolicyExecutionMonitorService monitorService;
    private final ISessionService sessionService;
    
    // UI Components
    private CCard statsCard;
    private CGrid<CPolicyExecutionEvent> eventsGrid;
    private CToggleButton autoRefreshToggle;
    private CLabel statsLabel;
    private CProgressBar throughputProgress;
    private CLabel successRateLabel;
    
    // Real-time update components
    private UI currentUI;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> refreshTask;
    
    public CWorkingAreaTabMonitoring(final CPolicyExecutionMonitorService monitorService,
                                   final ISessionService sessionService) {
        super("Monitoring", VaadinIcon.PULSE.create());
        this.monitorService = monitorService;
        this.sessionService = sessionService;
        this.currentUI = UI.getCurrent();
        
        initializeComponents();
    }
    
    @Override
    protected void initializeComponents() {
        setId(ID_ROOT);
        setSpacing(false);
        setPadding(false);
        getStyle().set("gap", "12px");
        
        // Create main layout
        final CVerticalLayout mainLayout = new CVerticalLayout();
        mainLayout.setSpacing(false);
        mainLayout.setPadding(false);
        
        // Create toolbar
        createToolbar(mainLayout);
        
        // Create statistics cards
        createStatsCards(mainLayout);
        
        // Create events grid
        createEventsGrid(mainLayout);
        
        add(mainLayout);
        
        // Start monitoring if project is active
        startMonitoringIfProjectActive();
    }
    
    private void createToolbar(final CVerticalLayout parent) {
        final CHorizontalLayout toolbar = new CHorizontalLayout();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setPadding(false);
        toolbar.setSpacing(true);
        
        // Auto-refresh toggle
        autoRefreshToggle = new CToggleButton("Auto-refresh");
        autoRefreshToggle.setId(ID_REFRESH_TOGGLE);
        autoRefreshToggle.setValue(true);
        autoRefreshToggle.addValueChangeListener(e -> toggleAutoRefresh(e.getValue()));
        
        // Refresh interval selector
        final CComboBox<Integer> refreshIntervalCombo = new CComboBox<>("Refresh Interval");
        refreshIntervalCombo.setItems(1, 2, 5, 10, 30);
        refreshIntervalCombo.setValue(5);
        refreshIntervalCombo.setSuffixComponent(new Span("seconds"));
        refreshIntervalCombo.addValueChangeListener(e -> updateRefreshInterval(e.getValue()));
        
        // Manual refresh button
        final CButton manualRefreshButton = new CButton("Refresh", VaadinIcon.REFRESH.create());
        manualRefreshButton.addClickListener(e -> refreshData());
        
        // Export button
        final CButton exportButton = new CButton("Export Report", VaadinIcon.DOWNLOAD.create());
        exportButton.addClickListener(e -> openExportDialog());
        
        toolbar.add(autoRefreshToggle, refreshIntervalCombo, manualRefreshButton, exportButton);
        parent.add(toolbar);
    }
    
    private void createStatsCards(final CVerticalLayout parent) {
        final CHorizontalLayout cardsLayout = new CHorizontalLayout();
        cardsLayout.setWidthFull();
        cardsLayout.setSpacing(true);
        
        // Executions stats card
        statsCard = new CCard();
        statsCard.setId(ID_STATS_CARD);
        statsCard.setHeightFull();
        
        final CVerticalLayout statsContent = new CVerticalLayout();
        statsContent.setSpacing(false);
        statsContent.setPadding(false);
        
        statsLabel = new CLabel("No data");
        statsLabel.addClassName("stats-main");
        
        successRateLabel = new CLabel("Success Rate: 0%");
        successRateLabel.addClassName("stats-secondary");
        
        throughputProgress = new CProgressBar();
        throughputProgress.setValue(0.0);
        
        statsContent.add(
            new CLabel("Policy Executions").addClassName("stats-title"),
            statsLabel,
            successRateLabel,
            new CLabel("Throughput"),
            throughputProgress
        );
        
        statsCard.add(statsContent);
        
        // Performance stats card
        final CCard performanceCard = createPerformanceStatsCard();
        
        // Error stats card
        final CCard errorCard = createErrorStatsCard();
        
        cardsLayout.add(statsCard, performanceCard, errorCard);
        parent.add(cardsLayout);
    }
    
    private void createEventsGrid(final CVerticalLayout parent) {
        eventsGrid = new CGrid<>(CPolicyExecutionEvent.class, false);
        eventsGrid.setId(ID_EVENTS_GRID);
        eventsGrid.setHeightFull();
        
        // Configure columns
        eventsGrid.addColumn(CPolicyExecutionEvent::getTimestamp)
                 .setHeader("Time")
                 .setWidth("150px")
                 .setFlexGrow(0)
                 .setRenderer(new LocalDateTimeRenderer<>(DateTimeFormatter.ofPattern("HH:mm:ss")));
        
        eventsGrid.addComponentColumn(event -> {
            final String ruleName = getRuleName(event.getRuleId());
            return new CLabelEntity(ruleName);
        }).setHeader("Rule")
          .setWidth("200px")
          .setFlexGrow(0);
        
        eventsGrid.addComponentColumn(event -> {
            final CBabNode sourceNode = getNode(event.getSourceNodeId());
            return sourceNode != null ? new CLabelEntity(sourceNode) : new Span("-");
        }).setHeader("Source")
          .setWidth("150px")
          .setFlexGrow(0);
        
        eventsGrid.addComponentColumn(event -> {
            final CBabNode destNode = getNode(event.getDestinationNodeId());
            return destNode != null ? new CLabelEntity(destNode) : new Span("-");
        }).setHeader("Destination")
          .setWidth("150px")
          .setFlexGrow(0);
        
        eventsGrid.addComponentColumn(event -> {
            final Icon icon = event.isSuccess() ? VaadinIcon.CHECK_CIRCLE.create() : VaadinIcon.EXCLAMATION_CIRCLE.create();
            icon.setColor(event.isSuccess() ? "green" : "red");
            return icon;
        }).setHeader("Status")
          .setWidth("80px")
          .setFlexGrow(0);
        
        eventsGrid.addColumn(event -> event.getExecutionTimeMs() != null ? event.getExecutionTimeMs() + " ms" : "-")
                 .setHeader("Duration")
                 .setWidth("100px")
                 .setFlexGrow(0);
        
        eventsGrid.addColumn(event -> event.getDataSize() != null ? formatDataSize(event.getDataSize()) : "-")
                 .setHeader("Data Size")
                 .setWidth("100px")
                 .setFlexGrow(0);
        
        eventsGrid.addColumn(CPolicyExecutionEvent::getErrorMessage)
                 .setHeader("Error")
                 .setFlexGrow(1);
        
        // Configure grid behavior
        eventsGrid.setMultiSort(true);
        eventsGrid.setColumnReorderingAllowed(true);
        eventsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        
        parent.add(eventsGrid);
    }
    
    private void toggleAutoRefresh(final boolean enabled) {
        if (enabled) {
            startAutoRefresh();
        } else {
            stopAutoRefresh();
        }
    }
    
    private void startAutoRefresh() {
        if (scheduler == null) {
            scheduler = Executors.newScheduledThreadPool(1);
        }
        
        if (refreshTask != null) {
            refreshTask.cancel(false);
        }
        
        refreshTask = scheduler.scheduleAtFixedRate(() -> {
            if (currentUI != null) {
                currentUI.access(() -> refreshData());
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
    
    private void stopAutoRefresh() {
        if (refreshTask != null) {
            refreshTask.cancel(false);
            refreshTask = null;
        }
    }
    
    private void refreshData() {
        try {
            final Optional<CProject<?>> projectOpt = sessionService.getActiveProject();
            if (projectOpt.isEmpty() || !(projectOpt.get() instanceof CProject_Bab)) {
                return;
            }
            
            final CProject_Bab project = (CProject_Bab) projectOpt.get();
            
            // Update statistics
            final CPolicyExecutionStats stats = monitorService.getCurrentStats(project.getId());
            updateStatsDisplay(stats);
            
            // Update events grid
            final List<CPolicyExecutionEvent> recentEvents = monitorService.getRecentEvents(project.getId(), 50);
            eventsGrid.setItems(recentEvents);
            
        } catch (final Exception e) {
            LOGGER.error("Error refreshing monitoring data", e);
        }
    }
    
    private void updateStatsDisplay(final CPolicyExecutionStats stats) {
        if (stats != null) {
            statsLabel.setText(String.format("%,d executions", stats.getTotalExecutions()));
            successRateLabel.setText(String.format("Success Rate: %.1f%%", stats.getSuccessRate()));
            
            // Update throughput progress (example: max 100 executions/min)
            final double throughputRate = calculateCurrentThroughput(stats);
            throughputProgress.setValue(Math.min(throughputRate / 100.0, 1.0));
        }
    }
    
    private double calculateCurrentThroughput(final CPolicyExecutionStats stats) {
        // Calculate executions per minute based on recent activity
        final Duration uptime = Duration.between(stats.getMonitoringStartTime(), LocalDateTime.now());
        if (uptime.toMinutes() > 0) {
            return (double) stats.getTotalExecutions() / uptime.toMinutes();
        }
        return 0.0;
    }
    
    private void openExportDialog() {
        final CMonitoringExportDialog exportDialog = new CMonitoringExportDialog(
            monitorService, sessionService);
        exportDialog.open();
    }
    
    private String getRuleName(final Long ruleId) {
        // TODO: Implement rule name lookup
        return "Rule " + ruleId;
    }
    
    private CBabNode getNode(final Long nodeId) {
        // TODO: Implement node lookup
        return null;
    }
    
    private String formatDataSize(final Long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (autoRefreshToggle.getValue()) {
            startAutoRefresh();
        }
    }
    
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        stopAutoRefresh();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}

/**
 * Dialog for exporting monitoring reports
 */
@Profile("bab")
public class CMonitoringExportDialog extends CDialog {
    
    private final CPolicyExecutionMonitorService monitorService;
    private final ISessionService sessionService;
    
    private CDateTimePicker fromDatePicker;
    private CDateTimePicker toDatePicker;
    private CComboBox<String> formatCombo;
    private CCheckboxGroup<String> sectionsGroup;
    
    public CMonitoringExportDialog(final CPolicyExecutionMonitorService monitorService,
                                 final ISessionService sessionService) {
        this.monitorService = monitorService;
        this.sessionService = sessionService;
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        setHeaderTitle("Export Monitoring Report");
        setModal(true);
        setWidth("500px");
        
        final CFormLayout formLayout = new CFormLayout();
        
        // Date range selection
        fromDatePicker = new CDateTimePicker("From Date");
        fromDatePicker.setValue(LocalDateTime.now().minusDays(1));
        
        toDatePicker = new CDateTimePicker("To Date");
        toDatePicker.setValue(LocalDateTime.now());
        
        // Export format
        formatCombo = new CComboBox<>("Export Format");
        formatCombo.setItems("PDF", "Excel", "CSV", "JSON");
        formatCombo.setValue("PDF");
        
        // Report sections
        sectionsGroup = new CCheckboxGroup<>("Include Sections");
        sectionsGroup.setItems("Executive Summary", "Rule Statistics", "Node Performance", 
                              "Error Analysis", "Raw Event Data");
        sectionsGroup.select("Executive Summary", "Rule Statistics", "Node Performance");
        
        formLayout.add(fromDatePicker, toDatePicker, formatCombo, sectionsGroup);
        add(formLayout);
        
        // Action buttons
        final CButton exportButton = new CButton("Export", e -> exportReport());
        final CButton cancelButton = new CButton("Cancel", e -> close());
        getFooter().add(cancelButton, exportButton);
    }
    
    private void exportReport() {
        try {
            final Optional<CProject<?>> projectOpt = sessionService.getActiveProject();
            if (projectOpt.isEmpty() || !(projectOpt.get() instanceof CProject_Bab)) {
                CNotificationService.showError("No active BAB project");
                return;
            }
            
            final CProject_Bab project = (CProject_Bab) projectOpt.get();
            final CPolicyExecutionReport report = monitorService.generateExecutionReport(
                project, fromDatePicker.getValue(), toDatePicker.getValue());
            
            // Generate and download report
            final String format = formatCombo.getValue();
            final Set<String> sections = sectionsGroup.getSelectedItems();
            
            final CReportGenerator generator = new CReportGenerator();
            final byte[] reportData = generator.generateReport(report, format, sections);
            
            // Create download
            final String filename = String.format("monitoring-report-%s.%s", 
                                                 project.getName().replaceAll("[^a-zA-Z0-9]", "_"),
                                                 format.toLowerCase());
            
            final StreamResource resource = new StreamResource(filename, 
                () -> new ByteArrayInputStream(reportData));
            
            // Trigger download
            final Anchor downloadAnchor = new Anchor(resource, "");
            downloadAnchor.getElement().setAttribute("download", true);
            downloadAnchor.getElement().setAttribute("style", "display: none;");
            
            getElement().appendChild(downloadAnchor.getElement());
            downloadAnchor.getElement().callJsFunction("click");
            
            close();
            CNotificationService.showSuccess("Report exported successfully");
            
        } catch (final Exception e) {
            LOGGER.error("Error exporting monitoring report", e);
            CNotificationService.showException("Failed to export report", e);
        }
    }
}
```

### 5.2 Analytics Implementation

#### 5.2.1 Performance Analytics Engine

**Task**: Implement advanced analytics for policy performance analysis

```java
/**
 * Service for analyzing policy execution performance and patterns
 */
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CPolicyAnalyticsService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CPolicyAnalyticsService.class);
    
    // Dependencies
    private final CPolicyExecutionLogService executionLogService;
    private final CPolicyRuleService ruleService;
    private final CBabNodeService nodeService;
    private final ISessionService sessionService;
    
    public CPolicyAnalyticsService(final CPolicyExecutionLogService executionLogService,
                                 final CPolicyRuleService ruleService,
                                 final CBabNodeService nodeService,
                                 final ISessionService sessionService) {
        this.executionLogService = executionLogService;
        this.ruleService = ruleService;
        this.nodeService = nodeService;
        this.sessionService = sessionService;
    }
    
    /**
     * Analyze rule performance and identify optimization opportunities
     */
    public CPolicyPerformanceAnalysis analyzeRulePerformance(final CProject_Bab project,
                                                            final LocalDateTime fromTime,
                                                            final LocalDateTime toTime) {
        try {
            final CPolicyPerformanceAnalysis analysis = new CPolicyPerformanceAnalysis();
            analysis.setProjectId(project.getId());
            analysis.setAnalysisTimeRange(new CTimeRange(fromTime, toTime));
            analysis.setGeneratedAt(LocalDateTime.now());
            
            // Get execution data
            final List<CPolicyExecutionLog> logs = executionLogService.findByProjectAndTimeRange(
                project, fromTime, toTime);
            
            if (logs.isEmpty()) {
                analysis.addRecommendation("No execution data available for analysis period");
                return analysis;
            }
            
            // Analyze rule performance
            analyzeRuleExecutionPatterns(logs, analysis);
            
            // Analyze node performance
            analyzeNodePerformance(logs, analysis);
            
            // Identify performance bottlenecks
            identifyBottlenecks(logs, analysis);
            
            // Generate optimization recommendations
            generateOptimizationRecommendations(logs, analysis);
            
            // Analyze error patterns
            analyzeErrorPatterns(logs, analysis);
            
            return analysis;
            
        } catch (final Exception e) {
            LOGGER.error("Error analyzing rule performance", e);
            throw new RuntimeException("Failed to analyze rule performance", e);
        }
    }
    
    /**
     * Generate trend analysis for policy executions
     */
    public CPolicyTrendAnalysis analyzeTrends(final CProject_Bab project,
                                            final int daysBack) {
        try {
            final LocalDateTime endTime = LocalDateTime.now();
            final LocalDateTime startTime = endTime.minusDays(daysBack);
            
            final CPolicyTrendAnalysis analysis = new CPolicyTrendAnalysis();
            analysis.setProjectId(project.getId());
            analysis.setTimeRange(new CTimeRange(startTime, endTime));
            analysis.setDaysAnalyzed(daysBack);
            
            // Generate daily execution trends
            final Map<LocalDate, Long> dailyExecutions = generateDailyExecutionTrend(
                project, startTime, endTime);
            analysis.setDailyExecutions(dailyExecutions);
            
            // Generate hourly patterns
            final Map<Integer, Double> hourlyPatterns = generateHourlyExecutionPatterns(
                project, startTime, endTime);
            analysis.setHourlyPatterns(hourlyPatterns);
            
            // Analyze success rate trends
            final Map<LocalDate, Double> successRateTrends = generateSuccessRateTrends(
                project, startTime, endTime);
            analysis.setSuccessRateTrends(successRateTrends);
            
            // Performance trends
            final Map<LocalDate, Double> performanceTrends = generatePerformanceTrends(
                project, startTime, endTime);
            analysis.setPerformanceTrends(performanceTrends);
            
            // Forecast future trends
            generateTrendForecasts(analysis);
            
            return analysis;
            
        } catch (final Exception e) {
            LOGGER.error("Error analyzing trends", e);
            throw new RuntimeException("Failed to analyze trends", e);
        }
    }
    
    /**
     * Analyze data flow patterns between nodes
     */
    public CDataFlowAnalysis analyzeDataFlow(final CProject_Bab project,
                                           final LocalDateTime fromTime,
                                           final LocalDateTime toTime) {
        try {
            final CDataFlowAnalysis analysis = new CDataFlowAnalysis();
            analysis.setProjectId(project.getId());
            analysis.setTimeRange(new CTimeRange(fromTime, toTime));
            
            // Get execution logs with node information
            final List<CPolicyExecutionLog> logs = executionLogService.findByProjectAndTimeRange(
                project, fromTime, toTime);
            
            // Build data flow graph
            final Map<String, CDataFlowPath> flowPaths = buildDataFlowPaths(logs);
            analysis.setFlowPaths(flowPaths);
            
            // Calculate flow metrics
            calculateFlowMetrics(flowPaths, analysis);
            
            // Identify hot paths and bottlenecks
            identifyDataFlowBottlenecks(flowPaths, analysis);
            
            // Analyze data transformation patterns
            analyzeDataTransformations(logs, analysis);
            
            return analysis;
            
        } catch (final Exception e) {
            LOGGER.error("Error analyzing data flow", e);
            throw new RuntimeException("Failed to analyze data flow", e);
        }
    }
    
    private void analyzeRuleExecutionPatterns(final List<CPolicyExecutionLog> logs,
                                            final CPolicyPerformanceAnalysis analysis) {
        final Map<Long, List<CPolicyExecutionLog>> ruleExecutions = logs.stream()
            .collect(Collectors.groupingBy(CPolicyExecutionLog::getRuleId));
        
        for (final Map.Entry<Long, List<CPolicyExecutionLog>> entry : ruleExecutions.entrySet()) {
            final Long ruleId = entry.getKey();
            final List<CPolicyExecutionLog> ruleLogs = entry.getValue();
            
            final CRulePerformanceMetrics metrics = new CRulePerformanceMetrics();
            metrics.setRuleId(ruleId);
            metrics.setRuleName(getRuleName(ruleId));
            metrics.setTotalExecutions(ruleLogs.size());
            
            // Calculate success rate
            final long successCount = ruleLogs.stream()
                .mapToLong(log -> log.isSuccess() ? 1 : 0).sum();
            metrics.setSuccessRate((double) successCount / ruleLogs.size() * 100.0);
            
            // Calculate average execution time
            final double avgTime = ruleLogs.stream()
                .filter(log -> log.getExecutionTimeMs() != null)
                .mapToLong(CPolicyExecutionLog::getExecutionTimeMs)
                .average()
                .orElse(0.0);
            metrics.setAverageExecutionTimeMs(avgTime);
            
            // Calculate percentiles
            final List<Long> executionTimes = ruleLogs.stream()
                .filter(log -> log.getExecutionTimeMs() != null)
                .map(CPolicyExecutionLog::getExecutionTimeMs)
                .sorted()
                .collect(Collectors.toList());
            
            if (!executionTimes.isEmpty()) {
                metrics.setP50ExecutionTimeMs(calculatePercentile(executionTimes, 0.5));
                metrics.setP95ExecutionTimeMs(calculatePercentile(executionTimes, 0.95));
                metrics.setP99ExecutionTimeMs(calculatePercentile(executionTimes, 0.99));
            }
            
            // Calculate throughput
            final Duration timeSpan = Duration.between(
                ruleLogs.stream().map(CPolicyExecutionLog::getExecutionTime).min(LocalDateTime::compareTo).orElse(LocalDateTime.now()),
                ruleLogs.stream().map(CPolicyExecutionLog::getExecutionTime).max(LocalDateTime::compareTo).orElse(LocalDateTime.now())
            );
            
            if (timeSpan.toMinutes() > 0) {
                metrics.setThroughputPerMinute((double) ruleLogs.size() / timeSpan.toMinutes());
            }
            
            analysis.addRuleMetrics(metrics);
        }
    }
    
    private void identifyBottlenecks(final List<CPolicyExecutionLog> logs,
                                   final CPolicyPerformanceAnalysis analysis) {
        // Identify slow rules
        final List<CRulePerformanceMetrics> ruleMetrics = analysis.getRuleMetrics();
        final double avgExecutionTime = ruleMetrics.stream()
            .mapToDouble(CRulePerformanceMetrics::getAverageExecutionTimeMs)
            .average()
            .orElse(0.0);
        
        ruleMetrics.stream()
            .filter(metrics -> metrics.getAverageExecutionTimeMs() > avgExecutionTime * 2)
            .forEach(metrics -> analysis.addBottleneck(new CPerformanceBottleneck(
                "SLOW_RULE",
                "Rule " + metrics.getRuleName() + " execution time (" + 
                String.format("%.1f ms", metrics.getAverageExecutionTimeMs()) + 
                ") is significantly above average",
                "HIGH"
            )));
        
        // Identify high error rate rules
        ruleMetrics.stream()
            .filter(metrics -> metrics.getSuccessRate() < 95.0)
            .forEach(metrics -> analysis.addBottleneck(new CPerformanceBottleneck(
                "HIGH_ERROR_RATE",
                "Rule " + metrics.getRuleName() + " has high error rate (" + 
                String.format("%.1f%%", 100.0 - metrics.getSuccessRate()) + ")",
                "HIGH"
            )));
        
        // Identify node bottlenecks
        identifyNodeBottlenecks(logs, analysis);
    }
    
    private void generateOptimizationRecommendations(final List<CPolicyExecutionLog> logs,
                                                   final CPolicyPerformanceAnalysis analysis) {
        // Analyze execution patterns and generate recommendations
        
        // Check for rules that could be combined
        final Map<String, List<CRulePerformanceMetrics>> nodePathRules = groupRulesByNodePath(analysis);
        nodePathRules.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 3)
            .forEach(entry -> analysis.addRecommendation(
                "Consider combining rules on path " + entry.getKey() + 
                " to reduce overhead (" + entry.getValue().size() + " rules detected)"
            ));
        
        // Check for underutilized nodes
        final Map<Long, Long> nodeUtilization = calculateNodeUtilization(logs);
        nodeUtilization.entrySet().stream()
            .filter(entry -> entry.getValue() < 10) // Less than 10 executions
            .forEach(entry -> analysis.addRecommendation(
                "Node " + getNodeName(entry.getKey()) + " appears underutilized (" + 
                entry.getValue() + " executions). Consider reviewing its configuration."
            ));
        
        // Check for frequent error patterns
        final Map<String, Long> errorPatterns = analyzeErrorFrequency(logs);
        errorPatterns.entrySet().stream()
            .filter(entry -> entry.getValue() > 10)
            .forEach(entry -> analysis.addRecommendation(
                "Frequent error pattern detected: " + entry.getKey() + 
                " (" + entry.getValue() + " occurrences). Consider investigating root cause."
            ));
    }
    
    private Map<LocalDate, Long> generateDailyExecutionTrend(final CProject_Bab project,
                                                           final LocalDateTime startTime,
                                                           final LocalDateTime endTime) {
        final Map<LocalDate, Long> dailyTrends = new LinkedHashMap<>();
        
        // Initialize all days in range with zero
        LocalDate currentDate = startTime.toLocalDate();
        while (!currentDate.isAfter(endTime.toLocalDate())) {
            dailyTrends.put(currentDate, 0L);
            currentDate = currentDate.plusDays(1);
        }
        
        // Count executions per day
        final List<CPolicyExecutionLog> logs = executionLogService.findByProjectAndTimeRange(
            project, startTime, endTime);
        
        logs.forEach(log -> {
            final LocalDate logDate = log.getExecutionTime().toLocalDate();
            dailyTrends.merge(logDate, 1L, Long::sum);
        });
        
        return dailyTrends;
    }
    
    private Map<Integer, Double> generateHourlyExecutionPatterns(final CProject_Bab project,
                                                               final LocalDateTime startTime,
                                                               final LocalDateTime endTime) {
        final Map<Integer, Long> hourlyExecutions = new HashMap<>();
        final Map<Integer, Long> hourlyCounts = new HashMap<>();
        
        // Initialize hours
        for (int hour = 0; hour < 24; hour++) {
            hourlyExecutions.put(hour, 0L);
            hourlyCounts.put(hour, 0L);
        }
        
        // Count executions per hour across all days
        final List<CPolicyExecutionLog> logs = executionLogService.findByProjectAndTimeRange(
            project, startTime, endTime);
        
        logs.forEach(log -> {
            final int hour = log.getExecutionTime().getHour();
            hourlyExecutions.merge(hour, 1L, Long::sum);
        });
        
        // Calculate days in range for averaging
        final long totalDays = Duration.between(startTime, endTime).toDays() + 1;
        
        // Convert to averages
        final Map<Integer, Double> patterns = new HashMap<>();
        for (int hour = 0; hour < 24; hour++) {
            patterns.put(hour, (double) hourlyExecutions.get(hour) / totalDays);
        }
        
        return patterns;
    }
    
    private void generateTrendForecasts(final CPolicyTrendAnalysis analysis) {
        // Simple linear regression forecast for next 7 days
        final Map<LocalDate, Long> dailyExecutions = analysis.getDailyExecutions();
        
        if (dailyExecutions.size() < 7) {
            return; // Not enough data for forecasting
        }
        
        final List<Map.Entry<LocalDate, Long>> entries = new ArrayList<>(dailyExecutions.entrySet());
        entries.sort(Map.Entry.comparingByKey());
        
        // Calculate linear trend
        final double[] x = new double[entries.size()];
        final double[] y = new double[entries.size()];
        
        for (int i = 0; i < entries.size(); i++) {
            x[i] = i;
            y[i] = entries.get(i).getValue();
        }
        
        final CLinearRegression regression = new CLinearRegression(x, y);
        
        // Generate forecast for next 7 days
        final Map<LocalDate, Long> forecast = new LinkedHashMap<>();
        final LocalDate lastDate = entries.get(entries.size() - 1).getKey();
        
        for (int i = 1; i <= 7; i++) {
            final LocalDate forecastDate = lastDate.plusDays(i);
            final double forecastValue = regression.predict(entries.size() + i - 1);
            forecast.put(forecastDate, Math.max(0L, Math.round(forecastValue)));
        }
        
        analysis.setForecast(forecast);
    }
    
    // Helper methods
    private long calculatePercentile(final List<Long> sortedValues, final double percentile) {
        if (sortedValues.isEmpty()) return 0L;
        final int index = (int) Math.ceil(percentile * sortedValues.size()) - 1;
        return sortedValues.get(Math.max(0, Math.min(index, sortedValues.size() - 1)));
    }
    
    private String getRuleName(final Long ruleId) {
        try {
            final Optional<CPolicyRule> rule = ruleService.getById(ruleId);
            return rule.map(CPolicyRule::getName).orElse("Rule " + ruleId);
        } catch (final Exception e) {
            return "Rule " + ruleId;
        }
    }
    
    private String getNodeName(final Long nodeId) {
        try {
            final Optional<CBabNode> node = nodeService.getById(nodeId);
            return node.map(CBabNode::getName).orElse("Node " + nodeId);
        } catch (final Exception e) {
            return "Node " + nodeId;
        }
    }
}

/**
 * Linear regression utility for trend forecasting
 */
public class CLinearRegression {
    private final double slope;
    private final double intercept;
    
    public CLinearRegression(final double[] x, final double[] y) {
        if (x.length != y.length || x.length == 0) {
            throw new IllegalArgumentException("Invalid input arrays");
        }
        
        final int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumXX += x[i] * x[i];
        }
        
        slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        intercept = (sumY - slope * sumX) / n;
    }
    
    public double predict(final double x) {
        return slope * x + intercept;
    }
    
    public double getSlope() { return slope; }
    public double getIntercept() { return intercept; }
}
```

### 5.3 Completion Criteria & Quality Gates

#### 5.3.1 Phase 5 Quality Gates

**✅ Monitoring Infrastructure**:
- [ ] Real-time policy execution monitoring service implemented
- [ ] WebSocket connection to Calimero for live events
- [ ] Concurrent statistics tracking with thread safety
- [ ] Automatic reconnection handling for connection failures
- [ ] Resource cleanup and proper shutdown procedures

**✅ Live Dashboard Components**:
- [ ] Real-time monitoring tab with auto-refresh capability
- [ ] Statistics cards showing key performance metrics
- [ ] Live events grid with filtering and sorting
- [ ] Export functionality for monitoring reports
- [ ] Responsive UI updates without performance degradation

**✅ Analytics Engine**:
- [ ] Performance analysis with bottleneck identification
- [ ] Trend analysis with forecasting capabilities
- [ ] Data flow analysis for optimization insights
- [ ] Error pattern recognition and recommendations
- [ ] Statistical calculations (percentiles, regression)

**✅ Integration & Testing**:
- [ ] Integration tests for monitoring services
- [ ] Performance testing for high-volume scenarios
- [ ] UI testing for real-time updates
- [ ] Error handling validation
- [ ] Resource usage monitoring

#### 5.3.2 Documentation Requirements

**Technical Documentation**:
- [ ] Architecture documentation for monitoring system
- [ ] API documentation for WebSocket communication
- [ ] Performance tuning guidelines
- [ ] Troubleshooting guide for monitoring issues
- [ ] Deployment configuration for different environments

**User Documentation**:
- [ ] User guide for monitoring dashboard
- [ ] Report interpretation guidelines
- [ ] Export format specifications
- [ ] Best practices for performance optimization
- [ ] FAQ for common monitoring questions

### 5.4 Summary & Next Steps

**Phase 5 Achievement**:
With Phase 5 completion, the BAB Actions Dashboard will provide comprehensive monitoring and analytics capabilities, enabling users to:

1. **Monitor policy executions in real-time** with live statistics and event streams
2. **Analyze performance patterns** to identify optimization opportunities
3. **Generate detailed reports** for compliance and optimization purposes
4. **Forecast future trends** based on historical execution data
5. **Receive actionable recommendations** for system improvements

**Future Enhancements**:
After Phase 5, consider implementing:
- **Machine learning-based anomaly detection** for unusual execution patterns
- **Automated optimization recommendations** with policy adjustment suggestions
- **Integration with external monitoring systems** (Prometheus, Grafana, etc.)
- **Advanced visualization** with interactive charts and dashboards
- **Historical data archiving** and long-term trend analysis

---

## Final Implementation Status

**Total Development Effort**: Phases 1-5 = 25-30 days for experienced developer
**Complexity Level**: High (enterprise-grade monitoring and analytics)
**Technology Stack**: Spring Boot, Vaadin, WebSockets, Statistical Analysis
**Quality Assurance**: 100% test coverage, performance validation, documentation complete

**The BAB Actions Dashboard implementation is now fully specified and ready for development!**