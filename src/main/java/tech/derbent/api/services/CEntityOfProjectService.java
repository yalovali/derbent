package tech.derbent.api.services;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.utils.CPageableUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.ISessionService;
import tech.derbent.users.domain.CUser;

public abstract class CEntityOfProjectService<EntityClass extends CEntityOfProject<EntityClass>> extends CEntityNamedService<EntityClass> {

	public CEntityOfProjectService(final IEntityOfProjectRepository<EntityClass> repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final EntityClass entity) {
		String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null; // No dependencies found by default
	}

	@Override
	public long count() {
		return countByProject(sessionService.getActiveProject()
				.orElseThrow(() -> new IllegalStateException("No active project selected, cannot count entities without project context")));
	}

	@Transactional (readOnly = true)
	public long countByProject(final CProject project) {
		Check.notNull(project, "Project cannot be null");
		try {
			return ((IEntityOfProjectRepository<EntityClass>) repository).countByProject(project);
		} catch (final Exception e) {
			LOGGER.error("Error counting entities by project '{}' in {}: {}", project.getName(), getClass().getSimpleName(), e.getMessage());
			throw new RuntimeException("Failed to count entities by project", e);
		}
	}

	public EntityClass createEntity(final String name, final CProject project) {
		try {
			Check.notNull(project, "Project cannot be null");
			Check.notBlank(name, "Entity name cannot be null or empty");
			final EntityClass entity = newEntity(name, project);
			repository.saveAndFlush(entity);
			return entity;
		} catch (final Exception e) {
			throw new RuntimeException("Failed to create instance of " + getEntityClass().getName(), e);
		}
	}

	@Override
	public List<EntityClass> findAll() {
		final CProject project = sessionService.getActiveProject()
				.orElseThrow(() -> new IllegalStateException("No active project selected, cannot list entities without project context"));
		return listByProject(project);
	}

