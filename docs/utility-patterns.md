# Icon and Auxiliary Utility Patterns

This document covers the utility patterns used throughout the Derbent project, focusing on CAuxillaries usage, icon standards, invoke function patterns, and other common utility practices.

## 🔧 CAuxillaries Utility Class

The `CAuxillaries` class is the central utility hub for the Derbent project, providing reflection-based services, component utilities, and dynamic class resolution.

### Core Purpose
- **Dynamic Class Resolution**: Find entity, service, and view classes by name
- **Reflection Utilities**: Invoke static methods safely
- **Component Management**: ID generation and component utilities
- **Type Safety**: Centralized type checking and validation

### Location
`tech.derbent.api.utils.CAuxillaries`

## 🎯 Class Resolution Patterns

### Entity Class Resolution
```java
// Get entity class dynamically
Class<?> entityClass = CAuxillaries.getEntityClass("CActivity");
Class<?> userClass = CAuxillaries.getEntityClass("CUser");
Class<?> projectClass = CAuxillaries.getEntityClass("CProject");

// Usage in generic methods
public <T> void processEntity(String entityType) {
    Class<T> clazz = (Class<T>) CAuxillaries.getEntityClass(entityType);
    // Process entity type
}
```

### Service Class Resolution
```java
// Get service class for entity
Class<?> serviceClass = CAuxillaries.getServiceClass("CActivity");
// Returns: tech.derbent.activities.service.CActivityService.class

Class<?> initializerClass = CAuxillaries.getInitializerService("CActivity");
// Returns: tech.derbent.activities.service.CActivityInitializerService.class

// Usage in dependency injection scenarios
@Autowired
private ApplicationContext applicationContext;

public Object getServiceForEntity(String entityType) {
    Class<?> serviceClass = CAuxillaries.getServiceClass(entityType);
    return applicationContext.getBean(serviceClass);
}
```

### View Class Resolution
```java
// Get view class for entity
Class<?> viewClass = CAuxillaries.getViewClassForEntity("CActivity");
// Returns: tech.derbent.activities.view.CActivitiesView.class

// Usage in navigation
public void navigateToEntityView(String entityType) {
    Class<?> viewClass = CAuxillaries.getViewClassForEntity(entityType);
    // Navigate to view
}
```

### Adding New Entity Types
When creating new entities, update the CAuxillaries switch statements:

```java
// In CAuxillaries.getEntityClass()
case "CNewEntity":
    return CNewEntity.class;

// In CAuxillaries.getServiceClass()  
case "CNewEntity":
    return CNewEntityService.class;

// In CAuxillaries.getViewClassForEntity()
case "CNewEntity":
    return CNewEntityView.class;
```

## 🔍 Reflection and Method Invocation

### Static Method Invocation
```java
// Invoke static methods safely
String iconFilename = CAuxillaries.invokeStaticMethodOfStr(CActivity.class, "getStaticIconFilename");
String colorCode = CAuxillaries.invokeStaticMethodOfStr(CActivity.class, "getStaticIconColorCode");

// Invoke by class name
String entityColor = CAuxillaries.invokeStaticMethodOfStr("tech.derbent.activities.domain.CActivity", 
    "getStaticEntityColorCode");

// Invoke void methods
CAuxillaries.invokeMethodOfVoid(SomeClass.class, "initializeDefaults");

// Invoke methods returning lists
List<String> items = CAuxillaries.invokeStaticMethodOfList("tech.derbent.utils.ColorUtils", 
    "getAvailableColors");
```

### Error Handling in Reflection
```java
try {
    String result = CAuxillaries.invokeStaticMethodOfStr(entityClass, methodName);
    return result;
} catch (Exception e) {
    LOGGER.error("Failed to invoke method {} on class {}", methodName, entityClass.getName(), e);
    return defaultValue;
}
```

### Dynamic Entity Information
```java
// Get available entity types
List<String> entityTypes = CAuxillaries.getAvailableEntityTypes();
// Returns: ["CActivity", "CMeeting", "CRisk", "CProject", "CUser"]

// Get web colors
List<String> colors = CAuxillaries.getWebColors();
// Returns list of web-safe color codes
```

## 🎨 Icon Usage Patterns

### Static Icon Methods in Entities
Every entity must implement these static methods:

