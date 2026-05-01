package tech.derbent.api.dashboard.dashboardprojecttype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table (name = "cdashboardprojecttype")
@AttributeOverride (name = "id", column = @Column (name = "dashboardprojecttype_id"))
public class CDashboardProjectType extends CTypeEntity<CDashboardProjectType> {

	public static final String DEFAULT_COLOR = "#009688";
	public static final String DEFAULT_ICON = "vaadin:dashboard";
	public static final String ENTITY_TITLE_PLURAL = "Dashboard Types";
	public static final String ENTITY_TITLE_SINGULAR = "Dashboard Type";
	public static final String VIEW_NAME = "Dashboard Types View";

	@Column (nullable = true, length = 500)
	@AMetaData (
			displayName = "Description", required = false, readOnly = false,
			description = "Description for this dashboard type", hidden = false, maxLength = 500
	)
	private String description;

	protected CDashboardProjectType() {}

	public CDashboardProjectType(final String name, final CCompany company) {
		super(CDashboardProjectType.class, name, company);
	}

	public String getDescription() { return description; }

	public void setDescription(final String description) {
		this.description = description;
		updateLastModified();
	}
}
