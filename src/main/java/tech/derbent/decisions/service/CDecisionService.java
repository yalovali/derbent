package tech.derbent.decisions.service;

import java.time.Clock;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.domain.CDecisionApproval;
import tech.derbent.users.domain.CUser;

/**
 * CDecisionService - Service class for CDecision entities. Layer: Service (MVC) Provides
 * business logic operations for decision management including validation, creation,
 * approval workflow management, and project-based queries.
 */
@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionService extends CEntityOfProjectService<CDecision> {

	public CDecisionService(final CDecisionRepository repository, final Clock clock) {
		super(repository, clock);
	}

	/**
	 * Adds an approval requirement to a decision.
	 * @param decision the decision - must not be null
	 * @param approver the approver user - must not be null
	 * @return the created approval
	 */
	@Transactional
	public CDecisionApproval addApprovalRequirement(final CDecision decision,
		final CUser approver) {
		LOGGER.info("addApprovalRequirement called with decision: {}, approver: {}",
			decision != null ? decision.getName() : "null",
			approver != null ? approver.getName() : "null");

		if (decision == null) {
			LOGGER.error("addApprovalRequirement called with null decision");
			throw new IllegalArgumentException("Decision cannot be null");
		}

		if (approver == null) {
			LOGGER.error("addApprovalRequirement called with null approver");
			throw new IllegalArgumentException("Approver cannot be null");
		}
		final CDecisionApproval approval = new CDecisionApproval("approval");
		approval.setDecision(decision);
		decision.addApproval(approval);
		repository.saveAndFlush(decision);
		return approval;
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
		return ((CDecisionService) repository).findDecisionsPendingApprovalByUser(user);
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

	@Override
	protected Class<CDecision> getEntityClass() { return CDecision.class; }

	/**
	 * Enhanced initialization of lazy-loaded fields specific to Decision entities. Based
	 * on CActivityService implementation style.
	 * @param entity the decision entity to initialize
	 */
	@Override
	protected void initializeLazyFields(final CDecision entity) {

		if (entity == null) {
			return;
		}
		LOGGER.debug("Initializing lazy fields for Decision with ID: {} entity: {}",
			entity.getId(), entity.getName());

		try {
			// First call the parent implementation to handle common fields
			super.initializeLazyFields(entity);
			// Initialize Decision-specific relationships
			initializeLazyRelationship(entity.getDecisionType());
			initializeLazyRelationship(entity.getDecisionStatus());
			initializeLazyRelationship(entity.getAccountableUser());

			// Handle collections
			if ((entity.getTeamMembers() != null) && !entity.getTeamMembers().isEmpty()) {
				entity.getTeamMembers().forEach(this::initializeLazyRelationship);
			}

			if ((entity.getApprovals() != null) && !entity.getApprovals().isEmpty()) {
				entity.getApprovals().forEach(this::initializeLazyRelationship);
			}
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy fields for Decision with ID: {}",
				entity.getId(), e);
		}
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