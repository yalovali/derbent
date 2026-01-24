package tech.derbent.plm.issues.issuetype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table (name = "cissuetype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cissuetype_id"))
public class CIssueType extends CTypeEntity<CIssueType> {

	public static final String DEFAULT_COLOR = "#D32F2F"; // Red for issues/bugs
	public static final String DEFAULT_ICON = "vaadin:bug";
	public static final String ENTITY_TITLE_PLURAL = "Issue Types";
	public static final String ENTITY_TITLE_SINGULAR = "Issue Type";
	public static final String VIEW_NAME = "Issue Type Management";

	/** Default constructor for JPA. */
	public CIssueType() {
		super();
		initializeDefaults();
	}

	public CIssueType(final String name, final CCompany company) {
		super(CIssueType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
