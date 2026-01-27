package tech.derbent.plm.products.productversiontype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.products.productversion.service.IProductVersionRepository;
import tech.derbent.plm.products.productversiontype.domain.CProductVersionType;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CProductVersionTypeService extends CTypeEntityService<CProductVersionType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProductVersionTypeService.class);
	@Autowired
	private final IProductVersionRepository productversionRepository;

	public CProductVersionTypeService(final IProductVersionTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IProductVersionRepository productversionRepository) {
		super(repository, clock, sessionService);
		this.productversionRepository = productversionRepository;
	}

	@Override
	public String checkDeleteAllowed(final CProductVersionType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = productversionRepository.countByType(entity);
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
	public Class<CProductVersionType> getEntityClass() { return CProductVersionType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProductVersionTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProductVersionType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		if (entity instanceof final CEntityNamed entityCasted && entityCasted.getName() == null) {
			final CCompany activeCompany =
					sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
			final long typeCount = ((IProductVersionTypeRepository) repository).countByCompany(activeCompany);
			final String autoName = String.format("ProductVersionType %02d", typeCount + 1);
			((CEntityNamed<?>) entity).setName(autoName);
		}
	}

	@Override
	protected void validateEntity(final CProductVersionType entity) {
		super.validateEntity(entity);
		// Unique Name Check - use base class helper
		validateUniqueNameInCompany((IProductVersionTypeRepository) repository, entity, entity.getName(), entity.getCompany());
	}
}
