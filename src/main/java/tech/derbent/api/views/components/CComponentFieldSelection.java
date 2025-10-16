package tech.derbent.api.views.components;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.utils.Check;

/** Generic field selection component for selecting and ordering items from a source list. This component provides a two-panel interface with
 * available items on the left and selected items on the right, with buttons for adding, removing, and reordering selections. Implements HasValue and
 * HasValueAndElement to integrate with Vaadin binders.
 * <p>
 * Features:
 * <ul>
 * <li>Color-aware rendering for CEntityNamed entities (displays with colors and icons)</li>
 * <li>Text rendering for non-entity types (strings, numbers, etc.)</li>
 * <li>Add/Remove buttons for moving items between lists</li>
 * <li>Up/Down buttons for reordering selected items</li>
 * <li>Double-click support for quick item movement</li>
 * <li>Full Vaadin binder integration with List support for ordered fields</li>
 * <li>Read-only mode support</li>
 * <li>Preserves ordering for fields with @OrderColumn annotation</li>
 * </ul>
 * <p>
 * Usage Pattern:
 * <ol>
 * <li>Call setSourceItems(allAvailableItems) to provide the complete list of items that can be selected</li>
 * <li>Binder will call setValue(entity.getFieldValue()) to set currently selected items from the entity</li>
 * <li>Component automatically separates items into selected and available lists</li>
 * <li>Order of selected items is preserved for fields with @OrderColumn</li>
 * </ol>
 * @param <MasterEntity> The master entity type (e.g., CUser)
 * @param <DetailEntity> The detail entity type to select (e.g., CActivity) */
