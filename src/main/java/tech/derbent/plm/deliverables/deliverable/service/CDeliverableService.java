package tech.derbent.plm.deliverables.deliverable.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.deliverables.deliverable.domain.CDeliverable;
import tech.derbent.plm.deliverables.deliverabletype.service.CDeliverableTypeService;

@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CDeliverableService extends CProjectItemService<CDeliverable> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CDeliverableService.class);
	private final CDeliverableTypeService typeService;

	CDeliverableService(final IDeliverableRepository repository, final Clock clock, final ISessionService sessionService,
			final CDeliverableTypeService deliverableTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = deliverableTypeService;
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
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	@Override
	protected void validateEntity(final CDeliverable entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Deliverable type is required");
		final Optional<CDeliverable> existingName = ((IDeliverableRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
	}
}
