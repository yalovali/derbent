package tech.derbent.api.ui.component.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import tech.derbent.api.ui.component.basic.CComboBox;

/** CKanbanSprintMembershipFilter — filters Status Board items by sprint membership.
 * <p>
 * Only meaningful in Status Board mode. Options:
 * <ul>
 * <li><strong>ALL</strong> — show every project item regardless of sprint assignment</li>
 * <li><strong>IN_SPRINT</strong> — show only items currently assigned to a sprint</li>
 * <li><strong>BACKLOG_ONLY</strong> — show only items not assigned to any sprint</li>
 * </ul> */
public class CKanbanSprintMembershipFilter extends CAbstractFilterComponent<CKanbanSprintMembershipFilter.MembershipMode> {

	/** Sprint membership options for Status Board filtering. */
	public enum MembershipMode {
		ALL("All items"),
		IN_SPRINT("In sprint"),
		BACKLOG_ONLY("Backlog only");

		private final String label;

		MembershipMode(final String label) { this.label = label; }

		public String getLabel() { return label; }
	}

	public static final String FILTER_KEY = "sprintMembership";

	private final CComboBox<MembershipMode> comboBox;

	public CKanbanSprintMembershipFilter() {
		super(FILTER_KEY);
		comboBox = new CComboBox<>("Scope");
		comboBox.setWidth("140px");
		comboBox.getStyle().set("min-width", "0");
		comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
		comboBox.setItems(MembershipMode.values());
		comboBox.setItemLabelGenerator(MembershipMode::getLabel);
		comboBox.setValue(MembershipMode.ALL);
		comboBox.setAllowCustomValue(false);
		comboBox.addValueChangeListener(
				event -> notifyChangeListeners(event.getValue() != null ? event.getValue() : MembershipMode.ALL));
	}

	@Override
	public void clearFilter() {
		comboBox.setValue(MembershipMode.ALL);
	}

	@Override
	protected Component createComponent() {
		return comboBox;
	}

	@Override
	protected void updateComponentValue(final MembershipMode value) {
		comboBox.setValue(value != null ? value : MembershipMode.ALL);
	}

	/** Shows or hides this filter based on the current board mode. */
	public void setVisible(final boolean visible) {
		comboBox.setVisible(visible);
	}

	@Override
	public void valuePersist_enable(final String storageId) {
		comboBox.enablePersistence(storageId + "_" + FILTER_KEY,
				stored -> MembershipMode.valueOf(stored));
	}
}
