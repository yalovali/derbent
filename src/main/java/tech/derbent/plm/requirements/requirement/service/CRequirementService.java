package tech.derbent.plm.requirements.requirement.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.plm.requirements.requirementtype.domain.CRequirementType;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.parentrelation.domain.CParentRelation;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.links.domain.IHasLinks;
import tech.derbent.plm.requirements.requirement.domain.CRequirement;
import tech.derbent.plm.requirements.requirementtype.service.CRequirementTypeService;

@Service
@Profile ({
		"derbent", "bab", "default"
})
@PreAuthorize ("isAuthenticated()")
public class CRequirementService extends CProjectItemService<CRequirement, CRequirementType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CRequirementService.class);
	private final CRequirementTypeService typeService;

	public CRequirementService(final IRequirementRepository repository, final Clock clock, final ISessionService sessionService,
			final CRequirementTypeService typeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		this.typeService = typeService;
	}

	@Override
	public void copyEntityFieldsTo(final CRequirement source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		if (!(target instanceof final CRequirement targetRequirement)) {
			return;
		}
		targetRequirement.setAcceptanceCriteria(source.getAcceptanceCriteria());
		targetRequirement.setSource(source.getSource());
		targetRequirement.setStartDate(source.getStartDate());
		targetRequirement.setDueDate(source.getDueDate());
		targetRequirement.setEntityType(source.getEntityType());
		IHasLinks.copyLinksTo(source, target, options);
	}

	@Override
	public Class<CRequirement> getEntityClass() { return CRequirement.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CRequirementInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceRequirement.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		final CRequirement requirement = (CRequirement) entity;
		@SuppressWarnings ("unchecked")
		final IHasStatusAndWorkflow<?, ?> typedEntity = (IHasStatusAndWorkflow<?, ?>) entity;
		initializeNewEntity_IHasStatusAndWorkflow(typedEntity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
		if (requirement.getParentRelation() == null) {
			requirement.setParentRelation(new CParentRelation(requirement));
		} else {
			requirement.getParentRelation().setOwnerItem(requirement);
		}
	}

	@Override
	protected void validateEntity(final CRequirement entity) {
		super.validateEntity(entity);
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Requirement type is required");
		validateStringLength(entity.getAcceptanceCriteria(), "Acceptance Criteria", 2000);
		validateStringLength(entity.getSource(), "Source", 500);
		if (entity.getDueDate() != null && entity.getStartDate() != null && entity.getDueDate().isBefore(entity.getStartDate())) {
			throw new CValidationException("Due Date cannot be before Start Date");
		}
		validateUniqueNameInProject((IRequirementRepository) repository, entity, entity.getName(), entity.getProject());
		LOGGER.debug("Validated requirement '{}'", entity.getName());
	}
}
