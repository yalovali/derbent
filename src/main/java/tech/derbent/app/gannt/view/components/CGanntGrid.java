package tech.derbent.app.gannt.view.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.GridVariant;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.grids.CGrid;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.gannt.domain.CGanttItem;
import tech.derbent.app.gannt.view.datasource.CGanttDataProvider;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

/** CGanntGrid - Gantt items displayed in a unified grid with navigation to entity pages. */
public class CGanntGrid extends CGrid<CGanttItem> {

	private static final long serialVersionUID = 1L;
	private final CGanttDataProvider dataProvider;
	private final CPageEntityService pageEntityService;

	public CGanntGrid(final CProject project, final CActivityService activityService, final CMeetingService meetingService,
			final CPageEntityService pageEntityService) {
		super(CGanttItem.class);
		Check.notNull(project, "Project cannot be null");
		Check.notNull(pageEntityService, "PageEntityService cannot be null");
		this.pageEntityService = pageEntityService;
		dataProvider = new CGanttDataProvider(project, activityService, meetingService);
		addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
		setHeightFull();
		setDataProvider(dataProvider);
		createColumns();
		setupItemClickNavigation();
	}

	private void createColumns() {
		addShortTextColumn(CGanttItem::getEntityType, "Type", "entityType");
		// Title column with hierarchical indentation based on hierarchy level
		addColumn(item -> {
			final StringBuilder title = new StringBuilder();
			// Add indentation based on hierarchy level (2 spaces per level)
			for (int i = 0; i < item.getHierarchyLevel(); i++) {
				title.append("  ");
			}
			title.append(item.getEntity().getName());
			return title.toString();
		}).setHeader("Title").setKey("title").setFlexGrow(3).setSortable(false);
		addShortTextColumn(CGanttItem::getResponsibleName, "Responsible", "responsible");
		addDateColumn(CGanttItem::getStartDate, "Start", "startDate");
		addDateColumn(CGanttItem::getEndDate, "End", "endDate");
		addIntegerColumn(item -> (int) item.getDurationDays(), "Duration (d)", "durationDays");
		addLongTextColumn(CGanttItem::getDescription, "Description", "description");
	}

	/** Public refresh hook. */
	public void refresh() {
		dataProvider.refreshAll();
	}

	/** Setup click navigation to appropriate entity page based on item type. */
	private void setupItemClickNavigation() {
		addItemClickListener(event -> {
			final CGanttItem item = event.getItem();
			if (item == null) {
				return;
			}
			try {
				// Get the page entity for this entity type
				final String entityType = item.getEntityType();
				final Long entityId = item.getEntityId();
				// Find the page for this entity type
				pageEntityService.findByEntityClass(entityType).ifPresent(pageEntity -> {
					final String navUrl = String.format("cdynamicpagerouter/page:%d/item:%d", pageEntity.getId(), entityId);
					UI.getCurrent().navigate(navUrl);
				});
			} catch (final Exception e) {
				// Log but don't disrupt user experience
				e.printStackTrace();
			}
		});
	}
}
