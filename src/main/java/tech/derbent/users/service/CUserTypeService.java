package tech.derbent.users.service;

import java.time.Clock;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.users.domain.CUserType;
import tech.derbent.projects.domain.CProject;

/**
 * CUserTypeService - Service layer for CUserType entity. Layer: Service (MVC) Handles
 * business logic for project-aware user type operations.
 */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CUserTypeService extends CEntityOfProjectService<CUserType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserTypeService.class);

	/**
	 * Constructor for CUserTypeService.
	 * @param repository the CUserTypeRepository to use for data access
	 * @param clock      the Clock instance for time-related operations
	 */
	CUserTypeService(final CUserTypeRepository repository, final Clock clock) {
		super(repository, clock);
	}

	/**
	 * Creates a new user type entity with name, description and project.
	 * @param name        the name of the user type
	 * @param description the description of the user type
	 * @param project     the project this type belongs to
	 */
	@Transactional
	public void createEntity(final String name, final String description, final CProject project) {
		LOGGER.info("Creating new user type: {} with description: {} for project: {}", 
			name, description, project.getName());

		// Standard test failure logic for error handler testing
		if ("fail".equals(name)) {
			LOGGER.warn("Test failure requested for name: {}", name);
			throw new RuntimeException("This is for testing the error handler");
		}
		// Validate name using parent validation
		validateEntityName(name);
		final CUserType entity = new CUserType(name, description, project);
		repository.saveAndFlush(entity);
		LOGGER.info("User type created successfully with name: {} for project: {}", 
			name, project.getName());
	}

	@Override
	protected CUserType createNewEntityInstance() {
		return new CUserType();
	}

	/**
	 * Gets a user type by ID with eagerly loaded relationships.
	 * Overrides parent get() method to prevent LazyInitializationException.
	 * @param id the user type ID
	 * @return Optional containing the user type with loaded relationships
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<CUserType> get(final Long id) {
		LOGGER.debug("Getting CUserType with ID {} (with eager loading)", id);
		final Optional<CUserType> entity = ((CUserTypeRepository) repository).findByIdWithRelationships(id);
		entity.ifPresent(this::initializeLazyFields);
		return entity;
	}
}