package tech.derbent.app.sprints.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.enhanced.CComponentBacklog;
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

	/** Reorders backlog items after inserting a new item at a specific sprint order position. All items with sprintOrder >= newOrder need to be
	 * shifted up by 1.
	 * @param newOrder      the sprint order value of the newly inserted item
	 * @param excludeItemId the ID of the item being inserted (to exclude it from reordering) */
	private void reorderBacklogItemsAfterInsert(final int newOrder, final Long excludeItemId) {
		try {
			final List<CProjectItem<?>> allBacklogItems = componentBacklogItems.getAllItems();
			// Shift items with sprintOrder >= newOrder
			for (final CProjectItem<?> item : allBacklogItems) {
				if (item instanceof ISprintableItem && !item.getId().equals(excludeItemId)) {
					final ISprintableItem sprintableItem = (ISprintableItem) item;
					final Integer itemOrder = sprintableItem.getSprintOrder();
					if (itemOrder != null && itemOrder >= newOrder) {
						sprintableItem.setSprintOrder(itemOrder + 1);
						// Save the updated item
						if (item instanceof CActivity) {
							activityService.save((CActivity) item);
						} else if (item instanceof CMeeting) {
							meetingService.save((tech.derbent.app.meetings.domain.CMeeting) item);
						}
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error reordering backlog items after insert", e);
		}
	}

	/** Sets up drag and drop between backlog and sprint items components using the general refresh listener pattern. This enables proper separation
	 * between: - Internal reordering within backlog (handled by backlog component) - Dragging from backlog to sprint items (handled via interface) -
	 * Reverse drag from sprint items back to backlog (removes from sprint) */
	private void setupDragAndDrop() {
		// Only set up if both components exist
		if ((componentBacklogItems != null) && (componentItemsSelection != null)) {
			// === Sprint Items → Backlog Listener (Update-Then-Notify pattern) ===
			// When sprint items change, backlog should refresh itself
			componentItemsSelection.addRefreshListener(changedItem -> {
				LOGGER.debug("Sprint items changed, refreshing backlog");
				componentBacklogItems.refreshGrid();
			});
			// === FORWARD DRAG: Backlog → Sprint Items ===
			// Enable dragging FROM backlog (implements IGridDragDropSupport)
			componentBacklogItems.setDragEnabled(true);
			// Track items dragged from backlog
			final CProjectItem<?>[] draggedFromBacklog = new CProjectItem<?>[1];
			// Listen for drag start from backlog
			final var backlogGrid = componentBacklogItems.getGrid();
			if (backlogGrid != null) {
				backlogGrid.addDragStartListener(event -> {
					if (!event.getDraggedItems().isEmpty()) {
						draggedFromBacklog[0] = event.getDraggedItems().get(0);
						LOGGER.debug("Item drag started from backlog: {}", draggedFromBacklog[0].getId());
					}
				});
				// Clear on drag end
				backlogGrid.addDragEndListener(event -> {
					draggedFromBacklog[0] = null;
				});
			}
			// Configure sprint items to ACCEPT drops from backlog (implements IDropTarget)
			final var sprintItemsGrid = componentItemsSelection.getGrid();
			if (sprintItemsGrid != null) {
				sprintItemsGrid.setDropMode(GridDropMode.BETWEEN);
				// Listen for drops ON sprint items grid FROM backlog
				sprintItemsGrid.addDropListener(event -> {
					if (draggedFromBacklog[0] != null) {
						final CProjectItem<?> itemToAdd = draggedFromBacklog[0];
						final CSprintItem targetItem = event.getDropTargetItem().orElse(null);
						final GridDropLocation dropLocation = event.getDropLocation();
						LOGGER.debug("Item dropped into sprint from backlog: {} at location: {} relative to target: {}", itemToAdd.getId(),
								dropLocation, targetItem != null ? targetItem.getId() : "null");
						// Add to sprint items at the specified position (which will update itself and notify listeners)
						componentItemsSelection.addDroppedItem(itemToAdd, targetItem, dropLocation);
						// Note: componentBacklogItems will refresh via the listener above
						// Clear tracker
						draggedFromBacklog[0] = null;
					}
				});
			}
			// === REVERSE DRAG: Sprint Items → Backlog ===
			// Enable dragging FROM sprint items back to backlog
			componentItemsSelection.setDragToBacklogEnabled(true);
			// Track items dragged from sprint
			final CSprintItem[] draggedFromSprint = new CSprintItem[1];
			// Listen for drag start from sprint items
			if (sprintItemsGrid != null) {
				sprintItemsGrid.addDragStartListener(event -> {
					if (!event.getDraggedItems().isEmpty()) {
						draggedFromSprint[0] = event.getDraggedItems().get(0);
						LOGGER.debug("Sprint item drag started: {} (itemId: {})", draggedFromSprint[0].getId(), draggedFromSprint[0].getItemId());
					}
				});
				// Clear on drag end
				sprintItemsGrid.addDragEndListener(event -> {
					draggedFromSprint[0] = null;
				});
			}
			// Configure backlog to ACCEPT drops from sprint items (implements IDropTarget)
			if (backlogGrid != null) {
				// Listen for drops ON backlog grid FROM sprint items
				backlogGrid.addDropListener(event -> {
					if (draggedFromSprint[0] != null) {
						final CSprintItem sprintItem = draggedFromSprint[0];
						final CProjectItem<?> item = sprintItem.getItem();
						final CProjectItem<?> targetBacklogItem = event.getDropTargetItem().orElse(null);
						final GridDropLocation dropLocation = event.getDropLocation();
						LOGGER.debug("Sprint item dropped back into backlog: {} (itemId: {}) at location: {} relative to target: {}",
								sprintItem.getId(), item != null ? item.getId() : "null", dropLocation,
								targetBacklogItem != null ? targetBacklogItem.getId() : "null");
						if (item != null) {
							// Set sprint order based on drop position
							if (targetBacklogItem != null && dropLocation != null && item instanceof tech.derbent.api.interfaces.ISprintableItem) {
								final tech.derbent.api.interfaces.ISprintableItem sprintableItem = (tech.derbent.api.interfaces.ISprintableItem) item;
								final tech.derbent.api.interfaces.ISprintableItem targetSprintableItem =
										targetBacklogItem instanceof tech.derbent.api.interfaces.ISprintableItem
												? (tech.derbent.api.interfaces.ISprintableItem) targetBacklogItem : null;
								if (targetSprintableItem != null) {
									// Calculate new sprint order based on drop location
									final Integer targetOrder = targetSprintableItem.getSprintOrder();
									if (targetOrder != null) {
										if (dropLocation == GridDropLocation.BELOW) {
											sprintableItem.setSprintOrder(targetOrder + 1);
										} else {
											sprintableItem.setSprintOrder(targetOrder);
										}
										// Save the updated sprint order
										if (item instanceof tech.derbent.app.activities.domain.CActivity) {
											activityService.save((tech.derbent.app.activities.domain.CActivity) item);
										} else if (item instanceof tech.derbent.app.meetings.domain.CMeeting) {
											meetingService.save((tech.derbent.app.meetings.domain.CMeeting) item);
										}
										// Reorder other backlog items
										reorderBacklogItemsAfterInsert(sprintableItem.getSprintOrder(), item.getId());
									}
								}
							}
							// Remove from sprint (which will update itself and notify listeners)
							componentItemsSelection.removeSprintItem(sprintItem);
							// Force refresh backlog to show the item in the correct position
							componentBacklogItems.refreshGrid();
						}
						// Clear tracker
						draggedFromSprint[0] = null;
					}
				});
			}
			LOGGER.debug("Bidirectional drag and drop configured with refresh listeners");
		} else {
			LOGGER.debug("Cannot setup drag and drop - components not yet initialized");
		}
	}
}
