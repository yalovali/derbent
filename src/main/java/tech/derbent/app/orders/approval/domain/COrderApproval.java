package tech.derbent.app.orders.approval.domain;

import java.time.LocalDateTime;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.app.orders.order.domain.COrder;
import tech.derbent.base.users.domain.CUser;

@Entity
@Table (name = "corderapproval")
@AttributeOverride (name = "id", column = @Column (name = "order_approval_id"))
public class COrderApproval extends CEntityNamed<COrderApproval> {

	public static final String DEFAULT_COLOR = "#28a745";
	public static final String DEFAULT_ICON = "vaadin:check";
	public static final String ENTITY_TITLE_PLURAL = "Order Approvals";
	public static final String ENTITY_TITLE_SINGULAR = "Order Approval";
	public static final String VIEW_NAME = "Order Approval View";
	@Column (name = "approval_date", nullable = true)
	@AMetaData (
			displayName = "Approval Date", required = false, readOnly = false, description = "Date and time when approval was given or rejected",
			hidden = false
	)
	private LocalDateTime approvalDate;
	@Column (name = "approval_level", nullable = false)
	@AMetaData (
			displayName = "Approval Level", required = true, readOnly = false, defaultValue = "1",
			description = "Sequential approval level (1 = first approval, 2 = second, etc.)", hidden = false, min = 1, max = 10
	)
	private Integer approvalLevel = 1;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "approval_status_id", nullable = false)
	@AMetaData (
			displayName = "Status", required = true, readOnly = false, description = "Current approval status", hidden = false,
			dataProviderBean = "CApprovalStatusService"
	)
	private CApprovalStatus approvalStatus;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "approver_id", nullable = true)
	@AMetaData (
			displayName = "Approver", required = false, readOnly = false, description = "User responsible for this approval", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser approver;
	@Column (name = "comments", nullable = true, length = 1000)
	@Size (max = 1000)
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Approval comments or rejection reasons", hidden = false,
			maxLength = 1000
	)
	private String comments;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "order_id", nullable = false)
	@AMetaData (
			displayName = "Order", required = true, readOnly = false, description = "The order this approval belongs to", hidden = false,
			dataProviderBean = "COrderService"
	)
	private COrder order;

	/** Constructor with name and order.
	 * @param name  the name of this approval step
	 * @param order the order this approval belongs to */
	public COrderApproval(final String name) {
		super(COrderApproval.class, name);
	}

	public LocalDateTime getApprovalDate() { return approvalDate; }

	public Integer getApprovalLevel() { return approvalLevel; }

	public CApprovalStatus getApprovalStatus() { return approvalStatus; }

	public CUser getApprover() { return approver; }

	public String getComments() { return comments; }

	// Getters and setters
	public COrder getOrder() { return order; }

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
