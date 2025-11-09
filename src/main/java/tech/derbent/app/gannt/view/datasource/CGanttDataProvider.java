package tech.derbent.app.gannt.view.datasource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import tech.derbent.api.utils.Check;
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
	private List<CGanttItem> cachedItems;

	public CGanttDataProvider(final CProject project, final CActivityService activityService, final CMeetingService meetingService) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(activityService, "ActivityService cannot be null");
		Check.notNull(meetingService, "MeetingService cannot be null");
		this.project = project;
		this.activityService = activityService;
		this.meetingService = meetingService;
	}

	@Override
	protected Stream<CGanttItem> fetchFromBackEnd(final Query<CGanttItem, Void> query) {
		try {
			final List<CGanttItem> allItems = getCachedItems();
			return allItems.stream().skip(query.getOffset()).limit(query.getLimit());
		} catch (final Exception e) {
			LOGGER.error("Error fetching Gantt items from backend: {}", e.getMessage(), e);
			return Stream.empty();
		}
	}

	/** Get cached items or load them if not cached. */
	private List<CGanttItem> getCachedItems() {
		if (cachedItems == null) {
			cachedItems = loadItems();
		}
		return cachedItems;
	}

	/** Merge activities and meetings into CGanttItems. */
	private List<CGanttItem> loadItems() {
		LOGGER.debug("Loading Gantt items for project: {} (ID: {})", project.getName(), project.getId());
		final List<CGanttItem> items = new ArrayList<>();
		// Counter to ensure unique IDs across different entity types (Activity ID=1, Meeting ID=1 would collide)
		final AtomicLong idCounter = new AtomicLong(0);
		try {
			// --- Activities ---
			final List<CActivity> activities = activityService.listByProject(project);
			for (final CActivity a : activities) {
				LOGGER.debug("Adding activity to Gantt items: {} (ID: {})", a.getId(), a.getName());
				items.add(new CGanttItem(a, idCounter.incrementAndGet()));
			}
			// --- Meetings ---
			final List<CMeeting> meetings = meetingService.listByProject(project);
			for (final CMeeting m : meetings) {
				LOGGER.debug("Adding meeting to Gantt items: {} (ID: {})", m.getId(), m.getName());
				items.add(new CGanttItem(m, idCounter.incrementAndGet()));
			}
			// --- Sıralama: startDate → endDate (null'lar sonda)
			items.sort(BY_TIMELINE);
			LOGGER.debug("Loaded {} Gantt items total with unique IDs 1-{}", items.size(), idCounter.get());
		} catch (final Exception e) {
			LOGGER.error("Error loading Gantt items: {}", e.getMessage(), e);
			// Return empty list on error to prevent UI crash
		}
		return items;
	}

	@Override
	public void refreshAll() {
		LOGGER.debug("Refreshing Gantt data provider - clearing cache");
		cachedItems = null; // Clear cache on refresh
		super.refreshAll();
	}

	@Override
	protected int sizeInBackEnd(final Query<CGanttItem, Void> query) {
		try {
			return getCachedItems().size();
		} catch (final Exception e) {
			LOGGER.error("Error getting size of Gantt items: {}", e.getMessage(), e);
			return 0;
		}
	}
}
