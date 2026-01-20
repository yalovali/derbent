package tech.derbent.plm.sprints.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dnd.DropEvent;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntityOfProject;
import tech.derbent.api.interfaces.IEntityUpdateListener;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentListSprintItems;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.meetings.service.CMeetingService;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.domain.CSprintItem;
import tech.derbent.plm.sprints.service.CSprintItemService;

/** CComponentWidgetSprint - Widget component for displaying Sprint entities in grids.
 * <p>
 * This widget displays sprint information in a three-row layout:
 * <ul>
 * <li><b>Row 1:</b> Sprint name with calendar icon and sprint color</li>
 * <li><b>Row 2:</b> Sprint type badge, item count with colorful display (clickable to expand/collapse)</li>
 * <li><b>Row 3:</b> Status badge, responsible user, and date range with calendar icons</li>
 * </ul>
 * </p>
 * <p>
 * When the item count is clicked, a collapsible section opens showing the sprint items in a grid using CComponentListSprintItems. The component
 * automatically refreshes the item count when items are added or removed.
 * </p>
 * <p>
 * Extends CComponentWidgetEntityOfProject and adds sprint-specific information like item count and sprint type. Uses CLabelEntity for colorful,
 * visually appealing badges and labels.
 * </p>
 * <p>
 * Implements IHasDragStart, IHasDragEnd, and IHasDrop to propagate drag-drop events from the internal sprint items grid to external listeners (e.g.,
 * page services). This enables automatic method binding in CPageService for drag-drop operations and supports reordering items within the sprint.
 * </p>
 * @author Derbent Framework
 * @since 1.0
 * @see CComponentWidgetEntityOfProject */
