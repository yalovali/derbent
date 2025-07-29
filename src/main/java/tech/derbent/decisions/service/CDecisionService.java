package tech.derbent.decisions.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.domain.CDecisionApproval;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * CDecisionService - Service class for CDecision entities. Layer: Service (MVC) Provides
 * business logic operations for decision management including validation, creation,
 * approval workflow management, and project-based queries.
 */
@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionService extends CEntityOfProjectService<CDecision> {

	private final CDecisionRepository decisionRepository;

	public CDecisionService(final CDecisionRepository repository, final Clock clock) {
		super(repository, clock);
		this.decisionRepository = repository;
	}

	/**
	 * Adds an approval requirement to a decision.
	 * @param decision   the decision - must not be null
	 * @param approver   the approver user - must not be null
	 * @param isRequired whether the approval is required
	 * @return the created approval
	 */
	@Transactional
	public CDecisionApproval addApprovalRequirement(final CDecision decision,
		final CUser approver, final boolean isRequired) {
		LOGGER.info(
			"addApprovalRequirement called with decision: {}, approver: {}, isRequired: {}",
			decision != null ? decision.getName() : "null",
			approver != null ? approver.getName() : "null", isRequired);

		if (decision == null) {
			LOGGER.error("addApprovalRequirement called with null decision");
			throw new IllegalArgumentException("Decision cannot be null");
		}

		if (approver == null) {
			LOGGER.error("addApprovalRequirement called with null approver");
			throw new IllegalArgumentException("Approver cannot be null");
		}
		final CDecisionApproval approval =
			new CDecisionApproval(decision, approver, isRequired, 3, null);
		decision.addApproval(approval);
		// Save the decision which will cascade to the approval
		repository.saveAndFlush(decision);
		return approval;
	}

	/**
	 * Creates a new decision for a project.
	 * @param name    the decision name - must not be null or empty
	 * @param project the project - must not be null
	 * @return the created decision
	 */
	@Transactional
	public CDecision createDecision(final String name, final CProject project) {
		LOGGER.info("createDecision called with name: {}, project: {}", name,
			project != null ? project.getName() : "null");

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.error("createDecision called with null or empty name");
			throw new IllegalArgumentException("Decision name cannot be null or empty");
		}

		if (project == null) {
			LOGGER.error("createDecision called with null project");
			throw new IllegalArgumentException("Project cannot be null");
		}
		final CDecision decision = new CDecision(name.trim(), project);
		return repository.saveAndFlush(decision);
	}

	/**
	 * Creates a new decision with description.
	 * @param name        the decision name - must not be null or empty
	 * @param project     the project - must not be null
	 * @param description the description - can be null
	 * @return the created decision
	 */
	@Transactional
	public CDecision createDecision(final String name, final CProject project,
		final String description) {
		LOGGER.info("createDecision called with name: {}, project: {}, description: {}",
			name, project != null ? project.getName() : "null", description);
		final CDecision decision = createDecision(name, project);

		if ((description != null) && !description.trim().isEmpty()) {
			decision.setDescription(description.trim());
			return repository.saveAndFlush(decision);
		}
		return decision;
	}

	@Override
	protected CDecision createNewEntityInstance() {
		return new CDecision();
	}

	/**
	 * Finds decisions by accountable user.
	 * @param user the accountable user
	 * @return list of decisions where the user is accountable
	 */
	@Transactional (readOnly = true)
	public List<CDecision> findByAccountableUser(final CUser user) {
		LOGGER.info("findByAccountableUser called with user: {}",
			user != null ? user.getName() : "null");

		if (user == null) {
			LOGGER.warn("findByAccountableUser called with null user");
			return List.of();
		}
		return decisionRepository.findByAccountableUser(user);
	}

	/**
	 * Finds a decision by ID with eagerly loaded decision type.
	 * @param id the decision ID
	 * @return optional CDecision with loaded decisionType
	 */
	@Transactional (readOnly = true)
	public Optional<CDecision> findByIdWithDecisionType(final Long id) {
		LOGGER.info("findByIdWithDecisionType called with id: {}", id);

		if (id == null) {
			LOGGER.warn("findByIdWithDecisionType called with null id");
			return Optional.empty();
		}
		return decisionRepository.findByIdWithDecisionType(id);
	}

	/**
	 * Finds all decisions by project with eagerly loaded relationships.
	 * @param project the project
	 * @return list of CDecision with loaded relationships
	 */
	@Transactional (readOnly = true)
	public List<CDecision> findByProjectWithAllRelationships(final CProject project) {
		LOGGER.info("findByProjectWithAllRelationships called with project: {}",
			project != null ? project.getName() : "null");

		if (project == null) {
			LOGGER.warn("findByProjectWithAllRelationships called with null project");
			return List.of();
		}
		return decisionRepository.findByProjectWithAllRelationships(project);
	}

	/**
	 * Finds decisions where the user is a team member.
	 * @param user the team member user
	 * @return list of decisions where the user is a team member
	 */
	@Transactional (readOnly = true)
	public List<CDecision> findByTeamMember(final CUser user) {
		LOGGER.info("findByTeamMember called with user: {}",
			user != null ? user.getName() : "null");

		if (user == null) {
			LOGGER.warn("findByTeamMember called with null user");
			return List.of();
		}
		return ((CDecisionRepository) repository).findByTeamMembersContaining(user);
	}

	/**
	 * Finds decisions that require approval from a specific user.
	 * @param user the approver user
	 * @return list of decisions that need approval from the user
	 */
	@Transactional (readOnly = true)
	public List<CDecision> findDecisionsPendingApprovalByUser(final CUser user) {
		LOGGER.info("findDecisionsPendingApprovalByUser called with user: {}",
			user != null ? user.getName() : "null");

		if (user == null) {
			LOGGER.warn("findDecisionsPendingApprovalByUser called with null user");
			return List.of();
		}
		return ((CDecisionRepository) repository)
			.findDecisionsPendingApprovalByUser(user);
	}

	/**
	 * Override get() method to eagerly load relationships and prevent
	 * LazyInitializationException. Following the comprehensive lazy loading fix pattern
	 * from the guidelines.
	 * @param id the decision ID
	 * @return optional CDecision with all relationships loaded
	 */
	@Override
	@Transactional (readOnly = true)
	public Optional<CDecision> get(final Long id) {
		LOGGER.info("get called with id: {} (overridden to eagerly load relationships)",
			id);

		if (id == null) {
			return Optional.empty();
		}
		final Optional<CDecision> entity =
			((CDecisionRepository) repository).findByIdWithAllRelationships(id);
		entity.ifPresent(this::initializeLazyFields);
		return entity;
	}

	/**
	 * Gets the approval progress for a decision.
	 * @param decision the decision
	 * @return string representation of approval progress (e.g., "2/5 approved")
	 */
	@Transactional (readOnly = true)
	public String getApprovalProgress(final CDecision decision) {
		LOGGER.info("getApprovalProgress called with decision: {}",
			decision != null ? decision.getName() : "null");

		if (decision == null) {
			LOGGER.warn("getApprovalProgress called with null decision");
			return "0/0 approved";
		}
		final int approvedCount = decision.getApprovedCount();
		final int totalCount = decision.getApprovalCount();
		return String.format("%d/%d approved", approvedCount, totalCount);
	}

	/**
	 * Checks if a decision is fully approved.
	 * @param decision the decision to check
	 * @return true if all required approvals are granted
	 */
	@Transactional (readOnly = true)
	public boolean isDecisionFullyApproved(final CDecision decision) {
		LOGGER.info("isDecisionFullyApproved called with decision: {}",
			decision != null ? decision.getName() : "null");

		if (decision == null) {
			LOGGER.warn("isDecisionFullyApproved called with null decision");
			return false;
		}
		return decision.isFullyApproved();
	}
}