package tech.derbent.app.gannt.view.components;

import java.time.LocalDate;
import java.util.List;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.grids.CGrid;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.gannt.domain.CGanttItem;
import tech.derbent.app.gannt.view.datasource.CGanttDataProvider;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

/** CGanntGrid - Gantt items displayed in a unified grid with navigation to entity pages and visual timeline bars. */
@CssImport ("./themes/default/gantt-timeline.css")
public class CGanntGrid extends CGrid<CGanttItem> {

	private static final long serialVersionUID = 1L;
	private static final int TIMELINE_WIDTH_PIXELS = 400; // Width for timeline column
	private final CGanttDataProvider dataProvider;
	private final CPageEntityService pageEntityService;
	private LocalDate timelineEnd;
	private LocalDate timelineStart;

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

	/** Calculate the overall timeline range from all items to properly scale the bars. */
	private void calculateTimelineRange() {
		final List<CGanttItem> allItems = dataProvider.fetch(new com.vaadin.flow.data.provider.Query<>()).toList();
		timelineStart = null;
		timelineEnd = null;
		for (final CGanttItem item : allItems) {
			if (item.hasDates()) {
				final LocalDate itemStart = item.getStartDate();
				final LocalDate itemEnd = item.getEndDate();
				if (timelineStart == null || itemStart.isBefore(timelineStart)) {
					timelineStart = itemStart;
				}
				if (timelineEnd == null || itemEnd.isAfter(timelineEnd)) {
					timelineEnd = itemEnd;
				}
			}
		}
		// Add padding to timeline range for better visualization
		if (timelineStart != null && timelineEnd != null) {
			timelineStart = timelineStart.minusDays(7); // Add 1 week before
			timelineEnd = timelineEnd.plusDays(7); // Add 1 week after
		}
	}

	private void createColumns() {
		// Calculate timeline range from all items
		calculateTimelineRange();
		addShortTextColumn(CGanttItem::getEntityType, "Type", "entityType").setWidth("80px").setFlexGrow(0);
		// Title column with hierarchical indentation based on hierarchy level
		addColumn(item -> {
			final StringBuilder title = new StringBuilder();
			// Add indentation based on hierarchy level (2 spaces per level)
			for (int i = 0; i < item.getHierarchyLevel(); i++) {
				title.append("  ");
			}
			title.append(item.getEntity().getName());
			return title.toString();
		}).setHeader("Title").setKey("title").setWidth("200px").setFlexGrow(0).setSortable(false);
		addShortTextColumn(CGanttItem::getResponsibleName, "Responsible", "responsible").setWidth("120px").setFlexGrow(0);
		// Timeline visual bar column - colorful, responsive, with proper scaling
		addDateColumn(CGanttItem::getStartDate, "Start", "startDate").setWidth("100px").setFlexGrow(0);
		addDateColumn(CGanttItem::getEndDate, "End", "endDate").setWidth("100px").setFlexGrow(0);
		addIntegerColumn(item -> (int) item.getDurationDays(), "Duration (d)", "durationDays").setWidth("100px").setFlexGrow(0);
		addLongTextColumn(CGanttItem::getDescription, "Description", "description").setWidth("200px");
		addColumn(new ComponentRenderer<>(item -> new CGanttTimelineBar(item, timelineStart, timelineEnd, TIMELINE_WIDTH_PIXELS)))
				.setHeader("Timeline").setKey("timeline").setWidth("450px").setFlexGrow(1).setSortable(false);
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
