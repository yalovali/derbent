package tech.derbent.decisions.domain;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.users.domain.CUser;

/**
 * CDecisionApproval - Domain entity representing individual approvals for decisions.
 * Layer: Domain (MVC)
 * 
 * Supports:
 * - Tracking approval status for each approver
 * - Recording approval/rejection dates
 * - Storing approval comments and feedback
 * - Managing approval workflow state
 * 
 * Each decision can have multiple approvals from different users or roles.
 */
@Entity
@Table(name = "cdecisionapproval")
@AttributeOverride(name = "id", column = @Column(name = "decision_approval_id"))
public class CDecisionApproval extends CEntityNamed {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDecisionApproval.class);

    // The decision this approval belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private CDecision decision;

    // The user who needs to provide approval
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_user_id", nullable = false)
    @MetaData(
        displayName = "Approver", required = true, readOnly = false,
        description = "User who needs to provide approval", hidden = false, order = 2,
        dataProviderBean = "CUserService"
    )
    private CUser approver;

    // Approval status
    @Column(name = "is_approved", nullable = true)
    @MetaData(
        displayName = "Is Approved", required = false, readOnly = false,
        description = "Approval status: null=pending, true=approved, false=rejected",
        hidden = false, order = 3
    )
    private Boolean isApproved;

    // Date when approval was granted or rejected
    @Column(name = "approval_date", nullable = true)
    @MetaData(
        displayName = "Approval Date", required = false, readOnly = true,
        description = "Date and time when approval decision was made", hidden = false,
        order = 4
    )
    private LocalDateTime approvalDate;

    // Comments from the approver
    @Column(name = "approval_comments", nullable = true, length = 2000)
    @Size(max = 2000)
    @MetaData(
        displayName = "Approval Comments", required = false, readOnly = false,
        description = "Comments or feedback from the approver", hidden = false,
        order = 5, maxLength = 2000
    )
    private String approvalComments;

    // Priority level for this approval
    @Column(name = "approval_priority", nullable = false)
    @MetaData(
        displayName = "Approval Priority", required = true, readOnly = false,
        defaultValue = "3", description = "Priority level (1=Critical, 5=Optional)",
        hidden = false, order = 6
    )
    private Integer approvalPriority = 3;

    // Whether this approval is required or optional
    @Column(name = "is_required", nullable = false)
    @MetaData(
        displayName = "Is Required", required = true, readOnly = false,
        defaultValue = "true",
        description = "Whether this approval is required for decision progression",
        hidden = false, order = 7
    )
    private boolean isRequired = true;

    // Due date for the approval
    @Column(name = "due_date", nullable = true)
    @MetaData(
        displayName = "Due Date", required = false, readOnly = false,
        description = "Date by which approval should be provided", hidden = false,
        order = 8
    )
    private LocalDateTime dueDate;

    /**
     * Default constructor for JPA.
     */
    public CDecisionApproval() {
        super();
        LOGGER.debug("CDecisionApproval() - Creating new decision approval instance");
    }

    /**
     * Constructor with decision and approver.
     * @param decision the decision requiring approval - must not be null
     * @param approver the user who needs to provide approval - must not be null
     */
    public CDecisionApproval(final CDecision decision, final CUser approver) {
        super();
        LOGGER.debug("CDecisionApproval constructor called with decision: {}, approver: {}", 
                    decision != null ? decision.getName() : "null",
                    approver != null ? approver.getName() : "null");

        if (decision == null) {
            LOGGER.warn("CDecisionApproval constructor - Decision parameter is null");
        }
        if (approver == null) {
            LOGGER.warn("CDecisionApproval constructor - Approver parameter is null");
        }

        this.decision = decision;
        this.approver = approver;
        
        // Set default name based on approver
        if (approver != null) {
            setName("Approval by " + approver.getName());
        }
    }

    /**
     * Constructor with all main fields.
     * @param decision the decision requiring approval - must not be null
     * @param approver the user who needs to provide approval - must not be null
     * @param isRequired whether this approval is required
     * @param approvalPriority the priority level of this approval
     * @param dueDate the due date for the approval - can be null
     */
    public CDecisionApproval(final CDecision decision, final CUser approver, 
                           final boolean isRequired, final Integer approvalPriority,
                           final LocalDateTime dueDate) {
        this(decision, approver);
        this.isRequired = isRequired;
        this.approvalPriority = approvalPriority != null ? approvalPriority : 3;
        this.dueDate = dueDate;
    }

    // Getters and Setters

    public CDecision getDecision() {
        return decision;
    }

    public void setDecision(final CDecision decision) {
        this.decision = decision;
        updateLastModified();
    }

    public CUser getApprover() {
        return approver;
    }

    public void setApprover(final CUser approver) {
        this.approver = approver;
        // Update name to reflect new approver
        if (approver != null) {
            setName("Approval by " + approver.getName());
        }
        updateLastModified();
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(final Boolean isApproved) {
        this.isApproved = isApproved;
        if (isApproved != null) {
            this.approvalDate = LocalDateTime.now();
        }
        updateLastModified();
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(final LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
        updateLastModified();
    }

    public String getApprovalComments() {
        return approvalComments;
    }

    public void setApprovalComments(final String approvalComments) {
        this.approvalComments = approvalComments;
        updateLastModified();
    }

    public Integer getApprovalPriority() {
        return approvalPriority != null ? approvalPriority : 3;
    }

    public void setApprovalPriority(final Integer approvalPriority) {
        this.approvalPriority = approvalPriority != null ? approvalPriority : 3;
        updateLastModified();
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(final boolean isRequired) {
        this.isRequired = isRequired;
        updateLastModified();
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(final LocalDateTime dueDate) {
        this.dueDate = dueDate;
        updateLastModified();
    }

    // Business Logic Methods

    /**
     * Approves this approval with optional comments.
     * @param comments optional approval comments
     */
    public void approve(final String comments) {
        LOGGER.debug("approve called for approval: {} with comments: {}", getName(), comments);
        this.isApproved = true;
        this.approvalDate = LocalDateTime.now();
        this.approvalComments = comments;
        updateLastModified();
    }

    /**
     * Rejects this approval with optional comments.
     * @param comments optional rejection comments
     */
    public void reject(final String comments) {
        LOGGER.debug("reject called for approval: {} with comments: {}", getName(), comments);
        this.isApproved = false;
        this.approvalDate = LocalDateTime.now();
        this.approvalComments = comments;
        updateLastModified();
    }

    /**
     * Resets the approval to pending state.
     */
    public void resetToPending() {
        LOGGER.debug("resetToPending called for approval: {}", getName());
        this.isApproved = null;
        this.approvalDate = null;
        this.approvalComments = null;
        updateLastModified();
    }

    /**
     * Checks if this approval is pending (no decision made yet).
     * @return true if approval is pending
     */
    public boolean isPending() {
        return isApproved == null;
    }

    /**
     * Checks if this approval has been approved.
     * @return true if approved
     */
    public boolean isApproved() {
        return Boolean.TRUE.equals(isApproved);
    }

    /**
     * Checks if this approval has been rejected.
     * @return true if rejected
     */
    public boolean isRejected() {
        return Boolean.FALSE.equals(isApproved);
    }

    /**
     * Checks if this approval is overdue.
     * @return true if due date has passed and approval is still pending
     */
    public boolean isOverdue() {
        return dueDate != null && isPending() && LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * Gets the approval status as a readable string.
     * @return "Approved", "Rejected", or "Pending"
     */
    public String getApprovalStatusText() {
        if (isApproved()) {
            return "Approved";
        } else if (isRejected()) {
            return "Rejected";
        } else {
            return "Pending";
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getName() != null ? getName() : "CDecisionApproval");
        sb.append(" (").append(getApprovalStatusText()).append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CDecisionApproval)) {
            return false;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}