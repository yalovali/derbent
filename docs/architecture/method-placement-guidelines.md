# Method Placement Guidelines

## Overview

This document defines strict rules for where methods should be defined in the Derbent codebase. Following these guidelines prevents code duplication, improves maintainability, and ensures consistent architecture.

**Target Audience**: Developers, AI agents (GitHub Copilot, Codex), code reviewers

**Related Documentation**:
- [Coding Standards](coding-standards.md)
- [Component Coding Standards](../development/component-coding-standards.md)
- [Component and Utility Reference](component-utility-reference.md)

## Core Principle: Check Before Creating

**MANDATORY RULE**: Before creating any new method, you MUST:

1. **Search Existing Code**: Check if similar functionality already exists
2. **Consult Reference**: Check [Component and Utility Reference](component-utility-reference.md)
3. **Verify Placement**: Ensure the method belongs in the class you're adding it to
4. **Ask "Why Here?"**: If you can't explain why the method belongs in this specific class, it doesn't belong there

## Method Placement Decision Tree

```
Is this method specific to THIS class's core responsibility?
├─ YES → Define it in this class
└─ NO → Where does it belong?
    ├─ Color manipulation? → CColorUtils
    ├─ UI component creation? → Appropriate CLabelEntity, CComponentXxx
    ├─ Entity display/formatting? → CLabelEntity
    ├─ User display/avatar? → CLabelEntity.createUserLabel()
    ├─ Icon operations? → CColorUtils
    ├─ Validation? → Check utility class
    ├─ General utility? → CAuxillaries
    ├─ Image processing? → CImageUtils
    └─ Entity queries? → Appropriate service class
```

## Forbidden Patterns

### ❌ FORBIDDEN: Misplaced Utility Methods

**Problem**: Creating utility methods in component classes that don't relate to the component's core responsibility.

```java
// ❌ WRONG - In CComponentKanbanPostit.java
public class CComponentKanbanPostit extends CComponentWidgetEntity<CSprintItem> {
    
    // ❌ WRONG: User label creation doesn't belong in a Kanban postit component
    private Span createCompactUserLabel(final CUser user) {
        if (user == null) {
            return new Span();  // Also wrong: using raw Span
        }
        String displayName = user.getName();
        if (displayName != null && displayName.length() > 15) {
            displayName = displayName.substring(0, 12) + "...";
        }
        final Span userSpan = new Span("👤 " + displayName);  // Also wrong: raw Span
        userSpan.getStyle()
            .set("font-size", "11px")
            .set("color", "#666");
        return userSpan;
    }
}
```

**Why Wrong**:
1. User label creation is not the responsibility of a Kanban postit component
2. This functionality would be duplicated if other components need user labels
3. Uses raw Vaadin `Span` instead of `CSpan`
4. Label styling logic should be centralized

**✅ CORRECT Solution**:

```java
// ✅ CORRECT - In CComponentKanbanPostit.java
public class CComponentKanbanPostit extends CComponentWidgetEntity<CSprintItem> {
    
    @Override
    protected void createSecondLine() throws Exception {
        final ISprintableItem item = resolveSprintableItem();
        // ...
        
        // Use existing CLabelEntity functionality
        if (item.getResponsible() != null) {
            final CLabelEntity userLabel = CLabelEntity.createUserLabel(item.getResponsible());
            userLabel.getStyle().set("font-size", "11px");  // Only style customization here
            layoutLineThree.add(userLabel);
        }
    }
}

// ✅ CORRECT - If compact user label is needed, add to CLabelEntity.java
public class CLabelEntity extends Div {
    
    /** Creates a compact user label with abbreviated name for space-constrained displays.
     * @param user the user to display (can be null)
     * @return a CLabelEntity with compact user display */
    public static CLabelEntity createCompactUserLabel(final CUser user) {
        final CLabelEntity label = new CLabelEntity();
        if (user == null) {
            label.setText("No user");
            label.getStyle().set("color", "#666").set("font-style", "italic");
            return label;
        }
        
        label.add(createUserAvatar(user, "16px"));
        
        String displayName = user.getName();
        if (displayName != null && displayName.length() > 15) {
            displayName = displayName.substring(0, 12) + "...";
        }
        
        final CSpan nameSpan = new CSpan("👤 " + displayName);
        nameSpan.getStyle()
            .set("font-size", "11px")
            .set("color", "#666");
        label.add(nameSpan);
        
        return label;
    }
}
```

