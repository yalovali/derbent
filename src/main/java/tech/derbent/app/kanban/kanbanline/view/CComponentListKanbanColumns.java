package tech.derbent.app.kanban.kanbanline.view;

import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.ui.component.enhanced.CComponentListEntityBase;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.app.kanban.kanbanline.service.CKanbanColumnService;
import tech.derbent.app.kanban.kanbanline.service.CKanbanLineService;

/** CComponentListKanbanColumns - Component for managing CKanbanColumn entities within a CKanbanLine. Provides CRUD and ordering controls for kanban
 * column definitions. */
public class CComponentListKanbanColumns extends CComponentListEntityBase<CKanbanLine, CKanbanColumn> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListKanbanColumns.class);
	private static final long serialVersionUID = 1L;
	private final CKanbanLineService kanbanLineService;

	/** Creates the column list component with required services. */
	public CComponentListKanbanColumns(final CKanbanLineService kanbanLineService, final CKanbanColumnService kanbanColumnService) {
		super("Kanban Columns", CKanbanLine.class, CKanbanColumn.class, kanbanColumnService);
		Check.notNull(kanbanLineService, "KanbanLineService cannot be null");
		Check.notNull(kanbanColumnService, "KanbanColumnService cannot be null");
		this.kanbanLineService = kanbanLineService;
		setDynamicHeight("400px");
	}

	/** Configures grid columns for kanban column display. */
	@Override
	public void configureGrid(final CGrid<CKanbanColumn> grid1) {
		Check.notNull(grid1, "Grid cannot be null");
		grid1.addIdColumn(CKanbanColumn::getId, "ID", "id");
		grid1.addShortTextColumn(CKanbanColumn::getName, "Name", "name");
		grid1.addShortTextColumn(this::formatIncludedStatuses, "Included Statuses", "includedStatuses");
		grid1.addBooleanColumn(CKanbanColumn::getDefaultColumn, "Default", "Yes", "No");
	}

	/** Creates a new column instance for the current line. */
	@Override
	protected CKanbanColumn createNewEntity() {
		Check.notNull(getMasterEntity(), "Kanban line must be selected before creating columns");
		final CKanbanColumn column = new CKanbanColumn("New Column", getMasterEntity());
		column.setItemOrder(getNextOrder());
		return column;
	}

	/** No-op for drag checks after pass. */
	@Override
	public void drag_checkEventAfterPass(final CEvent event) {
		// No-op for kanban column list (no special drag behavior).
	}

	/** No-op for drag checks before pass. */
	@Override
	public void drag_checkEventBeforePass(final CEvent event) {
		// No-op for kanban column list (no special drag behavior).
	}

	/** Formats included statuses for grid display. */
	private String formatIncludedStatuses(final CKanbanColumn column) {
		Check.notNull(column, "Kanban column cannot be null");
		if (column.getIncludedStatuses() == null || column.getIncludedStatuses().isEmpty()) {
			return "-";
		}
		return column.getIncludedStatuses().stream().map(status -> status.getName()).filter(name -> name != null && !name.isBlank())
				.sorted(String::compareToIgnoreCase).reduce((first, second) -> first + ", " + second).orElse("-");
	}

	/** Returns the component name for layout binding. */
	@Override
	public String getComponentName() { return "kanbanColumns"; }

	/** Calculates the next order index for a new column. */
	@Override
	protected Integer getNextOrder() {
		Check.notNull(getMasterEntity(), "Kanban line cannot be null when calculating next order");
		if (getMasterEntity().getId() == null) {
			return 1;
		}
		final CKanbanColumnService service = (CKanbanColumnService) childService;
		return service.getNextItemOrder(getMasterEntity());
	}

	/** Loads columns for the current line. */
	@Override
	protected List<CKanbanColumn> loadItems(final CKanbanLine master) {
		Check.notNull(master, "Kanban line cannot be null");
		if (master.getId() == null) {
			LOGGER.debug("Kanban line is new, returning empty columns list");
			return List.of();
		}
		final CKanbanColumnService service = (CKanbanColumnService) childService;
		return service.findByMaster(master);
	}

	/** Handles delete button with confirmation and refresh. */
	@Override
	protected void on_buttonDelete_clicked() {
		Check.notNull(getSelectedItem(), "No item selected for deletion");
		Check.notNull(getSelectedItem().getId(), "Cannot delete unsaved item");
		Check.notNull(getMasterEntity(), "Kanban line must be selected before deleting columns");
		try {
			CNotificationService.showConfirmationDialog("Delete selected Kanban column?", () -> {
				try {
					kanbanLineService.deleteKanbanColumn(getMasterEntity(), getSelectedItem());
					refreshGrid();
					grid.asSingleSelect().clear();
					CNotificationService.showDeleteSuccess();
				} catch (final Exception e) {
					CNotificationService.showException("Error deleting Kanban column", e);
				}
			});
		} catch (final Exception e) {
			CNotificationService.showException("Error opening delete confirmation", e);
		}
	}

	/** Opens the column edit dialog. */
	@Override
	protected void openEditDialog(final CKanbanColumn entity, final Consumer<CKanbanColumn> saveCallback, final boolean isNew) {
		try {
			final CDialogKanbanColumnEdit dialog = new CDialogKanbanColumnEdit(entity, saveCallback, isNew);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Error opening kanban column edit dialog", e);
			throw new IllegalStateException("Unable to open kanban column dialog", e);
		}
	}
}
