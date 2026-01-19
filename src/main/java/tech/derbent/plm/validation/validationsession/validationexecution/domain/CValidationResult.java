package tech.derbent.plm.validation.validationsession.validationexecution.domain;

/** Validation execution result status. */
public enum CValidationResult {
	/** Validation passed successfully. */
	PASSED,
	
	/** Validation failed. */
	FAILED,
	
	/** Validation was blocked due to preconditions not met. */
	BLOCKED,
	
	/** Validation was skipped. */
	SKIPPED,
	
	/** Validation is in progress. */
	IN_PROGRESS,
	
	/** Validation was not executed yet. */
	NOT_EXECUTED
}
