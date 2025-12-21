package tech.derbent.app.kanban.kanbanline.view;

import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.ui.component.enhanced.CComponentListEntityBase;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.app.kanban.kanbanline.service.CKanbanColumnService;

/** CComponentListKanbanColumns - Component for managing CKanbanColumn entities within a CKanbanLine. Provides CRUD and ordering controls for
 * kanban column definitions. */
public class CComponentListKanbanColumns extends CComponentListEntityBase<CKanbanLine, CKanbanColumn> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListKanbanColumns.class);
	private static final long serialVersionUID = 1L;

	public CComponentListKanbanColumns(final CKanbanColumnService kanbanColumnService) {
		super("Kanban Columns", CKanbanLine.class, CKanbanColumn.class, kanbanColumnService);
		Check.notNull(kanbanColumnService, "KanbanColumnService cannot be null");
		setDynamicHeight("400px");
	}

	@Override
	public void configureGrid(final CGrid<CKanbanColumn> grid) {
		Check.notNull(grid, "Grid cannot be null");
		grid.addIdColumn(CKanbanColumn::getId, "ID", "id");
		grid.addIntegerColumn(CKanbanColumn::getItemOrder, "Order", "itemOrder");
		grid.addShortTextColumn(CKanbanColumn::getName, "Name", "name");
		grid.addShortTextColumn(CKanbanColumn::getDescription, "Description", "description");
		grid.addShortTextColumn(this::formatIncludedStatuses, "Included Statuses", "includedStatuses");
		grid.addBooleanColumn(CKanbanColumn::getActive, "Status", "Active", "Inactive");
	}

	private String formatIncludedStatuses(final CKanbanColumn column) {
		Check.notNull(column, "Kanban column cannot be null");
		if (column.getIncludedStatuses() == null || column.getIncludedStatuses().isEmpty()) {
			return "-";
		}
		return column.getIncludedStatuses().stream().map(status -> status.getName()).filter(name -> name != null && !name.isBlank())
				.sorted(String::compareToIgnoreCase).reduce((first, second) -> first + ", " + second).orElse("-");
	}

	@Override
	public void drag_checkEventAfterPass(final CEvent event) {
		// No-op for kanban column list (no special drag behavior).
	}

	@Override
	public void drag_checkEventBeforePass(final CEvent event) {
		// No-op for kanban column list (no special drag behavior).
	}

	@Override
	protected CKanbanColumn createNewEntity() {
		Check.notNull(getMasterEntity(), "Kanban line must be selected before creating columns");
		final CKanbanColumn column = new CKanbanColumn("New Column", getMasterEntity());
		column.setItemOrder(getNextOrder());
		return column;
	}

	@Override
	protected Integer getNextOrder() {
		Check.notNull(getMasterEntity(), "Kanban line cannot be null when calculating next order");
		if (getMasterEntity().getId() == null) {
			return 1;
		}
		final CKanbanColumnService service = (CKanbanColumnService) childService;
		return service.getNextItemOrder(getMasterEntity());
	}

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

	@Override
	public String getComponentName() { return "kanbanColumns"; }
}
