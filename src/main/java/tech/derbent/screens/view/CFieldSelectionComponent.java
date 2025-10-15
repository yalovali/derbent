package tech.derbent.screens.view;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import tech.derbent.screens.domain.CGridEntity.FieldSelection;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/** Field selection component for selecting and ordering entity fields. This component uses CDualListSelectorComponent and provides specialized
 * handling for FieldSelection objects used in grid configuration. Integrates with binders and provides string-based value for backwards
 * compatibility. */
public class CFieldSelectionComponent extends VerticalLayout implements HasValue<HasValue.ValueChangeEvent<String>, String> {

	private static final long serialVersionUID = 1L;
	private final CDualListSelectorComponent<EntityFieldInfo> dualListSelector;
	private String entityType;
	private List<FieldSelection> selections = new ArrayList<>();
	private String currentValue = "";
	private final List<ValueChangeListener<? super ValueChangeEvent<String>>> listeners = new ArrayList<>();

	/** Constructor with enhanced binder and property name.
	 * @param title      The title for the component
	 * @param entityType The entity type to show fields for */
	public CFieldSelectionComponent(String title, String entityType) {
		this.entityType = entityType;
		this.selections = new ArrayList<>();
		// Create the dual list selector
		dualListSelector = new CDualListSelectorComponent<>(title, "Selected Fields");
		// Set display function for EntityFieldInfo
		dualListSelector.setItemLabelGenerator(fieldInfo -> fieldInfo.getDisplayName() + " (" + fieldInfo.getFieldName() + ")");
		// Load available fields
		refreshSourceList(entityType);
		// Listen to changes in the underlying dual list selector
		dualListSelector.addValueChangeListener(e -> onSelectionChanged(e.getValue()));
		// Add to layout
		setSpacing(false);
		setPadding(false);
		add(dualListSelector);
	}

	/** Sets the entity type and loads available fields. */
	private void refreshSourceList(String entityType) {
		if (entityType == null) {
			return;
		}
		List<EntityFieldInfo> allFields = CEntityFieldService.getEntityFields(entityType);
		dualListSelector.setItems(allFields);
	}

	/** Called when the underlying dual list selector selection changes. Updates the FieldSelection list. */
	private void onSelectionChanged(Set<EntityFieldInfo> selectedFields) {
		// Rebuild selections list from current order
		List<EntityFieldInfo> orderedFields = dualListSelector.getSelectedItems();
		selections.clear();
		for (int i = 0; i < orderedFields.size(); i++) {
			selections.add(new FieldSelection(orderedFields.get(i), i + 1));
		}
		// Fire string value change event for legacy compatibility
		fireStringValueChangeEvent();
	}

	/** Fires a value change event to string listeners for legacy compatibility. */
	private void fireStringValueChangeEvent() {
		String oldValue = currentValue;
		String newValue = getValue();
		currentValue = newValue;
		if (!oldValue.equals(newValue)) {
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
	}

	// HasValue<String> implementation for legacy compatibility
	/** Returns the current value as a string (fieldName:order,fieldName:order,...). */
	@Override
	public String getValue() {
		return selections.stream().map(fs -> fs.getFieldInfo().getFieldName() + ":" + fs.getOrder()).collect(Collectors.joining(","));
	}

	/** Sets the value from a string. */
	@Override
	public void setValue(String value) {
		setSelectedFieldsFromString(value);
	}

	/** Adds a value change listener for string values. */
	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super ValueChangeEvent<String>> listener) {
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	@Override
	public boolean isReadOnly() { return dualListSelector.isReadOnly(); }

	@Override
	public void setReadOnly(boolean readOnly) {
		dualListSelector.setReadOnly(readOnly);
	}

	@Override
	public boolean isEmpty() { return selections.isEmpty(); }

	@Override
	public boolean isRequiredIndicatorVisible() { return false; }

	@Override
	public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
		// No-op for simplicity
	}

	/** Returns the selected fields as a string. */
	public String getSelectedFieldsAsString() { return getValue(); }

	/** Sets the selected fields from a string value (fieldName:order,fieldName:order,...). */
	public void setSelectedFieldsFromString(String value) {
		selections.clear();
		if (value == null || value.trim().isEmpty()) {
			// Clear the underlying selector
			dualListSelector.setValue(new LinkedHashSet<>());
			return;
		}
		// Parse the string and build FieldSelection list
		List<FieldSelection> tempSelections = new ArrayList<>();
		String[] fieldPairs = value.split(",");
		for (String fieldPair : fieldPairs) {
			String[] parts = fieldPair.trim().split(":");
			if (parts.length == 2) {
				String fieldName = parts[0].trim();
				try {
					int order = Integer.parseInt(parts[1].trim());
					// Find field info from all available fields
					List<EntityFieldInfo> allFields = CEntityFieldService.getEntityFields(entityType != null ? entityType : getCurrentEntityType());
					EntityFieldInfo fieldInfo = allFields.stream().filter(f -> f.getFieldName().equals(fieldName)).findFirst().orElse(null);
					if (fieldInfo != null) {
						tempSelections.add(new FieldSelection(fieldInfo, order));
					}
				} catch (NumberFormatException e) {
					// Skip invalid entries
				}
			}
		}
		// Sort by order
		tempSelections.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
		selections.addAll(tempSelections);
		// Update the underlying dual list selector
		List<EntityFieldInfo> selectedFieldInfos = tempSelections.stream().map(FieldSelection::getFieldInfo).collect(Collectors.toList());
		dualListSelector.setSelectedItems(selectedFieldInfos);
	}

	/** Helper method to get current entity type (for reconstruction). */
	private String getCurrentEntityType() { return entityType; }

	/** Returns the selected fields as a list of FieldSelection objects. */
	public List<FieldSelection> getSelectedFields() {
		return new ArrayList<>(selections);
	}

	/** Sets the entity type and reloads available fields. */
	public void setEntityType(String entityType) {
		this.entityType = entityType;
		refreshSourceList(entityType);
	}
}