### ❌ FORBIDDEN: Duplicated Color Utility Methods

```java
// ❌ WRONG - In some random view class
public class CActivityView extends CAbstractPage {
    
    // ❌ WRONG: Color utilities belong in CColorUtils
    private String getContrastColor(String backgroundColor) {
        // Color calculation logic...
    }
    
    // ❌ WRONG: Color conversion belongs in CColorUtils
    private String rgbToHex(int r, int g, int b) {
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
```

**✅ CORRECT**: Use existing `CColorUtils` methods or add new ones there:

```java
// ✅ CORRECT - Use existing utility
String textColor = CColorUtils.getContrastTextColor(backgroundColor);

// ✅ CORRECT - If new color utility needed, add to CColorUtils.java
public final class CColorUtils {
    
    public static String rgbToHex(final int r, final int g, final int b) {
        Check.isTrue(r >= 0 && r <= 255, "Red value must be 0-255");
        Check.isTrue(g >= 0 && g <= 255, "Green value must be 0-255");
        Check.isTrue(b >= 0 && b <= 255, "Blue value must be 0-255");
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
```

### ❌ FORBIDDEN: Entity Display Logic in Wrong Classes

```java
// ❌ WRONG - In CLabelEntity.java
public class CLabelEntity extends Div {
    
    // ❌ WRONG: General entity color extraction belongs in CColorUtils
    private static String getColorForEntity(final CEntityDB<?> entity) {
        if (entity instanceof IHasColor) {
            return ((IHasColor) entity).getColor();
        }
        return "";
    }
}
```

**Why Wrong**: `CLabelEntity` is for creating visual labels, not for extracting entity properties. Property extraction should be in utility classes.

**✅ CORRECT**:

```java
// ✅ CORRECT - In CColorUtils.java (already exists)
public final class CColorUtils {
    
    public static String getColorFromEntity(final CEntity<?> entity) throws Exception {
        Check.notNull(entity, "entity cannot be null");
        if (entity instanceof CTypeEntity) {
            return ((CTypeEntity<?>) entity).getColor();
        } else if (entity instanceof CStatus) {
            return ((CStatus<?>) entity).getColor();
        }
        // ... more logic
    }
}

// ✅ CORRECT - In CLabelEntity.java, use the utility
public class CLabelEntity extends Div {
    
    public void setValue(final CEntityDB<?> entity, final boolean showIconColor) throws Exception {
        // ...
        if (entity instanceof IHasColor && showIconColor) {
            final String color = CColorUtils.getColorFromEntity(entity);  // Use utility
            getStyle().set("background-color", color);
        }
    }
}
```

## Method Placement Rules by Category

### 1. Color and Icon Operations

**Where**: `CColorUtils` utility class

**Methods Include**:
- Color calculation/conversion
- Brightness calculation
- Contrast color determination
- Icon creation with styling
- Static icon/color extraction from entities

**Examples**:
```java
CColorUtils.getContrastTextColor(backgroundColor)
CColorUtils.getColorFromEntity(entity)
CColorUtils.createStyledIcon(iconString)
CColorUtils.getIconForEntity(entity)
```

### 2. Entity Label Display

**Where**: `CLabelEntity` component class

**Methods Include**:
- Creating labels for entities
- Creating user labels/avatars
- Creating date labels
- Creating header labels (H2, H3)
- Any visual component that displays entity information

**Examples**:
```java
CLabelEntity.createLabel(entity)
CLabelEntity.createUserLabel(user)
CLabelEntity.createDateLabel(date)
CLabelEntity.createH2Label(entity)
```

