# Editable Grid Architecture

## Overview

The inline-edit architecture allows grid cells to be edited directly inside the grid without
opening a separate dialog. Editable columns are declared per-entity in the initializer service
using `CGridEntity.setEditableColumnFields()`. The rendering layer reads this list and activates
Vaadin's `Grid.Editor` with exactly **one editor component per column** – memory cost is
O(editable\_columns), not O(rows × columns).

## Key Design Decision

| Option | Memory | UX | Notes |
|--------|--------|-----|-------|
| Always-present widgets per cell | O(rows × cols) | Native | Too expensive |
| Shared popup dialog | O(1) | Extra click | Good for complex forms |
| **Vaadin Grid.Editor (chosen)** | **O(cols)** | **Click-to-edit inline** | Best balance |

Vaadin's `Grid.Editor` creates one editor component per editable column, shared across all rows.
Only the currently-edited row shows the components; all other rows remain read-only text.

## How to Configure Editable Columns

In an entity's initializer service (e.g. `CActivityInitializerService`), call
`setEditableColumnFields` on the `CGridEntity` after `setColumnFields`:

```java
public static CGridEntity createGridEntity(final CProject<?> project) {
    final CGridEntity grid = createBaseGridEntity(project, clazz);
    // All columns shown in the grid
    grid.setColumnFields(List.of("id", "name", "assignedTo", "startDate", ...));
    // Subset of columns that allow inline editing
    grid.setEditableColumnFields(List.of("name", "assignedTo", "startDate", "dueDate",
            "progressPercentage", "estimatedHours", "status", "priority"));
    return grid;
}
```

Rules:
- Fields in `editableColumnFields` **must also appear** in `columnFields`.
- Fields annotated `@AMetaData(readOnly = true)` are silently skipped.
- Entity-reference fields **must** have `@AMetaData(dataProviderBean = "...")` set.

## Component Classes

### `CGridEditorFactory`
`tech.derbent.api.screens.view.CGridEditorFactory`

Static utility that maps Java field types to Vaadin editor components:

| Java type | Vaadin component |
|-----------|-----------------|
| `String` | `CTextField` |
| `Integer` / `int` | `IntegerField` (with step buttons) |
| `BigDecimal` | `BigDecimalField` |
| `LocalDate` | `DatePicker` |
| `boolean` / `Boolean` | `Checkbox` |
| `CEntityDB` subclass | `ComboBox<T>` (items from `dataProviderBean`) |

Returns `null` for unsupported or read-only fields.

### `CComponentGridEntity.setupGridEditor()`
`tech.derbent.api.screens.view.CComponentGridEntity`

Called automatically from `createContent()` after `createGridColumns()`.

Flow:
1. Reads `CGridEntity.getEditableColumnFields()`
2. For each editable field, creates an editor component via `CGridEditorFactory`
3. Binds the component to the entity bean field using a reflection-based getter/setter via raw `Binder`
4. Sets the component on the grid column (`column.setEditorComponent(...)`)
5. Marks the column header with a ✏ pencil prefix
6. Registers an item-click listener to activate the editor on click
7. Registers a close listener to persist the entity via `service.save(item)`

### `CGrid.addEntityColumn()` (bug fix)
`tech.derbent.api.grid.domain.CGrid`

`addEntityColumn` previously did not call `column.setKey(key)`.
This caused `grid.getColumnByKey(fieldName)` to return `null` for entity-reference columns.
Fixed by adding `column.setKey(key)` before returning.

## User Interaction

1. User sees grid with editable columns marked by **✏** in the header.
2. Single click on any cell in an editable row activates the editor for that row.
3. The clicked column's editor component receives focus automatically.
4. User edits the value; the Binder writes it to the entity bean on each keystroke (unbuffered).
5. Clicking another row closes the editor for the current row, triggering auto-save.
6. On save failure, an error notification is shown; the bean retains the edited value for retry.

## CSS

Styles live in `themes/default/styles.css` under the `INLINE GRID EDITOR` section:
- Active editor row gets a subtle blue tint via `--lumo-primary-color-10pct`.
- Editor input fields are height-constrained to fit within the grid row.

## Extending to New Entities

To enable inline editing for a new entity:
1. In the entity's `XxxInitializerService.createGridEntity()`, call `setEditableColumnFields(...)`.
2. Ensure each editable entity-reference field has `@AMetaData(dataProviderBean = "SomeService")`.
3. No code changes required elsewhere – the architecture is fully driven by configuration.

## Testing

Run the Playwright selective test targeting the Activity grid:
```bash
.github/agents/verifier/scripts/test-selective.sh activity
```

Verify:
- Editable column headers show the ✏ prefix.
- Clicking a cell opens the editor for that row only.
- Changing a value and clicking another row saves the change (verify DB or UI refresh).
- Non-editable fields (e.g., `id`, `createdDate`) remain read-only.
- Hundreds-of-rows grids do not degrade in performance (editor components are O(cols) not O(rows)).
