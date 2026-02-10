package tech.derbent.plm.agile.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.agile.domain.CUserStoryType;

@Profile("derbent")
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
			LOGGER.error("Error checking dependencies for user story type: {}", entity.getName(), e);
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
}
