# CComponentListSelection - Usage Guide

## Overview
`CComponentListSelection` is a generic UI component for selecting multiple items from a list with a simple click-to-toggle interface. Items are displayed in a single grid with checkmarks (✓) indicating selection.

## When to Use
Use `CComponentListSelection` when you need to:
- Select multiple items from a predefined list
- Display selections visually with checkmarks
- Avoid the complexity of ordering controls
- Work with List or Set fields in domain entities

**Don't use when:**
- You need to maintain a specific order of selected items (use `CComponentFieldSelection` instead)
- You only need single selection (use ComboBox)
- You have a small number of options (use CheckboxGroup)

## Basic Usage

### 1. Add Annotation to Domain Field

```java
@Entity
public class CWorkflowStatusRelation extends CEntityDB<CWorkflowStatusRelation> {
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "cworkflowstatusrelation_roles", 
        joinColumns = @JoinColumn(name = "cworkflowstatusrelation_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    @AMetaData(
        displayName = "User Roles",
        description = "The user roles allowed to make this transition",
        dataProviderBean = "CUserProjectRoleService",
        useGridSelection = true,  // This triggers CComponentListSelection
        useIcon = true,            // Show icons for CEntityNamed items
        setBackgroundFromColor = true  // Use entity colors
    )
    private List<CUserProjectRole> roles = new ArrayList<>();
    
    // Getters and setters
    public List<CUserProjectRole> getRoles() { return roles; }
    public void setRoles(List<CUserProjectRole> roles) { this.roles = roles; }
}
```

### 2. Form Builder Integration

The component is automatically created by `CFormBuilder` when:
- Field is of type `List` or `Set`
- Field has a `dataProviderBean` annotation
- Annotation includes `useGridSelection = true`

```java
// This happens automatically in CFormBuilder
if (fieldInfo.isUseGridSelection()) {
    component = createGridListSelector(contentOwner, fieldInfo, binder);
}
```

### 3. Manual Component Creation

If you need to create the component manually:

```java
// Basic creation
CComponentListSelection<CWorkflowStatusRelation, CUserProjectRole> selector = 
    new CComponentListSelection<>(
        dataProviderResolver,
        contentOwner,
        fieldInfo,
        "Available Roles"  // Grid title
    );

// Set source items (all available items)
List<CUserProjectRole> allRoles = roleService.findAll();
selector.setSourceItems(allRoles);

// Set currently selected items
List<CUserProjectRole> selectedRoles = entity.getRoles();
selector.setValue(selectedRoles);

// Add to form
form.add(selector);
```

## Features

### Visual Rendering
- **CEntityNamed items**: Displayed with color and icon
- **Other types**: Displayed as text
- **Selected items**: Marked with green checkmark (✓)

### Interaction
- Click any item to toggle selection
- No double-click needed
- No ordering buttons (add/remove/up/down)

### Binder Integration
Fully compatible with Vaadin Binder:

```java
Binder<CWorkflowStatusRelation> binder = new Binder<>(CWorkflowStatusRelation.class);
binder.forField(selector).bind("roles");

// Load entity
CWorkflowStatusRelation relation = service.findById(id);
binder.setBean(relation);  // Automatically loads selected roles

// Save entity
binder.writeBeanIfValid(relation);  // Automatically saves selected roles
```

## Comparison with CComponentFieldSelection

| Feature | CComponentListSelection | CComponentFieldSelection |
|---------|------------------------|--------------------------|
| Display | Single grid with checkmarks | Two grids (available/selected) |
| Selection | Click to toggle | Buttons to add/remove |
| Ordering | Not supported | Full ordering with up/down |
| Use case | Simple multi-select | Ordered selection |
| Annotation | `useGridSelection = true` | `useDualListSelector = true` |
| UI complexity | Simple | Complex |

## Advanced Configuration

### Custom Item Label Generator
```java
selector.setItemLabelGenerator(role -> 
    role.getName() + " (" + role.getProject().getName() + ")"
);
```

### Read-Only Mode
```java
selector.setReadOnly(true);  // Disable selection changes
```

