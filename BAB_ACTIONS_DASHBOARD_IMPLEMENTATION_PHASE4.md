# BAB Actions Dashboard Implementation Guide - Phase 4

**Version**: 1.0  
**Date**: 2026-02-05  
**Phase**: 4 - Advanced Features & Integration  
**Status**: Implementation Ready  

## Phase 4: Advanced Features & Integration

### 4.1 Advanced UI Components Implementation

#### 4.1.1 Policy Rule Grid Cell Components

**Task**: Implement specialized grid cells for policy rule editing with drag-drop support

**Classes to Implement**:

```java
/**
 * Base class for policy rule grid cells with common functionality
 */
@Profile("bab")
public abstract class CPolicyRuleGridCellBase extends CComponentBabBase {
    
    // Constants
    public static final String CSS_CLASS_CELL_ACTIVE = "policy-cell-active";
    public static final String CSS_CLASS_CELL_INACTIVE = "policy-cell-inactive";
    public static final String CSS_CLASS_CELL_ERROR = "policy-cell-error";
    public static final String CSS_CLASS_DRAG_OVER = "policy-cell-drag-over";
    
    // Dependencies
    protected final CPolicyRuleService ruleService;
    protected final ISessionService sessionService;
    
    // Cell data
    protected CPolicyRule rule;
    protected String cellType;
    protected boolean isEditable = true;
    
    protected CPolicyRuleGridCellBase(final String cellType, 
                                     final CPolicyRuleService ruleService,
                                     final ISessionService sessionService) {
        this.cellType = cellType;
        this.ruleService = ruleService;
        this.sessionService = sessionService;
        initializeComponents();
    }
    
    protected abstract void configureDragAndDrop();
    protected abstract void renderCellContent();
    protected abstract void validateCellData();
    
    public void setRule(final CPolicyRule rule) {
        this.rule = rule;
        renderCellContent();
        validateCellData();
    }
}

/**
 * Grid cell for source node selection with drag-drop
 */
@Profile("bab")
public class CPolicyRuleSourceNodeCell extends CPolicyRuleGridCellBase {
    
    private CLabel nodeLabel;
    private CButton editButton;
    
    public CPolicyRuleSourceNodeCell(final CPolicyRuleService ruleService,
                                   final ISessionService sessionService) {
        super("source_node", ruleService, sessionService);
    }
    
    @Override
    protected void initializeComponents() {
        setId("custom-policy-source-node-cell");
        addClassName("policy-rule-cell");
        
        nodeLabel = new CLabel("");
        editButton = new CButton("Edit", VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.addClickListener(e -> openNodeSelectionDialog());
        
        configureDragAndDrop();
        
        add(nodeLabel, editButton);
    }
    
    @Override
    protected void configureDragAndDrop() {
        // Enable drop for CBabNode entities
        setReceiver(DropEffect.MOVE, CBabNode.class, event -> {
            final CBabNode droppedNode = event.getDragData().orElse(null);
            if (droppedNode != null && isValidSourceNode(droppedNode)) {
                rule.setSourceNode(droppedNode);
                ruleService.save(rule);
                renderCellContent();
                CNotificationService.showSuccess("Source node assigned: " + droppedNode.getName());
            } else {
                CNotificationService.showWarning("Invalid source node type for this rule");
            }
        });
        
        // Visual feedback for drag over
        addDropListener(event -> addClassName(CSS_CLASS_DRAG_OVER));
        addDragLeaveListener(event -> removeClassName(CSS_CLASS_DRAG_OVER));
    }
    
    @Override
    protected void renderCellContent() {
        if (rule != null && rule.getSourceNode() != null) {
            final CBabNode sourceNode = rule.getSourceNode();
            nodeLabel.setText(sourceNode.getName());
            nodeLabel.getElement().getStyle().set("color", sourceNode.getDefaultColor());
            addClassName(CSS_CLASS_CELL_ACTIVE);
            removeClassName(CSS_CLASS_CELL_ERROR);
        } else {
            nodeLabel.setText("Drop Source Node");
            nodeLabel.getElement().getStyle().remove("color");
            addClassName(CSS_CLASS_CELL_INACTIVE);
        }
    }
    
    @Override
    protected void validateCellData() {
        if (rule != null && rule.getSourceNode() == null) {
            addClassName(CSS_CLASS_CELL_ERROR);
            setTooltipText("Source node is required for this rule");
        } else {
            removeClassName(CSS_CLASS_CELL_ERROR);
            setTooltipText("");
        }
    }
    
    private boolean isValidSourceNode(final CBabNode node) {
        // Validate based on rule type and node capabilities
        // E.g., only network nodes for network rules
        return node != null && node.getEnabled();
    }
    
    private void openNodeSelectionDialog() {
        final CPolicyRuleNodeSelectionDialog dialog = new CPolicyRuleNodeSelectionDialog(
            "Select Source Node", 
            getAvailableSourceNodes(),
            selectedNode -> {
                rule.setSourceNode(selectedNode);
                ruleService.save(rule);
                renderCellContent();
            });
        dialog.open();
    }
    
    private List<CBabNode> getAvailableSourceNodes() {
        final CProject_Bab project = (CProject_Bab) sessionService.getActiveProject().orElse(null);
        if (project != null) {
            return project.getNodes().stream()
                .filter(this::isValidSourceNode)
                .collect(Collectors.toList());
        }
        return List.of();
    }
}

/**
 * Grid cell for destination node with validation
 */
@Profile("bab")
public class CPolicyRuleDestinationNodeCell extends CPolicyRuleGridCellBase {
    // Similar implementation to source node but with destination-specific validation
}

/**
 * Grid cell for trigger entity configuration
 */
@Profile("bab")
public class CPolicyRuleTriggerCell extends CPolicyRuleGridCellBase {
    
    private CComboBox<String> triggerTypeCombo;
    private CButton configureButton;
    
    @Override
    protected void initializeComponents() {
        setId("custom-policy-trigger-cell");
        addClassName("policy-rule-cell");
        
        triggerTypeCombo = new CComboBox<>("Trigger");
        triggerTypeCombo.setItems("Timer", "Data Change", "Status Change", "External Event");
        triggerTypeCombo.addValueChangeListener(e -> onTriggerTypeChanged(e.getValue()));
        
        configureButton = new CButton("Configure", VaadinIcon.COG.create());
        configureButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        configureButton.addClickListener(e -> openTriggerConfigurationDialog());
        
        add(triggerTypeCombo, configureButton);
    }
    
    private void onTriggerTypeChanged(final String triggerType) {
        if (rule != null && triggerType != null) {
            // Create or update trigger configuration
            CPolicyTrigger trigger = rule.getTrigger();
            if (trigger == null) {
                trigger = new CPolicyTrigger();
                rule.setTrigger(trigger);
            }
            trigger.setTriggerType(triggerType);
            ruleService.save(rule);
            renderCellContent();
        }
    }
    
    private void openTriggerConfigurationDialog() {
        final CPolicyTriggerConfigurationDialog dialog = new CPolicyTriggerConfigurationDialog(
            rule.getTrigger(),
            rule.getSourceNode(),
            updatedTrigger -> {
                rule.setTrigger(updatedTrigger);
                ruleService.save(rule);
                renderCellContent();
            });
        dialog.open();
    }
}

/**
 * Grid cell for action entity configuration
 */
@Profile("bab")
public class CPolicyRuleActionCell extends CPolicyRuleGridCellBase {
    
    private CComboBox<String> actionTypeCombo;
    private CButton configureButton;
    private CLabel statusLabel;
    
    @Override
    protected void initializeComponents() {
        setId("custom-policy-action-cell");
        addClassName("policy-rule-cell");
        
        actionTypeCombo = new CComboBox<>("Action");
        actionTypeCombo.setItems("Send Message", "Execute Command", "Change Status", "Log Event", "Forward Data");
        actionTypeCombo.addValueChangeListener(e -> onActionTypeChanged(e.getValue()));
        
        configureButton = new CButton("Configure", VaadinIcon.COG.create());
        configureButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        configureButton.addClickListener(e -> openActionConfigurationDialog());
        
        statusLabel = new CLabel("");
        statusLabel.addClassName("action-status");
        
        add(actionTypeCombo, configureButton, statusLabel);
    }
    
    private void onActionTypeChanged(final String actionType) {
        if (rule != null && actionType != null) {
            CPolicyAction action = rule.getAction();
            if (action == null) {
                action = new CPolicyAction();
                rule.setAction(action);
            }
            action.setActionType(actionType);
            ruleService.save(rule);
            renderCellContent();
        }
    }
    
    private void openActionConfigurationDialog() {
        final CPolicyActionConfigurationDialog dialog = new CPolicyActionConfigurationDialog(
            rule.getAction(),
            rule.getDestinationNode(),
            updatedAction -> {
                rule.setAction(updatedAction);
                ruleService.save(rule);
                renderCellContent();
            });
        dialog.open();
    }
}

/**
 * Grid cell for filter configuration
 */
@Profile("bab")
public class CPolicyRuleFilterCell extends CPolicyRuleGridCellBase {
    
    private CButton addFilterButton;
    private CVerticalLayout filtersLayout;
    
    @Override
    protected void initializeComponents() {
        setId("custom-policy-filter-cell");
        addClassName("policy-rule-cell");
        
        addFilterButton = new CButton("Add Filter", VaadinIcon.PLUS.create());
        addFilterButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        addFilterButton.addClickListener(e -> openFilterSelectionDialog());
        
        filtersLayout = new CVerticalLayout();
        filtersLayout.setSpacing(false);
        filtersLayout.setPadding(false);
        
        add(addFilterButton, filtersLayout);
    }
    
    @Override
    protected void renderCellContent() {
        filtersLayout.removeAll();
        
        if (rule != null && rule.getFilters() != null) {
            rule.getFilters().forEach(filter -> {
                final CLabel filterLabel = new CLabel(filter.getFilterDescription());
                final CButton removeButton = new CButton(VaadinIcon.TRASH.create());
                removeButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                removeButton.addClickListener(e -> removeFilter(filter));
                
                final CHorizontalLayout filterRow = new CHorizontalLayout(filterLabel, removeButton);
                filterRow.setAlignItems(Alignment.CENTER);
                filtersLayout.add(filterRow);
            });
        }
        
        validateCellData();
    }
    
    private void openFilterSelectionDialog() {
        final CPolicyFilterSelectionDialog dialog = new CPolicyFilterSelectionDialog(
            rule.getSourceNode(),
            rule.getDestinationNode(),
            selectedFilter -> {
                if (rule.getFilters() == null) {
                    rule.setFilters(new HashSet<>());
                }
                rule.getFilters().add(selectedFilter);
                ruleService.save(rule);
                renderCellContent();
            });
        dialog.open();
    }
    
    private void removeFilter(final CPolicyFilter filter) {
        if (rule.getFilters() != null) {
            rule.getFilters().remove(filter);
            ruleService.save(rule);
            renderCellContent();
        }
    }
}
```

