# Custom @MyMenu Annotation - Future Fix for Hierarchical Menu Ordering

**Date**: 2026-02-04  
**Status**: 🔧 **PROPOSED SOLUTION** (Not yet implemented)  
**Purpose**: Replace Vaadin's Double-based MenuEntry.order() with String-based ordering

---

## Problem Statement

Vaadin's `@Menu` annotation uses `Double order` which **cannot preserve hierarchical menu structures** with 3+ levels:

```java
@Menu(order = 5.43)  // ← Is this "5.4.3" or "5.43"? AMBIGUOUS!
```

**Root cause**: Converting "5.4.3" to Double concatenates digits → 5.43 → information lost!

---

## Proposed Solution: @MyMenu Annotation

### 1. Annotation Definition

```java
package tech.derbent.api.menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom menu annotation that preserves hierarchical ordering as String.
 * 
 * Replaces Vaadin's @Menu annotation to support 3+ level menu hierarchies
 * without data loss.
 * 
 * Example usage:
 * <pre>
 * {@literal @}MyMenu(
 *     title = "Project.Activities.Type1",
 *     orderString = "5.4.3",  // ← Preserved exactly as written!
 *     icon = "vaadin:tasks",
 *     route = "activities/type1"
 * )
 * public class CActivityTypeView extends CAbstractPage {
 *     // View implementation
 * }
 * </pre>
 * 
 * Benefits over @Menu:
 * - ✅ Supports unlimited hierarchy levels
 * - ✅ Supports multi-digit positions (e.g., "10.20.30")
 * - ✅ No ambiguity: "5.4.3" ≠ "5.43" ≠ "5.4.30"
 * - ✅ Human-readable in source code
 * 
 * @see MyMenuEntry
 * @see MyMenuConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyMenu {
    
    /**
     * Menu title with hierarchy separated by dots.
     * Example: "Project.Activities.Type1"
     * 
     * @return the hierarchical menu title
     */
    String title();
    
    /**
     * Menu ordering as String with hierarchy separated by dots.
     * Each part is an independent integer position.
     * 
     * Examples:
     * - "5" - Single level, position 5
     * - "4.1" - Two levels: parent at 4, child at 1
     * - "10.20.30" - Three levels: grandparent at 10, parent at 20, child at 30
     * - "523.123" - Two levels: parent at 523, child at 123
     * 
     * @return the hierarchical order string
     */
    String orderString();
    
    /**
     * Icon identifier (Vaadin icon name or class reference).
     * 
     * Examples:
     * - "vaadin:tasks"
     * - "class:tech.derbent.plm.activities.domain.CActivity"
     * 
     * @return the icon identifier
     */
    String icon() default "";
    
    /**
     * Route path for navigation.
     * Optional - can be inferred from class name.
     * 
     * @return the route path
     */
    String route() default "";
    
    /**
     * Whether to show in quick access toolbar.
     * 
     * @return true if shown in quick toolbar
     */
    boolean showInQuickToolbar() default false;
}
```

---

### 2. MyMenuEntry Class

