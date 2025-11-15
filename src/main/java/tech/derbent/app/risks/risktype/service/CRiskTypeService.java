package tech.derbent.app.risks.risktype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.risks.risk.service.IRiskRepository;
import tech.derbent.app.risks.risktype.domain.CRiskType;
import tech.derbent.base.session.service.ISessionService;

/** CActivityTypeService - Service layer for CActivityType entity. Layer: Service (MVC) Handles business logic for project-aware activity type
 * operations. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CRiskTypeService extends CTypeEntityService<CRiskType> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CRiskTypeService.class);
	@Autowired
	private IRiskRepository riskRepository;

	public CRiskTypeService(final IRiskTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IRiskRepository riskRepository) {
		super(repository, clock, sessionService);
		this.riskRepository = riskRepository;
	}

	/** Checks dependencies before allowing activity type deletion. Prevents deletion if the type is being used by any activities. Always calls
	 * super.checkDeleteAllowed() first to ensure all parent-level checks (null validation, non-deletable flag) are performed.
	 * @param entity the activity type entity to check
	 * @return null if type can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CRiskType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			// Check if any activities are using this type
			final long usageCount = riskRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d activit%s.", usageCount, usageCount == 1 ? "y" : "ies");
			}
			return null; // Type can be deleted
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for activity type: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CRiskType> getEntityClass() { return CRiskType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CRiskTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceRiskType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CRiskType entity) {
		super.initializeNewEntity(entity);
		CProject activeProject = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project in session"));
		long typeCount = ((IRiskTypeRepository) repository).countByProject(activeProject);
		String autoName = String.format("RiskType %02d", typeCount + 1);
		entity.setName(autoName);
	}
}
