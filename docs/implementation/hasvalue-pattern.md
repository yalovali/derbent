# HasValue Interface Implementation Pattern

## Overview

Components in the Derbent project that manage selectable items should implement the `HasValue` interface to enable automatic binding to page service methods via `CPageService.bindMethods()`.

## Components with HasValue Support

### Base Components
- **CComponentEntitySelection** - Multi-entity selection component with grid
- **CComponentListEntityBase** - Base class for managing ordered lists of child entities
- **CComponentOrderedListBase** - Abstract base for ordered list management
- **CComponentListSelection** - Simple list selection with checkmarks
- **CComponentFieldSelection** - Field-based entity selection

### Concrete Components (inherit from base)
- **CComponentListSprintItems** - Sprint items management (inherits from CComponentListEntityBase)
- **CComponentListDetailLines** - Detail lines management (inherits from CComponentListEntityBase)
- **CComponentBacklog** - Sprint backlog (inherits from CComponentEntitySelection)

## Implementation Pattern

### 1. Implement HasValue Interface

```java
public abstract class CComponentListEntityBase<MasterEntity extends CEntityDB<?>, 
                                                ChildEntity extends CEntityDB<?> & IOrderedEntity>
        extends VerticalLayout 
        implements IContentOwner, IGridComponent<ChildEntity>, IGridRefreshListener<ChildEntity>,
        HasValue<HasValue.ValueChangeEvent<ChildEntity>, ChildEntity> {
    
    // Value change listeners
    private final List<ValueChangeListener<? super ValueChangeEvent<ChildEntity>>> valueChangeListeners = new ArrayList<>();
    private ChildEntity previousValue = null;
    private boolean readOnly = false;
    
    // ... other fields
}
```

### 2. Implement Required Methods

```java
// Get current value
@Override
public ChildEntity getValue() {
    return selectedItem;
}

// Set value programmatically
@Override
public void setValue(final ChildEntity value) {
    grid.asSingleSelect().setValue(value);
    selectedItem = value;
    updateButtonStates(value != null);
    fireValueChangeEvent(value, false); // false = not from client
}

// Clear selection
@Override
public void clear() {
    setValue(null);
}

// Check if empty
@Override
public boolean isEmpty() {
    return selectedItem == null;
}

// Read-only support
@Override
public void setReadOnly(final boolean readOnly) {
    this.readOnly = readOnly;
    // Update button states and grid
}

@Override
public boolean isReadOnly() {
    return readOnly;
}

// Register listener
@Override
public Registration addValueChangeListener(
        final ValueChangeListener<? super ValueChangeEvent<ChildEntity>> listener) {
    valueChangeListeners.add(listener);
    return () -> valueChangeListeners.remove(listener);
}
```

### 3. Fire Value Change Events

Fire events when selection changes, tracking whether change is from client or programmatic:

```java
protected void on_gridItems_selected(final ChildEntity item) {
    selectedItem = item;
    updateButtonStates(item != null);
    notifySelectionOwner();
    fireValueChangeEvent(item, true); // true = from client interaction
}

protected void fireValueChangeEvent(final ChildEntity newValue, final boolean fromClient) {
    final ChildEntity oldValue = previousValue;
    previousValue = newValue;
    
    final ValueChangeEvent<ChildEntity> event = new ValueChangeEvent<ChildEntity>() {
        @Override
        public HasValue<?, ChildEntity> getHasValue() {
            return CComponentListEntityBase.this;
        }
        
        @Override
        public ChildEntity getOldValue() {
            return oldValue;
        }
        
        @Override
        public ChildEntity getValue() {
            return newValue;
        }
        
        @Override
        public boolean isFromClient() {
            return fromClient;
        }
    };
    
    for (final ValueChangeListener<? super ValueChangeEvent<ChildEntity>> listener : valueChangeListeners) {
        try {
            listener.valueChanged(event);
        } catch (final Exception e) {
            LOGGER.error("Error notifying value change listener", e);
        }
    }
}
```

