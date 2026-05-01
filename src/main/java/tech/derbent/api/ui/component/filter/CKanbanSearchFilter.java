package tech.derbent.api.ui.component.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import tech.derbent.api.ui.component.basic.CTextField;

/** CKanbanSearchFilter — free-text search across item name and description on the kanban board.
 * <p>
 * The search is case-insensitive and matches any substring of the item's name or description.
 * Works in both Sprint Board and Status Board modes. */
public class CKanbanSearchFilter extends CAbstractFilterComponent<String> {

	public static final String FILTER_KEY = "kanbanSearch";

	private final CTextField textField;

	public CKanbanSearchFilter() {
		super(FILTER_KEY);
		textField = new CTextField();
		textField.setPlaceholder("Search…");
		textField.setWidth("160px");
		textField.getStyle().set("min-width", "0");
		textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
		textField.setClearButtonVisible(true);
		textField.addValueChangeListener(event -> {
			final String value = event.getValue();
			notifyChangeListeners(value != null && !value.isBlank() ? value.trim() : null);
		});
	}

	@Override
	public void clearFilter() {
		textField.clear();
	}

	@Override
	protected Component createComponent() {
		return textField;
	}

	@Override
	protected void updateComponentValue(final String value) {
		textField.setValue(value != null ? value : "");
	}

	@Override
	public void valuePersist_enable(final String storageId) {
		// Text search is intentionally not persisted across sessions — it's ephemeral.
	}
}
