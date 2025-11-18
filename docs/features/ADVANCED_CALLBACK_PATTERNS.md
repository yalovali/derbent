# Advanced Component Callback Patterns

This document provides advanced real-world examples of the component callback pattern.

## Pattern 1: Dependent Field Updates

Update dependent ComboBox items when a parent field changes:

```java
protected void on_country_change(Component component, Object value) {
    LOGGER.info("Country changed to: {}", value);
    
    // Clear the dependent city field
    setComponentValue("city", null);
    
    // Get the city ComboBox and update its items
    ComboBox<CCity> cityCombo = getComboBox("city");
    if (cityCombo != null && value != null) {
        List<CCity> cities = cityService.getCitiesByCountry((CCountry) value);
        cityCombo.setItems(cities);
    }
}
```

## Pattern 2: Real-time Validation

Validate input when field loses focus:

```java
protected void on_email_blur(Component component) {
    Object emailValue = getComponentValue("email");
    
    if (emailValue != null && !emailValue.toString().trim().isEmpty()) {
        String email = emailValue.toString();
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            CNotificationService.showWarning("Please enter a valid email address");
            
            // Optionally refocus the field
            TextField emailField = getTextField("email");
            if (emailField != null) {
                emailField.focus();
            }
        }
    }
}

protected void on_endDate_change(Component component, Object value) {
    Object startDateValue = getComponentValue("startDate");
    
    if (startDateValue != null && value != null) {
        LocalDate startDate = (LocalDate) startDateValue;
        LocalDate endDate = (LocalDate) value;
        
        if (endDate.isBefore(startDate)) {
            CNotificationService.showWarning("End date must be after start date");
            setComponentValue("endDate", null);
        }
    }
}
```

## Pattern 3: Auto-Calculation

Calculate dependent values automatically:

```java
protected void on_price_change(Component component, Object value) {
    if (value == null) return;
    
    Double price = ((Number) value).doubleValue();
    
    // Get quantity (default to 1)
    Object quantityValue = getComponentValue("quantity");
    Double quantity = quantityValue != null ? 
                      ((Number) quantityValue).doubleValue() : 1.0;
    
    // Calculate and update fields
    Double subtotal = price * quantity;
    setComponentValue("subtotal", subtotal);
    
    Double tax = subtotal * 0.08;
    setComponentValue("tax", tax);
    
    Double total = subtotal + tax;
    setComponentValue("total", total);
    
    LOGGER.info("Calculated total: {} (price: {}, qty: {})", total, price, quantity);
}

protected void on_quantity_change(Component component, Object value) {
    // Trigger price change to recalculate all totals
    Object priceValue = getComponentValue("price");
    if (priceValue != null) {
        on_price_change(getComponentByName("price"), priceValue);
    }
}
```

## Pattern 4: Conditional Field Visibility

Show/hide fields based on other field values:

```java
protected void on_orderType_change(Component component, Object value) {
    LOGGER.info("Order type changed to: {}", value);
    
    if ("PICKUP".equals(value)) {
        // Hide shipping fields for pickup orders
        hideField("shippingAddress");
        hideField("shippingMethod");
        hideField("shippingCost");
        
        // Clear shipping values
        setComponentValue("shippingAddress", null);
        setComponentValue("shippingMethod", null);
        setComponentValue("shippingCost", 0.0);
    } else {
        // Show shipping fields for delivery
        showField("shippingAddress");
        showField("shippingMethod");
        showField("shippingCost");
    }
}

private void hideField(String fieldName) {
    Component component = getComponentByName(fieldName);
    if (component != null) {
        component.setVisible(false);
    }
}

private void showField(String fieldName) {
    Component component = getComponentByName(fieldName);
    if (component != null) {
        component.setVisible(true);
    }
}
```

## Pattern 5: Auto-save Functionality

Save automatically when field loses focus:

```java
protected void on_notes_blur(Component component) {
    Object notesValue = getComponentValue("notes");
    
    if (notesValue != null && !notesValue.toString().trim().isEmpty()) {
        try {
            actionSave();
            LOGGER.info("Auto-saved notes");
            CNotificationService.showInfo("Notes saved automatically");
        } catch (Exception e) {
            LOGGER.error("Auto-save failed: {}", e.getMessage());
            CNotificationService.showWarning("Auto-save failed - please save manually");
        }
    }
}
```

## Pattern 6: User Action Tracking

Track user interactions for analytics or UX improvements:

```java
protected void on_price_focus(Component component) {
    LOGGER.info("User focused on price field");
    
    // Show contextual help
    CNotificationService.showInfo("Enter price excluding tax");
    
    // In a real implementation:
    // - Track analytics
    // - Load price history
    // - Display pricing guidelines
}

protected void on_status_change(Component component, Object value) {
    Object oldStatus = getCurrentEntity() != null ? 
                       getCurrentEntity().getStatus() : null;
    
    LOGGER.info("Status changing from {} to {}", oldStatus, value);
    
    // Audit logging
    auditService.logStatusChange(
        getCurrentEntity().getId(),
        oldStatus,
        value,
        sessionService.getCurrentUser()
    );
    
    // Notify stakeholders
    if ("COMPLETED".equals(value)) {
        notificationService.notifyCompletion(getCurrentEntity());
    }
}
```

## Pattern 7: Multi-Field Validation

Validate based on multiple field values:

```java
protected void on_estimatedCost_blur(Component component) {
    Object costValue = getComponentValue("estimatedCost");
    Object budgetValue = getComponentValue("budget");
    
    if (costValue != null && budgetValue != null) {
        Double cost = ((Number) costValue).doubleValue();
        Double budget = ((Number) budgetValue).doubleValue();
        
        if (cost > budget) {
            CNotificationService.showWarning(
                String.format("Estimated cost ($%.2f) exceeds budget ($%.2f)", 
                             cost, budget)
            );
        } else {
            Double remaining = budget - cost;
            setComponentValue("remainingBudget", remaining);
            LOGGER.info("Budget validated: ${} remaining", remaining);
        }
    }
}
```

## Pattern 8: Dynamic Data Loading

Load data based on field selection:

```java
protected void on_project_change(Component component, Object value) {
    if (value == null) {
        setComponentValue("team", null);
        setComponentValue("manager", null);
        return;
    }
    
    CProject project = (CProject) value;
    
    // Auto-populate related fields
    setComponentValue("team", project.getTeam());
    setComponentValue("manager", project.getManager());
    
    // Load project-specific data
    ComboBox<CMilestone> milestoneCombo = getComboBox("milestone");
    if (milestoneCombo != null) {
        List<CMilestone> milestones = milestoneService.getByProject(project);
        milestoneCombo.setItems(milestones);
    }
}
```

## Pattern 9: Field Transformation

Transform input automatically:

```java
protected void on_username_blur(Component component) {
    Object usernameValue = getComponentValue("username");
    
    if (usernameValue != null) {
        String username = usernameValue.toString().toLowerCase().trim();
        setComponentValue("username", username);
        LOGGER.info("Normalized username to: {}", username);
    }
}

protected void on_phone_change(Component component, Object value) {
    if (value == null) return;
    
    String phone = value.toString().replaceAll("[^0-9]", "");
    
    // Format: (555) 123-4567
    if (phone.length() >= 10) {
        String formatted = String.format("(%s) %s-%s",
            phone.substring(0, 3),
            phone.substring(3, 6),
            phone.substring(6)
        );
        setComponentValue("phone", formatted);
    }
}
```

## Pattern 10: Progressive Disclosure

Show additional fields based on complexity:

```java
protected void on_taskComplexity_change(Component component, Object value) {
    if ("SIMPLE".equals(value)) {
        // Hide advanced fields
        hideField("subtasks");
        hideField("dependencies");
        hideField("resourceAllocation");
    } else if ("COMPLEX".equals(value)) {
        // Show all fields
        showField("subtasks");
        showField("dependencies");
        showField("resourceAllocation");
        
        // Provide defaults
        if (getComponentValue("resourceAllocation") == null) {
            setComponentValue("resourceAllocation", "HIGH");
        }
    }
}
```

## Best Practices

1. **Keep callbacks focused**: Each callback should handle one responsibility
2. **Use descriptive logging**: Help debug issues in production
3. **Handle null values**: Always check for null before processing
4. **Provide user feedback**: Use notifications for important actions
5. **Validate early**: Catch errors before save operations
6. **Consider performance**: Avoid heavy computations in rapid-fire events
7. **Test edge cases**: Empty values, boundary values, concurrent changes
8. **Document business logic**: Explain why validations and calculations exist

## Common Pitfalls to Avoid

1. **Circular dependencies**: `on_a_change` modifying `b` which triggers `on_b_change` modifying `a`
2. **Missing null checks**: Always verify component and value existence
3. **Type assumptions**: Cast values safely with proper type checking
4. **Synchronization issues**: Be aware of concurrent modifications
5. **Performance degradation**: Avoid database calls in change handlers
6. **Silent failures**: Always log errors and inform users

## Testing Recommendations

1. Test with valid and invalid inputs
2. Test field interdependencies
3. Test with null and empty values
4. Test rapid changes (typing fast, clicking multiple times)
5. Test with different user roles/permissions
6. Test auto-save and validation timing
7. Monitor logs during testing