#### 4.1.2 Specialized Configuration Dialogs

**Task**: Implement configuration dialogs for triggers, actions, and filters

**Classes to Implement**:

```java
/**
 * Dialog for configuring policy triggers based on node type
 */
@Profile("bab")
public class CPolicyTriggerConfigurationDialog extends CDialog {
    
    private final CPolicyTrigger trigger;
    private final CBabNode sourceNode;
    private final Consumer<CPolicyTrigger> onSave;
    
    // Configuration components
    private CVerticalLayout configurationPanel;
    private CFormLayout formLayout;
    
    public CPolicyTriggerConfigurationDialog(final CPolicyTrigger trigger,
                                           final CBabNode sourceNode,
                                           final Consumer<CPolicyTrigger> onSave) {
        this.trigger = trigger;
        this.sourceNode = sourceNode;
        this.onSave = onSave;
        initializeComponents();
    }
    
    private void initializeComponents() {
        setHeaderTitle("Configure Trigger - " + sourceNode.getName());
        setModal(true);
        setWidth("600px");
        
        configurationPanel = new CVerticalLayout();
        configurationPanel.setSpacing(false);
        configurationPanel.setPadding(false);
        
        createTriggerConfiguration();
        
        add(configurationPanel);
        
        // Action buttons
        final CButton saveButton = new CButton("Save", e -> saveTrigger());
        final CButton cancelButton = new CButton("Cancel", e -> close());
        getFooter().add(cancelButton, saveButton);
    }
    
    private void createTriggerConfiguration() {
        formLayout = new CFormLayout();
        
        // Dynamic configuration based on trigger type and source node type
        if ("Timer".equals(trigger.getTriggerType())) {
            createTimerConfiguration();
        } else if ("Data Change".equals(trigger.getTriggerType())) {
            createDataChangeConfiguration();
        } else if ("Status Change".equals(trigger.getTriggerType())) {
            createStatusChangeConfiguration();
        }
        
        configurationPanel.add(formLayout);
    }
    
    private void createTimerConfiguration() {
        final CIntegerField intervalField = new CIntegerField("Interval (seconds)");
        intervalField.setValue(trigger.getTimerInterval() != null ? trigger.getTimerInterval() : 60);
        intervalField.addValueChangeListener(e -> trigger.setTimerInterval(e.getValue()));
        
        final CCheckbox enabledCheckbox = new CCheckbox("Enabled");
        enabledCheckbox.setValue(trigger.getEnabled() != null ? trigger.getEnabled() : true);
        enabledCheckbox.addValueChangeListener(e -> trigger.setEnabled(e.getValue()));
        
        formLayout.add(intervalField, enabledCheckbox);
    }
    
    private void createDataChangeConfiguration() {
        if (sourceNode instanceof CBabNodeCAN) {
            createCANDataChangeConfiguration();
        } else if (sourceNode instanceof CBabNodeHTTP) {
            createHTTPDataChangeConfiguration();
        } else if (sourceNode instanceof CBabNodeFile) {
            createFileDataChangeConfiguration();
        }
    }
    
    private void createCANDataChangeConfiguration() {
        final CTextField canIdField = new CTextField("CAN ID");
        canIdField.setValue(trigger.getCanId() != null ? trigger.getCanId() : "");
        canIdField.addValueChangeListener(e -> trigger.setCanId(e.getValue()));
        
        final CComboBox<String> dataTypeCombo = new CComboBox<>("Data Type");
        dataTypeCombo.setItems("Raw", "OBD", "J1939");
        dataTypeCombo.setValue(trigger.getDataType() != null ? trigger.getDataType() : "Raw");
        dataTypeCombo.addValueChangeListener(e -> trigger.setDataType(e.getValue()));
        
        formLayout.add(canIdField, dataTypeCombo);
    }
    
    private void createHTTPDataChangeConfiguration() {
        final CTextField endpointField = new CTextField("HTTP Endpoint");
        endpointField.setValue(trigger.getHttpEndpoint() != null ? trigger.getHttpEndpoint() : "/api/data");
        endpointField.addValueChangeListener(e -> trigger.setHttpEndpoint(e.getValue()));
        
        final CComboBox<String> methodCombo = new CComboBox<>("HTTP Method");
        methodCombo.setItems("GET", "POST", "PUT", "DELETE");
        methodCombo.setValue(trigger.getHttpMethod() != null ? trigger.getHttpMethod() : "POST");
        methodCombo.addValueChangeListener(e -> trigger.setHttpMethod(e.getValue()));
        
        formLayout.add(endpointField, methodCombo);
    }
    
    private void createFileDataChangeConfiguration() {
        final CTextField filePathField = new CTextField("File Path");
        filePathField.setValue(trigger.getFilePath() != null ? trigger.getFilePath() : "");
        filePathField.addValueChangeListener(e -> trigger.setFilePath(e.getValue()));
        
        final CComboBox<String> fileTypeCombo = new CComboBox<>("File Type");
        fileTypeCombo.setItems("Log", "CSV", "JSON", "XML", "Binary");
        fileTypeCombo.setValue(trigger.getFileType() != null ? trigger.getFileType() : "Log");
        fileTypeCombo.addValueChangeListener(e -> trigger.setFileType(e.getValue()));
        
        formLayout.add(filePathField, fileTypeCombo);
    }
    
    private void saveTrigger() {
        try {
            validateTriggerConfiguration();
            onSave.accept(trigger);
            close();
            CNotificationService.showSuccess("Trigger configuration saved");
        } catch (final Exception e) {
            CNotificationService.showException("Failed to save trigger configuration", e);
        }
    }
    
    private void validateTriggerConfiguration() {
        if ("Timer".equals(trigger.getTriggerType())) {
            if (trigger.getTimerInterval() == null || trigger.getTimerInterval() <= 0) {
                throw new IllegalArgumentException("Timer interval must be positive");
            }
        } else if ("Data Change".equals(trigger.getTriggerType())) {
            if (sourceNode instanceof CBabNodeCAN && 
                (trigger.getCanId() == null || trigger.getCanId().trim().isEmpty())) {
                throw new IllegalArgumentException("CAN ID is required for CAN data change triggers");
            }
        }
        // Additional validation based on trigger type...
    }
}

/**
 * Dialog for configuring policy actions
 */
@Profile("bab")
public class CPolicyActionConfigurationDialog extends CDialog {
    
    private final CPolicyAction action;
    private final CBabNode destinationNode;
    private final Consumer<CPolicyAction> onSave;
    
    public CPolicyActionConfigurationDialog(final CPolicyAction action,
                                          final CBabNode destinationNode,
                                          final Consumer<CPolicyAction> onSave) {
        this.action = action;
        this.destinationNode = destinationNode;
        this.onSave = onSave;
        initializeComponents();
    }
    
    // Similar structure to trigger dialog but for action configuration
}

/**
 * Dialog for selecting and configuring filters
 */
@Profile("bab")
public class CPolicyFilterSelectionDialog extends CDialog {
    
    private final CBabNode sourceNode;
    private final CBabNode destinationNode;
    private final Consumer<CPolicyFilter> onSave;
    
    public CPolicyFilterSelectionDialog(final CBabNode sourceNode,
                                      final CBabNode destinationNode,
                                      final Consumer<CPolicyFilter> onSave) {
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.onSave = onSave;
        initializeComponents();
    }
    
    // Implementation for filter selection and configuration
}
```

