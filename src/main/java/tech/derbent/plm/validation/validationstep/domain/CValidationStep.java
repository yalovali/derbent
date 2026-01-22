package tech.derbent.plm.validation.validationstep.domain;

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
import tech.derbent.plm.validation.validationcase.domain.CValidationCase;

/** CValidationStep - Entity representing individual validation steps within a validation case. Each validation step defines a specific action and
 * expected result. */
@Entity
@Table (name = "cvalidationstep")
@AttributeOverride (name = "id", column = @Column (name = "validationstep_id"))
public class CValidationStep extends CEntityDB<CValidationStep> {

	public static final String DEFAULT_COLOR = "#87CEEB"; // SkyBlue - validation steps
	public static final String DEFAULT_ICON = "vaadin:step-forward";
	public static final String ENTITY_TITLE_PLURAL = "Validation Steps";
	public static final String ENTITY_TITLE_SINGULAR = "Validation Step";
	public static final String VIEW_NAME = "Validation Steps View";
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Action", required = false, readOnly = false, description = "Action to perform in this step", hidden = false,
			maxLength = 2000
	)
	private String action;
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Expected Result", required = false, readOnly = false, description = "Expected outcome after performing action",
			hidden = false, maxLength = 2000
	)
	private String expectedResult;
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Notes", required = false, readOnly = false, description = "Additional notes for this step", hidden = false,
			maxLength = 2000
	)
	private String notes;
	@Column (name = "step_order", nullable = false)
	@Min (value = 1, message = "Step order must be at least 1")
	@AMetaData (
			displayName = "Step Order", required = true, readOnly = false, defaultValue = "1", description = "Execution order of this step",
			hidden = false
	)
	private Integer stepOrder = 1;
	@Column (nullable = true, length = 1000)
	@Size (max = 1000)
	@AMetaData (
			displayName = "Validation Data", required = false, readOnly = false, description = "Validation data to use in this step", hidden = false,
			maxLength = 1000
	)
	private String testData;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "validationcase_id", nullable = false)
	@AMetaData (
			displayName = "Validation Case", required = true, readOnly = false, description = "Parent validation case", hidden = false,
			dataProviderBean = "CValidationCaseService"
	)
	private CValidationCase validationCase;

	/** Default constructor for JPA. */
	public CValidationStep() {
		super();
		initializeDefaults();
	}

	public CValidationStep(final CValidationCase validationCase, final Integer stepOrder) {
		super();
		this.validationCase = validationCase;
		this.stepOrder = stepOrder;
		initializeDefaults();
	}

	public String getAction() { return action; }

	public String getExpectedResult() { return expectedResult; }

	public String getNotes() { return notes; }

	public Integer getStepOrder() { return stepOrder; }

	public String getTestData() { return testData; }

	public CValidationCase getValidationCase() { return validationCase; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		stepOrder = 1;
	}

	public void setAction(final String action) { this.action = action; }

	public void setExpectedResult(final String expectedResult) { this.expectedResult = expectedResult; }

	public void setNotes(final String notes) { this.notes = notes; }

	public void setStepOrder(final Integer stepOrder) { this.stepOrder = stepOrder; }

	public void setTestData(final String testData) { this.testData = testData; }

	public void setValidationCase(final CValidationCase validationCase) { this.validationCase = validationCase; }

	@Override
	public String toString() {
		return String.format("Step %d: %s", stepOrder, action != null ? action : "No action");
	}
}
