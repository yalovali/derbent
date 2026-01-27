package tech.derbent.plm.components.component.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.components.component.domain.CProjectComponent;
import tech.derbent.plm.components.componenttype.service.CProjectComponentTypeService;

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
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		if (entity.getComponentCode() != null && entity.getComponentCode().length() > 100) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Component Code cannot exceed %d characters", 100));
		}
		// 3. Unique Checks
		final Optional<CProjectComponent> existingName =
				((IProjectComponentRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
	}
}
