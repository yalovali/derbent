package tech.derbent.api.screens.view;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.screens.domain.CGridEntity.FieldSelection;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;

/** Field selection component for selecting and ordering entity fields. This component uses CDualListSelectorComponent and provides specialized
 * handling for FieldSelection objects used in grid configuration. Integrates with binders and provides List<String> value. */
public class CFieldSelectionComponent extends VerticalLayout implements HasValue<HasValue.ValueChangeEvent<List<String>>, List<String>> {

	private static final long serialVersionUID = 1L;
	private List<String> currentValue = new ArrayList<>();
	private final CDualListSelectorComponent<EntityFieldInfo> dualListSelector;
	private String entityType;
	private final List<ValueChangeListener<? super ValueChangeEvent<List<String>>>> listeners = new ArrayList<>();
	private List<FieldSelection> selections = new ArrayList<>();

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

	/** Adds a value change listener for List<String> values. */
	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super ValueChangeEvent<List<String>>> listener) {
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	/** Fires a value change event to listeners. */
	private void fireValueChangeEvent() {
		final List<String> oldValue = new ArrayList<>(currentValue);
		final List<String> newValue = getValue();
		currentValue = new ArrayList<>(newValue);
		if (!oldValue.equals(newValue)) {
			final ValueChangeEvent<List<String>> event = new ValueChangeEvent<List<String>>() {

				private static final long serialVersionUID = 1L;

				@Override
				public HasValue<?, List<String>> getHasValue() { return CFieldSelectionComponent.this; }

				@Override
				public List<String> getOldValue() { return oldValue; }

				@Override
				public List<String> getValue() { return newValue; }

				@Override
				public boolean isFromClient() { return true; }
			};
			listeners.forEach(listener -> listener.valueChanged(event));
		}
	}

	/** Helper method to get current entity type (for reconstruction). */
	private String getCurrentEntityType() { return entityType; }

	/** Returns the selected fields as a list of FieldSelection objects. */
	public List<FieldSelection> getSelectedFields() {
		return new ArrayList<>(selections);
	}

	/** Returns the selected fields as a list of strings. */
	public List<String> getSelectedFieldsAsString() { return getValue(); }

	// HasValue<List<String>> implementation
	/** Returns the current value as a list of field names. */
	@Override
	public List<String> getValue() { return selections.stream().map(fs -> fs.getFieldInfo().getFieldName()).collect(Collectors.toList()); }

	@Override
	public boolean isEmpty() { return selections.isEmpty(); }

	@Override
	public boolean isReadOnly() { return dualListSelector.isReadOnly(); }

	@Override
	public boolean isRequiredIndicatorVisible() { return false; }

	/** Called when the underlying dual list selector selection changes. Updates the FieldSelection list. */
	private void onSelectionChanged( Set<EntityFieldInfo> selectedFields) {
		// Rebuild selections list from current order
		final List<EntityFieldInfo> orderedFields = dualListSelector.getSelectedItems();
		selections.clear();
		for (int i = 0; i < orderedFields.size(); i++) {
			selections.add(new FieldSelection(orderedFields.get(i), i + 1));
		}
		// Fire value change event
		fireValueChangeEvent();
	}

	/** Sets the entity type and loads available fields. */
	private void refreshSourceList(String entityType1) {
		if (entityType1 == null) {
			return;
		}
		final List<EntityFieldInfo> allFields = CEntityFieldService.getEntityFields(entityType1);
		dualListSelector.setItems(allFields);
	}

	/** Sets the selected fields from a string value (fieldName:order,fieldName:order,...). */
	public void setColumnFieldsFromString(List<String> currentSelections) {
		selections.clear();
		if (currentSelections == null || currentSelections.isEmpty()) {
			// Clear the underlying selector
			dualListSelector.setValue(new LinkedHashSet<>());
			return;
		}
		// Parse the string and build FieldSelection list
		final List<FieldSelection> tempSelections = new ArrayList<>();
		int order = 0;
		for (final String fieldName : currentSelections) {
			try {
				// Find field info from all available fields
				final List<EntityFieldInfo> allFields = CEntityFieldService.getEntityFields(entityType != null ? entityType : getCurrentEntityType());
				final EntityFieldInfo fieldInfo = allFields.stream().filter(f -> f.getFieldName().equals(fieldName)).findFirst().orElse(null);
				if (fieldInfo != null) {
					tempSelections.add(new FieldSelection(fieldInfo, order));
				}
				order++;
			} catch ( final NumberFormatException e) {
				// Skip invalid entries
			}
		}
		// Sort by order
		tempSelections.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
		selections.addAll(tempSelections);
		// Update the underlying dual list selector
		final List<EntityFieldInfo> selectedFieldInfos = tempSelections.stream().map(FieldSelection::getFieldInfo).collect(Collectors.toList());
		dualListSelector.setSelectedItems(selectedFieldInfos);
	}

	/** Sets the entity type and reloads available fields. */
	public void setEntityType(String entityType) {
		this.entityType = entityType;
		refreshSourceList(entityType);
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		dualListSelector.setReadOnly(readOnly);
	}

	@Override
	public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
		// No-op for simplicity
	}

	/** Sets the value from a list of field names. */
	@Override
	public void setValue(List<String> value) {
		setColumnFieldsFromString(value);
	}
}
