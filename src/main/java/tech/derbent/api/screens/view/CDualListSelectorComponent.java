package tech.derbent.api.screens.view;

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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.ui.component.basic.CEntityLabel;
import tech.derbent.api.utils.Check;

/** Generic dual list selector component for selecting and ordering items from a source list. This component provides a two-panel interface with
 * available items on the left and selected items on the right, with buttons for adding, removing, and reordering selections. Implements HasValue and
 * HasValueAndElement to integrate with Vaadin binders.
 * <p>
 * Features:
 * <ul>
 * <li>Color-aware rendering for CEntityNamed entities (displays with colors and icons)</li>
 * <li>Text rendering for non-entity types (strings, numbers, etc.)</li>
 * <li>Add/Remove buttons for moving items between lists</li>
 * <li>Up/Down buttons for reordering selected items</li>
 * <li>Full Vaadin binder integration</li>
 * <li>Read-only mode support</li>
 * </ul>
 * @param <T> The type of items to select */
public class CDualListSelectorComponent<T> extends VerticalLayout
		implements HasValue<HasValue.ValueChangeEvent<Set<T>>, Set<T>>, HasValueAndElement<HasValue.ValueChangeEvent<Set<T>>, Set<T>> {

	private static final String DEFAULT_LIST_HEIGHT = "250px";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDualListSelectorComponent.class);
	private static final long serialVersionUID = 1L;
	private Button addButton;
	// UI Components
	private ListBox<T> availableList;
	private Set<T> currentValue = new LinkedHashSet<>();
	private Button downButton;
	// Display configuration
	private ItemLabelGenerator<T> itemLabelGenerator = Object::toString;
	// Event handling
	private final List<ValueChangeListener<? super ValueChangeEvent<Set<T>>>> listeners = new ArrayList<>();
	private boolean readOnly = false;
	private Button removeButton;
	private final List<T> selectedItems = new ArrayList<>();
	private ListBox<T> selectedList;
	// Data
	private final List<T> sourceItems = new ArrayList<>();
	private Button upButton;

	/** Creates a new dual list selector with custom titles.
	 * @param availableTitle Title for available items panel (must not be null or blank)
	 * @param selectedTitle  Title for selected items panel (must not be null or blank)
	 * @throws IllegalArgumentException if titles are null or blank */
	public CDualListSelectorComponent(String availableTitle, String selectedTitle) {
		Check.notBlank(availableTitle, "Available title cannot be null or blank");
		Check.notBlank(selectedTitle, "Selected title cannot be null or blank");
		initializeUI(availableTitle, selectedTitle);
		setupEventHandlers();
	}

	/** Creates a new dual list selector with custom titles and initial items.
	 * @param availableTitle Title for available items panel (must not be null or blank)
	 * @param selectedTitle  Title for selected items panel (must not be null or blank)
	 * @param items          Available items to choose from (can be null or empty)
	 * @throws IllegalArgumentException if titles are null or blank */
	public CDualListSelectorComponent(String availableTitle, String selectedTitle, List<T> items) {
		this(availableTitle, selectedTitle);
		setItems(items);
	}

	/** Adds the selected item from availableList to selectedItems. */
	private void addSelectedItem() {
		final T selected = availableList.getValue();
		if (selected != null && !selectedItems.contains(selected)) {
			selectedItems.add(selected);
			refreshLists();
			availableList.clear();
		}
	}

	/** Adds a value change listener.
	 * @param listener The listener to add (must not be null)
	 * @return Registration for removing the listener
	 * @throws IllegalArgumentException if listener is null */
	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super ValueChangeEvent<Set<T>>> listener) {
		Check.notNull(listener, "ValueChangeListener cannot be null");
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	/** Clears all selected items.
	 * @throws IllegalStateException if refresh fails */
	@Override
	public void clear() {
		try {
			selectedItems.clear();
			refreshLists();
		} catch (final Exception e) {
			LOGGER.error("Failed to clear selected items", e);
			throw new IllegalStateException("Failed to clear items", e);
		}
	}

	/** Configures color-aware rendering for entity items in ListBox. If the item is a CEntityNamed, it will be rendered with its color and icon using
	 * CEntityLabel. Otherwise, it falls back to text rendering.
	 * @param listBox The ListBox to configure (must not be null)
	 * @throws IllegalArgumentException if listBox is null */
	private void configureColorAwareRenderer(ListBox<T> listBox) {
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
					} catch (final Exception e) {
						LOGGER.error("Failed to create CEntityLabel for entity: {}", item, e);
						throw new IllegalStateException("Failed to render entity with color: " + item, e);
					}
				}
				// Fall back to text rendering for non-entity types
				final String text = itemLabelGenerator != null ? itemLabelGenerator.apply(item) : item.toString();
				return new Span(text);
			} catch (final Exception e) {
				// Log error and provide fallback rendering
				LOGGER.error("Error rendering item in dual list selector: {}", item, e);
				final String fallbackText = item != null ? item.toString() : "Error";
				return new Span(fallbackText);
			}
		}));
	}

	/** Fires a value change event to listeners. */
	private void fireValueChangeEvent() {
		final Set<T> oldValue = currentValue;
		final Set<T> newValue = getValue();
		currentValue = newValue;
		if (!oldValue.equals(newValue)) {
			final ValueChangeEvent<Set<T>> event = new ValueChangeEvent<Set<T>>() {

				private static final long serialVersionUID = 1L;

				@Override
				public HasValue<?, Set<T>> getHasValue() { return CDualListSelectorComponent.this; }

				@Override
				public Set<T> getOldValue() { return oldValue; }

				@Override
				public Set<T> getValue() { return newValue; }

				@Override
				public boolean isFromClient() { return true; }
			};
			listeners.forEach(listener -> listener.valueChanged(event));
		}
	}

	/** Returns the currently selected items.
	 * @return List of selected items in order (never null) */
	public List<T> getSelectedItems() { return new ArrayList<>(selectedItems); }

	// HasValue implementation
	/** Returns the current value as a set of selected items.
	 * @return Set of selected items (never null) */
	@Override
	public Set<T> getValue() { return new LinkedHashSet<>(selectedItems); }

	/** Initializes the UI components with proper validation and configuration.
	 * @param availableTitle Title for available items panel
	 * @param selectedTitle  Title for selected items panel */
	private void initializeUI(String availableTitle, String selectedTitle) {
		Check.notBlank(availableTitle, "Available title cannot be null or blank");
		Check.notBlank(selectedTitle, "Selected title cannot be null or blank");
		setSpacing(true);
		setPadding(false);
		setWidthFull();
		// Available items section
		final H4 availableHeader = new H4(availableTitle);
		availableList = new ListBox<>();
		availableList.setHeight(DEFAULT_LIST_HEIGHT);
		availableList.setWidthFull();
		// Selected items section
		final H4 selectedHeader = new H4(selectedTitle);
		selectedList = new ListBox<>();
		selectedList.setHeight(DEFAULT_LIST_HEIGHT);
		selectedList.setWidthFull();
		// Set up color-aware rendering for entities
		configureColorAwareRenderer(availableList);
		configureColorAwareRenderer(selectedList);
		// Control buttons with icons
		addButton = new Button("Add", VaadinIcon.ARROW_RIGHT.create());
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.setEnabled(false);
		addButton.setTooltipText("Add selected item to the list");
		removeButton = new Button("Remove", VaadinIcon.ARROW_LEFT.create());
		removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		removeButton.setEnabled(false);
		removeButton.setTooltipText("Remove selected item from the list");
		// Order buttons with icons
		upButton = new Button("Move Up", VaadinIcon.ARROW_UP.create());
		upButton.setEnabled(false);
		upButton.setTooltipText("Move selected item up in the order");
		downButton = new Button("Move Down", VaadinIcon.ARROW_DOWN.create());
		downButton.setEnabled(false);
		downButton.setTooltipText("Move selected item down in the order");
		// Layouts
		final HorizontalLayout controlButtons = new HorizontalLayout(addButton, removeButton);
		controlButtons.setSpacing(true);
		final HorizontalLayout orderButtons = new HorizontalLayout(upButton, downButton);
		orderButtons.setSpacing(true);
		final VerticalLayout availableSection = new VerticalLayout(availableHeader, availableList, controlButtons);
		availableSection.setSpacing(false);
		availableSection.setPadding(false);
		availableSection.setWidthFull();
		final VerticalLayout selectedSection = new VerticalLayout(selectedHeader, selectedList, orderButtons);
		selectedSection.setSpacing(false);
		selectedSection.setPadding(false);
		selectedSection.setWidthFull();
		final HorizontalLayout mainLayout = new HorizontalLayout(availableSection, selectedSection);
		mainLayout.setWidthFull();
		mainLayout.setSpacing(true);
		add(mainLayout);
	}

	/** Returns whether the selected items list is empty.
	 * @return true if no items are selected, false otherwise */
	@Override
	public boolean isEmpty() { return selectedItems.isEmpty(); }

	/** Returns whether the component is in read-only mode.
	 * @return true if read-only, false otherwise */
	@Override
	public boolean isReadOnly() { return readOnly; }

	/** Returns whether the required indicator is visible.
	 * @return false (not implemented) */
	@Override
	public boolean isRequiredIndicatorVisible() { return false; }

	/** Moves the selected item down in the order. */
	private void moveDown() {
		final T selected = selectedList.getValue();
		if (selected != null) {
			final int index = selectedItems.indexOf(selected);
			if (index < selectedItems.size() - 1) {
				selectedItems.remove(index);
				selectedItems.add(index + 1, selected);
				refreshLists();
				selectedList.setValue(selected);
			}
		}
	}

	/** Moves the selected item up in the order. */
	private void moveUp() {
		final T selected = selectedList.getValue();
		if (selected != null) {
			final int index = selectedItems.indexOf(selected);
			if (index > 0) {
				selectedItems.remove(index);
				selectedItems.add(index - 1, selected);
				refreshLists();
				selectedList.setValue(selected);
			}
		}
	}

	/** Refreshes both lists and fires value change event. */
	private void refreshLists() {
		// Update available list - show items not in selected
		final List<T> available = sourceItems.stream().filter(item -> !selectedItems.contains(item)).collect(Collectors.toList());
		availableList.setItems(available);
		// Update selected list
		selectedList.setItems(selectedItems);
		// Fire value change event
		fireValueChangeEvent();
	}

	/** Removes the selected item from selectedItems. */
	private void removeSelectedItem() {
		final T selected = selectedList.getValue();
		if (selected != null) {
			selectedItems.remove(selected);
			refreshLists();
			selectedList.clear();
		}
	}

	/** Sets the item label generator for displaying items. This is used for non-entity items or as a fallback.
	 * @param itemLabelGenerator Function to generate display text for items (if null, uses Object::toString)
	 * @throws IllegalStateException if renderer configuration fails */
	public void setItemLabelGenerator(ItemLabelGenerator<T> itemLabelGenerator) {
		try {
			this.itemLabelGenerator = itemLabelGenerator != null ? itemLabelGenerator : Object::toString;
			// Refresh renderer to use new label generator
			configureColorAwareRenderer(availableList);
			configureColorAwareRenderer(selectedList);
		} catch (final Exception e) {
			LOGGER.error("Failed to set item label generator", e);
			throw new IllegalStateException("Failed to set item label generator", e);
		}
	}

	// Public API methods
	/** Sets the available items to choose from.
	 * @param items List of items (can be null, will be treated as empty list)
	 * @throws IllegalStateException if refresh fails */
	public void setItems(List<T> items) {
		try {
			sourceItems.clear();
			if (items != null) {
				sourceItems.addAll(items);
			}
			refreshLists();
		} catch (final Exception e) {
			LOGGER.error("Failed to set items in dual list selector", e);
			throw new IllegalStateException("Failed to set items", e);
		}
	}

	/** Sets the read-only mode of the component.
	 * @param readOnly true to make read-only, false to make editable */
	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		updateButtonStates();
		availableList.setReadOnly(readOnly);
		selectedList.setReadOnly(readOnly);
	}

	/** Sets whether the required indicator should be visible.
	 * @param requiredIndicatorVisible true to show indicator, false to hide */
	@Override
	public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
		// Could be implemented if needed
	}

	/** Sets the selected items.
	 * @param items List of items to mark as selected (can be null, will be treated as empty)
	 * @throws IllegalStateException if refresh fails */
	public void setSelectedItems(List<T> items) {
		try {
			selectedItems.clear();
			if (items != null) {
				selectedItems.addAll(items);
			}
			refreshLists();
		} catch (final Exception e) {
			LOGGER.error("Failed to set selected items", e);
			throw new IllegalStateException("Failed to set selected items", e);
		}
	}

	/** Sets up event handlers for buttons and list selections. */
	private void setupEventHandlers() {
		// Enable/disable buttons based on selection
		availableList.addValueChangeListener(e -> {
			final boolean hasSelection = e.getValue() != null && !readOnly;
			addButton.setEnabled(hasSelection);
		});
		selectedList.addValueChangeListener(e -> {
			final boolean hasSelection = e.getValue() != null && !readOnly;
			removeButton.setEnabled(hasSelection);
			upButton.setEnabled(hasSelection);
			downButton.setEnabled(hasSelection);
		});
		// Button actions
		addButton.addClickListener(e -> {
			try {
				addSelectedItem();
			} catch (final Exception ex) {
				LOGGER.error("Error adding item to selected list", ex);
				throw new IllegalStateException("Failed to add item", ex);
			}
		});
		removeButton.addClickListener(e -> {
			try {
				removeSelectedItem();
			} catch (final Exception ex) {
				LOGGER.error("Error removing item from selected list", ex);
				throw new IllegalStateException("Failed to remove item", ex);
			}
		});
		upButton.addClickListener(e -> {
			try {
				moveUp();
			} catch (final Exception ex) {
				LOGGER.error("Error moving item up", ex);
				throw new IllegalStateException("Failed to move item up", ex);
			}
		});
		downButton.addClickListener(e -> {
			try {
				moveDown();
			} catch (final Exception ex) {
				LOGGER.error("Error moving item down", ex);
				throw new IllegalStateException("Failed to move item down", ex);
			}
		});
	}

	/** Sets the value from a set of items.
	 * @param value Set of items to select (can be null, will be treated as empty)
	 * @throws IllegalStateException if refresh fails */
	@Override
	public void setValue(Set<T> value) {
		try {
			selectedItems.clear();
			if (value != null) {
				selectedItems.addAll(value);
			}
			refreshLists();
		} catch (final Exception e) {
			LOGGER.error("Failed to set value", e);
			throw new IllegalStateException("Failed to set value", e);
		}
	}

	/** Updates button enabled states based on current selections and read-only mode. */
	private void updateButtonStates() {
		addButton.setEnabled(!readOnly && availableList.getValue() != null);
		removeButton.setEnabled(!readOnly && selectedList.getValue() != null);
		upButton.setEnabled(!readOnly && selectedList.getValue() != null);
		downButton.setEnabled(!readOnly && selectedList.getValue() != null);
	}
}
