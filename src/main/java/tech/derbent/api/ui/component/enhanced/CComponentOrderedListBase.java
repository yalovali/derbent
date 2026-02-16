package tech.derbent.api.ui.component.enhanced;

import tech.derbent.api.utils.Check;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;

/** CComponentOrderedListBase - Abstract base component for managing ordered lists. Provides common functionality for components that need to display
 * and manage ordered collections, particularly for fields with @OrderColumn annotations in JPA entities.
 * <p>
 * Features:
 * <ul>
 * <li>Maintains ordered list of items</li>
 * <li>Supports add/remove operations</li>
 * <li>Supports reordering (move up/down)</li>
 * <li>Value change event handling</li>
 * <li>Read-only mode support</li>
 * <li>Item label generation for display</li>
 * </ul>
 * <p>
 * Subclasses must implement:
 * <ul>
 * <li>{@link #initializeUI()} - Create and configure UI components</li>
 * <li>{@link #refreshDisplay()} - Update UI to reflect current state</li>
 * <li>{@link #validateItem(Object)} - Optional validation before adding items</li>
 * </ul>
 * @param <T> The type of items in the ordered list */
public abstract class CComponentOrderedListBase<T> extends CHorizontalLayout
		implements HasValue<HasValue.ValueChangeEvent<List<T>>, List<T>>, HasValueAndElement<HasValue.ValueChangeEvent<List<T>>, List<T>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentOrderedListBase.class);
	private static final long serialVersionUID = 1L;
	protected boolean allowDuplicates = false;
	protected final List<T> availableItems = new ArrayList<>();
	protected List<T> currentValue = new ArrayList<>();
	// Configuration
	protected ItemLabelGenerator<T> itemLabelGenerator = Object::toString;
	// Event handling
	protected final List<ValueChangeListener<? super ValueChangeEvent<List<T>>>> listeners = new ArrayList<>();
	protected boolean readOnly = false;
	protected final List<T> selectedItems = new ArrayList<>();
	// Data management
	protected final List<T> sourceItems = new ArrayList<>();

	/** Constructor for ordered list base component. */
	protected CComponentOrderedListBase() {
	}

	/** Adds an item to the selected list at the specified position.
	 * @param item  The item to add
	 * @param index The position to insert at (-1 to add at end)
	 * @return true if item was added successfully */
	protected boolean addSelectedItem(T item, int index) {
		if (!validateItem(item)) {
			LOGGER.warn("Item failed validation, not adding: {}", item);
			return false;
		}
		if (!allowDuplicates && selectedItems.contains(item)) {
			LOGGER.debug("Item already selected and duplicates not allowed: {}", item);
			return false;
		}
		try {
			if (index < 0 || index >= selectedItems.size()) {
				selectedItems.add(item);
				LOGGER.debug("Added item to end of list: {}", itemLabelGenerator.apply(item));
			} else {
				selectedItems.add(index, item);
				LOGGER.debug("Inserted item at index {}: {}", index, itemLabelGenerator.apply(item));
			}
			updateAvailableItems();
			fireValueChangeEvent();
			refreshDisplay();
			return true;
		} catch (final Exception e) {
			LOGGER.error("Error adding item to selected list", e);
			return false;
		}
	}

	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super ValueChangeEvent<List<T>>> listener) {
		Check.notNull(listener, "ValueChangeListener cannot be null");
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	@Override
	public void clear() {
		LOGGER.debug("Clearing all selected items");
		selectedItems.clear();
		updateAvailableItems();
		fireValueChangeEvent();
		refreshDisplay();
	}

	/** Fires a value change event to all registered listeners. */
	protected void fireValueChangeEvent() {
		final List<T> oldValue = currentValue;
		currentValue = new ArrayList<>(selectedItems);
		final List<T> newValue = currentValue;
		final ValueChangeEvent<List<T>> event = new ValueChangeEvent<List<T>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public HasValue<?, List<T>> getHasValue() { return CComponentOrderedListBase.this; }

			@Override
			public List<T> getOldValue() { return oldValue; }

			@Override
			public List<T> getValue() { return newValue; }

			@Override
			public boolean isFromClient() { return false; }
		};
		listeners.forEach(listener -> {
			try {
				listener.valueChanged(event);
			} catch (final Exception e) {
				LOGGER.error("Error notifying value change listener", e);
			}
		});
	}

	/** Gets the list of available (not selected) items.
	 * @return List of available items (never null) */
	public List<T> getAvailableItems() { return new ArrayList<>(availableItems); }

	/** Gets the current item label generator.
	 * @return The item label generator */
	public ItemLabelGenerator<T> getItemLabelGenerator() { return itemLabelGenerator; }

	/** Gets the list of source items.
	 * @return Unmodifiable view of source items */
	public List<T> getItems() { return new ArrayList<>(sourceItems); }

	/** Gets the list of currently selected items in order.
	 * @return Ordered list of selected items (never null) */
	public List<T> getSelectedItems() { return new ArrayList<>(selectedItems); }

	@Override
	public List<T> getValue() { return new ArrayList<>(selectedItems); }

	/** Initialize the UI components. Subclasses must implement this to create their specific UI layout and components. */
	protected abstract void initializeUI();

	/** Gets whether duplicate items are allowed.
	 * @return true if duplicates are allowed */
	public boolean isAllowDuplicates() { return allowDuplicates; }

	@Override
	public boolean isEmpty() { return selectedItems.isEmpty(); }

	@Override
	public boolean isReadOnly() { return readOnly; }

	@Override
	public boolean isRequiredIndicatorVisible() { return false; }

	/** Moves an item down in the ordered list (towards end).
	 * @param item The item to move down
	 * @return true if item was moved successfully */
	protected boolean moveItemDown(T item) {
		Check.notNull(item, "Item to move cannot be null");
		try {
			final int index = selectedItems.indexOf(item);
			if (index >= 0 && index < selectedItems.size() - 1) {
				selectedItems.remove(index);
				selectedItems.add(index + 1, item);
				LOGGER.debug("Moved item down to index {}: {}", index + 1, itemLabelGenerator.apply(item));
				fireValueChangeEvent();
				refreshDisplay();
				return true;
			}
			return false;
		} catch (final Exception e) {
			LOGGER.error("Error moving item down", e);
			return false;
		}
	}
	// HasValue implementation

	/** Moves an item up in the ordered list (towards index 0).
	 * @param item The item to move up
	 * @return true if item was moved successfully */
	protected boolean moveItemUp(T item) {
		Check.notNull(item, "Item to move cannot be null");
		try {
			final int index = selectedItems.indexOf(item);
			if (index > 0) {
				selectedItems.remove(index);
				selectedItems.add(index - 1, item);
				LOGGER.debug("Moved item up to index {}: {}", index - 1, itemLabelGenerator.apply(item));
				fireValueChangeEvent();
				refreshDisplay();
				return true;
			}
			return false;
		} catch (final Exception e) {
			LOGGER.error("Error moving item up", e);
			return false;
		}
	}

	/** Refresh the display to show current state of items. Subclasses must implement this to update their specific UI components. */
	protected abstract void refreshDisplay();

	/** Removes an item from the selected list.
	 * @param item The item to remove
	 * @return true if item was removed successfully */
	protected boolean removeSelectedItem(T item) {
		Check.notNull(item, "Item to remove cannot be null");
		try {
			final boolean removed = selectedItems.remove(item);
			if (removed) {
				LOGGER.debug("Removed item from selected list: {}", itemLabelGenerator.apply(item));
				updateAvailableItems();
				fireValueChangeEvent();
				refreshDisplay();
			}
			return removed;
		} catch (final Exception e) {
			LOGGER.error("Error removing item from selected list", e);
			return false;
		}
	}

	/** Sets whether duplicate items are allowed in the selected list.
	 * @param allowDuplicates true to allow duplicates, false otherwise */
	public void setAllowDuplicates(boolean allowDuplicates) {
		this.allowDuplicates = allowDuplicates;
		updateAvailableItems();
		refreshDisplay();
	}

	/** Sets the item label generator for display customization.
	 * @param generator The label generator (must not be null) */
	public void setItemLabelGenerator(ItemLabelGenerator<T> generator) {
		Check.notNull(generator, "Item label generator cannot be null");
		this.itemLabelGenerator = generator;
		refreshDisplay();
	}

	/** Sets the source items that can be selected from.
	 * @param items List of available items (can be null, treated as empty list) */
	public void setItems(List<T> items) {
		Check.notNull(items, "Items list cannot be null");
		LOGGER.debug("Setting {} source items", items.size());
		sourceItems.clear();
		sourceItems.addAll(items);
		updateAvailableItems();
		refreshDisplay();
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		refreshDisplay();
	}

	@Override
	public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
		// Optional: implement if needed
	}

	@Override
	public void setValue(List<T> value) {
		Check.notNull(value, "Value cannot be null");
		LOGGER.debug("Setting value with {} items", value.size());
		selectedItems.clear();
		selectedItems.addAll(value);
		updateAvailableItems();
		currentValue = new ArrayList<>(selectedItems);
		refreshDisplay();
	}

	/** Updates the available items list by removing already selected items. */
	protected void updateAvailableItems() {
		availableItems.clear();
		if (!allowDuplicates) {
			availableItems.addAll(sourceItems.stream().filter(item -> !selectedItems.contains(item)).toList());
		} else {
			availableItems.addAll(sourceItems);
		}
		LOGGER.debug("Updated available items: {} items available", availableItems.size());
	}

	/** Validate an item before adding it to the selected list. Default implementation always returns true. Override to add custom validation.
	 * @param item The item to validate
	 * @return true if item is valid and can be added, false otherwise */
	protected boolean validateItem(T item) {
		return item != null;
	}
}
