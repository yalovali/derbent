package tech.derbent.orders.domain;

import java.time.LocalDateTime;

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
 * COrderApproval - Domain entity representing individual approval records for orders. Layer: Domain (MVC) Tracks
 * individual approval steps for orders, including the approver, status, date, and any comments. Each order can have
 * multiple approvals representing different approval levels or departments. This entity extends CEntityNamed to provide
 * approval step naming and description capabilities, with additional approval-specific metadata.
 */
@Entity
@Table(name = "corderapproval")
@AttributeOverride(name = "id", column = @Column(name = "order_approval_id"))
public class COrderApproval extends CEntityNamed<COrderApproval> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @MetaData(displayName = "Order", required = true, readOnly = false, description = "The order this approval belongs to", hidden = false, order = 2, dataProviderBean = "COrderService")
    private COrder order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approver_id", nullable = true)
    @MetaData(displayName = "Approver", required = false, readOnly = false, description = "User responsible for this approval", hidden = false, order = 3, dataProviderBean = "CUserService")
    private CUser approver;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approval_status_id", nullable = false)
    @MetaData(displayName = "Status", required = true, readOnly = false, description = "Current approval status", hidden = false, order = 4, dataProviderBean = "CApprovalStatusService")
    private CApprovalStatus approvalStatus;

    @Column(name = "approval_date", nullable = true)
    @MetaData(displayName = "Approval Date", required = false, readOnly = false, description = "Date and time when approval was given or rejected", hidden = false, order = 5)
    private LocalDateTime approvalDate;

    @Column(name = "comments", nullable = true, length = 1000)
    @Size(max = 1000)
    @MetaData(displayName = "Comments", required = false, readOnly = false, description = "Approval comments or rejection reasons", hidden = false, order = 6, maxLength = 1000)
    private String comments;

    @Column(name = "approval_level", nullable = false)
    @MetaData(displayName = "Approval Level", required = true, readOnly = false, defaultValue = "1", description = "Sequential approval level (1 = first approval, 2 = second, etc.)", hidden = false, order = 7, min = 1, max = 10)
    private Integer approvalLevel = 1;

    /**
     * Constructor with name and order.
     * 
     * @param name
     *            the name of this approval step
     * @param order
     *            the order this approval belongs to
     */
    public COrderApproval(final String name) {
        super(COrderApproval.class, name);
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public Integer getApprovalLevel() {
        return approvalLevel;
    }

    public CApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public CUser getApprover() {
        return approver;
    }

    public String getComments() {
        return comments;
    }

    // Getters and setters
    public COrder getOrder() {
        return order;
    }

    public void setApprovalDate(final LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
        updateLastModified();
    }

    public void setApprovalLevel(final Integer approvalLevel) {
        this.approvalLevel = approvalLevel;
        updateLastModified();
    }

    public void setApprovalStatus(final CApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
        updateLastModified();
    }

    public void setApprover(final CUser approver) {
        this.approver = approver;
        updateLastModified();
    }

    public void setComments(final String comments) {
        this.comments = comments;
        updateLastModified();
    }

    public void setOrder(final COrder order) {
        this.order = order;
        updateLastModified();
    }
}