### 3. Validation and Checking

**Where**: `Check` utility class

**Methods Include**:
- Null checking
- String blank checking
- Collection emptiness checking
- Type checking
- Range validation

**Examples**:
```java
Check.notNull(entity, "Entity cannot be null")
Check.notBlank(name, "Name cannot be blank")
Check.instanceOf(obj, CEntityNamed.class, "Must be CEntityNamed")
```

### 4. Component Creation

**Where**: Specific component classes (`CButton`, `CSpan`, `CDiv`, etc.)

**Pattern**: Use factory methods in the component class or instantiate directly

**Examples**:
```java
// Simple instantiation
final CButton button = new CButton("Save");
final CSpan span = new CSpan("Text");
final CDiv container = new CDiv();

// Factory methods for complex components
final CLabelEntity label = CLabelEntity.createUserLabel(user);
```

### 5. Business Logic

**Where**: Service classes (`CActivityService`, `CUserService`, etc.)

**Methods Include**:
- CRUD operations
- Business rule validation
- Entity state transitions
- Complex queries
- Transaction management

**Examples**:
```java
activityService.save(activity)
activityService.moveItemUp(item)
activityService.findByProject(project)
```

### 6. Component-Specific Behavior

**Where**: The component class itself

**Only Include**:
- Methods directly related to THIS component's display/interaction
- Event handlers for THIS component
- Layout construction for THIS component
- State management for THIS component

**Examples**:
```java
// ✅ CORRECT - In CComponentKanbanPostit.java
protected void createFirstLine() throws Exception {
    // Build THIS postit's first line - this belongs here
}

protected void on_component_click() {
    // Handle clicks on THIS postit - this belongs here
}

public ISprintableItem resolveSprintableItem() {
    // Resolve THIS postit's item - this belongs here
}
```

## Component Usage Rules

### MANDATORY: Use C-Prefixed Components

**Rule**: NEVER use raw Vaadin components directly. ALWAYS use C-prefixed wrappers.

| ❌ Forbidden | ✅ Use Instead |
|--------------|----------------|
| `new Span()` | `new CSpan()` |
| `new Div()` | `new CDiv()` |
| `new Button()` | `new CButton()` |
| `new VerticalLayout()` | `new CVerticalLayout()` |
| `new HorizontalLayout()` | `new CHorizontalLayout()` |
| `new Dialog()` | `new CDialog()` |

**Why**: C-prefixed components provide:
- Consistent styling
- Framework-specific behavior
- Easier maintenance
- Better type safety

### ❌ WRONG Examples

```java
// ❌ WRONG - Raw Vaadin components
final Span userSpan = new Span("👤 " + displayName);
final Div container = new Div();
final Button saveButton = new Button("Save");
final VerticalLayout layout = new VerticalLayout();
```

### ✅ CORRECT Examples

```java
// ✅ CORRECT - C-prefixed components
final CSpan userSpan = new CSpan("👤 " + displayName);
final CDiv container = new CDiv();
final CButton saveButton = new CButton("Save");
final CVerticalLayout layout = new CVerticalLayout();
```

## Before Creating a New Method Checklist

Use this checklist EVERY time before creating a new method:

1. ☐ **Search Phase**
   - [ ] Searched existing codebase for similar functionality
   - [ ] Checked [Component and Utility Reference](component-utility-reference.md)
   - [ ] Verified no existing method does this

2. ☐ **Classification Phase**
   - [ ] Identified what category this method falls into (color, validation, display, etc.)
   - [ ] Identified the correct utility class or component for this method
   - [ ] Confirmed this method relates to the class's core responsibility

3. ☐ **Implementation Phase**
   - [ ] If adding to utility class: Made method `public static`
   - [ ] If adding to component: Made method instance method
   - [ ] Used C-prefixed components instead of raw Vaadin
   - [ ] Added proper JavaDoc documentation
   - [ ] Added null checks using `Check` utility

