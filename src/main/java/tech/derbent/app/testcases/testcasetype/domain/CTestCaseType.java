package tech.derbent.app.testcases.testcasetype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfCompany.domain.CCompany;

/** CTestCaseType - Type entity for test case categorization. */
@Entity
@Table (name = "ctestcasetype")
@AttributeOverride (name = "id", column = @Column (name = "testcasetype_id"))
public class CTestCaseType extends CTypeEntity {

	public static final String DEFAULT_COLOR = "#4169E1"; // RoyalBlue - testing
	public static final String DEFAULT_ICON = "vaadin:clipboard-check";
	public static final String ENTITY_TITLE_PLURAL = "Test Case Types";
	public static final String ENTITY_TITLE_SINGULAR = "Test Case Type";
	public static final String VIEW_NAME = "Test Case Type View";

	/** Default constructor for JPA. */
	public CTestCaseType() {
		super(CTestCaseType.class, "New Test Case Type", null);
	}

	public CTestCaseType(final String name, final CCompany company) {
		super(CTestCaseType.class, name, company);
	}
}