```java
package tech.derbent.api.menu;

import java.util.Arrays;
import java.util.Objects;
import com.vaadin.flow.component.Component;

/**
 * Menu entry that preserves hierarchical ordering as String array.
 * 
 * Replaces Vaadin's MenuEntry to avoid Double-based ordering limitations.
 */
public class MyMenuEntry {
    
    private final String path;
    private final String title;
    private final String orderString;
    private final int[] orderComponents;
    private final String icon;
    private final Class<? extends Component> menuClass;
    private final boolean showInQuickToolbar;
    
    /**
     * Constructor for MyMenuEntry.
     * 
     * @param path the navigation path
     * @param title the menu title (may include dots for hierarchy)
     * @param orderString the order string (e.g., "5.4.3")
     * @param icon the icon identifier
     * @param menuClass the view class
     * @param showInQuickToolbar whether to show in quick toolbar
     */
    public MyMenuEntry(String path, String title, String orderString, String icon, 
                       Class<? extends Component> menuClass, boolean showInQuickToolbar) {
        this.path = Objects.requireNonNull(path, "Path cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.orderString = orderString != null ? orderString : "999";
        this.icon = icon != null ? icon : "";
        this.menuClass = Objects.requireNonNull(menuClass, "Menu class cannot be null");
        this.showInQuickToolbar = showInQuickToolbar;
        
        // Parse orderString into integer components
        // "5.4.3" → [5, 4, 3]
        this.orderComponents = parseOrderString(this.orderString);
    }
    
    /**
     * Parse order string into integer array.
     * 
     * Examples:
     * - "5" → [5]
     * - "4.1" → [4, 1]
     * - "10.20.30" → [10, 20, 30]
     * 
     * @param orderStr the order string
     * @return array of integer order components
     */
    private static int[] parseOrderString(String orderStr) {
        if (orderStr == null || orderStr.trim().isEmpty()) {
            return new int[]{999};
        }
        
        try {
            String[] parts = orderStr.trim().split("\\.");
            int[] components = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                components[i] = Integer.parseInt(parts[i].trim());
            }
            return components;
        } catch (NumberFormatException e) {
            // Invalid format - return default high order
            return new int[]{999};
        }
    }
    
    /**
     * Get order component for specific level.
     * 
     * @param level the hierarchy level (0-based)
     * @return the order value at that level, or 999 if level doesn't exist
     */
    public int getOrderComponent(int level) {
        if (level < 0 || level >= orderComponents.length) {
            return 999;
        }
        return orderComponents[level];
    }
    
    /**
     * Compare this entry to another for sorting.
     * 
     * Compares level by level:
     * - "5.4.3" comes before "5.4.30" (3 < 30)
     * - "5.14.3" comes before "5.4.3" because parent level comparison (14 > 4) takes precedence
     * 
     * Wait, that's wrong! Let me fix:
     * - "5.4.3" comes before "5.14.3" (4 < 14 at child level)
     * 
     * @param other the other entry to compare
     * @return negative if this < other, zero if equal, positive if this > other
     */
    public int compareTo(MyMenuEntry other) {
        int maxLevels = Math.max(this.orderComponents.length, other.orderComponents.length);
        for (int level = 0; level < maxLevels; level++) {
            int thisOrder = this.getOrderComponent(level);
            int otherOrder = other.getOrderComponent(level);
            if (thisOrder != otherOrder) {
                return Integer.compare(thisOrder, otherOrder);
            }
        }
        return 0; // Equal
    }
    
    // Getters
    public String path() { return path; }
    public String title() { return title; }
    public String orderString() { return orderString; }
    public int[] orderComponents() { return orderComponents.clone(); }
    public String icon() { return icon; }
    public Class<? extends Component> menuClass() { return menuClass; }
    public boolean showInQuickToolbar() { return showInQuickToolbar; }
    
    @Override
    public String toString() {
        return String.format("MyMenuEntry{title='%s', orderString='%s', orderComponents=%s}", 
                           title, orderString, Arrays.toString(orderComponents));
    }
}
```

---

### 3. MyMenuConfiguration - Annotation Scanner

