package tech.derbent.plm.validation.validationsuite.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.validation.validationsuite.domain.CValidationSuite;

@Profile("derbent")
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

	/**
	 * Copy CValidationSuite-specific fields from source to target entity.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CValidationSuite source, final CEntityDB<?> target,
			final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		
		if (!(target instanceof CValidationSuite)) {
			return;
		}
		final CValidationSuite targetSuite = (CValidationSuite) target;
		
		// Copy basic fields
		targetSuite.setObjective(source.getObjective());
		targetSuite.setPrerequisites(source.getPrerequisites());
		
		// Copy relations conditionally
		if (options.includesRelations()) {
			// Copy collections
			if (source.getValidationCases() != null) {
				targetSuite.setValidationCases(new java.util.HashSet<>(source.getValidationCases()));
			}
		}
		
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

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
		
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getDescription(), "Description", 5000);
		validateStringLength(entity.getObjective(), "Objective", 2000);
		validateStringLength(entity.getPrerequisites(), "Prerequisites", 2000);
		
		// 3. Unique Name Check - USE STATIC HELPER
		validateUniqueNameInProject((IValidationSuiteRepository) repository, entity, entity.getName(), entity.getProject());
	}
}
