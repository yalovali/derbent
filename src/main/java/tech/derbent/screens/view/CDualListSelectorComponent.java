package tech.derbent.screens.view;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.shared.Registration;

/** Generic dual list selector component for selecting and ordering items from a source list. This component provides a two-panel interface with
 * available items on the left and selected items on the right, with buttons for adding, removing, and reordering selections. Implements HasValue and
 * HasValueAndElement to integrate with Vaadin binders.
 * @param <T> The type of items to select */
public class CDualListSelectorComponent<T> extends VerticalLayout
		implements HasValue<HasValue.ValueChangeEvent<Set<T>>, Set<T>>, HasValueAndElement<HasValue.ValueChangeEvent<Set<T>>, Set<T>> {

	private static final long serialVersionUID = 1L;
	// UI Components
	private ListBox<T> availableList;
	private ListBox<T> selectedList;
	private Button addButton;
	private Button removeButton;
	private Button upButton;
	private Button downButton;
	// Data
	private final List<T> sourceItems = new ArrayList<>();
	private final List<T> selectedItems = new ArrayList<>();
	private Set<T> currentValue = new LinkedHashSet<>();
	private boolean readOnly = false;
	// Display configuration
	private ItemLabelGenerator<T> itemLabelGenerator = Object::toString;
	// Event handling
	private final List<ValueChangeListener<? super ValueChangeEvent<Set<T>>>> listeners = new ArrayList<>();

	/** Creates a new dual list selector with default titles.
	 * @param availableTitle Title for available items panel
	 * @param selectedTitle  Title for selected items panel */
	public CDualListSelectorComponent(String availableTitle, String selectedTitle) {
		initializeUI(availableTitle, selectedTitle);
		setupEventHandlers();
	}

	/** Creates a new dual list selector with available items.
	 * @param availableTitle Title for available items panel
	 * @param selectedTitle  Title for selected items panel
	 * @param items          Available items to choose from */
	public CDualListSelectorComponent(String availableTitle, String selectedTitle, List<T> items) {
		this(availableTitle, selectedTitle);
		setItems(items);
	}

	private void initializeUI(String availableTitle, String selectedTitle) {
		setSpacing(true);
		setPadding(false);
		setWidthFull();
		// Available items section
		H4 availableHeader = new H4(availableTitle != null ? availableTitle : "Available");
		availableList = new ListBox<>();
		availableList.setHeight("250px");
		availableList.setWidthFull();
		// Selected items section
		H4 selectedHeader = new H4(selectedTitle != null ? selectedTitle : "Selected");
		selectedList = new ListBox<>();
		selectedList.setHeight("250px");
		selectedList.setWidthFull();
		// Control buttons
		addButton = new Button("Add →", VaadinIcon.ARROW_RIGHT.create());
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.setEnabled(false);
		removeButton = new Button("← Remove", VaadinIcon.ARROW_LEFT.create());
		removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		removeButton.setEnabled(false);
		// Order buttons
		upButton = new Button("Up", VaadinIcon.ARROW_UP.create());
		upButton.setEnabled(false);
		downButton = new Button("Down", VaadinIcon.ARROW_DOWN.create());
		downButton.setEnabled(false);
		// Layouts
		HorizontalLayout controlButtons = new HorizontalLayout(addButton, removeButton);
		controlButtons.setSpacing(true);
		HorizontalLayout orderButtons = new HorizontalLayout(upButton, downButton);
		orderButtons.setSpacing(true);
		VerticalLayout availableSection = new VerticalLayout(availableHeader, availableList, controlButtons);
		availableSection.setSpacing(false);
		availableSection.setPadding(false);
		availableSection.setWidthFull();
		VerticalLayout selectedSection = new VerticalLayout(selectedHeader, selectedList, orderButtons);
		selectedSection.setSpacing(false);
		selectedSection.setPadding(false);
		selectedSection.setWidthFull();
		HorizontalLayout mainLayout = new HorizontalLayout(availableSection, selectedSection);
		mainLayout.setWidthFull();
		mainLayout.setSpacing(true);
		add(mainLayout);
	}

	private void setupEventHandlers() {
		// Enable/disable buttons based on selection
		availableList.addValueChangeListener(e -> addButton.setEnabled(e.getValue() != null && !readOnly));
		selectedList.addValueChangeListener(e -> {
			boolean hasSelection = e.getValue() != null && !readOnly;
			removeButton.setEnabled(hasSelection);
			upButton.setEnabled(hasSelection);
			downButton.setEnabled(hasSelection);
		});
		// Button actions
		addButton.addClickListener(e -> addSelectedItem());
		removeButton.addClickListener(e -> removeSelectedItem());
		upButton.addClickListener(e -> moveUp());
		downButton.addClickListener(e -> moveDown());
	}

	/** Adds the selected item from availableList to selectedItems. */
	private void addSelectedItem() {
		T selected = availableList.getValue();
		if (selected != null && !selectedItems.contains(selected)) {
			selectedItems.add(selected);
			refreshLists();
			availableList.clear();
		}
	}

	/** Removes the selected item from selectedItems. */
	private void removeSelectedItem() {
		T selected = selectedList.getValue();
		if (selected != null) {
			selectedItems.remove(selected);
			refreshLists();
			selectedList.clear();
		}
	}

	/** Moves the selected item up in the order. */
	private void moveUp() {
		T selected = selectedList.getValue();
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
		T selected = selectedList.getValue();
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
		List<T> available = sourceItems.stream().filter(item -> !selectedItems.contains(item)).collect(Collectors.toList());
		availableList.setItems(available);
		// Update selected list
		selectedList.setItems(selectedItems);
		// Fire value change event
		fireValueChangeEvent();
	}

	/** Fires a value change event to listeners. */
	private void fireValueChangeEvent() {
		Set<T> oldValue = currentValue;
		Set<T> newValue = getValue();
		currentValue = newValue;
		if (!oldValue.equals(newValue)) {
			ValueChangeEvent<Set<T>> event = new ValueChangeEvent<Set<T>>() {

				private static final long serialVersionUID = 1L;

				@Override
				public HasValue<?, Set<T>> getHasValue() { return CDualListSelectorComponent.this; }

				@Override
				public boolean isFromClient() { return true; }

				@Override
				public Set<T> getOldValue() { return oldValue; }

				@Override
				public Set<T> getValue() { return newValue; }
			};
			listeners.forEach(listener -> listener.valueChanged(event));
		}
	}

	// Public API methods
	/** Sets the available items to choose from.
	 * @param items List of items */
	public void setItems(List<T> items) {
		sourceItems.clear();
		if (items != null) {
			sourceItems.addAll(items);
		}
		refreshLists();
	}

	/** Sets the item label generator for displaying items.
	 * @param itemLabelGenerator Function to generate display text for items */
	public void setItemLabelGenerator(ItemLabelGenerator<T> itemLabelGenerator) {
		this.itemLabelGenerator = itemLabelGenerator != null ? itemLabelGenerator : Object::toString;
		availableList.setRenderer(new TextRenderer<>(this.itemLabelGenerator));
		selectedList.setRenderer(new TextRenderer<>(this.itemLabelGenerator));
	}

	/** Returns the currently selected items.
	 * @return List of selected items in order */
	public List<T> getSelectedItems() { return new ArrayList<>(selectedItems); }

	/** Sets the selected items.
	 * @param items List of items to mark as selected */
	public void setSelectedItems(List<T> items) {
		selectedItems.clear();
		if (items != null) {
			selectedItems.addAll(items);
		}
		refreshLists();
	}

	// HasValue implementation
	@Override
	public Set<T> getValue() { return new LinkedHashSet<>(selectedItems); }

	@Override
	public void setValue(Set<T> value) {
		selectedItems.clear();
		if (value != null) {
			selectedItems.addAll(value);
		}
		refreshLists();
	}

	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super ValueChangeEvent<Set<T>>> listener) {
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	@Override
	public boolean isReadOnly() { return readOnly; }

	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		addButton.setEnabled(!readOnly && availableList.getValue() != null);
		removeButton.setEnabled(!readOnly && selectedList.getValue() != null);
		upButton.setEnabled(!readOnly && selectedList.getValue() != null);
		downButton.setEnabled(!readOnly && selectedList.getValue() != null);
		availableList.setReadOnly(readOnly);
		selectedList.setReadOnly(readOnly);
	}

	@Override
	public boolean isEmpty() { return selectedItems.isEmpty(); }

	@Override
	public boolean isRequiredIndicatorVisible() { return false; }

	@Override
	public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
		// Could be implemented if needed
	}

	@Override
	public void clear() {
		selectedItems.clear();
		refreshLists();
	}
}
