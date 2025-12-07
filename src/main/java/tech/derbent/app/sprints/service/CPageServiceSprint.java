package tech.derbent.app.sprints.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.enhanced.CComponentBacklog;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection;
import tech.derbent.api.ui.component.enhanced.CComponentListSprintItems;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.sprints.view.CComponentWidgetSprint;

/** CPageServiceSprint - Page service for Sprint management UI. Handles UI events and interactions for sprint views. */
public class CPageServiceSprint extends CPageServiceDynamicPage<CSprint>
		implements IPageServiceHasStatusAndWorkflow<CSprint>, IComponentWidgetEntityProvider<CSprint> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceSprint.class);
	private CActivityService activityService;
	private CComponentBacklog componentBacklogItems;
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

	/** Creates and configures the backlog items component for displaying items not in the sprint.
	 * @return configured CComponentBacklog component */
	private CComponentBacklog createBacklogItemsComponent() {
		final CSprint currentSprint = (CSprint) getView().getCurrentEntity();
		if (currentSprint == null) {
			LOGGER.warn("No current sprint available for backlog component");
			// Return empty backlog - will be populated when sprint is selected
			return new CComponentBacklog(new CSprint());
		}
		return new CComponentBacklog(currentSprint);
	}

	public Component createSpritActivitiesComponent() {
		try {
			componentItemsSelection = new CComponentListSprintItems(sprintItemService, activityService, meetingService);
			// Enable drag-and-drop reordering within sprint items
			componentItemsSelection.enableDragAndDropReordering();
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
			// Get the grid from backlog component
			final CGrid<CProjectItem<?>> backlogGrid = componentBacklogItems.getGrid();
			final CGrid<CSprintItem> sprintItemsGrid = componentItemsSelection.getGridItems();
			if (backlogGrid != null && sprintItemsGrid != null) {
				// Track the currently dragged item from backlog
				final CProjectItem<?>[] draggedItemHolder = new CProjectItem<?>[1];
				// Add drag start listener to backlog to track the dragged item
				backlogGrid.addDragStartListener(event -> {
					final List<CProjectItem<?>> draggedItems = event.getDraggedItems();
					if ((draggedItems != null) && !draggedItems.isEmpty()) {
						draggedItemHolder[0] = draggedItems.get(0);
						LOGGER.debug("Drag started from backlog for item: {} ({})", draggedItemHolder[0].getId(),
								draggedItemHolder[0].getClass().getSimpleName());
					}
				});
				// Add drag end listener to clear the dragged item
				backlogGrid.addDragEndListener(event -> {
					LOGGER.debug("Drag ended from backlog");
					draggedItemHolder[0] = null;
				});
				// Configure sprint items grid to accept drops from backlog
				sprintItemsGrid.setDropMode(GridDropMode.BETWEEN);
				// Add drop listener to sprint items grid to handle drops from backlog
				sprintItemsGrid.addDropListener(event -> {
					if (draggedItemHolder[0] != null) {
						final CProjectItem<?> itemToAdd = draggedItemHolder[0];
						LOGGER.debug("Item dropped into sprint items from backlog: {} ({})", itemToAdd.getId(), itemToAdd.getClass().getSimpleName());
						// Add the item to sprint items
						componentItemsSelection.addDroppedItem(itemToAdd);
						// Refresh the backlog to hide the newly added item
						componentBacklogItems.refresh();
						// Clear the holder
						draggedItemHolder[0] = null;
					}
				});
				LOGGER.debug("Drag and drop configured between backlog and sprint items");
			}
		} else {
			LOGGER.debug("Cannot setup drag and drop - components not yet initialized (backlog: {}, items: {})", componentBacklogItems != null,
					componentItemsSelection != null);
		}
	}
}
