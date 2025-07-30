package tech.derbent.decisions.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CAccordionDescription;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.service.CDecisionService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/**
 * CDecisionsView - Main view for decision management. Layer: View (MVC) Provides a
 * complete decision management interface following the established patterns from
 * CActivitiesView. Includes grid listing, detail editing, and comprehensive panel
 * organization.
 */
@Route ("decisions/:decision_id?/:action?(edit)")
@PageTitle ("Decision Master Detail")
@Menu (order = 1.5, icon = "vaadin:gavel", title = "Project.Decisions")
@PermitAll // When security is enabled, allow all authenticated users
public class CDecisionsView extends CProjectAwareMDPage<CDecision> {

	private static final long serialVersionUID = 1L;

	private final String ENTITY_ID_FIELD = "decision_id";

	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "decisions/%s/edit";

	/**
	 * Constructor for decisions view.
	 * @param entityService  decision service for business logic operations
	 * @param sessionService session service for project context
	 */
	public CDecisionsView(final CDecisionService entityService,
		final CSessionService sessionService) {
		super(CDecision.class, entityService, sessionService);
		addClassNames("decisions-view");
	}

	/**
	 * Creates the entity details section using decision panels. Follows the same pattern
	 * as CActivitiesView for consistency.
	 */
	@Override
	protected void createDetailsLayout() {
		CAccordionDescription<CDecision> panel;
		// Basic Information Panel (opened by default)
		panel = new CPanelDecisionDescription(getCurrentEntity(), getBinder(),
			(CDecisionService) entityService);
		addAccordionPanel(panel);
		// Status & Workflow Panel
		panel = new CPanelDecisionStatusManagement(getCurrentEntity(), getBinder(),
			(CDecisionService) entityService);
		addAccordionPanel(panel);
		// Cost & Financial Impact Panel
		panel = new CPanelDecisionCostManagement(getCurrentEntity(), getBinder(),
			(CDecisionService) entityService);
		addAccordionPanel(panel);
		// Team & Accountability Panel
		panel = new CPanelDecisionTeamManagement(getCurrentEntity(), getBinder(),
			(CDecisionService) entityService);
		addAccordionPanel(panel);
	}

	@Override
	protected void createGridForEntity() {
		// Configure grid columns for decision display
		grid.addShortTextColumn(CDecision::getProjectName, "Project", "project");
		grid.addShortTextColumn(CDecision::getName, "Decision Name", "name");
		grid.addReferenceColumn(decision -> decision.getDecisionType() != null
			? decision.getDecisionType().getName() : "No Type", "Type");
		grid.addShortTextColumn(decision -> decision.getDecisionStatus() != null
			? decision.getDecisionStatus().getName() : "No Status", "Status", null);
		grid.addShortTextColumn(
			decision -> decision.getAccountableUser() != null
				? decision.getAccountableUser().getName() : "Unassigned",
			"Accountable", null);
		grid.addColumn(decision -> decision.getEstimatedCost() != null
			? decision.getEstimatedCost().toString() : "No Cost", "Est. Cost", null);
		grid.addColumn(decision -> decision.getApprovalCount() > 0
			? decision.getApprovedCount() + "/" + decision.getApprovalCount()
			: "No Approvals", "Approvals", null);
		grid.addColumn(
			decision -> decision.getCreatedDate() != null
				? decision.getCreatedDate().toLocalDate().toString() : "",
			"Created", null);
		// Add selection listener to navigate to edit view
		grid.asSingleSelect().addValueChangeListener(event -> {

			if (event.getValue() != null) {
				UI.getCurrent().navigate(
					String.format(ENTITY_ROUTE_TEMPLATE_EDIT, event.getValue().getId()));
			}
			else {
				clearForm();
				UI.getCurrent().navigate(CDecisionsView.class);
			}
		});
	}

	@Override
	protected CDecision createNewEntityInstance() {
		return new CDecision();
	}

	/**
	 * Shows approval status for a decision.
	 * @param decision the decision to show approval status for
	 * @return formatted approval status string
	 */
	protected String formatApprovalStatus(final CDecision decision) {

		if (decision == null) {
			return "Unknown";
		}

		if (decision.getApprovalCount() == 0) {
			return "No approvals required";
		}
		final boolean isFullyApproved = decision.isFullyApproved();
		final int approvedCount = decision.getApprovedCount();
		final int totalCount = decision.getApprovalCount();
		final String status = isFullyApproved ? "Fully Approved" : "Pending";
		return String.format("%s (%d/%d)", status, approvedCount, totalCount);
	}

	/**
	 * Gets the current decision service.
	 * @return the decision service instance
	 */
	protected CDecisionService getDecisionService() {
		return (CDecisionService) entityService;
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	@Override
	protected void setProjectForEntity(final CDecision entity, final CProject project) {

		if (entity != null) {
			entity.setProject(project);
		}
	}

	@Override
	protected void setupToolbar() {
		// Setup toolbar if needed - following the pattern from CActivitiesView
	}
}