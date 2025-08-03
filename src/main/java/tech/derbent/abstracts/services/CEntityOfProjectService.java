package tech.derbent.abstracts.services;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;

/**
 * CEntityOfProjectService - Abstract service class for entities that extend
 * CEntityOfProject. Layer: Service (MVC) Provides common business logic operations for
 * project-aware entities including validation, creation, and project-based queries with
 * consistent error handling, logging, and proper lazy loading support.
 */
public abstract class CEntityOfProjectService<
	EntityClass extends CEntityOfProject<EntityClass>>
	extends CAbstractNamedEntityService<EntityClass> {

	protected final CEntityOfProjectRepository<EntityClass> projectRepository;

	/**
	 * Constructor for CEntityOfProjectService.
	 * @param repository the repository for data access operations
	 * @param clock      the Clock instance for time-related operations
	 */
	public CEntityOfProjectService(
		final CEntityOfProjectRepository<EntityClass> repository, final Clock clock) {
		super(repository, clock);
		this.projectRepository = repository;
	}

	/**
	 * Counts the number of entities for a specific project.
	 * @param project the project
	 * @return count of entities for the project
	 */
	@Transactional (readOnly = true)
	public long countByProject(final CProject project) {
		LOGGER.info("countByProject called with project: {} for {}",
			project != null ? project.getName() : "null", getClass().getSimpleName());

		if (project == null) {
			LOGGER.warn("countByProject called with null project for {}",
				getClass().getSimpleName());
			return 0L;
		}

		try {
			return projectRepository.countByProject(project);
		} catch (final Exception e) {
			LOGGER.error("Error counting entities by project '{}' in {}: {}",
				project.getName(), getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException("Failed to count entities by project", e);
		}
	}

	public EntityClass createEntity(final String name, final CProject project) {

		try {
			final EntityClass entity = newEntity(name, project);
			repository.saveAndFlush(entity);
			return entity;
		} catch (final Exception e) {
			throw new RuntimeException(
				"Failed to create instance of " + getEntityClass().getName(), e);
		}
	}

	/**
	 * Finds all entities by project with all relationships eagerly loaded. This
	 * implementation is similar to CActivityService but without explicit null checks or
	 * triggering lazy loading through getter methods.
	 * @param project the project to find entities for
	 * @return list of entities with all relationships initialized
	 */
	@Transactional (readOnly = true)
	public List<EntityClass> findByProjectWithAllRelationships(final CProject project) {
		LOGGER.info("findByProjectWithAllRelationships called for project: {}",
			project != null ? project.getName() : "null");
		// Direct repository call
		final List<EntityClass> entities = projectRepository.findByProject(project);
		// Initialize lazy fields without null checks or triggering loading
		entities.forEach(this::initializeLazyFields);
		return entities;
	}

	/**
	 * Finds entities by project with properly loaded relationships to prevent
	 * LazyInitializationException. This method provides the correct generic type for
	 * CEntityOfProject entities.
	 * @param project the project
	 * @return list of entities with loaded relationships
	 */
	@Transactional (readOnly = true)
	public List<EntityClass> findEntitiesByProject(final CProject project) {
		Assert.notNull(project,
			"Project must not be null for " + getClass().getSimpleName());

		try {
			final List<EntityClass> entities = projectRepository.findByProject(project);
			// Additional lazy field initialization if needed
			entities.forEach(this::initializeLazyFields);
			return entities;
		} catch (final Exception e) {
			LOGGER.error("Error finding entities by project '{}' in {}: {}",
				project.getName(), getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException("Failed to find entities by project", e);
		}
	}

	/**
	 * Finds entities by project with pagination and properly loaded relationships. This
	 * method provides the correct generic type for CEntityOfProject entities.
	 * @param project  the project
	 * @param pageable pagination information
	 * @return list of entities with loaded relationships
	 */
	@Transactional (readOnly = true)
	public List<EntityClass> findEntitiesByProject(final CProject project,
		final Pageable pageable) {
		LOGGER.info("findEntitiesByProject called with project: {} and pageable for {}",
			project != null ? project.getName() : "null", getClass().getSimpleName());

		if (project == null) {
			LOGGER.warn("findEntitiesByProject called with null project for {}",
				getClass().getSimpleName());
			return List.of();
		}

		try {
			final List<EntityClass> entities =
				projectRepository.findByProject(project, pageable);
			// Additional lazy field initialization if needed
			entities.forEach(this::initializeLazyFields);
			return entities;
		} catch (final Exception e) {
			LOGGER.error(
				"Error finding entities by project '{}' with pagination in {}: {}",
				project.getName(), getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException(
				"Failed to find entities by project with pagination", e);
		}
	}

	/**
	 * Override get() method to use the repository method that eagerly loads project
	 * relationships to prevent LazyInitializationException.
	 * @param id the entity ID
	 * @return optional entity with loaded relationships
	 */
	@Override
	@Transactional (readOnly = true)
	public Optional<EntityClass> getById(final Long id) {
		LOGGER.info("get called with id: {} for {}", id, getClass().getSimpleName());

		if (id == null) {
			return Optional.empty();
		}

		try {
			final Optional<EntityClass> entity =
				projectRepository.findByIdWithProjectRelationships(id);
			// Initialize any additional lazy fields
			entity.ifPresent(this::initializeLazyFields);
			return entity;
		} catch (final Exception e) {
			LOGGER.error("Error getting entity by id '{}' in {}: {}", id,
				getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException("Failed to get entity by id", e);
		}
	}

	/**
	 * Enhanced initialization of lazy-loaded fields for CEntityOfProject entities. Based
	 * on CActivityService implementation style without null checks or explicit loading.
	 * @param entity the entity to initialize
	 */
	@Override
	protected void initializeLazyFields(final EntityClass entity) {

		if (entity == null) {
			return;
		}

		try {
			// First call parent initialization
			super.initializeLazyFields(entity);
			// Initialize CEntityOfProject specific relationships
			initializeLazyRelationship(entity.getProject());
			initializeLazyRelationship(entity.getAssignedTo());
			initializeLazyRelationship(entity.getCreatedBy());
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy fields for {} with ID: {}",
				getEntityClass().getSimpleName(), entity.getId(), e);
		}
	}

	@Override
	@Transactional
	public EntityClass newEntity() {
		throw new IllegalArgumentException(
			"cannot call newEntity without name and project");
	}

	@Override
	@Transactional
	public EntityClass newEntity(final String name) {
		throw new IllegalArgumentException("cannot call newEntity without project");
	}

	@Transactional
	public EntityClass newEntity(final String name, final CProject project) {

		if ("fail".equals(name)) {
			throw new RuntimeException("This is for testing the error handler");
		}
		// Validate inputs
		validateEntityName(name);

		try {
			final Object instance =
				getEntityClass().getDeclaredConstructor(String.class, CProject.class)
					.newInstance(name, project);

			if (!getEntityClass().isInstance(instance)) {
				throw new IllegalStateException("Created object is not instance of T");
			}
			@SuppressWarnings ("unchecked")
			final EntityClass entity = ((EntityClass) instance);
			return entity;
		} catch (final Exception e) {
			throw new RuntimeException(
				"Failed to create instance of " + getEntityClass().getName(), e);
		}
	}
}