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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/** Component for selecting and ordering entity fields for grid display. Allows users to select fields from available entity fields and order them
 * using up/down buttons. The selection is stored as a comma-separated string with order information. */
public class CFieldSelectionComponent extends VerticalLayout implements HasValue<HasValue.ValueChangeEvent<String>, String> {
	/** Data class to hold field information with order */
	public static class FieldSelection {
		private EntityFieldInfo fieldInfo;
		private int order;

		public FieldSelection(final EntityFieldInfo fieldInfo, final int order) {
			this.fieldInfo = fieldInfo;
			this.order = order;
		}

		public EntityFieldInfo getFieldInfo() { return fieldInfo; }

		public int getOrder() { return order; }

		public void setFieldInfo(final EntityFieldInfo fieldInfo) { this.fieldInfo = fieldInfo; }

		public void setOrder(final int order) { this.order = order; }

		@Override
		public String toString() {
			return fieldInfo.getDisplayName() + " (" + fieldInfo.getFieldName() + ")";
		}
	}

	public static class ValueChangeEvent extends ComponentEvent<CFieldSelectionComponent> {
		private final String oldValue;
		private final String newValue;
		private final boolean fromClient;

		public ValueChangeEvent(CFieldSelectionComponent source, String oldValue, String newValue, boolean fromClient) {
			super(source, fromClient);
			this.oldValue = oldValue;
			this.newValue = newValue;
			this.fromClient = fromClient;
		}

		public String getOldValue() { return oldValue; }
		public String getNewValue() { return newValue; }
		public boolean isFromClient() { return fromClient; }
	}

	private static final long serialVersionUID = 1L;
	private ListBox<EntityFieldInfo> availableFieldsListBox;
	private ListBox<FieldSelection> selectedFieldsListBox;
	private Button addButton;
	private Button removeButton;
	private Button moveUpButton;
	private Button moveDownButton;
	private String entityTypeName;
	private List<EntityFieldInfo> availableFields;
	private List<FieldSelection> selectedFields;
	private String value = "";
	private boolean readOnly = false;
	private boolean requiredIndicatorVisible = false;
	private final List<ComponentEventListener<ValueChangeEvent>> listeners = new ArrayList<>();
	private final List<HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<String>>> hvListeners = new ArrayList<>();

	public CFieldSelectionComponent() {
		setSpacing(true);
		setPadding(false);
		initializeComponent();
	}

	private void addSelectedField() {
		final EntityFieldInfo selected = availableFieldsListBox.getValue();
		if (selected != null) {
			// Check if already selected
			final boolean alreadySelected = selectedFields.stream().anyMatch(fs -> fs.getFieldInfo().getFieldName().equals(selected.getFieldName()));
			if (alreadySelected) {
				Notification.show("Field is already selected", 2000, Notification.Position.MIDDLE);
				return;
			}
			final int order = selectedFields.size() + 1;
			final FieldSelection fieldSelection = new FieldSelection(selected, order);
			selectedFields.add(fieldSelection);
			refreshSelectedFieldsList();
			availableFieldsListBox.clear();
		}
	}

