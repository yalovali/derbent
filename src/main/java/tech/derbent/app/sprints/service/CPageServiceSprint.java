package tech.derbent.app.sprints.service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection;
import tech.derbent.api.ui.component.enhanced.CComponentListEntityBase;
import tech.derbent.api.ui.component.enhanced.CComponentListSprintItems;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.sprints.view.CComponentWidgetSprint;

/** CPageServiceSprint - Page service for Sprint management UI. Handles UI events and interactions for sprint views. */
public class CPageServiceSprint extends CPageServiceDynamicPage<CSprint>
		implements IPageServiceHasStatusAndWorkflow<CSprint>, IComponentWidgetEntityProvider<CSprint> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceSprint.class);
	private CActivityService activityService;
	private CComponentEntitySelection<CProjectItem<?>> componentBacklogItems;
	private CComponentListSprintItems componentItemsSelection;
	private CMeetingService meetingService;
	private CProjectItemStatusService projectItemStatusService;
	private CSprintItemService sprintItemService;

	public CPageServiceSprint(final IPageServiceImplementer<CSprint> view) {
		super(view);
		// Initialize the service from Spring context
		try {
			projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
			activityService = CSpringContext.getBean(CActivityService.class);
			meetingService = CSpringContext.getBean(CMeetingService.class);
			sprintItemService = CSpringContext.getBean(CSprintItemService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	public Component createSpritActivitiesComponent() {
		try {
			componentItemsSelection = new CComponentListSprintItems(sprintItemService, activityService, meetingService);
			// Set up drop handler to receive items from backlog
			componentItemsSelection.setDropHandler(item -> componentItemsSelection.addDroppedItem(item));
			// Wire up drag and drop between backlog and sprint items if backlog exists
			setupDragAndDrop();
			return componentItemsSelection;
		} catch (final Exception e) {
			LOGGER.error("Failed to create sprint activities component.", e);
			// Fallback to simple div with error message
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading sprint activities component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	public Component createSpritBacklogComponent() {
		try {
			// Initialize backlog items selection component - shows items NOT in the sprint
			if (componentBacklogItems == null) {
				componentBacklogItems = createBacklogItemsComponent();
			}
			// Wire up drag and drop between backlog and sprint items
			setupDragAndDrop();
			return componentBacklogItems;
		} catch (final Exception e) {
			LOGGER.error("Failed to create sprint backlog component.", e);
			// Fallback to simple div with error message
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading sprint backlog component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	/** Creates and configures the backlog items component for displaying items not in the sprint.
	 * @return configured CComponentEntitySelection component */
	private CComponentEntitySelection<CProjectItem<?>> createBacklogItemsComponent() {
		// Create entity type configurations for activities and meetings
		final List<CComponentEntitySelection.EntityTypeConfig<?>> entityTypes = new ArrayList<>();
		entityTypes.add(new CComponentEntitySelection.EntityTypeConfig<>("CActivity", tech.derbent.app.activities.domain.CActivity.class,
				activityService));
		entityTypes.add(new CComponentEntitySelection.EntityTypeConfig<>("CMeeting", tech.derbent.app.meetings.domain.CMeeting.class,
				meetingService));
		// Items provider - loads all project items
		final CComponentEntitySelection.ItemsProvider<CProjectItem<?>> itemsProvider = config -> {
			try {
				final CSprint sprint = (CSprint) getView().getCurrentEntity();
				if (sprint == null || sprint.getProject() == null) {
					LOGGER.warn("No sprint or project available for loading backlog items");
					return new ArrayList<>();
				}
				if (config.getEntityClass() == tech.derbent.app.activities.domain.CActivity.class) {
					return (List<CProjectItem<?>>) (List<?>) activityService.listByProject(sprint.getProject());
				} else if (config.getEntityClass() == tech.derbent.app.meetings.domain.CMeeting.class) {
					return (List<CProjectItem<?>>) (List<?>) meetingService.listByProject(sprint.getProject());
				}
				return new ArrayList<>();
			} catch (final Exception e) {
				LOGGER.error("Error loading backlog items for entity type: {}", config.getDisplayName(), e);
				return new ArrayList<>();
			}
		};
		// Already selected provider - returns items currently in the sprint to hide them
		final CComponentEntitySelection.ItemsProvider<CProjectItem<?>> alreadySelectedProvider = config -> {
			try {
				final CSprint sprint = (CSprint) getView().getCurrentEntity();
				if (sprint == null || sprint.getId() == null) {
					return new ArrayList<>();
				}
				// Get current sprint items
				final List<CSprintItem> sprintItems = sprintItemService.findByMasterIdWithItems(sprint.getId());
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
		// Selection change handler - just logs for now, drag & drop will handle actual addition
		final java.util.function.Consumer<java.util.Set<CProjectItem<?>>> onSelectionChanged = selectedItems -> {
			LOGGER.debug("Backlog selection changed: {} items selected", selectedItems.size());
		};
		// Create component with HIDE_ALREADY_SELECTED mode so sprint items are not shown in backlog
		return new CComponentEntitySelection<>(entityTypes, itemsProvider, onSelectionChanged, true, alreadySelectedProvider,
				CComponentEntitySelection.AlreadySelectedMode.HIDE_ALREADY_SELECTED);
	}

	/** Creates a widget component for displaying the given sprint entity.
	 * @param item the sprint to create a widget for
	 * @return the CComponentWidgetSprint component */
	@Override
	public Component getComponentWidget(final CSprint item) {
		return new CComponentWidgetSprint(item);
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return projectItemStatusService; }

	public void on_description_blur(final Component component, final Object value) {
		LOGGER.info("function: on_description_blur for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	public void on_description_focus(final Component component, final Object value) {
		LOGGER.info("function: on_description_focus for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	public void on_name_change(final Component component, final Object value) {
		LOGGER.info("function: on_name_change for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	public void on_status_change(final Component component, final Object value) {
		LOGGER.info("function: on_status_change for Component type: {}",
				component.getClass().getSimpleName() + " current value: " + value + " on page service:" + this.getClass().getSimpleName());
	}

	@Override
	public void populateForm() {
		LOGGER.debug("populateForm called - CComponentListSprintItems receives entity updates via IContentOwner interface");
	}

	/** Sets up drag and drop between backlog and sprint items components. Enables dragging from backlog and dropping into sprint items. */
	private void setupDragAndDrop() {
		// Only set up if both components exist
		if ((componentBacklogItems != null) && (componentItemsSelection != null)) {
			// Enable drag from backlog component
			componentBacklogItems.setDragEnabled(true);
			// Set drop handler on backlog to handle when items are dragged
			componentBacklogItems.setDropHandler(item -> {
				LOGGER.debug("Item dragged from backlog: {} ({})", item.getId(), item.getClass().getSimpleName());
				// Add the item to sprint items
				componentItemsSelection.addDroppedItem(item);
				// Refresh the backlog to hide the newly added item
				componentBacklogItems.refresh();
			});
			LOGGER.debug("Drag and drop configured between backlog and sprint items");
		} else {
			LOGGER.debug("Cannot setup drag and drop - components not yet initialized (backlog: {}, items: {})",
					componentBacklogItems != null, componentItemsSelection != null);
		}
	}
}
