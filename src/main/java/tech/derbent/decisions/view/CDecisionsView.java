package tech.derbent.decisions.view;

import java.util.List;
import java.util.Optional;
import org.springframework.util.Assert;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.service.CDecisionService;
import tech.derbent.decisions.service.CDecisionViewService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

/** CDecisionsView - Main view for decision management. Layer: View (MVC) Provides a complete decision management interface following the established
 * patterns from CActivitiesView. Includes grid listing, detail editing, and comprehensive panel organization. */
@Route ("cdecisionsview/:decision_id?/:action?(edit)")
@PageTitle ("Decision Master Detail")
@Menu (order = 1.5, icon = "class:tech.derbent.decisions.view.CDecisionsView", title = "Project.Decisions")
@PermitAll // When security is enabled, allow all authenticated users
public class CDecisionsView extends CProjectAwareMDPage<CDecision> implements CInterfaceIconSet {
	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CDecision.getIconColorCode(); // Use the static method from CDecision
	}

	public static String getIconFilename() { return CDecision.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "decision_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "cdecisionsview/%s/edit";

	/** Constructor for decisions view.
	 * @param entityService  decision service for business logic operations
	 * @param sessionService session service for project context */
	public CDecisionsView(final CDecisionService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(CDecision.class, entityService, sessionService, screenService);
	}

	/** Creates the entity details section using decision panels. Follows the same pattern as CActivitiesView for consistency.
	 * @throws Exception */
	@Override
	protected void createDetailsLayout() throws Exception {
		// final CAccordionDBEntity<CDecision> panel;
		// panel = new CPanelDecisionTeamManagement(getCurrentEntity(), getBinder(), (CDecisionService) entityService);
		// addAccordionPanel(panel);
		buildScreen(CDecisionViewService.BASE_VIEW_NAME);
	}

	@Override
	protected void createGridForEntity() {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumnEntityNamed(CDecision::getDecisionType, "Type");
		grid.addColumnEntityNamed(CDecision::getDecisionStatus, "Status");
		grid.addColumnEntityNamed(CDecision::getAccountableUser, "Accountable");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addColumn(decision -> decision.getEstimatedCost() != null ? decision.getEstimatedCost().toString() : "No Cost", "Est. Cost", null);
		grid.addDateTimeColumn(CEntityNamed::getCreatedDate, "Created", null);
	}

	/** Gets the current decision service.
	 * @return the decision service instance */
	protected CDecisionService getDecisionService() { return (CDecisionService) entityService; }

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	/** Override the refreshProjectAwareGrid method to use the eager-loading method for decisions to prevent LazyInitializationException. */
	@Override
	protected void refreshProjectAwareGrid() {
		final Optional<CProject> activeProject = sessionService.getActiveProject();
		Assert.isTrue(activeProject.isPresent(), "Active project must be present to load decisions");
		List<CDecision> items;
		items = getDecisionService().findEntriesByProject(activeProject.get());
		// Update the grid with the loaded items
		grid.setItems(items);
		LOGGER.debug("Grid updated with {} items with eager-loaded relationships", items.size());
	}

	@Override
	protected void setupToolbar() {
		// Setup toolbar if needed - following the pattern from CActivitiesView
	}
}
