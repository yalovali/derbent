package tech.derbent.plm.milestones.milestone.service;

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
import tech.derbent.plm.milestones.milestone.domain.CMilestone;
import tech.derbent.plm.milestones.milestonetype.service.CMilestoneTypeService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.Milestones")
@PermitAll
public class CMilestoneService extends CProjectItemService<CMilestone> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CMilestoneService.class);
	private final CMilestoneTypeService milestoneTypeService;

	CMilestoneService(final IMilestoneRepository repository, final Clock clock, final ISessionService sessionService,
			final CMilestoneTypeService milestoneTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.milestoneTypeService = milestoneTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CMilestone entity) {
		return super.checkDeleteAllowed(entity);
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
	public void initializeNewEntity(final CMilestone entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new milestone entity");
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize milestone"));
		entity.initializeDefaults_IHasStatusAndWorkflow(currentProject, milestoneTypeService, projectItemStatusService);
		LOGGER.debug("Milestone initialization complete");
	}

	@Override
	protected void validateEntity(final CMilestone entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Milestone type is required");
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		// 3. Unique Checks
		final Optional<CMilestone> existingName = ((IMilestoneRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
	}
}
