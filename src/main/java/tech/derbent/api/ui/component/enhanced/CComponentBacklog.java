package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.sprints.service.CSprintItemService;

public class CComponentBacklog extends CComponentEntitySelection<CProjectItem<?>> implements IPageServiceAutoRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentBacklog.class);
	private static final long serialVersionUID = 1L;

	/** Creates the already-selected provider that filters out items already in the sprint.
	 * @param sprint the sprint to check for existing items
	 * @return provider for already-selected items */
	private static ItemsProvider<CProjectItem<?>> createAlreadySelectedProvider(final CSprint sprint) {
		return config -> {
			try {
				if (sprint == null || sprint.getId() == null) {
					return new ArrayList<>();
				}
				final CSprintItemService service = CSpringContext.getBean(CSprintItemService.class);
				final List<CSprintItem> sprintItems = service.findByMasterIdWithItems(sprint.getId());
				// Filter by entity type and extract the underlying items
				final List<CProjectItem<?>> result = new ArrayList<>();
				final String targetType = config.getEntityClass().getSimpleName();
				for (final CSprintItem sprintItem : sprintItems) {
					if (sprintItem.getItem() != null && targetType.equals(sprintItem.getItemType())) {
						result.add(sprintItem.getItem());
					}
				}
				return result;
			} catch (final Exception e) {
				LOGGER.error("Error loading already selected items for backlog: {}", config.getDisplayName(), e);
				return new ArrayList<>();
			}
		};
	}

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
	 * @param sprint the sprint (provides access to project)
	 * @return provider for loading items */
	@SuppressWarnings ("unchecked")
	private static ItemsProvider<CProjectItem<?>> createItemsProvider(final CSprint sprint) {
		return config -> {
			try {
				final CProject project = sprint != null ? sprint.getProject() : null;
				if (project == null) {
					LOGGER.warn("No project available for loading backlog items");
					return new ArrayList<>();
				}
				// Get services from Spring context
				final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
				final CMeetingService meetingService = CSpringContext.getBean(CMeetingService.class);
				// Load items ordered by sprintOrder for proper backlog display
				if (config.getEntityClass() == CActivity.class) {
					return (List<CProjectItem<?>>) (List<?>) activityService.listByProjectOrderedBySprintOrder(project);
				} else if (config.getEntityClass() == CMeeting.class) {
					return (List<CProjectItem<?>>) (List<?>) meetingService.listByProjectOrderedBySprintOrder(project);
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

	private boolean dragEnabled = false;

	/** Constructor for backlog component.
	 * @param sprint The sprint for which to display the backlog (items NOT in this sprint) */
	public CComponentBacklog(final CSprint sprint) {
		super(createEntityTypes(), createItemsProvider(sprint), createSelectionHandler(), false, createAlreadySelectedProvider(sprint),
				AlreadySelectedMode.HIDE_ALREADY_SELECTED);
		Check.notNull(sprint, "Sprint cannot be null");
		CSpringContext.getBean(CActivityService.class);
		CSpringContext.getBean(CMeetingService.class);
		CSpringContext.getBean(CSprintItemService.class);
		setDynamicHeight("600px");
		LOGGER.debug("CComponentBacklog created for sprint: {}", sprint.getId());
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

	/** Returns the component name for method binding.
	 * <p>
	 * This component uses "backlogItems" as its name for handler binding.
	 * @return The component name "backlogItems" */
	@Override
	public String getComponentName() { return "backlogItems"; }

	@Override
	public boolean isDragEnabled() { return dragEnabled; }

	@Override
	public boolean isDropEnabled() {
		// Backlog can always receive drops from sprint items
		return true;
	}

	/** Registers this component with the page service for automatic event binding.
	 * <p>
	 * This component uses "backlogItems" as its name, enabling automatic binding to page service handlers like
	 * on_backlogItems_drop, on_backlogItems_change, etc.
	 * <p>
	 * Note: dragStart and dragEnd handlers are rarely needed since all drag data is carried in events.
	 * <p>
	 * Note: This method only registers the component. The actual method binding happens when CPageService.bind() is called, which occurs once during
	 * page initialization.
	 * @param pageService The page service to register with */
	@Override
	public void registerWithPageService(final CPageService<?> pageService) {
		Check.notNull(pageService, "Page service cannot be null");
		final String componentName = getComponentName();
		pageService.registerComponent(componentName, this);
		LOGGER.debug("[BindDebug] {} auto-registered with page service as '{}' (binding will occur during CPageService.bind())",
				getClass().getSimpleName(), componentName);
	}

	@Override
	public void setDragEnabled(final boolean enabled) {
		dragEnabled = enabled;
		final var grid = getGrid();
		if (grid != null) {
			grid.setDragEnabled(enabled); // Use CGrid's IHasDragControl method
			// LOGGER.debug("External drag from backlog {}", enabled ? "enabled" : "disabled");
		}
	}

	// IPageServiceAutoRegistrable interface implementation
}
