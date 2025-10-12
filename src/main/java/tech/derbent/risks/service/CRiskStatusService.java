package tech.derbent.risks.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.risks.domain.CRiskStatus;
import tech.derbent.session.service.ISessionService;

/** CRiskStatusService - Service class for managing CRiskStatus entities. Layer: Service (MVC) Provides business logic for risk status management
 * including CRUD operations, validation, and workflow management. */
@Service
@Transactional
public class CRiskStatusService extends CEntityOfProjectService<CRiskStatus> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CRiskStatusService.class);

	@Autowired
	public CRiskStatusService(final IRiskStatusRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Create default risk statuses if they don't exist. This method should be called during application startup. */
	public void createDefaultStatusesIfNotExist() {
		LOGGER.debug("createDefaultStatusesIfNotExist() - Creating default risk statuses");
		// TODO implement default statuses creation logic
	}

	@Override
	protected Class<CRiskStatus> getEntityClass() { return CRiskStatus.class; }

	@Override
	public String checkDependencies(final CRiskStatus riskStatus) {
		final String superCheck = super.checkDependencies(riskStatus);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final CRiskStatus entity) {
		super.initializeNewEntity(entity);
		tech.derbent.api.utils.Check.notNull(entity, "Risk status cannot be null");
		tech.derbent.api.utils.Check.notNull(sessionService, "Session service is required for risk status initialization");
		try {
			java.util.Optional<tech.derbent.projects.domain.CProject> activeProject = sessionService.getActiveProject();
			tech.derbent.api.utils.Check.isTrue(activeProject.isPresent(),
					"No active project in session - project context is required to create risk statuses");
			tech.derbent.projects.domain.CProject currentProject = activeProject.get();
			entity.setProject(currentProject);
			java.util.Optional<tech.derbent.users.domain.CUser> currentUser = sessionService.getActiveUser();
			if (currentUser.isPresent()) {
				entity.setCreatedBy(currentUser.get());
			}
			long statusCount = ((IRiskStatusRepository) repository).countByProject(currentProject);
			String autoName = String.format("RiskStatus%02d", statusCount + 1);
			entity.setName(autoName);
			entity.setDescription("");
			entity.setColor(tech.derbent.risks.domain.CRiskStatus.DEFAULT_COLOR);
			entity.setSortOrder(100);
			entity.setAttributeNonDeletable(false);
			entity.setIsFinal(false);
			LOGGER.debug("Initialized new risk status with auto-generated name: {}", autoName);
		} catch (final Exception e) {
			LOGGER.error("Error initializing new risk status", e);
			throw new IllegalStateException("Failed to initialize risk status: " + e.getMessage(), e);
		}
	}
}
