package tech.derbent.app.sprints.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.companies.domain.CCompany;

/** CSprintType - Domain entity representing sprint types. Layer: Domain (MVC) Inherits from CEntityOfProject to provide project-aware type
 * functionality for sprints. */
@Entity
@Table(name = "csprinttype", uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames = {
		"name", "company_id"
}))
@AttributeOverride(name = "id", column = @Column(name = "csprinttype_id"))
public class CSprintType extends CTypeEntity<CSprintType> {

	public static final String DEFAULT_COLOR = "#8377C5"; // CDE Active Purple - sprint types
	public static final String DEFAULT_ICON = "vaadin:calendar-clock";
	public static final String ENTITY_TITLE_PLURAL = "Sprint Types";
	public static final String ENTITY_TITLE_SINGULAR = "Sprint Type";
	public static final String VIEW_NAME = "Sprint Types View";

	/** Default constructor for JPA. */
	public CSprintType() {
		super();
	}

	public CSprintType(final String name, final CCompany company) {
		super(CSprintType.class, name, company);
	}
}
