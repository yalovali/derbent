package tech.derbent.risks.service;

import java.util.Optional;
import tech.derbent.projects.domain.CProject;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CStatusService;
import tech.derbent.risks.domain.CRiskStatus;
import tech.derbent.session.service.ISessionService;

/** CRiskStatusService - Service class for managing CRiskStatus entities. Layer: Service (MVC) Provides business logic for risk status management
 * including CRUD operations, validation, and workflow management. */
@Service
@Transactional
public class CRiskStatusService extends CStatusService<CRiskStatus> {

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

	/** Checks dependencies before allowing risk status deletion. Always calls super.checkDeleteAllowed() first to ensure all parent-level checks
	 * (null validation, non-deletable flag) are performed.
	 * @param entity the risk status entity to check
	 * @return null if status can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CRiskStatus entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final CRiskStatus entity) {
		super.initializeNewEntity(entity);
		try {
			Optional<CProject> activeProject = sessionService.getActiveProject();
			if (activeProject.isPresent()) {
				long statusCount = ((IRiskStatusRepository) repository).countByProject(activeProject.get());
				String autoName = String.format("RiskStatus%02d", statusCount + 1);
				entity.setName(autoName);
			}
			entity.setIsFinal(false);
			LOGGER.debug("Initialized new risk status");
		} catch (final Exception e) {
			LOGGER.error("Error initializing new risk status", e);
			throw new IllegalStateException("Failed to initialize risk status: " + e.getMessage(), e);
		}
	}
}
