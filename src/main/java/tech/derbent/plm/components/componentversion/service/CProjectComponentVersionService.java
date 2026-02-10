package tech.derbent.plm.components.componentversion.service;

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
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.components.component.domain.CProjectComponent;
import tech.derbent.plm.components.component.service.CProjectComponentService;
import tech.derbent.plm.components.componentversion.domain.CProjectComponentVersion;
import tech.derbent.plm.components.componentversiontype.service.CProjectComponentVersionTypeService;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CProjectComponentVersionService extends CProjectItemService<CProjectComponentVersion> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectComponentVersionService.class);
	private final CProjectComponentService componentService;
	private final CProjectComponentVersionTypeService typeService;

	CProjectComponentVersionService(final IProjectComponentVersionRepository repository, final Clock clock, final ISessionService sessionService,
			final CProjectComponentVersionTypeService componentversionTypeService, final CProjectItemStatusService statusService,
			final CProjectComponentService componentService) {
		super(repository, clock, sessionService, statusService);
		typeService = componentversionTypeService;
		this.componentService = componentService;
	}

	@Override
	public String checkDeleteAllowed(final CProjectComponentVersion entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CProjectComponentVersion> getEntityClass() { return CProjectComponentVersion.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProjectComponentVersionInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceComponentVersion.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/**
	 * Copy CProjectComponentVersion-specific fields from source to target entity.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CProjectComponentVersion source, final CEntityDB<?> target,
			final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		
		if (!(target instanceof CProjectComponentVersion)) {
			return;
		}
		final CProjectComponentVersion targetVersion = (CProjectComponentVersion) target;
		
		// Copy basic fields
		targetVersion.setVersionNumber(source.getVersionNumber());
		
		// Copy relations conditionally
		if (options.includesRelations()) {
			targetVersion.setProjectComponent(source.getProjectComponent());
		}
		
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		final CProjectComponentVersion componentVersion = (CProjectComponentVersion) entity;
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
		if (componentVersion.getProjectComponent() == null) {
			final var project = componentVersion.getProject();
			final java.util.List<CProjectComponent> components = componentService.listByProject(project);
			if (!components.isEmpty()) {
				componentVersion.setProjectComponent(components.get(0));
			}
		}
	}

	@Override
	protected void validateEntity(final CProjectComponentVersion entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Version type is required");
		Check.notNull(entity.getProjectComponent(), "Component is required");
		
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getVersionNumber(), "Version Number", 50);
		
		// 3. Unique Checks
		validateUniqueNameInProject((IProjectComponentVersionRepository) repository, entity, entity.getName(), entity.getProject());
	}
}
