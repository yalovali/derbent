package tech.derbent.plm.validation.validationsuite.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.plm.validation.validationsuite.domain.CValidationSuite;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize("isAuthenticated()")
@PermitAll
public class CValidationSuiteService extends CEntityOfProjectService<CValidationSuite> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationSuiteService.class);

	CValidationSuiteService(final IValidationSuiteRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CValidationSuite validationSuite) {
		return super.checkDeleteAllowed(validationSuite);
	}

	@Override
	public Class<CValidationSuite> getEntityClass() {
		return CValidationSuite.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CValidationSuiteInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceValidationSuite.class;
	}

	@Override
	public Class<?> getServiceClass() {
		return this.getClass();
	}

	@Override
	public void initializeNewEntity(final CValidationSuite entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new validation suite entity");
	}
}
