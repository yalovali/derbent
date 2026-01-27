package tech.derbent.plm.milestones.milestonetype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

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
										/** Default constructor for JPA. */
	protected CMilestoneType() {}

	public CMilestoneType(final String name, final CCompany company) {
		super(CMilestoneType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
