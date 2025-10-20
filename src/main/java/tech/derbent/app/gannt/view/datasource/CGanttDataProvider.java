package tech.derbent.app.gannt.view.datasource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.gannt.domain.CGanttItem;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.projects.domain.CProject;

/** CGanttDataProvider - Combines activities and meetings of a project into unified CGanttItem list. Provides a single data source for Gantt grids and
 * charts. */
public class CGanttDataProvider extends AbstractBackEndDataProvider<CGanttItem, Void> {

	/** Zaman çizelgesi için tarih öncelikli sıralama: startDate (null'lar sonda) → endDate (null'lar sonda) */
	private static final Comparator<CGanttItem> BY_TIMELINE =
			Comparator.comparing(CGanttItem::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(CGanttItem::getEndDate,
					Comparator.nullsLast(Comparator.naturalOrder()));
	private static final long serialVersionUID = 1L;
	private final CActivityService activityService;
	private final Logger LOGGER = LoggerFactory.getLogger(CGanttDataProvider.class);
	private final CMeetingService meetingService;
	private final CProject project;

	public CGanttDataProvider(final CProject project, final CActivityService activityService, final CMeetingService meetingService) {
		this.project = project;
		this.activityService = activityService;
		this.meetingService = meetingService;
	}

	@Override
	protected Stream<CGanttItem> fetchFromBackEnd(final Query<CGanttItem, Void> query) {
		List<CGanttItem> allItems;
		try {
			allItems = loadItems();
			return allItems.stream().skip(query.getOffset()).limit(query.getLimit());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Stream.empty();
	}

	/** Merge activities and meetings into CGanttItems.
	 * @throws Exception */
	private List<CGanttItem> loadItems() throws Exception {
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
		try {
			return loadItems().size();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
}