## Automatic Binding with CPageService

### How It Works

1. Component implements `HasValue` interface
2. Component is registered in FormBuilder's componentMap with a field name (e.g., "sprintItems", "backlogItems")
3. Page service defines handler method following naming convention: `on_{fieldName}_{action}`
4. `CPageService.bindMethods()` automatically wires the component to the handler

### Example: Sprint Items Component

**Component Registration** (in form builder or page setup):
```java
CComponentListSprintItems componentSprintItems = new CComponentListSprintItems(...);
formBuilder.getComponentMap().put("sprintItems", componentSprintItems);
```

**Page Service Handler**:
```java
public class CPageServiceSprint extends CPageServiceDynamicPage<CSprint> {
    
    public void on_sprintItems_change(final Component component, final Object value) {
        LOGGER.info("Sprint items selection changed: {}", value);
        // Handle the value change
        if (value instanceof CSprintItem) {
            CSprintItem selectedItem = (CSprintItem) value;
            // Do something with the selected item
        }
    }
}
```

**Automatic Wiring** (happens in CPageService.bindMethods()):
```java
protected void bindMethods(final CPageService<?> page) {
    final var components = formBuilder.getComponentMap();
    
    // For each component, find matching handler methods
    // Pattern: on_componentName_action
    // Example: on_sprintItems_change
    
    for (Method method : page.getClass().getDeclaredMethods()) {
        Matcher matcher = HANDLER_PATTERN.matcher(method.getName());
        if (matcher.matches()) {
            String componentName = matcher.group(1); // "sprintItems"
            String action = matcher.group(2);         // "change"
            Component component = components.get(componentName);
            
            if (action.equals("change") && component instanceof HasValue) {
                HasValue<?, ?> hasValue = (HasValue<?, ?>) component;
                hasValue.addValueChangeListener(event -> {
                    method.invoke(page, component, event.getValue());
                });
            }
        }
    }
}
```

## Benefits

1. **Automatic Event Binding**: No need to manually wire event listeners
2. **Consistent Pattern**: All components follow the same HasValue interface
3. **Type Safety**: Generic types ensure compile-time type checking
4. **Vaadin Integration**: Full compatibility with Vaadin Binder
5. **Testability**: Easy to unit test with value change events

## Migration Guide

### For Existing Components

If you have a component that manages selection:

1. Add `HasValue<ValueChangeEvent, T>` to implements clause
2. Add value change listener list and tracking fields
3. Implement getValue(), setValue(), clear(), isEmpty()
4. Implement addValueChangeListener() returning Registration
5. Fire value change events when selection changes
6. Track isFromClient flag to distinguish user vs programmatic changes

### For New Components

1. Consider extending CComponentListEntityBase if managing ordered child entities
2. Or extend CComponentEntitySelection if managing multi-entity selection
3. Or implement HasValue directly if custom component
4. Follow the pattern shown in this document

## Testing

Test HasValue implementation with:

```java
@Test
void testImplementsHasValueInterface() {
    assertTrue(HasValue.class.isAssignableFrom(YourComponent.class));
}

@Test
void testHasGetValueMethod() throws Exception {
    Method method = YourComponent.class.getMethod("getValue");
    assertNotNull(method);
}

@Test
void testHasAddValueChangeListenerMethod() throws Exception {
    Method method = YourComponent.class.getMethod("addValueChangeListener", 
        HasValue.ValueChangeListener.class);
    assertTrue(Registration.class.isAssignableFrom(method.getReturnType()));
}
```

## See Also

- `CComponentEntitySelection` - Reference implementation for multi-select
- `CComponentListEntityBase` - Reference implementation for single-select lists
- `CPageService.bindMethods()` - Automatic binding mechanism
- `CComponentEntitySelectionHasValueTest` - Test examples
- `CComponentListEntityBaseHasValueTest` - Test examples