	@Override
	public Registration addValueChangeListener(final ComponentEventListener<ValueChangeEvent> listener) {
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	@Override
	public Registration addValueChangeListener(HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<String>> listener) {
		hvListeners.add(listener);
		return () -> hvListeners.remove(listener);
	}

	private void fireValueChangeEvent(final boolean fromClient) {
		String oldValue = value;
		String newValue = getSelectedFieldsAsString();
		value = newValue;
		final ValueChangeEvent event = new ValueChangeEvent(this, oldValue, newValue, fromClient);
		for (final ComponentEventListener<ValueChangeEvent> listener : listeners) {
			listener.onComponentEvent(event);
		}
		final HasValue.ValueChangeEvent<String> hvEvent = new HasValue.ValueChangeEvent<>(this, oldValue, newValue, fromClient);
		for (final HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<String>> listener : hvListeners) {
			listener.valueChanged(hvEvent);
		}
	}

	private void refreshSelectedFieldsList() {
		selectedFieldsListBox.setItems(selectedFields);
		fireValueChangeEvent(true);
	}

	private void removeSelectedField() {
		final FieldSelection selected = selectedFieldsListBox.getValue();
		if (selected != null) {
			selectedFields.remove(selected);
			for (int i = 0; i < selectedFields.size(); i++) {
				selectedFields.get(i).setOrder(i + 1);
			}
			refreshSelectedFieldsList();
			selectedFieldsListBox.clear();
		}
	}

	private void moveSelectedFieldUp() {
		final FieldSelection selected = selectedFieldsListBox.getValue();
		if (selected != null) {
			final int index = selectedFields.indexOf(selected);
			if (index > 0) {
				final FieldSelection previous = selectedFields.get(index - 1);
				selectedFields.set(index - 1, selected);
				selectedFields.set(index, previous);
				selected.setOrder(index);
				previous.setOrder(index + 1);
				refreshSelectedFieldsList();
				selectedFieldsListBox.setValue(selected);
			}
		}
	}

	private void moveSelectedFieldDown() {
		final FieldSelection selected = selectedFieldsListBox.getValue();
		if (selected != null) {
			final int index = selectedFields.indexOf(selected);
			if (index < (selectedFields.size() - 1)) {
				final FieldSelection next = selectedFields.get(index + 1);
				selectedFields.set(index + 1, selected);
				selectedFields.set(index, next);
				selected.setOrder(index + 2);
				next.setOrder(index + 1);
				refreshSelectedFieldsList();
				selectedFieldsListBox.setValue(selected);
			}
		}
	}

	private HorizontalLayout createControlButtons() {
		final HorizontalLayout layout = new HorizontalLayout();
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
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		moveUpButton = new Button("Move Up", VaadinIcon.ARROW_UP.create());
		moveUpButton.setEnabled(false);
		moveDownButton = new Button("Move Down", VaadinIcon.ARROW_DOWN.create());
		moveDownButton.setEnabled(false);
		layout.add(moveUpButton, moveDownButton);
		return layout;
	}

	private void initializeComponent() {
		// Create available fields section
		final H4 availableHeader = new H4("Available Fields");
		availableFieldsListBox = new ListBox<>();
		availableFieldsListBox.setHeight("200px");
		availableFieldsListBox.setWidth("100%");
		availableFieldsListBox.setRenderer(new ComponentRenderer<>(
				field -> new com.vaadin.flow.component.html.Span(field.getDisplayName() + " (" + field.getFieldName() + ")")));
		// Create selected fields section
		final H4 selectedHeader = new H4("Selected Fields (in order)");
		selectedFieldsListBox = new ListBox<>();
		selectedFieldsListBox.setHeight("200px");
		selectedFieldsListBox.setWidth("100%");
		selectedFieldsListBox
				.setRenderer(new ComponentRenderer<>(fieldSelection -> new com.vaadin.flow.component.html.Span(fieldSelection.toString())));
		// Create control buttons
		final HorizontalLayout controlButtons = createControlButtons();
		final HorizontalLayout orderButtons = createOrderButtons();
		// Layout
		final VerticalLayout availableSection = new VerticalLayout(availableHeader, availableFieldsListBox, controlButtons);
		availableSection.setSpacing(false);
		availableSection.setPadding(false);
		final VerticalLayout selectedSection = new VerticalLayout(selectedHeader, selectedFieldsListBox, orderButtons);
		selectedSection.setSpacing(false);
		selectedSection.setPadding(false);
		final HorizontalLayout mainLayout = new HorizontalLayout(availableSection, selectedSection);
		mainLayout.setWidthFull();
		add(mainLayout);
		// Initialize data
		selectedFields = new ArrayList<>();
		setupEventHandlers();
	}

	private void setupEventHandlers() {
		// Available fields selection
		availableFieldsListBox.addValueChangeListener(e -> {
			addButton.setEnabled(e.getValue() != null);
		});
		// Selected fields selection
		selectedFieldsListBox.addValueChangeListener(e -> {
			final boolean hasSelection = e.getValue() != null;
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

	/** Get the list of selected field selections */
	public List<FieldSelection> getSelectedFields() { return new ArrayList<>(selectedFields); }

	/** Get the selected fields as a comma-separated string */
	public String getSelectedFieldsAsString() {
		return selectedFields.stream().map(fs -> fs.getFieldInfo().getFieldName() + ":" + fs.getOrder()).collect(Collectors.joining(","));
	}

	@Override
	public String getValue() { return getSelectedFieldsAsString(); }

	/** Set the selected fields from a comma-separated string */
	public void setSelectedFieldsFromString(final String selectedFieldsString) {
		selectedFields.clear();
		if ((selectedFieldsString != null) && !selectedFieldsString.trim().isEmpty()) {
			final String[] fieldPairs = selectedFieldsString.split(",");
			for (final String fieldPair : fieldPairs) {
				final String[] parts = fieldPair.trim().split(":");
				if (parts.length == 2) {
					final String fieldName = parts[0].trim();
					try {
						final int order = Integer.parseInt(parts[1].trim());
						// Find the field info
						final EntityFieldInfo fieldInfo =
								availableFields.stream().filter(f -> f.getFieldName().equals(fieldName)).findFirst().orElse(null);
						if (fieldInfo != null) {
							selectedFields.add(new FieldSelection(fieldInfo, order));
						}
					} catch (final NumberFormatException e) {
						// Skip invalid order values
					}
				}
			}
			// Sort by order
			selectedFields.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
		}
		refreshSelectedFieldsList();
	}

	@Override
	public void setValue(final String value) {
		setSelectedFieldsFromString(value);
		this.value = getSelectedFieldsAsString();
		fireValueChangeEvent(false);
	}

	@Override
	public boolean isEmpty() { return (getValue() == null) || getValue().isEmpty(); }

	@Override
	public boolean isReadOnly() { return readOnly; }

	@Override
	public boolean isRequiredIndicatorVisible() { return requiredIndicatorVisible; }

	@Override
	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
		addButton.setEnabled(!readOnly);
		removeButton.setEnabled(!readOnly);
		moveUpButton.setEnabled(!readOnly);
		moveDownButton.setEnabled(!readOnly);
	}

	@Override
	public void setRequiredIndicatorVisible(final boolean requiredIndicatorVisible) { this.requiredIndicatorVisible = requiredIndicatorVisible; }
}