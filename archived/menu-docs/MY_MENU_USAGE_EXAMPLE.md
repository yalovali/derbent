# @MyMenu Annotation - Usage Guide

**Date**: 2026-02-04  
**Status**: ✅ **IMPLEMENTED AND READY**  

---

## Quick Start

The `@MyMenu` annotation is now available and solves the hierarchical menu ordering bug by preserving order as String!

### Example Usage

```java
package tech.derbent.plm.activities.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import tech.derbent.api.menu.MyMenu;
import tech.derbent.api.ui.view.MainLayout;

@MyMenu(
    title = "Project.Activities.Activity Types",
    order = "1.2.3",  // ← Preserved exactly! No data loss!
    icon = "vaadin:tasks",
    route = "activities/types"
)
@Route(value = "activities/types", layout = MainLayout.class)
@PageTitle("Activity Types")
public class CActivityTypeView extends VerticalLayout {
    // Your view implementation
}
```

---

## How It Works

### Before @MyMenu (BROKEN for 3+ levels)

```java
// Vaadin's @Menu with Double
@Menu(order = 10.23)  // ← Is this "10.2.3" or "10.23"? AMBIGUOUS!
```

**Problem**: Converting "10.2.3" to Double concatenates → 10.23 → information lost!

### After @MyMenu (PERFECT!)

```java
// Our @MyMenu with String
@MyMenu(order = "10.2.3")  // ← EXACT! No conversion, no loss!
```

**Solution**: String is parsed directly → `["10", "2", "3"]` → Perfect preservation!

---

## Order String Format

The `order` field uses dots (`.`) to separate hierarchy levels:

| Order String | Meaning | Result |
|--------------|---------|--------|
| `"5"` | Single level at position 5 | Top menu item 5 |
| `"4.1"` | Parent at 4, child at 1 | "Parent 4" → "Child 1" |
| `"10.20.30"` | Three levels | "Grandparent 10" → "Parent 20" → "Child 30" |
| `"523.123"` | Two levels with large numbers | Works perfectly! |

**Key**: Each number is independent - "10.20.30" means [10, 20, 30], NOT 10.2030!

---

## Complete Annotation Parameters

```java
@MyMenu(
    title = "Project.Activities.Priority Management",  // Hierarchy via dots
    order = "1.2.15",                                   // Order string
    icon = "vaadin:star",                               // Optional icon
    route = "activities/priorities",                    // Optional route
    showInQuickToolbar = false                          // Optional toolbar flag
)
```

### Parameters

| Parameter | Required | Type | Description |
|-----------|----------|------|-------------|
| `title` | ✅ Yes | String | Menu hierarchy with dots (e.g., "Project.Activities.Types") |
| `order` | ✅ Yes | String | **Order string with dots** (e.g., "1.2.3") |
| `icon` | ❌ No | String | Vaadin icon or `class:...` reference |
| `route` | ❌ No | String | Route path (auto-inferred from class name if empty) |
| `showInQuickToolbar` | ❌ No | boolean | Show in quick access toolbar (default: false) |

---

## Real-World Examples

### Example 1: Simple Two-Level Menu

```java
@MyMenu(
    title = "Finance.Invoices",
    order = "10.5",  // Finance=10, Invoices=5
    icon = "vaadin:invoice"
)
@Route(value = "invoices", layout = MainLayout.class)
@PageTitle("Invoices")
public class CInvoiceView extends CAbstractPage {
    // Implementation
}
```

**Result**: Appears under "Finance" menu at position 5

### Example 2: Three-Level Menu with Multi-Digit Positions

```java
@MyMenu(
    title = "Project.Products.Component Types",
    order = "1.20.15",  // Project=1, Products=20, Component Types=15
    icon = "vaadin:package"
)
@Route(value = "products/component-types", layout = MainLayout.class)
@PageTitle("Component Types")
public class CComponentTypeView extends CAbstractPage {
    // Implementation
}
```

**Result**: "Project" → "Products" → "Component Types"  
**Order**: Products appears at position 20, Component Types at position 15 within Products

### Example 3: Using Class Icon Reference

```java
@MyMenu(
    title = "Setup.System.User Management",
    order = "400.10.5",
    icon = "class:tech.derbent.base.users.domain.CUser"  // Use entity's icon
)
@Route(value = "users", layout = MainLayout.class)
@PageTitle("User Management")
public class CUserView extends CAbstractPage {
    // Implementation
}
```

