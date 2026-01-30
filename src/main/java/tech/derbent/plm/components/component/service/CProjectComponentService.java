package tech.derbent.plm.components.component.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.components.component.domain.CProjectComponent;
import tech.derbent.plm.components.componenttype.service.CProjectComponentTypeService;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CProjectComponentService extends CProjectItemService<CProjectComponent> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectComponentService.class);
	private final CProjectComponentTypeService typeService;

	CProjectComponentService(final IProjectComponentRepository repository, final Clock clock, final ISessionService sessionService,
			final CProjectComponentTypeService projectComponentTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = projectComponentTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CProjectComponent entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CProjectComponent> getEntityClass() { return CProjectComponent.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProjectComponentInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProjectComponent.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/**
	 * Copy CProjectComponent-specific fields from source to target entity.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CProjectComponent source, final CEntityDB<?> target,
			final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		
		if (!(target instanceof CProjectComponent)) {
			return;
		}
		final CProjectComponent targetComponent = (CProjectComponent) target;
		
		// Copy unique fields - make unique by appending suffix
		if (source.getComponentCode() != null) {
			targetComponent.setComponentCode(source.getComponentCode() + "-COPY");
		}
		
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	@Override
	protected void validateEntity(final CProjectComponent entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Component type is required");
		
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getComponentCode(), "Component Code", 100);
		
		// 3. Unique Checks
		validateUniqueNameInProject((IProjectComponentRepository) repository, entity, entity.getName(), entity.getProject());
	}
}
