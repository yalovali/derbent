package tech.derbent.decisions.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.projects.domain.CProject;

/**
 * CDecisionTypeService - Service class for CDecisionType entities. Layer: Service (MVC)
 * Provides business logic operations for project-aware decision type management including
 * validation, creation, and status management.
 */
@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionTypeService extends CEntityOfProjectService<CDecisionType> {

	private final CDecisionTypeRepository decisionTypeRepository;

	public CDecisionTypeService(final CDecisionTypeRepository repository,
		final Clock clock) {
		super(repository, clock);
		this.decisionTypeRepository = repository;
	}

	/**
	 * Creates a new decision type with only required fields.
	 * @param name    the decision type name - must not be null or empty
	 * @param project the project this type belongs to - must not be null
	 * @return the created decision type
	 */
	@Transactional
	public CDecisionType createDecisionType(final String name, final CProject project) {
		LOGGER.info("createDecisionType called with name: {} for project: {}", name,
			project != null ? project.getName() : "null");

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.error("createDecisionType called with null or empty name");
			throw new IllegalArgumentException(
				"Decision type name cannot be null or empty");
		}

		if (project == null) {
			LOGGER.error("createDecisionType called with null project");
			throw new IllegalArgumentException("Project cannot be null");
		}
		final CDecisionType decisionType = new CDecisionType(name.trim(), project);
		return repository.saveAndFlush(decisionType);
	}

	@Override
	protected CDecisionType createNewEntityInstance() {
		return new CDecisionType();
	}

	/**
	 * Finds all active decision types for a project.
	 * @param project the project
	 * @return list of active decision types for the project
	 */
	@Transactional (readOnly = true)
	public List<CDecisionType> findAllActiveByProject(final CProject project) {
		LOGGER.info("findAllActiveByProject called for project: {}",
			project != null ? project.getName() : "null");

		if (project == null) {
			return List.of();
		}
		return decisionTypeRepository.findByProjectAndIsActiveTrue(project);
	}

	/**
	 * Finds all decision types for a project ordered by sort order.
	 * @param project the project
	 * @return list of decision types for the project sorted by sort order
	 */
	@Transactional (readOnly = true)
	public List<CDecisionType> findAllByProjectOrdered(final CProject project) {
		LOGGER.info("findAllByProjectOrdered called for project: {}",
			project != null ? project.getName() : "null");

		if (project == null) {
			return List.of();
		}
		return decisionTypeRepository.findByProjectOrderBySortOrderAsc(project);
	}

	/**
	 * Finds decision types that require approval for a project.
	 * @param project the project
	 * @return list of decision types that require approval for the project
	 */
	@Transactional (readOnly = true)
	public List<CDecisionType> findRequiringApprovalByProject(final CProject project) {
		LOGGER.info("findRequiringApprovalByProject called for project: {}",
			project != null ? project.getName() : "null");

		if (project == null) {
			return List.of();
		}
		return decisionTypeRepository.findByProjectAndRequiresApprovalTrue(project);
	}

	/**
	 * Gets a decision type by ID with eagerly loaded relationships. Overrides parent
	 * get() method to prevent LazyInitializationException.
	 * @param id the decision type ID
	 * @return Optional containing the decision type with loaded relationships
	 */
	@Override
	@Transactional (readOnly = true)
	public Optional<CDecisionType> get(final Long id) {

		if (id == null) {
			LOGGER.debug("Getting CDecisionType with null ID - returning empty");
			return Optional.empty();
		}
		LOGGER.debug("Getting CDecisionType with ID {} (with eager loading)", id);
		final Optional<CDecisionType> entity =
			decisionTypeRepository.findByIdWithRelationships(id);
		entity.ifPresent(this::initializeLazyFields);
		return entity;
	}

	/**
	 * Updates the sort order of a decision type.
	 * @param decisionType the decision type to update - must not be null
	 * @param sortOrder    the new sort order
	 * @return the updated decision type
	 */
	@Transactional
	public CDecisionType updateSortOrder(final CDecisionType decisionType,
		final Integer sortOrder) {
		LOGGER.info("updateSortOrder called with decisionType: {}, sortOrder: {}",
			decisionType != null ? decisionType.getName() : "null", sortOrder);

		if (decisionType == null) {
			LOGGER.error("updateSortOrder called with null decisionType");
			throw new IllegalArgumentException("Decision type cannot be null");
		}
		decisionType.setSortOrder(sortOrder);
		return repository.saveAndFlush(decisionType);
	}
}