```java
package tech.derbent.api.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;

/**
 * Service that scans for @MyMenu annotations and builds menu entries.
 * 
 * Similar to Vaadin's MenuConfiguration but for custom annotation.
 */
@Service
public class MyMenuConfiguration {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MyMenuConfiguration.class);
    
    private final List<MyMenuEntry> menuEntries = new ArrayList<>();
    
    /**
     * Scan classpath for @MyMenu annotated classes.
     * 
     * Call this at application startup to build menu structure.
     */
    public void scanMyMenuAnnotations() {
        LOGGER.info("Scanning for @MyMenu annotations...");
        
        // Scan all packages for @MyMenu annotations
        Reflections reflections = new Reflections("tech.derbent");
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(MyMenu.class);
        
        for (Class<?> clazz : annotatedClasses) {
            try {
                MyMenu annotation = clazz.getAnnotation(MyMenu.class);
                
                // Validate that class extends Component (Vaadin view)
                if (!Component.class.isAssignableFrom(clazz)) {
                    LOGGER.warn("@MyMenu found on non-Component class: {}", clazz.getName());
                    continue;
                }
                
                @SuppressWarnings("unchecked")
                Class<? extends Component> componentClass = (Class<? extends Component>) clazz;
                
                // Determine route path
                String route = annotation.route();
                if (route.isEmpty()) {
                    // Infer from class name: CActivityView → "activities"
                    route = inferRouteFromClassName(clazz.getSimpleName());
                }
                
                // Create MyMenuEntry
                MyMenuEntry entry = new MyMenuEntry(
                    route,
                    annotation.title(),
                    annotation.orderString(),
                    annotation.icon(),
                    componentClass,
                    annotation.showInQuickToolbar()
                );
                
                menuEntries.add(entry);
                LOGGER.debug("Registered @MyMenu: {}", entry);
                
            } catch (Exception e) {
                LOGGER.error("Error processing @MyMenu on class {}: {}", 
                           clazz.getName(), e.getMessage(), e);
            }
        }
        
        LOGGER.info("Found {} @MyMenu entries", menuEntries.size());
    }
    
    /**
     * Get all registered menu entries.
     * 
     * @return list of menu entries
     */
    public List<MyMenuEntry> getMyMenuEntries() {
        return new ArrayList<>(menuEntries);
    }
    
    /**
     * Infer route from class name.
     * 
     * Examples:
     * - CActivityView → "activities"
     * - CMeetingPage → "meetings"
     * 
     * @param className the simple class name
     * @return the inferred route
     */
    private String inferRouteFromClassName(String className) {
        // Remove C prefix and View/Page suffix
        String name = className.replaceFirst("^C", "")
                               .replaceFirst("(View|Page)$", "");
        // Convert to lowercase
        return name.toLowerCase();
    }
}
```

---

### 4. Integration with CHierarchicalSideMenu

```java
// In CHierarchicalSideMenu constructor:

public CHierarchicalSideMenu(CPageMenuIntegrationService pageMenuService, 
                             CPageTestAuxillaryService pageTestAuxillaryService,
                             MyMenuConfiguration myMenuConfig) throws Exception {
    // ... existing initialization ...
    
    // Build menu hierarchy from BOTH annotations
    buildMenuHierarchy();
    
    // ... rest of initialization ...
}

private void buildMenuHierarchy() throws Exception {
    // ... existing Vaadin @Menu processing ...
    
    // Add @MyMenu entries (NEW!)
    final List<MyMenuEntry> myMenuEntries = myMenuConfig.getMyMenuEntries();
    for (final MyMenuEntry myEntry : myMenuEntries) {
        processMyMenuEntry(myEntry);
    }
}

/**
 * Process a MyMenuEntry with String-based ordering.
 * 
 * @param myEntry the menu entry with preserved orderString
 */
private void processMyMenuEntry(final MyMenuEntry myEntry) throws Exception {
    String title = myEntry.title();
    final String path = myEntry.path();
    final String iconName = myEntry.icon();
    final int[] orderComponents = myEntry.orderComponents();
    
    // Split title by dots to get hierarchy levels
    final String[] titleParts = title.split("\\.");
    final int levelCount = Math.min(titleParts.length, MAX_MENU_LEVELS);
    
    // ✅ NO DATA LOSS! orderComponents already parsed from orderString
    // Example: orderString "5.4.3" → orderComponents [5, 4, 3]
    
    // Ensure all parent levels exist
    String currentLevelKey = "root";
    for (int i = 0; i < levelCount - 1; i++) {
        final String levelName = titleParts[i].trim();
        final String childLevelKey = currentLevelKey + "." + levelName;
        
        if (!menuLevels.containsKey(childLevelKey)) {
            final CMenuLevel parentLevel = menuLevels.get(currentLevelKey);
            final CMenuLevel newLevel = new CMenuLevel(childLevelKey, levelName, parentLevel);
            menuLevels.put(childLevelKey, newLevel);
            
            // Add navigation item to parent level with EXACT order
            // ✅ orderComponents[i] is EXACT value from orderString!
            parentLevel.addNavigationItem(
                myEntry.menuClass(), 
                levelName, 
                iconName, 
                childLevelKey, 
                (double) orderComponents[i]  // ✅ No data loss!
            );
        }
        currentLevelKey = childLevelKey;
    }
    
    // Add final menu item (leaf node) with EXACT order
    if (levelCount > 0) {
        final String itemName = titleParts[levelCount - 1].trim();
        final CMenuLevel targetLevel = menuLevels.get(currentLevelKey);
        if (targetLevel != null) {
            targetLevel.addMenuItem(
                myEntry.menuClass(), 
                itemName, 
                iconName, 
                path, 
                (double) orderComponents[levelCount - 1]  // ✅ No data loss!
            );
        }
    }
}
```

