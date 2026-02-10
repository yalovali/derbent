package tech.derbent.plm.components.componenttype.service;

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
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.components.component.service.IProjectComponentRepository;
import tech.derbent.plm.components.componenttype.domain.CProjectComponentType;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CProjectComponentTypeService extends CTypeEntityService<CProjectComponentType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectComponentTypeService.class);
	@Autowired
	private final IProjectComponentRepository componentRepository;

	public CProjectComponentTypeService(final IProjectComponentTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IProjectComponentRepository componentRepository) {
		super(repository, clock, sessionService);
		this.componentRepository = componentRepository;
	}

	@Override
	public String checkDeleteAllowed(final CProjectComponentType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = componentRepository.countByType(entity);
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
	public Class<CProjectComponentType> getEntityClass() { return CProjectComponentType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProjectComponentTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProjectComponentType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		if (entity instanceof final CEntityNamed entityCasted && entityCasted.getName() == null) {
			final CCompany activeCompany =
					sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
			final long typeCount = ((IProjectComponentTypeRepository) repository).countByCompany(activeCompany);
			final String autoName = String.format("ComponentType %02d", typeCount + 1);
			((CEntityNamed<?>) entity).setName(autoName);
		}
	}

	@Override
	protected void validateEntity(final CProjectComponentType entity) {
		super.validateEntity(entity);
		
		// Unique Name Check - USE STATIC HELPER
		validateUniqueNameInCompany((IProjectComponentTypeRepository) repository, entity, entity.getName(), entity.getCompany());
	}
}
