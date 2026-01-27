package tech.derbent.api.ui.component.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.ui.component.basic.CComboBox;

/** CAssignedToFilter - Assigned to user/assignment filter component.
 * <p>
 * Provides filtering by item assignment with modes:
 * <ul>
 * <li>All items - Show all items regardless of assignment</li>
 * <li>My items - Show only items assigned to current user</li>
 * </ul>
 * </p>
 */
public class CAssignedToFilter extends CAbstractFilterComponent<CAssignedToFilter.AssignedToFilterMode> {

	/** Assigned to filter mode enum. */
	public enum AssignedToFilterMode {

		ALL("All items"), CURRENT_USER("My items");

		private final String label;

		AssignedToFilterMode(final String label) {
			this.label = label;
		}

		public String getLabel() { return label; }
	}

	public static final String FILTER_KEY = "assignedTo";
	private static final Logger LOGGER = LoggerFactory.getLogger(CAssignedToFilter.class);
	private final CComboBox<AssignedToFilterMode> comboBox;

	/** Creates an assigned to user filter. */
	public CAssignedToFilter() {
		super(FILTER_KEY);
		comboBox = new CComboBox<>("Assigned To");
		comboBox.setItems(AssignedToFilterMode.values());
		comboBox.setItemLabelGenerator(AssignedToFilterMode::getLabel);
		comboBox.setValue(AssignedToFilterMode.ALL);
		// Enable automatic persistence in CComboBox
		comboBox.enablePersistence("assignedToFilter_" + FILTER_KEY, modeName -> {
			// Convert stored enum name back to enum value
			try {
				return AssignedToFilterMode.valueOf(modeName);
			} catch (final IllegalArgumentException e) {
				return AssignedToFilterMode.ALL; // Safe default
			}
		});
		// Notify listeners on value change
		comboBox.addValueChangeListener(event -> {
			final AssignedToFilterMode value = event.getValue() != null ? event.getValue() : AssignedToFilterMode.ALL;
			notifyChangeListeners(value);
		});
	}

	@Override
	public void clearFilter() {
		comboBox.setValue(AssignedToFilterMode.ALL);
	}

	@Override
	protected Component createComponent() {
		return comboBox;
	}

	@Override
	protected void updateComponentValue(final AssignedToFilterMode value) {
		comboBox.setValue(value != null ? value : AssignedToFilterMode.ALL);
	}

	@Override
	public void valuePersist_enable(final String storageId) {
		// Persistence is now handled automatically by CComboBox.enablePersistence()
		// This method remains for interface compatibility but does nothing
		LOGGER.debug("[FilterPersistence] enableValuePersistence called with storageId: {} (CComboBox handles persistence)", storageId);
	}
}
