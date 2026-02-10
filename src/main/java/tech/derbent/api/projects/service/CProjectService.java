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
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.domain.CProjectType;
import tech.derbent.api.projects.events.ProjectListChangeEvent;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.ui.component.enhanced.CComponentProjectUserSettings;
import tech.derbent.api.utils.CPageableUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.api.session.service.ISessionService;

@PreAuthorize ("isAuthenticated()")
public abstract class CProjectService<ProjectClass extends CProject<ProjectClass>> extends CEntityOfCompanyService<ProjectClass>
		implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectService.class);
	private final ApplicationEventPublisher eventPublisher;
	private final IProjectRepository<ProjectClass> projectRepository;
	private final CProjectItemStatusService statusService;
	private final CProjectTypeService typeService;

	public CProjectService(final IProjectRepository<ProjectClass> repository, final Clock clock, final ISessionService sessionService,
			final ApplicationEventPublisher eventPublisher, final CProjectTypeService projectTypeService,
			final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService);
		this.eventPublisher = eventPublisher;
		typeService = projectTypeService;
		this.statusService = statusService;
		projectRepository = repository;
	}

	@Override
	public String checkDeleteAllowed(final ProjectClass project) {
		return super.checkDeleteAllowed(project);
	}

	public Component createProjectUserSettingsComponent() {
		try {
			final CComponentProjectUserSettings<ProjectClass> component = new CComponentProjectUserSettings<>(this, sessionService);
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
	public void delete(final ProjectClass entity) {
		super.delete(entity);
		// Publish project list change event after deletion
		eventPublisher.publishEvent(new ProjectListChangeEvent(this, entity, ProjectListChangeEvent.ChangeType.DELETED));
	}

	@Override
	@PreAuthorize ("permitAll()")
	public List<ProjectClass> findAll() {
		Check.notNull(projectRepository, "Repository must not be null");
		return projectRepository.findByCompanyId(getCurrentCompany().getId());
	}

	@PreAuthorize ("permitAll()")
	public Page<ProjectClass> findAll(Pageable pageable) {
		Check.notNull(projectRepository, "Repository must not be null");
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		final CCompany company = getCurrentCompany();
		return projectRepository.findByCompanyId(company.getId(), safePage);
	}

	/** Override to generate unique name based on company-specific project count. Pattern: "Project##" where ## is zero-padded number within company
	 * (e.g., "Project01", "Project02").
	 * @return unique project name for the current company */
	@Override
	protected String generateUniqueName(String clazzName) {
		try {
			final CCompany currentCompany = getCurrentCompany();
			final long existingCount = projectRepository.countByCompany_Id(currentCompany.getId());
			return "Project%02d".formatted(existingCount + 1);
		} catch (final Exception e) {
			LOGGER.warn("Error generating unique project name, falling back to base class: {}", e.getMessage());
			return super.generateUniqueName(clazzName);
		}
	}

	@Transactional (readOnly = true)
	@PreAuthorize ("permitAll()")
	public List<ProjectClass> getAvailableProjectsForUser(final Long userId) {
		Check.notNull(userId, "User ID must not be null");
		return projectRepository.findNotAssignedToUser(userId);
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<ProjectClass> getById(final Long id) {
		Check.notNull(id, "ID cannot be null");
		// Use findByIdForPageView to fetch with kanbanLine (avoids lazy loading issues)
		return projectRepository.findByIdForPageView(id);
	}

	CCompany getCurrentCompany() {
		Check.notNull(sessionService, "Session service must not be null");
		final CCompany currentCompany = sessionService.getCurrentCompany();
		Check.notNull(currentCompany, "Current company must not be null");
		return currentCompany;
	}

	@Override
	public abstract Class<ProjectClass> getEntityClass();
	@Override
	public abstract Class<?> getInitializerServiceClass();
	@Override
	public abstract Class<?> getPageServiceClass();
	@Override
	public abstract Class<?> getServiceClass();

	@PreAuthorize ("permitAll()")
	public long getTotalProjectCount() { return countByCompany(getCurrentCompany()); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		@SuppressWarnings ("unchecked")
		final ProjectClass entityCasted = (ProjectClass) entity;
		// LOGGER.debug("Initializing new project entity");
		final List<?> availableTypes = typeService.listByCompany(entityCasted.getCompany());
		Check.notEmpty(availableTypes,
				"No project types available in company " + entityCasted.getCompany().getName() + " - cannot initialize project");
		// Cast safely - CProjectTypeService only returns CProjectType instances
		final Object firstType = availableTypes.get(0);
		Check.instanceOf(firstType, CProjectType.class, "Expected CProjectType but got " + firstType.getClass().getSimpleName());
		final CProjectType selectedType = (CProjectType) firstType;
		entityCasted.setEntityType(selectedType);
		// Initialize workflow-based status
		Check.notNull(entityCasted.getWorkflow(), "Workflow cannot be null for project type " + selectedType.getName());
		final CProjectItemStatus initialStatus = IHasStatusAndWorkflowService.getInitialStatus(entityCasted, statusService);
		Check.notNull(initialStatus, "Initial status cannot be null for project");
		entityCasted.setStatus(initialStatus);
	}

	@Override
	@Transactional (readOnly = true)
	public Page<ProjectClass> list(final Pageable pageable) {
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		final CCompany company = getCurrentCompany();
		final Page<ProjectClass> entities = projectRepository.findByCompanyId(company.getId(), safePage);
		return entities;
	}

	@Override
	@Transactional (readOnly = true)
	public Page<ProjectClass> list(final Pageable pageable, final Specification<ProjectClass> filter) {
		LOGGER.debug("Listing entities with filter specification");
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		// Apply company filter to the specification
		final CCompany company = getCurrentCompany();
		final Specification<ProjectClass> companySpec =
				(root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("company").get("id"), company.getId());
		final Specification<ProjectClass> typeSpec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.type(), getEntityClass());
		final Specification<ProjectClass> combinedSpec = filter != null ? companySpec.and(typeSpec).and(filter) : companySpec.and(typeSpec);
		final Page<ProjectClass> page = projectRepository.findAll(combinedSpec, safePage);
		return page;
	}

	@Override
	@Transactional (readOnly = true)
	public Page<ProjectClass> listForPageView(final Pageable pageable, final String searchText) {
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		final String term = searchText == null ? "" : searchText.trim();
		final CCompany company = getCurrentCompany();
		final List<ProjectClass> all = projectRepository.listByCompanyForPageView(company.getId());
		final boolean searchable = ISearchable.class.isAssignableFrom(getEntityClass());
		final List<ProjectClass> filtered = term.isEmpty() || !searchable ? all : all.stream().filter(e -> e.matches(term)).toList();
		final int start = (int) Math.min(safePage.getOffset(), filtered.size());
		final int end = Math.min(start + safePage.getPageSize(), filtered.size());
		final List<ProjectClass> content = filtered.subList(start, end);
		return new PageImpl<>(content, safePage, filtered.size());
	}

	@Override
	@Transactional
	public ProjectClass save(final ProjectClass entity) {
		Check.notNull(entity.getCompany(), "Company must be set before saving a project");
		final boolean isNew = entity.getId() == null;
		final ProjectClass savedEntity = super.save(entity);
		final ProjectListChangeEvent.ChangeType changeType =
				isNew ? ProjectListChangeEvent.ChangeType.CREATED : ProjectListChangeEvent.ChangeType.UPDATED;
		eventPublisher.publishEvent(new ProjectListChangeEvent(this, savedEntity, changeType));
		return projectRepository.findByIdForPageView(savedEntity.getId()).orElse(savedEntity);
	}

	@Override
	protected void validateEntity(final ProjectClass entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getCompany(), ValidationMessages.COMPANY_REQUIRED);
		Check.notNull(entity.getEntityType(), "Project Type is required");
		Check.notNull(entity.getStatus(), "Status is required");
		// 3. Unique Checks - Use helper for company-scoped project
		validateUniqueNameInCompany((IProjectRepository<ProjectClass>) repository, entity, entity.getName(), entity.getCompany());
	}
}
