package tech.derbent.api.projects.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

/** CProjectType - Domain entity representing project types. Layer: Domain (MVC) Inherits from CTypeEntity to provide project-aware type functionality
 * for projects. */
@Entity
@Table (name = "cprojecttype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cprojecttype_id"))
public class CProjectType extends CTypeEntity<CProjectType> {

	public static final String DEFAULT_COLOR = "#6B5FA7"; // CDE Purple - organizational entity
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Project Types";
	public static final String ENTITY_TITLE_SINGULAR = "Project Type";
	public static final String VIEW_NAME = "Project Types View";

	/** Default constructor for JPA. */
	public CProjectType() {
		super();
		initializeDefaults();
	}

	public CProjectType(final String name, final CCompany company) {
		super(CProjectType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
