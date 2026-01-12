package tech.derbent.api.entityOfProject.service;

import java.time.Clock;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

public abstract class CProjectItemService<EntityClass extends CProjectItem<EntityClass>> extends CEntityOfProjectService<EntityClass>
		implements IHasStatusAndWorkflowService<EntityClass> {

	protected CProjectItemStatusService projectItemStatusService;

	public CProjectItemService(final IEntityOfProjectRepository<EntityClass> repository, final Clock clock, final ISessionService sessionService,
			final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService);
		this.projectItemStatusService = projectItemStatusService;
	}

	/** Validates that project item can be saved. Checks that status is set and valid for the entity's workflow.
	 * @param entity the entity to validate
	 * @return null if entity can be saved, or error message describing validation failure */
	@Override
	public String checkSaveAllowed(final EntityClass entity) {
		// Call parent validation first
		final String superCheck = super.checkSaveAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		// Validate status is set
		if (entity.getStatus() == null) {
			return "Status must be set before saving. Please select a status from the available options.";
		}
		// Validate status belongs to same company as entity
		final var project = entity.getProject();
		if (project != null && project.getCompany() != null) {
			final var entityCompany = project.getCompany();
			final var statusCompany = entity.getStatus().getCompany();
			if (statusCompany == null) {
				return "Status company cannot be null.";
			}
			if (!entityCompany.getId().equals(statusCompany.getId())) {
				return "Status must belong to the same company as the entity. Entity company: " + entityCompany.getName()
						+ ", Status company: " + statusCompany.getName();
			}
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final EntityClass entity) {
		super.initializeNewEntity(entity);
		if (entity.getStatus() != null) {
			return;
		}
		final var project = entity.getProject();
		Check.notNull(project, "Project must be set before initializing status");
		final var defaultStatus = projectItemStatusService.findDefaultStatus(project).orElseGet(() -> {
			Check.notNull(project.getCompany(), "Company must be set before initializing status");
			final var available = projectItemStatusService.listByCompany(project.getCompany());
			Check.notEmpty(available, "No project item statuses available for company " + project.getCompany().getName());
			return available.get(0);
		});
		entity.setStatus(defaultStatus);
	}

	@SuppressWarnings ("unchecked")
	public final void revokeSave(final CProjectItem<?> rawEntity) {
		save((EntityClass) rawEntity);
	}
}
