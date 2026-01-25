package tech.derbent.plm.components.componentversion.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
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
import tech.derbent.plm.components.componentversion.domain.CProjectComponentVersion;
import tech.derbent.plm.components.componentversiontype.service.CProjectComponentVersionTypeService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.ComponentVersions")
@PermitAll
public class CProjectComponentVersionService extends CProjectItemService<CProjectComponentVersion> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectComponentVersionService.class);
	private final CProjectComponentVersionTypeService typeService;

	CProjectComponentVersionService(final IProjectComponentVersionRepository repository, final Clock clock, final ISessionService sessionService,
			final CProjectComponentVersionTypeService componentversionTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = componentversionTypeService;
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

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	@Override
	protected void validateEntity(final CProjectComponentVersion entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Version type is required");
		Check.notNull(entity.getProjectComponent(), "Component is required");
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		if (entity.getVersionNumber() != null && entity.getVersionNumber().length() > 50) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Version Number cannot exceed %d characters", 50));
		}
		// 3. Unique Checks
		final Optional<CProjectComponentVersion> existingName =
				((IProjectComponentVersionRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
	}
}
