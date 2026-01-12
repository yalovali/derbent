package tech.derbent.app.products.producttype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.app.products.product.service.IProductRepository;
import tech.derbent.app.products.producttype.domain.CProductType;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CProductTypeService extends CTypeEntityService<CProductType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProductTypeService.class);
	@Autowired
	private final IProductRepository productRepository;

	public CProductTypeService(final IProductTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IProductRepository productRepository) {
		super(repository, clock, sessionService);
		this.productRepository = productRepository;
	}

	@Override
	public String checkDeleteAllowed(final CProductType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = productRepository.countByType(entity);
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
	public Class<CProductType> getEntityClass() { return CProductType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProductTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProductType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CProductType entity) {
		super.initializeNewEntity(entity);
		final CCompany activeCompany = sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
		final long typeCount = ((IProductTypeRepository) repository).countByCompany(activeCompany);
		final String autoName = String.format("ProductType %02d", typeCount + 1);
		entity.setName(autoName);
	}
}
