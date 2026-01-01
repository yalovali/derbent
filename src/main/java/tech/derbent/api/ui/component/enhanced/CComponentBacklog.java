package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ComponentEventListener;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.CSelectEvent;
import tech.derbent.api.interfaces.IHasSelectionNotification;
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

public class CComponentBacklog extends CComponentEntitySelection<CProjectItem<?>> implements IPageServiceAutoRegistrable, IHasSelectionNotification {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentBacklog.class);
	private static final long serialVersionUID = 1L;

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

	/** Selection listeners for notification pattern */
	private final Set<ComponentEventListener<CSelectEvent>> selectListeners = new HashSet<>();
	private final CActivityService activityService;
	private final boolean compactMode;
	private final CMeetingService meetingService;
	/** Currently selected backlog item for detail display */
	private CProjectItem<?> selectedBacklogItem;

	/** Constructor for backlog component.
	 * @param project project to load backlog items for (required) */
	public CComponentBacklog(final CProject project) {
		this(project, false);
	}

	/** Constructor for backlog component with compact mode option.
	 * @param project     project to load backlog items for (required)
	 * @param compactMode true for compact display (only name column in grid, only type selector in toolbar), false for full display */
	public CComponentBacklog(final CProject project, final boolean compactMode) {
		super(createEntityTypes(), createItemsProvider(project), createSelectionHandler(), false, null, AlreadySelectedMode.HIDE_ALREADY_SELECTED);
		Check.notNull(project, "Project cannot be null");
		this.compactMode = compactMode;
		activityService = CSpringContext.getBean(CActivityService.class);
		meetingService = CSpringContext.getBean(CMeetingService.class);
		setDynamicHeight("600px");
		LOGGER.debug("CComponentBacklog created for project: {} (compact mode: {})", project.getId(), compactMode);
	}

	@Override
	public void configureGrid(final CGrid<CProjectItem<?>> grid) {
		// Clear existing columns first
		grid.getColumns().forEach(grid::removeColumn);
		// In compact mode, only show name column
		if (compactMode) {
			grid.addShortTextColumn(item -> {
				if (item instanceof CEntityNamed) {
					return ((CEntityNamed<?>) item).getName();
				}
				return item.toString();
			}, "Name", "name");
			LOGGER.debug("Configured backlog grid in compact mode - only name column visible");
		} else {
			// In normal mode, call parent to configure standard columns
			super.configureGrid(grid);
			// Add story point column
			grid.addStoryPointColumn(item -> {
				Check.instanceOf(item, ISprintableItem.class, "Backlog item must implement ISprintableItem");
				return (ISprintableItem) item;
			}, this::saveStoryPoint, this::handleStoryPointError, "Story Points", "storyPoint");
		}
	}

	/** Factory method for search toolbar - overridden to support compact mode configuration. */
	@Override
	protected CComponentFilterToolbar create_gridSearchToolbar() {
		// Create toolbar with compact config if needed
		final CComponentGridSearchToolbar.ToolbarConfig config = new CComponentGridSearchToolbar.ToolbarConfig();
		if (compactMode) {
			// Compact mode: hide all filters, leaving only the type selector combobox
			config.setIdFilter(false).setNameFilter(false).setDescriptionFilter(false).setStatusFilter(false).setClearButton(false);
		} else {
			// Normal mode: show all filters
			config.showAll();
		}
		final CComponentFilterToolbar toolbar = new CComponentFilterToolbar(config);
		return toolbar;
	}

	@Override
	public void drag_checkEventAfterPass(CEvent event) {
		super.drag_checkEventAfterPass(event);
		refreshComponent();
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

	/** Gets the currently selected backlog item.
	 * @return The selected project item or null if no selection */
	public CProjectItem<?> getSelectedBacklogItem() { return selectedBacklogItem; }

	private void handleStoryPointError(final Exception exception) {
		Check.notNull(exception, "Exception cannot be null when handling story point errors");
		CNotificationService.showException("Error saving story points", exception);
	}

	/** Overridden to propagate selection events to listeners (e.g., kanban board). When an item is selected in the backlog grid, this notifies the
	 * parent container to display the item details in the entity detail view. */
	@Override
	protected void on_gridItems_singleSelectionChanged(final CProjectItem<?> value) {
		super.on_gridItems_singleSelectionChanged(value);
		// Store selected item for retrieval by parent
		selectedBacklogItem = value;
		// Propagate selection event to listeners (following kanban postit pattern)
		if (value != null) {
			LOGGER.debug("Backlog item selected: {} ({})", value.getId(), value.getClass().getSimpleName());
			select_notifyEvents(new CSelectEvent(this, true));
		} else {
			LOGGER.debug("Backlog selection cleared");
			select_notifyEvents(new CSelectEvent(this, true));
		}
	}

	/** Refresh the backlog component and underlying grid. */
	public void refreshComponent() {
		refreshGrid();
	}

	@Override
	public void registerWithPageService(final CPageService<?> pageService) {
		Check.notNull(pageService, "Page service cannot be null");
		final String componentName = getComponentName();
		pageService.registerComponent(componentName, this);
		LOGGER.debug("[BindDebug] {} auto-registered with page service as '{}' (binding will occur during CPageService.bind())",
				getClass().getSimpleName(), componentName);
	}
	// IHasSelectionNotification implementation

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

	@Override
	public void select_checkEventAfterPass(final CEvent event) {
		LOGGER.debug("[BacklogSelect] Selection event propagated");
	}

	@Override
	public void select_checkEventBeforePass(final CEvent event) {
		Check.notNull(event, "Selection event cannot be null for backlog");
		LOGGER.debug("[BacklogSelect] Processing selection event from backlog");
	}

	@Override
	public Set<ComponentEventListener<CSelectEvent>> select_getSelectListeners() {
		return selectListeners;
	}
}
