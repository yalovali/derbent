# Derbent Menu System - Complete Guide

**Date**: 2026-02-04  
**Status**: ✅ OPERATIONAL - @MyMenu annotation system with String-based ordering  
**Version**: 2.0 (Consolidated from all menu documentation)

---

## Table of Contents

1. [Quick Start](#quick-start)
2. [Menu Ordering Format](#menu-ordering-format)
3. [Using @MyMenu Annotation](#using-mymenu-annotation)
4. [Quick Toolbar Integration](#quick-toolbar-integration)
5. [Database-Driven Menus](#database-driven-menus)
6. [Architecture Overview](#architecture-overview)
7. [Troubleshooting](#troubleshooting)

---

## Quick Start

### Basic Menu Item

```java
import tech.derbent.api.menu.MyMenu;

@MyMenu(order = "10", icon = "vaadin:file", title = "My Page")
@Route("mypage")
public class MyView extends CAbstractPage {
}
```

### Nested Menu (Parent → Child)

```java
@MyMenu(order = "10.5", icon = "vaadin:folder", title = "Parent.Child Page")
@Route("childpage")
public class ChildView extends CAbstractPage {
}
```

### Deep Hierarchy (3+ Levels)

```java
@MyMenu(order = "10.20.30", icon = "vaadin:file-tree", title = "Level1.Level2.Level3")
@Route("deeppage")
public class DeepView extends CAbstractPage {
}
```

### Quick Toolbar Button

```java
@MyMenu(
    order = "10.5", 
    icon = "vaadin:star", 
    title = "Favorites.Important",
    showInQuickToolbar = true  // ← Shows in top toolbar
)
@Route("important")
public class ImportantView extends CAbstractPage {
}
```

---

## Menu Ordering Format

### String-Based Ordering (NO Data Loss)

| Order String | Parsed As | Display Position |
|--------------|-----------|------------------|
| `"10"` | `[10]` | Top-level item #10 |
| `"10.5"` | `[10, 5]` | Child #5 under parent #10 |
| `"10.5.2"` | `[10, 5, 2]` | Grandchild #2 under child #5 under parent #10 |
| `"100.190.250"` | `[100, 190, 250]` | Multi-digit support! |

### Key Rules

1. **Separator**: Use `.` (dot) to separate hierarchy levels
2. **Format**: Each number represents position at that level
3. **Multi-Digit**: Supports `100`, `190`, `250` (not limited to single digits)
4. **No Limit**: Unlimited hierarchy depth (not restricted by double precision)
5. **Precision**: 100% data preservation (String → int[] array)

### Order String Examples

```
Order String      Hierarchy
"5"              → Menu item #5 (top level)
"5.4"            → Child #4 under parent #5
"5.4.3"          → Grandchild #3 under child #4 under parent #5
"523.123"        → Multi-digit parent 523, child 123
"10.20.30.40"    → 4-level deep hierarchy
```

### Important: NOT Double!

**❌ OLD SYSTEM (BROKEN):**
```java
@Menu(order = 5.43)  // Is this "5.4.3" or "5.43"? AMBIGUOUS!
```

**✅ NEW SYSTEM (CORRECT):**
```java
@MyMenu(order = "5.4.3")  // Unambiguous string → [5, 4, 3]
```

---

## Using @MyMenu Annotation

### Full Annotation Syntax

```java
@MyMenu(
    order = "10.5",              // REQUIRED: Menu position (String!)
    icon = "vaadin:file",        // REQUIRED: Vaadin icon name
    title = "Parent.Child",      // REQUIRED: Menu path with dots
    showInQuickToolbar = false   // OPTIONAL: Show in top toolbar (default: false)
)
```

### Annotation Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `order` | `String` | ✅ YES | Menu position. Format: "10" or "10.5" or "10.5.2" |
| `icon` | `String` | ✅ YES | Vaadin icon name (e.g., "vaadin:file") |
| `title` | `String` | ✅ YES | Menu path with dots (e.g., "Parent.Child") |
| `showInQuickToolbar` | `boolean` | ❌ NO | Show in top toolbar (default: false) |

### Menu Title Format

The `title` field defines the menu hierarchy using dots:

```java
// Single level
@MyMenu(title = "Activities")  // → Appears at root level

// Two levels (Parent → Child)
@MyMenu(title = "Project.Activities")  // → Creates "Project" parent with "Activities" child

// Three levels (Parent → Child → Grandchild)
@MyMenu(title = "Project.Management.Activities")  // → Multi-level hierarchy
```

### Icon Format

Use Vaadin icon names:

```java
@MyMenu(icon = "vaadin:file")      // File icon
@MyMenu(icon = "vaadin:folder")    // Folder icon
@MyMenu(icon = "vaadin:cog")       // Settings icon
@MyMenu(icon = "vaadin:users")     // Users icon
```

For entity icons, use class reference:

```java
@MyMenu(icon = "class:tech.derbent.plm.activities.domain.CActivity")
```

### Complete Examples

#### 1. Top-Level Menu Item

```java
@MyMenu(order = "10", icon = "vaadin:home", title = "Dashboard")
@Route("dashboard")
public class DashboardView extends CAbstractPage {
    // Implementation
}
```

#### 2. Parent with Children

```java
// Parent navigation item created automatically
@MyMenu(order = "20.10", icon = "vaadin:folder", title = "Reports.Sales")
@Route("reports/sales")
public class SalesReportView extends CAbstractPage {
}

@MyMenu(order = "20.20", icon = "vaadin:folder", title = "Reports.Inventory")
@Route("reports/inventory")
public class InventoryReportView extends CAbstractPage {
}
```

#### 3. Deep Hierarchy (3 Levels)

```java
@MyMenu(
    order = "30.10.5", 
    icon = "vaadin:file", 
    title = "Project.Activities.High Priority"
)
@Route("project/activities/high")
public class HighPriorityActivitiesView extends CAbstractPage {
}
```

#### 4. Quick Toolbar Item

```java
@MyMenu(
    order = "40", 
    icon = "vaadin:star", 
    title = "Favorites",
    showInQuickToolbar = true  // ← Appears in top toolbar
)
@Route("favorites")
public class FavoritesView extends CAbstractPage {
}
```

---

## Quick Toolbar Integration

### How It Works

1. **Selection**: Only items with `showInQuickToolbar = true` appear in toolbar
2. **Ordering**: Toolbar buttons are **sorted by menu order string** (same as side menu)
3. **Position**: Buttons appear left-to-right in order value

### Order String Parsing for Toolbar

```java
// Order string → int[] array → compare level by level
"10"       → [10]
"10.5"     → [10, 5]
"10.5.2"   → [10, 5, 2]

// Comparison examples:
"5" < "10"           // [5] < [10]
"10.5" < "10.20"     // [10,5] < [10,20] (compare second level)
"10.5.2" < "10.5.10" // [10,5,2] < [10,5,10] (compare third level)
```

### Toolbar Button Creation

```java
// Example: Multiple quick toolbar items with proper ordering
@MyMenu(order = "10", icon = "vaadin:home", title = "Dashboard", 
        showInQuickToolbar = true)  // First button

@MyMenu(order = "20", icon = "vaadin:calendar", title = "Calendar", 
        showInQuickToolbar = true)  // Second button

@MyMenu(order = "20.5", icon = "vaadin:tasks", title = "Project.Tasks", 
        showInQuickToolbar = true)  // Third button (child of "20")

@MyMenu(order = "30", icon = "vaadin:users", title = "Users", 
        showInQuickToolbar = true)  // Fourth button
```

**Result**: Toolbar shows: `[Dashboard] [Calendar] [Tasks] [Users]`

### Implementation Details

**CViewToolbar.java** - Lines 360-395:

```java
private Component createQuickToolbar() {
    final List<ToolbarItem> toolbarItems = new ArrayList<>();
    
    // Collect @MyMenu items marked for quick toolbar
    final List<MyMenuEntry> myMenuEntries = 
        pageMenuIntegrationService.getMyMenuEntriesForQuickToolbar();
    for (final MyMenuEntry entry : myMenuEntries) {
        final CButton button = createMyMenuButton(entry);
        toolbarItems.add(new ToolbarItem(entry.getOrderString(), button));
    }
    
    // Collect database pages marked for quick toolbar
    final List<CPageEntity> quickToolbarPages = 
        pageMenuIntegrationService.getQuickToolbarPages();
    for (final CPageEntity page : quickToolbarPages) {
        final CButton pageButton = createDynamicPageButton(page);
        final String orderString = String.valueOf(page.getMenuOrder());
        toolbarItems.add(new ToolbarItem(orderString, pageButton));
    }
    
    // Sort toolbar items by menu order (same algorithm as side menu)
    toolbarItems.sort((a, b) -> compareMenuOrder(a.order, b.order));
    
    // Extract buttons in sorted order
    final List<CButton> sortedButtons = toolbarItems.stream()
        .map(item -> item.button)
        .toList();
    
    return new CDiv(sortedButtons.toArray(new Component[0]));
}
```

### Order Comparison Algorithm

**CViewToolbar.java** - Lines 414-450:

```java
private int compareMenuOrder(final String order1, final String order2) {
    final int[] parts1 = parseOrderString(order1);
    final int[] parts2 = parseOrderString(order2);
    
    // Compare level by level
    final int minLength = Math.min(parts1.length, parts2.length);
    for (int i = 0; i < minLength; i++) {
        if (parts1[i] != parts2[i]) {
            return Integer.compare(parts1[i], parts2[i]);
        }
    }
    
    // If all compared levels are equal, shorter path comes first
    return Integer.compare(parts1.length, parts2.length);
}

private int[] parseOrderString(final String orderString) {
    if (orderString == null || orderString.trim().isEmpty()) {
        return new int[] { 999 }; // Default to end
    }
    
    try {
        final String[] parts = orderString.trim().split("\\.");
        final int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
    } catch (final NumberFormatException e) {
        LOGGER.warn("Invalid menu order format: '{}', using default", orderString);
        return new int[] { 999 };
    }
}
```

---

## Database-Driven Menus

### CPageEntity Menu Configuration

For dynamically created menu items (stored in database):

```java
// Entity with menu configuration
CPageEntity page = new CPageEntity();
page.setName("My Dynamic Page");
page.setMenuTitle("Project.Dynamic Items");  // Menu hierarchy
page.setMenuOrder("15.20");                  // Order string
page.setIcon("vaadin:file");
page.setShowInQuickToolbar(true);            // Add to toolbar
```

### Menu Order Storage

| Field | Type | Description |
|-------|------|-------------|
| `menuTitle` | `String` | Menu path with dots (e.g., "Parent.Child") |
| `menuOrder` | `String` | Order string (e.g., "15.20") |
| `icon` | `String` | Vaadin icon name |
| `showInQuickToolbar` | `Boolean` | Include in top toolbar |

### Integration with Side Menu

**CPageMenuIntegrationService** processes database pages:

```java
public List<MenuEntry> getDynamicMenuEntries() {
    List<CPageEntity> pages = pageRepository.findAllActivePages();
    
    return pages.stream()
        .map(page -> new MenuEntry(
            "dynamic." + page.getId(),      // Path
            page.getMenuTitle(),             // Title with hierarchy
            page.getIcon(),                  // Icon
            parseMenuOrder(page.getMenuOrder()), // String → parsed order
            page.getClass()
        ))
        .collect(Collectors.toList());
}
```

---

## Architecture Overview

### Components

```
┌─────────────────────────────────────────────────────────────┐
│                      MainLayout                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ CViewToolbar (Top Bar)                                 │  │
│  │  ┌─────────┐ ┌──────────┐ ┌─────────┐                 │  │
│  │  │Dashboard│ │Calendar  │ │Tasks    │ ← Quick Toolbar │  │
│  │  │order=10 │ │order=20  │ │order=30 │   (sorted)     │  │
│  │  └─────────┘ └──────────┘ └─────────┘                 │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ CHierarchicalSideMenu (Left Sidebar)                   │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │ Root Level                                       │  │  │
│  │  │  ├─ Dashboard (order="10")                       │  │  │
│  │  │  ├─ Calendar (order="20")                        │  │  │
│  │  │  ├─ Project (order="30") → [Navigate to child]   │  │  │
│  │  │  └─ Users (order="40")                           │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │ Project Level (Parent: order="30")              │  │  │
│  │  │  ├─ Tasks (order="30.10")                        │  │  │
│  │  │  ├─ Activities (order="30.20")                   │  │  │
│  │  │  └─ Reports (order="30.30") → [Navigate deeper]  │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Data Flow

```
@MyMenu Annotation → MyMenuEntry → CHierarchicalSideMenu
     ↓                   ↓                    ↓
String order     String orderString    String-based sorting
  "10.5.2"           "10.5.2"         [10, 5, 2] comparison
                         ↓
                 CViewToolbar (if showInQuickToolbar=true)
                         ↓
              Sorted Quick Toolbar Buttons
```

### Key Classes

| Class | Role | Order Handling |
|-------|------|----------------|
| `MyMenu` | Annotation | `order` field (String) |
| `MyMenuEntry` | Data holder | `getOrderString()` returns String |
| `CHierarchicalSideMenu` | Side menu display | Parses order string per level |
| `CViewToolbar` | Top toolbar | `compareMenuOrder()` for sorting |
| `CPageEntity` | Database menu | `menuOrder` field (String) |
| `CPageMenuIntegrationService` | Integration | Converts DB pages to menu entries |

---

## Troubleshooting

### Menu Items Not Appearing

**Check:**
1. ✅ `@MyMenu` annotation present on view class
2. ✅ `order` field is not empty string
3. ✅ `title` field matches expected hierarchy
4. ✅ View class is in scanned package
5. ✅ Application restarted after annotation changes

**Debug:**
```bash
# Check if annotation is found
grep -r "@MyMenu" src/main/java/path/to/view/

# Check logs for menu loading
tail -f application.log | grep -i "menu\|hierarchy"
```

### Menu Order Not Correct

**Check:**
1. ✅ Order string format: "10" or "10.5" (not "10.05" or "10,5")
2. ✅ No duplicate order values at same level
3. ✅ Parent items have lower order than children

**Example Fix:**
```java
// ❌ WRONG - Same order at same level
@MyMenu(order = "10", title = "Page A")
@MyMenu(order = "10", title = "Page B")  // Conflict!

// ✅ CORRECT - Different orders
@MyMenu(order = "10", title = "Page A")
@MyMenu(order = "20", title = "Page B")
```

### Quick Toolbar Buttons Missing

**Check:**
1. ✅ `showInQuickToolbar = true` in annotation
2. ✅ `CPageMenuIntegrationService` is available
3. ✅ View is properly registered

**Example:**
```java
// ❌ WRONG - Missing showInQuickToolbar
@MyMenu(order = "10", icon = "vaadin:star", title = "Favorites")

// ✅ CORRECT - Explicitly enabled
@MyMenu(order = "10", icon = "vaadin:star", title = "Favorites", 
        showInQuickToolbar = true)
```

### Quick Toolbar Button Order Wrong

**Check:**
1. ✅ Order strings follow same format as side menu
2. ✅ `compareMenuOrder()` handles your order format
3. ✅ No mixing of formats (e.g., "10" vs "10.0")

**Debug:**
```java
// Add logging to CViewToolbar.createQuickToolbar()
LOGGER.debug("Toolbar item: order='{}', title='{}'", 
    entry.getOrderString(), entry.getTitle());
```

### Menu Hierarchy Not Expanding

**Check:**
1. ✅ Title uses dots to separate levels: "Parent.Child"
2. ✅ Order string matches hierarchy depth: "10.5" for 2 levels
3. ✅ Parent levels are created automatically

**Example:**
```java
// ❌ WRONG - Title doesn't match order depth
@MyMenu(order = "10.5", title = "Flat Item")  // Order has 2 levels, title has 1

// ✅ CORRECT - Matching depths
@MyMenu(order = "10.5", title = "Parent.Child")  // Both have 2 levels
```

---

## Migration from Old @Menu System

**Status**: Old @Menu annotation is **REMOVED** from codebase as of 2026-02-04.

If you have old code using `@Menu`:

### Step 1: Replace Import

```java
// OLD
import com.vaadin.flow.server.menu.Menu;

// NEW
import tech.derbent.api.menu.MyMenu;
```

### Step 2: Update Annotation

```java
// OLD
@Menu(order = 10.5, icon = "vaadin:file", title = "Parent.Child")

// NEW
@MyMenu(order = "10.5", icon = "vaadin:file", title = "Parent.Child")
```

### Step 3: Add Toolbar Flag (Optional)

```java
// NEW with toolbar
@MyMenu(order = "10.5", icon = "vaadin:file", title = "Parent.Child",
        showInQuickToolbar = true)
```

---

## Summary

✅ **String-based ordering** - No data loss from Double conversion  
✅ **Hierarchical support** - Unlimited depth (not limited by double precision)  
✅ **Quick toolbar integration** - Same ordering algorithm as side menu  
✅ **Database compatibility** - Works with dynamic CPageEntity menus  
✅ **Multi-digit support** - "100.190.250" works perfectly  

**Key Rule**: Menu order is ALWAYS a String like "10.5.2", parsed into int[] array for comparison.

---

**Last Updated**: 2026-02-04  
**Maintained By**: Derbent Development Team
