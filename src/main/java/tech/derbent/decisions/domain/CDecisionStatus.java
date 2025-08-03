package tech.derbent.decisions.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.base.domain.CStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CDecisionStatus - Domain entity representing decision status types. Layer: Domain (MVC) Inherits from CStatus to
 * provide status functionality for decisions. This entity defines the possible statuses a decision can have (e.g.,
 * DRAFT, UNDER_REVIEW, APPROVED, REJECTED, IMPLEMENTED).
 */
@Entity
@Table(name = "cdecisionstatus")
@AttributeOverride(name = "id", column = @Column(name = "decision_status_id"))
public class CDecisionStatus extends CStatus<CDecisionStatus> {

    @Column(name = "is_final", nullable = false)
    @MetaData(displayName = "Is Final Status", required = true, readOnly = false, defaultValue = "false", description = "Indicates if this is a final status (implemented/rejected)", hidden = false, order = 4)
    private Boolean isFinal = Boolean.FALSE;

    @Column(name = "requires_approval", nullable = false)
    @MetaData(displayName = "Requires Approval", required = true, readOnly = false, defaultValue = "false", description = "Whether decisions with this status require approval to proceed", hidden = false, order = 7)
    private Boolean requiresApproval = Boolean.FALSE;

    /**
     * Constructor with name and description.
     * 
     * @param name
     *            the name of the decision status - must not be null or empty
     * @param description
     *            detailed description of the decision status - can be null
     */
    public CDecisionStatus(final String name, final CProject project) {
        super(CDecisionStatus.class, name, project);
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof CDecisionStatus)) {
            return false;
        }
        return super.equals(o);
    }

    /**
     * Checks if this status indicates completion of the decision process.
     * 
     * @return true if this is a final status
     */
    public Boolean isCompleted() {
        return Boolean.TRUE.equals(isFinal);
    }

    public Boolean isFinal() {
        return isFinal;
    }

    /**
     * Checks if decisions with this status are pending approval.
     * 
     * @return true if approval is required and status is not final
     */
    public Boolean isPendingApproval() {
        return Boolean.TRUE.equals(requiresApproval) && !Boolean.TRUE.equals(isFinal);
    }

    public Boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setFinal(final Boolean isFinal) {
        this.isFinal = isFinal;
        updateLastModified();
    }

    public void setRequiresApproval(final Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
        updateLastModified();
    }

    @Override
    public String toString() {
        return getName() != null ? getName() : super.toString();
    }
}
