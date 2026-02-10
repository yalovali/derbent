package tech.derbent.plm.milestones.milestone.service;

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
import tech.derbent.plm.milestones.milestone.domain.CMilestone;
import tech.derbent.plm.milestones.milestonetype.service.CMilestoneTypeService;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CMilestoneService extends CProjectItemService<CMilestone> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CMilestoneService.class);
	private final CMilestoneTypeService typeService;

	CMilestoneService(final IMilestoneRepository repository, final Clock clock, final ISessionService sessionService,
			final CMilestoneTypeService milestoneTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = milestoneTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CMilestone entity) {
		return super.checkDeleteAllowed(entity);
	}

	/**
	 * Copy CMilestone-specific fields from source to target entity.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CMilestone source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);

		if (!(target instanceof CMilestone)) {
			return;
		}

		// CMilestone has no additional fields beyond parent classes
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	@Override
	public Class<CMilestone> getEntityClass() { return CMilestone.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CMilestoneInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceMilestone.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	@Override
	protected void validateEntity(final CMilestone entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Milestone type is required");
		validateUniqueNameInProject((IMilestoneRepository) repository, entity, entity.getName(), entity.getProject());
	}
}
