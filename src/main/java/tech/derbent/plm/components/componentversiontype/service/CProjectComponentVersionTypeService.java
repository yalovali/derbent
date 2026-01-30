package tech.derbent.plm.components.componentversiontype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.components.componentversion.service.IProjectComponentVersionRepository;
import tech.derbent.plm.components.componentversiontype.domain.CProjectComponentVersionType;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CProjectComponentVersionTypeService extends CTypeEntityService<CProjectComponentVersionType>
		implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectComponentVersionTypeService.class);
	@Autowired
	private final IProjectComponentVersionRepository componentversionRepository;

	public CProjectComponentVersionTypeService(final IProjectComponentVersionTypeRepository repository, final Clock clock,
			final ISessionService sessionService, final IProjectComponentVersionRepository componentversionRepository) {
		super(repository, clock, sessionService);
		this.componentversionRepository = componentversionRepository;
	}

	@Override
	public String checkDeleteAllowed(final CProjectComponentVersionType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = componentversionRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d item%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CProjectComponentVersionType> getEntityClass() { return CProjectComponentVersionType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProjectComponentVersionTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceComponentVersionType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		if (entity instanceof final CEntityNamed entityCasted && entityCasted.getName() == null) {
			final CCompany activeCompany =
					sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
			final long typeCount = ((IProjectComponentVersionTypeRepository) repository).countByCompany(activeCompany);
			final String autoName = String.format("ComponentVersionType %02d", typeCount + 1);
			((CEntityNamed<?>) entity).setName(autoName);
		}
	}

	@Override
	protected void validateEntity(final CProjectComponentVersionType entity) {
		super.validateEntity(entity);
		
		// Unique Name Check - USE STATIC HELPER
		validateUniqueNameInCompany((IProjectComponentVersionTypeRepository) repository, entity, entity.getName(), entity.getCompany());
	}
}