---

### 5. Usage Examples

#### Static Page Menu (Annotation-based)

```java
package tech.derbent.plm.activities.view;

import tech.derbent.api.menu.MyMenu;
import tech.derbent.api.entity.view.CAbstractPage;

@MyMenu(
    title = "Project.Activities.Activity Types",
    orderString = "1.2.3",  // ← Exact: parent=1, child1=2, child2=3
    icon = "vaadin:tasks",
    route = "activities/types",
    showInQuickToolbar = false
)
@Route(value = "activities/types", layout = MainLayout.class)
@PageTitle("Activity Types")
public class CActivityTypeView extends CAbstractPage {
    // View implementation
}
```

#### Dynamic Page Menu (Database-based)

```java
// In CPageMenuIntegrationService:

private static MyMenuEntry createMyMenuEntryFromPage(CPageEntity page) {
    String icon = page.getIconString();
    if (icon == null || icon.trim().isEmpty()) {
        icon = "vaadin:file-text-o";
    }
    
    String menuTitle = page.getMenuTitle();
    if (menuTitle == null || menuTitle.trim().isEmpty()) {
        menuTitle = "dynamic/" + page.getPageTitle();
    } else {
        menuTitle = "dynamic/" + menuTitle;
    }
    
    // ✅ NO CONVERSION TO DOUBLE! Keep as String!
    final String orderString = page.getMenuOrder();
    
    return new MyMenuEntry(
        "dynamic." + page.getId(),
        menuTitle,
        orderString,  // ← Preserved exactly from DB!
        icon,
        CDynamicPageRouter.class,
        false
    );
}
```

---

## Migration Strategy

### Phase 1: Add @MyMenu Support (Parallel to @Menu)

1. Create annotation, classes, and configuration
2. Update CHierarchicalSideMenu to process both types
3. Add tests for @MyMenu parsing
4. Keep existing @Menu working (backward compatible)

**Effort**: 2-3 days  
**Risk**: Low (additive change)

### Phase 2: Migrate Static Pages

1. Find all classes with complex menu orders (3+ levels)
2. Replace `@Menu(order = 1.23)` with `@MyMenu(orderString = "1.2.3")`
3. Test menu ordering visually
4. Commit incrementally (per module)

**Effort**: 1-2 weeks  
**Risk**: Low (can test each migration)

### Phase 3: Migrate Dynamic Pages

1. Update CPageMenuIntegrationService to create MyMenuEntry
2. Update database queries to preserve menuOrder as String
3. Test with existing data
4. Migration script for production

**Effort**: 3-4 days  
**Risk**: Medium (affects DB-driven menus)

### Phase 4: Deprecate Double-based Ordering

1. Mark old parsing methods as @Deprecated
2. Add migration guide to docs
3. Schedule removal for next major version
4. Remove after 6 months deprecation period

**Effort**: 1 day  
**Risk**: Low (gradual deprecation)

---

## Benefits Summary