```java
public class CActivity extends CProjectItem<CActivity> {
    
    /**
     * Returns the Vaadin icon name for this entity type.
     * Used in menus, buttons, and UI components.
     */
    public static String getStaticIconFilename() { 
        return "vaadin:tasks"; 
    }
    
    /**
     * Returns the color code for this entity type.
     * Used for theming and visual consistency.
     */
    public static String getStaticIconColorCode() { 
        return "#007bff"; 
    }
    
    /**
     * Returns the entity color (usually same as icon color).
     * Used for status indicators and entity identification.
     */
    public static String getStaticEntityColorCode() { 
        return getStaticIconColorCode(); 
    }
}
```

### Icon Naming Standards
Use descriptive Vaadin icon names:

```java
// Entity type icons
"vaadin:tasks"          // Activities, tasks, work items
"vaadin:users"          // Users, people, team members  
"vaadin:folder"         // Projects, containers, groups
"vaadin:calendar"       // Meetings, events, schedules
"vaadin:warning"        // Risks, issues, problems
"vaadin:archive"        // Archives, storage, history
"vaadin:cog"            // Settings, configuration
"vaadin:chart"          // Reports, analytics, charts

// Type and status icons
"vaadin:tag"            // Types, categories, labels
"vaadin:flag"           // Statuses, states, flags
"vaadin:star"           // Priorities, ratings, favorites
"vaadin:circle"         // Simple status indicators
"vaadin:check"          // Completed, verified, approved
"vaadin:clock"          // Time-related, pending, scheduled

// Action icons
"vaadin:plus"           // Add, create, new
"vaadin:edit"           // Edit, modify, update
"vaadin:trash"          // Delete, remove, discard
"vaadin:download"       // Export, download, save
"vaadin:upload"         // Import, upload, load
"vaadin:search"         // Search, find, filter
"vaadin:refresh"        // Refresh, reload, sync
```

### Dynamic Icon Resolution
```java
// Get icon dynamically from entity class
String iconName = CAuxillaries.invokeStaticMethodOfStr(entityClass, "getStaticIconFilename");
Icon icon = VaadinIcon.valueOf(iconName.replace("vaadin:", "").toUpperCase()).create();

// Usage in buttons
CButton entityButton = new CButton("View " + entityName, icon);

// Usage in menu items (via class reference)
@Menu(order = 1.1, icon = "class:tech.derbent.activities.view.CActivitiesView", title = "Activities")
```

### Color Standards
Use Bootstrap-compatible color schemes:

```java
// Primary brand colors
"#007bff"   // Primary blue - main actions, activities
"#6c757d"   // Secondary gray - inactive, disabled
"#28a745"   // Success green - completed, approved
"#dc3545"   // Danger red - errors, critical items
"#ffc107"   // Warning yellow - pending, attention needed
"#17a2b8"   // Info cyan - information, details

// Extended palette
"#6f42c1"   // Purple - special items, premium features
"#e83e8c"   // Pink - urgent, high priority
"#fd7e14"   // Orange - moderate priority, in progress
"#20c997"   // Teal - success variants, positive indicators
"#6610f2"   // Indigo - deep specialization, advanced features

// Neutral shades
"#343a40"   // Dark gray - text, primary content
"#495057"   // Medium gray - secondary text
"#6c757d"   // Light gray - muted text, placeholders
"#adb5bd"   // Lighter gray - borders, dividers
"#dee2e6"   // Very light gray - backgrounds, subtle borders
"#f8f9fa"   // Off white - page backgrounds, panels
```

## 🧩 Component Utility Patterns

### Component ID Generation
```java
// Automatic ID generation for testing
CAuxillaries.setId(component);

// Manual ID generation (for debugging)
String generatedId = CAuxillaries.generateId(component);
// Returns: "button-save-activity" or "textfield-activity-name"

// Usage patterns
CButton saveButton = new CButton("Save");
CAuxillaries.setId(saveButton);  // Sets ID: "cbutton-save"

TextField nameField = new TextField("Name");  
CAuxillaries.setId(nameField);   // Sets ID: "textfield-name"

ComboBox<CActivityType> typeCombo = new ComboBox<>("Type");
CAuxillaries.setId(typeCombo);   // Sets ID: "combobox-type"
```

### Component Text Extraction
```java
// Get component text for ID generation
String text = CAuxillaries.getComponentText(component);

// Supports various component types:
// - Button: button text
// - TextField: label or placeholder
// - ComboBox: label  
// - Label: text content
// - H1-H6: heading text
```