### Value Change Listener
```java
selector.addValueChangeListener(event -> {
    List<CUserProjectRole> oldRoles = event.getOldValue();
    List<CUserProjectRole> newRoles = event.getValue();
    System.out.println("Roles changed from " + oldRoles.size() + " to " + newRoles.size());
});
```

## Example: Complete Form Integration

```java
public class CWorkflowStatusRelationDialog extends CDBRelationDialog<
    CWorkflowStatusRelation, CWorkflowEntity, CProjectItemStatus> {
    
    public CWorkflowStatusRelationDialog(
            IContentOwner parentContent,
            CWorkflowStatusRelation relation,
            CWorkflowEntity workflow,
            Consumer<CWorkflowStatusRelation> onSave) throws Exception {
        super(parentContent, relation, workflow, ...);
        setupDialog();
        populateForm();
    }
    
    @Override
    protected List<String> getFormFields() {
        // "roles" field will automatically use CComponentListSelection
        return List.of("fromStatus", "toStatus", "roles");
    }
}
```

## Styling

The component uses consistent styling:
- **Grid header**: Blue (#1565C0)
- **Selected checkmark**: Green (#4CAF50), bold, 18px
- **Grid height**: 300px (default)

### Custom Styling
```java
// Access internal grid (not recommended, but possible)
// Better to use CSS classes
selector.addClassName("custom-role-selector");
```

## Best Practices

1. **Always provide source items**: Call `setSourceItems()` before `setValue()`
2. **Use appropriate data types**: Works best with CEntityNamed entities
3. **Keep item labels short**: Grid displays items in a single column
4. **Limit number of items**: For very large lists (>100 items), consider pagination or filtering
5. **Clear null handling**: The component handles null values gracefully

## Common Pitfalls

### ❌ Don't Do This
```java
// Setting value before source items
selector.setValue(selectedRoles);  // Will fail!
selector.setSourceItems(allRoles);
```

### ✅ Do This Instead
```java
// Set source items first
selector.setSourceItems(allRoles);
selector.setValue(selectedRoles);
```

### ❌ Don't Do This
```java
// Assuming order is preserved
List<CUserProjectRole> roles = selector.getValue();
// roles may not be in the same order as setValue()
```

### ✅ Do This Instead
```java
// Use CComponentFieldSelection if order matters
// CComponentListSelection does not guarantee order
```

## Testing

Example unit test:
```java
@Test
public void testRoleSelection() {
    CComponentListSelection<Object, CUserProjectRole> selector = 
        new CComponentListSelection<>(null, null, null, "Roles");
    
    List<CUserProjectRole> allRoles = createTestRoles();
    selector.setSourceItems(allRoles);
    
    List<CUserProjectRole> selected = List.of(allRoles.get(0), allRoles.get(2));
    selector.setValue(selected);
    
    assertEquals(2, selector.getValue().size());
    assertTrue(selector.getValue().contains(allRoles.get(0)));
    assertTrue(selector.getValue().contains(allRoles.get(2)));
}
```

## Migration from Single to Multiple Selection

If you're migrating from a single role field to multiple roles:

### Before:
```java
@ManyToOne
private CUserProjectRole role;

public CUserProjectRole getRole() { return role; }
public void setRole(CUserProjectRole role) { this.role = role; }
```

### After:
```java
@ManyToMany
@AMetaData(useGridSelection = true, dataProviderBean = "CUserProjectRoleService")
private List<CUserProjectRole> roles = new ArrayList<>();

public List<CUserProjectRole> getRoles() { return roles; }
public void setRoles(List<CUserProjectRole> roles) { this.roles = roles; }
```

### Data Migration:
```java
// Migrate single role to roles list
if (entity.getRole() != null) {
    entity.setRoles(List.of(entity.getRole()));
}
```

## Support

For issues or questions about `CComponentListSelection`:
1. Check this documentation
2. Review `CComponentListSelectionTest.java` for examples
3. Compare with `CComponentFieldSelection` for similar patterns
4. Consult the implementation documentation in `docs/implementation/`

## See Also
- `CComponentFieldSelection` - For ordered selection
- `CFormBuilder` - Automatic component creation
- `AMetaData` - Field annotation system
- Vaadin Grid documentation