| Aspect | Current (@Menu with Double) | Future (@MyMenu with String) |
|--------|----------------------------|------------------------------|
| **Data Loss** | ❌ Loses hierarchy for 3+ levels | ✅ Zero data loss |
| **Ambiguity** | ❌ "5.43" could mean "5.4.3" or "5.43" | ✅ "5.4.3" is exact |
| **Max Levels** | ⚠️ Limited to 2 levels reliably | ✅ Unlimited levels |
| **Multi-digit** | ❌ Breaks for "5.14.3" | ✅ Works perfectly |
| **Parsing** | ❌ Complex math with data loss | ✅ Simple String.split() |
| **Human Readable** | ❌ Hard to understand Double values | ✅ Clear and intuitive |
| **Type Safety** | ⚠️ Double (numeric operations possible) | ✅ String (intent clear) |

---

## Testing Plan

### Unit Tests

```java
@Test
void testMyMenuEntry_SingleLevel() {
    MyMenuEntry entry = new MyMenuEntry("path", "Title", "5", "icon", View.class, false);
    assertArrayEquals(new int[]{5}, entry.orderComponents());
    assertEquals(5, entry.getOrderComponent(0));
    assertEquals(999, entry.getOrderComponent(1)); // Beyond depth
}

@Test
void testMyMenuEntry_ThreeLevels() {
    MyMenuEntry entry = new MyMenuEntry("path", "Title", "5.4.3", "icon", View.class, false);
    assertArrayEquals(new int[]{5, 4, 3}, entry.orderComponents());
}

@Test
void testMyMenuEntry_MultiDigit() {
    MyMenuEntry entry = new MyMenuEntry("path", "Title", "10.20.30", "icon", View.class, false);
    assertArrayEquals(new int[]{10, 20, 30}, entry.orderComponents());
}

@Test
void testMyMenuEntry_Comparison() {
    MyMenuEntry e1 = new MyMenuEntry("p1", "T1", "5.4.3", "i", View.class, false);
    MyMenuEntry e2 = new MyMenuEntry("p2", "T2", "5.4.30", "i", View.class, false);
    MyMenuEntry e3 = new MyMenuEntry("p3", "T3", "5.14.3", "i", View.class, false);
    
    assertTrue(e1.compareTo(e2) < 0); // 3 < 30
    assertTrue(e1.compareTo(e3) < 0); // 4 < 14 at child level
    assertTrue(e2.compareTo(e3) < 0); // 4 < 14 at child level
}
```

### Integration Tests

```java
@Test
void testMyMenuAnnotationScanning() {
    MyMenuConfiguration config = new MyMenuConfiguration();
    config.scanMyMenuAnnotations();
    List<MyMenuEntry> entries = config.getMyMenuEntries();
    assertTrue(entries.size() > 0);
}

@Test
void testCHierarchicalSideMenu_WithMyMenuEntries() {
    // Create menu with MyMenuEntry items
    // Verify ordering is correct
    // Verify navigation works
}
```

### Visual Testing

1. Create test views with complex ordering:
   - "1.2.3", "1.2.30", "1.14.5"
2. Navigate menu in browser
3. Verify items appear in correct order
4. Verify navigation to correct pages

---

## Related Files

- **Analysis**: `HIERARCHICAL_MENU_ORDERING_BUG_ANALYSIS.md`
- **Bug Location**: `CPageMenuIntegrationService.parseMenuOrderToDouble()`
- **Recovery Attempt**: `CHierarchicalSideMenu.parseHierarchicalOrder()`

---

## References

- Vaadin Menu Configuration: https://vaadin.com/docs/latest/routing/navigation
- Spring Reflections: https://github.com/ronmamo/reflections
- Java Annotations: https://docs.oracle.com/javase/tutorial/java/annotations/

---

## Conclusion

The custom `@MyMenu` annotation provides a **clean, type-safe, and lossless** solution to the hierarchical menu ordering problem. By keeping order values as Strings throughout the entire pipeline, we:

1. ✅ Eliminate data loss completely
2. ✅ Support unlimited hierarchy levels
3. ✅ Support multi-digit position numbers
4. ✅ Improve code readability
5. ✅ Maintain backward compatibility during migration

**Recommendation**: Implement this solution in phases, starting with @MyMenu support alongside existing @Menu, then gradually migrate pages over time.