### Component Type Detection
```java
// Check component types for ID generation
if (component instanceof Button) {
    // Handle button-specific ID logic
} else if (component instanceof TextField) {
    // Handle text field-specific ID logic
} else if (component instanceof ComboBox) {
    // Handle combo box-specific ID logic
}
```

## 🔧 Invoke Function Patterns

### Safe Method Invocation
```java
// Pattern 1: Try-catch with logging
public String getEntityIcon(Class<?> entityClass) {
    try {
        return CAuxillaries.invokeStaticMethodOfStr(entityClass, "getStaticIconFilename");
    } catch (Exception e) {
        LOGGER.warn("Failed to get icon for {}, using default", entityClass.getSimpleName());
        return "vaadin:file"; // Default icon
    }
}

// Pattern 2: With validation
public String getEntityColor(String entityType) {
    Check.notBlank(entityType, "Entity type cannot be blank");
    
    try {
        Class<?> entityClass = CAuxillaries.getEntityClass(entityType);
        return CAuxillaries.invokeStaticMethodOfStr(entityClass, "getStaticIconColorCode");
    } catch (Exception e) {
        LOGGER.error("Failed to get color for entity type: {}", entityType, e);
        return "#6c757d"; // Default gray
    }
}

// Pattern 3: Batch processing
public Map<String, String> getEntityIcons(List<String> entityTypes) {
    Map<String, String> icons = new HashMap<>();
    
    for (String entityType : entityTypes) {
        try {
            Class<?> entityClass = CAuxillaries.getEntityClass(entityType);
            String icon = CAuxillaries.invokeStaticMethodOfStr(entityClass, "getStaticIconFilename");
            icons.put(entityType, icon);
        } catch (Exception e) {
            LOGGER.warn("Failed to get icon for {}", entityType);
            icons.put(entityType, "vaadin:file");
        }
    }
    
    return icons;
}
```

### Method Existence Checking
```java
// Check if method exists before invoking
public boolean hasStaticMethod(Class<?> clazz, String methodName) {
    try {
        Method method = clazz.getMethod(methodName);
        return Modifier.isStatic(method.getModifiers());
    } catch (NoSuchMethodException e) {
        return false;
    }
}

// Usage
if (hasStaticMethod(entityClass, "getStaticIconFilename")) {
    String icon = CAuxillaries.invokeStaticMethodOfStr(entityClass, "getStaticIconFilename");
} else {
    LOGGER.warn("Entity {} does not implement getStaticIconFilename", entityClass.getSimpleName());
}
```

### Parameterized Method Invocation
```java
// For methods with parameters (less common in CAuxillaries)
public Object invokeMethodWithParams(Class<?> clazz, String methodName, Object... params) {
    try {
        Method method = getClazzMethod(clazz, methodName, getParameterTypes(params));
        return method.invoke(null, params);
    } catch (Exception e) {
        LOGGER.error("Failed to invoke method {} with parameters", methodName, e);
        throw new RuntimeException("Method invocation failed", e);
    }
}
```

## 🎪 Advanced Utility Patterns

### Entity Type Enumeration
```java
// Get all available entity types
public enum EntityType {
    ACTIVITY("CActivity"),
    USER("CUser"), 
    PROJECT("CProject"),
    MEETING("CMeeting"),
    RISK("CRisk");
    
    private final String className;
    
    EntityType(String className) {
        this.className = className;
    }
    
    public Class<?> getEntityClass() {
        return CAuxillaries.getEntityClass(className);
    }
    
    public String getIconFilename() {
        return CAuxillaries.invokeStaticMethodOfStr(getEntityClass(), "getStaticIconFilename");
    }
    
    public String getColorCode() {
        return CAuxillaries.invokeStaticMethodOfStr(getEntityClass(), "getStaticIconColorCode");
    }
}
```

