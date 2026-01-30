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
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.agile.domain.CEpicType;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CEpicTypeService extends CTypeEntityService<CEpicType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEpicTypeService.class);
	private final IEpicRepository epicRepository;

	public CEpicTypeService(final IEpicTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IEpicRepository epicRepository) {
		super(repository, clock, sessionService);
		this.epicRepository = epicRepository;
	}

	@Override
	public String checkDeleteAllowed(final CEpicType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = epicRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d epic%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for epic type: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CEpicType> getEntityClass() { return CEpicType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CEpicTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceEpicType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }
}
