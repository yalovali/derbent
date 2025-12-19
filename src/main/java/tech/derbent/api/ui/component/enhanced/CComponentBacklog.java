package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.projects.domain.CProject;

public class CComponentBacklog extends CComponentEntitySelection<CProjectItem<?>> implements IPageServiceAutoRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentBacklog.class);
	private static final long serialVersionUID = 1L;
	private final CActivityService activityService;
	private final CMeetingService meetingService;

	/** Creates the list of entity type configurations for the backlog.
	 * @return list of entity type configs (CActivity, CMeeting) */
	private static List<EntityTypeConfig<?>> createEntityTypes() {
		final List<EntityTypeConfig<?>> entityTypes = new ArrayList<>();
		// Get services from Spring context
		final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
		final CMeetingService meetingService = CSpringContext.getBean(CMeetingService.class);
		entityTypes.add(new EntityTypeConfig<>("CActivity", CActivity.class, activityService));
		entityTypes.add(new EntityTypeConfig<>("CMeeting", CMeeting.class, meetingService));
		return entityTypes;
	}

	/** Creates the items provider that loads all project items ordered by sprint order.
	 * @param project the project to load backlog items for
	 * @return provider for loading items */
	@SuppressWarnings ("unchecked")
	private static ItemsProvider<CProjectItem<?>> createItemsProvider(final CProject project) {
		return config -> {
			try {
				if (project == null) {
					LOGGER.warn("No project available for loading backlog items");
					return new ArrayList<>();
				}
				// Get services from Spring context
				final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
				final CMeetingService meetingService = CSpringContext.getBean(CMeetingService.class);
				// Load items ordered by sprintOrder for proper backlog display
				if (config.getEntityClass() == CActivity.class) {
					return (List<CProjectItem<?>>) (List<?>) activityService.listForProjectBacklog(project);
				} else if (config.getEntityClass() == CMeeting.class) {
					return (List<CProjectItem<?>>) (List<?>) meetingService.listForProjectBacklog(project);
				}
				return new ArrayList<>();
			} catch (final Exception e) {
				LOGGER.error("Error loading backlog items for entity type: {}", config.getDisplayName(), e);
				return new ArrayList<>();
			}
		};
	}

	/** Creates a selection handler for the backlog.
	 * @return selection handler that logs selection changes */
	private static Consumer<Set<CProjectItem<?>>> createSelectionHandler() {
		return selectedItems -> {
			LOGGER.debug("Backlog selection changed: {} items selected", selectedItems.size());
		};
	}

	/** Constructor for backlog component.
	 * @param project project to load backlog items for (required) */
	public CComponentBacklog(final CProject project) {
		super(createEntityTypes(), createItemsProvider(project), createSelectionHandler(), false, null, AlreadySelectedMode.HIDE_ALREADY_SELECTED);
		Check.notNull(project, "Project cannot be null");
		activityService = CSpringContext.getBean(CActivityService.class);
		meetingService = CSpringContext.getBean(CMeetingService.class);
		setDynamicHeight("600px");
		LOGGER.debug("CComponentBacklog created for project: {}", project.getId());
	}

	public void drag_checkEventAfterPass(CEvent event) {
		super.drag_checkEventAfterPass(event);
		refreshComponent();
	}

	@Override
	public void configureGrid(final CGrid<CProjectItem<?>> grid) {
		super.configureGrid(grid);
		grid.addStoryPointColumn(item -> {
			Check.instanceOf(item, ISprintableItem.class, "Backlog item must implement ISprintableItem");
			return (ISprintableItem) item;
		}, this::saveStoryPoint, this::handleStoryPointError, "Story Points", "storyPoint");
	}

	/** Gets all items currently displayed in the grid.
	 * @return list of all items in grid */
	public List<CProjectItem<?>> getAllItems() {
		final var grid = getGrid();
		if (grid == null) {
			return List.of();
		}
		return grid.getListDataView().getItems().toList();
	}

	@Override
	public String getComponentName() { return "backlogItems"; }

	/** Refresh the backlog component and underlying grid. */
	public void refreshComponent() {
		refreshGrid();
	}

	private void saveStoryPoint(final ISprintableItem item) {
		Check.notNull(item, "Sprintable item cannot be null when saving story points");
		Check.notNull(item.getId(), "Sprintable item must be persisted before updating story points");
		if (item instanceof CActivity) {
			activityService.save((CActivity) item);
			return;
		}
		if (item instanceof CMeeting) {
			meetingService.save((CMeeting) item);
			return;
		}
		throw new IllegalArgumentException("Unsupported sprintable item type: " + item.getClass().getSimpleName());
	}

	private void handleStoryPointError(final Exception exception) {
		Check.notNull(exception, "Exception cannot be null when handling story point errors");
		CNotificationService.showException("Error saving story points", exception);
	}

	@Override
	public void registerWithPageService(final CPageService<?> pageService) {
		Check.notNull(pageService, "Page service cannot be null");
		final String componentName = getComponentName();
		pageService.registerComponent(componentName, this);
		LOGGER.debug("[BindDebug] {} auto-registered with page service as '{}' (binding will occur during CPageService.bind())",
				getClass().getSimpleName(), componentName);
	}
}
