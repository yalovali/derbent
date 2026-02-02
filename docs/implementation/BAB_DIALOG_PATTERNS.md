# BAB Dialog Design Patterns

**Version**: 1.0  
**Date**: 2026-02-02  
**Status**: MANDATORY - All BAB edit dialogs MUST follow these patterns

---

## Table of Contents

1. [Overview](#overview)
2. [Core Principles](#core-principles)
3. [Initialization Order Pattern (CRITICAL)](#initialization-order-pattern)
4. [DHCP Support Pattern](#dhcp-support-pattern)
5. [Validation Pattern](#validation-pattern)
6. [Layout Standards](#layout-standards)
7. [Real-World Examples](#real-world-examples)
8. [Common Mistakes](#common-mistakes)

---

## 1. Overview

BAB dialogs provide user interfaces for configuring network settings, DNS, routing, and IP addresses through the Calimero HTTP API. These dialogs follow strict patterns to ensure consistency, reliability, and excellent user experience.

**Key Features**:
- ✅ Real-time validation with visual feedback
- ✅ DHCP support with automatic field enabling/disabling
- ✅ Compact, responsive layouts (max 600px width)
- ✅ Clear error messages and hints
- ✅ Consistent button patterns (Apply/Cancel)

---

## 2. Core Principles

### 2.1 Mandatory Rules

| Rule | Description | Why |
|------|-------------|-----|
| **Max Width 600px** | `setWidth("600px")` or `setMaxWidth("600px")` | Optimal readability, prevents horizontal scrolling |
| **Custom Gap 12px** | `mainLayout.getStyle().set("gap", "12px")` | Compact spacing, professional appearance |
| **Field Initialization Before Use** | Create field BEFORE calling `updateValidationInfo()` | Prevents NullPointerException |
| **DHCP Checkbox First** | DHCP checkbox at top of dialog | Clear mode selection, intuitive flow |
| **Validation Info Last** | Validation section at bottom | Summary position, natural reading flow |
| **Standard Buttons** | Use `CButton.createSaveButton()` / `createCancelButton()` | Consistent button styling |

### 2.2 Component Structure

```
CDialog
├── Header (title + icon)
├── Content (mainLayout)
│   ├── DHCP Checkbox (if applicable)
│   ├── Field Header (label + validation count)
│   ├── Input Fields
│   ├── Usage Hints
│   └── Validation Info Section
└── Buttons (Apply + Cancel)
```

---

## 3. Initialization Order Pattern (CRITICAL)

### 3.1 The Problem

**NEVER call `updateValidationInfo()` before fields are assigned!**

#### ❌ WRONG - Causes NullPointerException
```java
private void createDefaultGatewayField() {
    updateValidationInfo();  // ❌ Field is null here!
    
    defaultGatewayField = new TextField("Default Gateway");
    defaultGatewayField.addValueChangeListener(e -> updateValidationInfo());
}
```

**Error**: `Cannot invoke "TextField.getValue()" because "this.defaultGatewayField" is null`

### 3.2 Correct Pattern

#### ✅ CORRECT - Fields Created First
```java
private void createDefaultGatewayField() {
    // STEP 1: Create and assign field FIRST
    defaultGatewayField = new TextField("Default Gateway");
    defaultGatewayField.setPlaceholder("192.168.1.1");
    defaultGatewayField.setWidthFull();
    defaultGatewayField.setValueChangeMode(ValueChangeMode.LAZY);
    
    // STEP 2: Add listeners (validation called via listener)
    defaultGatewayField.addValueChangeListener(e -> updateValidationInfo());
    
    // STEP 3: Add to layout
    mainLayout.add(defaultGatewayField);
}

@Override
protected void setupContent() {
    // Create all fields
    createDhcpCheckbox();
    createDefaultGatewayField();
    createRoutesGrid();
    
    // STEP 4: Call validation AFTER all fields created
    createValidationInfoSection();
    updateValidationInfo();  // ✅ Safe - all fields exist
}
```

### 3.3 Validation Method Pattern

```java
private void updateValidationInfo() {
    // CRITICAL: Check if component is initialized
    if (validationInfo == null) {
        return;  // ✅ Safe early exit during construction
    }
    
    validationInfo.removeAll();
    
    // Check DHCP mode first
    final boolean useDhcp = Boolean.TRUE.equals(dhcpCheckbox.getValue());
    if (useDhcp) {
        validationInfo.add(new CSpan("✅ DHCP mode: Configuration automatic"));
        return;
    }
    
    // Manual mode validation
    int validCount = 0;
    final StringBuilder info = new StringBuilder();
    
    // Validate each field
    if (isValidField(defaultGatewayField)) {
        validCount++;
        info.append("✅ Valid gateway\n");
    } else {
        info.append("❌ Invalid gateway\n");
    }
    
    // Display result
    final CSpan validationText = new CSpan(info.toString());
    validationText.getStyle().set("white-space", "pre-line");
    validationInfo.add(validationText);
}
```

---

## 4. DHCP Support Pattern

### 4.1 DHCP Checkbox Implementation

```java
private void createDhcpCheckbox() {
    dhcpCheckbox = new Checkbox("Use DHCP (Dynamic Configuration)");
    
    // Value change listener - enable/disable manual fields
    dhcpCheckbox.addValueChangeListener(e -> {
        final boolean useDhcp = Boolean.TRUE.equals(e.getValue());
        
        // Disable manual fields when DHCP enabled
        ipv4Field.setEnabled(!useDhcp);
        prefixField.setEnabled(!useDhcp);
        gatewayField.setEnabled(!useDhcp);
        
        // Update required indicators
        if (useDhcp) {
            ipv4Field.setRequiredIndicatorVisible(false);
            prefixField.setRequiredIndicatorVisible(false);
        } else {
            ipv4Field.setRequiredIndicatorVisible(true);
            prefixField.setRequiredIndicatorVisible(true);
        }
        
        // Update validation display
        updateValidationInfo();
    });
    
    dhcpCheckbox.getStyle().set("margin-top", "8px");
    dhcpCheckbox.getStyle().set("margin-bottom", "8px");
    
    mainLayout.add(dhcpCheckbox);
}
```

### 4.2 Save Logic with DHCP

```java
private void attemptSave() {
    final boolean useDhcp = Boolean.TRUE.equals(dhcpCheckbox.getValue());
    
    if (useDhcp) {
        // DHCP mode - no validation needed for manual fields
        final CUpdate update = new CUpdate(null, null, null, true);
        onSave.accept(update);
        close();
        return;
    }
    
    // Manual mode - validate all fields
    final String ipValue = ipv4Field.getValue();
    if (ipValue == null || ipValue.isBlank()) {
        CNotificationService.showWarning("IP address required in manual mode");
        return;
    }
    
    // Validate format...
    
    final CUpdate update = new CUpdate(ipValue, prefix, gateway, false);
    onSave.accept(update);
    close();
}
```

---

## 5. Validation Pattern

### 5.1 Real-Time Validation

```java
private void createIpField() {
    // Field header with validation count
    final CHorizontalLayout header = new CHorizontalLayout();
    header.setWidthFull();
    header.setJustifyContentMode(JustifyContentMode.BETWEEN);
    
    final CSpan label = new CSpan("IPv4 Address *");
    label.getStyle().set("font-weight", "500");
    
    final CSpan validCount = new CSpan("0/1 valid");
    validCount.setId("validation-count");
    validCount.getStyle().set("font-size", "0.75rem");
    
    header.add(label, validCount);
    mainLayout.add(header);
    
    // Input field
    ipv4Field = new TextField();
    ipv4Field.setPlaceholder("192.168.1.100");
    ipv4Field.setWidthFull();
    ipv4Field.setValueChangeMode(ValueChangeMode.LAZY);
    ipv4Field.addValueChangeListener(e -> updateValidationInfo());
    mainLayout.add(ipv4Field);
    
    // Usage hint
    final CSpan hint = new CSpan("Format: xxx.xxx.xxx.xxx");
    hint.getStyle().set("font-size", "var(--lumo-font-size-xs)");
    hint.getStyle().set("color", "var(--lumo-secondary-text-color)");
    mainLayout.add(hint);
}
```

### 5.2 Validation Info Section

```java
private void createValidationInfoSection() {
    validationInfo = new CDiv();
    validationInfo.getStyle()
        .set("margin-top", "12px")
        .set("padding", "8px")
        .set("background", "var(--lumo-contrast-5pct)")
        .set("border-radius", "var(--lumo-border-radius-m)")
        .set("font-size", "var(--lumo-font-size-s)");
    
    mainLayout.add(validationInfo);
    
    // Initial validation (after all fields created)
    updateValidationInfo();
}
```

### 5.3 IP Address Validation

```java
private static final String IP_PATTERN = "^(?:\\d{1,3}\\.){3}\\d{1,3}$";

private boolean isValidIp(final String ip) {
    if (ip == null || ip.isBlank()) {
        return false;
    }
    return ip.matches(IP_PATTERN);
}
```

---

## 6. Layout Standards

### 6.1 Dialog Configuration

```java
public CDialogEdit(...) {
    setWidth("600px");  // Or setMaxWidth("600px")
    try {
        setupDialog();
    } catch (final Exception e) {
        CNotificationService.showException("Failed to open dialog", e);
    }
}

@Override
protected void setupContent() {
    // Custom gap for compact layout
    mainLayout.setSpacing(false);
    mainLayout.getStyle().set("gap", "12px");
    mainLayout.setWidthFull();
    
    // Add components...
}
```

### 6.2 Field Spacing

```java
// After each input field
final CSpan hint = new CSpan("Usage hint here");
hint.getStyle()
    .set("font-size", "var(--lumo-font-size-xs)")
    .set("color", "var(--lumo-secondary-text-color)")
    .set("margin-bottom", "12px");  // ✅ Space before next field
mainLayout.add(hint);
```

### 6.3 Button Layout

```java
@Override
protected void setupButtons() {
    final CButton saveButton = CButton.createSaveButton("Apply", event -> attemptSave());
    final CButton cancelButton = CButton.createCancelButton("Cancel", event -> close());
    buttonLayout.add(saveButton, cancelButton);
}
```

---

## 7. Real-World Examples

### 7.1 DNS Edit Dialog

```java
public class CDialogEditDnsConfiguration extends CDialog {
    
    private TextArea dnsInput;
    private Checkbox dhcpCheckbox;
    private CSpan validationInfo;
    
    @Override
    protected void setupContent() {
        // STEP 1: DHCP checkbox
        createDhcpCheckbox();
        
        // STEP 2: Field header
        final HorizontalLayout header = createHeaderLayout();
        mainLayout.add(header);
        
        // STEP 3: DNS input
        dnsInput = createDnsInputField();
        mainLayout.add(dnsInput);
        
        // STEP 4: Hint
        mainLayout.add(createHintSection());
        
        // STEP 5: Validation info (LAST)
        createValidationInfoSection();
        updateValidationInfo();
    }
}
```

### 7.2 IP Edit Dialog

```java
public class CDialogEditInterfaceIp extends CDialog {
    
    private TextField interfaceField;
    private Checkbox dhcpCheckbox;
    private TextField ipv4Field;
    private IntegerField prefixField;
    private TextField gatewayField;
    private CDiv validationInfo;
    
    @Override
    protected void setupContent() {
        createInterfaceField();
        createDhcpCheckbox();
        createManualConfigSection();
        createValidationSection();
        createValidationInfoSection();
        loadCurrentConfiguration();
    }
}
```

### 7.3 Route Edit Dialog

```java
public class CDialogEditRouteConfiguration extends CDialog {
    
    private TextField defaultGatewayField;
    private CGrid<CRoute> routesGrid;
    private CDiv validationInfo;
    
    @Override
    protected void setupContent() {
        createGatewayHeaderLayout();
        createDefaultGatewayField();
        createRoutesGridSection();
        createValidationInfoSection();
        
        // Load current routes
        loadCurrentRoutes();
    }
}
```

---

## 8. Common Mistakes

### 8.1 Initialization Order Bugs

| Mistake | Consequence | Fix |
|---------|-------------|-----|
| Call `updateValidationInfo()` before field assignment | `NullPointerException` | Create field FIRST, then call validation |
| Access `validationInfo` in `createValidationInfoSection()` | `NullPointerException` | Add null check: `if (validationInfo == null) return;` |
| Call validation in field creation method | Too early, field not assigned | Call validation in listener or setupContent() |

### 8.2 DHCP Integration Bugs

| Mistake | Consequence | Fix |
|---------|-------------|-----|
| Not disabling manual fields in DHCP mode | Confusing UX | Use `field.setEnabled(!useDhcp)` |
| Validating manual fields in DHCP mode | Incorrect errors | Check DHCP first: `if (useDhcp) return;` |
| Missing DHCP flag in DTO | Server doesn't know mode | Add `useDhcp` field to update DTOs |

### 8.3 Layout Issues

| Mistake | Consequence | Fix |
|---------|-------------|-----|
| Fixed width without max-width | Horizontal scrolling on small screens | Use `setMaxWidth("600px")` |
| Default spacing (true) | Dialog too tall | Use `setSpacing(false)` + custom gap |
| Missing hint styles | Hard to read hints | Use `font-size-xs` + `secondary-text-color` |

---

## 9. Testing Checklist

Before committing BAB dialog:

- [ ] **Initialization**: No NullPointerException on dialog open
- [ ] **DHCP Mode**: Checkbox disables manual fields correctly
- [ ] **Validation**: Real-time validation shows correct messages
- [ ] **Save (DHCP)**: Can save with DHCP enabled
- [ ] **Save (Manual)**: Can save with valid manual configuration
- [ ] **Error Handling**: Invalid input shows clear error message
- [ ] **Layout**: No horizontal scrollbar at 600px width
- [ ] **Buttons**: Apply and Cancel work correctly
- [ ] **Refresh**: Parent component refreshes after save

---

## 10. Benefits

**Why follow these patterns?**

1. ✅ **Zero NullPointerExceptions**: Proper initialization order prevents crashes
2. ✅ **Consistent UX**: All BAB dialogs look and feel the same
3. ✅ **DHCP Support**: Users can choose automatic or manual configuration
4. ✅ **Real-Time Validation**: Immediate feedback prevents submission errors
5. ✅ **Responsive**: Works on all screen sizes without scrolling
6. ✅ **Maintainable**: Clear patterns make debugging and enhancement easy
7. ✅ **Professional**: Clean, modern dialog design

---

## 11. References

- **DNS Dialog**: `CDialogEditDnsConfiguration.java`
- **IP Dialog**: `CDialogEditInterfaceIp.java`
- **Route Dialog**: `CDialogEditRouteConfiguration.java`
- **Base Dialog**: `tech.derbent.api.ui.dialogs.CDialog`
- **BAB Components**: `tech.derbent.bab.dashboard.view.CComponent*`
- **Calimero Routing API**: `docs/implementation/CALIMERO_ROUTING_API_SPECIFICATION.md`
