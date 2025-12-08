package tech.derbent.api.ui.component.enhanced;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.base.session.service.ISessionService;

/** CComponentItemDetailsDemo - Demonstration page showing CComponentItemDetails in action.
 * <p>
 * This demo page shows a typical use case: a grid of activities on the left, and when an activity is selected, its details are displayed on the right
 * using CComponentItemDetails.
 * </p>
 * @author Derbent Framework
 * @since 1.0 */
public class CComponentItemDetailsDemo extends VerticalLayout {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentItemDetailsDemo.class);
	private static final long serialVersionUID = 1L;
	private final CActivityService activityService;
	private CGrid<CActivity> gridActivities;
	private CComponentItemDetails itemDetails;
	private final ISessionService sessionService;

	@Autowired
	public CComponentItemDetailsDemo(final ISessionService sessionService, final CPageEntityService pageEntityService,
			final CDetailSectionService detailSectionService, final CActivityService activityService) throws Exception {
		this.sessionService = sessionService;
		this.activityService = activityService;
		setupComponent();
	}

	/** Creates the activity grid on the left side */
	private CGrid<CActivity> create_gridActivities() {
		final CGrid<CActivity> grid = new CGrid<>(CActivity.class);
		grid.setWidthFull();
		grid.setHeight("600px");
		// Configure grid columns
		grid.addColumn(CActivity::getName).setHeader("Activity Name").setFlexGrow(1);
		grid.addColumn(activity -> activity.getStatus() != null ? activity.getStatus().getName() : "").setHeader("Status").setWidth("150px");
		// Load activities
		try {
			grid.setItems(activityService.findAll());
		} catch (final Exception e) {
			LOGGER.error("Error loading activities", e);
		}
		// When an activity is selected, display its details
		grid.asSingleSelect().addValueChangeListener(event -> {
			final CActivity selectedActivity = event.getValue();
			LOGGER.debug("Activity selected: {}", selectedActivity != null ? selectedActivity.getName() : "null");
			// Set the value in itemDetails - it will automatically display the activity's detail page
			itemDetails.setValue(selectedActivity);
		});
		return grid;
	}

	/** Creates the item details component on the right side */
	private CComponentItemDetails create_itemDetails() throws Exception {
		final CComponentItemDetails details = new CComponentItemDetails(sessionService);
		details.setWidthFull();
		details.setHeight("600px");
		// Add value change listener to log changes
		details.addValueChangeListener(event -> {
			final CEntityNamed<?> newValue = event.getValue();
			LOGGER.debug("Item details value changed to: {}", newValue != null ? newValue.getName() : "null");
		});
		return details;
	}

	/** Sets up the demo page layout */
	private void setupComponent() throws Exception {
		setWidthFull();
		setPadding(true);
		setSpacing(true);
		// Title
		add(new CH3("CComponentItemDetails Demo"));
		// Create main content area with grid and details side by side
		final CHorizontalLayout layoutContent = new CHorizontalLayout();
		layoutContent.setWidthFull();
		layoutContent.setSpacing(true);
		// Left side: Grid of activities
		gridActivities = create_gridActivities();
		layoutContent.add(gridActivities);
		// Right side: Item details
		itemDetails = create_itemDetails();
		layoutContent.add(itemDetails);
		// Both sides should expand equally
		layoutContent.setFlexGrow(1, gridActivities);
		layoutContent.setFlexGrow(1, itemDetails);
		add(layoutContent);
		LOGGER.debug("CComponentItemDetailsDemo initialized");
	}
}
