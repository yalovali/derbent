package tech.derbent.api.ui.component.enhanced;

import java.util.List;
import java.util.function.Consumer;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.screens.view.CDialogDetailLinesEdit;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

/**
 * CComponentListDetailLines - Component for managing CDetailLines in a CDetailSection.
 * Provides full CRUD functionality for screen field definitions with ordering.
 *
 * <p>Features inherited from CComponentListEntityBase:
 * <ul>
 *   <li>Grid display with ID, Order, Caption, Field Name, Required, Status columns</li>
 *   <li>Add/Edit/Delete operations with validation</li>
 *   <li>Move up/down for reordering</li>
 *   <li>Dialog-based editing</li>
 *   <li>Selection management</li>
 * </ul>
 */
public class CComponentListDetailLines extends CComponentListEntityBase<CDetailSection, CDetailLines> {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for CComponentListDetailLines.
	 *
	 * @param detailLinesService The service for CDetailLines operations
	 */
	public CComponentListDetailLines(final CDetailLinesService detailLinesService) {
		super("Screen Field Definitions", CDetailSection.class, CDetailLines.class, detailLinesService);
		Check.notNull(detailLinesService, "DetailLinesService cannot be null");
		LOGGER.debug("CComponentListDetailLines created");
	}

	@Override
	protected void configureGrid(final CGrid<CDetailLines> grid) {
		Check.notNull(grid, "Grid cannot be null");
		LOGGER.debug("Configuring grid columns for CDetailLines");
		grid.addColumn(CDetailLines::getId).setHeader("Id").setWidth("50px");
		grid.addColumn(CDetailLines::getItemOrder).setHeader("Order").setWidth("50px");
		grid.addColumn(CDetailLines::getFieldCaption).setHeader("Caption").setAutoWidth(true);
		grid.addColumn(CDetailLines::getEntityProperty).setHeader("Field Name").setAutoWidth(true);
		grid.addColumn(line -> line.getIsRequired() ? "Yes" : "No").setHeader("Required").setWidth("80px");
		grid.addColumn(line -> line.getActive() ? "Active" : "Inactive").setHeader("Status").setWidth("80px");
	}

	@Override
	protected CDetailLines createNewEntity() {
		final CDetailSection master = getMasterEntity();
		Check.notNull(master, "Master section cannot be null when creating new entity");
		Check.notNull(master.getId(), "Master section must be saved before adding detail lines");
		LOGGER.debug("Creating new CDetailLines entity for section: {}", master.getId());
		final CDetailLinesService service = (CDetailLinesService) childService;
		// Get the position of the current item if selected, otherwise add at end
		Integer position = 0;
		if (selectedItem != null) {
			position = selectedItem.getItemOrder();
			LOGGER.debug("Inserting before position: {}", position);
			try {
				return service.insertLineBefore(master, CEntityFieldService.THIS_CLASS, "name", position);
			} catch (final Exception e) {
				LOGGER.error("Error inserting line before position, falling back to append", e);
				// Fall through to create at end
			}
		}
		// Create new line at the end
		LOGGER.debug("Creating new line at end of list");
		return service.newEntity(master, CEntityFieldService.THIS_CLASS, "name");
	}

	@Override
	protected Integer getNextOrder() {
		final CDetailSection master = getMasterEntity();
		Check.notNull(master, "Master section cannot be null when getting next order");
		final CDetailLinesService service = (CDetailLinesService) childService;
		final Integer nextOrder = service.getNextItemOrder(master);
		LOGGER.debug("Next line order for section {}: {}", master.getId() != null ? master.getId() : "null", nextOrder);
		return nextOrder;
	}

	@Override
	protected List<CDetailLines> loadItems(final CDetailSection master) {
		Check.notNull(master, "Master section cannot be null when loading items");
		LOGGER.debug("Loading detail lines for section: {}", master.getId() != null ? master.getId() : "null");
		if (master.getId() == null) {
			LOGGER.debug("Master section is new, returning empty list");
			return List.of();
		}
		final CDetailLinesService service = (CDetailLinesService) childService;
		final List<CDetailLines> lines = service.findByMaster(master);
		Check.notNull(lines, "Loaded lines cannot be null");
		LOGGER.debug("Loaded {} detail lines", lines.size());
		return lines;
	}

	@Override
	protected void openEditDialog(final CDetailLines entity, final Consumer<CDetailLines> saveCallback, final boolean isNew) {
		Check.notNull(entity, "Entity cannot be null when opening edit dialog");
		Check.notNull(saveCallback, "Save callback cannot be null");
		final CDetailSection master = getMasterEntity();
		Check.notNull(master, "Master section cannot be null when opening edit dialog");
		LOGGER.debug("Opening edit dialog for CDetailLines: {} (isNew={})", entity.getId() != null ? entity.getId() : "new", isNew);
		try {
			final CDialogDetailLinesEdit dialog = new CDialogDetailLinesEdit(entity, saveCallback, isNew, master);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Error opening CDetailLinesEditDialog", e);
			CNotificationService.showException("Error opening edit dialog", e);
		}
	}
}