	@Transactional (readOnly = true)
	public Optional<EntityClass> findByNameAndProject(final String name, final CProject project) {
		Check.notNull(project, "Project cannot be null");
		Check.notBlank(name, "Entity name cannot be null or empty");
		try {
			final Optional<EntityClass> entities = ((IEntityOfProjectRepository<EntityClass>) repository).findByNameAndProject(name, project);
			return entities;
		} catch (final Exception e) {
			LOGGER.error("Error finding entities by project '{}' in {}: {}", project.getName(), getClass().getSimpleName(), e.getMessage());
			throw new RuntimeException("Failed to find entities by project", e);
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

	public EntityClass getRandom(final CProject project) {
		Check.notNull(project, "Project cannot be null");
		final List<EntityClass> all = listByProject(project);
		if (all.isEmpty()) {
			throw new IllegalStateException("No entities found for project: " + project.getName());
		}
		final int randomIndex = (int) (Math.random() * all.size());
		return all.get(randomIndex);
	}

	@Override
	public void initializeNewEntity(final EntityClass entity) {
		super.initializeNewEntity(entity);
		Check.notNull(sessionService, "Session service is required for entity initialization");
		// Initialize project from session
		Optional<CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isPresent()) {
			entity.setProject(activeProject.get());
		}
		// Initialize createdBy from session
		Optional<CUser> currentUser = sessionService.getActiveUser();
		if (currentUser.isPresent()) {
			entity.setCreatedBy(currentUser.get());
		}
		// If entity extends CTypeEntity, initialize common type fields
		if (entity instanceof tech.derbent.api.domains.CTypeEntity) {
			tech.derbent.api.domains.CTypeEntity<?> typeEntity = (tech.derbent.api.domains.CTypeEntity<?>) entity;
			if (typeEntity.getColor() == null || typeEntity.getColor().isEmpty()) {
				typeEntity.setColor("#4A90E2");
			}
			if (typeEntity.getSortOrder() == null) {
				typeEntity.setSortOrder(100);
			}
			typeEntity.setAttributeNonDeletable(false);
		}
	}

	@Override
	@Transactional (readOnly = true)
	public Page<EntityClass> list(final Pageable pageable) {
		final CProject project = sessionService.getActiveProject()
				.orElseThrow(() -> new IllegalStateException("No active project selected, cannot list entities without project context"));
		return listByProject(project, pageable);
	}

	@Override
	@Transactional (readOnly = true)
	public Page<EntityClass> list(final Pageable pageable, final String searchText) {
		final CProject project = sessionService.getActiveProject()
				.orElseThrow(() -> new IllegalStateException("No active project selected, cannot list entities without project context"));
		return listByProject(project, pageable, searchText);
	}

	@Transactional (readOnly = true)
	public List<EntityClass> listByProject(final CProject project) {
		Check.notNull(project, "Project cannot be null");
		try {
			final List<EntityClass> entities = ((IEntityOfProjectRepository<EntityClass>) repository).listByProject(project);
			return entities;
		} catch (final RuntimeException ex) {
			LOGGER.error("findByProject failed (project: {}): {}", Optional.ofNullable(project.getName()).orElse("<no-name>"), ex.toString(), ex);
			throw ex; // Spring’in exception translation’ını koru
		}
	}

	@Transactional (readOnly = true)
	public Page<EntityClass> listByProject(final CProject project, final Pageable pageable) {
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
	public Page<EntityClass> listByProject(final CProject project, final Pageable pageable, final String searchText) {
		LOGGER.debug("Listing entities for project:'{}' with search text: '{}'", project != null ? project.getName() : "<null>", searchText);
		Check.notNull(project, "Project cannot be null");
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		final String term = (searchText == null) ? "" : searchText.trim();
		// Pull all for project (ensure repo method DOES NOT fetch to-many relations!)
		final List<EntityClass> all = ((IEntityOfProjectRepository<EntityClass>) repository).listByProject(project, Pageable.unpaged()).getContent();
		final boolean searchable = ISearchable.class.isAssignableFrom(getEntityClass());
		final List<EntityClass> filtered = (term.isEmpty() || !searchable) ? all : all.stream().filter(e -> ((ISearchable) e).matches(term)).toList();
		// --- apply sort from Pageable (name/id supported here; override to extend)
		final List<EntityClass> sorted = applySort(filtered, safePage.getSort());
		// --- slice
		final int start = (int) Math.min(safePage.getOffset(), sorted.size());
		final int end = Math.min(start + safePage.getPageSize(), sorted.size());
		final List<EntityClass> content = sorted.subList(start, end);
		return new PageImpl<>(content, safePage, filtered.size());
	}

	@Override
	@Transactional
	public EntityClass newEntity() {
		throw new IllegalArgumentException("cannot call newEntity without name and project");
	}

	@Override
	@Transactional
	public EntityClass newEntity(final String name) {
		final CProject project = sessionService.getActiveProject()
				.orElseThrow(() -> new IllegalStateException("No active project selected, cannot list entities without project context"));
		return newEntity(name, project);
	}

	@Transactional
	public EntityClass newEntity(final String name, final CProject project) {
		Check.notNull(project, "Project cannot be null");
		Check.notBlank(name, "Entity name cannot be null or empty");
		try {
			final Object instance = getEntityClass().getDeclaredConstructor(String.class, CProject.class).newInstance(name, project);
			Check.instanceOf(instance, getEntityClass(), "Created object is not instance of EntityClass");
			@SuppressWarnings ("unchecked")
			final EntityClass entity = ((EntityClass) instance);
			return entity;
		} catch (final Exception e) {
			throw new RuntimeException("Failed to create instance of " + getEntityClass().getName(), e);
		}
	}

	@Override
	@Transactional
	public EntityClass save(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
		Check.notNull(entity.getProject(), "Entity's project cannot be null");
		final String trimmedName = entity.getName().trim();
		// search with same name and same project exclude self if updating
		final Optional<EntityClass> existing = ((IEntityOfProjectRepository<EntityClass>) repository)
				.findByNameAndProject(trimmedName, entity.getProject()).filter(existingStatus -> {
					// Exclude self if updating
					return (entity.getId() == null) || !existingStatus.getId().equals(entity.getId());
				});
		if (existing.isPresent()) {
			LOGGER.error("save(entity={}) - Entity with name '{}' already exists in project {}", entity.getId(), trimmedName,
					entity.getProject().getName());
			throw new IllegalArgumentException(
					"Entity with name '" + trimmedName + "' already exists in project '" + entity.getProject().getName() + "'");
		}
		try {
			final EntityClass savedStatus = repository.save(entity);
			return savedStatus;
		} catch (final Exception e) {
			LOGGER.error("save(entity={}) - Error saving entity: {}", entity.getId(), e.getMessage());
			throw new RuntimeException("Failed to save entity", e);
		}
	}
}
