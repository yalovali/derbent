package tech.derbent.screens.view;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.screens.domain.CGridEntity.FieldSelection;

/** Simple field selection component for selecting and ordering entity fields. Integrates with binders and provides up/down ordering functionality. */
public class CFieldSelectionComponent extends VerticalLayout implements HasValue<HasValue.ValueChangeEvent<String>, String> {

	private static final long serialVersionUID = 1L;
	// UI Components
	private ListBox<EntityFieldInfo> availableFields;
	private ListBox<FieldSelection> selectedFields;
	private Button addButton;
	private Button removeButton;
	private Button upButton;
	private Button downButton;
	// Data
	private List<EntityFieldInfo> sourceList;
	private List<FieldSelection> selections;
	private String currentValue = "";
	private boolean readOnly = false;
	// Event handling
	private final List<ValueChangeListener<? super ValueChangeEvent<String>>> listeners = new ArrayList<>();

	/** Constructor with enhanced binder and property name.
	 * @param title */
	public CFieldSelectionComponent(String title) {
		this.sourceList = new ArrayList<>();
		this.selections = new ArrayList<>();
		initializeUI(title);
		setupEventHandlers();
	}

	private void initializeUI(String title) {
		setSpacing(true);
		setPadding(false);
		// Available fields section
		H4 availableHeader = new H4(title);
		availableFields = new ListBox<>();
		availableFields.setHeight("200px");
		availableFields.setWidth("100%");
		// Selected fields section
		H4 selectedHeader = new H4("Selected Fields");
		selectedFields = new ListBox<>();
		selectedFields.setHeight("200px");
		selectedFields.setWidth("100%");
		// Control buttons
		addButton = new Button("Add →", VaadinIcon.ARROW_RIGHT.create());
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		removeButton = new Button("← Remove", VaadinIcon.ARROW_LEFT.create());
		removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		// Order buttons
		upButton = new Button("Up", VaadinIcon.ARROW_UP.create());
		downButton = new Button("Down", VaadinIcon.ARROW_DOWN.create());
		// Layout
		HorizontalLayout controlButtons = new HorizontalLayout(addButton, removeButton);
		HorizontalLayout orderButtons = new HorizontalLayout(upButton, downButton);
		VerticalLayout availableSection = new VerticalLayout(availableHeader, availableFields, controlButtons);
		availableSection.setSpacing(false);
		availableSection.setPadding(false);
		VerticalLayout selectedSection = new VerticalLayout(selectedHeader, selectedFields, orderButtons);
		selectedSection.setSpacing(false);
		selectedSection.setPadding(false);
		HorizontalLayout mainLayout = new HorizontalLayout(availableSection, selectedSection);
		mainLayout.setWidthFull();
		add(mainLayout);
	}

	private void setupEventHandlers() {
		// Enable/disable buttons based on selection
		availableFields.addValueChangeListener(e -> addButton.setEnabled(e.getValue() != null && !readOnly));
		selectedFields.addValueChangeListener(e -> {
			boolean hasSelection = e.getValue() != null && !readOnly;
			removeButton.setEnabled(hasSelection);
			upButton.setEnabled(hasSelection);
			downButton.setEnabled(hasSelection);
		});
		// Button actions
		addButton.addClickListener(e -> addSelectedField());
		removeButton.addClickListener(e -> removeSelectedField());
		upButton.addClickListener(e -> moveUp());
		downButton.addClickListener(e -> moveDown());
	}

	/** Adds the selected field from availableFields to selections. */
	private void addSelectedField() {
		EntityFieldInfo selected = availableFields.getValue();
		if (selected != null) {
			// Check if already selected
			boolean alreadySelected = selections.stream().anyMatch(fs -> fs.getFieldInfo().getFieldName().equals(selected.getFieldName()));
			if (!alreadySelected) {
				int order = selections.size() + 1;
				selections.add(new FieldSelection(selected, order));
				// Remove from available list
				sourceList.remove(selected);
				refreshSelections();
				refreshAvailableFields();
				availableFields.clear();
			}
		}
	}

	/** Removes the selected field from selections. */
	private void removeSelectedField() {
		FieldSelection selected = selectedFields.getValue();
		if (selected != null) {
			selections.remove(selected);
			// Add back to available list
			sourceList.add(selected.getFieldInfo());
			// Reorder remaining items
			for (int i = 0; i < selections.size(); i++) {
				selections.get(i).setOrder(i + 1);
			}
			refreshSelections();
			refreshAvailableFields();
			selectedFields.clear();
		}
	}

	/** Moves the selected field up in the order. */
	private void moveUp() {
		FieldSelection selected = selectedFields.getValue();
		if (selected != null) {
			int index = selections.indexOf(selected);
			if (index > 0) {
				// Swap with previous
				FieldSelection previous = selections.get(index - 1);
				selections.set(index - 1, selected);
				selections.set(index, previous);
				// Update orders
				selected.setOrder(index);
				previous.setOrder(index + 1);
				refreshSelections();
				selectedFields.setValue(selected);
			}
		}
	}

