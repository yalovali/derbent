package tech.derbent.gannt.view.datasource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.gannt.domain.CGanttItem;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.projects.domain.CProject;

/** CGanttDataProvider - Combines activities and meetings of a project into unified CGanttItem list. Provides a single data source for Gantt grids and
 * charts. */
public class CGanttDataProvider extends AbstractBackEndDataProvider<CGanttItem, Void> {
	private static final long serialVersionUID = 1L;
	/** Zaman çizelgesi için tarih öncelikli sıralama: startDate (null'lar sonda) → endDate (null'lar sonda) */
	private static final Comparator<CGanttItem> BY_TIMELINE =
			Comparator.comparing(CGanttItem::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(CGanttItem::getEndDate,
					Comparator.nullsLast(Comparator.naturalOrder()));
	private final Logger LOGGER = LoggerFactory.getLogger(CGanttDataProvider.class);
	private final CProject project;
	private final CActivityService activityService;
	private final CMeetingService meetingService;

	public CGanttDataProvider(final CProject project, final CActivityService activityService, final CMeetingService meetingService) {
		this.project = project;
		this.activityService = activityService;
		this.meetingService = meetingService;
	}

	@Override
	protected Stream<CGanttItem> fetchFromBackEnd(final Query<CGanttItem, Void> query) {
		final List<CGanttItem> allItems = loadItems();
		return allItems.stream().skip(query.getOffset()).limit(query.getLimit());
	}

	/** Merge activities and meetings into CGanttItems. */
	private List<CGanttItem> loadItems() {
		LOGGER.debug("Loading Gantt items for project: {} (ID: {})", project.getName(), project.getId());
		final List<CGanttItem> items = new ArrayList<>();
		// --- Activities ---
		final List<CActivity> activities = activityService.listByProject(project);
		for (final CActivity a : activities) {
			items.add(new CGanttItem(a));
		}
		// --- Meetings ---
		final List<CMeeting> meetings = meetingService.listByProject(project);
		for (final CMeeting m : meetings) {
			items.add(new CGanttItem(m));
		}
		// --- Sıralama: startDate → endDate (null'lar sonda)
		items.sort(BY_TIMELINE);
		return items;
	}

	@Override
	protected int sizeInBackEnd(final Query<CGanttItem, Void> query) {
		return loadItems().size();
	}
}
