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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.utils.CColorUtils;
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
 * <li>Full Vaadin binder integration</li>
 * <li>Read-only mode support</li>
 * </ul>
 * @param <MasterEntity> The master entity type (e.g., CUser)
 * @param <DetailEntity> The detail entity type to select (e.g., CActivity) */
public class CComponentFieldSelection<MasterEntity, DetailEntity> extends CHorizontalLayout
		implements HasValue<HasValue.ValueChangeEvent<Set<DetailEntity>>, Set<DetailEntity>>,
		HasValueAndElement<HasValue.ValueChangeEvent<Set<DetailEntity>>, Set<DetailEntity>> {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentFieldSelection.class);
	private static final String DEFAULT_LIST_HEIGHT = "250px";
	// UI Components
	private CButton addButton;
	private ListBox<DetailEntity> availableList;
	private CButton downButton;
	private CButton removeButton;
	private ListBox<DetailEntity> selectedList;
	private CButton upButton;
	// Data
	private final List<DetailEntity> sourceItems = new ArrayList<>();
	private final List<DetailEntity> selectedItems = new ArrayList<>();
	private Set<DetailEntity> currentValue = new LinkedHashSet<>();
	private boolean readOnly = false;
	// Display configuration
	private ItemLabelGenerator<DetailEntity> itemLabelGenerator = Object::toString;
	// Event handling
	private final List<ValueChangeListener<? super ValueChangeEvent<Set<DetailEntity>>>> listeners = new ArrayList<>();

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
		availableList.getElement().addEventListener("dblclick", e -> {
			try {
				DetailEntity selected = availableList.getValue();
				if (selected != null && !readOnly) {
					addSelectedItem();
				}
			} catch (Exception ex) {
				LOGGER.error("Error handling double-click on available list", ex);
			}
		});
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
		});
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

	/** Refreshes both lists and fires value change event. */
	private void refreshLists() {
		// Update available list - show items not in selected
		List<DetailEntity> available = sourceItems.stream().filter(item -> !selectedItems.contains(item)).collect(Collectors.toList());
		availableList.setItems(available);
		// Update selected list
		selectedList.setItems(selectedItems);
		// Fire value change event
		fireValueChangeEvent();
	}

	/** Fires a value change event to listeners. */
	private void fireValueChangeEvent() {
		Set<DetailEntity> oldValue = currentValue;
		Set<DetailEntity> newValue = getValue();
		currentValue = newValue;
		if (!oldValue.equals(newValue)) {
			ValueChangeEvent<Set<DetailEntity>> event = new ValueChangeEvent<Set<DetailEntity>>() {

				private static final long serialVersionUID = 1L;

				@Override
				public HasValue<?, Set<DetailEntity>> getHasValue() { return CComponentFieldSelection.this; }

				@Override
				public boolean isFromClient() { return true; }

				@Override
				public Set<DetailEntity> getOldValue() { return oldValue; }

				@Override
				public Set<DetailEntity> getValue() { return newValue; }
			};
			listeners.forEach(listener -> listener.valueChanged(event));
		}
	}

	// Public API methods
	/** Sets the available items to choose from.
	 * @param items List of items (can be null, will be treated as empty list)
	 * @throws IllegalStateException if refresh fails */
	public void setItems(List<DetailEntity> items) {
		try {
			sourceItems.clear();
			if (items != null) {
				sourceItems.addAll(items);
			}
			refreshLists();
		} catch (Exception e) {
			LOGGER.error("Failed to set items in field selection", e);
			throw new IllegalStateException("Failed to set items", e);
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
			LOGGER.error("Failed to set item label generator", e);
			throw new IllegalStateException("Failed to set item label generator", e);
		}
	}

	/** Returns the currently selected items.
	 * @return List of selected items in order (never null) */
	public List<DetailEntity> getSelectedItems() { return new ArrayList<>(selectedItems); }

	/** Sets the selected items.
	 * @param items List of items to mark as selected (can be null, will be treated as empty)
	 * @throws IllegalStateException if refresh fails */
	public void setSelectedItems(List<DetailEntity> items) {
		try {
			selectedItems.clear();
			if (items != null) {
				selectedItems.addAll(items);
			}
			refreshLists();
		} catch (Exception e) {
			LOGGER.error("Failed to set selected items", e);
			throw new IllegalStateException("Failed to set selected items", e);
		}
	}

	// HasValue implementation
	/** Returns the current value as a set of selected items.
	 * @return Set of selected items (never null) */
	@Override
	public Set<DetailEntity> getValue() { return new LinkedHashSet<>(selectedItems); }

	/** Sets the value from a set of items.
	 * @param value Set of items to select (can be null, will be treated as empty)
	 * @throws IllegalStateException if refresh fails */
	@Override
	public void setValue(Set<DetailEntity> value) {
		try {
			selectedItems.clear();
			if (value != null) {
				selectedItems.addAll(value);
			}
			refreshLists();
		} catch (Exception e) {
			LOGGER.error("Failed to set value", e);
			throw new IllegalStateException("Failed to set value", e);
		}
	}

	/** Adds a value change listener.
	 * @param listener The listener to add (must not be null)
	 * @return Registration for removing the listener
	 * @throws IllegalArgumentException if listener is null */
	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super ValueChangeEvent<Set<DetailEntity>>> listener) {
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
			LOGGER.error("Failed to clear selected items", e);
			throw new IllegalStateException("Failed to clear items", e);
		}
	}
}