	/** Moves the selected field down in the order. */
	private void moveDown() {
		FieldSelection selected = selectedFields.getValue();
		if (selected != null) {
			int index = selections.indexOf(selected);
			if (index < selections.size() - 1) {
				// Swap with next
				FieldSelection next = selections.get(index + 1);
				selections.set(index + 1, selected);
				selections.set(index, next);
				// Update orders
				selected.setOrder(index + 2);
				next.setOrder(index + 1);
				refreshSelections();
				selectedFields.setValue(selected);
			}
		}
	}

	/** Refreshes the selectedFields list and fires value change event. */
	private void refreshSelections() {
		selectedFields.setItems(selections);
		fireValueChangeEvent();
	}

	/** Refreshes the availableFields list. */
	private void refreshAvailableFields() {
		availableFields.setItems(sourceList);
	}

	/** Fires a value change event to listeners. */
	private void fireValueChangeEvent() {
		String oldValue = currentValue;
		String newValue = getValue();
		currentValue = newValue;
		ValueChangeEvent<String> event = new ValueChangeEvent<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public HasValue<?, String> getHasValue() { return CFieldSelectionComponent.this; }

			@Override
			public boolean isFromClient() { return true; }

			@Override
			public String getOldValue() { return oldValue; }

			@Override
			public String getValue() { return newValue; }
		};
		listeners.forEach(listener -> listener.valueChanged(event));
	}

	// HasValue implementation
	/** Returns the current value as a string. */
	@Override
	public String getValue() {
		return selections.stream().map(fs -> fs.getFieldInfo().getFieldName() + ":" + fs.getOrder()).collect(Collectors.joining(","));
	}

	/** Sets the value from a string. */
	@Override
	public void setValue(String value) {
		setSelectedFieldsFromString(value);
	}

	/** Adds a value change listener. */
	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super ValueChangeEvent<String>> listener) {
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	@Override
	public boolean isReadOnly() { return readOnly; }

	/** Sets the component to read-only mode. */
	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		addButton.setEnabled(!readOnly && availableFields.getValue() != null);
		removeButton.setEnabled(!readOnly && selectedFields.getValue() != null);
		upButton.setEnabled(!readOnly && selectedFields.getValue() != null);
		downButton.setEnabled(!readOnly && selectedFields.getValue() != null);
	}

	@Override
	public boolean isEmpty() { return selections.isEmpty(); }

	@Override
	public boolean isRequiredIndicatorVisible() { return false; }

	@Override
	public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
		// No-op for simplicity
	}

	// API methods for external usage
	/** Sets the entity type and loads available fields. */
	public void setEntityType(String entityType) {
		if (entityType != null) {
			sourceList = new ArrayList<>(CEntityFieldService.getEntityFields(entityType));
			// Remove any fields that are already selected
			for (FieldSelection selected : selections) {
				sourceList.removeIf(field -> field.getFieldName().equals(selected.getFieldInfo().getFieldName()));
			}
			refreshAvailableFields();
		}
	}

	/** Returns the selected fields as a string. */
	public String getSelectedFieldsAsString() { return getValue(); }

	/** Sets the selected fields from a string value. */
	public void setSelectedFieldsFromString(String value) {
		selections.clear();
		if (value != null && !value.trim().isEmpty()) {
			String[] fieldPairs = value.split(",");
			for (String fieldPair : fieldPairs) {
				String[] parts = fieldPair.trim().split(":");
				if (parts.length == 2) {
					String fieldName = parts[0].trim();
					try {
						int order = Integer.parseInt(parts[1].trim());
						// Find field info in source list (backup list for reconstruction)
						List<EntityFieldInfo> allFields = CEntityFieldService.getEntityFields(getCurrentEntityType());
						EntityFieldInfo fieldInfo = allFields.stream().filter(f -> f.getFieldName().equals(fieldName)).findFirst().orElse(null);
						if (fieldInfo != null) {
							selections.add(new FieldSelection(fieldInfo, order));
						}
					} catch (NumberFormatException e) {
						// Skip invalid entries
					}
				}
			}
			// Sort by order
			selections.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
		}
		// Update available fields to exclude selected ones
		if (sourceList != null) {
			List<EntityFieldInfo> filteredList = new ArrayList<>(sourceList);
			for (FieldSelection selected : selections) {
				filteredList.removeIf(field -> field.getFieldName().equals(selected.getFieldInfo().getFieldName()));
			}
			sourceList = filteredList;
		}
		refreshSelections();
		refreshAvailableFields();
	}

	/** Helper method to get current entity type (for reconstruction). */
	private String getCurrentEntityType() {
		// This is a simple implementation - in a real scenario this might be stored
		return null; // Will be enhanced if needed
	}

	/** Returns the selected fields as a list. */
	public List<FieldSelection> getSelectedFields() { return new ArrayList<>(selections); }
}
