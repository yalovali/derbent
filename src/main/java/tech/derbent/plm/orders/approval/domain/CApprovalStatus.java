package tech.derbent.plm.orders.approval.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.domain.CStatus;

@Entity
@Table (name = "capprovalstatus")
@AttributeOverride (name = "id", column = @Column (name = "approval_status_id"))
public class CApprovalStatus extends CStatus<CApprovalStatus> {

	@SuppressWarnings ("hiding")
	public static final String DEFAULT_COLOR = "#A9A08B"; // OpenWindows Disabled Gray - status
	@SuppressWarnings ("hiding")
	public static final String DEFAULT_ICON = "vaadin:check";
	public static final String ENTITY_TITLE_PLURAL = "Approval Statuses";
	public static final String ENTITY_TITLE_SINGULAR = "Approval Status";
	public static final String VIEW_NAME = "Approval Status View";

	/** Default constructor for JPA. */
	public CApprovalStatus() {
		super();
		initializeDefaults();
	}

	public CApprovalStatus(final String name, final CCompany company) {
		super(CApprovalStatus.class, name, company);
		initializeDefaults();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		setColor(DEFAULT_COLOR);
	}
}
