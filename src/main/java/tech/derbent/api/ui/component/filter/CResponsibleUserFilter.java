package tech.derbent.api.ui.component.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.ui.component.basic.CComboBox;

/** CResponsibleUserFilter - Responsible user/ownership filter component.
 * <p>
 * Provides filtering by item ownership with modes:
 * <ul>
 * <li>All items - Show all items regardless of owner</li>
 * <li>My items - Show only items assigned to current user</li>
 * </ul>
 * </p>
 */
public class CResponsibleUserFilter extends CAbstractFilterComponent<CResponsibleUserFilter.ResponsibleFilterMode> {

	/** Responsible filter mode enum. */
	public enum ResponsibleFilterMode {

		ALL("All items"), CURRENT_USER("My items");

		private final String label;

		ResponsibleFilterMode(final String label) {
			this.label = label;
		}

		public String getLabel() { return label; }
	}

	public static final String FILTER_KEY = "responsibleUser";
	private static final Logger LOGGER = LoggerFactory.getLogger(CResponsibleUserFilter.class);
	private final CComboBox<ResponsibleFilterMode> comboBox;

	/** Creates a responsible user filter. */
	
	public CResponsibleUserFilter() {
		super(FILTER_KEY);
		comboBox = new CComboBox<>("Responsible");
		comboBox.setItems(ResponsibleFilterMode.values());
		comboBox.setItemLabelGenerator(ResponsibleFilterMode::getLabel);
		comboBox.setValue(ResponsibleFilterMode.ALL);
		// Enable automatic persistence in CComboBox
		comboBox.enablePersistence("responsibleUserFilter_" + FILTER_KEY, modeName -> {
			// Convert stored enum name back to enum value
			try {
				return ResponsibleFilterMode.valueOf(modeName);
			} catch (final IllegalArgumentException e) {
				return ResponsibleFilterMode.ALL; // Safe default
			}
		});
		// Notify listeners on value change
		comboBox.addValueChangeListener(event -> {
			final ResponsibleFilterMode value = event.getValue() != null ? event.getValue() : ResponsibleFilterMode.ALL;
			notifyChangeListeners(value);
		});
	}

	@Override
	public void clearFilter() {
		comboBox.setValue(ResponsibleFilterMode.ALL);
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
	public void valuePersist_enable(final String storageId) {
		// Persistence is now handled automatically by CComboBox.enablePersistence()
		// This method remains for interface compatibility but does nothing
		LOGGER.debug("[FilterPersistence] enableValuePersistence called with storageId: {} (CComboBox handles persistence)", storageId);
	}
}
