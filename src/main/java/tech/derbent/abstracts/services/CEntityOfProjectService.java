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
	 * Finds entities by project with all relationships eagerly loaded. This is the
	 * standard method for project-based queries that loads all necessary relationships
	 * to prevent LazyInitializationException.
	 * 
	 * @param project the project to find entities for
	 * @return list of entities with all relationships initialized
	 */
	@Transactional(readOnly = true)
	public List<EntityClass> findEntriesByProject(final CProject project) {
		LOGGER.info("findEntriesByProject called for project: {}",
			project != null ? project.getName() : "null");

		if (project == null) {
			LOGGER.warn("findEntriesByProject called with null project for {}",
				getClass().getSimpleName());
			return List.of();
		}

		try {
			final List<EntityClass> entities = projectRepository.findByProject(project);
			// Initialize any additional lazy fields that weren't loaded by the query
			entities.forEach(this::initializeLazyFields);
			return entities;
		} catch (final Exception e) {
			LOGGER.error("Error finding entities by project '{}' in {}: {}",
				project.getName(), getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException("Failed to find entities by project", e);
		}
	}

	/**
	 * Finds entities by project with pagination support. This method supports grid
	 * functionality while maintaining eager loading of all relationships.
	 * 
	 * @param project  the project
	 * @param pageable pagination information
	 * @return list of entities with loaded relationships
	 */
	@Transactional(readOnly = true)
	public List<EntityClass> findEntriesByProject(final CProject project,
		final Pageable pageable) {
		LOGGER.info("findEntriesByProject called with project: {} and pageable for {}",
			project != null ? project.getName() : "null", getClass().getSimpleName());

		if (project == null) {
			LOGGER.warn("findEntriesByProject called with null project for {}",
				getClass().getSimpleName());
			return List.of();
		}

		try {
			final List<EntityClass> entities =
				projectRepository.findByProject(project, pageable);
			// Initialize any additional lazy fields that weren't loaded by the query
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
	 * Gets an entity by ID with all relationships eagerly loaded. This is the standard
	 * method for single entity retrieval that loads all necessary relationships to
	 * prevent LazyInitializationException.
	 * 
	 * @param id the entity ID
	 * @return optional entity with loaded relationships
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<EntityClass> getById(final Long id) {
		LOGGER.info("getById called with id: {} for {}", id, getClass().getSimpleName());

		if (id == null) {
			return Optional.empty();
		}

		try {
			final Optional<EntityClass> entity =
				projectRepository.findByIdWithProjectRelationships(id);
			// With eager loading of small entities, minimal lazy field initialization needed
			entity.ifPresent(this::initializeLazyFields);
			return entity;
		} catch (final Exception e) {
			LOGGER.error("Error getting entity by id '{}' in {}: {}", id,
				getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException("Failed to get entity by id", e);
		}
	}

	/**
	 * Minimal lazy field initialization for CEntityOfProject entities. With eager
	 * loading of small entities (status, type, user references), this mainly handles
	 * any remaining complex relationships.
	 * 
	 * @param entity the entity to initialize
	 */
	@Override
	protected void initializeLazyFields(final EntityClass entity) {

		if (entity == null) {
			return;
		}

		try {
			// Call parent initialization for basic entity relationships
			super.initializeLazyFields(entity);
			// Most relationships are now eager-loaded, minimal initialization needed
			// Only initialize complex collections if present
			if (entity.getProject() != null) {
				initializeLazyRelationship(entity.getProject());
			}
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