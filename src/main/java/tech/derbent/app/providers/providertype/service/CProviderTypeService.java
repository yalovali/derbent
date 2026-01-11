package tech.derbent.app.providers.providertype.service;

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
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.providers.provider.service.IProviderRepository;
import tech.derbent.app.providers.providertype.domain.CProviderType;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CProviderTypeService extends CTypeEntityService<CProviderType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProviderTypeService.class);
	@Autowired
	private final IProviderRepository providerRepository;

	public CProviderTypeService(final IProviderTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IProviderRepository providerRepository) {
		super(repository, clock, sessionService);
		this.providerRepository = providerRepository;
	}

	@Override
	public String checkDeleteAllowed(final CProviderType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = providerRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d item%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for provider type: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CProviderType> getEntityClass() { return CProviderType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProviderTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProviderType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CProviderType entity) {
		super.initializeNewEntity(entity);
		final CCompany activeCompany = sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
		final long typeCount = ((IProviderTypeRepository) repository).countByCompany(activeCompany);
		final String autoName = String.format("ProviderType %02d", typeCount + 1);
		entity.setName(autoName);
	}
}
