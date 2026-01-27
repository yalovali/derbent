package tech.derbent.plm.validation.validationcasetype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.validation.validationcasetype.domain.CValidationCaseType;

@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CValidationCaseTypeService extends CTypeEntityService<CValidationCaseType> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationCaseTypeService.class);

	CValidationCaseTypeService(final IValidationCaseTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CValidationCaseType testCaseType) {
		return super.checkDeleteAllowed(testCaseType);
	}

	@Override
	public Class<CValidationCaseType> getEntityClass() { return CValidationCaseType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CValidationCaseTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceValidationCaseType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final CValidationCaseType entity) {
		super.validateEntity(entity);
		
		// Unique Name Check - USE STATIC HELPER
		validateUniqueNameInCompany((IValidationCaseTypeRepository) repository, entity, entity.getName(), entity.getCompany());
	}
}
