package tech.derbent.plm.meetings.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

/** CMeetingType - Domain entity representing meeting types. Layer: Domain (MVC) Inherits from CEntityOfProject to provide project-aware type
 * functionality for meetings. */
@Entity
@Table (name = "cmeetingtype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cmeetingtype_id"))
public class CMeetingType extends CTypeEntity<CMeetingType> {

	public static final String DEFAULT_COLOR = "#17a2b8";
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Meeting Types";
	public static final String ENTITY_TITLE_SINGULAR = "Meeting Type";
	public static final String VIEW_NAME = "Meeting Types View";

	/** Default constructor for JPA. */
	public CMeetingType() {
		super();
		initializeDefaults();
	}

	public CMeetingType(final String name, final CCompany company) {
		super(CMeetingType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
