package tech.derbent.app.activities.service;

import java.time.Clock;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CTypeEntityService;
import tech.derbent.app.activities.domain.CActivityPriority;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;

@Service
@Transactional
public class CActivityPriorityService extends CTypeEntityService<CActivityPriority> {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CActivityPriorityService.class);

	public CActivityPriorityService(final IActivityPriorityRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks dependencies before allowing activity priority deletion. Always calls super.checkDeleteAllowed() first to ensure all parent-level
	 * checks (null validation, non-deletable flag) are performed.
	 * @param entity the activity priority entity to check
	 * @return null if priority can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CActivityPriority entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Transactional (readOnly = true)
	public Optional<CActivityPriority> findDefaultPriority(final CProject project) {
		return ((IActivityPriorityRepository) repository).findByIsDefaultTrue(project);
	}

	@Override
	protected Class<CActivityPriority> getEntityClass() { return CActivityPriority.class; }

	@Override
	public void initializeNewEntity(final CActivityPriority entity) {
		super.initializeNewEntity(entity);
		try {
			entity.setIsDefault(false);
			entity.setPriorityLevel(3);
			setNameOfEntity(entity, "Activity Priority");
		} catch (final Exception e) {
			LOGGER.error(e.getMessage());
			throw e;
		}
	}
}
