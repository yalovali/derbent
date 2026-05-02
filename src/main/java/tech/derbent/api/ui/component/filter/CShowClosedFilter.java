package tech.derbent.api.ui.component.filter;

import com.vaadin.flow.component.Component;
import tech.derbent.api.ui.component.basic.CCheckbox;

/** CShowClosedFilter - Boolean filter checkbox to show or hide closed (final-status) items.
 * <p>
 * Default value is {@code false} — closed items are hidden. When the user checks "Show closed", the value becomes {@code true} and the board
 * includes items whose status has {@code finalStatus == true}.
 * </p>
 * <p>
 * Used in Kanban, GNTT, and Sprint Planning filter toolbars.
 * </p> */
public class CShowClosedFilter extends CAbstractFilterComponent<Boolean> {

	public static final String FILTER_KEY = "showClosed";
	private static final long serialVersionUID = 1L;
	private final CCheckbox checkbox;

	public CShowClosedFilter() {
		super(FILTER_KEY);
		checkbox = new CCheckbox("Show closed");
		checkbox.addValueChangeListener(event -> notifyChangeListeners(event.getValue()));
	}

	@Override
	public void clearFilter() {
		checkbox.setValue(false);
	}

	@Override
	protected Component createComponent() {
		return checkbox;
	}

	@Override
	public Boolean getValue() {
		return checkbox.getValue();
	}

	@Override
	protected void updateComponentValue(final Boolean value) {
		checkbox.setValue(value != null ? value : false);
	}

	@Override
	public void valuePersist_enable(final String storageId) {
		checkbox.enablePersistence(storageId + "_" + FILTER_KEY);
	}
}
