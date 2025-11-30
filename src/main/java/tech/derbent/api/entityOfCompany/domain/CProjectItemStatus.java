package tech.derbent.api.entityOfCompany.domain;

import java.util.Objects;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.app.companies.domain.CCompany;

@Entity
@Table (name = "cprojectitemstatus", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cprojectitemstatus_id"))
public class CProjectItemStatus extends CStatus<CProjectItemStatus> {

	public static final String DEFAULT_COLOR = "#4966B0"; // OpenWindows Selection Blue - project item statuses
	public static final String DEFAULT_ICON = "vaadin:flag";
	public static final String ENTITY_TITLE_PLURAL = "Statuses";
	public static final String ENTITY_TITLE_SINGULAR = "Status";
	public static final String VIEW_NAME = "Activity Statuses View";
	@Column (name = "is_final", nullable = false)
	@AMetaData (
			displayName = "Is Final Status", required = true, readOnly = false, defaultValue = "false",
			description = "Indicates if this is a final status (completed/cancelled)", hidden = true
	)
	private Boolean finalStatus = Boolean.FALSE;

	/** Default constructor for JPA. */
	public CProjectItemStatus() {
		super();
		setColor(DEFAULT_COLOR);
		// Initialize with default values for JPA
		finalStatus = Boolean.FALSE;
	}

	public CProjectItemStatus(final String name, final CCompany company) {
		super(CProjectItemStatus.class, name, company);
		setColor(DEFAULT_COLOR);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CProjectItemStatus)) {
			return false;
		}
		return super.equals(o);
	}

	public Boolean getFinalStatus() { return finalStatus; }

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), finalStatus);
	}

	public void setFinalStatus(final Boolean finalStatus) { this.finalStatus = finalStatus; }

	@Override
	public String toString() {
		return getName() != null ? getName() : super.toString();
	}
}