### Generic Entity Operations
```java
public class EntityUtils {
    
    /**
     * Creates a button for an entity type with proper icon and color.
     */
    public static CButton createEntityButton(String entityType, String text) {
        try {
            Class<?> entityClass = CAuxillaries.getEntityClass(entityType);
            String iconName = CAuxillaries.invokeStaticMethodOfStr(entityClass, "getStaticIconFilename");
            String colorCode = CAuxillaries.invokeStaticMethodOfStr(entityClass, "getStaticIconColorCode");
            
            Icon icon = VaadinIcon.valueOf(iconName.replace("vaadin:", "").toUpperCase()).create();
            CButton button = new CButton(text, icon);
            button.getStyle().set("color", colorCode);
            
            CAuxillaries.setId(button);
            
            return button;
        } catch (Exception e) {
            LOGGER.warn("Failed to create entity button for {}", entityType, e);
            return new CButton(text);
        }
    }
    
    /**
     * Creates a navigation item for an entity view.
     */
    public static RouterLink createEntityNavigation(String entityType) {
        try {
            Class<?> viewClass = CAuxillaries.getViewClassForEntity(entityType);
            String iconName = CAuxillaries.invokeStaticMethodOfStr(
                CAuxillaries.getEntityClass(entityType), "getStaticIconFilename");
            
            Icon icon = VaadinIcon.valueOf(iconName.replace("vaadin:", "").toUpperCase()).create();
            RouterLink link = new RouterLink();
            link.setRoute(viewClass);
            link.add(icon, new Span(entityType));
            
            return link;
        } catch (Exception e) {
            LOGGER.error("Failed to create navigation for {}", entityType, e);
            return new RouterLink(entityType);
        }
    }
}
```

### Configuration-Driven Utilities
```java
// Entity configuration from properties
@ConfigurationProperties("derbent.entities")
public class EntityConfiguration {
    
    private Map<String, EntityConfig> entities = new HashMap<>();
    
    public static class EntityConfig {
        private String icon;
        private String color;
        private boolean enabled = true;
        private int menuOrder;
        
        // Getters and setters
    }
    
    public String getEntityIcon(String entityType) {
        EntityConfig config = entities.get(entityType);
        if (config != null && config.getIcon() != null) {
            return config.getIcon();
        }
        
        // Fallback to reflection
        try {
            return CAuxillaries.invokeStaticMethodOfStr(
                CAuxillaries.getEntityClass(entityType), "getStaticIconFilename");
        } catch (Exception e) {
            return "vaadin:file";
        }
    }
}
```

## 🚫 Utility Anti-Patterns

### Prohibited Practices
```java
// ❌ Don't hardcode class names
Class<?> activityClass = Class.forName("tech.derbent.activities.domain.CActivity"); // Wrong
Class<?> activityClass = CAuxillaries.getEntityClass("CActivity");                  // Correct

// ❌ Don't ignore exceptions
String icon = CAuxillaries.invokeStaticMethodOfStr(clazz, "getStaticIconFilename"); // Wrong
// Always wrap in try-catch

// ❌ Don't bypass CAuxillaries for component IDs
component.setId("manual-id");           // Wrong
CAuxillaries.setId(component);         // Correct

// ❌ Don't use reflection directly when CAuxillaries provides utilities
Method method = clazz.getMethod("methodName");  // Wrong
String result = CAuxillaries.invokeStaticMethodOfStr(clazz, "methodName");  // Correct
```

### Required Practices
```java
// ✅ Always use CAuxillaries for class resolution
Class<?> entityClass = CAuxillaries.getEntityClass(entityType);

// ✅ Always handle exceptions in reflection calls
try {
    String result = CAuxillaries.invokeStaticMethodOfStr(clazz, methodName);
} catch (Exception e) {
    LOGGER.error("Method invocation failed", e);
    // Handle gracefully
}

// ✅ Always set component IDs for testing
CAuxillaries.setId(component);

// ✅ Use validation before processing
Check.notBlank(entityType, "Entity type cannot be blank");
Check.notNull(clazz, "Class cannot be null");
```

## ✅ Utility Usage Checklist

When using CAuxillaries and utility patterns:

### Required Elements
- [ ] Use CAuxillaries for all class resolution
- [ ] Wrap reflection calls in try-catch blocks
- [ ] Set component IDs using CAuxillaries.setId()
- [ ] Validate parameters before processing
- [ ] Log errors and warnings appropriately
- [ ] Provide fallback values for failed operations

### Best Practices
- [ ] Use consistent icon naming conventions
- [ ] Follow color standards for consistency
- [ ] Document any custom utility methods
- [ ] Test utility methods thoroughly
- [ ] Handle null values gracefully

### Integration Requirements
- [ ] Update CAuxillaries switch statements for new entities
- [ ] Ensure all entities implement required static methods
- [ ] Test component ID generation
- [ ] Verify icon and color resolution
- [ ] Check reflection method availability

This utility system provides a robust foundation for dynamic class resolution, safe reflection operations, and consistent component management throughout the Derbent application.