### 4.2 Advanced Workflow Implementation

#### 4.2.1 Policy Rule Drag & Drop Manager

**Task**: Implement comprehensive drag-and-drop functionality for policy rule creation

```java
/**
 * Manager for handling drag and drop operations in policy rule grid
 */
@Profile("bab")
public class CPolicyRuleDragDropManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CPolicyRuleDragDropManager.class);
    
    private final CPolicyRuleService ruleService;
    private final ISessionService sessionService;
    
    public CPolicyRuleDragDropManager(final CPolicyRuleService ruleService,
                                    final ISessionService sessionService) {
        this.ruleService = ruleService;
        this.sessionService = sessionService;
    }
    
    /**
     * Configure drag source for node list component
     */
    public void configureNodeListDragSource(final CComponentNodeList nodeList) {
        nodeList.getGrid().setRowsDraggable(true);
        
        nodeList.getGrid().addDragStartListener(event -> {
            final CBabNode draggedNode = event.getDraggedItems().iterator().next();
            event.setDragData(draggedNode);
            
            // Visual feedback
            nodeList.getGrid().getElement().getClassList().add("dragging-active");
            
            LOGGER.debug("Started dragging node: {}", draggedNode.getName());
        });
        
        nodeList.getGrid().addDragEndListener(event -> {
            nodeList.getGrid().getElement().getClassList().remove("dragging-active");
            LOGGER.debug("Ended dragging");
        });
    }
    
    /**
     * Configure drop targets for rule grid cells
     */
    public void configureRuleGridDropTargets(final CGrid<CPolicyRule> ruleGrid) {
        // This will be handled by individual grid cells
        // Each cell type (source, destination, etc.) will have its own drop configuration
    }
    
    /**
     * Validate if a node can be dropped on a specific rule cell
     */
    public boolean validateNodeDrop(final CBabNode node, final CPolicyRule rule, final String cellType) {
        switch (cellType) {
            case "source_node":
                return validateSourceNodeDrop(node, rule);
            case "destination_node":
                return validateDestinationNodeDrop(node, rule);
            default:
                return false;
        }
    }
    
    private boolean validateSourceNodeDrop(final CBabNode node, final CPolicyRule rule) {
        // Validate based on node capabilities and rule requirements
        if (node == null || !node.getEnabled()) {
            return false;
        }
        
        // Check if node supports the required protocols
        if (rule.getRequiredProtocols() != null) {
            return node.getSupportedProtocols().containsAll(rule.getRequiredProtocols());
        }
        
        return true;
    }
    
    private boolean validateDestinationNodeDrop(final CBabNode node, final CPolicyRule rule) {
        // Similar validation for destination node
        if (node == null || !node.getEnabled()) {
            return false;
        }
        
        // Ensure source and destination are different
        if (rule.getSourceNode() != null && rule.getSourceNode().equals(node)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Handle successful node drop on rule cell
     */
    public void handleNodeDrop(final CBabNode droppedNode, final CPolicyRule rule, final String cellType) {
        try {
            switch (cellType) {
                case "source_node":
                    rule.setSourceNode(droppedNode);
                    break;
                case "destination_node":
                    rule.setDestinationNode(droppedNode);
                    break;
                default:
                    LOGGER.warn("Unknown cell type for node drop: {}", cellType);
                    return;
            }
            
            ruleService.save(rule);
            LOGGER.debug("Successfully assigned {} as {} for rule {}", 
                        droppedNode.getName(), cellType, rule.getId());
            
        } catch (final Exception e) {
            LOGGER.error("Error handling node drop", e);
            throw new RuntimeException("Failed to assign node to rule", e);
        }
    }
}
```