public class CComponentFieldSelection<MasterEntity, DetailEntity> extends CHorizontalLayout
		implements HasValue<HasValue.ValueChangeEvent<List<DetailEntity>>, List<DetailEntity>>,
		HasValueAndElement<HasValue.ValueChangeEvent<List<DetailEntity>>, List<DetailEntity>> {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentFieldSelection.class);
	private static final String DEFAULT_LIST_HEIGHT = "250px";
	private CButton addButton;
	private ListBox<DetailEntity> availableList;
	private CButton downButton;
	private CButton removeButton;
	private ListBox<DetailEntity> selectedList;
	private CButton upButton;
	private final List<DetailEntity> sourceItems = new ArrayList<>();
	private final List<DetailEntity> selectedItems = new ArrayList<>();
	private final List<DetailEntity> notselectedItems = new ArrayList<>();
	private List<DetailEntity> currentValue = new ArrayList<>();
	private boolean readOnly = false;
	private ItemLabelGenerator<DetailEntity> itemLabelGenerator = Object::toString;
	private final List<ValueChangeListener<? super ValueChangeEvent<List<DetailEntity>>>> listeners = new ArrayList<>();

	/** Creates a new field selection component with default titles. */
	public CComponentFieldSelection() {
		this("Available Items", "Selected Items");
	}

	/** Creates a new field selection component with custom titles.
	 * @param availableTitle Title for available items panel (must not be null or blank)
	 * @param selectedTitle  Title for selected items panel (must not be null or blank)
	 * @throws IllegalArgumentException if titles are null or blank */
	public CComponentFieldSelection(String availableTitle, String selectedTitle) {
		super();
		Check.notBlank(availableTitle, "Available title cannot be null or blank");
		Check.notBlank(selectedTitle, "Selected title cannot be null or blank");
		initializeUI(availableTitle, selectedTitle);
		setupEventHandlers();
	}

	/** Initializes the UI components with proper validation and configuration.
	 * @param availableTitle Title for available items panel
	 * @param selectedTitle  Title for selected items panel */
	private void initializeUI(String availableTitle, String selectedTitle) {
		Check.notBlank(availableTitle, "Available title cannot be null or blank");
		Check.notBlank(selectedTitle, "Selected title cannot be null or blank");
		setSpacing(true);
		setWidthFull();
		// Create left and right sections
		CVerticalLayout leftLayout = new CVerticalLayout(false, false, false);
		CVerticalLayout rightLayout = new CVerticalLayout(false, false, false);
		leftLayout.setWidth("50%");
		rightLayout.setWidth("50%");
		// Add titles
		CDiv availableHeader = new CDiv(availableTitle);
		availableHeader.getStyle().set("font-weight", "bold").set("margin-bottom", "8px");
		CDiv selectedHeader = new CDiv(selectedTitle);
		selectedHeader.getStyle().set("font-weight", "bold").set("margin-bottom", "8px");
		leftLayout.add(availableHeader);
		rightLayout.add(selectedHeader);
		// Create list boxes
		availableList = new ListBox<>();
		availableList.setHeight(DEFAULT_LIST_HEIGHT);
		availableList.setWidthFull();
		selectedList = new ListBox<>();
		selectedList.setHeight(DEFAULT_LIST_HEIGHT);
		selectedList.setWidthFull();
		// Set up color-aware rendering for entities
		configureColorAwareRenderer(availableList);
		configureColorAwareRenderer(selectedList);
		leftLayout.add(availableList);
		rightLayout.add(selectedList);
		// Control buttons with icons
		addButton = new CButton("Add", VaadinIcon.ARROW_RIGHT.create());
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.setEnabled(false);
		addButton.setTooltipText("Add selected item to the list");
		removeButton = new CButton("Remove", VaadinIcon.ARROW_LEFT.create());
		removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		removeButton.setEnabled(false);
		removeButton.setTooltipText("Remove selected item from the list");
		CHorizontalLayout controlButtons = new CHorizontalLayout(addButton, removeButton);
		controlButtons.setSpacing(true);
		leftLayout.add(controlButtons);
		// Order buttons with icons
		upButton = new CButton("Move Up", VaadinIcon.ARROW_UP.create());
		upButton.setEnabled(false);
		upButton.setTooltipText("Move selected item up in the order");
		downButton = new CButton("Move Down", VaadinIcon.ARROW_DOWN.create());
		downButton.setEnabled(false);
		downButton.setTooltipText("Move selected item down in the order");
		CHorizontalLayout orderButtons = new CHorizontalLayout(upButton, downButton);
		orderButtons.setSpacing(true);
		rightLayout.add(orderButtons);
		// Add layouts to main component
		this.add(leftLayout, rightLayout);
	}

	/** Configures color-aware rendering for entity items in ListBox. If the item is a CEntityNamed, it will be rendered with its color and icon using
	 * CEntityLabel. Otherwise, it falls back to text rendering.
	 * @param listBox The ListBox to configure (must not be null)
	 * @throws IllegalArgumentException if listBox is null */
	private void configureColorAwareRenderer(ListBox<DetailEntity> listBox) {
		Check.notNull(listBox, "ListBox cannot be null");
		listBox.setRenderer(new ComponentRenderer<>(item -> {
			try {
				if (item == null) {
					LOGGER.warn("Rendering null item in list - returning N/A placeholder");
					return new Span("N/A");
				}
				// Check if item is a CEntityNamed (has color and icon support)
				if (item instanceof CEntityNamed) {
					try {
						// Use CEntityLabel for colored rendering with icon
						return new CEntityLabel((CEntityNamed<?>) item);
					} catch (Exception e) {
						LOGGER.error("Failed to create CEntityLabel for entity: {}", item, e);
						throw new IllegalStateException("Failed to render entity with color: " + item, e);
					}
				} else {
					// Fall back to text rendering for non-entity types
					String text = itemLabelGenerator != null ? itemLabelGenerator.apply(item) : item.toString();
					return new Span(text);
				}
			} catch (Exception e) {
				// Log error and provide fallback rendering
				LOGGER.error("Error rendering item in field selection: {}", item, e);
				String fallbackText = item != null ? item.toString() : "Error";
				return new Span(fallbackText);
			}
		}));
	}

	/** Sets up event handlers for buttons and list selections. */
	private void setupEventHandlers() {
		// Enable/disable buttons based on selection
		availableList.addValueChangeListener(e -> {
			boolean hasSelection = e.getValue() != null && !readOnly;
			addButton.setEnabled(hasSelection);
		});
		selectedList.addValueChangeListener(e -> {
			boolean hasSelection = e.getValue() != null && !readOnly;
			removeButton.setEnabled(hasSelection);
			upButton.setEnabled(hasSelection);
			downButton.setEnabled(hasSelection);
		});
		// Button actions
		addButton.addClickListener(e -> {
			try {
				addSelectedItem();
			} catch (Exception ex) {
				LOGGER.error("Error adding item to selected list", ex);
				throw new IllegalStateException("Failed to add item", ex);
			}
		});
		removeButton.addClickListener(e -> {
			try {
				removeSelectedItem();
			} catch (Exception ex) {
				LOGGER.error("Error removing item from selected list", ex);
				throw new IllegalStateException("Failed to remove item", ex);
			}
		});
		upButton.addClickListener(e -> {
			try {
				moveUp();
			} catch (Exception ex) {
				LOGGER.error("Error moving item up", ex);
				throw new IllegalStateException("Failed to move item up", ex);
			}
		});
		downButton.addClickListener(e -> {
			try {
				moveDown();
			} catch (Exception ex) {
				LOGGER.error("Error moving item down", ex);
				throw new IllegalStateException("Failed to move item down", ex);
			}
		});
		// Double-click support for quick movement using DOM events
		setupDoubleClickSupport();
	}

	/** Sets up double-click support on list boxes using DOM events. */
	private void setupDoubleClickSupport() {
		// Add double-click listener to available list
		// Use addListener with synchronizeProperty to ensure server knows about selections
		availableList.getElement().addEventListener("dblclick", e -> {
			try {
				DetailEntity selected = availableList.getValue();
				if (selected != null && !readOnly) {
					addSelectedItem();
				}
			} catch (Exception ex) {
				LOGGER.error("Error handling double-click on available list", ex);
			}
		}).synchronizeProperty("value");
		// Add double-click listener to selected list
		selectedList.getElement().addEventListener("dblclick", e -> {
			try {
				DetailEntity selected = selectedList.getValue();
				if (selected != null && !readOnly) {
					removeSelectedItem();
				}
			} catch (Exception ex) {
				LOGGER.error("Error handling double-click on selected list", ex);
			}
		}).synchronizeProperty("value");
	}

	/** Adds the selected item from availableList to selectedItems. */
	private void addSelectedItem() {
		DetailEntity selected = availableList.getValue();
		if (selected != null && !selectedItems.contains(selected)) {
			selectedItems.add(selected);
			refreshLists();
			availableList.clear();
		}
	}

	/** Removes the selected item from selectedItems. */
	private void removeSelectedItem() {
		DetailEntity selected = selectedList.getValue();
		if (selected != null) {
			selectedItems.remove(selected);
			refreshLists();
			selectedList.clear();
		}
	}

	/** Moves the selected item up in the order. */
	private void moveUp() {
		DetailEntity selected = selectedList.getValue();
		if (selected != null) {
			int index = selectedItems.indexOf(selected);
			if (index > 0) {
				selectedItems.remove(index);
				selectedItems.add(index - 1, selected);
				refreshLists();
				selectedList.setValue(selected);
			}
		}
	}

	/** Moves the selected item down in the order. */
	private void moveDown() {
		DetailEntity selected = selectedList.getValue();
		if (selected != null) {
			int index = selectedItems.indexOf(selected);
			if (index < selectedItems.size() - 1) {
				selectedItems.remove(index);
				selectedItems.add(index + 1, selected);
				refreshLists();
				selectedList.setValue(selected);
			}
		}
	}

	/** Refreshes both lists and fires value change event. Separates source items into selected and available lists based on selectedItems. */
	private void refreshLists() {
		// Update notselectedItems - show items not in selected
		notselectedItems.clear();
		notselectedItems.addAll(sourceItems.stream().filter(item -> !selectedItems.contains(item)).collect(Collectors.toList()));
		// Update available list with items not yet selected
		availableList.setItems(notselectedItems);
		// Update selected list - order is preserved from selectedItems
		selectedList.setItems(selectedItems);
		// Fire value change event
		fireValueChangeEvent();
	}

	/** Fires a value change event to listeners. */
	private void fireValueChangeEvent() {
		List<DetailEntity> oldValue = currentValue;
		List<DetailEntity> newValue = getValue();
		currentValue = new ArrayList<>(newValue);
		if (!oldValue.equals(newValue)) {
			ValueChangeEvent<List<DetailEntity>> event = new ValueChangeEvent<List<DetailEntity>>() {

				private static final long serialVersionUID = 1L;

				@Override
				public HasValue<?, List<DetailEntity>> getHasValue() { return CComponentFieldSelection.this; }

				@Override
				public boolean isFromClient() { return true; }

				@Override
				public List<DetailEntity> getOldValue() { return oldValue; }

				@Override
				public List<DetailEntity> getValue() { return newValue; }
			};
			listeners.forEach(listener -> listener.valueChanged(event));
		}
	}

	// Public API methods
	/** Sets the available items to choose from. This is the complete list of items that can be selected. The component will automatically separate
	 * these into selected and available lists based on the current value (set by binder).
	 * <p>
	 * Usage Pattern:
	 * <ol>
	 * <li>Call this method with all available items (e.g., all activities in the system)</li>
	 * <li>Binder will subsequently call setValue() with the entity's current field value</li>
	 * <li>Component separates items into selected (from entity) and available (not selected)</li>
	 * <li>Order of selected items is preserved from the entity's list field</li>
	 * </ol>
	 * @param items List of items (can be null, will be treated as empty list)
	 * @throws IllegalStateException if refresh fails */
	public void setSourceItems(List<DetailEntity> items) {
		try {
			sourceItems.clear();
			if (items != null) {
				sourceItems.addAll(items);
			}
			refreshLists();
		} catch (Exception e) {
			LOGGER.error("Failed to set source items:" + e.getMessage());
			throw e;
		}
	}

	/** Sets the item label generator for displaying items. This is used for non-entity items or as a fallback.
	 * @param itemLabelGenerator Function to generate display text for items (if null, uses Object::toString)
	 * @throws IllegalStateException if renderer configuration fails */
	public void setItemLabelGenerator(ItemLabelGenerator<DetailEntity> itemLabelGenerator) {
		try {
			this.itemLabelGenerator = itemLabelGenerator != null ? itemLabelGenerator : Object::toString;
			// Refresh renderer to use new label generator
			configureColorAwareRenderer(availableList);
			configureColorAwareRenderer(selectedList);
		} catch (Exception e) {
			LOGGER.error("Failed to set item label generator:" + e.getMessage());
			throw e;
		}
	}

	/** Returns the currently selected items.
	 * @return List of selected items in order (never null) */
	public List<DetailEntity> getSelectedItems() { return new ArrayList<>(selectedItems); }

	/** Sets the selected items. This method preserves the order of items in the list, which is important for fields with @OrderColumn annotation.
	 * @param items List of items to mark as selected in the specified order (can be null, will be treated as empty)
	 * @throws IllegalStateException if refresh fails */
	public void setSelectedItems(List<DetailEntity> items) {
		try {
			selectedItems.clear();
			if (items != null) {
				// Preserve the order from the incoming list
				selectedItems.addAll(items);
			}
			refreshLists();
		} catch (Exception e) {
			LOGGER.error("Failed to set selected items:" + e.getMessage());
			throw e;
		}
	}

	// HasValue implementation
	/** Returns the current value as a list of selected items. The order is preserved from the selectedItems list, which is critical for fields with
	 * @OrderColumn annotation.
	 * @return List of selected items in order (never null) */
	@Override
	public List<DetailEntity> getValue() { return new ArrayList<>(selectedItems); }

	/** Sets the value from a list of items. This is called by Vaadin binder when loading entity data. The order of items in the list is preserved,
	 * which is essential for fields with @OrderColumn annotation.
	 * @param value List of items to select in order (can be null, will be treated as empty)
	 * @throws IllegalStateException if refresh fails */
	@Override
	public void setValue(List<DetailEntity> value) {
		try {
			selectedItems.clear();
			if (value != null) {
				// Preserve the order from the entity's list field
				selectedItems.addAll(value);
			}
			refreshLists();
		} catch (Exception e) {
			LOGGER.error("Failed to set value:" + e.getMessage());
			throw e;
		}
	}

	/** Adds a value change listener.
	 * @param listener The listener to add (must not be null)
	 * @return Registration for removing the listener
	 * @throws IllegalArgumentException if listener is null */
	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super ValueChangeEvent<List<DetailEntity>>> listener) {
		Check.notNull(listener, "ValueChangeListener cannot be null");
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	/** Returns whether the component is in read-only mode.
	 * @return true if read-only, false otherwise */
	@Override
	public boolean isReadOnly() { return readOnly; }

	/** Sets the read-only mode of the component.
	 * @param readOnly true to make read-only, false to make editable */
	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		updateButtonStates();
		availableList.setReadOnly(readOnly);
		selectedList.setReadOnly(readOnly);
	}

	/** Updates button enabled states based on current selections and read-only mode. */
	private void updateButtonStates() {
		addButton.setEnabled(!readOnly && availableList.getValue() != null);
		removeButton.setEnabled(!readOnly && selectedList.getValue() != null);
		upButton.setEnabled(!readOnly && selectedList.getValue() != null);
		downButton.setEnabled(!readOnly && selectedList.getValue() != null);
	}

	/** Returns whether the selected items list is empty.
	 * @return true if no items are selected, false otherwise */
	@Override
	public boolean isEmpty() { return selectedItems.isEmpty(); }

	/** Returns whether the required indicator is visible.
	 * @return false (not implemented) */
	@Override
	public boolean isRequiredIndicatorVisible() { return false; }

	/** Sets whether the required indicator should be visible.
	 * @param requiredIndicatorVisible true to show indicator, false to hide */
	@Override
	public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
		// Could be implemented if needed
	}

	/** Clears all selected items.
	 * @throws IllegalStateException if refresh fails */
	@Override
	public void clear() {
		try {
			selectedItems.clear();
			refreshLists();
		} catch (Exception e) {
			LOGGER.error("Failed to clear selected items:" + e.getMessage());
			throw e;
		}
	}
}
