package tech.derbent.activities.view;

import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CAccordionDescription;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.SessionService;

@Route ("activities/:activity_id?/:action?(edit)")
@PageTitle ("Activity Master Detail")
@Menu (order = 0, icon = "vaadin:calendar-clock", title = "Project.Activities")
@PermitAll // When security is enabled, allow all authenticated users
public class CActivitiesView extends CProjectAwareMDPage<CActivity> {

	private static final long serialVersionUID = 1L;

	private final String ENTITY_ID_FIELD = "activity_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "activities/%s/edit";

	public CActivitiesView(final CActivityService entityService,
		final SessionService sessionService) {
		super(CActivity.class, entityService, sessionService);
		addClassNames("activities-view");
	}

	/**
	 * Creates the entity details section using CPanelActivityDescription. Follows the
	 * same pattern as CUsersView for consistency.
	 */
	@Override
	protected void createDetailsLayout() {
		CAccordionDescription<CActivity> panel;
		panel = new CPanelActivityDescription(getCurrentEntity(), getBinder(),
			(CActivityService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelActivityProject(getCurrentEntity(), getBinder(),
			(CActivityService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelActivityResourceManagement(getCurrentEntity(), getBinder(),
			(CActivityService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelActivityStatusPriority(getCurrentEntity(), getBinder(),
			(CActivityService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelActivityTimeTracking(getCurrentEntity(), getBinder(),
			(CActivityService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelActivityHierarchy(getCurrentEntity(), getBinder(),
			(CActivityService) entityService);
		addAccordionPanel(panel);
		panel = new CPanelActivityBudgetManagement(getCurrentEntity(), getBinder(),
			(CActivityService) entityService);
		addAccordionPanel(panel);
	}

	@Override
	protected void createGridForEntity() {
		LOGGER.info(
			"Creating enhanced grid for activities with comprehensive field coverage");
		// Project Name - Important for context (similar to meetings view)
		grid.addShortTextColumn(CActivity::getProjectName, "Project", "project");
		grid.addShortTextColumn(CActivity::getName, "Activity Name", "name");
		// grid.addReferenceColumn(activity -> { return activity.getActivityType() != null
		// ? activity.getActivityType().getName() : "No Type"; }, "Type"); // Type is
		// derived from join, not directly sortable // Assigned To - User assigned to
		// activity (critical for resource management) grid.addReferenceColumn(activity ->
		// { return activity.getAssignedTo() != null ? activity.getAssignedTo().getName()
		// : "Unassigned"; }, "Assigned To"); // User is derived from join, not directly
		// sortable // Status - Current status of activity
		// grid.addReferenceColumn(activity -> { return activity.getStatus() != null ?
		// activity.getStatus().getName() : "No Status"; }, "Status"); // Status is
		// derived from join, not directly sortable // Priority - Important for task
		// prioritization grid.addReferenceColumn(activity -> { return
		// activity.getPriority() != null ? activity.getPriority().getName() : "No
		// Priority"; }, "Priority"); // Priority is derived from join, not directly
		// sortable // Start Date - Timeline information with proper formatting
		// grid.addDateColumn(activity -> activity.getStartDate(), "Start Date",
		// "startDate"); // Due Date - Timeline information with proper formatting
		// grid.addDateColumn(activity -> activity.getDueDate(), "Due Date", "dueDate");
		// // Progress Percentage - Completion tracking grid.addIntegerColumn(activity ->
		// activity.getProgressPercentage(), "Progress %", "progressPercentage"); //
		// Description - Shortened for grid display (similar to meetings view)
		// grid.addColumn(activity -> { if ((activity.getDescription() == null) ||
		// activity.getDescription().trim().isEmpty()) { return "No description"; } final
		// String desc = activity.getDescription().trim(); return desc.length() > 50 ?
		// desc.substring(0, 47) + "..." : desc; }, "Description", "description");
		/*
		 * grid.addReferenceColumn(activity -> { if ((activity.getDescription() == null)
		 * || activity.getDescription().trim().isEmpty()) { return "No description"; }
		 * final String desc = activity.getDescription().trim(); return desc.length() > 50
		 * ? desc.substring(0, 47) + "..." : desc; }, "Description"); // Description is
		 * not directly sortable
		 */
		// when a row is selected or deselected, populate form
		grid.asSingleSelect().addValueChangeListener(event -> {

			if (event.getValue() != null) {
				UI.getCurrent().navigate(
					String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
			}
			else {
				clearForm();
				UI.getCurrent().navigate(CActivitiesView.class);
			}
		});
	}

	// private final BeanValidationBinder<CProject> binder; private final CProjectService
	// userService; private final Grid<CProject> grid;// = new Grid<>(CProject.class,
	// false);
	@Override
	protected CActivity createNewEntityInstance() {
		return new CActivity();
	}

	@Override
	protected String getEntityRouteIdField() { // TODO Auto-generated method stub
		return ENTITY_ID_FIELD;
	}

	@Override
	protected String getEntityRouteTemplateEdit() { // TODO Auto-generated method stub
		return ENTITY_ROUTE_TEMPLATE_EDIT;
	}

	@Override
	protected List<CActivity> getProjectFilteredData(final CProject project,
		final org.springframework.data.domain.Pageable pageable) {
		return ((CActivityService) entityService).listByProject(project, pageable)
			.getContent();
	}

	@Override
	protected CActivity newEntity() {
		return super.newEntity(); // Uses the project-aware implementation from parent
	}

	@Override
	protected void setProjectForEntity(final CActivity entity, final CProject project) {
		entity.setProject(project);
	}

	@Override
	protected void setupToolbar() {
		// TODO Auto-generated method stub
	}
}