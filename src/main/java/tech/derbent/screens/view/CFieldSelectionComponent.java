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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/** Simple field selection component for selecting and ordering entity fields. Integrates with binders and provides up/down ordering functionality. */
public class CFieldSelectionComponent extends VerticalLayout implements HasValue<HasValue.ValueChangeEvent<String>, String> {

	private static final long serialVersionUID = 1L;

	// Data class for field selections with order
	public static class FieldSelection {

		private final EntityFieldInfo fieldInfo;
		private int order;

		public FieldSelection(EntityFieldInfo fieldInfo, int order) {
			this.fieldInfo = fieldInfo;
			this.order = order;
		}

		public EntityFieldInfo getFieldInfo() { return fieldInfo; }

		public int getOrder() { return order; }

		public void setOrder(int order) { this.order = order; }

		@Override
		public String toString() {
			return fieldInfo.getDisplayName() + " (" + fieldInfo.getFieldName() + ")";
		}
	}

	// Dependencies
	private final CEntityFieldService entityFieldService;
	private final Binder<?> binder;
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

	/** Default constructor for backward compatibility. */
	public CFieldSelectionComponent() {
		this(null, null);
	}

	/** Constructor taking service and binder as required. */
	public CFieldSelectionComponent(CEntityFieldService entityFieldService, Binder<?> binder) {
		this.entityFieldService = entityFieldService;
		this.binder = binder;
		this.sourceList = new ArrayList<>();
		this.selections = new ArrayList<>();
		initializeUI();
		setupEventHandlers();
	}

	/** Constructor with enhanced binder. */
	public CFieldSelectionComponent(CEntityFieldService entityFieldService, CEnhancedBinder<?> binder) {
		this(entityFieldService, (Binder<?>) binder);
	}

	private void initializeUI() {
		setSpacing(true);
		setPadding(false);
		// Available fields section
		H4 availableHeader = new H4("Available Fields");
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

	private void addSelectedField() {
		EntityFieldInfo selected = availableFields.getValue();
		if (selected != null) {
			// Check if already selected
			boolean alreadySelected = selections.stream().anyMatch(fs -> fs.getFieldInfo().getFieldName().equals(selected.getFieldName()));
			if (!alreadySelected) {
				int order = selections.size() + 1;
				selections.add(new FieldSelection(selected, order));
				refreshSelections();
				availableFields.clear();
			}
		}
	}

	private void removeSelectedField() {
		FieldSelection selected = selectedFields.getValue();
		if (selected != null) {
			selections.remove(selected);
			// Reorder remaining items
			for (int i = 0; i < selections.size(); i++) {
				selections.get(i).setOrder(i + 1);
			}
			refreshSelections();
			selectedFields.clear();
		}
	}

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

	private void refreshSelections() {
		selectedFields.setItems(selections);
		fireValueChangeEvent();
	}

	private void fireValueChangeEvent() {
		String oldValue = currentValue;
		String newValue = getValue();
		currentValue = newValue;
		ValueChangeEvent<String> event = new ValueChangeEvent<String>() {

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
	@Override
	public String getValue() {
		return selections.stream().map(fs -> fs.getFieldInfo().getFieldName() + ":" + fs.getOrder()).collect(Collectors.joining(","));
	}

	@Override
	public void setValue(String value) {
		loadFromString(value);
	}

	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super ValueChangeEvent<String>> listener) {
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	@Override
	public boolean isReadOnly() { return readOnly; }

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
	public void setEntityType(String entityType) {
		if (entityType != null) {
			sourceList = CEntityFieldService.getEntityFields(entityType);
			availableFields.setItems(sourceList);
		}
	}

	public void loadFromBinder() {
		if (binder != null) {
			// Load from binder if needed - implementation depends on use case
		}
	}

	public void saveToBinder() {
		if (binder != null) {
			// Save to binder if needed - implementation depends on use case
		}
	}

	public void loadFromString(String value) {
		selections.clear();
		if (value != null && !value.trim().isEmpty()) {
			String[] fieldPairs = value.split(",");
			for (String fieldPair : fieldPairs) {
				String[] parts = fieldPair.trim().split(":");
				if (parts.length == 2) {
					String fieldName = parts[0].trim();
					try {
						int order = Integer.parseInt(parts[1].trim());
						// Find field info in source list
						EntityFieldInfo fieldInfo = sourceList.stream().filter(f -> f.getFieldName().equals(fieldName)).findFirst().orElse(null);
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
		refreshSelections();
	}

	public String getSelectedFieldsAsString() { return getValue(); }

	public void setSelectedFieldsFromString(String value) {
		loadFromString(value);
	}

	public List<FieldSelection> getSelectedFields() { return new ArrayList<>(selections); }
}
