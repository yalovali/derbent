package tech.derbent.app.decisions.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.domains.CProjectItemStatus;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.decisions.domain.CDecision;
import tech.derbent.app.decisions.domain.CDecisionType;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/** CDecisionService - Service class for CDecision entities. Layer: Service (MVC) Provides business logic operations for decision management including
 * validation, creation, approval workflow management, and project-based queries. */
@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionService extends CEntityOfProjectService<CDecision> {

	private final CProjectItemStatusService statusService;
	private final CDecisionTypeService decisionTypeService;

	public CDecisionService(final IDecisionRepository repository, final Clock clock, final ISessionService sessionService,
			final CDecisionTypeService decisionTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService);
		this.decisionTypeService = decisionTypeService;
		this.statusService = statusService;
	}

	@Override
	public String checkDeleteAllowed(final CDecision decision) {
		return super.checkDeleteAllowed(decision);
	}

	@Override
	protected Class<CDecision> getEntityClass() { return CDecision.class; }

	@Override
	public void initializeNewEntity(final CDecision entity) {
		super.initializeNewEntity(entity);
		// Get current user and project from session
		final CUser currentUser = sessionService.getActiveUser()
				.orElseThrow(() -> new CInitializationException("No active user in session - cannot initialize decision"));
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize decision"));
		// Initialize numeric fields
		entity.setEstimatedCost(BigDecimal.ZERO);
		// Initialize accountable user with current user
		entity.setAccountableUser(currentUser);
		// Initialize decision type - get first available decision type for the project (optional field, don't throw if missing)
		final List<CDecisionType> availableTypes = decisionTypeService.listByProject(currentProject);
		if (!availableTypes.isEmpty()) {
			entity.setDecisionType(availableTypes.get(0));
		}
		// Note: If no decision type exists, the field will remain null (it's nullable)
		// Initialize status - get first available status for the project (optional field, don't throw if missing)
		final List<CProjectItemStatus> availableStatuses = statusService.listByProject(currentProject);
		if (!availableStatuses.isEmpty()) {
			entity.setDecisionStatus(availableStatuses.get(0));
		}
		// Note: If no status exists, the field will remain null (it's nullable)
		// Note: dates (implementationDate, reviewDate) are optional and can remain null initially
	}
}
