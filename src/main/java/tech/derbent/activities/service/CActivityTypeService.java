package tech.derbent.activities.service;

import java.time.Clock;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.projects.domain.CProject;

/**
 * CActivityTypeService - Service layer for CActivityType entity. Layer: Service (MVC)
 * Handles business logic for project-aware activity type operations.
 */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CActivityTypeService extends CEntityOfProjectService<CActivityType> {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CActivityTypeService.class);

	/**
	 * Constructor for CActivityTypeService.
	 * @param repository the CActivityTypeRepository to use for data access
	 * @param clock      the Clock instance for time-related operations
	 */
	CActivityTypeService(final CActivityTypeRepository repository, final Clock clock) {
		super(repository, clock);
	}

	/**
	 * Creates a new activity type entity with name, description and project.
	 * @param name        the name of the activity type
	 * @param description the description of the activity type
	 * @param project     the project this type belongs to
	 */
	@Transactional
	public void createEntity(final String name, final String description, final CProject project) {
		LOGGER.info("Creating new activity type: {} with description: {} for project: {}", 
			name, description, project.getName());

		if ("fail".equals(name)) {
			LOGGER.warn("Test failure requested for name: {}", name);
			throw new RuntimeException("This is for testing the error handler");
		}
		validateEntityName(name);
		final CActivityType entity = new CActivityType(name, project);
		entity.setDescription(description);
		repository.saveAndFlush(entity);
		LOGGER.info("Activity type created successfully with name: {} for project: {}", 
			name, project.getName());
	}

	@Override
	protected CActivityType createNewEntityInstance() {
		return new CActivityType();
	}

	/**
	 * Gets an activity type by ID with eagerly loaded relationships.
	 * Overrides parent get() method to prevent LazyInitializationException.
	 * @param id the activity type ID
	 * @return Optional containing the activity type with loaded relationships
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<CActivityType> get(final Long id) {
		if (id == null) {
			LOGGER.debug("Getting CActivityType with null ID - returning empty");
			return Optional.empty();
		}
		LOGGER.debug("Getting CActivityType with ID {} (with eager loading)", id);
		final Optional<CActivityType> entity = ((CActivityTypeRepository) repository).findByIdWithRelationships(id);
		entity.ifPresent(this::initializeLazyFields);
		return entity;
	}
}