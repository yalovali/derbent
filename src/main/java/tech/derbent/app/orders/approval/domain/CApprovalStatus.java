package tech.derbent.app.orders.approval.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.entityOfCompany.domain.CStatus;
import tech.derbent.app.companies.domain.CCompany;

@Entity
@Table (name = "capprovalstatus")
@AttributeOverride (name = "id", column = @Column (name = "approval_status_id"))
public class CApprovalStatus extends CStatus<CApprovalStatus> {

	public static final String DEFAULT_COLOR = "#28a745";
	public static final String DEFAULT_ICON = "vaadin:check";
	public static final String VIEW_NAME = "Approval Status View";

	/** Default constructor for JPA. */
	public CApprovalStatus() {
		super();
		setColor(DEFAULT_COLOR);
	}

	public CApprovalStatus(final String name, final CCompany company) {
		super(CApprovalStatus.class, name, company);
		setColor(DEFAULT_COLOR);
	}
}
