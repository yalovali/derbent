package tech.derbent.plm.agile.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.agile.domain.CUserStoryType;

/**
 * Type service for agile user stories.
 *
 * <p>User-story types default to leaf semantics, so validation explicitly guards against turning a
 * leaf type into a parent-capable type without also changing its hierarchy level.</p>
 */
@Profile({"derbent", "bab", "default"})
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CUserStoryTypeService extends CTypeEntityService<CUserStoryType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserStoryTypeService.class);
	private final IUserStoryRepository userStoryRepository;

	public CUserStoryTypeService(final IUserStoryTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IUserStoryRepository userStoryRepository) {
		super(repository, clock, sessionService);
		this.userStoryRepository = userStoryRepository;
	}

	@Override
	public String checkDeleteAllowed(final CUserStoryType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = userStoryRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d user stor%s.", usageCount, usageCount == 1 ? "y" : "ies");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for user story type: {} reason={}", entity.getName(), e.getMessage());
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CUserStoryType> getEntityClass() { return CUserStoryType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CUserStoryTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceUserStoryType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected void validateEntity(final CUserStoryType entity) {
		super.validateEntity(entity);
		Check.notNull(entity.getLevel(), "Hierarchy level is required");
		if (entity.getLevel() < -1) {
			throw new CValidationException("Hierarchy level cannot be less than -1");
		}
		if (entity.getLevel() == -1 && entity.getCanHaveChildren()) {
			throw new CValidationException("Leaf user story types cannot allow children");
		}
	}
}
