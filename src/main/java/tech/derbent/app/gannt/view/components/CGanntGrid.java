package tech.derbent.app.gannt.view.components;

import java.time.LocalDate;
import java.util.List;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CDiv;
import tech.derbent.api.views.grids.CGrid;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.gannt.domain.CGanttItem;
import tech.derbent.app.gannt.view.datasource.CGanttDataProvider;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

/** CGanntGrid - Gantt items displayed in a unified grid with navigation to entity pages and visual timeline bars. */
@CssImport ("./themes/default/gantt.css")
public class CGanntGrid extends CGrid<CGanttItem> {

	private static final int DEFAULT_TIMELINE_WIDTH_PIXELS = 800; // Default width for timeline column
	private static final int MAX_TIMELINE_WIDTH_PIXELS = 1600; // Maximum width
	private static final int MIN_TIMELINE_WIDTH_PIXELS = 400; // Minimum width
	private static final long serialVersionUID = 1L;
	private final CGanttDataProvider dataProvider;
	private LocalDate timelineEnd;
	private CGanttTimelineHeader timelineHeader;
	private LocalDate timelineStart;
	private int timelineWidthPixels = DEFAULT_TIMELINE_WIDTH_PIXELS;

	public CGanntGrid(final CProject project, final CActivityService activityService, final CMeetingService meetingService,
			final CPageEntityService pageEntityService) {
		super(CGanttItem.class);
		LOGGER.debug("Initializing CGanntGrid for project: {} (ID: {})", project.getName(), project.getId());
		Check.notNull(project, "Project cannot be null");
		Check.notNull(pageEntityService, "PageEntityService cannot be null");
		dataProvider = new CGanttDataProvider(project, activityService, meetingService);
		addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
		setHeightFull();
		setDataProvider(dataProvider);
		createColumns();
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
				if ((timelineStart == null) || itemStart.isBefore(timelineStart)) {
					timelineStart = itemStart;
				}
				if ((timelineEnd == null) || itemEnd.isAfter(timelineEnd)) {
					timelineEnd = itemEnd;
				}
			}
		}
		// Add padding to timeline range for better visualization
		if ((timelineStart != null) && (timelineEnd != null)) {
			timelineStart = timelineStart.minusDays(7); // Add 1 week before
			timelineEnd = timelineEnd.plusDays(7); // Add 1 week after
		} else {
			final LocalDate today = LocalDate.now();
			timelineStart = today.minusDays(30);
			timelineEnd = today.plusDays(60);
		}
	}

	private void createColumns() {
		// Calculate timeline range from all items
		calculateTimelineRange();
		addIdColumn(CGanttItem::getEntityId, "ID", "entityId").setFlexGrow(0);
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
		// Timeline column with custom header showing timeline markers
		final Renderer<CGanttItem> timelineRenderer = new ComponentRenderer<>(item -> {
			final CDiv wrapper = new CDiv();
			wrapper.setWidth(timelineWidthPixels + "px");
			wrapper.setHeight("10px");
			// wrapper.getStyle().set("border", "1px dashed lightgray");
			wrapper.add(new CGanttTimelineBar(item, timelineStart, timelineEnd, timelineWidthPixels));
			return wrapper;
		});
		timelineHeader = new CGanttTimelineHeader(timelineStart, timelineEnd, timelineWidthPixels, range -> updateTimelineRange(range),
				this::setTimelineWidth);
		addColumn(timelineRenderer).setHeader(timelineHeader).setKey("timeline").setFlexGrow(1).setSortable(false);
	}

	/** Public refresh hook. */
	public void refresh() {
		dataProvider.refreshAll();
	}

	/** Set the timeline column width and refresh the view.
	 * @param widthPixels The new width in pixels */
	public void setTimelineWidth(final int widthPixels) {
		if ((widthPixels >= MIN_TIMELINE_WIDTH_PIXELS) && (widthPixels <= MAX_TIMELINE_WIDTH_PIXELS)) {
			timelineWidthPixels = widthPixels;
			// Recreate columns to apply new width
			getColumns().forEach(this::removeColumn);
			createColumns();
		}
	}

	private void updateTimelineRange(final CGanttTimelineHeader.CGanttTimelineRange range) {
		timelineStart = range.startDate();
		timelineEnd = range.endDate();
		dataProvider.refreshAll();
		getDataCommunicator().reset();
	}
}