#### 4.2.2 Policy Export & Apply Manager

**Task**: Implement policy export to JSON and transmission to Calimero system

```java
/**
 * Manager for exporting policies and applying them to Calimero system
 */
@Profile("bab")
public class CPolicyExportManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CPolicyExportManager.class);
    
    private final CPolicyRuleService ruleService;
    private final ISessionService sessionService;
    private final ObjectMapper objectMapper;
    
    public CPolicyExportManager(final CPolicyRuleService ruleService,
                              final ISessionService sessionService) {
        this.ruleService = ruleService;
        this.sessionService = sessionService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Export complete project policy as JSON
     */
    public String exportProjectPolicyAsJson() throws Exception {
        final CProject_Bab project = getCurrentBabProject();
        final List<CPolicyRule> rules = ruleService.listByProject(project);
        
        final CPolicyExportDTO export = new CPolicyExportDTO();
        export.setProjectId(project.getId());
        export.setProjectName(project.getName());
        export.setExportTimestamp(LocalDateTime.now());
        export.setVersion("1.0");
        
        // Convert rules to export format
        export.setRules(rules.stream()
            .map(this::convertRuleToExport)
            .collect(Collectors.toList()));
        
        // Convert nodes to export format
        export.setNodes(project.getNodes().stream()
            .map(this::convertNodeToExport)
            .collect(Collectors.toList()));
        
        final String json = objectMapper.writeValueAsString(export);
        LOGGER.debug("Exported policy JSON: {} characters", json.length());
        
        return json;
    }
    
    /**
     * Apply exported policy to Calimero system
     */
    public CCalimeroResponse<String> applyPolicyToCalimero(final String policyJson) throws Exception {
        final CProject_Bab project = getCurrentBabProject();
        
        // Create Calimero client
        final CCalimeroClient client = new CCalimeroClient(project);
        
        // Prepare policy application request
        final CCalimeroRequest request = CCalimeroRequest.builder()
            .type("policy")
            .operation("apply")
            .data(policyJson)
            .timestamp(LocalDateTime.now())
            .build();
        
        LOGGER.info("Applying policy to Calimero system for project: {}", project.getName());
        
        try {
            final CCalimeroResponse<String> response = client.sendRequest(request, String.class);
            
            if (response.isSuccess()) {
                // Update policy status in database
                updatePolicyApplicationStatus(project, true, response.getMessage());
                LOGGER.info("Policy successfully applied to Calimero system");
            } else {
                updatePolicyApplicationStatus(project, false, response.getMessage());
                LOGGER.error("Failed to apply policy: {}", response.getMessage());
            }
            
            return response;
            
        } catch (final Exception e) {
            updatePolicyApplicationStatus(project, false, e.getMessage());
            LOGGER.error("Error applying policy to Calimero system", e);
            throw e;
        }
    }
    
    /**
     * Validate policy before export
     */
    public List<String> validatePolicy() {
        final List<String> validationErrors = new ArrayList<>();
        final CProject_Bab project = getCurrentBabProject();
        final List<CPolicyRule> rules = ruleService.listByProject(project);
        
        // Check for incomplete rules
        for (final CPolicyRule rule : rules) {
            if (rule.getSourceNode() == null) {
                validationErrors.add("Rule " + rule.getId() + ": Missing source node");
            }
            if (rule.getDestinationNode() == null) {
                validationErrors.add("Rule " + rule.getId() + ": Missing destination node");
            }
            if (rule.getTrigger() == null) {
                validationErrors.add("Rule " + rule.getId() + ": Missing trigger configuration");
            }
            if (rule.getAction() == null) {
                validationErrors.add("Rule " + rule.getId() + ": Missing action configuration");
            }
        }
        
        // Check for node conflicts
        validateNodeConflicts(rules, validationErrors);
        
        // Check for circular dependencies
        validateCircularDependencies(rules, validationErrors);
        
        return validationErrors;
    }
    
    private CPolicyRuleExportDTO convertRuleToExport(final CPolicyRule rule) {
        final CPolicyRuleExportDTO dto = new CPolicyRuleExportDTO();
        dto.setId(rule.getId());
        dto.setName(rule.getName());
        dto.setEnabled(rule.getEnabled());
        dto.setSourceNodeId(rule.getSourceNode() != null ? rule.getSourceNode().getId() : null);
        dto.setDestinationNodeId(rule.getDestinationNode() != null ? rule.getDestinationNode().getId() : null);
        
        // Convert trigger
        if (rule.getTrigger() != null) {
            dto.setTrigger(convertTriggerToExport(rule.getTrigger()));
        }
        
        // Convert action
        if (rule.getAction() != null) {
            dto.setAction(convertActionToExport(rule.getAction()));
        }
        
        // Convert filters
        if (rule.getFilters() != null) {
            dto.setFilters(rule.getFilters().stream()
                .map(this::convertFilterToExport)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    private CBabNodeExportDTO convertNodeToExport(final CBabNode node) {
        final CBabNodeExportDTO dto = new CBabNodeExportDTO();
        dto.setId(node.getId());
        dto.setName(node.getName());
        dto.setNodeType(node.getNodeType());
        dto.setEnabled(node.getEnabled());
        
        // Convert node-specific configuration
        if (node instanceof CBabNodeCAN) {
            dto.setCanConfiguration(convertCANNodeToExport((CBabNodeCAN) node));
        } else if (node instanceof CBabNodeHTTP) {
            dto.setHttpConfiguration(convertHTTPNodeToExport((CBabNodeHTTP) node));
        } else if (node instanceof CBabNodeFile) {
            dto.setFileConfiguration(convertFileNodeToExport((CBabNodeFile) node));
        }
        
        return dto;
    }
    
    private void validateNodeConflicts(final List<CPolicyRule> rules, final List<String> validationErrors) {
        // Check for rules that might conflict with each other
        final Map<CBabNode, List<CPolicyRule>> nodeRules = new HashMap<>();
        
        for (final CPolicyRule rule : rules) {
            if (rule.getSourceNode() != null) {
                nodeRules.computeIfAbsent(rule.getSourceNode(), k -> new ArrayList<>()).add(rule);
            }
        }
        
        for (final Map.Entry<CBabNode, List<CPolicyRule>> entry : nodeRules.entrySet()) {
            if (entry.getValue().size() > 1) {
                // Check for conflicting rules on the same node
                validateConflictingRules(entry.getKey(), entry.getValue(), validationErrors);
            }
        }
    }
    
    private void validateCircularDependencies(final List<CPolicyRule> rules, final List<String> validationErrors) {
        // Build dependency graph and check for cycles
        final Map<CBabNode, Set<CBabNode>> dependencies = new HashMap<>();
        
        for (final CPolicyRule rule : rules) {
            if (rule.getSourceNode() != null && rule.getDestinationNode() != null) {
                dependencies.computeIfAbsent(rule.getSourceNode(), k -> new HashSet<>())
                          .add(rule.getDestinationNode());
            }
        }
        
        // Use DFS to detect cycles
        final Set<CBabNode> visited = new HashSet<>();
        final Set<CBabNode> recursionStack = new HashSet<>();
        
        for (final CBabNode node : dependencies.keySet()) {
            if (hasCycle(node, dependencies, visited, recursionStack)) {
                validationErrors.add("Circular dependency detected involving node: " + node.getName());
                break;
            }
        }
    }
    
    private boolean hasCycle(final CBabNode node, 
                           final Map<CBabNode, Set<CBabNode>> dependencies,
                           final Set<CBabNode> visited, 
                           final Set<CBabNode> recursionStack) {
        visited.add(node);
        recursionStack.add(node);
        
        final Set<CBabNode> neighbors = dependencies.get(node);
        if (neighbors != null) {
            for (final CBabNode neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    if (hasCycle(neighbor, dependencies, visited, recursionStack)) {
                        return true;
                    }
                } else if (recursionStack.contains(neighbor)) {
                    return true;
                }
            }
        }
        
        recursionStack.remove(node);
        return false;
    }
    
    private CProject_Bab getCurrentBabProject() {
        final CProject<?> project = sessionService.getActiveProject()
            .orElseThrow(() -> new IllegalStateException("No active project"));
        
        if (!(project instanceof CProject_Bab)) {
            throw new IllegalStateException("Active project is not a BAB project");
        }
        
        return (CProject_Bab) project;
    }
    
    private void updatePolicyApplicationStatus(final CProject_Bab project, 
                                             final boolean success, 
                                             final String message) {
        try {
            project.setLastPolicyAppliedAt(LocalDateTime.now());
            project.setLastPolicyApplicationSuccess(success);
            project.setLastPolicyApplicationMessage(message);
            // Save via project service
        } catch (final Exception e) {
            LOGGER.error("Error updating policy application status", e);
        }
    }
}

/**
 * DTO classes for policy export
 */
public class CPolicyExportDTO {
    private Long projectId;
    private String projectName;
    private String version;
    private LocalDateTime exportTimestamp;
    private List<CPolicyRuleExportDTO> rules;
    private List<CBabNodeExportDTO> nodes;
    
    // Standard getters and setters...
}

public class CPolicyRuleExportDTO {
    private Long id;
    private String name;
    private Boolean enabled;
    private Long sourceNodeId;
    private Long destinationNodeId;
    private CPolicyTriggerExportDTO trigger;
    private CPolicyActionExportDTO action;
    private List<CPolicyFilterExportDTO> filters;
    
    // Standard getters and setters...
}
```