4. ☐ **Validation Phase**
   - [ ] Method has clear, single responsibility
   - [ ] Method name follows naming conventions
   - [ ] Method is in the correct class
   - [ ] No code duplication

## Code Review Checklist for Method Placement

When reviewing code, check for these violations:

- [ ] ❌ Utility methods in component classes
- [ ] ❌ Color/icon operations outside CColorUtils
- [ ] ❌ Label creation logic outside CLabelEntity
- [ ] ❌ Validation logic outside Check utility
- [ ] ❌ Raw Vaadin components (`new Span()`, `new Div()`)
- [ ] ❌ Duplicated utility methods across classes
- [ ] ❌ Methods that don't relate to the class's purpose

## Examples of Correct Method Placement

### Example 1: Adding User Avatar Support

**Scenario**: Need to display user avatars in multiple places

**❌ WRONG Approach**:
```java
// DON'T create avatar methods in every component that needs them
public class CComponentKanbanPostit {
    private Avatar createAvatar(CUser user) { /* ... */ }
}

public class CActivityView {
    private Avatar createAvatar(CUser user) { /* ... */ }  // Duplication!
}
```

**✅ CORRECT Approach**:
```java
// DO create centralized avatar creation in CLabelEntity
public class CLabelEntity extends Div {
    protected static Avatar createUserAvatar(final CUser user) {
        return createUserAvatar(user, "24px");
    }
    
    protected static Avatar createUserAvatar(final CUser user, final String size) {
        final Avatar avatar = user.getAvatar();
        avatar.setWidth(size);
        avatar.setHeight(size);
        avatar.getStyle().set("flex-shrink", "0");
        return avatar;
    }
}

// Use it everywhere
public class CComponentKanbanPostit {
    protected void createSecondLine() {
        label.add(CLabelEntity.createUserAvatar(user, "16px"));
    }
}
```

### Example 2: Adding Color Manipulation

**Scenario**: Need to lighten/darken colors for UI effects

**❌ WRONG Approach**:
```java
// DON'T add color manipulation to view classes
public class CKanbanBoard {
    private String lightenColor(String hex, double factor) { /* ... */ }
}
```

**✅ CORRECT Approach**:
```java
// DO add color manipulation to CColorUtils
public final class CColorUtils {
    public static String lightenColor(final String hex, final double factor) {
        Check.notBlank(hex, "Hex color cannot be blank");
        Check.isTrue(factor >= 0 && factor <= 1, "Factor must be 0-1");
        // Implementation
    }
}

// Use it in views
public class CKanbanBoard {
    String lightBackground = CColorUtils.lightenColor(baseColor, 0.3);
}
```

## Refactoring Existing Violations

If you find methods in the wrong place:

1. **Identify the correct location** using the decision tree
2. **Move the method** to the appropriate class
3. **Make it public/static** if it's a utility method
4. **Update all call sites** to use the new location
5. **Add JavaDoc** explaining the method's purpose
6. **Verify with tests** that functionality didn't break

## AI Agent Instructions

When generating code, AI agents MUST:

1. **Before creating ANY method**: Check if similar functionality exists in:
   - CColorUtils (colors, icons, contrast)
   - CLabelEntity (entity display, labels)
   - Check (validation)
   - CAuxillaries (general utilities)
   - CImageUtils (image operations)

2. **If creating new utility method**: Add to appropriate utility class, NOT to component

3. **If creating component-specific method**: Ensure it's ONLY about THIS component's behavior

4. **Always use C-prefixed components**: Never `new Span()`, always `new CSpan()`

5. **Document new methods**: Add JavaDoc explaining purpose and usage

## Related Documentation

- [Component and Utility Reference](component-utility-reference.md) - Complete index of available classes
- [Coding Standards](coding-standards.md) - General coding standards
- [UI, CSS, and Layout Coding Standards](ui-css-coding-standards.md) - UI-specific guidelines
- [Component Coding Standards](../development/component-coding-standards.md) - Component creation guidelines

---

**Version**: 1.0  
**Last Updated**: 2026-01-03  
**Maintainer**: Derbent Development Team
