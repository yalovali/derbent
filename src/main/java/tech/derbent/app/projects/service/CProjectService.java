package tech.derbent.app.projects.service;

import java.time.Clock;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.services.CEntityNamedService;
import tech.derbent.api.utils.CPageableUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CComponentProjectUserSettings;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.projects.events.ProjectListChangeEvent;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CProjectService extends CEntityNamedService<CProject> {

	private final ApplicationEventPublisher eventPublisher;

	public CProjectService(final IProjectRepository repository, final Clock clock, final ISessionService sessionService,
			final ApplicationEventPublisher eventPublisher) {
		super(repository, clock, sessionService);
		this.eventPublisher = eventPublisher;
	}

	@Override
	public String checkDeleteAllowed(final CProject project) {
		return super.checkDeleteAllowed(project);
	}

	public Component createProjectUserSettingsComponent() {
		try {
			CComponentProjectUserSettings component = new CComponentProjectUserSettings(this, sessionService);
			return component;
		} catch (Exception e) {
			LOGGER.error("Failed to create project user settings component.");
			// Fallback to simple div with error message
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading project user settings component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
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
		Check.notNull(repository, "Repository must not be null");
		return ((IProjectRepository) repository).findByCompanyId(getCurrentCompany().getId());
	}

	@PreAuthorize ("permitAll()")
	public Page<CProject> findAll(Pageable pageable) {
		Check.notNull(repository, "Repository must not be null");
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		CCompany company = getCurrentCompany();
		return ((IProjectRepository) repository).findByCompanyId(company.getId(), safePage);
	}

	@Transactional (readOnly = true)
	@PreAuthorize ("permitAll()")
	public List<CProject> getAvailableProjectsForUser(final Long userId) {
		Check.notNull(userId, "User ID must not be null");
		return ((IProjectRepository) repository).findNotAssignedToUser(userId);
	}

	CCompany getCurrentCompany() {
		Check.notNull(sessionService, "Session service must not be null");
		CCompany currentCompany = sessionService.getCurrentCompany();
		Check.notNull(currentCompany, "Current company must not be null");
		return currentCompany;
	}

	@Override
	protected Class<CProject> getEntityClass() { return CProject.class; }

	@PreAuthorize ("permitAll()")
	public long getTotalProjectCount() { return repository.count(); }

	@Override
	public void initializeNewEntity(final CProject entity) {
		super.initializeNewEntity(entity);
		// Get current company from session
		final CCompany currentCompany = getCurrentCompany();
		// Initialize company with current company
		entity.setCompany(currentCompany);
		// Name is set by base class generateUniqueName() which is overridden below
		// Note: CProject extends CEntityNamed, not CEntityOfProject, so it doesn't have project field
		// The company field is the primary association for projects
	}

	/** Override to generate unique name based on company-specific project count. Pattern: "Project##" where ## is zero-padded number within company
	 * (e.g., "Project01", "Project02").
	 * @return unique project name for the current company */
	@Override
	protected String generateUniqueName() {
		try {
			final CCompany currentCompany = getCurrentCompany();
			final long existingCount = ((IProjectRepository) repository).countByCompanyId(currentCompany.getId());
			return String.format("Project%02d", existingCount + 1);
		} catch (final Exception e) {
			LOGGER.warn("Error generating unique project name, falling back to base class: {}", e.getMessage());
			return super.generateUniqueName();
		}
	}

	@Override
	@Transactional (readOnly = true)
	public Page<CProject> list(final Pageable pageable) {
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		CCompany company = getCurrentCompany();
		final Page<CProject> entities = ((IProjectRepository) repository).findByCompanyId(company.getId(), safePage);
		return entities;
	}

	@Override
	@Transactional (readOnly = true)
	public Page<CProject> list(final Pageable pageable, final Specification<CProject> filter) {
		LOGGER.debug("Listing entities with filter specification");
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		// Apply company filter to the specification
		CCompany company = getCurrentCompany();
		Specification<CProject> companySpec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("company").get("id"), company.getId());
		Specification<CProject> combinedSpec = filter != null ? companySpec.and(filter) : companySpec;
		final Page<CProject> page = repository.findAll(combinedSpec, safePage);
		return page;
	}

	@Override
	@Transactional
	public CProject save(final CProject entity) {
		// Ensure company is set for new entities
		if (entity.getCompany() == null) {
			CCompany company = getCurrentCompany();
			entity.setCompany(company);
		}
		final boolean isNew = entity.getId() == null;
		final CProject savedEntity = super.save(entity);
		final ProjectListChangeEvent.ChangeType changeType =
				isNew ? ProjectListChangeEvent.ChangeType.CREATED : ProjectListChangeEvent.ChangeType.UPDATED;
		eventPublisher.publishEvent(new ProjectListChangeEvent(this, savedEntity, changeType));
		return savedEntity;
	}
}
