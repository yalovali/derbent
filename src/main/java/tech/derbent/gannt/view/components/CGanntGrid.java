package tech.derbent.gannt.view.components;

import com.vaadin.flow.component.grid.GridVariant;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.gannt.domain.CGanttItem;
import tech.derbent.gannt.view.datasource.CGanttDataProvider;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.projects.domain.CProject;

/** CGanntGrid - Gantt items displayed in a unified grid. */
public class CGanntGrid extends CGrid<CGanttItem> {

	private static final long serialVersionUID = 1L;
	private final CGanttDataProvider dataProvider;

	public CGanntGrid(final CProject project, final CActivityService activityService, final CMeetingService meetingService) {
		super(CGanttItem.class);
		Check.notNull(project, "Project cannot be null");
		this.dataProvider = new CGanttDataProvider(project, activityService, meetingService);
		addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
		setHeightFull();
		setDataProvider(dataProvider);
		createColumns();
	}

	private void createColumns() {
		addShortTextColumn(CGanttItem::getEntityType, "Type", "entityType");
		addShortTextColumn(CGanttItem::getDisplayName, "Title", "displayName");
		addShortTextColumn(CGanttItem::getResponsibleName, "Responsible", "responsible");
		addDateColumn(CGanttItem::getStartDate, "Start", "startDate");
		addDateColumn(CGanttItem::getEndDate, "End", "endDate");
		addIntegerColumn(item -> (int) item.getDurationDays(), "Duration (d)", "durationDays");
		addIntegerColumn(CGanttItem::getHierarchyLevel, "Level", "hierarchyLevel");
		addShortTextColumn(item -> {
			if (item.hasParent()) {
				return item.getParentType() + "#" + item.getParentId();
			}
			return "";
		}, "Parent", "parent");
		addLongTextColumn(CGanttItem::getDescription, "Description", "description");
	}

	/** Public refresh hook. */
	public void refresh() {
		dataProvider.refreshAll();
	}
}