public class CComponentWidgetSprint extends CComponentWidgetEntityOfProject<CSprint> implements IEntityUpdateListener<CSprintItem> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentWidgetSprint.class);
	private static final long serialVersionUID = 1L;
	private CButton buttonToggleItems;
	private CComponentListSprintItems componentSprintItems;
	private CDiv containerSprintItems;
	private DropTarget<CComponentWidgetSprint> dropTarget;
	private CLabelEntity itemCountLabel;
	private CSpan itemCountText;
	private boolean sprintItemsVisible = false;

	/** Creates a new sprint widget for the specified sprint.
	 * @param sprint the sprint to display in the widget */
	public CComponentWidgetSprint(final CSprint sprint) {
		super(sprint);
		drag_initialize();
	}

	@Override
	protected void createFirstLine() throws Exception {
		super.createFirstLine();
		// Show sprint type with color if available
		if (getEntity().getEntityType() != null) {
			final CLabelEntity typeLabel = new CLabelEntity();
			typeLabel.setValue(getEntity().getEntityType(), true);
			typeLabel.getStyle().set("margin-right", "8px");
			layoutLineOne.add(typeLabel);
		}
	}

	/** Creates the item count label with icon and styling.
	 * @return the configured label */
	private CLabelEntity createItemCountLabel() {
		final Integer itemCount = getEntity().getItemCount();
		final Long totalStoryPoints = getEntity().getTotalStoryPoints();
		final CLabelEntity label = new CLabelEntity();
		label.getStyle().set("display", "flex").set("align-items", "center").set("gap", "4px").set("background-color", "#E3F2FD") // Light blue
				// background
				.set("color", "#1976D2") // Blue text
				.set("padding", "4px 8px").set("border-radius", "4px").set("font-size", "10pt").set("font-weight", "500");
		// Add tasks icon
		final Icon icon = VaadinIcon.TASKS.create();
		icon.getStyle().set("width", "14px").set("height", "14px").set("color", "#1976D2");
		label.add(icon);
		// Add count text with story points
		final String countText = (itemCount != null ? itemCount : 0) + " item" + (itemCount != null && itemCount != 1 ? "s" : "");
		final String storyPointsText = totalStoryPoints != null && totalStoryPoints > 0 ? " (" + totalStoryPoints + " SP)" : "";
		itemCountText = new CSpan(countText + storyPointsText);
		label.add(itemCountText);
		return label;
	}

	/** Creates the second line with sprint type and item count. This line shows colorful badges for sprint type and item count. The item count is
	 * clickable to show/hide the sprint items component.
	 * @throws Exception */
	
	@Override
	protected void createSecondLine() throws Exception {
		final CVerticalLayout layoutMid = new CVerticalLayout();
		final CHorizontalLayout layoutMidLineOne = new CHorizontalLayout();
		createSprintItemsComponent();
		layoutMid.add(layoutMidLineOne, containerSprintItems);
		// Show item count with icon and colorful badge - make it clickable
		itemCountLabel = createItemCountLabel();
		// Make the label clickable
		itemCountLabel.getStyle().set("cursor", "pointer");
		itemCountLabel.addClickListener(event -> on_itemCountLabel_clicked());
		layoutMidLineOne.add(itemCountLabel);
		layoutLineTwo.add(layoutMid);
	}

	/** Creates the sprint items component with the list of sprint items. This component is shown/hidden when the item count is clicked.
	 * @throws Exception if component creation fails */
	
	private void createSprintItemsComponent() throws Exception {
		if (componentSprintItems != null) {
			return; // Already created
		}
		try {
			// Get services from Spring context
			final CSprintItemService sprintItemService = CSpringContext.getBean(CSprintItemService.class);
			final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
			final CMeetingService meetingService = CSpringContext.getBean(CMeetingService.class);
			// Create the component
			componentSprintItems = new CComponentListSprintItems(sprintItemService, activityService, meetingService);
			// Configure for widget mode with dynamic height (max 400px)
			componentSprintItems.setDynamicHeight("400px");
			// Set the current entity (sprint)
			componentSprintItems.setValue(getEntity());
			// Use the general refresh listener pattern instead of the old setOnItemChangeListener
			componentSprintItems.addRefreshListener(event -> refreshItemCount());
			// Enable drag-drop on the grid for external drag-drop operations
			componentSprintItems.drag_setDragEnabled(true);
			componentSprintItems.drag_setDropEnabled(true);
			// Set up drag-drop event forwarding from componentSprintItems to this widget
			// This ensures events from the internal grid propagate up through the widget hierarchy
			setupChildDragDropForwarding(componentSprintItems);
			// Create container for sprint items
			containerSprintItems = new CDiv();
			containerSprintItems.getStyle().set("margin-top", "8px").set("padding", "8px").set("background-color", "#F5F5F5")
					.set("border-radius", "4px").set("border", "1px solid #E0E0E0");
			// Create and add toggle button to the toolbar of sprint items component
			buttonToggleItems = new CButton(VaadinIcon.ANGLE_UP.create());
			buttonToggleItems.setTooltipText("Hide sprint items");
			buttonToggleItems.addClickListener(event -> on_buttonToggleItems_clicked());
			// Add toggle button to the toolbar (next to other CRUD buttons)
			componentSprintItems.getLayoutToolbar().addComponentAsFirst(buttonToggleItems);
			// Add only the component to the container (button is now in its toolbar)
			containerSprintItems.add(componentSprintItems);
			containerSprintItems.setVisible(false); // Initially hidden
		} catch (final Exception e) {
			LOGGER.error("Failed to create sprint items component for sprint {}", getEntity().getId(), e);
			CNotificationService.showException("Failed to load sprint items", e);
		}
	}

	@Override
	public void drag_checkEventAfterPass(CEvent event) {
		super.drag_checkEventAfterPass(event);
		refreshComponent();
	}

	void drag_initialize() {
		dropTarget = DropTarget.create(this);
		dropTarget.addDropListener(drag_on_component_drop());
		dropTarget.setActive(true);
	}

	
	private ComponentEventListener<DropEvent<CComponentWidgetSprint>> drag_on_component_drop() {
		return event -> {
			try {
				// LOGGER.debug("Drop event details: Drag source id: {}, Drop target id: {}",
				// getId().orElse("None"),event.getSource().getId().orElse("None"));
				final CDragDropEvent dropEvent = new CDragDropEvent(getId().orElse("None"), this, getEntity(), null, true);
				notifyEvents(dropEvent);
			} catch (final Exception e) {
				LOGGER.error("Error handling grid drop event", e);
			}
		};
	}

	public CGrid<?> getGrid() { return componentSprintItems.getGrid(); }

	/** Handle click on the item count label. Toggles visibility of the sprint items component. */
	protected void on_buttonToggleItems_clicked() {
		try {
			sprintItemsVisible = !sprintItemsVisible;
			syncToggleButtonState();
		} catch (final Exception e) {
			LOGGER.error("Error toggling sprint items visibility", e);
			CNotificationService.showException("Error toggling sprint items", e);
		}
	}

	/** Handle click on the item count label. Toggles visibility of the sprint items component. */
	protected void on_itemCountLabel_clicked() {
		try {
			sprintItemsVisible = !sprintItemsVisible;
			syncToggleButtonState();
		} catch (final Exception e) {
			LOGGER.error("Error toggling sprint items visibility", e);
			CNotificationService.showException("Error toggling sprint items", e);
		}
	}

	@Override
	public void onEntityCreated(final CSprintItem newEntity) throws Exception {
		refreshItemCount();
	}

	@Override
	public void onEntityDeleted(final CSprintItem entity1) throws Exception {
		refreshItemCount();
	}

	@Override
	public void onEntityRefreshed(final CSprintItem reloaded) throws Exception {
		refreshItemCount();
	}
	// IHasDragStart interface implementation - propagate drag events from internal grid

	@Override
	public void onEntitySaved(final CSprintItem savedEntity) throws Exception {
		refreshItemCount();
	}
	// IHasDragEnd interface implementation - propagate drag events from internal grid

	/** Refresh this widget by reloading sprint items (if present) and updating the item count label. */
	public void refreshComponent() {
		if (getEntity() != null && getEntity().getId() != null) {
			final CSprintItemService sprintItemService = CSpringContext.getBean(CSprintItemService.class);
			final var items = sprintItemService.findByMasterIdWithItems(getEntity().getId());
			getEntity().setSprintItems(items);
			if (componentSprintItems != null) {
				componentSprintItems.setValue(getEntity());
				componentSprintItems.refreshGrid();
			}
		}
		refreshItemCount();
		syncToggleButtonState();
	}

	/** Refresh the item count display by recreating the label with updated count. */
	private void refreshItemCount() {
		Check.notNull(itemCountLabel, "Item count label must be initialized");
		Check.notNull(itemCountText, "Item count text must be initialized");
		final Integer itemCount = getEntity().getItemCount();
		final Long totalStoryPoints = getEntity().getTotalStoryPoints();
		final String countText = (itemCount != null ? itemCount : 0) + " item" + (itemCount != null && itemCount != 1 ? "s" : "");
		final String storyPointsText = totalStoryPoints != null && totalStoryPoints > 0 ? " (" + totalStoryPoints + " SP)" : "";
		itemCountText.setText(countText + storyPointsText);
	}

	private void syncToggleButtonState() {
		if (containerSprintItems != null) {
			containerSprintItems.setVisible(sprintItemsVisible);
		}
		if (buttonToggleItems != null) {
			if (sprintItemsVisible) {
				buttonToggleItems.setIcon(VaadinIcon.ANGLE_UP.create());
				buttonToggleItems.setTooltipText("Hide sprint items");
			} else {
				buttonToggleItems.setIcon(VaadinIcon.ANGLE_DOWN.create());
				buttonToggleItems.setTooltipText("Show sprint items");
			}
		}
	}
	// IDropTarget implementation
}
