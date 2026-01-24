package tech.derbent.plm.activities.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table (name = "cactivitytype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cactivitytype_id"))
public class CActivityType extends CTypeEntity<CActivityType> {

	public static final String DEFAULT_COLOR = "#4966B0"; // OpenWindows Selection Blue - activity types
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Activity Types";
	public static final String ENTITY_TITLE_SINGULAR = "Activity Type";
	public static final String VIEW_NAME = "Activity Type Management";

	/** Default constructor for JPA. */
	protected CActivityType() {
		super();
	}

	public CActivityType(final String name, final CCompany company) {
		super(CActivityType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
