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
	public CUserTypeService(final CUserTypeRepository repository, final Clock clock) {
		super(repository, clock);
	}

	/**
	 * Gets a user type by ID with eagerly loaded relationships. Overrides parent get()
	 * method to prevent LazyInitializationException.
	 * @param id the user type ID
	 * @return Optional containing the user type with loaded relationships
	 */
	@Override
	@Transactional (readOnly = true)
	public Optional<CUserType> getById(final Long id) {

		if (id == null) {
			LOGGER.debug("Getting CUserType with null ID - returning empty");
			return Optional.empty();
		}
		final Optional<CUserType> entity =
			((CUserTypeRepository) repository).findByIdWithRelationships(id);
		entity.ifPresent(this::initializeLazyFields);
		return entity;
	}

	@Override
	protected Class<CUserType> getEntityClass() { return CUserType.class; }
}