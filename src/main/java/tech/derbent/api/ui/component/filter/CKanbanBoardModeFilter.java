package tech.derbent.api.ui.component.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import tech.derbent.api.ui.component.basic.CComboBox;
import tech.derbent.plm.kanban.kanbanline.domain.EKanbanViewMode;

/** CKanbanBoardModeFilter — toggles the kanban board between Sprint Board and Status Board modes.
 * <p>
 * Sprint Board (default): shows items from the selected sprint + a backlog column.<br>
 * Status Board: shows all project items grouped by status, no backlog column, drag only changes status.
 * </p> */
public class CKanbanBoardModeFilter extends CAbstractFilterComponent<EKanbanViewMode> {

	public static final String FILTER_KEY = EKanbanViewMode.FILTER_KEY;

	private final CComboBox<EKanbanViewMode> comboBox;

	public CKanbanBoardModeFilter() {
		super(FILTER_KEY);
		comboBox = new CComboBox<>("View");
		comboBox.setWidth("150px");
		comboBox.getStyle().set("min-width", "0");
		comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
		comboBox.setItems(EKanbanViewMode.values());
		comboBox.setItemLabelGenerator(EKanbanViewMode::getLabel);
		comboBox.setValue(EKanbanViewMode.SPRINT_BOARD);
		comboBox.setAllowCustomValue(false);
		comboBox.addValueChangeListener(event -> {
			final EKanbanViewMode value = event.getValue() != null ? event.getValue() : EKanbanViewMode.SPRINT_BOARD;
			notifyChangeListeners(value);
		});
	}

	@Override
	public void clearFilter() {
		comboBox.setValue(EKanbanViewMode.SPRINT_BOARD);
	}

	@Override
	protected Component createComponent() {
		return comboBox;
	}

	@Override
	protected void updateComponentValue(final EKanbanViewMode value) {
		comboBox.setValue(value != null ? value : EKanbanViewMode.SPRINT_BOARD);
	}

	@Override
	public void valuePersist_enable(final String storageId) {
		comboBox.enablePersistence(storageId + "_" + FILTER_KEY,
				stored -> EKanbanViewMode.valueOf(stored));
	}
}
