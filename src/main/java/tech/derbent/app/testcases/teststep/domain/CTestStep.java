package tech.derbent.app.testcases.teststep.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.app.testcases.testcase.domain.CTestCase;

/** CTestStep - Entity representing individual test steps within a test case.
 * Each test step defines a specific action and expected result. */
@Entity
@Table (name = "cteststep")
@AttributeOverride (name = "id", column = @Column (name = "teststep_id"))
public class CTestStep extends CEntityDB<CTestStep> {

	public static final String DEFAULT_COLOR = "#87CEEB"; // SkyBlue - test steps
	public static final String DEFAULT_ICON = "vaadin:step-forward";
	public static final String ENTITY_TITLE_PLURAL = "Test Steps";
	public static final String ENTITY_TITLE_SINGULAR = "Test Step";
	public static final String VIEW_NAME = "Test Steps View";

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "testcase_id", nullable = false)
	@AMetaData (
			displayName = "Test Case", required = true, readOnly = false,
			description = "Parent test case", hidden = false,
			dataProviderBean = "CTestCaseService"
	)
	private CTestCase testCase;

	@Column (name = "step_order", nullable = false)
	@Min (value = 1, message = "Step order must be at least 1")
	@AMetaData (
			displayName = "Step Order", required = true, readOnly = false, defaultValue = "1",
			description = "Execution order of this step", hidden = false
	)
	private Integer stepOrder = 1;

	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Action", required = false, readOnly = false,
			description = "Action to perform in this step", hidden = false, maxLength = 2000
	)
	private String action;

	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Expected Result", required = false, readOnly = false,
			description = "Expected outcome after performing action", hidden = false, maxLength = 2000
	)
	private String expectedResult;

	@Column (nullable = true, length = 1000)
	@Size (max = 1000)
	@AMetaData (
			displayName = "Test Data", required = false, readOnly = false,
			description = "Test data to use in this step", hidden = false, maxLength = 1000
	)
	private String testData;

	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Notes", required = false, readOnly = false,
			description = "Additional notes for this step", hidden = false, maxLength = 2000
	)
	private String notes;

	/** Default constructor for JPA. */
	public CTestStep() {
		super();
		initializeDefaults();
	}

	public CTestStep(final CTestCase testCase, final Integer stepOrder) {
		super();
		this.testCase = testCase;
		this.stepOrder = stepOrder;
		initializeDefaults();
	}

	protected void initializeDefaults() {
		super.initializeDefaults();
		if (stepOrder == null) {
			stepOrder = 1;
		}
	}

	public CTestCase getTestCase() { return testCase; }

	public void setTestCase(final CTestCase testCase) {
		this.testCase = testCase;
	}

	public Integer getStepOrder() { return stepOrder; }

	public void setStepOrder(final Integer stepOrder) {
		this.stepOrder = stepOrder;
	}

	public String getAction() { return action; }

	public void setAction(final String action) {
		this.action = action;
	}

	public String getExpectedResult() { return expectedResult; }

	public void setExpectedResult(final String expectedResult) {
		this.expectedResult = expectedResult;
	}

	public String getTestData() { return testData; }

	public void setTestData(final String testData) {
		this.testData = testData;
	}

	public String getNotes() { return notes; }

	public void setNotes(final String notes) {
		this.notes = notes;
	}

	@Override
	public String toString() {
		return String.format("Step %d: %s", stepOrder, action != null ? action : "No action");
	}
}
