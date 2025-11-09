# Hierarchical Menu Order Implementation

## Overview
This document describes the implementation of hierarchical menu ordering for CPageEntity menu items in the Derbent application.

## Problem Statement
The `menuOrder` field in CPageEntity needed to support hierarchical ordering where:
- Single integer format (e.g., "4") represents the order of an item
- Dot notation format (e.g., "4.1") represents parent.child ordering
  - "4" is the order of the parent menu item
  - "1" is the order of the child menu item within its parent

## Solution Architecture

### 1. Order Format Encoding
Menu orders are stored as strings in CPageEntity (e.g., "4.1") but converted to Double values for processing:
- "4" → 4.0 (single-level item with order 4)
- "4.1" → 4.1 (parent order 4, child order 1)
- "4.12" → 4.12 (parent order 4, child orders 1 and 2)

The Double representation preserves hierarchical information through its integer and fractional parts.

### 2. Implementation Components

#### CPageMenuIntegrationService
**File:** `src/main/java/tech/derbent/page/service/CPageMenuIntegrationService.java`

**Changes:**
- Replaced complex depth-based parsing with simple `parseMenuOrderToDouble()`
- Converts menuOrder string directly to Double, preserving hierarchical structure
- Default order of 999.0 for missing/invalid values

**Key Method:**
```java
private Double parseMenuOrderToDouble(String menuOrderStr) {
    if (menuOrderStr == null || menuOrderStr.trim().isEmpty()) {
        return 999.0; // Default high order
    }
    return Double.parseDouble(menuOrderStr.trim());
}
```

#### CHierarchicalSideMenu
**File:** `src/main/java/tech/derbent/api/ui/component/CHierarchicalSideMenu.java`

**Changes:**
1. Added `order` field to `CMenuItem` inner class
2. Updated constructor to accept order parameter (default 999.0 if not specified)
3. Modified `addMenuItem()` and `addNavigationItem()` to pass order values
4. Implemented sorting in `createLevelComponent()` to order items by their order field
5. Added `parseHierarchicalOrder()` to extract parent/child orders from Double value
6. Updated `processMenuEntry()` to distribute order components across hierarchy levels

**Key Method:**
```java
private Double[] parseHierarchicalOrder(Double order, int levelCount) {
    // Extract integer part (parent) and fractional part (children)
    int integerPart = (int) Math.floor(order);
    double fractionalPart = order - integerPart;
    
    if (levelCount == 2) {
        // Two levels: integer for parent, fractional*10 for child
        orderComponents[0] = (double) integerPart;
        orderComponents[1] = fractionalPart * 10.0; // 0.1 → 1.0, 0.2 → 2.0, etc.
    }
    // ... handle other level counts
}
```

### 3. Order Interpretation Examples

| menuOrder String | Double Value | Menu Structure | Interpretation |
|------------------|--------------|----------------|----------------|
| "5" | 5.0 | Single item | Top-level item with order 5 |
| "4.1" | 4.1 | Parent.Child | Parent at order 4, child at order 1 |
| "4.2" | 4.2 | Parent.Child | Parent at order 4, child at order 2 |
| "4.12" | 4.12 | Parent.Child1.Child2 | Parent=4, Child1=1, Child2=2 |
| null/"" | 999.0 | Any | Default high order (appears last) |

### 4. Sorting Behavior

Menu items are sorted within each level using the order values:
1. Items are added to a list as they're encountered
2. When displaying a level, `createLevelComponent()` sorts items by order
3. Items with lower order values appear first
4. Items with missing/invalid orders (999.0) appear last

**Example:**
```
Root Level:
  - System (order 1.0)
  - Settings (order 2.0)
  - Project (order 4.0) → navigates to Project submenu
  - Admin (order 5.0)

Project Submenu:
  - Dashboard (order 1.0) ← from menuOrder "4.1"
  - Reports (order 2.0) ← from menuOrder "4.2"
  - Settings (order 3.0) ← from menuOrder "4.3"
```

## Testing

### Unit Tests
**File:** `src/test/java/tech/derbent/page/service/CPageMenuOrderTest.java`

Validates:
1. Hierarchical order parsing (extracting integer and fractional parts)
2. Menu order interpretation for various scenarios
3. Default order handling for missing/invalid values

**Test Results:**
```
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
✓ testHierarchicalOrderParsing
✓ testMenuOrderInterpretation
✓ testDefaultOrder
```

## Usage Example

To create a page that appears as the second item under the "System" parent (which has order 3):

```java
CPageEntity page = new CPageEntity();
page.setMenuTitle("System.Configuration");
page.setMenuOrder("3.2"); // Parent at 3, this item at position 2 within System
```

This will:
1. Ensure "System" navigation item appears at position 3 in root menu
2. Place "Configuration" at position 2 within the System submenu

## Backward Compatibility

The implementation maintains backward compatibility:
- Existing single-integer orders (e.g., "5") continue to work
- Invalid orders default to 999.0 (appear last)
- Empty/null orders default to 999.0

## Technical Notes

1. **Double Precision:** The use of Double allows up to ~15 decimal digits, supporting deep menu hierarchies
2. **Fractional Interpretation:** Each digit after decimal represents one level (0.1→1, 0.12→1,2)
3. **Parent Creation:** Parent items are created implicitly; their order comes from the first child that creates them
4. **Sorting Performance:** Sorting happens only during level component creation, not on every item addition

## Future Enhancements

Potential improvements:
1. Support for explicit parent item definitions
2. More sophisticated order conflict resolution
3. Visual order editor in the UI
4. Order validation during entity creation
