package tech.derbent.plm.providers.provider.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.providers.provider.domain.CProvider;
import tech.derbent.plm.providers.providertype.service.CProviderTypeService;

import java.util.Optional;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.validation.ValidationMessages;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.Providers")
@PermitAll
public class CProviderService extends CProjectItemService<CProvider> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProviderService.class);
	private final CProviderTypeService providerTypeService;

	CProviderService(final IProviderRepository repository, final Clock clock, final ISessionService sessionService,
			final CProviderTypeService providerTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.providerTypeService = providerTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CProvider entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	protected void validateEntity(final CProvider entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Provider type is required");
		
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		
		// 3. Unique Checks
		final Optional<CProvider> existingName = ((IProviderRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
	}

	@Override
	public Class<CProvider> getEntityClass() { return CProvider.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProviderInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProvider.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@SuppressWarnings ("null")
	@Override
	public void initializeNewEntity(final CProvider entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new provider entity");
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize provider"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, providerTypeService, projectItemStatusService);
		LOGGER.debug("Provider initialization complete");
	}
}
