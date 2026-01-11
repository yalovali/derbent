package tech.derbent.app.milestones.milestonetype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.companies.domain.CCompany;

@Entity
@Table (name = "cmilestonetype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cmilestonetype_id"))
public class CMilestoneType extends CTypeEntity<CMilestoneType> {

	public static final String DEFAULT_COLOR = "#4B4382"; // CDE Titlebar Purple - milestone types
	public static final String DEFAULT_ICON = "vaadin:flag";
	public static final String ENTITY_TITLE_PLURAL = "Milestone Types";
	public static final String ENTITY_TITLE_SINGULAR = "Milestone Type";
	public static final String VIEW_NAME = "Milestone Type Management";

	/** Default constructor for JPA. */
	public CMilestoneType() {
		super();
	}

	public CMilestoneType(final String name, final CCompany company) {
		super(CMilestoneType.class, name, company);
	}
}