### 4.3 Integration & Testing

#### 4.3.1 Integration Testing Strategy

**Task**: Implement comprehensive testing for BAB Actions Dashboard

```java
/**
 * Integration tests for BAB Actions Dashboard components
 */
@SpringBootTest
@TestPropertySource(properties = {"spring.profiles.active=test,bab"})
@Transactional
public class CBabActionsDashboardIntegrationTest {
    
    @Autowired
    private CPolicyRuleService policyRuleService;
    
    @Autowired
    private CBabNodeService babNodeService;
    
    @Autowired
    private ISessionService sessionService;
    
    private CProject_Bab testProject;
    private CBabNodeCAN canNode;
    private CBabNodeHTTP httpNode;
    
    @BeforeEach
    void setUp() {
        // Create test BAB project
        testProject = createTestBabProject();
        
        // Create test nodes
        canNode = createTestCanNode();
        httpNode = createTestHttpNode();
        
        // Set active project in session
        sessionService.setActiveProject(testProject);
    }
    
    @Test
    void testPolicyRuleCreation() {
        // Test: Create complete policy rule
        final CPolicyRule rule = policyRuleService.newEntity();
        rule.setName("Test CAN to HTTP Rule");
        rule.setSourceNode(canNode);
        rule.setDestinationNode(httpNode);
        
        // Configure trigger
        final CPolicyTrigger trigger = new CPolicyTrigger();
        trigger.setTriggerType("Data Change");
        trigger.setCanId("0x123");
        trigger.setDataType("J1939");
        rule.setTrigger(trigger);
        
        // Configure action
        final CPolicyAction action = new CPolicyAction();
        action.setActionType("Send Message");
        action.setHttpEndpoint("/api/vehicle/data");
        action.setHttpMethod("POST");
        rule.setAction(action);
        
        // Save and verify
        final CPolicyRule savedRule = policyRuleService.save(rule);
        assertNotNull(savedRule.getId());
        assertEquals("Test CAN to HTTP Rule", savedRule.getName());
        assertNotNull(savedRule.getTrigger());
        assertNotNull(savedRule.getAction());
    }
    
    @Test
    void testPolicyValidation() {
        // Test: Validation catches incomplete rules
        final CPolicyExportManager exportManager = new CPolicyExportManager(policyRuleService, sessionService);
        
        // Create incomplete rule
        final CPolicyRule incompleteRule = policyRuleService.newEntity();
        incompleteRule.setName("Incomplete Rule");
        incompleteRule.setSourceNode(canNode);
        // Missing destination, trigger, action
        policyRuleService.save(incompleteRule);
        
        final List<String> validationErrors = exportManager.validatePolicy();
        assertFalse(validationErrors.isEmpty());
        assertTrue(validationErrors.stream().anyMatch(error -> error.contains("Missing destination node")));
        assertTrue(validationErrors.stream().anyMatch(error -> error.contains("Missing trigger configuration")));
        assertTrue(validationErrors.stream().anyMatch(error -> error.contains("Missing action configuration")));
    }
    
    @Test
    void testDragDropValidation() {
        // Test: Drag drop validation works correctly
        final CPolicyRuleDragDropManager dragDropManager = new CPolicyRuleDragDropManager(policyRuleService, sessionService);
        
        final CPolicyRule rule = policyRuleService.newEntity();
        rule.setName("Test Rule");
        
        // Valid source node drop
        assertTrue(dragDropManager.validateNodeDrop(canNode, rule, "source_node"));
        
        // Invalid destination (same as source)
        rule.setSourceNode(canNode);
        assertFalse(dragDropManager.validateNodeDrop(canNode, rule, "destination_node"));
        
        // Valid destination (different from source)
        assertTrue(dragDropManager.validateNodeDrop(httpNode, rule, "destination_node"));
    }
    
    @Test
    void testPolicyExportJson() throws Exception {
        // Test: Complete policy export to JSON
        createCompleteTestPolicy();
        
        final CPolicyExportManager exportManager = new CPolicyExportManager(policyRuleService, sessionService);
        final String exportJson = exportManager.exportProjectPolicyAsJson();
        
        assertNotNull(exportJson);
        assertFalse(exportJson.trim().isEmpty());
        
        // Verify JSON structure
        final ObjectMapper objectMapper = new ObjectMapper();
        final CPolicyExportDTO exportDto = objectMapper.readValue(exportJson, CPolicyExportDTO.class);
        
        assertEquals(testProject.getId(), exportDto.getProjectId());
        assertEquals(testProject.getName(), exportDto.getProjectName());
        assertNotNull(exportDto.getRules());
        assertFalse(exportDto.getRules().isEmpty());
        assertNotNull(exportDto.getNodes());
        assertFalse(exportDto.getNodes().isEmpty());
    }
    
    private CProject_Bab createTestBabProject() {
        final CProject_Bab project = new CProject_Bab("Test BAB Project");
        project.setDescription("Test project for BAB Actions Dashboard");
        return project;
    }
    
    private CBabNodeCAN createTestCanNode() {
        final CBabNodeCAN node = new CBabNodeCAN("CAN Vehicle Bus", "CAN");
        node.setBitrate(500000);
        node.setEnabled(true);
        return babNodeService.save(node);
    }
    
    private CBabNodeHTTP createTestHttpNode() {
        final CBabNodeHTTP node = new CBabNodeHTTP("HTTP Server", "HTTP");
        node.setBaseUrl("http://localhost:8080");
        node.setEnabled(true);
        return babNodeService.save(node);
    }
    
    private void createCompleteTestPolicy() {
        final CPolicyRule rule = policyRuleService.newEntity();
        rule.setName("Vehicle Data Forwarding");
        rule.setSourceNode(canNode);
        rule.setDestinationNode(httpNode);
        rule.setEnabled(true);
        
        // Complete trigger configuration
        final CPolicyTrigger trigger = new CPolicyTrigger();
        trigger.setTriggerType("Data Change");
        trigger.setCanId("0x18FEF100");
        trigger.setDataType("J1939");
        trigger.setEnabled(true);
        rule.setTrigger(trigger);
        
        // Complete action configuration
        final CPolicyAction action = new CPolicyAction();
        action.setActionType("Send Message");
        action.setHttpEndpoint("/api/vehicle/diagnostics");
        action.setHttpMethod("POST");
        action.setEnabled(true);
        rule.setAction(action);
        
        policyRuleService.save(rule);
    }
}
```