---

## Comparison: @Menu vs @MyMenu

### Using Vaadin @Menu (BROKEN for 3+ levels)

```java
@Menu(
    title = "Project.Activities.Types",
    order = 1.23  // ← WRONG! Becomes 1.23 (ambiguous)
)
```

**Problem**: Order 1.23 could mean:
- "1.2.3" (parent=1, child1=2, child2=3)?
- "1.23" (parent=1, child=23)?
- "1.2.30" (parent=1, child1=2, child2=30)?

### Using @MyMenu (PERFECT!)

```java
@MyMenu(
    title = "Project.Activities.Types",
    order = "1.2.3"  // ← PERFECT! Exact meaning
)
```

**Solution**: Order "1.2.3" means exactly [1, 2, 3] - no ambiguity!

---

## Migration from @Menu to @MyMenu

### Step 1: Identify Menu Order

If your view currently uses `@Menu`:

```java
@Menu(order = 5.43)  // What does this mean?
```

Check the documentation or initializer to find original intent:
- Was it "5.4.3"? (3 levels)
- Was it "5.43"? (2 levels)

### Step 2: Replace Annotation

```java
// BEFORE
@Menu(order = 5.43)
public class CMyView extends CAbstractPage { }

// AFTER
@MyMenu(
    title = "Project.Activities.MyEntity",  // Add hierarchy
    order = "5.4.3"                          // Use String!
)
public class CMyView extends CAbstractPage { }
```

### Step 3: Test Menu Ordering

1. Start application
2. Navigate to menu
3. Verify items appear in correct order

---

## Benefits Summary

| Aspect | @Menu (Double) | @MyMenu (String) |
|--------|---------------|------------------|
| **Data Loss** | ❌ 3+ levels | ✅ Zero loss |
| **Max Levels** | ⚠️ 2 reliable | ✅ Unlimited |
| **Multi-digit** | ❌ Breaks | ✅ Works |
| **Ambiguity** | ❌ Yes | ✅ No |
| **Human Readable** | ❌ No | ✅ Yes |
| **Parsing** | ❌ Complex | ✅ Simple |

---

## Technical Details

### How @MyMenu Works

1. **Annotation Scanning**: `MyMenuConfiguration` scans classpath at startup
2. **Parsing**: Order string "10.20.30" → int array [10, 20, 30]
3. **No Conversion**: String stays as String until needed
4. **Menu Building**: `CHierarchicalSideMenu` processes MyMenuEntry directly
5. **Sorting**: Items sorted by int array comparison (level by level)

### Classes Involved

| Class | Purpose |
|-------|---------|
| `@MyMenu` | Annotation definition |
| `MyMenuEntry` | Holds parsed order array |
| `MyMenuConfiguration` | Scans and registers annotations |
| `CHierarchicalSideMenu` | Processes both @Menu and @MyMenu |

---

## Coexistence with @Menu

Both `@Menu` and `@MyMenu` work together:

```java
// Old views keep using @Menu
@Menu(order = 5.1)  // Works for 2 levels
public class CSimpleView extends CAbstractPage { }

// New views use @MyMenu
@MyMenu(order = "10.20.30")  // Works for any levels!
public class CComplexView extends CAbstractPage { }
```

**Result**: Both appear in menu correctly!

---

## FAQ

### Q: Do I need to migrate all @Menu annotations?

**A**: No, only views with 3+ level menus or multi-digit positions need @MyMenu.

### Q: Can I use @MyMenu for 2-level menus?

**A**: Yes! It works for all cases: 1-level, 2-level, 3+ levels.

### Q: What if I don't specify a route?

**A**: Route is inferred from class name:
- `CActivityView` → `"activity"`
- `CActivityTypeView` → `"activitytype"`

### Q: Will this break existing menus?

**A**: No! @MyMenu works alongside @Menu. Both are processed.

---

## Next Steps

1. ✅ Start using `@MyMenu` for new views
2. ✅ Migrate views with 3+ level menus
3. ✅ Gradually migrate all views for consistency
4. ✅ Document your menu orders in code

**The hierarchical menu ordering bug is now solved!** 🎯✨
