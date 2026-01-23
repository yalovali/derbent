package tech.derbent.plm.deliverables.deliverable.service;

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
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.deliverables.deliverable.domain.CDeliverable;
import tech.derbent.plm.deliverables.deliverabletype.service.CDeliverableTypeService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.Deliverables")
@PermitAll
public class CDeliverableService extends CProjectItemService<CDeliverable> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDeliverableService.class);
	private final CDeliverableTypeService deliverableTypeService;

	CDeliverableService(final IDeliverableRepository repository, final Clock clock, final ISessionService sessionService,
			final CDeliverableTypeService deliverableTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.deliverableTypeService = deliverableTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CDeliverable entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CDeliverable> getEntityClass() { return CDeliverable.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CDeliverableInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceDeliverable.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CDeliverable entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new deliverable entity");
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize deliverable"));
		entity.initializeDefaults_IHasStatusAndWorkflow(currentProject, deliverableTypeService, projectItemStatusService);
		LOGGER.debug("Deliverable initialization complete");
	}

	@Override
	protected void validateEntity(final CDeliverable entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Deliverable type is required");
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		// 3. Unique Checks
		final Optional<CDeliverable> existingName = ((IDeliverableRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
	}
}
