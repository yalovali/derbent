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
 * CDecisionService - Service class for CDecision entities. Layer: Service (MVC) Provides business logic operations for
 * decision management including validation, creation, approval workflow management, and project-based queries.
 */
@Service
@PreAuthorize("isAuthenticated()")
public class CDecisionService extends CEntityOfProjectService<CDecision> {

    public CDecisionService(final CDecisionRepository repository, final Clock clock) {
        super(repository, clock);
    }

    /**
     * Adds an approval requirement to a decision.
     * 
     * @param decision
     *            the decision - must not be null
     * @param approver
     *            the approver user - must not be null
     * @return the created approval
     */
    @Transactional
    public CDecisionApproval addApprovalRequirement(final CDecision decision, final CUser approver) {
        LOGGER.info("addApprovalRequirement called with decision: {}, approver: {}",
                decision != null ? decision.getName() : "null", approver != null ? approver.getName() : "null");

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
     * 
     * @param user
     *            the team member user
     * @return list of decisions where the user is a team member
     */
    @Transactional(readOnly = true)
    public List<CDecision> findByTeamMember(final CUser user) {
        LOGGER.info("findByTeamMember called with user: {}", user != null ? user.getName() : "null");

        if (user == null) {
            LOGGER.warn("findByTeamMember called with null user");
            return List.of();
        }
        return ((CDecisionRepository) repository).findByTeamMembersContaining(user);
    }

    /**
     * Finds decisions that require approval from a specific user.
     * 
     * @param user
     *            the approver user
     * @return list of decisions that need approval from the user
     */
    @Transactional(readOnly = true)
    public List<CDecision> findDecisionsPendingApprovalByUser(final CUser user) {
        LOGGER.info("findDecisionsPendingApprovalByUser called with user: {}", user != null ? user.getName() : "null");

        if (user == null) {
            LOGGER.warn("findDecisionsPendingApprovalByUser called with null user");
            return List.of();
        }
        return ((CDecisionService) repository).findDecisionsPendingApprovalByUser(user);
    }

    /**
     * Gets the approval progress for a decision.
     * 
     * @param decision
     *            the decision
     * @return string representation of approval progress (e.g., "2/5 approved")
     */
    @Transactional(readOnly = true)
    public String getApprovalProgress(final CDecision decision) {
        LOGGER.info("getApprovalProgress called with decision: {}", decision != null ? decision.getName() : "null");

        if (decision == null) {
            LOGGER.warn("getApprovalProgress called with null decision");
            return "0/0 approved";
        }
        final int approvedCount = decision.getApprovedCount();
        final int totalCount = decision.getApprovalCount();
        return String.format("%d/%d approved", approvedCount, totalCount);
    }

    @Override
    protected Class<CDecision> getEntityClass() {
        return CDecision.class;
    }

    /**
     * Gets a decision by ID with all relationships eagerly loaded. This prevents LazyInitializationException
     * when accessing decision details.
     * 
     * @param id
     *            the decision ID
     * @return optional decision with loaded relationships
     */
    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<CDecision> getById(final Long id) {
        if (id == null) {
            return java.util.Optional.empty();
        }

        LOGGER.debug("Getting CDecision with ID {} (overridden to eagerly load relationships)", id);
        final java.util.Optional<CDecision> entity = ((CDecisionRepository) repository).findByIdWithAllRelationships(id);
        entity.ifPresent(this::initializeLazyFields);
        return entity;
    }

    /**
     * Enhanced initialization of lazy-loaded fields specific to Decision entities. Uses improved null-safe patterns.
     * 
     * @param entity
     *            the decision entity to initialize
     */
    @Override
    public void initializeLazyFields(final CDecision entity) {

        if (entity == null) {
            LOGGER.debug("Decision entity is null, skipping lazy field initialization");
            return;
        }

        try {
            super.initializeLazyFields(entity); // Handles CEntityOfProject relationships automatically
            initializeLazyRelationship(entity.getDecisionType(), "decisionType");
            initializeLazyRelationship(entity.getDecisionStatus(), "decisionStatus");
            initializeLazyRelationship(entity.getAccountableUser(), "accountableUser");

            if ((entity.getTeamMembers() != null) && !entity.getTeamMembers().isEmpty()) {
                entity.getTeamMembers().forEach(member -> initializeLazyRelationship(member, "teamMember"));
            }

            if ((entity.getApprovals() != null) && !entity.getApprovals().isEmpty()) {
                entity.getApprovals().forEach(approval -> initializeLazyRelationship(approval, "approval"));
            }
        } catch (final Exception e) {
            LOGGER.warn("Error initializing lazy fields for Decision with ID: {}", entity.getId(), e);
        }
    }

    /**
     * Checks if a decision is fully approved.
     * 
     * @param decision
     *            the decision to check
     * @return true if all required approvals are granted
     */
    @Transactional(readOnly = true)
    public boolean isDecisionFullyApproved(final CDecision decision) {
        LOGGER.info("isDecisionFullyApproved called with decision: {}", decision != null ? decision.getName() : "null");

        if (decision == null) {
            LOGGER.warn("isDecisionFullyApproved called with null decision");
            return false;
        }
        return decision.isFullyApproved();
    }
}