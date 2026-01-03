package tech.derbent.api.ui.component.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import tech.derbent.api.utils.CValueStorageHelper;

/**
 * CResponsibleUserFilter - Responsible user/ownership filter component.
 * <p>
 * Provides filtering by item ownership with modes:
 * <ul>
 * <li>All items - Show all items regardless of owner</li>
 * <li>My items - Show only items assigned to current user</li>
 * </ul>
 * </p>
 */
public class CResponsibleUserFilter extends CAbstractFilterComponent<CResponsibleUserFilter.ResponsibleFilterMode> {

	public static final String FILTER_KEY = "responsibleUser";

	/**
	 * Responsible filter mode enum.
	 */
	public enum ResponsibleFilterMode {

		ALL("All items"), CURRENT_USER("My items");

		private final String label;

		ResponsibleFilterMode(final String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}
	}

	private final ComboBox<ResponsibleFilterMode> comboBox;

	/**
	 * Creates a responsible user filter.
	 */
	public CResponsibleUserFilter() {
		super(FILTER_KEY);
		comboBox = new ComboBox<>("Responsible");
		comboBox.setItems(ResponsibleFilterMode.values());
		comboBox.setItemLabelGenerator(ResponsibleFilterMode::getLabel);
		comboBox.setValue(ResponsibleFilterMode.ALL);
		comboBox.addValueChangeListener(event -> {
			final ResponsibleFilterMode value = event.getValue() != null ? event.getValue() : ResponsibleFilterMode.ALL;
			notifyChangeListeners(value);
		});
	}

	@Override
	protected Component createComponent() {
		return comboBox;
	}

	@Override
	protected void updateComponentValue(final ResponsibleFilterMode value) {
		comboBox.setValue(value != null ? value : ResponsibleFilterMode.ALL);
	}

	@Override
	public void clearFilter() {
		comboBox.setValue(ResponsibleFilterMode.ALL);
	}

	@Override
	public void enableValuePersistence(final String storageId) {
		// Enable persistence for Responsible Mode ComboBox
		CValueStorageHelper.valuePersist_enable(comboBox, storageId + "_" + FILTER_KEY, modeName -> {
			// Converter: find ResponsibleFilterMode by name
			try {
				return ResponsibleFilterMode.valueOf(modeName);
			} catch (@SuppressWarnings("unused") final IllegalArgumentException e) {
				return ResponsibleFilterMode.ALL;
			}
		});
	}
}
