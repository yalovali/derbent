package tech.derbent.abstracts.services;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.projects.domain.CProject;

public abstract class CEntityOfProjectService<EntityClass extends CEntityOfProject<EntityClass>> extends CAbstractNamedEntityService<EntityClass> {
	public CEntityOfProjectService(final CEntityOfProjectRepository<EntityClass> repository, final Clock clock) {
		super(repository, clock);
	}

	@Transactional (readOnly = true)
	public long countByProject(final CProject project) {
		Check.notNull(project, "Project cannot be null");
		try {
			return ((CEntityOfProjectRepository<EntityClass>) repository).countByProject(project);
		} catch (final Exception e) {
			LOGGER.error("Error counting entities by project '{}' in {}: {}", project.getName(), getClass().getSimpleName(), e.getMessage(), e);
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

	@Transactional (readOnly = true)
	public List<EntityClass> findEntriesByProject(final CProject project) {
		Check.notNull(project, "Project cannot be null");
		try {
			final List<EntityClass> entities = ((CEntityOfProjectRepository<EntityClass>) repository).findByProject(project);
			// Initialize any additional lazy fields that weren't loaded by the query
			entities.forEach(this::initializeLazyFields);
			return entities;
		} catch (final Exception e) {
			LOGGER.error("Error finding entities by project '{}' in {}: {}", project.getName(), getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException("Failed to find entities by project", e);
		}
	}

	@Transactional (readOnly = true)
	public Page<EntityClass> findEntriesByProject(final CProject project, final Pageable pageable) {
		Check.notNull(project, "Project cannot be null");
		try {
			final Page<EntityClass> entities = ((CEntityOfProjectRepository<EntityClass>) repository).findByProject(project, pageable);
			// Initialize any additional lazy fields that weren't loaded by the query
			entities.forEach(this::initializeLazyFields);
			return entities;
		} catch (final Exception e) {
			LOGGER.error("Error finding entities by project '{}' with pagination in {}: {}", project.getName(), getClass().getSimpleName(),
					e.getMessage(), e);
			throw new RuntimeException("Failed to find entities by project with pagination", e);
		}
	}

	@Override
	public void initializeLazyFields(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
		try {
			super.initializeLazyFields(entity); // This will handle the project
												// relationship automatically
			// Initialize specific CEntityOfProject relationships that may not be covered
			// by the base
			initializeLazyRelationship(entity.getAssignedTo(), "assignedTo");
			initializeLazyRelationship(entity.getCreatedBy(), "createdBy");
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy fields for {} with ID: {}", getEntityClass().getSimpleName(), entity.getId(), e);
		}
	}

	@Override
	@Transactional
	public EntityClass newEntity() {
		throw new IllegalArgumentException("cannot call newEntity without name and project");
	}

	@Override
	@Transactional
	public EntityClass newEntity(final String name) {
		throw new IllegalArgumentException("cannot call newEntity without project name");
	}

	@Transactional
	public EntityClass newEntity(final String name, final CProject project) {
		if ("fail".equals(name)) {
			throw new RuntimeException("This is for testing the error handler");
		}
		Check.notNull(project, "Project cannot be null");
		Check.notBlank(name, "Entity name cannot be null or empty");
		// Validate inputs
		validateEntityName(name);
		try {
			final Object instance = getEntityClass().getDeclaredConstructor(String.class, CProject.class).newInstance(name, project);
			if (!getEntityClass().isInstance(instance)) {
				throw new IllegalStateException("Created object is not instance of T");
			}
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
		// Check for duplicate names (excluding self for updates)
		final String trimmedName = entity.getName().trim();
		// search with same name and same project exclude self if updating
		final Optional<EntityClass> existing = ((CEntityOfProjectRepository<EntityClass>) repository)
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
			LOGGER.error("save(entity={}) - Error saving entity: {}", entity.getId(), e.getMessage(), e);
			throw new RuntimeException("Failed to save entity", e);
		}
	}
}