#### 4.3.2 UI Component Testing

**Task**: Implement Playwright tests for dashboard UI components

```javascript
// tests/bab-actions-dashboard.spec.js
const { test, expect } = require('@playwright/test');

test.describe('BAB Actions Dashboard', () => {
    
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('#username', 'testuser');
        await page.fill('#password', 'testpass');
        await page.click('#login-button');
        
        // Navigate to BAB project
        await page.click('#projects-menu');
        await page.click('[data-testid="bab-project"]');
        
        // Open Actions Dashboard
        await page.click('#actions-dashboard-menu');
        await expect(page.locator('#custom-actions-dashboard')).toBeVisible();
    });
    
    test('should display node list and working area', async ({ page }) => {
        // Verify main dashboard components are visible
        await expect(page.locator('#custom-dashboard-node-list')).toBeVisible();
        await expect(page.locator('#custom-dashboard-working-area')).toBeVisible();
        
        // Verify node list contains nodes
        const nodeItems = page.locator('#custom-dashboard-node-list .node-item');
        await expect(nodeItems).toHaveCountGreaterThan(0);
        
        // Verify working area has tabs
        await expect(page.locator('#custom-working-area-tab-rules')).toBeVisible();
        await expect(page.locator('#custom-working-area-tab-logs')).toBeVisible();
        await expect(page.locator('#custom-working-area-tab-views')).toBeVisible();
    });
    
    test('should create new policy rule via drag and drop', async ({ page }) => {
        // Click on Rules tab
        await page.click('#custom-working-area-tab-rules');
        
        // Add new rule
        await page.click('#custom-rules-add-button');
        
        // Verify new rule appears in grid
        const ruleRows = page.locator('#custom-policy-rules-grid tbody tr');
        const initialCount = await ruleRows.count();
        
        await expect(ruleRows).toHaveCountGreaterThan(0);
        
        // Drag CAN node to source column
        const canNode = page.locator('#custom-dashboard-node-list [data-node-type="CAN"]').first();
        const sourceCell = page.locator('#custom-policy-rules-grid .source-node-cell').first();
        
        await canNode.dragTo(sourceCell);
        
        // Verify source node is assigned
        await expect(sourceCell.locator('.node-label')).toHaveText(/CAN/);
        
        // Drag HTTP node to destination column
        const httpNode = page.locator('#custom-dashboard-node-list [data-node-type="HTTP"]').first();
        const destinationCell = page.locator('#custom-policy-rules-grid .destination-node-cell').first();
        
        await httpNode.dragTo(destinationCell);
        
        // Verify destination node is assigned
        await expect(destinationCell.locator('.node-label')).toHaveText(/HTTP/);
    });
    
    test('should configure trigger and action', async ({ page }) => {
        // Navigate to rules tab
        await page.click('#custom-working-area-tab-rules');
        
        // Click on trigger cell
        const triggerCell = page.locator('#custom-policy-rules-grid .trigger-cell').first();
        await triggerCell.locator('vaadin-combo-box').click();
        await page.click('[value="Data Change"]');
        
        // Open trigger configuration
        await triggerCell.locator('#configure-button').click();
        
        // Verify trigger configuration dialog
        await expect(page.locator('[aria-label="Configure Trigger"]')).toBeVisible();
        
        // Configure CAN trigger
        await page.fill('#can-id-field', '0x123');
        await page.selectOption('#data-type-combo', 'J1939');
        
        // Save configuration
        await page.click('#save-trigger-button');
        
        // Configure action
        const actionCell = page.locator('#custom-policy-rules-grid .action-cell').first();
        await actionCell.locator('vaadin-combo-box').click();
        await page.click('[value="Send Message"]');
        
        // Open action configuration
        await actionCell.locator('#configure-button').click();
        
        // Configure HTTP action
        await page.fill('#http-endpoint-field', '/api/vehicle/data');
        await page.selectOption('#http-method-combo', 'POST');
        
        // Save action configuration
        await page.click('#save-action-button');
        
        // Verify configurations are saved
        await expect(triggerCell.locator('.status-label')).toHaveText(/Configured/);
        await expect(actionCell.locator('.status-label')).toHaveText(/Configured/);
    });
    
    test('should validate and apply policy', async ({ page }) => {
        // Navigate to rules tab
        await page.click('#custom-working-area-tab-rules');
        
        // Create complete rule (assume helper function)
        await createCompleteRule(page);
        
        // Click Apply Policy button
        await page.click('#custom-apply-policy-button');
        
        // Verify validation dialog
        await expect(page.locator('[aria-label="Validate Policy"]')).toBeVisible();
        
        // Should show validation success
        await expect(page.locator('#validation-results')).toHaveText(/Validation passed/);
        
        // Confirm application
        await page.click('#confirm-apply-button');
        
        // Verify success notification
        await expect(page.locator('.notification-success')).toHaveText(/Policy applied successfully/);
        
        // Verify policy status updated
        await expect(page.locator('#policy-status')).toHaveText(/Applied/);
    });
    
    test('should display validation errors for incomplete rules', async ({ page }) => {
        // Navigate to rules tab
        await page.click('#custom-working-area-tab-rules');
        
        // Create incomplete rule
        await page.click('#custom-rules-add-button');
        // Don't configure trigger or action
        
        // Try to apply policy
        await page.click('#custom-apply-policy-button');
        
        // Verify validation errors
        await expect(page.locator('#validation-results')).toHaveText(/Missing trigger configuration/);
        await expect(page.locator('#validation-results')).toHaveText(/Missing action configuration/);
        
        // Apply button should be disabled
        await expect(page.locator('#confirm-apply-button')).toBeDisabled();
    });
    
    async function createCompleteRule(page) {
        // Add new rule
        await page.click('#custom-rules-add-button');
        
        // Assign source and destination nodes via drag-drop
        const canNode = page.locator('#custom-dashboard-node-list [data-node-type="CAN"]').first();
        const httpNode = page.locator('#custom-dashboard-node-list [data-node-type="HTTP"]').first();
        
        await canNode.dragTo(page.locator('#custom-policy-rules-grid .source-node-cell').first());
        await httpNode.dragTo(page.locator('#custom-policy-rules-grid .destination-node-cell').first());
        
        // Configure trigger
        const triggerCell = page.locator('#custom-policy-rules-grid .trigger-cell').first();
        await triggerCell.locator('vaadin-combo-box').selectOption('Data Change');
        await triggerCell.locator('#configure-button').click();
        await page.fill('#can-id-field', '0x123');
        await page.click('#save-trigger-button');
        
        // Configure action
        const actionCell = page.locator('#custom-policy-rules-grid .action-cell').first();
        await actionCell.locator('vaadin-combo-box').selectOption('Send Message');
        await actionCell.locator('#configure-button').click();
        await page.fill('#http-endpoint-field', '/api/data');
        await page.click('#save-action-button');
    }
});
```

