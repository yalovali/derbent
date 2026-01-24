package tech.derbent.plm.validation.validationsuite.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.validation.validationsuite.domain.CValidationSuite;

@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CValidationSuiteService extends CEntityOfProjectService<CValidationSuite> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CValidationSuiteService.class);

	CValidationSuiteService(final IValidationSuiteRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CValidationSuite validationSuite) {
		return super.checkDeleteAllowed(validationSuite);
	}

	@Override
	public Class<CValidationSuite> getEntityClass() { return CValidationSuite.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CValidationSuiteInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceValidationSuite.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final CValidationSuite entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		if (entity.getDescription() != null && entity.getDescription().length() > 5000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Description cannot exceed %d characters", 5000));
		}
		if (entity.getObjective() != null && entity.getObjective().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Objective cannot exceed %d characters", 2000));
		}
		if (entity.getPrerequisites() != null && entity.getPrerequisites().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Prerequisites cannot exceed %d characters", 2000));
		}
		// 3. Unique Checks
		final Optional<CValidationSuite> existingName =
				((IValidationSuiteRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
	}
}
