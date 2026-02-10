package tech.derbent.api.entityOfProject.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.service.CEntityNamedService;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.CPageableUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.users.domain.CUser;

public abstract class CEntityOfProjectService<EntityClass extends CEntityOfProject<EntityClass>> extends CEntityNamedService<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityOfProjectService.class);

	/** Validates that entity name is unique within project scope. Checks both for new entities and updates, excluding current entity ID.
	 * @param repository the repository to query
	 * @param entity     the entity being validated
	 * @param name       the name to check for uniqueness (trimmed)
	 * @param project    the project scope
	 * @param <T>        the entity type
	 * @throws CValidationException if name is not unique */
	protected static <T extends CEntityOfProject<T>> void validateUniqueNameInProject(final IEntityOfProjectRepository<T> repository, final T entity,
			final String name, final CProject<?> project) {
		Check.notNull(repository, "Repository cannot be null");
		Check.notNull(entity, "Entity cannot be null");
		Check.notBlank(name, "Name cannot be null or empty");
		Check.notNull(project, "Project cannot be null");
		final Optional<T> existing = repository.findByNameAndProject(name.trim(), project);
		if (!(existing.isPresent() && !existing.get().getId().equals(entity.getId()))) {
			return;
		}
		final T existingEntity = existing.get();
		throw new CValidationException(ValidationMessages.formatDuplicate(
			ValidationMessages.DUPLICATE_NAME_IN_PROJECT, 
			name.trim(), 
			existingEntity.getId()));
	}

	public CEntityOfProjectService(final IEntityOfProjectRepository<EntityClass> repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final EntityClass entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		return superCheck != null ? superCheck : null;
	}

	@Override
	public long count() {
		return countByProject(sessionService.getActiveProject()
				.orElseThrow(() -> new IllegalStateException("No active project selected, cannot count entities without project context")));
	}

	@Transactional (readOnly = true)
	public long countByProject(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		try {
			return ((IEntityOfProjectRepository<EntityClass>) repository).countByProject(project);
		} catch (final Exception e) {
			LOGGER.error("Error counting entities by project '{}' in {}: {}", project.getName(), getClass().getSimpleName(), e.getMessage());
			throw e;
		}
	}

	@Override
	public List<EntityClass> findAll() {
		final CProject<?> project = sessionService.getActiveProject()
				.orElseThrow(() -> new IllegalStateException("No active project selected, cannot list entities without project context"));
		return listByProject(project);
	}

	@Transactional (readOnly = true)
	public Optional<EntityClass> findByNameAndProject(final String name, final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		Check.notBlank(name, "Entity name cannot be null or empty");
		try {
			return ((IEntityOfProjectRepository<EntityClass>) repository).findByNameAndProject(name, project);
		} catch (final Exception e) {
			LOGGER.error("Error finding entities by project '{}' in {}: {}", project.getName(), getClass().getSimpleName(), e.getMessage());
			throw e;
		}
	}

	/** Override to generate unique name based on project-specific entity count. Pattern: "EntitySimpleName##" where ## is zero-padded number within
	 * project (e.g., "Activity01", "Meeting02").
	 * @return unique entity name for the current project */
	@Override
	protected String generateUniqueName(String clazzName) {
		try {
			final CProject<?> currentProject = sessionService.getActiveProject()
					.orElseThrow(() -> new CInitializationException("No active project in session - cannot generate unique name"));
			final List<EntityClass> existingUsers = ((IEntityOfProjectRepository<EntityClass>) repository).listByProject(currentProject);
			return getUniqueNameFromList(clazzName.replace("C", ""), existingUsers);
		} catch (final Exception e) {
			LOGGER.warn("Error generating unique user name, falling back to base class: {}", e.getMessage());
			return super.generateUniqueName(clazzName);
		}
	}

	public String getProjectId() {
		// return active project from session
		return sessionService.getActiveProject().map(CProject::getId).orElseThrow(() -> new IllegalStateException("No active project in session"))
				.toString();
	}

	// CEntityOfProjectService içinde
	public String getProjectName() {
		// return active project from session
		return sessionService.getActiveProject().map(CProject::getName).orElseThrow(() -> new IllegalStateException("No active project in session"));
	}

	public EntityClass getRandom(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		final List<EntityClass> all = listByProject(project);
		if (all.isEmpty()) {
			throw new IllegalStateException("No entities found for project: " + project.getName());
		}
		final int randomIndex = (int) (Math.random() * all.size());
		return all.get(randomIndex);
	}

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		@SuppressWarnings ("unchecked")
		final CEntityOfProject<EntityClass> entityCasted = (CEntityOfProject<EntityClass>) entity;
		final ISessionService session = CSpringContext.getBean(ISessionService.class);
		final CProject<?> project = session.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize project-scoped entity"));
		final CUser user = session.getActiveUser().orElseThrow();
		LOGGER.debug("Initializing {} with project '{}' and user '{}'", getEntityClass().getSimpleName(), project.getName(), user.getLogin());
		entityCasted.setProject(project);
		entityCasted.setAssignedTo(user);
		entityCasted.setCreatedBy(user);
	}

	@Override
	@Transactional (readOnly = true)
	public Page<EntityClass> list(final Pageable pageable) {
		final CProject<?> project = sessionService.getActiveProject()
				.orElseThrow(() -> new IllegalStateException("No active project selected, cannot list entities without project context"));
		return listByProject(project, pageable);
	}

	@Transactional (readOnly = true)
	public List<EntityClass> listByProject(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		try {
			return ((IEntityOfProjectRepository<EntityClass>) repository).listByProject(project);
		} catch (final RuntimeException ex) {
			LOGGER.error("findByProject failed (project: {}): {}", Optional.ofNullable(project.getName()).orElse("<no-name>"), ex.toString(), ex);
			throw ex; // Spring’in exception translation’ını koru
		}
	}

	@Transactional (readOnly = true)
	public Page<EntityClass> listByProject(final CProject<?> project, final Pageable pageable) {
		Check.notNull(project, "Project cannot be null");
		final Pageable safe = CPageableUtils.validateAndFix(pageable);
		try {
			return ((IEntityOfProjectRepository<EntityClass>) repository).listByProject(project, safe);
		} catch (final RuntimeException ex) {
			LOGGER.error("findByProject failed (project: {}, page: {}): {}", Optional.ofNullable(project.getName()).orElse("<no-name>"), safe,
					ex.toString(), ex);
			throw ex; // Spring’in exception translation’ını koru
		}
	}

	@Transactional (readOnly = true)
	public Page<EntityClass> listByProjectForPageView(final CProject<?> project, final Pageable pageable, final String searchText) {
		LOGGER.debug("Listing entities for project:'{}' with search text: '{}'", project != null ? project.getName() : "<null>", searchText);
		Check.notNull(project, "Project cannot be null");
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		final String term = searchText == null ? "" : searchText.trim();
		// Repository query includes ORDER BY clause, no need for manual sorting
		final List<EntityClass> all = ((IEntityOfProjectRepository<EntityClass>) repository).listByProjectForPageView(project);
		final boolean searchable = ISearchable.class.isAssignableFrom(getEntityClass());
		final List<EntityClass> filtered = term.isEmpty() || !searchable ? all : all.stream().filter(e -> ((ISearchable) e).matches(term)).toList();
		// Data is already sorted by repository query
		final int start = (int) Math.min(safePage.getOffset(), filtered.size());
		final int end = Math.min(start + safePage.getPageSize(), filtered.size());
		final List<EntityClass> content = filtered.subList(start, end);
		return new PageImpl<>(content, safePage, filtered.size());
	}

	@Override
	@Transactional (readOnly = true)
	public Page<EntityClass> listForPageView(final Pageable pageable, final String searchText) {
		final CProject<?> project = sessionService.getActiveProject()
				.orElseThrow(() -> new IllegalStateException("No active project selected, cannot list entities without project context"));
		return listByProjectForPageView(project, pageable, searchText);
	}

	@Override
	@Transactional
	public EntityClass newEntity() {
		return newEntity("New " + getEntityClass().getSimpleName());
	}

	@Override
	@Transactional
	public EntityClass newEntity(final String name) {
		final CProject<?> project = sessionService.getActiveProject()
				.orElseThrow(() -> new IllegalStateException("No active project selected, cannot list entities without project context"));
		return newEntity(name, project);
	}

	@Transactional
	public EntityClass newEntity(final String name, final CProject<?> project) {
		// LOGGER.debug("Creating new entity with name '{}' in project '{}'", name, project != null ? project.getName() : "<null>");
		Check.notNull(project, "Project cannot be null");
		Check.notBlank(name, "Entity name cannot be null or empty");
		try {
			final Object instance = getEntityClass().getDeclaredConstructor(String.class, CProject.class).newInstance(name, project);
			Check.instanceOf(instance, getEntityClass(), "Created object is not instance of EntityClass");
			@SuppressWarnings ("unchecked")
			final EntityClass entity = (EntityClass) instance;
			return entity;
		} catch (final Exception e) {
			throw new RuntimeException("Failed to create instance of " + getEntityClass().getName(), e);
		}
	}

	protected void setNameOfEntity(final EntityClass entity, final String prefix) {
		try {
			final Optional<CProject<?>> activeProject = sessionService.getActiveProject();
			activeProject.map(value -> ((IEntityOfProjectRepository<?>) repository).countByProject(value))
					.map(priorityCount -> String.format(prefix + " %02d", priorityCount + 1)).ifPresent(entity::setName);
		} catch (final Exception e) {
			LOGGER.error("Error setting name of entity: {}", e.getMessage());
			throw e;
		}
	}
	// ========== Static Validation Helper Methods ==========

	@Override
	protected void validateEntity(final EntityClass entity) {
		super.validateEntity(entity);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		validateUniqueNameInProject((IEntityOfProjectRepository<EntityClass>) repository, entity, entity.getName(), entity.getProject());
	}
}