### 4.4 Documentation & Quality Gates

#### 4.4.1 Implementation Validation Checklist

**Phase 4 Quality Gates**:

**✅ Advanced UI Components**:
- [ ] All grid cell components implement drag-drop functionality
- [ ] Specialized configuration dialogs for triggers, actions, filters
- [ ] Visual feedback for drag-over states
- [ ] Validation for incompatible node assignments
- [ ] Error handling for configuration failures

**✅ Workflow Management**:
- [ ] Comprehensive drag-drop manager with validation
- [ ] Policy export to JSON with complete rule structure
- [ ] Integration with Calimero API for policy application
- [ ] Validation for circular dependencies and conflicts
- [ ] Status tracking for policy application results

**✅ Integration & Testing**:
- [ ] Unit tests for all new service classes
- [ ] Integration tests for complete workflows
- [ ] Playwright tests for UI interactions
- [ ] Performance testing for large rule sets
- [ ] Error scenario testing and recovery

**✅ Code Quality**:
- [ ] All classes follow Derbent naming conventions
- [ ] Proper use of @Profile("bab") annotations
- [ ] Complete JavaDoc documentation
- [ ] Error handling with appropriate logging
- [ ] Null-safe implementations throughout

#### 4.4.2 Pattern Reuse Validation

**Validation of Derbent Patterns**:

**✅ Entity Patterns**:
- [ ] All new entities extend proper base classes
- [ ] Constructor patterns follow initialization rules
- [ ] Service-based copy pattern implemented
- [ ] Repository queries use eager loading
- [ ] @AMetaData annotations for UI generation

**✅ Service Patterns**:
- [ ] validateEntity() methods implemented
- [ ] Unique validation helpers used
- [ ] Proper transaction annotations
- [ ] Profile-based registration
- [ ] Dependency injection via constructor

**✅ UI Patterns**:
- [ ] CComponentBabBase extension for BAB components
- [ ] @Transient placeholder pattern for form integration
- [ ] Component factory methods in page services
- [ ] CLabelEntity for colorful grid columns
- [ ] Proper component ID conventions

**✅ BAB Patterns**:
- [ ] CCalimeroClient for HTTP communication
- [ ] JSON serialization for policy export
- [ ] Session service for context access
- [ ] Error handling with notifications
- [ ] Real-time data refresh capabilities

### 4.5 Completion Criteria

**Phase 4 is complete when**:

1. **All specialized UI components are implemented and functional**
2. **Drag-drop workflow works end-to-end from node list to policy application**
3. **Policy validation catches all error scenarios**
4. **JSON export produces valid Calimero-compatible policies**
5. **Integration tests cover all major workflows**
6. **Playwright tests verify UI interactions**
7. **All code follows Derbent patterns and quality standards**
8. **Performance is acceptable for typical rule set sizes (100+ rules)**

**Next Steps After Phase 4**:
- Implement additional working area tabs (Logs, Views, Monitoring)
- Add real-time policy execution monitoring
- Implement policy versioning and rollback
- Add advanced visualization for policy dependencies
- Integrate with external monitoring systems

---

## Implementation Notes

**Total Estimated Effort**: Phase 4 - 8-10 days for experienced developer

**Key Dependencies**:
- Phase 1-3 completion (entities, services, basic UI)
- Calimero API specification for policy format
- Vaadin 24.x drag-drop APIs
- Jackson JSON processing library

**Risk Mitigation**:
- Incremental development with frequent testing
- Prototype drag-drop interactions early
- Validate JSON format with Calimero team
- Plan for graceful degradation if external systems unavailable

**Success Metrics**:
- Policy creation time: < 5 minutes for complex rules
- Validation feedback: < 2 seconds for full project
- Export generation: < 10 seconds for 100+ rules
- UI responsiveness: < 500ms for drag-drop operations