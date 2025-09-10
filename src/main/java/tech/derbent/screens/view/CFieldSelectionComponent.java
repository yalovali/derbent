package tech.derbent.screens.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/** Component for selecting and ordering entity fields for grid display. Allows users to select fields from available entity fields and order them
 * using up/down buttons. The selection is stored as a comma-separated string with order information. */
public class CFieldSelectionComponent extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	/** Data class to hold field information with order */
	public static class FieldSelection {

		private EntityFieldInfo fieldInfo;
		private int order;

		public FieldSelection(EntityFieldInfo fieldInfo, int order) {
			this.fieldInfo = fieldInfo;
			this.order = order;
		}

		public EntityFieldInfo getFieldInfo() { return fieldInfo; }

		public void setFieldInfo(EntityFieldInfo fieldInfo) { this.fieldInfo = fieldInfo; }

		public int getOrder() { return order; }

		public void setOrder(int order) { this.order = order; }

		@Override
		public String toString() {
			return fieldInfo.getDisplayName() + " (" + fieldInfo.getFieldName() + ")";
		}
	}

	private ListBox<EntityFieldInfo> availableFieldsListBox;
	private ListBox<FieldSelection> selectedFieldsListBox;
	private Button addButton;
	private Button removeButton;
	private Button moveUpButton;
	private Button moveDownButton;
	private String entityTypeName;
	private List<EntityFieldInfo> availableFields;
	private List<FieldSelection> selectedFields;

	public CFieldSelectionComponent() {
		initializeComponent();
	}

	private void initializeComponent() {
		setSpacing(true);
		setPadding(false);
		// Create available fields section
		H4 availableHeader = new H4("Available Fields");
		availableFieldsListBox = new ListBox<>();
		availableFieldsListBox.setHeight("200px");
		availableFieldsListBox.setWidth("100%");
		availableFieldsListBox.setRenderer(new ComponentRenderer<>(
				field -> new com.vaadin.flow.component.html.Span(field.getDisplayName() + " (" + field.getFieldName() + ")")));
		// Create selected fields section
		H4 selectedHeader = new H4("Selected Fields (in order)");
		selectedFieldsListBox = new ListBox<>();
		selectedFieldsListBox.setHeight("200px");
		selectedFieldsListBox.setWidth("100%");
		selectedFieldsListBox
				.setRenderer(new ComponentRenderer<>(fieldSelection -> new com.vaadin.flow.component.html.Span(fieldSelection.toString())));
		// Create control buttons
		HorizontalLayout controlButtons = createControlButtons();
		HorizontalLayout orderButtons = createOrderButtons();
		// Layout
		VerticalLayout availableSection = new VerticalLayout(availableHeader, availableFieldsListBox, controlButtons);
		availableSection.setSpacing(false);
		availableSection.setPadding(false);
		VerticalLayout selectedSection = new VerticalLayout(selectedHeader, selectedFieldsListBox, orderButtons);
		selectedSection.setSpacing(false);
		selectedSection.setPadding(false);
		HorizontalLayout mainLayout = new HorizontalLayout(availableSection, selectedSection);
		mainLayout.setWidthFull();
		add(mainLayout);
		// Initialize data
		selectedFields = new ArrayList<>();
		setupEventHandlers();
	}

	private HorizontalLayout createControlButtons() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		addButton = new Button("Add →", VaadinIcon.ARROW_RIGHT.create());
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.setEnabled(false);
		removeButton = new Button("← Remove", VaadinIcon.ARROW_LEFT.create());
		removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		removeButton.setEnabled(false);
		layout.add(addButton, removeButton);
		return layout;
	}

	private HorizontalLayout createOrderButtons() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		moveUpButton = new Button("Move Up", VaadinIcon.ARROW_UP.create());
		moveUpButton.setEnabled(false);
		moveDownButton = new Button("Move Down", VaadinIcon.ARROW_DOWN.create());
		moveDownButton.setEnabled(false);
		layout.add(moveUpButton, moveDownButton);
		return layout;
	}

	private void setupEventHandlers() {
		// Available fields selection
		availableFieldsListBox.addValueChangeListener(e -> {
			addButton.setEnabled(e.getValue() != null);
		});
		// Selected fields selection
		selectedFieldsListBox.addValueChangeListener(e -> {
			boolean hasSelection = e.getValue() != null;
			removeButton.setEnabled(hasSelection);
			moveUpButton.setEnabled(hasSelection);
			moveDownButton.setEnabled(hasSelection);
		});
		// Button actions
		addButton.addClickListener(e -> addSelectedField());
		removeButton.addClickListener(e -> removeSelectedField());
		moveUpButton.addClickListener(e -> moveSelectedFieldUp());
		moveDownButton.addClickListener(e -> moveSelectedFieldDown());
	}

	private void addSelectedField() {
		EntityFieldInfo selected = availableFieldsListBox.getValue();
		if (selected != null) {
			// Check if already selected
			boolean alreadySelected = selectedFields.stream().anyMatch(fs -> fs.getFieldInfo().getFieldName().equals(selected.getFieldName()));
			if (alreadySelected) {
				Notification.show("Field is already selected", 2000, Notification.Position.MIDDLE);
				return;
			}
			int order = selectedFields.size() + 1;
			FieldSelection fieldSelection = new FieldSelection(selected, order);
			selectedFields.add(fieldSelection);
			refreshSelectedFieldsList();
			availableFieldsListBox.clear();
		}
	}

	private void removeSelectedField() {
		FieldSelection selected = selectedFieldsListBox.getValue();
		if (selected != null) {
			selectedFields.remove(selected);
			// Reorder remaining fields
			for (int i = 0; i < selectedFields.size(); i++) {
				selectedFields.get(i).setOrder(i + 1);
			}
			refreshSelectedFieldsList();
			selectedFieldsListBox.clear();
		}
	}

	private void moveSelectedFieldUp() {
		FieldSelection selected = selectedFieldsListBox.getValue();
		if (selected != null) {
			int index = selectedFields.indexOf(selected);
			if (index > 0) {
				// Swap with previous
				FieldSelection previous = selectedFields.get(index - 1);
				selectedFields.set(index - 1, selected);
				selectedFields.set(index, previous);
				// Update order values
				selected.setOrder(index);
				previous.setOrder(index + 1);
				refreshSelectedFieldsList();
				selectedFieldsListBox.setValue(selected); // Keep selection
			}
		}
	}

	private void moveSelectedFieldDown() {
		FieldSelection selected = selectedFieldsListBox.getValue();
		if (selected != null) {
			int index = selectedFields.indexOf(selected);
			if (index < selectedFields.size() - 1) {
				// Swap with next
				FieldSelection next = selectedFields.get(index + 1);
				selectedFields.set(index + 1, selected);
				selectedFields.set(index, next);
				// Update order values
				selected.setOrder(index + 2);
				next.setOrder(index + 1);
				refreshSelectedFieldsList();
				selectedFieldsListBox.setValue(selected); // Keep selection
			}
		}
	}

	private void refreshSelectedFieldsList() {
		selectedFieldsListBox.setItems(selectedFields);
	}

	/** Set the entity type to load available fields */
	public void setEntityType(String entityTypeName) {
		Check.notBlank(entityTypeName, "Entity type name cannot be blank");
		this.entityTypeName = entityTypeName;
		loadAvailableFields();
	}

	private void loadAvailableFields() {
		if (entityTypeName != null) {
			try {
				availableFields = CEntityFieldService.getEntityFields(entityTypeName);
				availableFieldsListBox.setItems(availableFields);
			} catch (Exception e) {
				Notification.show("Error loading fields for entity: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
				availableFields = new ArrayList<>();
				availableFieldsListBox.setItems(availableFields);
			}
		}
	}

	/** Get the selected fields as a comma-separated string */
	public String getSelectedFieldsAsString() {
		return selectedFields.stream().map(fs -> fs.getFieldInfo().getFieldName() + ":" + fs.getOrder()).collect(Collectors.joining(","));
	}

	/** Set the selected fields from a comma-separated string */
	public void setSelectedFieldsFromString(String selectedFieldsString) {
		selectedFields.clear();
		if (selectedFieldsString != null && !selectedFieldsString.trim().isEmpty()) {
			String[] fieldPairs = selectedFieldsString.split(",");
			for (String fieldPair : fieldPairs) {
				String[] parts = fieldPair.trim().split(":");
				if (parts.length == 2) {
					String fieldName = parts[0].trim();
					try {
						int order = Integer.parseInt(parts[1].trim());
						// Find the field info
						EntityFieldInfo fieldInfo = availableFields.stream().filter(f -> f.getFieldName().equals(fieldName)).findFirst().orElse(null);
						if (fieldInfo != null) {
							selectedFields.add(new FieldSelection(fieldInfo, order));
						}
					} catch (NumberFormatException e) {
						// Skip invalid order values
					}
				}
			}
			// Sort by order
			selectedFields.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
		}
		refreshSelectedFieldsList();
	}

	/** Get the list of selected field selections */
	public List<FieldSelection> getSelectedFields() { return new ArrayList<>(selectedFields); }
}
