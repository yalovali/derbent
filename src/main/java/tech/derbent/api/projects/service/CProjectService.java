package tech.derbent.api.projects.service;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.ui.component.enhanced.CComponentProjectUserSettings;
import tech.derbent.api.utils.CPageableUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.domain.CProjectType;
import tech.derbent.api.projects.events.ProjectListChangeEvent;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CProjectService extends CEntityOfCompanyService<CProject> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectService.class);
	private final ApplicationEventPublisher eventPublisher;
	private final CProjectTypeService projectTypeService;
	private final CProjectItemStatusService projectItemStatusService;

	public CProjectService(final IProjectRepository repository, final Clock clock, final ISessionService sessionService,
			final ApplicationEventPublisher eventPublisher,
			final CProjectTypeService projectTypeService,
			final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService);
		this.eventPublisher = eventPublisher;
		this.projectTypeService = projectTypeService;
		this.projectItemStatusService = projectItemStatusService;
	}

	@Override
	public String checkDeleteAllowed(final CProject project) {
		return super.checkDeleteAllowed(project);
	}

	public Component createProjectUserSettingsComponent() {
		try {
			final CComponentProjectUserSettings component = new CComponentProjectUserSettings(this, sessionService);
			return component;
		} catch (final Exception e) {
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
		final CCompany company = getCurrentCompany();
		return ((IProjectRepository) repository).findByCompanyId(company.getId(), safePage);
	}

	/** Override to generate unique name based on company-specific project count. Pattern: "Project##" where ## is zero-padded number within company
	 * (e.g., "Project01", "Project02").
	 * @return unique project name for the current company */
	@Override
	protected String generateUniqueName() {
		try {
			final CCompany currentCompany = getCurrentCompany();
			final long existingCount = ((IProjectRepository) repository).countByCompany_Id(currentCompany.getId());
			return String.format("Project%02d", existingCount + 1);
		} catch (final Exception e) {
			LOGGER.warn("Error generating unique project name, falling back to base class: {}", e.getMessage());
			return super.generateUniqueName();
		}
	}

	@Transactional (readOnly = true)
	@PreAuthorize ("permitAll()")
	public List<CProject> getAvailableProjectsForUser(final Long userId) {
		Check.notNull(userId, "User ID must not be null");
		return ((IProjectRepository) repository).findNotAssignedToUser(userId);
	}

	CCompany getCurrentCompany() {
		Check.notNull(sessionService, "Session service must not be null");
		final CCompany currentCompany = sessionService.getCurrentCompany();
		Check.notNull(currentCompany, "Current company must not be null");
		return currentCompany;
	}

	@Override
	public Class<CProject> getEntityClass() { return CProject.class; }
	
	@Override
	@Transactional(readOnly = true)
	public Optional<CProject> getById(final Long id) {
		Check.notNull(id, "ID cannot be null");
		// Use findByIdForPageView to fetch with kanbanLine (avoids lazy loading issues)
		return ((IProjectRepository) repository).findByIdForPageView(id);
	}

	@Override
	public Class<?> getInitializerServiceClass() { return CProjectInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProject.class; }

        @Override
        public Class<?> getServiceClass() { return this.getClass(); }

        @PreAuthorize ("permitAll()")
        public long getTotalProjectCount() { return countByCompany(getCurrentCompany()); }

	@Override
        public void initializeNewEntity(final CProject entity) {
                super.initializeNewEntity(entity);
                LOGGER.debug("Initializing new project entity");
                // Get current company from session
                final CCompany currentCompany = getCurrentCompany();
                // Initialize company with current company
                entity.setCompany(currentCompany);
                // Name is set by base class generateUniqueName() which is overridden below
                
                // Initialize entity type
                final List<?> availableTypes = projectTypeService.listByCompany(currentCompany);
                Check.notEmpty(availableTypes, 
                        "No project types available in company " + currentCompany.getName() + " - cannot initialize project");
                // Cast safely - CProjectTypeService only returns CProjectType instances
                final Object firstType = availableTypes.get(0);
                Check.instanceOf(firstType, CProjectType.class, 
                        "Expected CProjectType but got " + firstType.getClass().getSimpleName());
                final CProjectType selectedType = (CProjectType) firstType;
                entity.setEntityType(selectedType);
                
                // Initialize workflow-based status
                Check.notNull(entity.getWorkflow(), 
                        "Workflow cannot be null for project type " + selectedType.getName());
                final CProjectItemStatus initialStatus = 
                        IHasStatusAndWorkflowService.getInitialStatus(entity, projectItemStatusService);
                Check.notNull(initialStatus, 
                        "Initial status cannot be null for project");
                entity.setStatus(initialStatus);
                
                LOGGER.debug("Project initialization complete with workflow and status");
        }

	@Override
	@Transactional (readOnly = true)
	public Page<CProject> list(final Pageable pageable) {
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		final CCompany company = getCurrentCompany();
		final Page<CProject> entities = ((IProjectRepository) repository).findByCompanyId(company.getId(), safePage);
		return entities;
	}

	@Override
	@Transactional (readOnly = true)
	public Page<CProject> list(final Pageable pageable, final Specification<CProject> filter) {
		LOGGER.debug("Listing entities with filter specification");
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		// Apply company filter to the specification
		final CCompany company = getCurrentCompany();
		final Specification<CProject> companySpec =
				(root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("company").get("id"), company.getId());
		final Specification<CProject> combinedSpec = filter != null ? companySpec.and(filter) : companySpec;
		final Page<CProject> page = repository.findAll(combinedSpec, safePage);
		return page;
	}

	@Override
	@Transactional (readOnly = true)
	public Page<CProject> listForPageView(final Pageable pageable, final String searchText) {
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		final String term = searchText == null ? "" : searchText.trim();
		final CCompany company = getCurrentCompany();
		final List<CProject> all = ((IProjectRepository) repository).listByCompanyForPageView(company.getId());
		final boolean searchable = ISearchable.class.isAssignableFrom(getEntityClass());
		final List<CProject> filtered = term.isEmpty() || !searchable ? all : all.stream().filter(e -> e.matches(term)).toList();
		final int start = (int) Math.min(safePage.getOffset(), filtered.size());
		final int end = Math.min(start + safePage.getPageSize(), filtered.size());
		final List<CProject> content = filtered.subList(start, end);
		return new PageImpl<>(content, safePage, filtered.size());
	}

        @Override
        @Transactional
        public CProject save(final CProject entity) {
                Check.notNull(entity.getCompany(), "Company must be set before saving a project");
                if (entity.getKanbanLine() != null) { Check.isSameCompany(entity, entity.getKanbanLine()); }
                final boolean isNew = entity.getId() == null;
                final CProject savedEntity = super.save(entity);
                final ProjectListChangeEvent.ChangeType changeType =
                                isNew ? ProjectListChangeEvent.ChangeType.CREATED : ProjectListChangeEvent.ChangeType.UPDATED;
		eventPublisher.publishEvent(new ProjectListChangeEvent(this, savedEntity, changeType));
		return ((IProjectRepository) repository).findByIdForPageView(savedEntity.getId()).orElse(savedEntity);
	}
}
