package tech.derbent.projects.service;

import java.time.Clock;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.services.CAbstractNamedEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.events.ProjectListChangeEvent;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserTypeService;
import tech.derbent.users.view.CComponentUserProjectSettings;

@Service
@PreAuthorize ("isAuthenticated()")
public class CProjectService extends CAbstractNamedEntityService<CProject> {

	@Autowired
	private ApplicationContext applicationContext;
	private final ApplicationEventPublisher eventPublisher;

	public CProjectService(final IProjectRepository repository, final Clock clock, final CSessionService sessionService,
			final ApplicationEventPublisher eventPublisher) {
		super(repository, clock, sessionService);
		this.eventPublisher = eventPublisher;
	}

	@Override
	@Transactional
	public void delete(final CProject entity) {
		super.delete(entity);
		// Publish project list change event after deletion
		eventPublisher.publishEvent(new ProjectListChangeEvent(this, entity, ProjectListChangeEvent.ChangeType.DELETED));
	}

	@Override
	@PreAuthorize ("permitAll()")
	public List<CProject> findAll() {
		return repository.findAll();
	}

	/** Gets a project with all its user settings loaded (to avoid lazy loading issues) */
	@Transactional (readOnly = true)
	public CProject findByIdWithUserSettings(final Long projectId) {
		return ((IProjectRepository) repository).findByIdWithUserSettings(projectId).orElse(null);
	}

	/** Gets projects available for assignment to a specific user (excluding projects the user is already assigned to).
	 * @param userId the ID of the user
	 * @return list of available projects for the user */
	@Transactional (readOnly = true)
	@PreAuthorize ("permitAll()")
	public List<CProject> getAvailableProjectsForUser(final Long userId) {
		Check.notNull(userId, "User ID must not be null");
		return ((IProjectRepository) repository).findProjectsNotAssignedToUser(userId);
	}

	@Override
	protected Class<CProject> getEntityClass() { return CProject.class; }

	@PreAuthorize ("permitAll()")
	public long getTotalProjectCount() { return repository.count(); }

	@Override
	@Transactional
	public CProject save(final CProject entity) {
		LOGGER.debug("save called with entity: {}", entity);
		final boolean isNew = entity.getId() == null;
		final CProject savedEntity = super.save(entity);
		// Publish project list change event after saving
		final ProjectListChangeEvent.ChangeType changeType =
				isNew ? ProjectListChangeEvent.ChangeType.CREATED : ProjectListChangeEvent.ChangeType.UPDATED;
		eventPublisher.publishEvent(new ProjectListChangeEvent(this, savedEntity, changeType));
		return savedEntity;
	}

	public Component createProjectUserSettingsComponent() {
		try {
			// Get services from ApplicationContext to avoid circular dependency
			CProjectService projectService = applicationContext.getBean(CProjectService.class);
			CUserProjectSettingsService userProjectSettingsService = applicationContext.getBean(CUserProjectSettingsService.class);
			CUserTypeService userTypeService = applicationContext.getBean(CUserTypeService.class);
			CCompanyService companyService = applicationContext.getBean(CCompanyService.class);
			// Create a minimal binder for the component
			CEnhancedBinder<CUser> binder = new CEnhancedBinder<>(CUser.class);
			// Create the enhanced component with proper service dependencies
			CComponentUserProjectSettings component = new CComponentUserProjectSettings(null, null, binder, this, userTypeService, companyService,
					projectService, userProjectSettingsService);
			LOGGER.debug("Successfully created CComponentUserProjectSettings");
			return component;
		} catch (Exception e) {
			LOGGER.error("Failed to create user project settings component: {}", e.getMessage(), e);
			// Fallback to simple div with error message
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading user project settings component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}
}
