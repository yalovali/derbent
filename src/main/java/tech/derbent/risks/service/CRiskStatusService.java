package tech.derbent.risks.service;

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

	/** Create default risk statuses if they don't exist. This method should be called during application startup. */
	public void createDefaultStatusesIfNotExist() {
		LOGGER.debug("createDefaultStatusesIfNotExist() - Creating default risk statuses");
		// TODO implement default statuses creation logic
	}

	@Override
	protected Class<CRiskStatus> getEntityClass() { return CRiskStatus.class; }

	@Override
	public void initializeNewEntity(final CRiskStatus entity) {
		super.initializeNewEntity(entity);
		setNameOfEntity(entity, "Risk Status");
	}
}
