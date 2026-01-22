package tech.derbent.plm.validation.validationcasetype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.companies.domain.CCompany;

/** CValidationCaseType - Type entity for validation case categorization. */
@Entity
@Table (name = "cvalidationcasetype")
@AttributeOverride (name = "id", column = @Column (name = "validationcasetype_id"))
public class CValidationCaseType extends CTypeEntity<CValidationCaseType> {

	public static final String DEFAULT_COLOR = "#4169E1"; // RoyalBlue - testing
	public static final String DEFAULT_ICON = "vaadin:clipboard-check";
	public static final String ENTITY_TITLE_PLURAL = "Validation Case Types";
	public static final String ENTITY_TITLE_SINGULAR = "Validation Case Type";
	public static final String VIEW_NAME = "Validation Case Type View";

	/** Default constructor for JPA. */
	public CValidationCaseType() {
		super(CValidationCaseType.class, "New Validation Case Type", null);
		initializeDefaults();
	}

	public CValidationCaseType(final String name, final CCompany company) {
		super(CValidationCaseType.class, name, company);
		initializeDefaults();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		setColor(DEFAULT_COLOR);
	}
}
