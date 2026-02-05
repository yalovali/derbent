# BAB Dialog Patterns & Coding Rules

**Version**: 1.0  
**Date**: 2026-02-02  
**Status**: MANDATORY - All BAB dialogs MUST follow these patterns  
**Context**: Lessons learned from DNS and Route configuration dialogs

---

## Table of Contents

1. [Dialog Architecture](#dialog-architecture)
2. [Initialization Order Rules](#initialization-order-rules)
3. [Layout Patterns](#layout-patterns)
4. [Validation Patterns](#validation-patterns)
5. [HTTP API Integration](#http-api-integration)
6. [Component Toolbar Patterns](#component-toolbar-patterns)
7. [Complete Example](#complete-example)

---

## 1. Dialog Architecture

### 1.1 Base Class

**RULE**: All BAB configuration dialogs MUST extend `CDialog` and follow standard patterns.

```java
public class CDialogEditDnsConfiguration extends CDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(CDialogEditDnsConfiguration.class);
    private static final long serialVersionUID = 1L;
    
    // Constants
    public static final String ID_DIALOG = "custom-edit-dns-dialog";
    public static final String ID_TEXTAREA = "custom-dns-servers-textarea";
    
    // Dependencies
    private final CProject_Bab project;
    private final Runnable onSaveCallback;
    
    // UI Components
    private TextArea dnsServersArea;
    private Div validationInfo;
    
    public CDialogEditDnsConfiguration(final CProject_Bab project, final Runnable onSaveCallback) {
        super("Edit DNS Configuration", "600px");  // Title + width
        this.project = project;
        this.onSaveCallback = onSaveCallback;
        setId(ID_DIALOG);
    }
    
    @Override
    protected void setupContent(final VerticalLayout content) {
        // Dialog content setup
    }
    
    @Override
    protected void onConfirm() {
        // Save logic
    }
}
```

**Key Points**:
- Constructor: Takes dependencies + callback
- Uses standard `CDialog` constructor with title and width
- Implements `setupContent()` and `onConfirm()`
- Standard button methods inherited from `CDialog`

---

## 2. Initialization Order Rules (CRITICAL)

### 2.1 The NullPointerException Problem

**CRITICAL RULE**: NEVER call methods that use fields before those fields are initialized.

#### ❌ WRONG - Initialization Order Bug

```java
@Override
protected void setupContent(final VerticalLayout content) {
    // ❌ BUG: Calls updateValidationInfo() which uses validationInfo
    content.add(createHeaderLayout());  // Calls updateValidationInfo() internally
    
    // ❌ TOO LATE: validationInfo initialized after it's used
    content.add(createTextAreaSection());  // Creates validationInfo
}

private Component createHeaderLayout() {
    updateValidationInfo();  // ❌ validationInfo is null!
    return layout;
}

private Component createTextAreaSection() {
    validationInfo = new Div();  // Created AFTER being used
    return layout;
}
```

**Error**: `NullPointerException: Cannot invoke "Div.removeAll()" because "this.validationInfo" is null`

#### ✅ CORRECT - Proper Initialization Order

```java
@Override
protected void setupContent(final VerticalLayout content) {
    // ✅ STEP 1: Initialize ALL components first
    initializeComponents();
    
    // ✅ STEP 2: Create layout sections (can now use initialized fields)
    content.add(createHeaderLayout());
    content.add(createTextAreaSection());
    content.add(createUsageSection());
    
    // ✅ STEP 3: Load initial data
    loadCurrentConfiguration();
    
    // ✅ STEP 4: Setup listeners (after data loaded)
    setupListeners();
}

private void initializeComponents() {
    // Create ALL components upfront
    dnsServersArea = new TextArea();
    dnsServersArea.setId(ID_TEXTAREA);
    
    validationInfo = new Div();  // ✅ Created BEFORE use
    validationInfo.getStyle()
        .set("color", "var(--lumo-success-color)")
        .set("font-size", "var(--lumo-font-size-s)");
}

private Component createHeaderLayout() {
    // ✅ SAFE: validationInfo already initialized
    updateValidationInfo();
    return layout;
}
```

### 2.2 Initialization Pattern Template

**MANDATORY ORDER**:

```java
@Override
protected void setupContent(final VerticalLayout content) {
    // PHASE 1: INITIALIZE (create all objects)
    initializeComponents();
    
    // PHASE 2: ASSEMBLE (build UI structure)
    content.add(createHeaderLayout());
    content.add(createMainContentSection());
    content.add(createFooterSection());
    
    // PHASE 3: POPULATE (load data)
    loadInitialData();
    
    // PHASE 4: CONNECT (wire listeners)
    setupListeners();
}

private void initializeComponents() {
    // Create ALL UI components here
    // NO layout operations, NO data loading
    field1 = new TextField();
    field2 = new TextArea();
    infoDiv = new Div();
    grid = new Grid<>();
}

private Component createHeaderLayout() {
    // Assemble components into layouts
    // CAN call update methods (fields already exist)
    updateValidationInfo();  // ✅ SAFE
    return layout;
}

private void loadInitialData() {
    // Fetch and display data
    // Components and layouts already exist
}

private void setupListeners() {
    // Wire event handlers
    // Everything already initialized
}
```

---

## 5. Common Initialization Pitfalls (CRITICAL)

### Pitfall #1: Calling updateValidationInfo() Before Field Assignment

```java
// ❌ WRONG - validationInfo is null!
private CDiv createValidationInfoSection() {
    final CDiv div = new CDiv();
    updateValidationInfo();  // CRASH! validationInfo not assigned yet
    return div;
}

// ✅ CORRECT - Create first, assign, then update
protected void setupContent() {
    validationInfo = new CDiv();  // Create
    // ... add to layout ...
    updateValidationInfo();  // ✅ SAFE - now assigned
}
```

### Pitfall #2: Setting Initial Value in Field Factory

**The Problem**: Value change listeners fire DURING `setValue()`, before field is assigned to instance variable.

```java
// ❌ WRONG - Listener fires before field assigned!
private TextField createDefaultGatewayField() {
    final TextField field = new TextField();
    field.addValueChangeListener(e -> updateValidationInfo());  // Listener added
    field.setValue(initialValue);  // CRASH! Listener fires, but defaultGatewayField is null
    return field;
}

// ✅ CORRECT - Set value after assignment
private TextField createDefaultGatewayField() {
    final TextField field = new TextField();
    field.addValueChangeListener(e -> updateValidationInfo());
    // DO NOT set value here - listener will fire before field assigned!
    return field;
}

protected void setupContent() {
    defaultGatewayField = createDefaultGatewayField();  // Assign first
    defaultGatewayField.setValue(initialValue);  // ✅ SAFE - field assigned, listener can run
}
```

**Rule**: In field factory methods:
1. ✅ Create component
2. ✅ Add listeners
3. ❌ DO NOT set initial value
4. ✅ Return component

Then in `setupContent()`:
1. ✅ Assign field
2. ✅ Set initial value

---

## 3. Layout Patterns

### 3.1 Dialog Width and Spacing

**RULE**: Use max-width constraint with full width for responsive design.

```java
public CDialogEditDnsConfiguration(...) {
    super("Edit DNS Configuration", "600px");  // Max width
    
    // Dialog already handles:
    // - setMaxWidth("600px")
    // - setWidthFull()
    // - setSpacing(false)
    // - Custom gap: "12px"
}
```

**Benefits**:
- Responsive: Scales down on mobile
- No horizontal scrollbar issues
- Consistent spacing

### 3.2 Header Layout with Validation Counter

**PATTERN**: Title on left, validation counter on right.

```java
private Component createHeaderLayout() {
    final HorizontalLayout header = new HorizontalLayout();
    header.setWidthFull();
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.getStyle().set("margin-bottom", "8px");
    
    // Left: Title
    final Span title = new Span("DNS Servers *");
    title.getStyle()
        .set("font-weight", "bold")
        .set("font-size", "var(--lumo-font-size-m)");
    
    // Right: Validation counter
    validationInfo = new Div();
    validationInfo.getStyle()
        .set("color", "var(--lumo-success-color)")
        .set("font-size", "var(--lumo-font-size-s)");
    updateValidationInfo();  // Initial update
    
    header.add(title, validationInfo);
    return header;
}
```

### 3.3 Text Area Configuration

**PATTERN**: Full width, multi-line, placeholder text.

```java
private Component createTextAreaSection() {
    dnsServersArea = new TextArea();
    dnsServersArea.setId(ID_TEXTAREA);
    dnsServersArea.setWidthFull();
    dnsServersArea.setHeight("200px");
    dnsServersArea.setPlaceholder("8.8.8.8\n8.8.4.4\n1.1.1.1");
    dnsServersArea.getStyle()
        .set("font-family", "monospace")
        .set("font-size", "var(--lumo-font-size-s)");
    
    return dnsServersArea;
}
```

### 3.4 Usage Instructions Section

**PATTERN**: Info box below main content.

```java
private Component createUsageSection() {
    final Div usageInfo = new Div();
    usageInfo.getStyle()
        .set("background-color", "var(--lumo-contrast-5pct)")
        .set("padding", "12px")
        .set("border-radius", "var(--lumo-border-radius-m)")
        .set("font-size", "var(--lumo-font-size-s)")
        .set("color", "var(--lumo-secondary-text-color)");
    
    usageInfo.add(new Html("""
        <div>
            <strong>📝 Usage:</strong><br/>
            • Enter one IP address per line<br/>
            • First IP is the primary DNS server<br/>
            • Additional IPs are secondary servers<br/>
            • Example: 8.8.8.8 (Google DNS)
        </div>
        """));
    
    return usageInfo;
}
```

### 3.5 Editable Grid Pattern (Routes)

**PATTERN**: Grid with add/remove buttons and inline editing.

```java
private Component createRoutesGridSection() {
    // Grid configuration
    routesGrid = new Grid<>(CRouteEntry.class, false);
    routesGrid.setId(ID_GRID);
    routesGrid.setHeight("300px");
    
    // Editable columns
    final Grid.Column<CRouteEntry> networkColumn = routesGrid
        .addColumn(new TextRenderer<>(CRouteEntry::getNetwork))
        .setHeader("Network/CIDR")
        .setWidth("180px")
        .setFlexGrow(0)
        .setEditorComponent(createNetworkEditor());
    
    // Add/Remove toolbar
    final HorizontalLayout toolbar = new HorizontalLayout();
    toolbar.setSpacing(true);
    
    final Button buttonAdd = new Button("Add Route", VaadinIcon.PLUS.create());
    buttonAdd.setId(ID_BUTTON_ADD);
    buttonAdd.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
    buttonAdd.addClickListener(e -> on_buttonAdd_clicked());
    
    final Button buttonRemove = new Button("Remove", VaadinIcon.TRASH.create());
    buttonRemove.setId(ID_BUTTON_REMOVE);
    buttonRemove.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
    buttonRemove.setEnabled(false);
    
    toolbar.add(buttonAdd, buttonRemove);
    
    final VerticalLayout gridLayout = new VerticalLayout(toolbar, routesGrid);
    gridLayout.setSpacing(false);
    gridLayout.setPadding(false);
    gridLayout.getStyle().set("gap", "8px");
    
    return gridLayout;
}
```

---

## 4. Validation Patterns

### 4.1 IP Address Validation

**PATTERN**: Regex + real-time feedback.

```java
private static final Pattern IP_PATTERN = Pattern.compile(
    "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
);

private boolean isValidIpAddress(final String ip) {
    if (ip == null || ip.trim().isEmpty()) {
        return false;
    }
    return IP_PATTERN.matcher(ip.trim()).matches();
}

private List<String> validateDnsServers(final String input) {
    if (input == null || input.trim().isEmpty()) {
        throw new CValidationException("DNS servers cannot be empty");
    }
    
    final List<String> servers = new ArrayList<>();
    final String[] lines = input.split("\\n");
    
    for (int i = 0; i < lines.length; i++) {
        final String line = lines[i].trim();
        if (line.isEmpty()) {
            continue;  // Skip empty lines
        }
        
        if (!isValidIpAddress(line)) {
            throw new CValidationException(
                String.format("Invalid IP address on line %d: '%s'", i + 1, line));
        }
        
        servers.add(line);
    }
    
    if (servers.isEmpty()) {
        throw new CValidationException("At least one DNS server is required");
    }
    
    return servers;
}
```

### 4.2 Real-Time Validation Counter

**PATTERN**: Update counter on every keystroke.

```java
private void setupListeners() {
    dnsServersArea.addValueChangeListener(e -> updateValidationInfo());
}

private void updateValidationInfo() {
    try {
        final List<String> validServers = validateDnsServers(dnsServersArea.getValue());
        
        validationInfo.removeAll();
        validationInfo.add(new Span(
            String.format("✓ %d valid DNS server%s", 
                validServers.size(), 
                validServers.size() == 1 ? "" : "s")));
        validationInfo.getStyle().set("color", "var(--lumo-success-color)");
        
    } catch (final CValidationException ex) {
        validationInfo.removeAll();
        validationInfo.add(new Span("✗ " + ex.getMessage()));
        validationInfo.getStyle().set("color", "var(--lumo-error-color)");
    }
}
```

### 4.3 CIDR Validation (Routes)

```java
private static final Pattern CIDR_PATTERN = Pattern.compile(
    "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/(3[0-2]|[12]?[0-9])$"
);

private boolean isValidCidr(final String cidr) {
    if (cidr == null || cidr.trim().isEmpty()) {
        return false;
    }
    return CIDR_PATTERN.matcher(cidr.trim()).matches();
}

private void validateRouteEntry(final CRouteEntry entry) {
    if (!isValidCidr(entry.getNetwork())) {
        throw new CValidationException(
            String.format("Invalid network/CIDR: '%s'", entry.getNetwork()));
    }
    
    if (!isValidIpAddress(entry.getGateway())) {
        throw new CValidationException(
            String.format("Invalid gateway IP: '%s'", entry.getGateway()));
    }
}
```

---

## 5. HTTP API Integration

### 5.1 Save Handler Pattern

**PATTERN**: Validate → Show progress → Call API → Handle response → Refresh component.

```java
@Override
protected void onConfirm() {
    try {
        // STEP 1: Validate input
        final List<String> dnsServers = validateDnsServers(dnsServersArea.getValue());
        
        // STEP 2: Show progress notification
        CNotificationService.showInfo("Applying DNS configuration...");
        
        // STEP 3: Create DTO
        final CDnsConfigurationUpdate config = new CDnsConfigurationUpdate();
        config.setDnsServers(dnsServers);
        
        // STEP 4: Call HTTP API
        final CNetworkDnsCalimeroClient client = new CNetworkDnsCalimeroClient(project);
        final CCalimeroResponse<CDnsConfigurationUpdate> response = client.setDnsServers(config);
        
        // STEP 5: Handle response
        if (response.isSuccess()) {
            CNotificationService.showSuccess(
                String.format("DNS configuration applied successfully (%d servers)", 
                    dnsServers.size()));
            
            // STEP 6: Refresh parent component
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            
            // STEP 7: Close dialog
            close();
        } else {
            CNotificationService.showError(
                "Failed to apply DNS configuration: " + response.getMessage());
        }
        
    } catch (final CValidationException e) {
        CNotificationService.showValidationException(e);
    } catch (final Exception e) {
        LOGGER.error("Error applying DNS configuration", e);
        CNotificationService.showException("Failed to apply DNS configuration", e);
    }
}
```

### 5.2 Loading Current Configuration

**PATTERN**: Fetch from API → Populate UI.

```java
private void loadCurrentConfiguration() {
    try {
        final CNetworkDnsCalimeroClient client = new CNetworkDnsCalimeroClient(project);
        final CCalimeroResponse<List<String>> response = client.getDnsServers();
        
        if (response.isSuccess() && response.getData() != null) {
            final String dnsText = String.join("\n", response.getData());
            dnsServersArea.setValue(dnsText);
            LOGGER.debug("Loaded {} DNS servers", response.getData().size());
        } else {
            LOGGER.warn("Failed to load DNS servers: {}", response.getMessage());
        }
    } catch (final Exception e) {
        LOGGER.error("Error loading DNS configuration", e);
        CNotificationService.showException("Failed to load DNS configuration", e);
    }
}
```

---

## 6. Component Toolbar Patterns

### 6.1 CComponentBabBase Toolbar Standardization

**RULE**: All BAB components share common toolbar structure.

#### Base Class Pattern

```java
public abstract class CComponentBabBase extends CDiv implements Serializable {
    
    protected CHorizontalLayout toolbar;
    protected CButton buttonRefresh;
    protected CButton buttonEdit;
    
    /**
     * Creates standard BAB component toolbar with Refresh button.
     * Subclasses can add additional buttons via addToolbarButton().
     */
    protected void createStandardToolbar() {
        toolbar = new CHorizontalLayout();
        toolbar.setSpacing(true);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        
        // Standard Refresh button
        buttonRefresh = new CButton("Refresh", VaadinIcon.REFRESH.create());
        buttonRefresh.setId(getComponentId() + "-refresh");
        buttonRefresh.addThemeVariants(ButtonVariant.LUMO_SMALL);
        buttonRefresh.addClickListener(e -> refreshComponent());
        
        toolbar.add(buttonRefresh);
        add(toolbar);
    }
    
    /**
     * Creates standard Edit button (optional - call if component supports editing).
     */
    protected void createEditButton() {
        buttonEdit = new CButton("Edit", VaadinIcon.EDIT.create());
        buttonEdit.setId(getComponentId() + "-edit");
        buttonEdit.addThemeVariants(ButtonVariant.LUMO_SMALL);
        buttonEdit.addClickListener(e -> on_buttonEdit_clicked());
        
        addToolbarButton(buttonEdit);
    }
    
    /**
     * Adds custom button to toolbar.
     */
    protected void addToolbarButton(final Component button) {
        if (toolbar != null) {
            toolbar.add(button);
        }
    }
    
    /**
     * Override to handle edit button click.
     */
    protected void on_buttonEdit_clicked() {
        // Subclass implements
    }
    
    /**
     * Abstract method - must be implemented by subclasses.
     */
    protected abstract void refreshComponent();
    
    /**
     * Gets component-specific ID prefix.
     */
    protected abstract String getComponentId();
}
```

#### Subclass Usage

```java
public class CComponentDnsConfiguration extends CComponentBabBase {
    
    @Override
    protected void initializeComponents() {
        configureComponent();
        
        // Create standard toolbar with Refresh + Edit
        createStandardToolbar();
        createEditButton();  // Add Edit button
        
        // Add custom buttons if needed
        final CButton buttonAdvanced = new CButton("Advanced", VaadinIcon.COG.create());
        addToolbarButton(buttonAdvanced);
        
        createGrid();
        loadData();
    }
    
    @Override
    protected void on_buttonEdit_clicked() {
        openDnsEditDialog();
    }
    
    @Override
    protected void refreshComponent() {
        loadData();
    }
    
    @Override
    protected String getComponentId() {
        return "custom-dns-configuration";
    }
}
```

**Benefits**:
- Consistent toolbar appearance across all BAB components
- No duplicate button definitions
- Easy to add component-specific buttons
- Automatic ID generation for Playwright tests

---

## 7. Complete Example

### 7.1 DNS Configuration Dialog (Complete)

```java
@Profile("bab")
public class CDialogEditDnsConfiguration extends CDialog {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CDialogEditDnsConfiguration.class);
    private static final long serialVersionUID = 1L;
    
    // Constants
    public static final String ID_DIALOG = "custom-edit-dns-dialog";
    public static final String ID_TEXTAREA = "custom-dns-servers-textarea";
    
    private static final Pattern IP_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );
    
    // Dependencies
    private final CProject_Bab project;
    private final Runnable onSaveCallback;
    
    // UI Components
    private TextArea dnsServersArea;
    private Div validationInfo;
    
    public CDialogEditDnsConfiguration(final CProject_Bab project, final Runnable onSaveCallback) {
        super("Edit DNS Configuration", "600px");
        this.project = project;
        this.onSaveCallback = onSaveCallback;
        setId(ID_DIALOG);
    }
    
    @Override
    protected void setupContent(final VerticalLayout content) {
        // PHASE 1: Initialize
        initializeComponents();
        
        // PHASE 2: Assemble
        content.add(createHeaderLayout());
        content.add(createTextAreaSection());
        content.add(createUsageSection());
        
        // PHASE 3: Populate
        loadCurrentConfiguration();
        
        // PHASE 4: Connect
        setupListeners();
    }
    
    private void initializeComponents() {
        dnsServersArea = new TextArea();
        dnsServersArea.setId(ID_TEXTAREA);
        
        validationInfo = new Div();
        validationInfo.getStyle()
            .set("color", "var(--lumo-success-color)")
            .set("font-size", "var(--lumo-font-size-s)");
    }
    
    private Component createHeaderLayout() {
        final HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle().set("margin-bottom", "8px");
        
        final Span title = new Span("DNS Servers *");
        title.getStyle()
            .set("font-weight", "bold")
            .set("font-size", "var(--lumo-font-size-m)");
        
        updateValidationInfo();
        
        header.add(title, validationInfo);
        return header;
    }
    
    private Component createTextAreaSection() {
        dnsServersArea.setWidthFull();
        dnsServersArea.setHeight("200px");
        dnsServersArea.setPlaceholder("8.8.8.8\n8.8.4.4\n1.1.1.1");
        dnsServersArea.getStyle()
            .set("font-family", "monospace")
            .set("font-size", "var(--lumo-font-size-s)");
        
        return dnsServersArea;
    }
    
    private Component createUsageSection() {
        final Div usageInfo = new Div();
        usageInfo.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "12px")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("color", "var(--lumo-secondary-text-color)");
        
        usageInfo.add(new Html("""
            <div>
                <strong>📝 Usage:</strong><br/>
                • Enter one IP address per line<br/>
                • First IP is the primary DNS server<br/>
                • Additional IPs are secondary servers<br/>
                • Example: 8.8.8.8 (Google DNS)
            </div>
            """));
        
        return usageInfo;
    }
    
    private void loadCurrentConfiguration() {
        try {
            final CNetworkDnsCalimeroClient client = new CNetworkDnsCalimeroClient(project);
            final CCalimeroResponse<List<String>> response = client.getDnsServers();
            
            if (response.isSuccess() && response.getData() != null) {
                final String dnsText = String.join("\n", response.getData());
                dnsServersArea.setValue(dnsText);
                LOGGER.debug("Loaded {} DNS servers", response.getData().size());
            }
        } catch (final Exception e) {
            LOGGER.error("Error loading DNS configuration", e);
            CNotificationService.showException("Failed to load DNS configuration", e);
        }
    }
    
    private void setupListeners() {
        dnsServersArea.addValueChangeListener(e -> updateValidationInfo());
    }
    
    private void updateValidationInfo() {
        try {
            final List<String> validServers = validateDnsServers(dnsServersArea.getValue());
            
            validationInfo.removeAll();
            validationInfo.add(new Span(
                String.format("✓ %d valid DNS server%s", 
                    validServers.size(), 
                    validServers.size() == 1 ? "" : "s")));
            validationInfo.getStyle().set("color", "var(--lumo-success-color)");
            
        } catch (final CValidationException ex) {
            validationInfo.removeAll();
            validationInfo.add(new Span("✗ " + ex.getMessage()));
            validationInfo.getStyle().set("color", "var(--lumo-error-color)");
        }
    }
    
    private boolean isValidIpAddress(final String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        return IP_PATTERN.matcher(ip.trim()).matches();
    }
    
    private List<String> validateDnsServers(final String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new CValidationException("DNS servers cannot be empty");
        }
        
        final List<String> servers = new ArrayList<>();
        final String[] lines = input.split("\\n");
        
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            
            if (!isValidIpAddress(line)) {
                throw new CValidationException(
                    String.format("Invalid IP address on line %d: '%s'", i + 1, line));
            }
            
            servers.add(line);
        }
        
        if (servers.isEmpty()) {
            throw new CValidationException("At least one DNS server is required");
        }
        
        return servers;
    }
    
    @Override
    protected void onConfirm() {
        try {
            final List<String> dnsServers = validateDnsServers(dnsServersArea.getValue());
            
            CNotificationService.showInfo("Applying DNS configuration...");
            
            final CDnsConfigurationUpdate config = new CDnsConfigurationUpdate();
            config.setDnsServers(dnsServers);
            
            final CNetworkDnsCalimeroClient client = new CNetworkDnsCalimeroClient(project);
            final CCalimeroResponse<CDnsConfigurationUpdate> response = client.setDnsServers(config);
            
            if (response.isSuccess()) {
                CNotificationService.showSuccess(
                    String.format("DNS configuration applied successfully (%d servers)", 
                        dnsServers.size()));
                
                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }
                
                close();
            } else {
                CNotificationService.showError(
                    "Failed to apply DNS configuration: " + response.getMessage());
            }
            
        } catch (final CValidationException e) {
            CNotificationService.showValidationException(e);
        } catch (final Exception e) {
            LOGGER.error("Error applying DNS configuration", e);
            CNotificationService.showException("Failed to apply DNS configuration", e);
        }
    }
}
```

---

## 8. Checklist for New BAB Dialogs

When creating a new BAB configuration dialog, verify:

**Structure**:
- [ ] Extends `CDialog`
- [ ] Constructor: `super(title, width)`
- [ ] Implements `setupContent()` and `onConfirm()`
- [ ] Has component IDs for Playwright

**Initialization Order**:
- [ ] `setupContent()` calls `initializeComponents()` FIRST
- [ ] All UI fields created in `initializeComponents()`
- [ ] NO method calls that use fields before initialization
- [ ] Layout assembly AFTER initialization
- [ ] Data loading AFTER layout assembly
- [ ] Listeners setup LAST

**Layout**:
- [ ] Dialog width: "600px" (or appropriate)
- [ ] Header layout with title + validation counter
- [ ] Text areas/grids properly sized
- [ ] Usage instructions section
- [ ] Responsive (no horizontal scrollbar)

**Validation**:
- [ ] Real-time validation on input
- [ ] Visual feedback (color coding)
- [ ] Validation counter updates
- [ ] Clear error messages
- [ ] Empty input handling

**HTTP Integration**:
- [ ] Create DTO class in `tech.derbent.bab.dto`
- [ ] Calimero client with proper error handling
- [ ] Progress notifications
- [ ] Success/error feedback
- [ ] Refresh callback invoked

**Component Integration**:
- [ ] Component extends `CComponentBabBase`
- [ ] Uses `createStandardToolbar()`
- [ ] Calls `createEditButton()` if editable
- [ ] Implements `on_buttonEdit_clicked()`
- [ ] Opens dialog with proper callback

**Testing**:
- [ ] Playwright component IDs set
- [ ] Manual testing with valid data
- [ ] Manual testing with invalid data
- [ ] Manual testing with empty data
- [ ] Calimero HTTP API verified

---

## 9. Common Pitfalls to Avoid

### ❌ Pitfall 1: Using fields before initialization
```java
// ❌ WRONG
@Override
protected void setupContent(final VerticalLayout content) {
    content.add(createHeader());  // Calls updateValidationInfo()
    validationInfo = new Div();   // Created AFTER use!
}
```

### ❌ Pitfall 2: Wide info text causing horizontal scroll
```java
// ❌ WRONG
final Div info = new Div();
info.add(new Span("Very long text that exceeds dialog width..."));
// No width constraint - causes horizontal scrollbar!
```

### ❌ Pitfall 3: Not using CDialog standard buttons
```java
// ❌ WRONG - Reimplementing buttons
final Button saveButton = new Button("Save");
final Button cancelButton = new Button("Cancel");
footer.add(saveButton, cancelButton);
// CDialog already provides these!
```

### ❌ Pitfall 4: Duplicate toolbar buttons in subclass
```java
// ❌ WRONG - Hiding base class fields
public class CComponentDnsConfiguration extends CComponentBabBase {
    private CButton buttonRefresh;  // ❌ Hides base class field!
    private CButton buttonEdit;     // ❌ Hides base class field!
}
```

### ❌ Pitfall 5: No refresh callback
```java
// ❌ WRONG - Component not refreshed after save
@Override
protected void onConfirm() {
    client.setDnsServers(config);
    close();  // Component still shows old data!
}

// ✅ CORRECT
@Override
protected void onConfirm() {
    client.setDnsServers(config);
    if (onSaveCallback != null) {
        onSaveCallback.run();  // Refresh component
    }
    close();
}
```

---

## 10. Summary

**Key Takeaways**:

1. **Initialization Order is CRITICAL** - Initialize ALL components before using them
2. **Use CDialog Standard Patterns** - Title, width, buttons built-in
3. **Real-Time Validation** - Provide immediate feedback to users
4. **HTTP Integration** - Validate → Progress → API → Feedback → Refresh
5. **Toolbar Standardization** - Use `CComponentBabBase` patterns
6. **Responsive Layout** - Max-width + full-width prevents scrollbars
7. **Test Early** - Verify Calimero API before implementing UI

**Reference Implementations**:
- `CDialogEditDnsConfiguration.java` - Simple text area pattern
- `CDialogEditRouteConfiguration.java` - Editable grid pattern
- `CComponentBabBase.java` - Toolbar standardization
- `CNetworkDnsCalimeroClient.java` - HTTP client pattern

---

**Status**: This document captures all patterns and lessons learned from DNS and Route configuration dialogs. Follow these rules for ALL future BAB dialogs to maintain consistency and avoid common pitfalls.
